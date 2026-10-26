#!/usr/bin/env python3
"""Remove versões antigas de APK das pastas publicadas no GitHub Pages.

O repositório F-Droid aponta sempre para as versões mais recentes, mas o Pages
acumula uma cópia de cada versão em três caminhos (raiz, `/fdroid/repo` e
`/repo`). Esta limpeza mantém apenas as N versões mais novas de cada app em cada
caminho — e nunca remove um APK que o índice daquele caminho referencia.

Cada app é identificado pelo nome do arquivo `<pacote>_<versionCode>.apk`; APKs
com outro padrão de nome são ignorados (nunca apagados por engano).

Uso:
    ./fdroid/limpar-pages.py pages [N]        # N padrão: 5
"""

from __future__ import annotations

import glob
import json
import os
import re
import sys

CAMINHOS = ("", "fdroid/repo", "repo")
PADRAO_APK = re.compile(r"^(.+)_(\d+)\.apk$")


def apps_por_pacote(pasta: str) -> dict[str, list[tuple[int, str]]]:
    """Agrupa os APKs da pasta por pacote: {'com.exemplo': [(code, caminho)]}."""
    grupos: dict[str, list[tuple[int, str]]] = {}
    for caminho in glob.glob(os.path.join(pasta, "*.apk")):
        m = PADRAO_APK.match(os.path.basename(caminho))
        if m:
            grupos.setdefault(m.group(1), []).append((int(m.group(2)), caminho))
    return grupos


def main() -> int:
    base = sys.argv[1] if len(sys.argv) > 1 else "pages"
    manter_n = int(sys.argv[2]) if len(sys.argv) > 2 else 5

    if not os.path.isdir(base):
        print(f"diretório inexistente: {base}", file=sys.stderr)
        return 1

    removidos = 0
    for sub in CAMINHOS:
        pasta = os.path.join(base, sub)
        if not os.path.isdir(pasta):
            continue

        referenciados: set[str] = set()
        indice = os.path.join(pasta, "index-v2.json")
        if os.path.exists(indice):
            try:
                dados = json.load(open(indice, encoding="utf-8"))
            except json.JSONDecodeError:
                dados = {}
            for pacote in dados.get("packages", {}).values():
                for versao in pacote.get("versions", {}).values():
                    referenciados.add(os.path.basename(versao["file"]["name"]))

        for pacote, arquivos in apps_por_pacote(pasta).items():
            if len(arquivos) <= manter_n:
                continue

            arquivos.sort(reverse=True)
            manter = {c for _, c in arquivos[:manter_n]}
            manter |= {os.path.join(pasta, n) for n in referenciados}

            for _, caminho in arquivos:
                if caminho not in manter:
                    os.remove(caminho)
                    removidos += 1
                    print(f"removido: {os.path.relpath(caminho, base)}")

    print(f"{removidos} APK(s) antigo(s) removido(s) (mantendo {manter_n} por app/caminho).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
