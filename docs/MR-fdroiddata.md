# MR no F-Droid oficial (`fdroiddata`) — rascunho para 16/01/2027

O MR anterior ([!53157](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/53157))
foi fechado por dois motivos: **template não seguido** e **política dos 3 meses**.

- Primeiro commit do projeto: **16/10/2026**
- Data mínima para reabrir: **16/01/2027**
- RFP aberta para referenciar: [rfp#4599](https://gitlab.com/fdroid/rfp/-/work_items/4599)

Este documento tem quatro partes: (1) o que ajustar **antes** de abrir, (2) o texto
pronto para colar no MR, (3) o YAML final e (4) o passo a passo do dia.

---

## 1. Pré-voo (fazer no dia da submissão)

| # | Ação | Por quê |
| - | ---- | ------- |
| 1 | Remover `Builds:` antigos, deixando **só a versão mais recente** | Checklist: *"Only the latest versions should be kept"* e *"Don't add any disabled versions"* |
| 2 | Remover `Name`/`Summary`/`Description` do YAML | Checklist: descrição/ícones vêm do Fastlane do upstream, não do `fdroiddata` |
| 3 | Adicionar `AllowedAPKSigningKeys` com a chave do app | Permite o build reprodutível assinado pelo autor |
| 4 | Conferir que `commit:` é hash **completo** | Exigência explícita do checklist |
| 5 | Bater `versionName`/`versionCode` com `app/build.gradle` e com a tag | Evita o erro do MR anterior |
| 6 | Rodar o CI e deixar o pipeline verde | Item obrigatório |
| 7 | Fork público, branch **não** protegida | Item obrigatório |
| 8 | Confirmar `fastlane/metadata/android/en-US/` presente | Já existe (4 idiomas) |

`AllowedAPKSigningKeys` a usar (chave do app, `CN=SeguroTurbo`):

```yaml
AllowedAPKSigningKeys: 3d548ee14413160b178d801ea467e7c0d4bcb4d9d91b0bfa1a02c279ec0b1d9f
```

> A chave do **repositório F-Droid próprio** (`92:FB:…:34`) não entra aqui — ela
> assina só o índice do repo, não o APK.

### Blocos de APK (ABI split)

O APK traz `native-code` para `arm64-v8a`, `armeabi-v7a`, `x86` e `x86_64`, mas
pesa ~3,7 MiB. O checklist só pede split quando o APK é grande e a divisão
compensa; aqui vale explicar no MR que não foi feito por ser pequeno.

---

## 2. Corpo do MR (colar em inglês)

**Título:** `New app: SeguroTurbo`

```markdown
## Abstract for new app inclusion

### Please introduce the functions and usage of your app in summary, and also attach some screenshots.

SeguroTurbo is a local-first Android utility that bundles three modules in one app:

- **Security** — audits sensitive permissions (camera, microphone, location, SMS,
  contacts and others) showing which apps hold them and how to revoke each one,
  with CSV export; checks device integrity (root, SELinux permissive, test-keys
  builds); and detects known stalkerware packages plus unusual permission
  combinations, with a shortcut to uninstall. Everything runs on-device.
- **Game Turbo** — performance profile, Do Not Disturb while playing, floating
  overlay with battery/RAM/temperature, per-game profiles, scheduled turbo, RAM
  cleanup with history, screen lock on game launch and media-only silencing.
- **Benchmark** — CPU, GPU, memory and UX tests, plus network (latency/download),
  display/touch (taps per second, multitouch) and a battery consumption test.

Screenshots (en-US):
https://github.com/cleberleonheart-maker/Seguroturbo/tree/main/fastlane/metadata/android/en-US/images/phoneScreenshots

### Please elaborate why you wrote this app. Please highlight the unique functions it provides over other similar apps in F-Droid.

Most "security booster" and "game booster" apps mix real functionality with
advertising, trackers and placebo features. SeguroTurbo was written to be honest
about what a non-rooted Android device can actually do: every check reports what
it measured, and when something cannot be verified locally the app says so
instead of showing a fake "100% secure" badge.

What is hard to find together in F-Droid today:

- A permission audit that explains how to revoke each permission and exports the
  result to CSV, instead of only listing package names.
- A stalkerware detector based on known packages and suspicious permission
  combinations, fully offline.
- A game performance overlay (battery, RAM, temperature) with per-game profiles
  that also handles Do Not Disturb and media silencing.
- A benchmark that includes display/touch and battery-consumption tests.

There is no account, no telemetry, no advertising and no data collection. Network
access is used only for the optional update check and the optional network speed
test.

## Checklist

### Policy

* [x] The app complies with the [inclusion criteria](https://f-droid.org/docs/Inclusion_Policy).
* [x] The original app author has been notified (and does not oppose the inclusion). If you are not the author, please paste the link of the reply from the author. — I am the author.
* [x] The upstream app source code repo contains the app metadata in a [Fastlane](https://gitlab.com/snippets/1895688) or [Triple-T](https://gitlab.com/snippets/1901490) folder structure. — `fastlane/metadata/android/{en-US,pt-BR,es-ES,fr-FR}` with title, short/full description, changelogs and screenshots.

### Docs

* [x] Please read [the guide](https://gitlab.com/fdroid/fdroiddata/-/blob/master/CONTRIBUTING.md) first if this is your first contribution.
* [x] Please make sure your metadata follows the best practice in [our templates](https://gitlab.com/fdroid/fdroiddata/tree/master/templates).
* [x] Please read the [Build Metadata Reference](https://f-droid.org/docs/Build_Metadata_Reference/) and make sure your metadata is valid.
* [x] Please read the [Quick Start Guide](https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/).

### Merge Request Setup

* [x] The title of this merge request should follow "New app: app name" format.
* [x] Please make sure your fdroiddata fork is public and your branch is not protected. See <https://docs.gitlab.com/user/project/repository/branches/protected/>.
* [x] Please read [our Git guide](https://gitlab.com/fdroid/wiki/-/wikis/Tips-for-fdroiddata-contributors/Git-Usage) if you don't know how to rebase your branch. Don't rebase your branch if there is no conflict.
* [x] All related [fdroiddata](https://gitlab.com/fdroid/fdroiddata/issues) and [RFP issues](https://gitlab.com/fdroid/rfp/issues) have been referenced in this merge request. — Closes rfp#4599.
* [x] Please only submit one app in one MR.

### Metadata

* [x] Metadata must be put in `metadata/<applicationId>.yml`.
* [x] Metadata must be a valid YAML file.
* [x] Metadata must use LF as line ending.
* [x] Don't add summary/description/changelog/images or anything that should be provided in upstream repo. Please check the Changes tab to make sure there is no other unrelated files added in the MR.
* [x] Releases are tagged and auto update is enabled unless there is a special reason. — `UpdateCheckMode: Tags`, `AutoUpdateMode: Version`, tags `v1.36.1` etc.
* [x] There is an issue tracker and contact info of the author so that we can report bugs and contact the author. — https://github.com/cleberleonheart-maker/Seguroturbo/issues
* [x] An AuthorName must be added. It doesn't need to be the real name.
* [x] External repos are added as git submodules instead of srclibs. — No external repos or submodules needed.
* [x] Enable [Reproducible Builds](https://f-droid.org/docs/Reproducible_Builds). — `AllowedAPKSigningKeys` set to the upstream signing certificate.
* [ ] Setup abi split if the APK is large and the splitted ones can be much smaller. — Not applicable: the APK is ~3.7 MiB with four ABIs already included; splitting would save little.
* [x] Only the latest versions should be kept in the metadata before it's merged.
* [x] Don't add any disabled versions in the metadata.
* [x] The `commit` field should be the full hash. Please don't use tag or branch in commit.

### Pipeline

* [x] All pipelines should pass.
* [x] All warnings and errors in the Reports tab should be fixed or explained.
* [x] F-Droid CI runners are under GitLab's FOSS program, so there's no need for you to pay for any CI time. If Gitlab starts asking for phone numbers or credit cards don't submit anything, just leave a note in the MR so we know we need to trigger the CI.

## Notes for reviewers

- **No proprietary dependencies.** The "Play Integrity" screen is a *local*
  heuristic based on root, bootloader and SELinux state; the app does not depend
  on Google Play services or any GMS library.
- **Network access** is limited to two optional actions: the update check
  (GitHub Releases API, with a fallback endpoint) and the network speed test
  (`speed.cloudflare.com`). There are no analytics, ad or crash-reporting SDKs.
- **Permissions** are used for the advertised features: `QUERY_ALL_PACKAGES` and
  `KILL_BACKGROUND_PROCESSES` for the permission audit and RAM cleanup,
  `SYSTEM_ALERT_WINDOW` + `FOREGROUND_SERVICE*` for the overlay,
  `ACCESS_NOTIFICATION_POLICY` for Do Not Disturb and `WRITE_SETTINGS` for
  brightness — all requested at runtime, only when the feature is used.
- **Build**: Gradle wrapper with `distributionSha256Sum` pinned; JDK 17.

Closes rfp#4599

/label ~"New App"
```

---

## 3. YAML final do MR (copiar como `metadata/com.seguraturbo.app.yml`)

```yaml
Categories:
  - Security
License: GPL-3.0-only
AuthorName: cleberleonheart-maker
SourceCode: https://github.com/cleberleonheart-maker/Seguroturbo
IssueTracker: https://github.com/cleberleonheart-maker/Seguroturbo/issues
Changelog: https://github.com/cleberleonheart-maker/Seguroturbo/releases

AutoName: SeguroTurbo

RepoType: git
Repo: https://github.com/cleberleonheart-maker/Seguroturbo.git

Builds:
  - versionName: '1.36.1'
    versionCode: 40
    commit: 7756ae8683051b3bbda4e82be8d8757f404134ec
    gradle:
      - yes

AllowedAPKSigningKeys: 3d548ee14413160b178d801ea467e7c0d4bcb4d9d91b0bfa1a02c279ec0b1d9f

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.36.1
CurrentVersionCode: 40
```

> Atualize `versionName`, `versionCode`, `commit` (hash da tag) e o par
> `CurrentVersion`/`CurrentVersionCode` para a release vigente no dia.

> **Não** mexa em `fdroid/com.seguraturbo.app.yml` do repositório: ele alimenta o
> repositório F-Droid **próprio** (workflow `publicar-repo-fdroid.yml`) e pode
> manter o histórico de versões. São arquivos com propósitos diferentes.

---

## 4. Passo a passo no dia

```bash
# fork de https://gitlab.com/fdroid/fdroiddata (pela interface)
git clone git@gitlab.com:<seu-usuario>/fdroiddata.git
cd fdroiddata
git checkout -b new-app-seguraturbo

# copie o metadata já ajustado (só a versão mais recente, sem Summary/Description)
$EDITOR metadata/com.seguraturbo.app.yml

git add metadata/com.seguraturbo.app.yml
git commit -m "New app: SeguroTurbo"
git push origin new-app-seguraturbo
```

Depois abra o MR usando **"New App"** como template, cole o corpo acima e
acompanhe o pipeline. O label `~"New App"` já vai no corpo.
