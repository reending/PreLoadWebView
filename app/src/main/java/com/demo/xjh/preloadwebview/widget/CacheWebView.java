package com.demo.xjh.preloadwebview.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CacheWebView extends CustomWebView {
    private ExecutorService mThreadPool = Executors.newCachedThreadPool();

    public CacheWebView(@NotNull Context context) {
        super(context);
        init();
    }

    public CacheWebView(@NotNull Context context, @NotNull AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CacheWebView(@NotNull Context context, @NotNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CacheWebView(@NotNull Context context, @NotNull AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public CacheWebView(@NotNull Context context, @NotNull AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        init();
    }

    private void init() {
        //打开 IndexedDB 缓存
        getSettings().setJavaScriptEnabled(true);
        setOnShouldInterceptRequest();
    }

    /**
     * jpg 图片缓存到data/data/package
     */
    private void setOnShouldInterceptRequest() {
        Context applicationContext = getContext().getApplicationContext();
        setOnShouldInterceptRequest(new CustomWebView.OnShouldInterceptRequest() {
            @Override
            public WebResourceResponse shouldInterceptRequest(@NotNull WebView view, @NotNull WebResourceRequest request) {
                String imgUrl = request.getUrl().toString();
                if (imgUrl.contains(".jpg")) {
                    //图片使用url哈希后作为文件名
                    String ext = imgUrl.hashCode() + ".jpg";
                    if (cachedImageIsExist(ext)) {
                        File file = new File(applicationContext.getCacheDir(), ext);
                        try {
                            FileInputStream inputStream = new FileInputStream(file);
                            //从本地读取图片
                            return new WebResourceResponse("image/jpg", "UTF-8", inputStream);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //图片用线程池存到本地
                        final HttpURLConnection connection = getConnection(imgUrl);
                        if (connection != null) {
                            mThreadPool.execute(() -> cacheImage(connection));
                        }
                    }
                }
                return null;
            }

            private HttpURLConnection getConnection(String imgUrl) {
                try {
                    URL url = new URL(imgUrl);
                    return (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            private boolean cachedImageIsExist(String path) {
                File file = new File(applicationContext.getCacheDir(), path);
                return file.exists() && file.length() > 0;
            }

            private void cacheImage(HttpURLConnection conn) {
                String imgUrl = conn.getURL().toString();
                String ext = imgUrl.hashCode() + ".jpg";
                File cacheDir = new File(applicationContext.getCacheDir(), ext);
                InputStream inputStream = null;
                FileOutputStream outStream = null;
                try {
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(2000);
                    if (conn.getResponseCode() == 200) {
                        inputStream = conn.getInputStream();
                        byte[] buffer = new byte[4096];
                        int len;
                        outStream = new FileOutputStream(cacheDir);
                        while ((len = inputStream.read(buffer)) != -1) {
                            outStream.write(buffer, 0, len);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outStream != null) {
                            outStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
