package com.demo.xjh.preloadwebview.main;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.demo.xjh.preloadwebview.R;
import com.demo.xjh.preloadwebview.web.WebViewActivity;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

public class MainActivity extends AppCompatActivity {

    String[] mUrls = {
            "https://sspai.com/post/44735",
            "https://sspai.com/post/44667",
            "https://sspai.com/post/44776",
            "https://sspai.com/post/44759",
            "https://sspai.com/post/44775",
            "https://sspai.com/post/44738",
            "https://sspai.com/post/44669",
            "https://sspai.com/post/44710",
            "https://sspai.com/post/44748",
            "https://sspai.com/post/44763",
            "https://sspai.com/post/44715",
            "https://sspai.com/post/44692",
            "https://sspai.com/post/44751",
            "https://sspai.com/post/44706",
            "https://sspai.com/post/44693",
            "https://sspai.com/post/44745",
            "https://sspai.com/post/44720",
            "https://sspai.com/post/44688",
            "https://sspai.com/post/44723",
            "https://sspai.com/post/44728",
            "https://sspai.com/post/44654",
            "https://sspai.com/post/44714",
            "https://sspai.com/post/44716",
            "https://sspai.com/post/44701",
            "https://sspai.com/post/44687",
            "https://sspai.com/post/44681",
            "https://sspai.com/post/44702",
            "https://sspai.com/post/44653",
            "https://sspai.com/post/44656",
            "https://sspai.com/post/44683",
            "https://sspai.com/post/44668",
            "https://sspai.com/post/44647"
    };

    private RecyclerView mRvMain;
    private MainAdapter mAdapter;
    private List<UrlItem> mList = new ArrayList<>();
    private Handler mHandler = new Handler();
    private PreLoadUtil mPreLoadUtil;
    private LinearLayoutManager mLayoutManager;
    //滑动监听
    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            dealState(newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreLoadUtil = new PreLoadUtil(this);
        initRecyclerView();
        initData();
        postPreLoad();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 滑动状态改变时，静止状态开始计时三秒，其他状态取消计时
     *
     * @param newState
     */
    private void dealState(int newState) {
        switch (newState) {
            case SCROLL_STATE_IDLE:
                postPreLoad();
                break;
            default:
                mHandler.removeCallbacksAndMessages(null);
                break;
        }
    }

    private void postPreLoad() {
        mHandler.postDelayed(() -> {
            List<UrlItem> items = getVisibleItem();
            mPreLoadUtil.reset();
            for (UrlItem urlItem : items) {
                if (!UrlItem.SUCCESS.equals(urlItem.state)) {
                    loadItem(urlItem);
                }
            }
        }, 3000);
    }

    private void loadItem(UrlItem urlItem) {
        mPreLoadUtil.loadUrl(urlItem.url, new PreLoadUtil.OnLoadListener() {
            @Override
            public void success(String url) {
                urlItem.state = UrlItem.SUCCESS;
                mAdapter.notifyItemChanged(mList.indexOf(urlItem));
            }

            @Override
            public void process(String url, int process) {
                urlItem.state = UrlItem.LOADING +" : "+ process;
                mAdapter.notifyItemChanged(mList.indexOf(urlItem));
            }

            @Override
            public void error(String url) {
                urlItem.state = UrlItem.ERROR;
                mAdapter.notifyItemChanged(mList.indexOf(urlItem));
            }
        });
    }

    /**
     * @return 当前可见项
     */
    @NonNull
    private List<UrlItem> getVisibleItem() {
        List<UrlItem> list = new ArrayList<>();
        int first = mLayoutManager.findFirstCompletelyVisibleItemPosition();
        int last = mLayoutManager.findLastCompletelyVisibleItemPosition();
        for (int i = first; i < last + 1; i++) {
            list.add(mList.get(i));
        }
        return list;
    }

    private void initData() {
        for (String mUrl : mUrls) {
            UrlItem urlItem = new UrlItem();
            urlItem.url = mUrl;
            urlItem.state = "未加载";
            mList.add(urlItem);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void initRecyclerView() {
        mRvMain = findViewById(R.id.rvMain);
        mLayoutManager = new LinearLayoutManager(this);
        mRvMain.setLayoutManager(mLayoutManager);
        mRvMain.addOnScrollListener(mScrollListener);

        mAdapter = new MainAdapter();
        mAdapter.setOnItemClickListener(url -> WebViewActivity.start(this, url));
        mAdapter.setData(mList);

        mRvMain.setAdapter(mAdapter);
    }

}
