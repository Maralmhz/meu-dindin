package com.maral.meudindinwidget;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView web;
    private TextView status;
    private static final String SITE = "https://meu-dindin.pages.dev";
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,24,24,24); root.setBackgroundColor(Color.rgb(15,23,42));
        TextView title = new TextView(this); title.setText("Meu Dindin Widget"); title.setTextSize(22); title.setTextColor(Color.WHITE); title.setGravity(Gravity.CENTER_VERTICAL); root.addView(title, new LinearLayout.LayoutParams(-1, 56));
        status = new TextView(this); status.setText("Conectando ao Meu Dindin..."); status.setTextColor(Color.LTGRAY); root.addView(status, new LinearLayout.LayoutParams(-1, 48));
        web = new WebView(this); web.getSettings().setJavaScriptEnabled(true); web.getSettings().setDomStorageEnabled(true); web.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                if (!SITE.equals(url) && !url.startsWith(SITE + "/")) return;
                view.evaluateJavascript("(function(){return localStorage.getItem('meudindin_data') || '[]';})()", value -> saveSnapshot(value));
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) { return !url.startsWith(SITE); }
        });
        root.addView(web, new LinearLayout.LayoutParams(0,0,1)); setContentView(root);
        web.loadUrl(SITE);
    }
    private void saveSnapshot(String encoded) {
        try {
            String json = encoded;
            if (json == null || json.equals("null")) throw new Exception("Não foi possível ler os lançamentos.");
            if (json.length() >= 2 && json.startsWith("\"") && json.endsWith("\"")) json = new JSONArray("[" + json + "]").getString(0);
            JSONArray arr = new JSONArray(json); double income=0, expense=0;
            for (int i=0;i<arr.length();i++) { JSONObject t=arr.getJSONObject(i); double amount=t.optDouble("amount",0); if ("RECEITA".equals(t.optString("category"))) income+=amount; else expense+=amount; }
            String stamp = new SimpleDateFormat("dd/MM HH:mm", Locale("pt","BR")).format(new Date());
            SharedPreferences p=getSharedPreferences("finance",MODE_PRIVATE);
            p.edit().putLong("income",Double.doubleToRawLongBits(income)).putLong("expense",Double.doubleToRawLongBits(expense)).putString("updated","Atualizado " + stamp).apply();
            BalanceWidgetProvider.updateAll(this); status.setText("Sincronizado. Entradas: " + BalanceWidgetProvider.money(income) + " | Saídas: " + BalanceWidgetProvider.money(expense) + "\nOs dados ficam salvos somente neste aparelho.");
        } catch(Exception e) { status.setText("Não consegui ler os lançamentos: " + e.getMessage()); }
    }
    @Override public void onBackPressed() { if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
