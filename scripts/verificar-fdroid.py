#!/usr/bin/env python3
"""Verifica a integridade de um repositório F-Droid (publicado ou local).

Checa, para cada versão no índice:

  1. `entry.json` existe e o `sha256` do índice confere;
  2. `entry.jar` (assinatura do índice) é válido;
  3. o APK existe, o `sha256` bate com o índice;
  4. a assinatura do APK bate com a registrada no índice;
  5. `versionName`/`versionCode` do APK batem com o índice.

Uso:
    ./scripts/verificar-fdroid.py                     # repo publicado
    ./scripts/verificar-fdroid.py --url <URL>         # outro repo
    ./scripts/verificar-fdroid.py --dir ~/fdroid-repo/repo
    ./scripts/verificar-fdroid.py --apenas-ultima     # só a versão mais recente

Requisitos: python3, apksigner e jarsigner no PATH (Android SDK + JDK).
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import urllib.request

PADRAO = "https://cleberleonheart-maker.github.io/seguraturbo-fdroid/repo"


class Falha(Exception):
    pass


def baixar(url: str, destino: str) -> None:
    with urllib.request.urlopen(url, timeout=60) as r, open(destino, "wb") as f:
        shutil.copyfileobj(r, f)


def sha256(caminho: str) -> str:
    h = hashlib.sha256()
    with open(caminho, "rb") as f:
        for bloco in iter(lambda: f.read(1 << 20), b""):
            h.update(bloco)
    return h.hexdigest()


def assinatura_apk(caminho: str) -> str:
    saida = subprocess.run(
        ["apksigner", "verify", "--print-certs", caminho],
        capture_output=True, text=True, check=False,
    ).stdout
    m = re.search(r"SHA-256 digest: ([0-9a-f]{64})", saida)
    if not m:
        raise Falha(f"não foi possível ler a assinatura de {os.path.basename(caminho)}")
    return m.group(1)


def badging(caminho: str) -> tuple[str, int]:
    saida = subprocess.run(
        ["aapt", "dump", "badging", caminho],
        capture_output=True, text=True, check=False,
    ).stdout
    nome = re.search(r"versionName='([^']*)'", saida)
    codigo = re.search(r"versionCode='(\d+)'", saida)
    if not nome or not codigo:
        raise Falha(f"não foi possível ler a versão de {os.path.basename(caminho)}")
    return nome.group(1), int(codigo.group(1))


def verificar_jar(entry_jar: str) -> str:
    r = subprocess.run(
        ["jarsigner", "-verify", entry_jar],
        capture_output=True, text=True, check=False,
    )
    if "jar verified" not in (r.stdout + r.stderr):
        raise Falha("entry.jar não passou na verificação de assinatura")
    cert = subprocess.run(
        ["keytool", "-printcert", "-jarfile", entry_jar],
        capture_output=True, text=True, check=False,
    ).stdout
    m = re.search(r"SHA256: ((?:[0-9A-F]{2}:){31}[0-9A-F]{2})", cert)
    return m.group(1) if m else "(desconhecida)"


def main() -> int:
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--url", default=None, help=f"URL do repo (padrão: {PADRAO})")
    p.add_argument("--dir", default=None, help="diretório local do repo (alternativo a --url)")
    p.add_argument("--apenas-ultima", action="store_true", help="verifica só a versão mais recente de cada app")
    args = p.parse_args()

    temp = None
    try:
        if args.dir:
            base = os.path.abspath(args.dir)
            if not os.path.isdir(base):
                raise Falha(f"diretório inexistente: {base}")
            origem = base

            def obter(nome: str) -> str:
                return os.path.join(base, nome)
        else:
            url = (args.url or PADRAO).rstrip("/")
            temp = tempfile.mkdtemp(prefix="fdroid-verify-")
            origem = url

            def obter(nome: str) -> str:
                destino = os.path.join(temp, os.path.basename(nome))
                baixar(f"{url}/{nome.lstrip('/')}", destino)
                return destino

        print(f"Repositório: {origem}\n")

        # 1. entry.json -> índice
        entrada = json.load(open(obter("entry.json"), encoding="utf-8"))
        indice_path = obter(entrada["index"]["name"].lstrip("/"))
        real = sha256(indice_path)
        if real != entrada["index"]["sha256"]:
            raise Falha("sha256 do índice não bate com o entry.json")
        print(f"✓ entry.json      índice íntegro ({real[:16]}…, {entrada['index']['size']} bytes)")

        # 2. assinatura do índice
        jar = obter("entry.jar")
        chave_repo = verificar_jar(jar)
        print(f"✓ entry.jar       assinado por {chave_repo}")

        indice = json.load(open(indice_path, encoding="utf-8"))
        pacotes = indice.get("packages", {})
        if not pacotes:
            raise Falha("índice sem pacotes")
        print(f"✓ índice          {len(pacotes)} pacote(s): {', '.join(sorted(pacotes))}\n")

        problemas: list[str] = []
        total = 0

        for nome_pkg, dados in sorted(pacotes.items()):
            versoes = sorted(
                dados["versions"].items(),
                key=lambda kv: kv[1]["manifest"].get("versionCode", 0),
            )
            if args.apenas_ultima:
                versoes = versoes[-1:]

            print(f"  {nome_pkg}")
            for _, v in versoes:
                total += 1
                arquivo = v["file"]["name"].lstrip("/")
                rotulo = f"    {v['manifest'].get('versionName','?'):>10} ({v['manifest'].get('versionCode','?')})"
                try:
                    caminho = obter(arquivo)
                    if sha256(caminho) != v["file"]["sha256"]:
                        raise Falha("sha256 do APK não bate com o índice")
                    if assinatura_apk(caminho) != v["manifest"]["signer"]["sha256"][0]:
                        raise Falha("assinatura do APK não bate com o índice")
                    vn, vc = badging(caminho)
                    if (vn, vc) != (v["manifest"]["versionName"], v["manifest"]["versionCode"]):
                        raise Falha(f"versão do APK ({vn}/{vc}) difere do índice")
                    print(f"{rotulo}  ✓")
                except Falha as e:
                    print(f"{rotulo}  ✗ {e}")
                    problemas.append(f"{nome_pkg} {v['manifest'].get('versionCode')}: {e}")

            if not args.apenas_ultima:
                print()

        print(f"\n{total} versão(ões) verificada(s).")
        if problemas:
            print(f"\n{len(problemas)} problema(s):")
            for item in problemas:
                print(f"  - {item}")
            return 1
        print("Tudo certo: índice, assinaturas, hashes e versões conferem.")
        return 0

    except Falha as e:
        print(f"✗ {e}", file=sys.stderr)
        return 1
    except urllib.error.URLError as e:
        print(f"✗ falha de rede: {e}", file=sys.stderr)
        return 1
    finally:
        if temp:
            shutil.rmtree(temp, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
