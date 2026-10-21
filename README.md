# SeguroTurbo

[![Android CI](https://github.com/cleberleonheart-maker/Seguroturbo/actions/workflows/android.yml/badge.svg)](https://github.com/cleberleonheart-maker/Seguroturbo/actions/workflows/android.yml)
[![Release](https://img.shields.io/github/v/release/cleberleonheart-maker/Seguroturbo?label=release)](https://github.com/cleberleonheart-maker/Seguroturbo/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/cleberleonheart-maker/Seguroturbo/total?label=downloads)](https://github.com/cleberleonheart-maker/Seguroturbo/releases)
[![Plataforma](https://img.shields.io/badge/plataforma-Android-3DDC84?logo=android&logoColor=white)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![F-Droid repo](https://img.shields.io/badge/F--Droid-reposit%C3%B3rio-1976D2?logo=fdroid&logoColor=white)](https://cleberleonheart-maker.github.io/seguraturbo-fdroid/repo)
[![Repositório F-Droid](https://img.shields.io/badge/repo-seguraturbo--fdroid-181717?logo=github&logoColor=white)](https://github.com/cleberleonheart-maker/seguraturbo-fdroid)

## Instalação

A forma mais fácil de instalar e receber atualizações automáticas é pelo
**F-Droid** (repositório próprio, sem depender da loja oficial):

**1.** No app F-Droid, abra **Configurações → Repositórios → +** e cole:

```
https://cleberleonheart-maker.github.io/seguraturbo-fdroid/repo
```

**2.** Ou escaneie o QR code com o F-Droid / celular:

<a href="https://cleberleonheart-maker.github.io/seguraturbo-fdroid/repo">
  <img src="docs/fdroid-repo-qr.png" alt="QR code do repositório F-Droid do SeguroTurbo" width="180">
</a>

Fingerprint da chave do repositório (SHA-256) para conferir a autenticidade:

```
92:FB:1F:52:80:E4:F8:26:32:72:12:81:B0:BB:55:59:AC:D9:93:E7:FE:A0:7A:67:10:D5:3F:6C:97:6E:73:34
```

Também dá para baixar o APK direto das
[**releases do GitHub**](https://github.com/cleberleonheart-maker/Seguroturbo/releases/latest).


Aplicativo Android que une três módulos em um só app, com tema futurista neon:

1. **Segurança** — verifica de verdade o estado do aparelho (root, bloqueio de tela,
   acessibilidade, instalação de fontes desconhecidas, opções de desenvolvedor, apps com
   permissões sensíveis e cache acumulado), mostra uma **pontuação 0–100** e permite
   **corrigir** cada alerta abrindo a tela de configuração correspondente.
2. **Game Turbo** — modo jogo e "otimização", listando os **jogos realmente instalados**
   no dispositivo, permitindo **abrir o jogo** com um toque e mostrando o status real de
   **otimização de bateria** (Doze) com atalho para ajustar.
3. **Benchmark** — mede de verdade a **CPU**, a **GPU (OpenGL ES offscreen)** e a
   velocidade do **armazenamento** (I/O), combinando com dados reais do aparelho num
   placar estilo AnTuTu, com histórico em gráfico e compartilhamento do resultado.

## Tecnologias

- **Linguagem:** Kotlin 1.9.10
- **UI:** Jetpack Compose + Material 3 (Compose 1.5.4, Compiler 1.5.3)
- **Arquitetura:** `ViewModel` + `StateFlow` de estado local (`mutableStateOf`)
- **SDK mínimo:** 24 (Android 7.0)
- **SDK alvo:** 34 (Android 14)
- **Otimização:** R8 / minify + shrinkResources no release

## Novidades da versão 1.34

- **Tema AMOLED**: modo escuro com **preto puro**, para economizar bateria em
  telas OLED — em pixels pretos o display fica desligado.
- **Filtro de alertas por categoria**: os alertas da tela de Segurança ganham
  chips por tipo (Sistema, Rede, Privacidade…), com a contagem de cada um.
- **Aparelho gerenciado / perfil de trabalho**: a varredura detecta (somente
  leitura) Device/Profile Owner e avisa, de forma informativa.
- **Atalhos no ícone** (toque longo): **Escanear**, **Limpar RAM** e **Testar
  rede** direto da tela inicial, disparando a ação ao abrir o app.

## Novidades da versão 1.33

- **Perfil de risco por app**: a auditoria de permissões agora cruza o acesso à
  **internet** com as permissões sensíveis de cada app e classifica em **alto,
  médio ou baixo risco**. O cartão mostra um resumo e a lista fica ordenada dos
  apps mais arriscados para os menos. Tudo offline, sem coletar nada.

## Novidades da versão 1.32

- **Inventário exportável**: na auditoria de permissões, um botão gera um **CSV**
  com todos os apps visíveis e as permissões sensíveis que cada um tem — bom para
  guardar, revisar com calma ou levar para outra ferramenta. Tudo local, sem
  enviar dados para lugar nenhum.

## Novidades da versão 1.31

- **Detector de stalkerware**: a varredura de segurança agora procura **apps de
  vigilância/espionagem conhecidos** (lista curada, offline) e sinaliza apps com
  **combinação incomum de permissões** (SMS + localização + contatos/microfone).
  Cada alerta tem um botão que abre os detalhes do app para desinstalar. Tudo no
  aparelho, sem enviar dados para lugar nenhum.

## Novidades da versão 1.30

- **Perfis de uso**: presets **Padrão**, **Gamer**, **Casual**, **Bateria** e
  **Segurança máxima** que aplicam várias configurações de uma vez (perfil de
  desempenho, Não Perturbe, trava de tela, silenciar mídia, lembrete e atualização
  automática). Tudo local, sem conta nem nuvem.

## Novidades da versão 1.29

- **Silenciar só mídia no jogo**: no Game Turbo, uma opção abaixa o volume de
  músicas e vídeos durante o **Modo Jogo** sem mexer em chamadas, alarmes e
  notificações. O volume anterior é restaurado quando o Modo Jogo é desligado.

## Novidades da versão 1.28

- **Bloqueio de tela (screen pinning) no Game Turbo**: ao abrir um jogo com a
  opção ligada, a tela fica fixa no jogo para evitar toques acidentais; para
  sair, toque e segure **Voltar + Recentes**. Se o bloqueio de tela estiver
  desligado nas configurações do sistema, o app avisa e mostra o atalho para
  ativá-lo.

## Novidades da versão 1.27.1

- Correções de polish: unidade do teste de toque localizada e remoção de textos
  não usados (APK um pouco menor).

## Novidades da versão 1.27

- **Testes instrumentados (Compose UI)**: testes automatizados dos fluxos
  principais (navegação, Segurança, Benchmark) rodando em aparelho/emulador
  (`./gradlew connectedDebugAndroidTest`).

## Novidades da versão 1.26

- **Teste de tela/touch**: no Benchmark, mede a resposta ao toque, os toques por
  segundo e o número máximo de dedos simultâneos (multitouch).

## Novidades da versão 1.25

- **Teste de rede**: no Benchmark, mede a latência (ms) e a velocidade de
  download (Mbps) da sua conexão.

## Novidades da versão 1.24

- **Limpeza de RAM**: no Game Turbo, veja o uso real de memória e escolha quais
  apps encerrar (processos em segundo plano) para liberar RAM.

## Novidades da versão 1.23

- **Integridade do sistema ampliada**: a varredura agora detecta indícios de root,
  o estado do **SELinux** (Enforcing/Permissive) e sinais de **bootloader
  desbloqueado/build de teste** (test-keys, userdebug/eng) — explicando cada caso.

## Novidades da versão 1.22

- **Auditoria de permissões**: nova seção na Segurança que mostra quais apps
  visíveis têm acesso concedido a câmera, microfone, localização, SMS, contatos,
  chamadas, telefone, sensores e calendário — com um resumo por permissão.

## Novidades da versão 1.21

- **Correção do auto-update**: o download da atualização agora roda fora da thread
  principal, corrigindo o erro “não foi possível baixar a atualização” no celular.

## Novidades da versão 1.20

- **Barra flutuante no jogo**: relógio, bateria e RAM com atalhos de captura de tela,
  gravação, Não Perturbe, Wi‑Fi, brilho e timer por cima do jogo.
- A barra aparece automaticamente ao abrir um jogo pelo Game Turbo.

## Novidades da versão 1.19

- **Compartilhar o gráfico**: o gráfico de evolução das pontuações do benchmark agora
  pode ser compartilhado como imagem (com última pontuação, mínimo e máximo).

## Novidades da versão 1.18

- **Changelog / Novidades**: tela que lista o que foi **adicionado** e **corrigido** em
  cada versão, exibida automaticamente após uma atualização e disponível nas configurações.
- **Correção do auto-update**: o download do APK agora segue manualmente os
  redirecionamentos do GitHub e, se ainda falhar, o app abre a página da release no navegador.

## Novidades da versão 1.17

- **Francês**: o app agora fala **português, inglês, espanhol e francês**.

## Novidades da versão 1.16

- **Backup e restauração**: salva as preferências e o histórico em um arquivo
  JSON e restaura quando quiser (tudo local, sem nuvem).
- **Checagem automática de atualizações**: uma vez por dia, o app verifica em
  segundo plano (WorkManager) se há versão nova e avisa por notificação.

## Novidades da versão 1.15

- **Espanhol**: o app agora fala português, inglês e espanhol (segue o idioma
  do sistema).
- **Cobertura de testes no CI**: o GitHub Actions gera um relatório de cobertura
  (JaCoCo) junto dos testes unitários.

## Novidades da versão 1.14

- **Tema automático**: agora dá para **seguir o sistema** (claro/escuro mudam
  sozinhos) ou fixar Claro/Escuro, em Configurações.
- **Comparação local no Benchmark**: mostra a **média** do aparelho, o **melhor
  resultado** e em que **posição** a sua nota atual ficou.

## Novidades da versão 1.13

- **Apps fora da loja** na Segurança: avisa quando há apps instalados sem vir
  da Play Store (sideload), listando os nomes.
- **Assinatura do app (SHA‑256)** nas configurações: mostra o certificado do
  app para você conferir que o APK é autêntico e não foi adulterado.

## Novidades da versão 1.12

- **Monitor térmico no Game Turbo**: mostra a **temperatura da bateria** em tempo
  real (Normal / Morno / Aquecido) e avisa quando passa de 42 °C.
- **Bloqueio automático da tela na Segurança**: novo alerta quando a tela demora
  mais de 5 minutos para bloquear sozinha, com atalho para as configurações.
- **Exportar histórico em CSV**: botão **CSV** no histórico gera um arquivo
  (abre bem no Excel pt‑BR) e compartilha pelo app que você escolher.

## Novidades da versão 1.11

- **Checagem de rede na Segurança**: o scan agora detecta quando o **Wi‑Fi conectado
  não tem senha** e informa se há **VPN ativa** e **DNS privado** — recursos que
  protegem o tráfego em redes públicas. O alerta abre as configurações de Wi‑Fi.
- **Perfis de desempenho no Game Turbo**: escolha entre **Economia**, **Equilibrado**
  e **Turbo**. O perfil Turbo liga o **Não Perturbe automático** junto com o Modo Jogo.

## Novidades da versão 1.10

- **Tela de boas-vindas (onboarding)**: no primeiro uso o app apresenta os três
  módulos (Segurança, Game Turbo e Benchmark) e explica que as permissões são pedidas
  **somente quando cada recurso é usado**. Aparece uma única vez, após a abertura.
- **Qualidade**: testes unitários novos para a **comparação de versões** (updates) e
  para o **cálculo da idade do patch de segurança**, e **lint** passou a rodar no CI.

## Novidades da versão 1.9

- **Atualização dentro do app (auto-update)**: o verificador agora **baixa o APK da
  release e abre o instalador do sistema**, sem passar pelo navegador. Mostra o
  **progresso do download**, confere a **assinatura** do arquivo (mesmo certificado do
  app) antes de instalar e, quando necessário, pede a autorização de "instalar apps
  desconhecidos".

## Novidades da versão 1.8

- **Não Perturbe automático (Game Turbo)**: nova opção que, com o acesso concedido,
  **silencia as notificações ao ativar o Modo Jogo** e restaura o modo anterior ao
  desligá-lo.
- **Checagens novas na segurança**:
  - **Patch de segurança**: alerta quando as últimas correções do sistema têm mais de
    6 meses (ou quando a data é desconhecida).
  - **Google Play Protect**: avisa quando a Play Store não está instalada ou a
    verificação de apps está desativada.
- **Documentação**: seção de **ideias futuras** em [`IDEIAS.md`](IDEIAS.md).

## Novidades da versão 1.7

- **Verificador de atualizações**: o app consulta a **última release publicada no GitHub**
  e avisa quando há uma versão mais nova, mostrando as notas e um botão para baixar.
- **Seção "Atualizações"** nas configurações, com a **versão instalada** e uma
  **checagem manual** ("Verificar"). A versão dispensada com "Depois" fica guardada.

## Novidades da versão 1.6

- **Relatório em PDF**: além da imagem, agora dá para **exportar o resultado em PDF (A4)**
  — bom para impressão e arquivo. Botões separados de **Imagem** e **PDF** nas telas de
  Segurança e Benchmark.
- **Detalhes de cada alerta**: toque em qualquer alerta para abrir um diálogo educativo com
  **"o que significa"** e **"como corrigir"** (com atalho direto para a configuração).
- **Suporte a inglês**: interface traduzida (`values-en`); o pt-BR continua padrão. No
  Android 13+ é possível escolher o idioma **por app** (com `locale-config`).
- **Atalhos no ícone** (App Shortcuts, Android 7.1+): toque longo no ícone abre direto
  **Segurança**, **Game Turbo** ou **Benchmark**.
- **Acessibilidade/UX**: abas com semântica de `Tab` (leitor de tela), botões com área de
  toque de 48 dp e descrição acessível no botão de configurações.

## Novidades da versão 1.5

- **Relatório em imagem**: os botões de compartilhar (Segurança e Benchmark) agora geram um
  **PNG** do resultado, desenhado no aparelho e enviado pela folha de compartilhamento
  (via `FileProvider`) — ótimo para prints e redes sociais.
- **Widget da tela inicial**: mostra o **último score de segurança (0–100)** e abre o app
  ao toque; atualiza sozinho quando você escaneia.
- **Lembrete de verificação**: notificação opcional a cada 7 dias convidando a escanear
  (alarme inexato, amigável à bateria, sem permissão extra além da de notificação).
- **Baseline Profile**: perfil de inicialização embutido no APK — abertura mais rápida.
- **Qualidade**: **testes unitários** (JUnit) da formatação e da normalização do benchmark
  e **integração contínua** (GitHub Actions) que roda os testes e compila a cada push.
- **Build robusto**: script `build.sh` que roda o release em **duas passadas**, evitando
  falhas por falta de memória no R8 (a segunda passada reaproveita o cache).
- **Publicação**: geração do **Android App Bundle (.aab)** assinado para a Play Store e
  **política de privacidade** (`POLITICA-DE-PRIVACIDADE.md`).

## Novidades da versão 1.4

- **Segurança acionável**:
  - Novas verificações reais: **bloqueio de tela**, **serviços de acessibilidade ativos**
    e **apps com permissões sensíveis** (câmera, microfone, localização, SMS…).
  - **Pontuação de segurança (0–100)** com cor conforme o risco.
  - Botão **Corrigir** em cada alerta, que abre a tela de configuração adequada
    (segurança, fontes desconhecidas, acessibilidade, opções de desenvolvedor) ou executa
    a limpeza de cache.
- **Benchmark real**:
  - **GPU medida com OpenGL ES** (renderização offscreen → FPS de verdade).
  - **Armazenamento medido** (escrita + leitura sequencial em MB/s).
  - **Gráfico** da evolução das pontuações e botão **Compartilhar resultado**.
- **Game Turbo**:
  - **Abrir o jogo** com um toque na lista.
  - Status real de **otimização de bateria** com atalho de ajuste.
  - Jogos otimizados agora **persistem** entre execuções.
- **Visual/UX**:
  - **Tema claro/escuro** com botão ⚙️ na barra superior.
  - **Tamanho da fonte** ajustável (Pequena / Normal / Grande) via `LocalDensity`.

## Novidades da versão 1.3

- **Correções de bugs** (13 no total):
  - Trabalho pesado saiu da thread principal (`Dispatchers.IO`/`Default`): benchmark de CPU,
    leitura do cache, limpeza e detecção de jogos não travam mais a interface.
  - Detecção de **fontes desconhecidas** corrigida para o Android 8+ (`canRequestPackageInstalls`).
  - Removido o "flash" claro na abertura (tema escuro + `windowBackground`) e o splash do
    Android 12+ alinhado ao fundo do app.
  - Splash não repete mais ao girar a tela (`rememberSaveable`).
  - Detecção de jogos também no Android 7 (flag `android:isGame`).
  - "Escanear" e "Limpar" não rodam mais ao mesmo tempo (botões bloqueados mutuamente).
  - Medição de CPU com "sorvedouro" volátil (impede o JIT de eliminar o laço).
  - Permissões não utilizadas removidas e `allowBackup` desativado.
  - Formato de data reaproveitado e recursos XML mortos removidos.

## Novidades da versão 1.2

- **Legibilidade dos botões**: novo estilo de **contorno neon** (fundo escuro, borda e
  rótulo na cor de destaque) — o texto passa a ser o elemento mais visível.
- **Barra de opções (abas)** com rótulos maiores (12sp), item ativo com pílula de destaque
  e inativos em branco-claro.
- **Contraste geral** melhorado: cinza secundário mais claro em todas as telas.

## Novidades da versão 1.1

- **Ícone neon próprio** (escudo + raio) com ícone adaptativo.
- **Splash screen** animada com a marca.
- **Animações**: transição entre abas, pontuação que "sobe" e pulso no scanner.
- **Dados reais** em vez de valores aleatórios.
- **Histórico** local das últimas ações (scan, limpeza, benchmark, turbo).
- Refatoração: lógica movida para `ViewModel`, paleta centralizada (`Cores`), textos em `strings.xml`.
- APK de release otimizado com R8 (**8 MB → 3 MB**).

## Estrutura do projeto

```
SeguroTurboApp/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── build.sh                      # build em 2 passadas (anti-OOM)
├── POLITICA-DE-PRIVACIDADE.md
├── .github/workflows/android.yml # CI: testes + build
└── app/
    ├── build.gradle              # Dependências, assinatura e R8
    ├── proguard-rules.pro
    ├── src/main/
    │   ├── AndroidManifest.xml
    │   ├── baseline-prof.txt      # Baseline Profile (abertura mais rápida)
    │   ├── java/com/seguraturbo/app/
    │   │   ├── MainActivity.kt
    │   │   ├── model/
    │   │   │   ├── Game.kt
    │   │   │   ├── BenchmarkResult.kt
    │   │   │   ├── HistoricoEntrada.kt
    │   │   │   └── Threat.kt
    │   │   ├── ui/
    │   │   │   ├── MainScreen.kt              (navegação + barra inferior + ⚙️ configurações)
    │   │   │   ├── splash/SplashScreen.kt
    │   │   │   ├── components/FuturisticComponents.kt  (cartões, botões, gráfico)
    │   │   │   ├── components/ConfiguracoesDialog.kt   (tema, fonte e lembrete)
    │   │   │   ├── components/DetalheAmeacaDialog.kt   (detalhe educativo por alerta)
    │   │   │   ├── theme/Cores.kt             (paleta clara/escura)
    │   │   │   ├── theme/Theme.kt             (tema + escala de fonte)
    │   │   │   ├── seguranca/  (Screen + ViewModel)
    │   │   │   ├── gameturbo/  (Screen + ViewModel)
    │   │   │   └── benchmark/  (Screen + ViewModel)
    │   │   ├── widget/SeguroTurboWidget.kt    (widget do score)
    │   │   └── util/
    │   │       ├── DeviceInfo.kt          (dados reais do aparelho)
    │   │       ├── SecurityScanner.kt     (verificações reais + pontuação)
    │   │       ├── AcoesSistema.kt        (abre telas de configuração / apps)
    │   │       ├── StorageCleaner.kt      (cache real)
    │   │       ├── JogosDetector.kt       (jogos reais instalados)
    │   │       ├── BenchmarkEngine.kt     (CPU real + orquestra GPU/armazenamento)
    │   │       ├── BenchmarkHardware.kt   (GPU via OpenGL ES + I/O de armazenamento)
    │   │       ├── BenchmarkStore.kt      (histórico de pontuações do gráfico)
    │   │       ├── RelatorioImagem.kt     (gera o PNG do relatório)
    │   │       ├── RelatorioPdf.kt        (gera o PDF do relatório)
    │   │       ├── Notificacoes.kt        (canal + lembrete)
    │   │       ├── Agendador.kt / LembreteReceiver.kt (alarme periódico)
    │   │       ├── Preferencias.kt        (tema, fonte, jogos, lembrete, score)
    │   │       ├── HistoricoStore.kt      (histórico via SharedPreferences)
    │   │       └── Formato.kt             (formatação de tamanhos/datas)
    │   └── res/
    │       ├── drawable/ic_*.xml          (ícones vetoriais)
    │       ├── layout/widget_seguraturbo.xml
    │       ├── xml/ (file_paths, widget_info, shortcuts, locales_config)
    │       ├── mipmap*/ic_launcher*.xml   (ícone adaptativo + fallback)
    │       └── values/ (strings, colors, themes)
    │       └── values-en/ (strings em inglês)
    └── src/test/java/...                  # Testes unitários (JUnit)
```

## Como gerar o APK pelo terminal

```bash
cd SeguroTurboApp

# Caminho recomendado: script anti-OOM (release + debug em 2 passadas)
./build.sh

# Ou manualmente:
./gradlew assembleDebug --no-daemon     # Debug (rápido, sem R8)
./gradlew assembleRelease --no-daemon   # Release assinado e otimizado (R8)

# Testes unitários
./gradlew test --no-daemon

# Android App Bundle (Play Store)
./gradlew bundleRelease --no-daemon
```

APKs gerados em `app/build/outputs/apk/`; o bundle em `app/build/outputs/bundle/release/`.

> **Dica (memória):** em máquinas pequenas o R8 pode ser encerrado por falta de memória na
> primeira passada. Rodar `assembleRelease` novamente resolve, pois o cache do Gradle pula a
> compilação Kotlin e o R8 roda num processo limpo. O `build.sh` já faz isso automaticamente.

Para abrir no **Android Studio**: *Open* → selecione a pasta `SeguroTurboApp` → aguarde o
Gradle → Run (▶).

## Observações importantes

- O módulo de **Segurança** faz verificações **reais** (root, bloqueio de tela, acessibilidade,
  fontes desconhecidas, opções de desenvolvedor, permissões sensíveis e cache) e oferece atalhos
  para corrigir, mas **não é um antivírus**: não analisa assinaturas de malware.
- O **Benchmark** mede **CPU, GPU (OpenGL ES) e armazenamento** de verdade; as notas de RAM são
  derivadas da RAM real do aparelho. A pontuação final é uma normalização dos valores medidos.
- O **Game Turbo** lista e abre jogos reais e consulta a otimização de bateria do sistema, mas
  não altera configurações do sistema por conta própria (isso exigiria Game Mode API ou APIs do
  fabricante).

## Licença

[GPL-3.0](LICENSE) — você pode usar, modificar e redistribuir livremente, desde que
mantenha a mesma licença.

## Doação

Se o app te ajudou, considere contribuir (links a preencher pelo mantenedor):

- **Pix**: `__CHAVE_PIX__`
- **PayPal**: `__LINK_PAYPAL__`

## Publicação no F-Droid

O projeto está preparado para o F-Droid:

- `LICENSE` com a GPL-3.0 (exigência do F-Droid)
- Metadados `fastlane/metadata/android/` (pt-BR, en-US, es-ES, fr-FR)

A submissão ao repositório **oficial** foi recusada (template do MR não seguido
e política dos 3 meses de manutenção). Enquanto isso, há um **repositório F-Droid
próprio** publicado e funcionando — veja como adicionar na seção
[**Instalação**](#instalação) no topo deste README.

> A chave do repositório foi regerada em 20/10/2026. Se você já tinha adicionado
> este repositório, remova e adicione de novo para aceitar a nova chave.

O MR oficial pode ser reaberto depois de cumprir a política dos **3 meses de
manutenção** (primeiro commit em 16/10/2026 — reabrir a partir de **16/01/2027**,
usando o template oficial de "New app").

Para gerar/atualizar esse repositório localmente:

```bash
pip install fdroidserver
./fdroid/gerar-repo.sh ~/fdroid-repo
```

Para publicar uma release nova de ponta a ponta (baixa o APK, regenera o índice
e publica no GitHub Pages) num comando só:

```bash
./scripts/publicar-repo.sh          # usa a última release
./scripts/publicar-repo.sh v1.30    # ou uma tag específica
```

O passo a passo completo está em [docs/F-DROID.md](docs/F-DROID.md).
