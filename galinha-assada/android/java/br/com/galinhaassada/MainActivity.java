package br.com.galinhaassada;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;
import java.io.File;

public class MainActivity extends Activity {

    private WebView web;
    private GamepadBridge bridge;
    private long lastDownloadId = -1;
    private BroadcastReceiver dlReceiver;
    private boolean loadedFromFile = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        w.setStatusBarColor(Color.parseColor("#1a0524"));
        w.setNavigationBarColor(Color.parseColor("#1a0524"));

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0d0116"));
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.setLongClickable(false);
        web.setHapticFeedbackEnabled(false);
        web.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        web.setHorizontalScrollBarEnabled(false);
        web.setVerticalScrollBarEnabled(false);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // localStorage do recorde
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setTextZoom(100);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("GalinhaJS", "pagina carregada: " + url);
                // o jogo compara a versao publicada com a instalada
                try {
                    view.evaluateJavascript("window.__galinhaVC='" + appVersionCode() + "';", null);
                } catch (Throwable t) { }


            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request == null || request.getUrl() == null) return false;
                return openExternally(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return openExternally(url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request != null && request.isForMainFrame()){
                    Log.d("GalinhaJS", "erro de rede: " + request.getUrl() + " -> " + error.getDescription());
                    // se a copia salva estiver corrompida, volta para a embutida
                    if (loadedFromFile){
                        loadedFromFile = false;
                        try { File f = contentFile(); if (f.exists()) f.delete(); } catch (Throwable t) {}
                        view.loadUrl("file:///android_asset/index.html");
                    }
                }
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(android.webkit.ConsoleMessage cm) {
                Log.d("GalinhaJS", cm.message() + "  (" + cm.sourceId() + ":" + cm.lineNumber() + ")");
                return true;
            }

            @Override
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                Log.d("GalinhaJS", message + "  (" + sourceID + ":" + lineNumber + ")");
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
                                      android.webkit.JsPromptResult result) {
                // o WebView do Android nao tem prompt nativo: sem isto o jogo
                // nunca consegue pedir o apelido do ranking online
                runOnUiThread(new Runnable() {
                    public void run() {
                        android.widget.EditText input = new android.widget.EditText(MainActivity.this);
                        input.setSingleLine(true);
                        input.setText(defaultValue == null ? "" : defaultValue);
                        new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle(message == null ? "" : message)
                                .setView(input)
                                .setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
                                    public void onClick(android.content.DialogInterface d, int w) {
                                        result.confirm(input.getText().toString());
                                    }
                                })
                                .setNegativeButton("Cancelar", new android.content.DialogInterface.OnClickListener() {
                                    public void onClick(android.content.DialogInterface d, int w) {
                                        result.cancel();
                                    }
                                })
                                .show();
                    }
                });
                return true;
            }
        });
        bridge = new GamepadBridge();
        web.addJavascriptInterface(bridge, "AndroidGamepad");
        web.addJavascriptInterface(new UpdateBridge(), "AndroidUpdate");
        loadGame();

        setContentView(web);
        immersive();

        dlReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent i) {
                if (i == null) return;
                long id = i.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id != -1 && id == lastDownloadId) openDownloadedApk(id);
            }
        };
        try {
            registerReceiver(dlReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Throwable t) { }

        if ((getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            web.postDelayed(new Runnable() {
                public void run() {
                    web.evaluateJavascript("String(typeof window.__galinha)", jsLog("hook"));
                }
            }, 6000);
        }
    }

    private static final String onlineServer = "https://iptv-srv.tail045fd4.ts.net/galinha";

    /* ---- atualizacao do proprio jogo, sem baixar nem instalar APK ----
       o jogo novo (so o index.html) fica no armazenamento do app e e
       carregado por cima do arquivo embutido. */
    private File contentFile(){ return new File(getFilesDir(), "index.html"); }

    private void loadGame(){
        File f = contentFile();
        boolean tem = f.exists() && f.length() > 20000;
        loadedFromFile = tem;
        if (tem) web.loadUrl("file://" + f.getAbsolutePath());
        else web.loadUrl("file:///android_asset/index.html");
    }

    private void saveContentFile(final String html){
        if (html == null || html.length() < 20000){
            postJs("window.__galinhaUpdateFail&&window.__galinhaUpdateFail('arquivo invalido')");
            return;
        }
        try {
            File f = contentFile();
            File tmp = new File(getFilesDir(), "index.new");
            java.io.FileWriter w = new java.io.FileWriter(tmp);
            w.write(html);
            w.close();
            if (f.exists()) f.delete();
            if (!tmp.renameTo(f)){
                java.io.FileInputStream in = new java.io.FileInputStream(tmp);
                java.io.FileOutputStream out = new java.io.FileOutputStream(f);
                byte[] buf = new byte[8192]; int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                in.close(); out.close();
                tmp.delete();
            }
        } catch (Throwable t){
            Log.d("GalinhaJS", "saveContent falhou: " + t);
            postJs("window.__galinhaUpdateFail&&window.__galinhaUpdateFail('erro ao salvar')");
            return;
        }
        runOnUiThread(new Runnable() {
            public void run() { loadGame(); }
        });
    }

    private int appVersionCode(){
        try {
            android.content.pm.PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) return (int) pi.getLongVersionCode();
            return pi.versionCode;
        } catch (Throwable t){ return 0; }
    }

    // links externos (download do APK, repositorio) saem do WebView para o navegador
    private boolean openExternally(String url){
        if (url == null || url.isEmpty()) return false;
        if (url.startsWith("file://") || url.startsWith("about:") || url.startsWith("data:")) return false;
        try {
            Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(it);
        } catch (Throwable t){
            Log.d("GalinhaJS", "nao consegui abrir fora: " + url);
        }
        return true;
    }

    private android.webkit.ValueCallback<String> jsLog(final String label) {
        return new android.webkit.ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.d("GalinhaJS", label + " = " + value);
            }
        };
    }

    private void immersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) immersive();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bridge != null) bridge.axis(0, 0, 0, 0);   // solta o manche ao sair da tela
        ultX = 999; ultY = 999; ultHx = 999; ultHy = 999;
        if (web != null) web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) web.onResume();
        immersive();
        // se o APK ja terminou de baixar enquanto o app estava em segundo plano
        if (lastDownloadId != -1) openDownloadedApk(lastDownloadId);
    }

    @Override
    protected void onDestroy() {
        try { if (dlReceiver != null) unregisterReceiver(dlReceiver); } catch (Throwable t) {}
        if (web != null) {
            web.loadUrl("about:blank");
            web.destroy();
        }
        super.onDestroy();
    }

    /* ---- controle bluetooth: os eixos do manche nao chegam no WebView,
       entao lemos direto do Android e mandamos para o jogo ---- */
    private class GamepadBridge {
        @JavascriptInterface
        public void axis(float x, float y, float hx, float hy) {
            postJs("window.__gpAxis&&window.__gpAxis(" + x + "," + y + "," + hx + "," + hy + ")");
        }
        @JavascriptInterface
        public void btn(int id, boolean down) {
            postJs("window.__gpBtn&&window.__gpBtn(" + id + "," + down + ")");
        }
    }

    // ponte do verificador de atualizacao: baixa o APK dentro do app
    private class UpdateBridge {
        @JavascriptInterface
        public void downloadApk(final String url, final String fileName) {
            runOnUiThread(new Runnable() {
                public void run() { startApkDownload(url, fileName); }
            });
        }
        @JavascriptInterface
        public void versionCode() {
            postJs("window.__galinhaVC='" + appVersionCode() + "'");
        }
        // o jogo novo: o app salva e recarrega sozinho, sem instalar nada
        @JavascriptInterface
        public void saveContent(String html) {
            saveContentFile(html);
        }
        // volta para a versao que veio embutida no APK
        @JavascriptInterface
        public void resetContent() {
            try { File f = contentFile(); if (f.exists()) f.delete(); } catch (Throwable t) {}
            runOnUiThread(new Runnable() {
                public void run() { loadGame(); }
            });
        }
    }

    private void startApkDownload(String url, String fileName){
        if (url == null || url.trim().isEmpty()) return;
        String name = (fileName == null || fileName.trim().isEmpty()) ? "galinha-assada.apk" : fileName.trim();
        try {
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url.trim()));
            req.setTitle("Galinha Assada");
            req.setDescription("Baixando " + name);
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setMimeType("application/vnd.android.package-archive");
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
            lastDownloadId = dm.enqueue(req);
            android.widget.Toast.makeText(this, "Baixando " + name + "…", android.widget.Toast.LENGTH_LONG).show();
        } catch (Throwable t){
            Log.d("GalinhaJS", "download falhou: " + t);
            openExternally(url);
        }
    }

    private void openDownloadedApk(long id){
        try {
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            Uri uri = dm.getUriForDownloadedFile(id);
            if (uri == null) return;
            lastDownloadId = -1;
            Intent inst = new Intent(Intent.ACTION_VIEW);
            inst.setDataAndType(uri, "application/vnd.android.package-archive");
            inst.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(inst);
        } catch (Throwable t){
            Log.d("GalinhaJS", "instalador nao abriu: " + t);
            android.widget.Toast.makeText(this, "APK baixado em Downloads. Abra o arquivo para instalar.",
                    android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void postJs(final String js) {
        if (web == null) return;
        web.post(new Runnable() {
            public void run() {
                try { web.evaluateJavascript(js, null); } catch (Throwable t) {}
            }
        });
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        int src = event.getSource();
        boolean pad = (src & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
                || (src & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD;
        if (pad) {
            float x = event.getAxisValue(MotionEvent.AXIS_X);
            float y = event.getAxisValue(MotionEvent.AXIS_Y);
            if (x == 0f && y == 0f) {
                // alguns controles reportam o manche em Z/RZ
                float z = event.getAxisValue(MotionEvent.AXIS_Z);
                float rz = event.getAxisValue(MotionEvent.AXIS_RZ);
                if (z != 0f || rz != 0f) { x = z; y = rz; }
            }
            float hx = event.getAxisValue(MotionEvent.AXIS_HAT_X);
            float hy = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
            if (bridge != null && eixoMudou(x, y, hx, hy)) bridge.axis(x, y, hx, hy);
            return true;
        }
        return super.dispatchGenericMotionEvent(event);
    }

    // evita inundar o WebView com eventos iguais (o jogo le uma vez por quadro)
    private float ultX = 999, ultY = 999, ultHx = 999, ultHy = 999;
    private boolean eixoMudou(float x, float y, float hx, float hy) {
        boolean mudou = Math.abs(x - ultX) > 0.01f || Math.abs(y - ultY) > 0.01f
                || Math.abs(hx - ultHx) > 0.01f || Math.abs(hy - ultHy) > 0.01f;
        if (mudou) { ultX = x; ultY = y; ultHx = hx; ultHy = hy; }
        return mudou;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int id = -1;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_UP: id = 0; break;
            case KeyEvent.KEYCODE_DPAD_RIGHT: id = 1; break;
            case KeyEvent.KEYCODE_DPAD_DOWN: id = 2; break;
            case KeyEvent.KEYCODE_DPAD_LEFT: id = 3; break;
            case KeyEvent.KEYCODE_BUTTON_A: id = 4; break;
            case KeyEvent.KEYCODE_BUTTON_B: id = 5; break;
            case KeyEvent.KEYCODE_BUTTON_START: id = 6; break;
            case KeyEvent.KEYCODE_BUTTON_SELECT: id = 7; break;
            default: break;
        }
        if (id >= 0) {
            boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
            if (bridge != null) bridge.btn(id, down);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Primeiro Voltar pausa o jogo (se estiver jogando). Segundo Voltar fecha.
            if (web != null) {
                web.evaluateJavascript(
                        "(typeof pauseGame === 'function') ? pauseGame() : false",
                        new android.webkit.ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                if (!"true".equals(value)) finish();
                            }
                        });
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}