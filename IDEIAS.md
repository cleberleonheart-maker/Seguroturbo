# Ideias futuras

Lista de ideias para o SeguroTurbo, agrupadas por tema. É o backlog do que ainda
**falta fazer**; o que já foi implementado está listado na seção **Feito**, no fim.

## Segurança (mais checagens reais)

- [x] **Auditoria de permissões** (v1.22): tela detalhada de quais apps têm acesso a
      câmera, microfone, SMS, localização etc.
- [x] **Root/SELinux/bootloader** (v1.23): detecção ampliada de root, estado do SELinux
      (Enforcing/Permissive) e indícios de bootloader desbloqueado, com explicações.
- [ ] **Play Integrity**: checar a integridade do dispositivo (quando integrável).
- [ ] **Detector de stalkerware/ratware** (v1.30): comparar os pacotes instalados com
      uma lista local de apps espiões conhecidos; só usa PackageManager, sem permissão extra.
- [ ] **Perfil de risco por app** (v1.30): cruzar `INTERNET` + permissões sensíveis e
      destacar os apps que combinam as duas coisas, sem coletar nada.
- [ ] **Alertas temporais de segurança** (v1.31): avisar quando o patch de segurança do
      sistema estiver desatualizado há mais de X meses.
- [ ] **Verificador de senhas vazadas (offline)** (v1.31): dicionário local, sem enviar
      nada pela rede — alinhado ao "sem coleta de dados".
- [ ] **Inventário exportável de apps + permissões** (v1.31): reaproveita `RelatorioCsv.kt`.

## Desempenho / Game Turbo

- [x] **Limpeza de RAM** (v1.24): encerrar apps escolhidos pelo usuário em segundo
      plano, com indicador de uso de memória.
- [x] **Bloqueio de tela (screen pinning)** ao iniciar um jogo (v1.28).
- [x] **Silenciar só mídia** durante o jogo (v1.29): baixa o volume de músicas e
      vídeos no Modo Jogo, sem afetar chamadas, alarmes e notificações.
- [ ] **Perfil por jogo** (v1.30): salvar perfil de desempenho, Não Perturbe e trava de
      tela por app de jogo, em vez de uma configuração global.
- [ ] **Auto-limpeza de RAM ao abrir o jogo** (v1.30): disparar a limpeza automaticamente
      quando o Modo Jogo liga (gancho no `GerenciadorRam.kt`).
- [ ] **Turbo agendado** (v1.31): ativar o Modo Jogo automaticamente em horários definidos.
- [ ] **Modo "Sessão de foco"** (v1.31): timer estilo Pomodoro ligado ao Não Perturbe.

## Benchmark

- [x] **Teste de rede** (v1.25): velocidade de download e latência.
- [x] **Teste de tela/touch** (v1.26): resposta ao toque e multitouch.
- [ ] **Monitor de FPS contínuo** (v1.30): overlay em tempo real reaproveitando a barra flutuante.
- [ ] **Monitor de rede em tempo real** (v1.30): velocidade up/down na barra flutuante.
- [ ] **Teste de consumo de bateria** (v1.31): mede a drenagem em X minutos por perfil.
- [ ] **Comparação com médias de referência** (v1.31): comparar a nota com faixas por
      classe de aparelho, além do histórico local.

## Produto / UX

- [ ] **Perfis de uso** (v1.30): presets **"Gamer"**, **"Casual"**, **"Bateria"** e
      **"Segurança máxima"** que aplicam várias preferências de uma vez (perfil de
      desempenho, Não Perturbe, trava de tela, nível de alerta). Totalmente local, sem
      conta nem nuvem — mantém o "sem coleta de dados".
- [ ] **Perfil de dispositivo** (v1.31): detecção read-only de work profile / Device
      Owner (aparelho gerenciado) na aba Segurança.
- [ ] **Widgets adicionais**: mais atalhos na tela inicial (score de segurança, limpeza
      de RAM com um toque).
- [ ] **Atalhos dinâmicos** (v1.30): long-press no ícone → "Escanear", "Limpar RAM",
      "Testar rede".
- [ ] **Modo escuro AMOLED** (v1.30): preto puro para economizar bateria em telas OLED.
- [ ] **Filtro de alertas por categoria** (v1.31) na tela de Segurança.

## Qualidade / engenharia

- [x] **Testes instrumentados** (Compose UI) para os fluxos principais (v1.27).
- [ ] **Baseline Profiles** por módulo, para abertura ainda mais rápida.
- [ ] **Publicação automática do repo F-Droid no CI** (v1.30): encadear o
      `scripts/publicar-repo.sh` no workflow de release, para fechar o ciclo.
- [ ] **Migrar SharedPreferences → DataStore** (v1.31): manter os 3 stores, mais robustos.
- [ ] **Testes de snapshot Compose** (v1.31): Paparazzi/Roborazzi nos fluxos já testados.

## Distribuição / projeto

- [ ] **Screenshots reais + feature graphic** na ficha F-Droid.
- [ ] **Página de changelog** no GitHub Pages do repositório F-Droid.
- [ ] **Reabrir o MR oficial do F-Droid** (a partir de 16/01/2027, com o template
      de "New app").

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
- [x] **Compartilhar o gráfico** de evolução das pontuações (v1.19).
- [x] **Barra flutuante** do Game Turbo sobre o jogo (v1.20).
- [x] **Auditoria de permissões** por app (câmera, microfone, etc.) (v1.22).
- [x] **Integridade do sistema**: root, SELinux e bootloader (v1.23).
- [x] **Limpeza de RAM**: encerrar apps escolhidos (v1.24).
- [x] **Teste de rede**: latência e download (v1.25).
- [x] **Teste de tela/touch**: resposta ao toque e multitouch (v1.26).
- [x] **Testes instrumentados** (Compose UI) dos fluxos principais (v1.27).
- [x] **Bloqueio de tela** (screen pinning) ao iniciar um jogo (v1.28).
- [x] **Silenciar só mídia** no Modo Jogo (v1.29).
