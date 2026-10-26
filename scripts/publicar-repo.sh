#!/usr/bin/env bash
#
# Publica uma release nova no repositório F-Droid próprio do SeguroTurbo,
# de ponta a ponta e com um comando só:
#
#   1. baixa o APK oficial da release (tag do GitHub)
#   2. coloca em ~/fdroid-repo/repo/ com o nome esperado (app_id_versionCode.apk)
#   3. regenera o índice assinado (fdroid update -c)
#   4. publica a pasta repo/ no repositório do GitHub Pages
#
# Uso:
#   ./scripts/publicar-repo.sh [tag]
#
#   tag  opcional (ex.: v1.29). Se omitida, usa a última release do GitHub.
#
# Requisitos: gh (autenticado), fdroidserver, git.
#
# A senha do keystore do repositório é lida de ~/fdroid-repo/.keystore-pass
# (ou das variáveis FDROID_KEY_STORE_PASS / FDROID_KEY_PASS).
#
set -euo pipefail

REPO_DIR="${FDROID_REPO_DIR:-$HOME/fdroid-repo}"
PAGES_DIR="${FDROID_PAGES_DIR:-$HOME/seguraturbo-fdroid}"
PAGES_REPO="cleberleonheart-maker/seguraturbo-fdroid"
GITHUB_REPO="cleberleonheart-maker/Seguroturbo"
PACOTE="com.seguraturbo.app"
TAG="${1:-}"

log() { printf '==> %s\n' "$*"; }
erro() { printf 'ERRO: %s\n' "$*" >&2; exit 1; }

# ---- pré-checagens -----------------------------------------------------------

command -v gh >/dev/null 2>&1 || erro "gh não encontrado (instale o GitHub CLI)."
command -v git >/dev/null 2>&1 || erro "git não encontrado."

# O fdroidserver do sistema (2.2.1 + androguard 3.4) falha ao ler os APKs do
# Galinha ("ResParserError: res1 must be zero!"). Se o venv com androguard 4.x
# existir, ele tem prioridade; dá para forçar com FDROID_BIN=/caminho/fdroid.
if [ -z "${FDROID_BIN:-}" ]; then
    if [ -x "$HOME/.venvs/fdroid/bin/fdroid" ]; then
        FDROID_BIN="$HOME/.venvs/fdroid/bin/fdroid"
    elif command -v fdroid >/dev/null 2>&1; then
        FDROID_BIN="$(command -v fdroid)"
    else
        erro "fdroidserver não encontrado (instale com pip ou crie ~/.venvs/fdroid)."
    fi
fi
log "fdroidserver: $FDROID_BIN"

[ -d "$REPO_DIR" ] || erro "pasta do repositório F-Droid não existe: $REPO_DIR"

# senha do keystore do repositório
if [ -z "${FDROID_KEY_STORE_PASS:-}" ]; then
    [ -f "$REPO_DIR/.keystore-pass" ] || \
        erro "senha do repo não encontrada. Defina FDROID_KEY_STORE_PASS ou crie $REPO_DIR/.keystore-pass"
    export FDROID_KEY_STORE_PASS="$(cat "$REPO_DIR/.keystore-pass")"
fi
export FDROID_KEY_PASS="${FDROID_KEY_PASS:-$FDROID_KEY_STORE_PASS}"

# ---- 1. descobrir a release e o versionCode ---------------------------------

if [ -z "$TAG" ]; then
    TAG="$(gh release view --repo "$GITHUB_REPO" --json tagName -q .tagName)"
    log "Usando a última release: $TAG"
else
    log "Usando a tag informada: $TAG"
fi

APK_URL="$(gh release view "$TAG" --repo "$GITHUB_REPO" --json assets \
    -q ".assets[] | select(.name | test(\"$PACOTE|SeguroTurbo-.*[^-debug].apk$\")) | select(.name | test(\"debug\") | not) | .url" \
    | head -n1)"

if [ -z "$APK_URL" ]; then
    APK_URL="$(gh release view "$TAG" --repo "$GITHUB_REPO" --json assets \
        -q ".assets[] | select(.name | test(\"\\\\.apk$\")) | select(.name | test(\"debug\") | not) | .url" \
        | head -n1)"
fi
[ -n "$APK_URL" ] || erro "nenhum APK encontrado na release $TAG."

TMP_APK="$(mktemp -t seguraturbo-XXXXXX.apk)"
log "Baixando APK da release $TAG..."
gh release download "$TAG" --repo "$GITHUB_REPO" --clobber \
    -p "$(basename "${APK_URL##*/}")" -O "$TMP_APK" 2>/dev/null \
    || curl -fsSL "$APK_URL" -o "$TMP_APK"

# descobre versionCode/versionName direto do APK (via aapt se existir; senão, do metadata)
AAPT="$(command -v aapt || command -v aapt2 || true)"
if [ -n "$AAPT" ]; then
    BADGING="$("$AAPT" dump badging "$TMP_APK" 2>/dev/null || true)"
    VCODE="$(printf '%s\n' "$BADGING" | sed -n "s/.*versionCode='\([0-9]*\)'.*/\1/p" | head -n1)"
fi
if [ -z "${VCODE:-}" ]; then
    VCODE="$(sed -n "s/^CurrentVersionCode: *\([0-9]*\).*/\1/p" "$REPO_DIR/metadata/$PACOTE.yml" | head -n1)"
fi
[ -n "${VCODE:-}" ] || erro "não foi possível determinar o versionCode."

APK_NAME="${PACOTE}_${VCODE}.apk"
log "versionCode detectado: $VCODE  ->  $APK_NAME"

# ---- 2. copiar para o repo ---------------------------------------------------

mkdir -p "$REPO_DIR/repo"
rm -f "$REPO_DIR/repo/${PACOTE}"_*.apk
cp "$TMP_APK" "$REPO_DIR/repo/$APK_NAME"
rm -f "$TMP_APK"

# atualiza metadata do fdroiddata, se existir
PROJ_DIR="$(cd "$(dirname "$0")/.." && pwd)"
if [ -f "$PROJ_DIR/fdroid/$PACOTE.yml" ]; then
    mkdir -p "$REPO_DIR/metadata"
    cp "$PROJ_DIR/fdroid/$PACOTE.yml" "$REPO_DIR/metadata/$PACOTE.yml"
fi

# lista de categorias válidas (usada pelo `fdroid lint`; veja docs/F-DROID.md)
mkdir -p "$REPO_DIR/config"
cp "$PROJ_DIR/fdroid/categories.yml" "$REPO_DIR/config/categories.yml"

# apps parceiros (metadata com campo Binaries). O script baixa o APK da
# release de cada um e confere versão e assinatura; aqui só trocamos a versão
# antiga pela nova, para o índice não acumular versões velhas.
PARCEIROS_TMP="$(mktemp -d -t seguraturbo-parceiros-XXXXXX)"
trap 'rm -rf "$PARCEIROS_TMP"' EXIT
"$PROJ_DIR/scripts/baixar-parceiros.sh" "$PARCEIROS_TMP"
# metadata de verdade = fdroid/<pacote>.yml; os arquivos de configuração
# (categories.yml, repo-config.example.yml) não entram em metadata/, senão o
# fdroidserver tenta lê-los como se fossem apps.
for MET in "$PROJ_DIR"/fdroid/*.yml; do
    case "$(basename "$MET")" in
        categories.yml | repo-config.example.yml) continue ;;
    esac
    cp "$MET" "$REPO_DIR/metadata/"
done
# ícones no layout do fdroidserver (metadata/<pacote>/<locale>/images/icon.png)
if [ -d "$PROJ_DIR/fdroid/metadata" ]; then
    cp -r "$PROJ_DIR/fdroid/metadata/." "$REPO_DIR/metadata/"
fi
for APK_PARCEIRO in "$PARCEIROS_TMP"/*.apk; do
    PKG_PARCEIRO="$(basename "$APK_PARCEIRO" | sed 's/_[0-9]*\.apk$//')"
    rm -f "$REPO_DIR/repo/${PKG_PARCEIRO}"_*.apk
    cp "$APK_PARCEIRO" "$REPO_DIR/repo/"
    log "parceiro: $(basename "$APK_PARCEIRO")"
done

# ---- 3. regenerar o índice assinado -----------------------------------------

log "Regenerando o índice assinado..."
( cd "$REPO_DIR" && "$FDROID_BIN" update --create-metadata && "$FDROID_BIN" update -c )

NOVO_HASH="$(sha256sum "$REPO_DIR/repo/$APK_NAME" | cut -d' ' -f1)"
log "APK publicado: $APK_NAME (sha256 ${NOVO_HASH:0:12}…)"

# ---- 4. publicar no GitHub Pages --------------------------------------------

if [ ! -d "$PAGES_DIR/.git" ]; then
    log "Clonando $PAGES_REPO em $PAGES_DIR..."
    gh repo clone "$PAGES_REPO" "$PAGES_DIR"
fi

log "Sincronizando a pasta repo/ com o GitHub Pages..."
git -C "$PAGES_DIR" pull --ff-only --quiet
# publica o mesmo repo em tres caminhos, para que qualquer URL que o app
# F-Droid tente funcionar (raiz, /fdroid/repo e o /repo antigo)
# '--filter=protect *.apk' mantem as versoes antigas no ar (os links ja
# enviados continuam baixando); a retencao abaixo corta o excesso.
rsync -a --delete --filter='protect *.apk' \
      --exclude='/fdroid' --exclude='/repo' \
      --exclude='/.nojekyll' --exclude='/README.md' --exclude='/.git' \
      "$REPO_DIR/repo/" "$PAGES_DIR/"
rsync -a --delete --filter='protect *.apk' "$REPO_DIR/repo/" "$PAGES_DIR/fdroid/repo/"
rsync -a --delete --filter='protect *.apk' "$REPO_DIR/repo/" "$PAGES_DIR/repo/"
# retencao: so as 5 versoes mais novas de cada app em cada caminho
python3 "$PROJ_DIR/fdroid/limpar-pages.py" "$PAGES_DIR" 5
# pagina de boas-vindas (troca o index.html gerado pelo fdroidserver)
python3 "$PROJ_DIR/fdroid/gerar-welcome.py" "$REPO_DIR/repo" "$PAGES_DIR/index.html" ""
python3 "$PROJ_DIR/fdroid/gerar-welcome.py" "$REPO_DIR/repo" "$PAGES_DIR/fdroid/repo/index.html" "/fdroid/repo"
python3 "$PROJ_DIR/fdroid/gerar-welcome.py" "$REPO_DIR/repo" "$PAGES_DIR/repo/index.html" "/repo"

git -C "$PAGES_DIR" add -A
if git -C "$PAGES_DIR" diff --cached --quiet; then
    log "Nada mudou no repositório F-Droid. Nada a publicar."
else
    git -C "$PAGES_DIR" -c user.name="cleberleonheart-maker" \
        -c user.email="cleberleonheart-maker@users.noreply.github.com" \
        commit -q -m "repo: publica $APK_NAME (${TAG})"
    git -C "$PAGES_DIR" push -q origin main
    log "Publicado no GitHub Pages."
fi

log "Pronto. URL: $(grep -m1 '^repo_url:' "$REPO_DIR/config.yml" | cut -d' ' -f2)"
