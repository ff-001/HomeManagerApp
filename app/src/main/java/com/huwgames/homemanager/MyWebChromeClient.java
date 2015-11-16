package com.huwgames.homemanager;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Created by ico0018 on 11/9/15.
 */
public class MyWebChromeClient extends WebChromeClient {

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
    }
}