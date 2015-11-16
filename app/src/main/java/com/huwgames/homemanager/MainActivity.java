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
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

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
        if (isOpenNetwork()) {
            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);

            mWebView.loadUrl("http://m.3150000.cn");
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.startimage));

            container = (ViewSwitcher) findViewById(R.id.viewswitcher);
            Animation slide_in_left = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            Animation slide_out_right = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

            container.setInAnimation(slide_in_left);
            container.setOutAnimation(slide_out_right);

            animationController = new AnimationController();
            Phone phone = new Phone();
            mWebView.addJavascriptInterface(phone, "androidPhoneObject");
            //web settings
            mWebView.setWebViewClient(new MyAppWebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
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
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("没有可用的网络").setMessage("是否对网络进行设置?");

            builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = null;

                    try {
                        String sdkVersion = android.os.Build.VERSION.SDK;
                        if(Integer.valueOf(sdkVersion) > 10) {
                            intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                        }else {
                            intent = new Intent();
                            ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                            intent.setComponent(comp);
                            intent.setAction("android.intent.action.VIEW");
                        }
                        MainActivity.this.startActivity(intent);
                    } catch (Exception e) {
//                        Log.w(TAG, "open network settings failed, please check...");
                        e.printStackTrace();
                    }
                }
            }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            }).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (!mSplashing) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                exitBy2Click();
            }
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
            finish();
            System.exit(0);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mSplashing) {
            return false;
        }
        menu.add(0, MENU_ADD_TASK, 0, R.string.hello_world);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mSplashing) {
            return false;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private boolean isOpenNetwork() {
        ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }

        return false;
    }

    //下面是打电话的操作方法
    final class Phone {
        public void call(String mobile) {
            Uri uri = Uri.parse("tel:" + mobile);
            Intent intent = new Intent(Intent.ACTION_CALL, uri);
            startActivity(intent);
        }
    }
}
