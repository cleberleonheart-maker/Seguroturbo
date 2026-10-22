package br.com.galinhaassada;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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

public class MainActivity extends Activity {

    private WebView web;
    private GamepadBridge bridge;

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
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request != null && request.isForMainFrame())
                    Log.d("GalinhaJS", "erro de rede: " + request.getUrl() + " -> " + error.getDescription());
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
        web.loadUrl("file:///android_asset/index.html");

        setContentView(web);
        immersive();

        if ((getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            web.postDelayed(new Runnable() {
                public void run() {
                    web.evaluateJavascript("String(typeof window.__galinha)", jsLog("hook"));
                    web.evaluateJavascript(
                            "fetch('" + onlineServer + "/health').then(r=>console.log('DIAG_GET_OK '+r.status)).catch(e=>console.log('DIAG_GET_ERRO '+e))",
                            jsLog("fetch"));
                }
            }, 6000);
        }
    }

    private static final String onlineServer = "https://iptv-srv.tail045fd4.ts.net/galinha";

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
    }

    @Override
    protected void onDestroy() {
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