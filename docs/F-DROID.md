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
(commits regulares, releases). Quando estiver pronto:

1. Faça fork de <https://gitlab.com/fdroid/fdroiddata>
2. Crie `metadata/com.seguraturbo.app.yml` com o conteúdo de
   [`fdroid/com.seguraturbo.app.yml`](../fdroid/com.seguraturbo.app.yml).
3. Abra um merge request **usando o template oficial** (não apague as seções).
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

5. A cada release nova: coloque o novo APK na pasta e rode `fdroid update` de novo.

### Ícone e screenshots

O F-Droid extrai o ícone do APK automaticamente. O guia oficial recomenda também:

- `fastlane/metadata/android/en-US/images/icon.png` (512×512 px)
- `fastlane/metadata/android/en-US/images/phoneScreenshots/1.png` (capturas de tela)

Screenshots aumentam a qualidade da ficha do app — vale tirar 3–4 do app rodando.

## Doação

O F-Droid é gratuito; a receita vem de doações. Preencha os links no `README.md`
(Pix/PayPal) e, se quiser, adicione a página no
[F-Droid donation page](https://f-droid.org/donate/).
