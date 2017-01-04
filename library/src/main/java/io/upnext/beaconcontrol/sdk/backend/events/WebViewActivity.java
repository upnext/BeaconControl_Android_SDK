/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.events;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import io.upnext.beaconcontrol.sdk.R;

public class WebViewActivity extends Activity {

    interface Extra {
        String NAME = "NAME";
        String URL = "URL";
    }

    public static Intent getIntent(Context context, String name, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(Extra.NAME, name);
        intent.putExtra(Extra.URL, url);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_web_view);
        setTitle(getNameFromExtras());

        setWebView(getURLFromExtras());
    }

    private String getNameFromExtras() {
        return getIntent().getExtras().getString(Extra.NAME);
    }

    private String getURLFromExtras() {
        return getIntent().getExtras().getString(Extra.URL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setWebView(String url) {
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);
    }
}
