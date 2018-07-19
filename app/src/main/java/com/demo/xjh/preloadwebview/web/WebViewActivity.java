package com.demo.xjh.preloadwebview.web;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.demo.xjh.preloadwebview.R;
import com.demo.xjh.preloadwebview.widget.CacheWebView;

import static android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK;

public class WebViewActivity extends AppCompatActivity {
    CacheWebView mWebView = null;

    public static void start(Context context, String url) {
        Intent starter = new Intent(context, WebViewActivity.class);
        starter.putExtra("url", url);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        mWebView = findViewById(R.id.cusWebView);
        initWebView();
        String url = getIntent().getStringExtra("url");
        mWebView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void initWebView() {
        mWebView.getSettings().setCacheMode(LOAD_CACHE_ELSE_NETWORK);
    }
}
