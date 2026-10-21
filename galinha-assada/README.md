# Galinha Assada: Açaí & Peixe

Jogo de labirinto estilo arcade, no estilo Pac-Man, com a pegada do Brasil: a
galinha foge pelos corredores atrás de açaí e peixe enquanto os gatos tentam te
pegar.

O jogo é uma **PWA** — o mesmo `index.html` roda no navegador, no celular e
dentro do app Android (que é só um WebView com um pontinho de ponte para
gamepad Bluetooth).

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
