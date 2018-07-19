package com.demo.xjh.preloadwebview.main;

import android.content.Context;

import com.demo.xjh.preloadwebview.widget.CacheWebView;

import java.util.concurrent.ConcurrentSkipListMap;

public class PreLoadUtil {

    private CacheWebView mCacheWebView;
    private ConcurrentSkipListMap<String, onLoadListener> mMap = new ConcurrentSkipListMap<>();
    private String mCurrentUrl = null;

    public PreLoadUtil(Context context) {
        init(context);
    }

    private void init(Context context) {
        mCacheWebView = new CacheWebView(context);
        mCacheWebView.setOnPageSuccessListener(view -> {
            mMap.get(mCurrentUrl).success(mCurrentUrl);
            mMap.remove(mCurrentUrl);
            if (!mMap.isEmpty()) {
                mCurrentUrl = mMap.firstKey();
                mCacheWebView.loadUrl(mCurrentUrl);
            }
        });
        mCacheWebView.setOnReceivedErrorListener((errorCode, description, failingUrl) -> {
            mMap.get(mCurrentUrl).error(mCurrentUrl);
            mMap.remove(mCurrentUrl);
            if (!mMap.isEmpty()) {
                mCurrentUrl = mMap.firstKey();
                mCacheWebView.loadUrl(mCurrentUrl);
            }
        });
    }

    public void loadUrl(String url, onLoadListener listener) {
        if (mMap.isEmpty()) {
            mCurrentUrl = url;
            mCacheWebView.loadUrl(url);
        }
        mMap.put(url, listener);
    }

    interface onLoadListener {
        void success(String url);

        void error(String url);
    }

}
