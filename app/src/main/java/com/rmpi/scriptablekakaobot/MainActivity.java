package com.rmpi.scriptablekakaobot;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import org.mozilla.javascript.Function;

public class MainActivity extends AppCompatActivity {
    private static String PREFS_KEY = "bot";
    private static String ON_KEY = "on";
    private boolean granted = true;

    // UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        grantPermission();
        KakaotalkListener.initializeScript();
        setContentView(R.layout.activity_main);
        Switch onOffSwitch = (Switch) findViewById(R.id.switch1);
        onOffSwitch.setChecked(getOn(this));
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean b) {
                putOn(getApplicationContext(), b);
            }
        });
    }

    public void onSettingsClick(View v) {
        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    public void onReloadClick(View v) {
        KakaotalkListener.initializeScript();
    }

    // Util

    private void grantPermission() {
        if (Build.VERSION.SDK_INT >= 23)
            if (!(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                granted = false;
                requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 1);
                Thread permChecker = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long currTime = System.currentTimeMillis();
                        while(!granted) if (System.currentTimeMillis() - currTime > 10000) MainActivity.this.finish();
                    }
                });
                permChecker.start();

                try {
                    permChecker.join();
                } catch (InterruptedException e) {
                    finish();
                }
            }

    }

    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == 1)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                granted = true;
    }

    static boolean getOn(Context ctx) {
        return ctx.getSharedPreferences(PREFS_KEY, MODE_PRIVATE).getBoolean(ON_KEY, false);
    }

    private static void putOn(Context ctx, boolean value) {
        ctx.getSharedPreferences(PREFS_KEY, MODE_PRIVATE).edit().putBoolean(ON_KEY, value).apply();
    }
}
