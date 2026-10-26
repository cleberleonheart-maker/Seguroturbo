#!/usr/bin/env bash
#
# Baixa e confere os APKs dos apps parceiros (Pulsa, Bússola...).
#
# Um app é "parceiro" quando o metadata em fdroid/<pacote>.yml tem o campo
# `Binaries:` apontando para o APK publicado pelo próprio app. Este script baixa
# esse APK e confere, contra o metadata:
#   - o nome do pacote;
#   - o versionCode e o versionName;
#   - a chave de assinatura (AllowedAPKSigningKeys).
# Se qualquer coisa divergir, sai com erro: é melhor não publicar do que
# publicar um APK trocado.
#
# Uso:
#   ./scripts/baixar-parceiros.sh <destino>
#
# Saída: <destino>/<pacote>_<versionCode>.apk (mesmo padrão dos outros APKs do
# repositório, usado pela retenção do Pages).
#
set -euo pipefail

DEST="${1:-}"
[ -n "$DEST" ] || { echo "uso: $0 <destino>" >&2; exit 1; }

PROJ_DIR="$(cd "$(dirname "$0")/.." && pwd)"

# ---- ferramentas do Android SDK ---------------------------------------------
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/android-sdk}}"
achar_bin() {
    local nome="$1" caminho
    if [ -d "$SDK/build-tools" ]; then
        caminho="$(ls -d "$SDK"/build-tools/*/ 2>/dev/null | sort -V | tail -1)"
        [ -n "$caminho" ] && [ -x "$caminho$nome" ] && { echo "$caminho$nome"; return 0; }
    fi
    command -v "$nome" || true
}

AAPT2="$(achar_bin aapt2)"
APKSIGNER="$(achar_bin apksigner)"
[ -n "$AAPT2" ] || { echo "aapt2 não encontrado (Android SDK build-tools)" >&2; exit 1; }
[ -n "$APKSIGNER" ] || { echo "apksigner não encontrado (Android SDK build-tools)" >&2; exit 1; }

mkdir -p "$DEST"

encontrou=0
for MET in "$PROJ_DIR"/fdroid/*.yml; do
    grep -qE '^Binaries:[[:space:]]*http' "$MET" || continue
    encontrou=1

    PKG="$(basename "$MET" .yml)"
    URL="$(sed -n 's/^Binaries:[[:space:]]*//p' "$MET" | head -n1)"
    VC_ESPERADO="$(sed -n 's/^CurrentVersionCode:[[:space:]]*//p' "$MET" | head -n1 | tr -d '[:space:]')"
    VN_ESPERADO="$(sed -n 's/^CurrentVersion:[[:space:]]*//p' "$MET" | head -n1 | tr -d '[:space:]')"
    CHAVE_ESPERADA="$(sed -n 's/^AllowedAPKSigningKeys:[[:space:]]*//p' "$MET" | head -n1 | tr -d '[:space:]')"
    [ -n "$URL" ] && [ -n "$VC_ESPERADO" ] && [ -n "$VN_ESPERADO" ] && [ -n "$CHAVE_ESPERADA" ] || {
        echo "erro: metadata incompleto em $MET" >&2
        echo "      precisa de Binaries, CurrentVersion, CurrentVersionCode e AllowedAPKSigningKeys." >&2
        exit 1
    }

    echo "==> $PKG <- $URL"
    curl -fsSL --retry 3 --retry-delay 5 -o "$DEST/$PKG.apk" "$URL"

    BADGING="$("$AAPT2" dump badging "$DEST/$PKG.apk")"
    PKG_APK="$(sed -n "s/^package: name='\([^']*\)'.*/\1/p" <<<"$BADGING" | head -n1)"
    VC="$(sed -n "s/.*versionCode='\([0-9]*\)'.*/\1/p" <<<"$BADGING" | head -n1)"
    VN="$(sed -n "s/.*versionName='\([^']*\)'.*/\1/p" <<<"$BADGING" | head -n1)"

    [ "$PKG_APK" = "$PKG" ] || { echo "erro: $MET baixou o pacote $PKG_APK" >&2; exit 1; }
    [ "$VC" = "$VC_ESPERADO" ] || {
        echo "erro: $PKG saiu com versionCode $VC, mas o metadata diz $VC_ESPERADO" >&2
        exit 1
    }
    [ "$VN" = "$VN_ESPERADO" ] || {
        echo "erro: $PKG saiu com versionName $VN, mas o metadata diz $VN_ESPERADO" >&2
        exit 1
    }

    CHAVE="$("$APKSIGNER" verify --print-certs "$DEST/$PKG.apk" \
        | sed -n 's/.*SHA-256 digest: //p' | head -n1)"
    [ "$CHAVE" = "$CHAVE_ESPERADA" ] || {
        echo "erro: assinatura de $PKG não confere" >&2
        echo "      esperado: $CHAVE_ESPERADA" >&2
        echo "      no APK:   $CHAVE" >&2
        exit 1
    }

    mv "$DEST/$PKG.apk" "$DEST/${PKG}_${VC}.apk"
    echo "$PKG $VN ($VC) — pacote, versão e assinatura conferem"
done

if [ "$encontrou" = 0 ]; then
    echo "nenhum metadata com Binaries: em $PROJ_DIR/fdroid" >&2
    exit 1
fi
