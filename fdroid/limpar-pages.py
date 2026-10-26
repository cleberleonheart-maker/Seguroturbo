#!/usr/bin/env python3
"""Remove versões antigas de APK das pastas publicadas no GitHub Pages.

O repositório F-Droid aponta sempre para a versão mais recente, mas o Pages
acumula uma cópia de cada versão em três caminhos (raiz, `/fdroid/repo` e
`/repo`). Esta limpeza mantém apenas as N versões mais novas de cada app em cada
caminho — e nunca remove um APK que o índice daquele caminho referencia.

Uso:
    ./fdroid/limpar-pages.py pages [N]        # N padrão: 5
"""

from __future__ import annotations

import glob
import json
import os
import re
import sys

PACOTES = ("com.seguraturbo.app", "br.com.galinhaassada")
CAMINHOS = ("", "fdroid/repo", "repo")


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

        for pacote in PACOTES:
            arquivos: list[tuple[int, str]] = []
            for caminho in glob.glob(os.path.join(pasta, f"{pacote}_*.apk")):
                m = re.search(r"_(\d+)\.apk$", caminho)
                if m:
                    arquivos.append((int(m.group(1)), caminho))

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
