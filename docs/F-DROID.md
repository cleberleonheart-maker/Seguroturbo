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

### Opção A — pedido de empacotamento (RFP) — mais simples

1. Acesse a fila de submissões: <https://gitlab.com/fdroid/rfp/issues>
2. Clique em **New issue** (precisa de conta no GitLab)
3. No título use: `New App: com.seguraturbo.app`
4. No corpo, cole:

```
App name: SeguroTurbo
Source code: https://github.com/cleberleonheart-maker/Seguroturbo
License: GPL-3.0-only
Version: 1.27.1 (versionCode 29)

SeguroTurbo is a security, privacy and performance app for Android:
permission audit, system integrity check (root/SELinux/bootloader),
RAM cleanup, Game Turbo and real benchmarks (CPU/GPU/network/touch).
Open source, no tracking, no ads, no data collection.
Metadata (fastlane) is already in the repo.
```

5. Responda eventualmente às perguntas dos revisores.

### Opção B — merge request no fdroiddata (mais rápido e recomendado)

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

## 4. Ícone e screenshots

O F-Droid extrai o ícone do APK automaticamente. O guia oficial recomenda também:

- `fastlane/metadata/android/en-US/images/icon.png` (512×512 px)
- `fastlane/metadata/android/en-US/images/phoneScreenshots/1.png` (capturas de tela)

Screenshots aumentam muito a chance de aceitação — vale tirar 3–4 do app rodando.

## 5. Página de doação

Como o F-Droid é gratuito, a receita vem de doações. Preencha os links no `README.md`
(Pix/PayPal) e, se quiser, adicione a página no
[F-Droid donation page](https://f-droid.org/donate/).
