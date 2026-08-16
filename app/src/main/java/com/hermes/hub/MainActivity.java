package com.hermes.hub;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Handler;

public class MainActivity extends Activity {
    private WebView webView;
    private Handler handler = new Handler();
    private Runnable pingRunnable;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadsImagesAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // إعادة المحاولة التلقائية عند انقطاع الشبكة
                handler.postDelayed(() -> webView.reload(), 3000);
            }
        });

        // تحميل واجهة السيرفر
        webView.loadUrl("http://187.124.6.112:8089");
        setContentView(webView);

        // بدء خدمة الحفاظ على الاتصال في الخلفية
        Intent serviceIntent = new Intent(this, ConnectionKeepAliveService.class);
        startService(serviceIntent);

        // آلية فحص الاتصال وإعادة التحميل الذاتي كل 10 ثوانٍ إذا حدث انقطاع
        pingRunnable = new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript("navigator.onLine", online -> {
                    if ("false".equals(online)) {
                        webView.reload();
                    }
                });
                handler.postDelayed(this, 10000);
            }
        };
        handler.postDelayed(pingRunnable, 10000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && pingRunnable != null) {
            handler.removeCallbacks(pingRunnable);
        }
    }
}
