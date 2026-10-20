#!/usr/bin/env bash
# Gera um repositório F-Droid próprio a partir dos APKs assinados do SeguroTurbo.
# Requer fdroidserver instalado (pip install fdroidserver).
# Veja docs/F-DROID.md (Caminho 2).
set -euo pipefail

PASTA="${1:-$HOME/fdroid-repo}"
AQUI="$(cd "$(dirname "$0")" && pwd)"

if ! command -v fdroid >/dev/null 2>&1; then
	echo "fdroidserver não encontrado. Instale com: pip install fdroidserver" >&2
	exit 1
fi

mkdir -p "$PASTA"
cd "$PASTA"

if [ ! -f keystore.jks ]; then
	echo "Gerando keystore do repositório (keystore.jks)..."
	SENHA="$(head -c 24 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 24)"
	keytool -genkey -v -keystore keystore.jks -alias seguraturbo \
		-keyalg RSA -keysize 2048 -validity 10000 \
		-storepass "$SENHA" -keypass "$SENHA" \
		-dname "CN=SeguroTurbo, OU=SeguroTurbo, O=SeguroTurbo, C=BR"
	printf '%s' "$SENHA" > .keystore-pass
	chmod 600 .keystore-pass
	echo "Senha do keystore salva em $PASTA/.keystore-pass (não versione este arquivo)."
fi

if [ ! -f config.yml ]; then
	SENHA="$(cat .keystore-pass)"
	sed -e "s|^keystorepass:.*|keystorepass: $SENHA|" \
	    -e "s|^keypass:.*|keypass: $SENHA|" \
	    "$AQUI/repo-config.example.yml" > config.yml
	echo "config.yml criado. Ajuste repo_url antes de publicar."
fi

chmod 600 config.yml

if [ ! -d metadata ]; then
	echo "Copiando metadados..."
	mkdir -p metadata
	cp "$AQUI/com.seguraturbo.app.yml" metadata/com.seguraturbo.app.yml
fi

if [ ! -f icon.png ]; then
	cp "$AQUI/../fastlane/metadata/android/pt-BR/images/icon.png" icon.png 2>/dev/null \
		|| echo "Aviso: ícone do repositório não encontrado (icon.png)."
fi

echo "Coloque os APKs assinados (*.apk) nesta pasta e pressione Enter."
read -r _

fdroid update --create-metadata
fdroid update -c

echo "Pronto. Publique a pasta '$PASTA' como site estático."
echo "URL do repositório para o F-Droid: (veja repo_url em $PASTA/config.yml)"
