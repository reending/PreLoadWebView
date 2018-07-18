package com.demo.xjh.preloadwebview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

public class WebViewActivity extends AppCompatActivity {
    CustomWebView mWebView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        mWebView = findViewById(R.id.cusWebView);
        initWebView();
        mWebView.loadUrl("https://shimo.im/docs/J3fdaU7eCkcd5tha");
    }

    private void initWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setOnShouldInterceptRequest(new CustomWebView.OnShouldInterceptRequest() {
            @NotNull
            @Override
            public WebResourceResponse shouldInterceptRequest(@NotNull WebView view, @NotNull WebResourceRequest request) {
                String imgUrl = request.getUrl().toString();
                if (imgUrl.contains(".jpg")) {
                    URLConnection connection = getIS(imgUrl);
                    try {
                        if (connection != null) {

                            return new WebResourceResponse(connection.getContentType(), connection.getHeaderField("encoding"), connection.getInputStream());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            private URLConnection getIS(String imgUrl) {
                try {
                    URL url = new URL(imgUrl);
                    return url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }
}
