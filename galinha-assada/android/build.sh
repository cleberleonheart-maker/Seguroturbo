#!/usr/bin/env bash
# Compila o APK do Galinha Assada sem Gradle (aapt2 + javac + d8 + apksigner).
set -euo pipefail

SDK="${ANDROID_HOME:-$HOME/android-sdk}"
BT="$(ls -d "$SDK"/build-tools/*/ 2>/dev/null | sort -V | tail -1)"
BT="${BT%/}"
AJ="$SDK/platforms/android-34/android.jar"
# se android-34 nao estiver instalado, usa a plataforma mais nova disponivel
if [ ! -f "$AJ" ]; then
  PLAT="$(ls -d "$SDK"/platforms/android-* 2>/dev/null | sort -V | tail -1)"
  AJ="$PLAT/android.jar"
fi
PROJ="$(cd "$(dirname "$0")" && pwd)"
OUT="$PROJ/../apk"
KS="$PROJ/galinha.keystore"
# senha do keystore: KS_PASS no CI, padrao local
KS_PASS="${KS_PASS:-galinha123}"
GAME="$(cd "$PROJ/.." && pwd)/index.html"

# versao automatica pela data (versionCode cresce a cada build do dia)
# pode ser sobrescrita com VC_OVERRIDE / VN_OVERRIDE para varios builds no mesmo dia
VC="${VC_OVERRIDE:-$(date +%y%m%d)}"
VN="${VN_OVERRIDE:-1.$(date +%y).$(date +%m).$(date +%d)}"

# o versionCode tem que ser >= o do APK anterior, senao o Android recusa a atualizacao
PREV_APK="$OUT/galinha-assada.apk"
if [ -f "$PREV_APK" ]; then
  PREV_VC=$("$BT/aapt2" dump badging "$PREV_APK" 2>/dev/null | sed -n "s/.*versionCode='\([0-9]*\)'.*/\1/p")
  if [ -n "$PREV_VC" ] && [ "$PREV_VC" -ge "$VC" ]; then
    echo "==> versionCode $VC ja usado; subindo para $((PREV_VC + 1))"
    VC=$((PREV_VC + 1))
  fi
fi

[ -f "$AJ" ] || { echo "android.jar nao encontrado: $AJ"; exit 1; }

rm -rf "$PROJ/build"; mkdir -p "$PROJ/build"/{res,classes,dex,gen} "$OUT"

echo "==> copiando o jogo para os assets"
mkdir -p "$PROJ/assets"
cp "$GAME" "$PROJ/assets/index.html"

echo "==> aapt2 compile (resources)"
"$BT/aapt2" compile --dir "$PROJ/res" -o "$PROJ/build/res/resources.zip"

echo "==> aapt2 link (manifest + resources)"
"$BT/aapt2" link \
  -o "$PROJ/build/base.apk" \
  -I "$AJ" \
  --manifest "$PROJ/AndroidManifest.xml" \
  -R "$PROJ/build/res/resources.zip" \
  --java "$PROJ/build/gen" \
  --min-sdk-version 21 \
  --target-sdk-version 34 \
  --version-code $VC \
  --version-name $VN \
  --auto-add-overlay

echo "==> javac (java + R.java)"
find "$PROJ/java" "$PROJ/build/gen" -name '*.java' > "$PROJ/build/sources.txt"
javac -nowarn -encoding UTF-8 \
  -source 8 -target 8 \
  -bootclasspath "$AJ" \
  -classpath "$AJ" \
  -d "$PROJ/build/classes" \
  @"$PROJ/build/sources.txt" 2>&1 | grep -v 'bootstrap class path\|source value 8\|target value 8\|deprecat' || true

echo "==> d8 (classes.dex)"
find "$PROJ/build/classes" -name '*.class' > "$PROJ/build/classes.txt"
"$BT/d8" --lib "$AJ" --min-api 21 --release \
  --output "$PROJ/build/dex" @"$PROJ/build/classes.txt"

echo "==> empacotando assets + dex"
cp "$PROJ/build/base.apk" "$PROJ/build/unsigned.apk"
( cd "$PROJ/build/dex" && zip -q -X "$PROJ/build/unsigned.apk" classes.dex )
( cd "$PROJ" && zip -q -X -r "$PROJ/build/unsigned.apk" assets )

echo "==> conferindo o que entrou no pacote"
unzip -l "$PROJ/build/unsigned.apk" > "$PROJ/build/pack.txt"
grep -q 'assets/index\.html' "$PROJ/build/pack.txt" \
  || { echo "ERRO: assets/index.html nao entrou no APK"; exit 1; }

echo "==> zipalign"
"$BT/zipalign" -f -p 4 "$PROJ/build/unsigned.apk" "$PROJ/build/aligned.apk"

if [ ! -f "$KS" ]; then
  echo "==> gerando keystore"
  keytool -genkeypair -v -keystore "$KS" -storepass "$KS_PASS" \
    -keypass "$KS_PASS" -alias galinha -keyalg RSA -keysize 2048 -validity 10950 \
    -dname "CN=Galinha Assada, OU=Jogo, O=Galinha, L=Brasil, S=BR, C=BR" >/dev/null 2>&1
fi

echo "==> apksigner"
"$BT/apksigner" sign \
  --ks "$KS" --ks-pass "pass:$KS_PASS" --key-pass "pass:$KS_PASS" \
  --v1-signing-enabled true --v2-signing-enabled true \
  --out "$PROJ/build/final.apk" "$PROJ/build/aligned.apk"

# assina em arquivo temporario e so substitui no final, para ninguem copiar um APK pela metade
mv -f "$PROJ/build/final.apk" "$OUT/galinha-assada.apk"
# idsig de build anterior nao serve mais para este APK e confunde o instalador
rm -f "$OUT/galinha-assada.apk.idsig"

echo "==> verificando"
"$BT/apksigner" verify --print-certs "$OUT/galinha-assada.apk" | head -4
"$BT/aapt2" dump badging "$OUT/galinha-assada.apk" | head -6
ls -la "$OUT/galinha-assada.apk"
echo "OK -> $OUT/galinha-assada.apk"