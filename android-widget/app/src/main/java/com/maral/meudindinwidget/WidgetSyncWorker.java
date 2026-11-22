package com.maral.meudindinwidget;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.OneTimeWorkRequest;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class WidgetSyncWorker extends Worker {
    private static final String API = "https://meu-dindin-lake.vercel.app/api/widget-snapshot";
    public WidgetSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) { super(context, params); }

    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(WidgetSyncWorker.class, 30, TimeUnit.MINUTES)
            .setConstraints(constraints).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("meu-dindin-widget-refresh",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE, request);
    }

    public static void syncNow(Context context) {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WidgetSyncWorker.class).setConstraints(constraints).build();
        WorkManager.getInstance(context).enqueue(request);
    }

    @NonNull @Override public Result doWork() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("finance", Context.MODE_PRIVATE);
        String token = prefs.getString("device_token", null);
        if (token == null) return Result.success();
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Authorization", "Bearer " + token);
            int code = connection.getResponseCode();
            if (code != 200) return code >= 500 ? Result.retry() : Result.failure();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
            reader.close();
            JSONObject json = new JSONObject(body.toString());
            prefs.edit()
                .putLong("income", Double.doubleToRawLongBits(json.optDouble("income", 0)))
                .putLong("expense", Double.doubleToRawLongBits(json.optDouble("expense", 0)))
                .putString("updated", "Atualizado agora")
                .apply();
            BalanceWidgetProvider.updateAll(getApplicationContext());
            return Result.success();
        } catch (Exception error) {
            return Result.retry();
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
