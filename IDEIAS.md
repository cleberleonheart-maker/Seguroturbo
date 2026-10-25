# Ideias futuras

Lista de ideias para o SeguroTurbo, agrupadas por tema. É o backlog do que ainda
**falta fazer**; o que já foi implementado está listado na seção **Feito**, no fim.

## Segurança (mais checagens reais)

- [x] **Auditoria de permissões** (v1.22): tela detalhada de quais apps têm acesso a
      câmera, microfone, SMS, localização etc.
- [x] **Root/SELinux/bootloader** (v1.23): detecção ampliada de root, estado do SELinux
      (Enforcing/Permissive) e indícios de bootloader desbloqueado, com explicações.
- [x] **Play Integrity**: checar a integridade do dispositivo (quando integrável).
- [x] **Detector de stalkerware/ratware** (v1.31): compara os pacotes instalados com
      uma lista local de apps espiões conhecidos e sinaliza combinações incomuns de
      permissões (SMS + localização + contatos/microfone); só usa PackageManager, sem
      permissão extra e sem enviar dados.
- [x] **Perfil de risco por app** (v1.33): cruza `INTERNET` + permissões sensíveis e
      destaca os apps que combinam as duas coisas, sem coletar nada.
- [x] **Alertas temporais de segurança** (v1.31): avisar quando o patch de segurança do
      sistema estiver desatualizado há mais de X meses.
- [x] **Verificador de senhas vazadas (offline)** (v1.31): dicionário local, sem enviar
      nada pela rede — alinhado ao "sem coleta de dados".
- [x] **Inventário exportável de apps + permissões** (v1.32): gera CSV (`InventarioCsv.kt`,
      reaproveitando `RelatorioCsv.kt`) com os apps visíveis e seus grupos de permissão
      sensível, tudo offline.

## Desempenho / Game Turbo

- [x] **Limpeza de RAM** (v1.24): encerrar apps escolhidos pelo usuário em segundo
      plano, com indicador de uso de memória.
- [x] **Bloqueio de tela (screen pinning)** ao iniciar um jogo (v1.28).
- [x] **Silenciar só mídia** durante o jogo (v1.29): baixa o volume de músicas e
      vídeos no Modo Jogo, sem afetar chamadas, alarmes e notificações.
- [x] **Perfil por jogo** (v1.30): salvar perfil de desempenho, Não Perturbe e trava de
      tela por app de jogo, em vez de uma configuração global.
- [x] **Auto-limpeza de RAM ao abrir o jogo** (v1.30): disparar a limpeza automaticamente
      quando o Modo Jogo liga (gancho no `GerenciadorRam.kt`).
- [x] **Turbo agendado** (v1.31): ativar o Modo Jogo automaticamente em horários definidos.
- [x] **Modo "Sessão de foco"** (v1.31): timer estilo Pomodoro ligado ao Não Perturbe.

## Benchmark

- [x] **Teste de rede** (v1.25): velocidade de download e latência.
- [x] **Teste de tela/touch** (v1.26): resposta ao toque e multitouch.
- [x] **Monitor de FPS contínuo** (v1.30): overlay em tempo real reaproveitando a barra flutuante.
- [x] **Monitor de rede em tempo real** (v1.30): velocidade up/down na barra flutuante.
- [x] **Teste de consumo de bateria** (v1.31): mede a drenagem em X minutos por perfil.
- [x] **Comparação com médias de referência** (v1.31): comparar a nota com faixas por
      classe de aparelho, além do histórico local.

## Produto / UX

- [x] **Perfis de uso** (v1.30): presets **"Gamer"**, **"Casual"**, **"Bateria"** e
      **"Segurança máxima"** que aplicam várias preferências de uma vez (perfil de
      desempenho, Não Perturbe, trava de tela, nível de alerta). Totalmente local, sem
      conta nem nuvem — mantém o "sem coleta de dados".
- [x] **Perfil de dispositivo** (v1.34): detecção read-only de work profile / Device
      Owner (aparelho gerenciado) na aba Segurança.
- [x] **Widgets adicionais**: mais atalhos na tela inicial (score de segurança, limpeza
      de RAM com um toque).
- [x] **Atalhos dinâmicos** (v1.34): long-press no ícone → "Escanear", "Limpar RAM",
      "Testar rede".
- [x] **Modo escuro AMOLED** (v1.34): preto puro para economizar bateria em telas OLED.
- [x] **Filtro de alertas por categoria** (v1.34) na tela de Segurança.

## Qualidade / engenharia

- [x] **Testes instrumentados** (Compose UI) para os fluxos principais (v1.27).
- [x] **Baseline Profiles** por módulo, para abertura ainda mais rápida.
- [x] **Publicação automática do repo F-Droid no CI** (v1.31): workflow
      `.github/workflows/publicar-repo-fdroid.yml` regenera o índice assinado e publica
      no GitHub Pages logo após a release, fechando o ciclo sem passo manual.
- [x] **Migrar SharedPreferences → DataStore** (v1.31): manter os 3 stores, mais robustos.
- [x] **Testes de snapshot Compose** (v1.31): Paparazzi/Roborazzi nos fluxos já testados.

## Distribuição / projeto

- [x] **Screenshots reais + feature graphic** na ficha F-Droid.
- [x] **Página de changelog** no GitHub Pages do repositório F-Droid.
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
- [x] **Perfis de uso** (Gamer/Casual/Bateria/Segurança máxima) (v1.30).
- [x] **Detector de stalkerware** (v1.31).
- [x] **Inventário exportável de apps + permissões** (v1.32).
- [x] **Perfil de risco por app** (v1.33): nível alto/médio/baixo cruzando internet e
      permissões sensíveis, com resumo no cartão e ordenação por risco no diálogo.
- [x] **Tema AMOLED, filtro de alertas, perfil de dispositivo e atalhos no ícone** (v1.34).

## Novas ideias (propostas)

- [x] **Widget da tela inicial** (v1.36): widget para escanear rápido, limpar RAM, ligar/desligar Game Turbo direto da home.
- [x] **Atalho por app para perfis** (v1.36): abrir diretamente o perfil do jogo escolhido pelo app launcher (shortcuts).
- [x] **Modo "Economia" automático** (v1.36): ativar perfis de economia com base em bateria baixa/temperatura.
- [x] **Histórico de limpezas de RAM** (v1.36): registrar quando e quais apps foram encerrados, com contador.
- [x] **Exportar relatório completo (ZIP)** (v1.36): gerar ZIP com inventário CSV, histórico, logs e configurações (tudo offline).
- [x] **Tema dinâmico Material You** (v1.36): suporte a cores dinâmicas (Android 12+) com opção para desativar.
- [x] **Modo "Não incomodar" agendado** (v1.36): ligar DND em horários/períodos além do Game Turbo.
- [x] **Verificador de apps com acessibilidade** (v1.36): listar apps com `BIND_ACCESSIBILITY_SERVICE` (requer atenção) e explicar riscos.
- [x] **Backup/restore de configurações** (v1.36): exportar/importar preferências JSON local (sem nuvem).
- [x] **Métricas de sessão Game Turbo** (v1.36): tempo em jogo, limpezas, ativação/desativação por sessão.
