#!/usr/bin/env python3
"""Gera a página de boas-vindas do repositório F-Droid (index.html).

Lê o index-v2.json gerado pelo fdroidserver e monta uma página simples em
português com: a URL para cadastrar no app F-Droid, a lista de apps com
versão/tamanho e o link direto de download do APK.

Uso:
    python3 gerar-welcome.py <dir-do-repo> <arquivo-index.html> [prefixo-url]

    <dir-do-repo>   pasta com index-v2.json e os APKs
    <arquivo>       destino do index.html
    <prefixo-url>   prefixo das URLs de download (ex.: /fdroid/repo); vazio = raiz
"""
import html
import json
import os
import sys

CANONICAL = "https://cleberleonheart-maker.github.io/seguraturbo-fdroid"
SOURCE = "https://github.com/cleberleonheart-maker/Seguroturbo"


def human_size(n):
    if not n:
        return "-"
    for unit in ("B", "KB", "MB"):
        if n < 1024:
            return f"{n:.0f} {unit}" if unit == "B" else f"{n:.1f} {unit}"
        n /= 1024.0
    return f"{n:.1f} GB"


def apps(index_v2):
    """Lista de apps, do mais novo para o mais antigo."""
    out = []
    for pkg, data in index_v2.get("packages", {}).items():
        meta = data.get("metadata", {})
        versions = data.get("versions", {})
        if not versions:
            continue
        latest = max(versions.values(), key=lambda v: v.get("manifest", {}).get("versionCode", 0))
        manifest = latest.get("manifest", {})
        name = meta.get("name", {}).get("en-US", pkg)
        icon = meta.get("icon", {}).get("en-US", {}).get("name")
        out.append(
            {
                "package": pkg,
                "name": name,
                "summary": meta.get("summary", {}).get("en-US", ""),
                "version": manifest.get("versionName", "-"),
                "versionCode": manifest.get("versionCode", 0),
                "size": latest.get("file", {}).get("size", 0),
                "apk": latest.get("file", {}).get("name", ""),
                "icon": icon,
                "license": meta.get("license", ""),
                "source": meta.get("sourceCode", ""),
            }
        )
    return sorted(out, key=lambda a: a["name"].lower())


def render(apps_list):
    cards = []
    for a in apps_list:
        # no index-v2 os nomes de arquivo vêm com "/" na frente; o link é
        # montado a partir de {PREFIX} (vazio na raiz, "/fdroid/repo/" etc.)
        apk_nome = a["apk"].lstrip("/")
        icone_nome = (a["icon"] or "").lstrip("/")
        icon = (
            f'<img class="icone" src="{{PREFIX}}{html.escape(icone_nome)}" alt="" width="64" height="64">'
            if icone_nome
            else '<div class="icone vazio" aria-hidden="true"></div>'
        )
        apk_link = (
            f'<a class="btn" href="{{PREFIX}}{html.escape(apk_nome)}">Baixar APK</a>'
            if apk_nome
            else ""
        )
        source = (
            f'<a class="link" href="{html.escape(a["source"])}">código-fonte</a>'
            if a["source"]
            else ""
        )
        cards.append(
            f"""    <article class="card">
      {icon}
      <div class="info">
        <h2>{html.escape(a["name"])}</h2>
        <p class="summary">{html.escape(a["summary"])}</p>
        <p class="meta">versão {html.escape(str(a["version"]))}
           · {human_size(a["size"])}
           · {html.escape(a["license"])}
           · {source}</p>
      </div>
      <div class="acoes">{apk_link}</div>
    </article>"""
        )

    return f"""<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Apps do Cleber — repositório F-Droid</title>
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
  h1 {{ font-size:1.7rem; margin:0 0 .3rem; letter-spacing:-.02em; }}
  .sub {{ color:var(--suave); margin:0 0 2rem; }}
  .box {{ background:var(--card); border:1px solid var(--borda); border-radius:14px;
          padding:1.1rem 1.2rem; margin:0 0 1.6rem; }}
  .box h3 {{ margin:0 0 .6rem; font-size:1rem; }}
  .url {{ display:block; word-break:break-all; background:var(--bg); border:1px solid var(--borda);
          border-radius:9px; padding:.7rem .8rem; font:14px/1.4 ui-monospace,SFMono-Regular,Menlo,monospace; }}
  ol {{ margin:.6rem 0 0; padding-left:1.3rem; }}
  li {{ margin:.25rem 0; }}
  .card {{ display:flex; gap:1rem; align-items:flex-start; background:var(--card);
           border:1px solid var(--borda); border-radius:14px; padding:1.1rem 1.2rem; margin:0 0 1rem; }}
  .icone {{ width:64px; height:64px; border-radius:16px; flex:0 0 64px; }}
  .icone.vazio {{ background:var(--borda); }}
  .info {{ flex:1 1 auto; min-width:0; }}
  .info h2 {{ margin:0 0 .2rem; font-size:1.1rem; }}
  .summary {{ margin:0 0 .35rem; color:var(--suave); font-size:.92rem; }}
  .meta {{ margin:0; color:var(--suave); font-size:.82rem; }}
  .link {{ color:var(--destaque); }}
  .acoes {{ flex:0 0 auto; }}
  .btn {{ display:inline-block; background:var(--destaque); color:#fff; text-decoration:none;
          padding:.55rem .9rem; border-radius:9px; font-size:.88rem; font-weight:600; }}
  footer {{ color:var(--suave); font-size:.83rem; margin-top:2rem; }}
  footer a {{ color:var(--destaque); }}
  code {{ background:var(--bg); border:1px solid var(--borda); border-radius:5px; padding:.05rem .3rem; }}
</style>
</head>
<body>
<main>
  <h1>Apps do Cleber</h1>
  <p class="sub">Repositório F-Droid · código aberto (GPL-3.0) · sem anúncios, sem rastreamento</p>

  <div class="box">
    <h3>Adicionar no app F-Droid</h3>
    <span class="url">{CANONICAL}</span>
    <ol>
      <li>Abra o F-Droid e vá em <strong>Configurações → Repositórios</strong>.</li>
      <li>Toque em <strong>Adicionar repositório</strong> e cole a URL acima.</li>
      <li>Pronto — os apps aparecem na lista, na aba <strong>Atualizar</strong>.</li>
    </ol>
  </div>

  <div class="box">
    <h3>Instalação manual</h3>
    <p style="margin:.2rem 0 0">Dá para baixar o APK direto abaixo e instalar
    pelo gerenciador de arquivos. No Android, é preciso permitir
    <em>instalar apps de fontes desconhecidas</em> para o navegador ou o
    gerenciador de arquivos que você usar.</p>
  </div>

{chr(10).join(cards)}

  <footer>
    Código-fonte: <a href="{SOURCE}">github.com/cleberleonheart-maker/Seguroturbo</a> ·
    Índice assinado pelo fdroidserver · <code>index.xml</code> /
    <code>index-v2.json</code> · o mesmo repositório também está em
    <code>/fdroid/repo</code>.
  </footer>
</main>
</body>
</html>
"""


def main():
    if len(sys.argv) < 3:
        print(__doc__, file=sys.stderr)
        return 1
    repo_dir, out = sys.argv[1], sys.argv[2]
    prefix = sys.argv[3] if len(sys.argv) > 3 else ""
    # {PREFIX} entra colado no nome do arquivo: vazio na raiz (link relativo)
    # ou com uma barra no fim (/fdroid/repo/). Sem a barra, o link da raiz
    # virava "/app.apk" e caía no domínio, fora da pasta do projeto (404).
    prefix = "/" + prefix.strip("/") + "/" if prefix.strip("/") else ""

    with open(os.path.join(repo_dir, "index-v2.json"), encoding="utf-8") as f:
        index_v2 = json.load(f)

    html_out = render(apps(index_v2)).replace("{PREFIX}", prefix)
    with open(out, "w", encoding="utf-8") as f:
        f.write(html_out)
    print(f"==> pagina de boas-vindas gerada: {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
