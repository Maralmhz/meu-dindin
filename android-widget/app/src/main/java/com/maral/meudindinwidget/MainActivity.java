package com.maral.meudindinwidget;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView web;
    private TextView status;
    private static final String SITE = "https://meu-dindin.pages.dev";
    private static final int PICK_BACKUP = 42;
    private final Handler syncHandler = new Handler(Looper.getMainLooper());
    private final Runnable syncTask = new Runnable() {
        @Override public void run() {
            if (web != null) web.evaluateJavascript("(function(){return localStorage.getItem('meudindin_data') || '[]';})()", value -> saveSnapshot(value));
            syncHandler.postDelayed(this, 10000);
        }
    };
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,24,24,24); root.setBackgroundColor(Color.rgb(15,23,42));
        TextView title = new TextView(this); title.setText("Meu Dindin Widget"); title.setTextSize(22); title.setTextColor(Color.WHITE); title.setGravity(Gravity.CENTER_VERTICAL); root.addView(title, new LinearLayout.LayoutParams(-1, 56));
        status = new TextView(this); status.setText("Conectando ao Meu Dindin..."); status.setTextColor(Color.LTGRAY); root.addView(status, new LinearLayout.LayoutParams(-1, 48));
        TextView importButton = new TextView(this); importButton.setText("IMPORTAR BACKUP DO NAVEGADOR"); importButton.setTextColor(Color.WHITE); importButton.setTextSize(13); importButton.setGravity(Gravity.CENTER); importButton.setPadding(12,14,12,14); importButton.setBackgroundColor(Color.rgb(37,99,235)); importButton.setOnClickListener(v -> { Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT); pick.setType("application/json"); pick.addCategory(Intent.CATEGORY_OPENABLE); startActivityForResult(pick, PICK_BACKUP); }); root.addView(importButton, new LinearLayout.LayoutParams(-1, 52));
        web = new WebView(this); web.getSettings().setJavaScriptEnabled(true); web.getSettings().setDomStorageEnabled(true); web.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                if (!SITE.equals(url) && !url.startsWith(SITE + "/")) return;
                view.evaluateJavascript("(function(){return localStorage.getItem('meudindin_data') || '[]';})()", value -> saveSnapshot(value));
                syncHandler.removeCallbacks(syncTask); syncHandler.postDelayed(syncTask, 10000);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) { return !url.startsWith(SITE); }
        });
        root.addView(web, new LinearLayout.LayoutParams(0,0,1)); setContentView(root);
        web.loadUrl(SITE);
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_BACKUP || resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        try (InputStream in = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            StringBuilder json = new StringBuilder(); String line;
            while ((line = reader.readLine()) != null) json.append(line);
            new JSONArray(json.toString());
            web.evaluateJavascript("localStorage.setItem('meudindin_data'," + JSONObject.quote(json.toString()) + "); location.reload();", null);
            status.setText("Backup importado. Atualizando o widget...");
        } catch (Exception e) { status.setText("Falha ao importar backup: " + e.getMessage()); }
    }
    private void saveSnapshot(String encoded) {
        try {
            String json = encoded;
            if (json == null || json.equals("null")) throw new Exception("Não foi possível ler os lançamentos.");
            if (json.length() >= 2 && json.startsWith("\"") && json.endsWith("\"")) json = new JSONArray("[" + json + "]").getString(0);
            JSONArray arr = new JSONArray(json); double income=0, expense=0;
            for (int i=0;i<arr.length();i++) { JSONObject t=arr.getJSONObject(i); double amount=t.optDouble("amount",0); if ("RECEITA".equals(t.optString("category"))) income+=amount; else expense+=amount; }
            String stamp = new SimpleDateFormat("dd/MM HH:mm", new Locale("pt","BR")).format(new Date());
            SharedPreferences p=getSharedPreferences("finance",MODE_PRIVATE);
            p.edit().putLong("income",Double.doubleToRawLongBits(income)).putLong("expense",Double.doubleToRawLongBits(expense)).putString("updated","Atualizado " + stamp).apply();
            BalanceWidgetProvider.updateAll(this); status.setText("Sincronizado. Entradas: " + BalanceWidgetProvider.money(income) + " | Saídas: " + BalanceWidgetProvider.money(expense) + "\nOs dados ficam salvos somente neste aparelho.");
        } catch(Exception e) { status.setText("Não consegui ler os lançamentos: " + e.getMessage()); }
    }
    @Override protected void onDestroy() { syncHandler.removeCallbacks(syncTask); super.onDestroy(); }
    @Override public void onBackPressed() { if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
