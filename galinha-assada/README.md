# Galinha Assada: Açaí & Peixe

Jogo de labirinto no estilo arcade, com a pegada do Pac-Man: a galinha corre
pelos corredores atrás de açaí e peixe enquanto os gatos tentam te pegar.

O mesmo `index.html` roda no navegador, no celular e dentro do app Android (que
é só um WebView com um pontinho de ponte para gamepad Bluetooth).

## Estrutura

| Caminho | O que é |
| --- | --- |
| `index.html` | o jogo inteiro (HTML + CSS + JS num arquivo só) |
| `manifest.webmanifest` | manifesto do PWA |
| `sw.js` | service worker (cache offline do PWA) |
| `icons/` | ícones do PWA e do launcher |
| `android/` | empacotamento Android (sem Gradle) |

## Rodar no navegador

Abra `index.html` num navegador. Para testar com gamepad Bluetooth no
computador, o Chrome funciona; no celular use o app Android.

## Testes

O jogo roda em Node com um DOM mínimo, sem navegador e sem rede:

```bash
node test/harness.js   # 37 verificações (curva, capítulos, receitas, ciclo do jogo)
node test/bugs.js      # stress: 50 fases, invariantes e o atualizador
```

O `harness.js` cobre o modo Zen (dificuldade nova, ciclo do menu, HUD, morte
sem perder vida, chefe e clone continuam valendo) e a regressão das outras
dificuldades. O `bugs.js` roda 50 fases no normal e no Zen, checa
posições/NaN/velocidade, se a vida nunca fica negativa e o fluxo de atualização
(consulta barata, URL válida e download do jogo novo).

## Ranking online

O placar online usa um servidor HTTP próprio, exposto publicamente por
Tailscale Funnel em `https://iptv-srv.tail045fd4.ts.net/galinha` (padrão em
`ONLINE_SERVER_DEFAULT`, em `index.html`). O endereço pode ser trocado no
CONFIG do jogo ("Servidor online").

O app Android precisa da permissão `INTERNET` (e `ACCESS_NETWORK_STATE`) para o
WebView falar com o servidor — sem elas nenhuma requisição sai do app e o
ranking fica em "Offline (sem ranking online)". As chamadas ao servidor têm
timeout de 7 s, para a tela não ficar presa em "Carregando ranking online...".

Para ver o que o jogo fala com o servidor, conecte o aparelho por adb e rode:

```bash
adb logcat -s GalinhaJS:D
```

## Gerar o APK Android

O APK é montado sem Gradle (aapt2 + javac + d8 + apksigner), o que deixa o
build rápido e sem download de dependências:

```bash
./android/build.sh
```

Sai em `apk/galinha-assada.apk`.

Requisitos: JDK 17+, Android SDK com `platforms/android-34` e `build-tools`, e
`zip`/`unzip`. O script usa `ANDROID_HOME` (ou `~/android-sdk`).

O keystore de assinatura (`android/galinha.keystore`) **não** fica no
repositório. O script cria um novo se ele não existir, mas a assinatura precisa
ser sempre a mesma — senão o Android não aceita a atualização por cima do app
já instalado. No CI a chave vem do secret
`FDROID_GALINHA_KEYSTORE_BASE64` e a senha do secret
`FDROID_GALINHA_KEYSTORE_PASS` (variável de ambiente `KS_PASS`).

## Publicar no repositório F-Droid

O metadata do app fica em [`../fdroid/br.com.galinhaassada.yml`](../fdroid/br.com.galinhaassada.yml).
O workflow **Publicar repo F-Droid** compila este APK junto com o do
SeguroTurbo e publica os dois no mesmo repositório F-Droid.

Veja [`../docs/F-DROID.md`](../docs/F-DROID.md).

## Licença

GPL-3.0-only, igual ao resto deste repositório.
