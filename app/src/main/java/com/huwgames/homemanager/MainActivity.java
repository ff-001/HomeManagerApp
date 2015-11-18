package com.huwgames.homemanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.security.PublicKey;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private WebView mWebView;
    private ViewSwitcher container;

    private void fadeOutAndHideImage(final ImageView img){
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(2000);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                container.setDisplayedChild(1);
                mSplashing = false;
                imageView.setImageDrawable(null);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }
    private static final int MENU_ADD_TASK = 0;
    private ImageView imageView;
    private boolean mSplashing;
    private boolean displayWeb;
    private boolean callView;
    private AnimationController animationController;
    private long durationMillis = 1000, delayMillis = 1000;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ActionBar actionBar = getActionBar();
//        actionBar.hide();
        mSplashing = true;
        displayWeb = true;
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.startimage);
        mWebView = (WebView) findViewById(R.id.webview);
        LoadingWebview();

    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadingWebview();
    }

    private void LoadingWebview(){

            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            if (isOpenNetwork()) {
                mWebView.loadUrl("http://m.3150000.cn");
            }else{
                mWebView.loadUrl("file:///android_asset/error.html");
            }
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.startimage));

            container = (ViewSwitcher) findViewById(R.id.viewswitcher);
            Animation slide_in_left = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            Animation slide_out_right = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

            container.setInAnimation(slide_in_left);
            container.setOutAnimation(slide_out_right);

            animationController = new AnimationController();
            //web settings
            mWebView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("tel:") || url.startsWith("mailto:")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(url));
                        callView = true;
                        startActivity(intent);
                    }
                    else if(url.startsWith("http:") || url.startsWith("https:")) {

                        callView = false;
                        view.loadUrl(url);
                    }
                    return super.shouldOverrideUrlLoading(view, url);
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    if (callView = false) {
                        mWebView.loadUrl("file:///android_asset/error.html");
                    }
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    if (callView = false) {
                        mWebView.loadUrl("file:///android_asset/error.html");
                    }
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed();
                }
            });

        mWebView.setWebChromeClient(new MyWebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (displayWeb == true && newProgress == 100) {
                    fadeOutAndHideImage(imageView);
                    animationController.hide(view);
                    animationController.slideFadeIn(mWebView, durationMillis, 500);
                    mSplashing = false;
                    displayWeb = false;
                }
            }
        });
        }
    boolean AlreadyBack = false;
    @Override
    public void onBackPressed() {
        if (!mSplashing) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                exitBy2Click();
            }
//            if ((!mWebView.getUrl().equals("http://m.3150000.cn")) && mWebView.canGoBack() && AlreadyBack == false) {
//                mWebView.goBack();
//                AlreadyBack = true;
//                mWebView.clearHistory();
//            } else if ( AlreadyBack == true ){
//                mWebView.clearHistory();
//                mWebView.loadUrl("http://m.3150000.cn");
//                AlreadyBack = false;
//            } else if (mWebView.getUrl().equals("http://m.3150000.cn")){
//                exitBy2Click();
//            }
        }
    }
    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // prepare to exit
            Toast.makeText(this, "再按一次返回键退出玉林家庭", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);

        } else {
            mWebView.clearCache(true);
            mWebView.clearHistory();
            finish();
            System.exit(0);
        }
    }

    private boolean isOpenNetwork() {
        ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }

        return false;
    }
}
