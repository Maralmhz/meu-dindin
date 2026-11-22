package com.maral.meudindinwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String SITE = "https://meu-dindin.pages.dev/";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        handleLink(getIntent());
        render();
        WidgetSyncWorker.schedule(this);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleLink(intent);
        render();
    }

    private void handleLink(Intent intent) {
        Uri uri = intent == null ? null : intent.getData();
        if (uri != null && "meudindinwidget".equals(uri.getScheme())) {
            String token = uri.getQueryParameter("token");
            if (token != null && token.length() >= 30) {
                getSharedPreferences("finance", MODE_PRIVATE).edit().putString("device_token", token).apply();
                WidgetSyncWorker.syncNow(this);
                Toast.makeText(this, "Conta conectada ao widget!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void render() {
        SharedPreferences prefs = getSharedPreferences("finance", MODE_PRIVATE);
        boolean linked = prefs.contains("device_token");
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 36, 28, 28);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(0xFFF3F4F6);

        TextView title = new TextView(this);
        title.setText("Meu Dindin");
        title.setTextSize(26);
        title.setTextColor(0xFF111827);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 12);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView status = new TextView(this);
        status.setText(linked ? "Conta conectada. O widget busca os dados da nuvem automaticamente." :
            "Para conectar: abra o Meu Dindin no Chrome, entre na sua conta Google e toque em “Conectar widget”.");
        status.setTextSize(15);
        status.setTextColor(0xFF374151);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 12, 0, 24);
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));

        Button add = new Button(this);
        add.setText("Adicionar widget à tela inicial");
        add.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 26 && AppWidgetManager.getInstance(this).isRequestPinAppWidgetSupported()) {
                AppWidgetManager.getInstance(this).requestPinAppWidget(
                    new ComponentName(this, BalanceWidgetProvider.class), null, null);
            } else {
                Toast.makeText(this, "Na tela inicial, segure o dedo e escolha Widgets > Meu Dindin.", Toast.LENGTH_LONG).show();
            }
        });
        root.addView(add, new LinearLayout.LayoutParams(-1, -2));

        Button refresh = new Button(this);
        refresh.setText("Atualizar agora");
        refresh.setOnClickListener(v -> {
            WidgetSyncWorker.syncNow(this);
            Toast.makeText(this, "Atualização solicitada.", Toast.LENGTH_SHORT).show();
        });
        root.addView(refresh, new LinearLayout.LayoutParams(-1, -2));

        Button open = new Button(this);
        open.setText("Abrir Meu Dindin");
        open.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(SITE))));
        root.addView(open, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
    }
}
