package com.otad.motionlab;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.graphics.Color;
public class MainActivity extends Activity {
 private WebView webView;
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  getWindow().setStatusBarColor(Color.rgb(13,22,25));
  getWindow().setNavigationBarColor(Color.rgb(13,22,25));
  webView=new WebView(this);
  webView.setBackgroundColor(Color.rgb(13,22,25));
  WebSettings s=webView.getSettings();
  s.setJavaScriptEnabled(true);
  s.setDomStorageEnabled(false);
  s.setAllowFileAccess(true);
  s.setAllowContentAccess(false);
  s.setCacheMode(WebSettings.LOAD_NO_CACHE);
  webView.setWebChromeClient(new WebChromeClient());
  setContentView(webView);
  webView.loadUrl("file:///android_asset/index.html");
 }
 @Override public void onBackPressed(){ if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed(); }
}
