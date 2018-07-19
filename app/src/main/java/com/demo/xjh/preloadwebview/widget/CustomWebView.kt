package com.demo.xjh.preloadwebview.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.net.http.SslError
import android.support.annotation.LayoutRes
import android.support.v4.content.ContextCompat
import android.util.ArrayMap
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.demo.xjh.preloadwebview.R

open class CustomWebView : WebView {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, privateBrowsing: Boolean) : super(context, attrs, defStyleAttr, privateBrowsing) {
        init()
    }

    internal var TAG = "CustomWebView"

    private lateinit var mProgressBar: ProgressBar
    private lateinit var mClient: CustomWebClient
    private lateinit var mChromeClient: CustomWebChromeClient

    private var mErrorPage: View? = null
    private var mOnPageFinishedListener: OnPageFinishedListener? = null
    private var mOnPageStartedListener: OnPageStartedListener? = null
    private var mOnReceivedTitleListener: OnReceivedTitleListener? = null
    private var mOnReceivedErrorListener: OnReceivedErrorListener? = null
    private var mOnPageSuccessListener: OnPageSuccessListener? = null
    private var mOnFileChooseCalledListener: OnFileChooseCalledListener? = null
    private var mOnShouldInterceptRequest: OnShouldInterceptRequest? = null
    private var mOnProcessListener: onProcessListener? = null

    private val loadFlag = booleanArrayOf(false, false, true)
    private val loadFlagMap: ArrayMap<String, Boolean> = ArrayMap()
    private val isLoadFlagTrue: Boolean
        get() = loadFlag[0] && loadFlag[1] && loadFlag[2]

    private fun resetLoadFlag() {
        loadFlag[0] = false
        loadFlag[1] = false
        loadFlag[2] = true
    }

    private val change: String = "ProgressChanged"
    private val finish: String = "finished"
    private val error: String = "error"

    init {
        loadFlagMap[change] = false
        loadFlagMap[finish] = false
        loadFlagMap[error] = true
    }

    private fun init() {
        initProgressBar()
        initWebClient()
        initErrorPage()
    }

    private fun initErrorPage() {
        val layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val layout = FrameLayout(context)
        layout.layoutParams = layoutParams
        layout.setBackgroundColor(Color.WHITE)
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER
        val textView = TextView(context)
        layout.addView(textView, params)
        textView.gravity = Gravity.CENTER
        textView.text = "网络不可用\n点击屏幕重试"
        setErrorPage(layout)
    }


    private fun initProgressBar() {
        mProgressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        val height = dp2dx(2f)
        val layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.MATCH_PARENT, height)
        val drawable = ContextCompat.getDrawable(context, R.drawable.web_progress_bar_states)
        mProgressBar.progressDrawable = drawable
        mProgressBar.layoutParams = layoutParams
        addView(mProgressBar)
    }

    private fun initWebClient() {
        mClient = CustomWebClient()
        mChromeClient = CustomWebChromeClient()
        setWebviewClient(mClient)
        setWebChromeClient(mChromeClient)
    }

    override fun reload() {
        resetLoadFlag()
        super.reload()
    }

    /**
     * 解决部分手机返回标题不刷新的bug,但这也造成另一部分手机mOnReceivedTitleListener调用两次bug.
     */
    override fun goBack() {
        super.goBack()
        resetLoadFlag()
        val list = copyBackForwardList()
        val item = list.currentItem
        if (item != null && mOnReceivedTitleListener != null) {
            mOnReceivedTitleListener?.onReceivedTitle(item.title)
        }
    }

    fun setWebChromeClient(client: CustomWebChromeClient) {
        super.setWebChromeClient(client)
        mChromeClient = client
    }

    fun setWebviewClient(client: CustomWebClient) {
        super.setWebViewClient(client)
        mClient = client
    }

    fun setErrorPage(errorPage: View) {
        mErrorPage?.let {
            removeView(mErrorPage)
        }
        removeView(mProgressBar)
        mErrorPage = errorPage
        addView(mErrorPage)
        mErrorPage?.visibility = View.GONE
        mErrorPage?.setOnClickListener { reload() }
        addView(mProgressBar)
    }

    fun setErrorPage(@LayoutRes errorPage: Int) {
        val view = LayoutInflater.from(context).inflate(errorPage, this, false)
        setErrorPage(view)
    }

    fun setProgressDrawable(drawable: Drawable?) {
        mProgressBar.progressDrawable = drawable
    }

    fun setProgressDrawableResource(resId: Int) {
        val drawable = ContextCompat.getDrawable(context, resId)
        setProgressDrawable(drawable)
    }

    fun setOnReceivedTitleListener(onReceivedTitleListener: OnReceivedTitleListener) {
        mOnReceivedTitleListener = onReceivedTitleListener
    }

    fun setOnReceivedErrorListener(onReceivedErrorListener: OnReceivedErrorListener) {
        mOnReceivedErrorListener = onReceivedErrorListener
    }

    fun setOnPageStartedListener(onPageStartedListener: OnPageStartedListener) {
        mOnPageStartedListener = onPageStartedListener
    }

    fun setOnPageFinishedListener(onPageFinishedListener: OnPageFinishedListener) {
        mOnPageFinishedListener = onPageFinishedListener
    }

    fun setOnPageSuccessListener(onPageSuccessListener: OnPageSuccessListener) {
        mOnPageSuccessListener = onPageSuccessListener
    }

    fun setOnFileChooseCalledListener(onFileChooseCalledListener: OnFileChooseCalledListener) {
        mOnFileChooseCalledListener = onFileChooseCalledListener
    }

    fun setOnShouldInterceptRequest(onShouldInterceptRequest: OnShouldInterceptRequest) {
        mOnShouldInterceptRequest = onShouldInterceptRequest;
    }

    fun setOnProcessListener(onProcessListener: onProcessListener) {
        mOnProcessListener = onProcessListener
    }

    @Deprecated("", ReplaceWith("throw RuntimeException(\"this method has been deprecated and you should use the one that with parameter extend CustomWebChromeClient\")"))
    override fun setWebChromeClient(client: WebChromeClient) {
        throw RuntimeException("this method has been deprecated and you should use the one that with parameter extend CustomWebChromeClient")
    }

    @Deprecated("", ReplaceWith("throw RuntimeException(\"this method has been deprecated and you should use the one that with parameter extend CustomWebClient\")"))
    override fun setWebViewClient(client: WebViewClient) {
    }

    inner class CustomWebChromeClient : android.webkit.WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            Log.d(TAG, newProgress.toString())
            mProgressBar.progress = newProgress
            mOnProcessListener?.onProcess(newProgress)
            if (newProgress == 100) {
                mProgressBar.visibility = View.GONE
                loadFlag[0] = true
                if (isLoadFlagTrue) {
                    mErrorPage?.visibility = View.GONE
                    mOnPageSuccessListener?.onPageSuccess(view)
                    resetLoadFlag()
                }
            } else {
                mProgressBar.visibility = View.VISIBLE
            }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            //屏蔽安卓6.0 调用两次 bug
            title?.let { if (!url.contains(title)) mOnReceivedTitleListener?.onReceivedTitle(title) }
        }

        // For Android < 3.0
        fun openFileChooser(valueCallback: ValueCallback<Uri>) {
            mOnFileChooseCalledListener?.onFileChooseCalled(object : OnFileChosenListener {
                override fun onFileChosen(uris: Array<Uri>) {
                    uris.forEach { url -> valueCallback.onReceiveValue(url) }
                }
            })
        }

        // For Android  >= 3.0
        fun openFileChooser(valueCallback: ValueCallback<Uri>, acceptType: String) {
            mOnFileChooseCalledListener?.onFileChooseCalled(object : OnFileChosenListener {
                override fun onFileChosen(uris: Array<Uri>) {
                    uris.forEach { url -> valueCallback.onReceiveValue(url) }
                }
            })
        }

        //For Android  >= 4.1
        fun openFileChooser(valueCallback: ValueCallback<Uri>, acceptType: String, capture: String) {
            mOnFileChooseCalledListener?.onFileChooseCalled(object : OnFileChosenListener {
                override fun onFileChosen(uris: Array<Uri>) {
                    uris.forEach { url -> valueCallback.onReceiveValue(url) }
                }
            })
        }

        // For Android >= 5.0
        override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
            mOnFileChooseCalledListener?.onFileChooseCalled(object : OnFileChosenListener {
                override fun onFileChosen(uris: Array<Uri>) {
                    filePathCallback.onReceiveValue(uris)
                }
            })
            return true
        }
    }

    inner class CustomWebClient : WebViewClient() {
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            return mOnShouldInterceptRequest?.shouldInterceptRequest(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            mOnPageStartedListener?.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            Log.d(TAG, "onPageFinished")
            loadFlag[1] = true
            if (isLoadFlagTrue) {
                mErrorPage?.visibility = View.GONE
                mOnPageSuccessListener?.onPageSuccess(view)
                resetLoadFlag()
            }
            mOnPageFinishedListener?.onPageFinished(view)
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            Log.d(TAG, "onReceivedError ssl")
            handler?.cancel()
        }

        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            Log.d(TAG, "onReceivedError")
            mErrorPage?.visibility = View.VISIBLE
            loadFlag[2] = false
            mOnReceivedErrorListener?.onReceivedError(errorCode, description, failingUrl)
            resetLoadFlag()
        }

        @TargetApi(android.os.Build.VERSION_CODES.M)
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            Log.d(TAG, "onReceivedError M")
            super.onReceivedError(view, request, error)
        }

        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
            Log.d(TAG, "onReceivedError http")
            super.onReceivedHttpError(view, request, errorResponse)
        }
    }


    private fun dp2dx(dp: Float): Int = (context.resources.displayMetrics.density * dp + 0.5).toInt()

    /**
     * 接收到网页title时
     */
    interface OnReceivedTitleListener {
        fun onReceivedTitle(title: String?)
    }

    interface OnReceivedErrorListener {
        fun onReceivedError(errorCode: Int, description: String?, failingUrl: String?)
    }

    interface OnPageFinishedListener {
        fun onPageFinished(view: WebView?)
    }

    interface OnPageSuccessListener {
        fun onPageSuccess(view: WebView?)
    }

    interface OnPageStartedListener {
        fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?)
    }

    interface onProcessListener {
        fun onProcess(process: Int);
    }

    interface OnFileChosenListener {
        fun onFileChosen(uris: Array<Uri>)
    }


    interface OnFileChooseCalledListener {
        fun onFileChooseCalled(onFileChosenListener: OnFileChosenListener)
    }

    interface OnShouldInterceptRequest {
        fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse?
    }
}