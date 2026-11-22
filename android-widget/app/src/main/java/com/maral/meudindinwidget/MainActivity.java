package com.maral.meudindinwidget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class MainActivity extends Activity {
    private static final String SITE = "https://meu-dindin.pages.dev/";
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(SITE)));
        finish();
    }
}
