# Como publicar no F-Droid

O F-Droid **não aceita apps pagos** — é um repositório exclusivo de software livre
gratuito. Ele compila o APK a partir do seu código-fonte (não publica builds seus)
e exige uma licença livre. O projeto já está preparado:

- `LICENSE` — GPL-3.0 (exigência obrigatória)
- `fastlane/metadata/android/` — descrições e changelogs em pt-BR, en-US, es-ES e fr-FR

## Situação da submissão

O merge request no repositório oficial **foi recusado**:

- Issue no RFP — <https://gitlab.com/fdroid/rfp/-/work_items/4599>
- Merge request no `fdroiddata` —
  <https://gitlab.com/fdroid/fdroiddata/-/merge_requests/53157> (**Closed**)

Motivos dados pela revisão:

1. **Template do MR não seguido** — o pedido precisa usar o template oficial de
   "New app" do `fdroiddata` (seções obrigatórias no corpo do MR).
2. **Política dos 3 meses** — apps novos precisam estar **mantidos por mais de
   3 meses** antes de entrar no repositório oficial.

Nada disso impede o app de funcionar. Há dois caminhos a seguir.

## Caminho 1 — reabrir para o F-Droid oficial (mais tarde)

Só vale depois de cumprir a política dos 3 meses, mantendo o repositório ativo
(commits regulares, releases).

> **Data-limite:** o primeiro commit do projeto é de **16/10/2026**, então o MR
> oficial só pode ser reaberto a partir de **16/01/2027**. Anote o lembrete.

Quando estiver pronto:

1. Faça fork de <https://gitlab.com/fdroid/fdroiddata>
2. Crie `metadata/com.seguraturbo.app.yml` com o conteúdo da seção
   ["YAML final do MR"](./MR-fdroiddata.md#3-yaml-final-do-mr-copiar-como-metadatacomseguraturboappyyml)
   — só a versão mais recente, sem `Summary`/`Description` e com
   `AllowedAPKSigningKeys`. O rascunho completo do corpo do MR está em
   [`docs/MR-fdroiddata.md`](./MR-fdroiddata.md).
3. Abra um merge request **usando o template oficial** (não apague as seções
   obrigatórias — foi esse o erro do MR anterior). O template exige a checklist
   de inclusão preenchida no corpo do MR.
4. Responda às perguntas dos revisores.

> **Atenção:** a cada release nova, atualize no YAML o `versionName`,
> `versionCode`, `commit` (hash do tag, ex.: `git rev-parse v1.29`),
> `CurrentVersion` e `CurrentVersionCode` — sempre batendo com o
> `versionCode`/`versionName` do `app/build.gradle`.

O aviso automático `insecure-gradlew` já foi resolvido na v1.27.2
(`distributionSha256Sum` no Gradle Wrapper).

## Caminho 2 — repositório F-Droid próprio (funciona já)

Como a aprovação oficial depende de tempo, você pode publicar seu **próprio
repositório F-Droid** e os usuários o adicionam como repositório alternativo.
Funciona imediatamente, sem revisão.

### O que é

Um repositório F-Droid é um site estático com:

- `index-v1.jar` / `index-v1.json` — o índice de apps
- `icons/` — os ícones
- os APKs assinados

O app [`fdroidserver`](https://f-droid.org/docs/) gera tudo isso a partir de um
`config.yml` apontando para os APKs já publicados nas releases do GitHub.

### Passo a passo

1. Instale o `fdroidserver`:

   ```bash
   pip install fdroidserver
   ```

2. Prepare uma pasta de trabalho e copie a config de exemplo:

   ```bash
   mkdir -p ~/fdroid-repo && cd ~/fdroid-repo
   cp /caminho/SeguroTurboApp/fdroid/repo-config.yml config.yml
   ```

3. Coloque os APKs assinados (das releases do GitHub) na pasta e registre:

   ```bash
   fdroid update --create-metadata
   fdroid update -c
   ```

4. Publique a pasta como site (GitHub Pages, Codeberg Pages, servidor próprio).
   Os usuários adicionam a URL em **F-Droid → Configurações → Repositórios**.

   O repositório precisa ficar num caminho que termine em `/fdroid/repo`, porque
   é esse que o cliente do F-Droid procura. Publicando a pasta `repo/` dentro de
   um diretório `fdroid/`, a URL que o usuário digita é a raiz do site:

   ```
   https://cleberleonheart-maker.github.io/seguraturbo-fdroid
   ```

   O cliente acrescenta `/fdroid/repo` sozinho. Se você digitar a URL completa
   com `/repo` no final, ele vai procurar `/repo/fdroid/repo` e vai avisar que o
   endereço do repositório está malformado.

5. A cada release nova: coloque o novo APK na pasta e rode `fdroid update` de novo.

### Publicação automatizada

O script [`scripts/publicar-repo.sh`](../scripts/publicar-repo.sh) faz o ciclo
inteiro de uma release nova num comando:

```bash
./scripts/publicar-repo.sh          # usa a última release do GitHub
./scripts/publicar-repo.sh v1.30    # ou uma tag específica
```

Ele: baixa o APK oficial da release, salva em `~/fdroid-repo/repo/` com o nome
`com.seguraturbo.app_<versionCode>.apk`, atualiza o metadata de todos os apps de
`fdroid/` (incluindo os parceiros, baixados e conferidos por
`scripts/baixar-parceiros.sh`), regenera o índice assinado e publica a pasta
`repo/` no repositório do GitHub Pages (`cleberleonheart-maker/seguraturbo-fdroid`).

Requisitos: `gh` autenticado, `fdroidserver`, `git` e `rsync`. A senha do keystore
do repositório é lida de `~/fdroid-repo/.keystore-pass`.

Variáveis opcionais: `FDROID_REPO_DIR` (padrão `~/fdroid-repo`) e
`FDROID_PAGES_DIR` (padrão `~/seguraturbo-fdroid`).

#### Automático no CI (recomendado)

O workflow [`.github/workflows/publicar-repo-fdroid.yml`](../.github/workflows/publicar-repo-fdroid.yml)
faz o mesmo ciclo sozinho: dispara logo após o workflow **Release** terminar com
sucesso (ou manualmente em **Actions → Publicar repo F-Droid → Run workflow**),
baixa o APK da release, regenera o índice assinado e publica no GitHub Pages. Não
é preciso rodar nada na sua máquina.

Ele precisa destes secrets no repositório do app:

| Secret | Conteúdo |
| --- | --- |
| `FDROID_REPO_KEYSTORE_BASE64` | `keystore.jks` do repositório, em base64 |
| `FDROID_REPO_KEYSTORE_PASS` | senha do keystore do repositório |
| `FDROID_PAGES_TOKEN` | PAT (escopo `repo`) com push em `cleberleonheart-maker/seguraturbo-fdroid` |
| `FDROID_GALINHA_KEYSTORE_BASE64` | `galinha.keystore` do Galinha Assada, em base64 |
| `FDROID_GALINHA_KEYSTORE_PASS` | senha do keystore do Galinha Assada |

O repositório F-Droid publica **quatro apps**: o SeguroTurbo, o
[Galinha Assada](../galinha-assada/) (jogo de labirinto), a **Pulsa** (player de
música) e a **Bússola** (ferramentas de navegação e sensores). O workflow
compila o APK do Galinha Assada com
[`../galinha-assada/android/build.sh`](../galinha-assada/android/build.sh) e
inclui os quatro no mesmo índice assinado.

A versão do jogo é fixa em
[`../galinha-assada/android/version.properties`](../galinha-assada/android/version.properties):
publicar de novo o mesmo código **não** cria versão nova, e o workflow falha se
esse arquivo divergir do metadata em `fdroid/br.com.galinhaassada.yml`. As
pastas do Pages guardam só as **5 versões mais novas** de cada app em cada
caminho (as antigas são limpas por `fdroid/limpar-pages.py`).

A Pulsa e a Bússola entram como **apps parceiros**: o metadata
([`com.pulsa.player.yml`](com.pulsa.player.yml), [`com.bussola.app.yml`](com.bussola.app.yml))
aponta para o APK da release de cada repositório no campo `Binaries:`. O workflow
usa [`../scripts/baixar-parceiros.sh`](../scripts/baixar-parceiros.sh), que baixa
o APK e confere pacote, versão e assinatura contra o metadata antes de publicar.
Para lançar uma versão nova deles, veja
[`RELEASE.md`](RELEASE.md#atualizar-um-app-parceiro-pulsa-bússola).

### Categorias e `fdroid lint`

O `fdroidserver` não traz a lista de categorias junto: sem um `categories.yml`,
o `fdroid lint` acusa **toda** categoria como inválida. Por isso o subconjunto
usado por aqui fica em [`categories.yml`](categories.yml) (fonte: o
[`config/categories.yml`](https://gitlab.com/fdroid/fdroiddata/-/blob/master/config/categories.yml)
do fdroiddata). Ao adotar uma categoria nova em algum metadata, acrescente ela
lá — e prefira os nomes exatos da lista oficial (é `Casual Game`, não `Game`).

Para rodar o lint na mão, num diretório limpo (os ícones que o
`fdroid update --create-metadata` cria em `metadata/<pacote>/` aparecem como
"Unused file" se você rodar dentro de `~/fdroid-repo`):

```bash
tmp="$(mktemp -d)"
mkdir -p "$tmp/metadata" "$tmp/config"
cp fdroid/com.*.yml fdroid/br.com.galinhaassada.yml "$tmp/metadata/"
cp -r fdroid/metadata/. "$tmp/metadata/"
cp fdroid/categories.yml "$tmp/config/categories.yml"
cp fdroid/repo-config.example.yml "$tmp/config.yml"
( cd "$tmp" && ~/.venvs/fdroid/bin/fdroid lint )
```

Se `FDROID_GALINHA_KEYSTORE_BASE64` não estiver configurado, o workflow publica
o SeguroTurbo e os apps parceiros e avisa com um warning — nada quebra.

O keystore do Galinha Assada precisa ser **sempre o mesmo**: se a assinatura
mudar, o Android recusa a atualização por cima do app já instalado no celular.

Para (re)criar os dois primeiros a partir da máquina local:

```bash
base64 -w0 ~/fdroid-repo/keystore.jks | gh secret set FDROID_REPO_KEYSTORE_BASE64
gh secret set FDROID_REPO_KEYSTORE_PASS --body "$(cat ~/fdroid-repo/.keystore-pass)"
```

O `FDROID_PAGES_TOKEN` é um [token de acesso pessoal](https://github.com/settings/tokens)
(classic) com o escopo `repo`; sem ele o push no repositório de Pages falha.

> O script `scripts/publicar-repo.sh` continua útil para publicar manualmente ou
> recuperar de uma falha no CI.

### Ícones e screenshots

O F-Droid extrai o ícone do APK automaticamente. Para os apps deste repositório,
o ícone publicado também pode vir de um arquivo próprio, no layout que o
`fdroidserver` lê:

- `fdroid/metadata/<pacote>/<locale>/images/icon.png` — ícone publicado
  (ex.: `fdroid/metadata/com.pulsa.player/en-US/images/icon.png`)

O workflow e o `scripts/publicar-repo.sh` copiam `fdroid/metadata/.` para
`metadata/` antes do `fdroid update`; o índice passa a apontar para o arquivo
copiado (`/<pacote>/en-US/icon_<hash>.png`) e não para o ícone extraído do APK.
Os três ícones atuais são 512×512, exceto o da Bússola, que usa o PNG nativo de
192×192 do APK (a Pulsa só tem VectorDrawable no repositório).

Para a ficha no F-Droid oficial, o guia recomenda manter no fastlane do app:

- `fastlane/metadata/android/<locale>/images/icon.png` (512×512 px)
- `fastlane/metadata/android/<locale>/images/phoneScreenshots/1.png` (capturas)

Screenshots aumentam a qualidade da ficha do app — vale tirar 3–4 do app rodando.

### Fingerprint da chave do repositório

Para conferir se o repositório é autêntico, use o fingerprint SHA-256 da chave
do repositório próprio:

```
92:FB:1F:52:80:E4:F8:26:32:72:12:81:B0:BB:55:59:AC:D9:93:E7:FE:A0:7A:67:10:D5:3F:6C:97:6E:73:34
```

> A chave foi regerada em **20/10/2026**. Ao usar o repositório próprio, o APK
> publicado é sempre o binário oficial da release (mesmo `sha256`) — em
> 20/10/2026 o índice passou a apontar para o APK da v1.29
> (`sha256 3e315c2f1c86da7ec6692fd75c8d68a61a547dddf933b74de37d10a938278485`).

### Senhas dos keystores

Dois keystores diferentes, não confundir:

- **App** (`seguraturbo.keystore`) — assina o APK. Senha nos secrets do GitHub
  Actions (`KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).
- **Repositório** (`~/fdroid-repo/keystore.jks`) — assina o índice do repositório
  próprio. Senha em `~/fdroid-repo/.keystore-pass` e no `~/fdroid-repo/config.yml`
  (permissão `600`, nunca versionar).

## Doação

O F-Droid é gratuito; a receita vem de doações. Preencha os links no `README.md`
(Pix/PayPal) e, se quiser, adicione a página no
[F-Droid donation page](https://f-droid.org/donate/).
