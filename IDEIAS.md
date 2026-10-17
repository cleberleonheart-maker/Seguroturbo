# Ideias futuras

Lista de ideias para o SeguroTurbo, agrupadas por tema. Serve de backlog: nada aqui
está implementado, exceto o que estiver marcado como **feito**.

## Segurança (mais checagens reais)

- [ ] **Wi-Fi**: detectar rede aberta (sem senha), VPN ativa e DNS configurado.
- [ ] **Auditoria de permissões**: tela detalhada de quais apps têm acesso a
      câmera, microfone, SMS, localização etc. (hoje há só um resumo).
- [ ] **Root/SELinux/bootloader**: ampliar a detecção de root (estado do SELinux,
      integridade do sistema) e explicar cada caso.
- [ ] **Sideload**: listar apps instalados de fontes externas à loja oficial.
- [ ] **Assinatura de apps**: mostrar o hash/certificado do APK (verificador de apps).
- [ ] **Verificação de senha/bloqueio**: avaliar o tempo de bloqueio automático da tela.
- [ ] **Play Integrity**: checar a integridade do dispositivo (quando integrável).

## Desempenho / Game Turbo

- [ ] **Perfis de desempenho**: Economia / Equilibrado / Turbo.
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

- [ ] **Onboarding** no primeiro uso, explicando os três módulos e pedindo permissões.
- [ ] **Tema automático** (seguir o sistema) além do claro/escuro manual.
- [ ] **Checagem periódica de atualização** em segundo plano (WorkManager).
- [ ] **Mais idiomas** (espanhol, francês).
- [ ] **Backup/restauração** das preferências e do histórico.

## Qualidade / engenharia

- [ ] **Testes instrumentados** (Compose UI) para os fluxos principais.
- [ ] **Lint + cobertura** no GitHub Actions.
- [ ] **Baseline Profiles** por módulo, para abertura ainda mais rápida.

## Feito

- [x] **Update checker** (v1.7): avisa quando há release nova no GitHub.
- [x] **Não Perturbe automático** no Game Turbo (v1.8).
- [x] **Patch de segurança** e **Google Play Protect** no scan (v1.8).
