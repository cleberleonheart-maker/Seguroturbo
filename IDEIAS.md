# Ideias futuras

Lista de ideias para o SeguroTurbo, agrupadas por tema. É o backlog do que ainda
**falta fazer**; o que já foi implementado está listado na seção **Feito**, no fim.

## Segurança (mais checagens reais)

- [ ] **Auditoria de permissões**: tela detalhada de quais apps têm acesso a
      câmera, microfone, SMS, localização etc. (hoje há só um resumo).
- [ ] **Root/SELinux/bootloader**: ampliar a detecção de root (estado do SELinux,
      integridade do sistema) e explicar cada caso.
- [ ] **Sideload**: listar apps instalados de fontes externas à loja oficial.
- [ ] **Assinatura de apps**: mostrar o hash/certificado do APK (verificador de apps).
- [ ] **Verificação de senha/bloqueio**: avaliar o tempo de bloqueio automático da tela.
- [ ] **Play Integrity**: checar a integridade do dispositivo (quando integrável).

## Desempenho / Game Turbo

- [ ] **Monitor térmico**: temperatura da bateria/térmico com alerta de aquecimento.
- [ ] **Limpeza de RAM**: encerrar apps escolhidos pelo usuário em segundo plano.
- [ ] **Bloqueio de tela (screen pinning)** ao iniciar um jogo.
- [ ] **Silenciar só mídia** durante o jogo (opções finas de áudio).

## Benchmark

- [ ] **Teste de rede**: velocidade de download e latência.
- [ ] **Teste de tela/touch**: resposta ao toque e multitouch.
- [ ] **Exportar histórico em CSV**.
- [ ] **Compartilhar o gráfico** de evolução das pontuações.
- [ ] **Comparação**: média/ranking local com os resultados anteriores.

## Produto / UX

- [ ] **Tema automático** (seguir o sistema) além do claro/escuro manual.
- [ ] **Checagem periódica de atualização** em segundo plano (WorkManager).
- [ ] **Mais idiomas** (espanhol, francês).
- [ ] **Backup/restauração** das preferências e do histórico.

## Qualidade / engenharia

- [ ] **Testes instrumentados** (Compose UI) para os fluxos principais.
- [ ] **Cobertura** de testes no GitHub Actions (o **lint** já roda).
- [ ] **Baseline Profiles** por módulo, para abertura ainda mais rápida.

## Feito

- [x] **Update checker** (v1.7): avisa quando há release nova no GitHub.
- [x] **Não Perturbe automático** no Game Turbo (v1.8).
- [x] **Patch de segurança** e **Google Play Protect** no scan (v1.8).
- [x] **Auto-update no app** (v1.9): baixa e instala a nova versão sem abrir o navegador.
- [x] **Onboarding** no primeiro uso (v1.10).
- [x] **Testes unitários** de versões/patch e **lint no CI** (v1.10).
- [x] **Checagem de Wi‑Fi/VPN/DNS** na Segurança (v1.11).
- [x] **Perfis de desempenho** Economia/Equilibrado/Turbo (v1.11).
