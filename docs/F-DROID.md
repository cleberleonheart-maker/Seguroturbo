# Como publicar no F-Droid

O F-Droid **não aceita apps pagos** — é um repositório exclusivo de software livre
gratuito. Ele compila o APK a partir do seu código-fonte (não publica builds seus)
e exige uma licença livre. O projeto já está preparado:

- `LICENSE` — GPL-3.0 (exigência obrigatória)
- `fastlane/metadata/android/` — descrições e changelogs em pt-BR, en-US, es-ES e fr-FR

**Submissão atual:**

- Issue no RFP — <https://gitlab.com/fdroid/rfp/-/work_items/4599>
- Merge request no `fdroiddata` —
  <https://gitlab.com/fdroid/fdroiddata/-/merge_requests/53157> (aguardando
  revisão de um mantenedor; link também comentado na issue do RFP)

O aviso automático `insecure-gradlew` já foi resolvido na v1.27.2
(`distributionSha256Sum` no Gradle Wrapper).

Os metadados prontos para o `fdroiddata` estão em
[`fdroid/com.seguraturbo.app.yml`](../fdroid/com.seguraturbo.app.yml) (última
versão: 1.29, versionCode 32).

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
Version: 1.29 (versionCode 32)

SeguroTurbo is a security, privacy and performance app for Android:
permission audit, system integrity check (root/SELinux/bootloader),
RAM cleanup, Game Turbo and real benchmarks (CPU/GPU/network/touch).
Open source, no tracking, no ads, no data collection.
Metadata (fastlane) is already in the repo.
```

5. Responda eventualmente às perguntas dos revisores.

### Opção B — merge request no fdroiddata (mais rápido e recomendado)

1. Crie conta no GitLab e faça fork de <https://gitlab.com/fdroid/fdroiddata>
2. Crie `metadata/com.seguraturbo.app.yml` no fork com o conteúdo de
   [`fdroid/com.seguraturbo.app.yml`](../fdroid/com.seguraturbo.app.yml).
3. Abra um merge request em <https://gitlab.com/fdroid/fdroiddata/-/merge_requests>

> **Status:** MR aberto em
> <https://gitlab.com/fdroid/fdroiddata/-/merge_requests/53157>
> (fork `cleberleonheart-maker/fdroiddata`, branch `add-seguraturbo` → `master`).

> **Atenção:** a cada release nova, atualize no YAML o `versionName`,
> `versionCode`, `commit` (hash do tag, ex.: `git rev-parse v1.29`),
> `CurrentVersion` e `CurrentVersionCode` — sempre batendo com o
> `versionCode`/`versionName` do `app/build.gradle`.

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
