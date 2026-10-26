# Publicar uma release (app → GitHub → F-Droid próprio)

O fluxo tem três pontos que precisam ficar **na mesma versão**:

| Onde | Campo | Exemplo |
| ---- | ----- | ------- |
| `app/build.gradle` | `versionCode` / `versionName` | `40` / `"1.36.1"` |
| tag no Git | nome da tag | `v1.36.1` |
| `fdroid/com.seguraturbo.app.yml` | `Builds` + `CurrentVersion(Code)` | `40` / `1.36.1` |

Se um deles sair do lugar, o `fdroiddata` recusa (`Repo git commit not found` ou
"versão não encontrada") e o repo próprio publica uma versão trocada. Para
evitar isso, o workflow **Publicar repo F-Droid** compara o `build.gradle` (app)
e o `version.properties` (Galinha) com o metadata em `fdroid/` e **falha** com
uma mensagem clara quando não batem — antes de compilar qualquer coisa.

---

## 1. Antes do commit

- [ ] `app/build.gradle`: `versionCode` +1 e `versionName` novo
- [ ] Changelog nos **4 idiomas**: `fastlane/metadata/android/{pt-BR,en-US,es-ES,fr-FR}/changelogs/<versionCode>.txt`
- [ ] `fdroid/com.seguraturbo.app.yml`: nova entrada em `Builds` + `CurrentVersion`/`CurrentVersionCode`
- [ ] Galinha Assada (se mudou): `galinha-assada/` atualizado **e** `galinha-assada/android/version.properties` com `versionCode` novo (sempre maior que o anterior) + `versionName`. Sem isso, o CI republica a mesma versão — de propósito.
- [ ] `./gradlew test lintDebug assembleDebug` ou só esperar o CI

## 2. Tag e release

```bash
git commit -am "release: vX.Y (versionCode N)"
git tag -a vX.Y -m "vX.Y"
git push origin main --tags
```

O workflow **Release** (`release.yml`) compila `assembleRelease`,
`assembleDebug` e `bundleRelease`, e publica na release do GitHub:
`SeguroTurbo-vX.Y.apk`, `-debug.apk` e `.aab`.

## 3. Repositório F-Droid (automático)

O workflow **Publicar repo F-Droid** (`publicar-repo-fdroid.yml`) dispara
sozinho quando o *Release* termina com sucesso (`workflow_run`):

1. baixa o APK oficial da release
2. monta o repo em `$RUNNER_TEMP` (metadados de `fdroid/`)
3. compila o Galinha Assada com a versão de `galinha-assada/android/version.properties` e inclui no mesmo repo
4. roda `fdroid update --create-metadata` + `fdroid update -c` (índice assinado)
5. publica em `pages/`, `pages/fdroid/repo/` e `pages/repo/`
6. gera `index.html` de boas-vindas e `changelog.html`
7. roda `fdroid/limpar-pages.py pages 5` — mantém as **5 versões mais novas**
   de cada app em cada caminho e nunca remove um APK citado no índice

Também dá para rodar na mão: **Actions → Publicar repo F-Droid → Run workflow**
(informando a tag) ou, localmente, `./scripts/publicar-repo.sh`.

### Conferir depois de publicar

```bash
./scripts/verificar-fdroid.py                 # repo publicado, todas as versões
./scripts/verificar-fdroid.py --apenas-ultima # só a mais recente (rápido)
./scripts/verificar-fdroid.py --dir ~/fdroid-repo/repo   # índice local
```

O script checa: `entry.json` × `index-v2.json` (sha256), assinatura do
`entry.jar`, existência/hash de cada APK, chave que assinou cada APK e
`versionName`/`versionCode`. Sai com código 1 se algo divergir.

## 4. Publicação local (só quando precisar)

`scripts/publicar-repo.sh` faz o mesmo caminho usando `gh` + `fdroidserver`
locais. Ele **prefere** `~/.venvs/fdroid/bin/fdroid` (ou `FDROID_BIN=...`) e
aplica a mesma retenção de 5 versões do CI.

**Atenção:** o `fdroidserver` do sistema (2.2.1 + androguard 3.4) falha ao ler
os APKs do Galinha:

```
androguard...ResParserError: res1 must be zero!
```

Por isso o venv com `androguard` 4.x é o caminho seguro:

```bash
~/.venvs/fdroid/bin/fdroid update -c          # dentro de ~/fdroid-repo
# ou
export PATH="$HOME/.venvs/fdroid/bin:$PATH"
```

Para recriar o venv:

```bash
python3 -m venv --without-pip ~/.venvs/fdroid
curl -fsSL https://bootstrap.pypa.io/get-pip.py | ~/.venvs/fdroid/bin/python
~/.venvs/fdroid/bin/pip install fdroidserver
```

### Cuidado com o diretório do repo

`fdroid update` indexa **tudo** que estiver em `repo/`. Arquivos gerados como
`repo/changelog.html` viram um pacote fantasma no índice. Antes de rodar
manualmente:

```bash
rm -f ~/fdroid-repo/repo/changelog.html
rm -f ~/fdroid-repo/metadata/*.yml.bak
```

O `repo/` deve conter só APKs + artefatos do fdroidserver (`index*.json`,
`entry.*`, `icons*`, `diff/`, `status/`).

## 5. Regras de segurança

- A senha do keystore do repo fica em `~/fdroid-repo/.keystore-pass` (modo `600`)
  e nos secrets do GitHub (`FDROID_REPO_KEYSTORE_*`). Nunca versione.
- `AllowedAPKSigningKeys` (em `fdroid/*.yml`) faz o `fdroid update` **ignorar**
  APK assinado com chave diferente. Para limpar os inválidos:
  `fdroid update --delete-unknown`.
- `keystore.properties`, `seguraturbo.keystore` e `CREDENCIAIS-KEYSTORE.txt`
  estão no `.gitignore` e com permissão `600` na máquina.

## 6. F-Droid oficial (mais tarde)

Só a partir de **16/01/2027** (política dos 3 meses). Passo a passo e texto do
MR em [`docs/MR-fdroiddata.md`](./MR-fdroiddata.md); lembrete na issue
[#1](https://github.com/cleberleonheart-maker/Seguroturbo/issues/1).
