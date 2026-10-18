# Como publicar no F-Droid

O F-Droid **não aceita apps pagos** — é um repositório exclusivo de software livre
gratuito. Ele compila o APK a partir do seu código-fonte (não publica builds seus)
e exige uma licença livre. O projeto já está preparado:

- `LICENSE` — GPL-3.0 (exigência obrigatória)
- `fastlane/metadata/android/` — descrições e changelogs em pt-BR, en-US, es-ES e fr-FR

## 1. Pré-requisitos

- Conta no [GitLab](https://gitlab.com) (é onde o F-Droid hospeda os metadados)
- O repositório público no GitHub já está pronto: `cleberleonheart-maker/Seguroturbo`

## 2. Submeter o app

Duas opções:

### Opção A — formulário (mais simples)

1. Acesse <https://f-droid.org/en/docs/Inclusion_Policy/>
2. Use o formulário de submissão: <https://f-droid.org/packages/#suggest>
3. Informe a URL do repositório: `https://github.com/cleberleonheart-maker/Seguroturbo`
4. Descreva o app em poucas frases (cole o `short_description.txt`)

O F-Droid cria um pedido de inclusão e um voluntário revisa.

### Opção B — merge request no fdroiddata (mais rápido)

1. Crie conta no GitLab e faça fork de <https://gitlab.com/fdroid/fdroiddata>
2. Crie `metadata/com.seguraturbo.app.yml` no fork com:

```yaml
Categories:
    - Security
License: GPL-3.0-only
Author: cleberleonheart-maker
SourceCode: https://github.com/cleberleonheart-maker/Seguroturbo
IssueTracker: https://github.com/cleberleonheart-maker/Seguroturbo/issues
Changelog: https://github.com/cleberleonheart-maker/Seguroturbo/releases

AutoName: SeguroTurbo

RepoType: git
Repo: https://github.com/cleberleonheart-maker/Seguroturbo.git

Builds:
    - versionName: 1.27.1
      versionCode: 29
      commit: v1.27.1
      subdir: app
      gradle:
          - yes

AutoUpdateMode: VersionTag
UpdateCheckMode: Tags
```

3. Abra um merge request em <https://gitlab.com/fdroid/fdroiddata/-/merge_requests>

> **Atenção:** use o commit/tag `v1.27.1` correspondente ao versionCode 29. Ao publicar
> uma versão nova, incremente o `versionCode` no `app/build.gradle` e crie a tag.

## 3. Depois da aprovação

- O F-Droid compila e assina o APK com a chave deles (o seu `keystore.properties` é ignorado)
- O app aparece em <https://f-droid.org/packages/com.seguraturbo.app/>
- Atualizações: quando você criar uma tag `vX.Y` no GitHub com o `versionCode` incrementado
  no `app/build.gradle`, o F-Droid detecta automaticamente (`UpdateCheckMode: Tags`)

## 4. Ícone

O F-Droid extrai o ícone do APK automaticamente. Se quiser um ícone de 512×512 px
para a listagem, adicione `fastlane/metadata/android/en-US/images/icon.png`.

## 5. Página de doação

Como o F-Droid é gratuito, a receita vem de doações. Preencha os links no `README.md`
(Pix/PayPal) e, se quiser, adicione a página no
[F-Droid donation page](https://f-droid.org/donate/).
