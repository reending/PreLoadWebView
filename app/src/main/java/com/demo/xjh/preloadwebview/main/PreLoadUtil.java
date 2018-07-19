package com.demo.xjh.preloadwebview.main;

import android.content.Context;

import com.demo.xjh.preloadwebview.widget.CacheWebView;
import com.demo.xjh.preloadwebview.widget.CustomWebView;

import java.util.concurrent.ConcurrentSkipListMap;

public class PreLoadUtil {

    private CacheWebView mCacheWebView;
    private ConcurrentSkipListMap<String, OnLoadListener> mMap = new ConcurrentSkipListMap<>();
    private String mCurrentUrl = null;

    public PreLoadUtil(Context context) {
        init(context);
    }

    private void init(Context context) {
        mCacheWebView = new CacheWebView(context);
        mCacheWebView.setOnPageSuccessListener(view -> {
            OnLoadListener onLoadListener = mMap.get(mCurrentUrl);
            if (onLoadListener != null) {
                onLoadListener.success(mCurrentUrl);
            }
            mMap.remove(mCurrentUrl);
            if (!mMap.isEmpty()) {
                mCurrentUrl = mMap.firstKey();
                mCacheWebView.loadUrl(mCurrentUrl);
            }
        });
        mCacheWebView.setOnReceivedErrorListener((errorCode, description, failingUrl) -> {
            OnLoadListener onLoadListener = mMap.get(mCurrentUrl);
            if (onLoadListener != null) {
                onLoadListener.error(mCurrentUrl);
            }
            mMap.remove(mCurrentUrl);
            if (!mMap.isEmpty()) {
                mCurrentUrl = mMap.firstKey();
                mCacheWebView.loadUrl(mCurrentUrl);
            }
        });
        mCacheWebView.setOnProcessListener(new CustomWebView.onProcessListener() {
            @Override
            public void onProcess(int process) {
                OnLoadListener onLoadListener = mMap.get(mCurrentUrl);
                if (onLoadListener != null) {
                    onLoadListener.process(mCurrentUrl, process);
                }
            }
        });
    }

    public void loadUrl(String url, OnLoadListener listener) {
        if (mMap.isEmpty()) {
            mMap.put(url, listener);
            mCurrentUrl = url;
            mCacheWebView.loadUrl(url);
        } else {
            mMap.put(url, listener);
        }
    }

    public void reset() {
        mMap.clear();
    }

    interface OnLoadListener {
        void success(String url);

        void process(String url, int process);

        void error(String url);
    }

}
