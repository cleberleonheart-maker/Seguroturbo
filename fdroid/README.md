# Metadados e repositório F-Droid

Esta pasta reúne tudo do F-Droid:

- `com.seguraturbo.app.yml` — metadados do app (formato `fdroiddata`).
- `repo-config.example.yml` — modelo de config para um **repositório F-Droid próprio**
  (sem senhas; o script gera o `config.yml` real com senha aleatória).
- `gerar-repo.sh` — script que monta o repositório próprio a partir dos APKs.

## Repositório oficial — recusado

O merge request em [`fdroid/fdroiddata`](https://gitlab.com/fdroid/fdroiddata)
foi **fechado** com dois motivos: template do MR não seguido e política dos
3 meses. Detalhes em [`../docs/F-DROID.md`](../docs/F-DROID.md).

## Repositório próprio — funciona já

Gera um repositório F-Droid que os usuários adicionam como repositório
alternativo, sem depender de aprovação:

```bash
pip install fdroidserver
./gerar-repo.sh ~/fdroid-repo
```

Ao reabrir o MR mais tarde, atualize `versionName`, `versionCode`, `commit`
(hash da tag, ex.: `git rev-parse v1.29`), `CurrentVersion` e `CurrentVersionCode`.
