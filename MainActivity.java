package com.karar.mybrowser;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.Context;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
 private final ArrayList<WebView> tabs=new ArrayList<>();
 private final ArrayList<String> titles=new ArrayList<>();
 private LinearLayout root,bar,webBox; private EditText address; private ProgressBar progress; private WebView current;
 private boolean privateMode=false; private final int ink=Color.rgb(28,32,40), blue=Color.rgb(24,132,215); private static final String HOME="https://www.google.com";
 private GradientDrawable round(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
 private Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextColor(ink);b.setTextSize(17);b.setAllCaps(false);b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(4,0,4,0);return b;}
 @Override public void onCreate(Bundle b){super.onCreate(b);buildUi();newTab(HOME);}
 private void buildUi(){
  root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.rgb(247,248,250));
  LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(6,7,6,3);
  TextView icon=new TextView(this);icon.setText("🌐");icon.setTextSize(22);icon.setGravity(Gravity.CENTER);top.addView(icon,new LinearLayout.LayoutParams(38,46));
  address=new EditText(this);address.setSingleLine(true);address.setTextSize(15);address.setHint("Search or enter website");address.setImeOptions(EditorInfo.IME_ACTION_GO);address.setPadding(13,0,10,0);address.setBackground(round(Color.WHITE,25));top.addView(address,new LinearLayout.LayoutParams(0,46,1));
  Button go=btn("Go");top.addView(go,new LinearLayout.LayoutParams(54,46));go.setOnClickListener(v->navigate());
  address.setOnEditorActionListener((v,a,e)->{if(a==EditorInfo.IME_ACTION_GO||(e!=null&&e.getKeyCode()==KeyEvent.KEYCODE_ENTER)){navigate();return true;}return false;});root.addView(top);
  progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(100);progress.setVisibility(View.GONE);root.addView(progress,new LinearLayout.LayoutParams(-1,3));
  webBox=new LinearLayout(this);webBox.setOrientation(1);root.addView(webBox,new LinearLayout.LayoutParams(-1,0,1));
  bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER);bar.setPadding(2,4,2,5);
  addTool("‹",()->{if(current!=null&&current.canGoBack())current.goBack();});addTool("›",()->{if(current!=null&&current.canGoForward())current.goForward();});addTool("⌂",()->load(HOME));addTool("↻",()->{if(current!=null)current.reload();});addTool("+",()->newTab(HOME));addTool("▣",this::showTabs);addTool("⋮",this::menu);root.addView(bar);setContentView(root);
 }
 private void addTool(String s,Runnable r){Button b=btn(s);bar.addView(b,new LinearLayout.LayoutParams(0,48,1));b.setOnClickListener(v->r.run());}
 private void newTab(String url){
  WebView w=new WebView(this);WebSettings s=w.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setDatabaseEnabled(true);s.setLoadsImagesAutomatically(true);s.setSupportMultipleWindows(false);s.setUseWideViewPort(true);s.setLoadWithOverviewMode(true);s.setBuiltInZoomControls(true);s.setDisplayZoomControls(false);
  w.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest req){return false;}@Override public void onPageStarted(WebView v,String u,android.graphics.Bitmap f){if(v==current)address.setText(u);progress.setVisibility(View.VISIBLE);}@Override public void onPageFinished(WebView v,String u){if(v==current)address.setText(u);progress.setVisibility(View.GONE);int i=tabs.indexOf(v);if(i>=0){String t=v.getTitle();titles.set(i,(t==null||t.isEmpty())?u:t);}}});
  w.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView v,int p){if(v==current){progress.setProgress(p);progress.setVisibility(p>=100?View.GONE:View.VISIBLE);}}@Override public void onReceivedTitle(WebView v,String t){int i=tabs.indexOf(v);if(i>=0)titles.set(i,t==null?"New tab":t);}});
  tabs.add(w);titles.add("New tab");switchTo(tabs.size()-1);w.loadUrl(url);
 }
 private void switchTo(int i){if(i<0||i>=tabs.size())return;if(current!=null)webBox.removeView(current);current=tabs.get(i);if(current.getParent()!=null)((android.view.ViewGroup)current.getParent()).removeView(current);webBox.addView(current,new LinearLayout.LayoutParams(-1,-1));address.setText(current.getUrl()==null?"":current.getUrl());}
 private void navigate(){String in=address.getText().toString().trim();if(in.isEmpty())return;((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(address.getWindowToken(),0);String u;if(in.matches("(?i)^https?://.*"))u=in;else if(in.matches("(?i)^[a-z0-9.-]+\\.[a-z]{2,}(/.*)?$")&&!in.contains(" "))u="https://"+in;else try{u="https://www.google.com/search?q="+java.net.URLEncoder.encode(in,"UTF-8");}catch(Exception ex){u=HOME;}load(u);}
 private void load(String u){if(current==null)newTab(u);else current.loadUrl(u);}
 private void showTabs(){if(tabs.isEmpty()){newTab(HOME);return;}String[] a=new String[titles.size()+1];for(int i=0;i<titles.size();i++)a[i]=(i+1)+". "+titles.get(i);a[a.length-1]="+ New tab";new AlertDialog.Builder(this).setTitle("Open tabs").setItems(a,(d,n)->{if(n==a.length-1)newTab(HOME);else switchTo(n);}).setNeutralButton("Close current tab",(d,n)->closeTab()).show();}
 private void closeTab(){if(current==null)return;int i=tabs.indexOf(current);webBox.removeView(current);current.destroy();tabs.remove(i);titles.remove(i);current=null;if(tabs.isEmpty())newTab(HOME);else switchTo(Math.max(0,i-1));}
 private void menu(){String[] a={privateMode?"Turn private mode OFF":"Turn private mode ON","Clear browsing data","About My Browser"};new AlertDialog.Builder(this).setTitle("Browser menu").setItems(a,(d,n)->{if(n==0){privateMode=!privateMode;CookieManager.getInstance().setAcceptCookie(!privateMode);Toast.makeText(this,privateMode?"Basic private mode ON":"Private mode OFF",Toast.LENGTH_LONG).show();}else if(n==1)new AlertDialog.Builder(this).setTitle("Clear browsing data?").setMessage("Clear WebView cache, history and cookies.").setNegativeButton("Cancel",null).setPositiveButton("Clear",(x,y)->{for(WebView w:tabs){w.clearHistory();w.clearCache(true);w.clearFormData();}CookieManager.getInstance().removeAllCookies(null);CookieManager.getInstance().flush();Toast.makeText(this,"Browsing data cleared",Toast.LENGTH_SHORT).show();}).show();else new AlertDialog.Builder(this).setTitle("My Browser").setMessage("Android WebView browser project · Version 1.0").setPositiveButton("OK",null).show();}).show();}
 @Override public void onBackPressed(){if(current!=null&&current.canGoBack())current.goBack();else if(tabs.size()>1)closeTab();else super.onBackPressed();}
 @Override protected void onDestroy(){for(WebView w:tabs)w.destroy();tabs.clear();super.onDestroy();}
}
