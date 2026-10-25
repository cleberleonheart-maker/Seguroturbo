#!/usr/bin/env python3
"""Gera a página de changelog do repositório F-Droid.

Lê os arquivos fastlane/metadata/android/*/changelogs/*.txt e monta um
changelog.html simples em português, agrupado por versão (versionCode).

Uso:
    python3 gerar-changelog.py <dir-fastlane-metadata> <arquivo-changelog.html> [prefixo-url]
"""
import html
import os
import sys
from pathlib import Path

SOURCE = "https://github.com/cleberleonheart-maker/Seguroturbo"
CANONICAL = "https://cleberleonheart-maker.github.io/seguraturbo-fdroid"


def ler_changelogs(metadata_dir):
    """Retorna lista de (versionCode, linhas_ptbr, linhas_en) ordenada desc."""
    pt_dir = os.path.join(metadata_dir, "pt-BR", "changelogs")
    en_dir = os.path.join(metadata_dir, "en-US", "changelogs")
    es_dir = os.path.join(metadata_dir, "es-ES", "changelogs")
    fr_dir = os.path.join(metadata_dir, "fr-FR", "changelogs")

    versoes = set()
    for d in [pt_dir, en_dir, es_dir, fr_dir]:
        if not os.path.isdir(d):
            continue
        for f in os.listdir(d):
            if f.endswith(".txt"):
                try:
                    versoes.add(int(f.replace(".txt", "")))
                except ValueError:
                    pass

    itens = []
    for v in sorted(versoes, reverse=True):
        def _ler(p):
            try:
                with open(p, encoding="utf-8") as fh:
                    return [l.rstrip() for l in fh if l.strip()]
            except FileNotFoundError:
                return []

        linhas_pt = _ler(os.path.join(pt_dir, f"{v}.txt"))
        linhas_en = _ler(os.path.join(en_dir, f"{v}.txt"))
        linhas_es = _ler(os.path.join(es_dir, f"{v}.txt"))
        linhas_fr = _ler(os.path.join(fr_dir, f"{v}.txt"))
        if not linhas_pt and not linhas_en:
            continue
        itens.append({
            "vcode": v,
            "pt": linhas_pt,
            "en": linhas_en,
            "es": linhas_es,
            "fr": linhas_fr,
        })
    return itens


def render(itens, prefix):
    blocos = []
    for it in itens:
        vcode = it["vcode"]
        # usa pt-BR como principal
        linhas = it["pt"] or it["en"] or it["es"] or it["fr"]
        if not linhas:
            continue
        lis = "\n".join(f"        <li>{html.escape(l.lstrip('- ').strip())}</li>" for l in linhas)
        blocos.append(f"""    <section class="card">
      <header class="card-head">
        <h2>Versão <span class="vcode">{vcode}</span></h2>
      </header>
      <ul class="lista">
{lis}
      </ul>
    </section>""")

    if not blocos:
        blocos.append('    <p class="vazio">Nenhum changelog encontrado.</p>')

    return f"""<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Changelog — Galinha Assada &amp; SeguroTurbo</title>
<style>
  :root {{ color-scheme: light dark; --bg:#f6f6f4; --fg:#1c1b1a; --card:#fff;
           --borda:#dcdcd6; --destaque:#0a7d55; --suave:#6b6b66; }}
  @media (prefers-color-scheme: dark) {{
    :root {{ --bg:#141413; --fg:#ecebe7; --card:#1e1e1c; --borda:#33332f;
             --destaque:#4ed09a; --suave:#9c9c94; }}
  }}
  * {{ box-sizing:border-box; }}
  body {{ margin:0; padding:2.2rem 1.2rem 3rem; background:var(--bg); color:var(--fg);
          font:16px/1.55 system-ui,-apple-system,"Segoe UI",Roboto,sans-serif; }}
  main {{ max-width:44rem; margin:0 auto; }}
  header {{ margin:0 0 2rem; }}
  h1 {{ font-size:1.9rem; margin:0 0 .3rem; letter-spacing:-.02em; }}
  .sub {{ color:var(--suave); margin:0; }}
  nav {{ margin:1rem 0 0; display:flex; gap:1rem; flex-wrap:wrap; }}
  nav a {{ color:var(--destaque); text-decoration:none; }}
  nav a:hover {{ text-decoration:underline; }}
  .card {{ background:var(--card); border:1px solid var(--borda); border-radius:14px;
           margin:0 0 1.2rem; overflow:hidden; }}
  .card-head {{ padding:1rem 1.2rem .6rem; display:flex; align-items:baseline; gap:.8rem; }}
  .card-head h2 {{ margin:0; font-size:1.1rem; }}
  .vcode {{ display:inline-block; padding:.1rem .5rem; border-radius:999px;
            border:1px solid var(--borda); color:var(--suave); font-size:.9rem; }}
  .lista {{ margin:0; padding:0 1.2rem 1.1rem 2.2rem; display:grid; gap:.4rem; }}
  .lista li {{ line-height:1.6; }}
  .vazio {{ color:var(--suave); }}
  footer {{ margin:2.4rem 0 0; color:var(--suave); font-size:.95rem; }}
  footer a {{ color:var(--destaque); text-decoration:none; }}
  footer a:hover {{ text-decoration:underline; }}
</style>
</head>
<body>
<main>
  <header>
    <h1>Changelog</h1>
    <p class="sub">Galinha Assada &amp; SeguroTurbo — histórico de versões</p>
    <nav>
      <a href="{CANONICAL}/">← Voltar ao repositório F-Droid</a>
      <a href="{SOURCE}">Código-fonte</a>
    </nav>
  </header>

{chr(10).join(blocos)}

  <footer>
    Gerado a partir dos metadados Fastlane · <a href="{SOURCE}">github.com/cleberleonheart-maker/Seguroturbo</a>
  </footer>
</main>
</body>
</html>
""".replace("{PREFIX}", prefix)


def main():
    if len(sys.argv) < 3:
        print(__doc__, file=sys.stderr)
        return 1
    metadata_dir = sys.argv[1]
    out = sys.argv[2]
    prefix = sys.argv[3] if len(sys.argv) > 3 else ""
    if prefix and not prefix.startswith("/"):
        prefix = "/" + prefix
    prefix = prefix.rstrip("/")

    itens = ler_changelogs(metadata_dir)
    html_out = render(itens, prefix)
    os.makedirs(os.path.dirname(os.path.abspath(out)), exist_ok=True)
    with open(out, "w", encoding="utf-8") as f:
        f.write(html_out)
    print(f"==> changelog gerado: {out} ({len(itens)} versões)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
