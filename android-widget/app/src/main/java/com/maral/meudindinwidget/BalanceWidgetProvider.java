package com.maral.meudindinwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import java.text.NumberFormat;
import java.util.Locale;

public class BalanceWidgetProvider extends AppWidgetProvider {
    static String money(double value) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(value);
    }
    static void refresh(Context context, AppWidgetManager manager, int[] ids) {
        SharedPreferences p = context.getSharedPreferences("finance", Context.MODE_PRIVATE);
        double income = Double.longBitsToDouble(p.getLong("income", Double.doubleToRawLongBits(0)));
        double expense = Double.longBitsToDouble(p.getLong("expense", Double.doubleToRawLongBits(0)));
        String updated = p.getString("updated", "Abra o app para sincronizar");
        for (int id : ids) {
            RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.balance_widget);
            v.setTextViewText(R.id.widget_balance, money(income - expense));
            v.setTextViewText(R.id.widget_income, "Entradas  " + money(income));
            v.setTextViewText(R.id.widget_expense, "Saídas  " + money(expense));
            v.setTextViewText(R.id.widget_updated, updated);
            Intent open = new Intent(context, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            v.setOnClickPendingIntent(R.id.widget_balance, pi);
            v.setOnClickPendingIntent(R.id.widget_updated, pi);
            manager.updateAppWidget(id, v);
        }
    }
    public static void updateAll(Context context) {
        AppWidgetManager m = AppWidgetManager.getInstance(context);
        int[] ids = m.getAppWidgetIds(new ComponentName(context, BalanceWidgetProvider.class));
        new BalanceWidgetProvider().refresh(context, m, ids);
    }
    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) { refresh(c, m, ids); }
}
