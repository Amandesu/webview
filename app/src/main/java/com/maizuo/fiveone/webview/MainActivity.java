package com.maizuo.fiveone.webview;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private JSONObject resObj = new JSONObject();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        final WebView webView = (WebView)findViewById(R.id.h5_shop);
        final Handler handler = new Handler(){
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    webView.loadUrl("javascript:window.Bridge.receiveNative("+resObj.toString()+")");
                }
            }
        };


        webView.getSettings().setJavaScriptEnabled(true);
        class JsInterface {
            @JavascriptInterface
            public void postMessage(String a) throws JSONException{
                JSONObject jsonObj = new JSONObject(a);
                String bridgeName = jsonObj.getString("bridgeName");
                if ( bridgeName.equals("getUserInfo") ){
                    resObj.put("cid", jsonObj.get("cid"));
                    resObj.put("name", "leiwuyi");
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }

            }
        }
        webView.addJavascriptInterface(new JsInterface(), "nativeBridge");

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("JsAlert")
                        .setMessage(message)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .setCancelable(false)
                        .show();
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });
        webView.loadUrl("file:///android_asset/shop.html");

    }
}
