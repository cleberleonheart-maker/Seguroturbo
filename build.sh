#!/usr/bin/env bash
#
# Build do SeguroTurbo com proteção contra OOM no R8.
#
# Por que duas passadas?
#   A compilação Kotlin roda "in-process" (dentro do JVM do Gradle) para economizar
#   memória numa máquina pequena. Ao terminar, esse JVM fica cheio de lixo do compilador
#   e o R8 (que vem logo depois) pode estourar a memória e ser morto pelo kernel.
#   Rodando de novo, o cache do Gradle pula a compilação e o R8 roda num JVM limpo.
#
# Uso:
#   ./build.sh                 # release + debug, e copia os APKs para o pendrive
#   ./build.sh /caminho/saida  # define outra pasta de destino dos APKs
#
set -uo pipefail
cd "$(dirname "$0")"

VERSAO="1.26"
DESTINO="${1:-/mnt/chromeos/removable/UsbStorage/SeguroTurbo-APKs}"

limpar_daemons() {
    pkill -f '[K]otlinCompileDaemon' 2>/dev/null || true
    pkill -f '[G]radleDaemon' 2>/dev/null || true
    sleep 2
}

build_com_retry() {
    local tarefa="$1"
    limpar_daemons
    echo ">>> $tarefa (1ª passada)"
    if ./gradlew "$tarefa" --no-daemon; then
        return 0
    fi
    echo ">>> 1ª passada falhou (provável OOM no R8). Repetindo com o cache quente..."
    limpar_daemons
    echo ">>> $tarefa (2ª passada)"
    ./gradlew "$tarefa" --no-daemon
}

echo "== SeguroTurbo $VERSAO — build =="
build_com_retry assembleRelease || { echo "Falha no release"; exit 1; }
build_com_retry assembleDebug   || { echo "Falha no debug";   exit 1; }

REL="app/build/outputs/apk/release/app-release.apk"
DBG="app/build/outputs/apk/debug/app-debug.apk"
cp "$REL" "SeguroTurbo-v${VERSAO}.apk"
cp "$DBG" "SeguroTurbo-v${VERSAO}-debug.apk"

mkdir -p "$DESTINO"
cp "SeguroTurbo-v${VERSAO}.apk" "SeguroTurbo-v${VERSAO}-debug.apk" "$DESTINO/"

limpar_daemons
echo "== Concluído =="
echo "APKs na pasta do projeto e em: $DESTINO"
ls -la "SeguroTurbo-v${VERSAO}.apk" "SeguroTurbo-v${VERSAO}-debug.apk"
