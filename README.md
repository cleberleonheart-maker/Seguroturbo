# SeguroTurbo

[![Android CI](https://github.com/cleberleonheart-maker/Seguroturbo/actions/workflows/android.yml/badge.svg)](https://github.com/cleberleonheart-maker/Seguroturbo/actions/workflows/android.yml)
[![Release](https://img.shields.io/github/v/release/cleberleonheart-maker/Seguroturbo?label=release)](https://github.com/cleberleonheart-maker/Seguroturbo/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/cleberleonheart-maker/Seguroturbo/total?label=downloads)](https://github.com/cleberleonheart-maker/Seguroturbo/releases)
[![Plataforma](https://img.shields.io/badge/plataforma-Android-3DDC84?logo=android&logoColor=white)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)

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

Projeto de exemplo educacional. Use e modifique livremente.
