# Ideias futuras

Lista de ideias para o SeguroTurbo, agrupadas por tema. É o backlog do que ainda
**falta fazer**; o que já foi implementado está listado na seção **Feito**, no fim.

## Segurança (mais checagens reais)

- [ ] **Auditoria de permissões**: tela detalhada de quais apps têm acesso a
      câmera, microfone, SMS, localização etc. (hoje há só um resumo).
- [ ] **Root/SELinux/bootloader**: ampliar a detecção de root (estado do SELinux,
      integridade do sistema) e explicar cada caso.
- [ ] **Play Integrity**: checar a integridade do dispositivo (quando integrável).

## Desempenho / Game Turbo

- [ ] **Limpeza de RAM**: encerrar apps escolhidos pelo usuário em segundo plano.
- [ ] **Bloqueio de tela (screen pinning)** ao iniciar um jogo.
- [ ] **Silenciar só mídia** durante o jogo (opções finas de áudio).

## Benchmark

- [ ] **Teste de rede**: velocidade de download e latência.
- [ ] **Teste de tela/touch**: resposta ao toque e multitouch.
- [ ] **Compartilhar o gráfico** de evolução das pontuações.
## Produto / UX

- [ ] **Widgets adicionais**: mais atalhos na tela inicial.

## Qualidade / engenharia

- [ ] **Testes instrumentados** (Compose UI) para os fluxos principais.
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
- [x] **Monitor térmico** da bateria no Game Turbo (v1.12).
- [x] **Tempo de bloqueio automático** da tela no scan (v1.12).
- [x] **Exportar histórico em CSV** (v1.12).
- [x] **Sideload**: apps instalados fora da loja (v1.13).
- [x] **Assinatura do app** SHA‑256 nas configurações (v1.13).
- [x] **Tema automático** (seguir o sistema) (v1.14).
- [x] **Comparação local** de benchmark: média e ranking (v1.14).
- [x] **Espanhol** nas strings do app (v1.15).
- [x] **Cobertura de testes no CI** com JaCoCo (v1.15).
- [x] **Backup/restauração** das preferências e do histórico (v1.16).
- [x] **Checagem periódica de atualização** em segundo plano (WorkManager) (v1.16).
- [x] **Espanhol e francês** nas strings — app em pt/en/es/fr (v1.15–v1.17).
- [x] **Changelog / Novidades** (novo + correções) mostrado ao atualizar (v1.18).
- [x] **Auto-update mais robusto**: segue redirecionamentos e abre o navegador como reserva (v1.18).
