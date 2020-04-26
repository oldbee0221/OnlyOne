package mms5.onepagebook.com.onlyonesms;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import mms5.onepagebook.com.onlyonesms.util.Utils;

/**
 * Created by jeonghopark on 2020/03/02.
 */
public class HomeActivity extends AppCompatActivity {
    private WebView mWebView;
    private WebSettings mWebSettings;

    private JavaScriptInterface mJavaScriptInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mWebView = findViewById(R.id.webView);
        mWebView.setWebViewClient(new MyWebViewClient());
        mJavaScriptInterface = new JavaScriptInterface(this);
        mWebView.addJavascriptInterface(mJavaScriptInterface, "AppScript");

        mWebSettings = mWebView.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setSupportMultipleWindows(false);
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        mWebSettings.setLoadWithOverviewMode(true);
        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setSupportZoom(false);
        mWebSettings.setBuiltInZoomControls(false);
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebSettings.setDomStorageEnabled(true);

        mWebView.loadUrl("http://obmms.net/m/");
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private class JavaScriptInterface {
        private Context mContext;

        public JavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void goOnlyOneApp(String id) {
            Intent intent = new Intent(HomeActivity.this, LogInActivity.class);
            if(Utils.IsEmpty(id)) {
                intent.putExtra("ID", "");
            } else {
                intent.putExtra("ID", id);
            }
            startActivity(intent);
        }

        @JavascriptInterface
        public void goCallbackApp(String id) {
            Intent intent = new Intent(HomeActivity.this, CBMDoor2Activity.class);
            if(Utils.IsEmpty(id)) {
                intent.putExtra("ID", "");
            } else {
                intent.putExtra("ID", id);
            }
            startActivity(intent);
        }
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Utils.Log("shouldOverrideUrlLoading() " + url);

            //if(url.contains("http://obmms.net/iam/")) {
            if(url.contains("http://obmms.net")) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                Uri u = Uri.parse(url);
                i.setData(u);
                startActivity(i);
                return true;
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }
}
