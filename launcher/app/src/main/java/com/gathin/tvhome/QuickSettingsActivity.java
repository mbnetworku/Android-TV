package com.gathin.tvhome;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The panel the remote's Settings button opens when {@code settings_key_panel} is on (HomeKeyService):
 * a few glass rows under the Settings pill, over Home or whatever app is playing. Translucent and in a
 * task of its own, so the app behind keeps running and Back returns to it. Pressing Settings again
 * closes the panel.
 */
public final class QuickSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_quick_settings);
        findViewById(R.id.scrim).setOnClickListener(v -> finish());
        LinearLayout rows = findViewById(R.id.rows);
        // Through Home, so it rebuilds with the new choices when Launcher settings closes.
        addRow(rows, R.drawable.ic_home_settings, R.string.launcher_settings, 0,
                new Intent(this, MainActivity.class).setAction(MainActivity.ACTION_LAUNCHER_SETTINGS));
        addRow(rows, R.drawable.ic_wifi, R.string.quick_wifi, 0, SystemSettings.intent(this, Settings.ACTION_WIFI_SETTINGS));
        addRow(rows, R.drawable.ic_ethernet, R.string.quick_network, 0, SystemSettings.intent(this, Settings.ACTION_WIRELESS_SETTINGS));
        addRow(rows, R.drawable.ic_settings, R.string.quick_bluetooth, 0, SystemSettings.intent(this, Settings.ACTION_BLUETOOTH_SETTINGS));
        addRow(rows, R.drawable.ic_picture, R.string.quick_picture, 0, SystemSettings.intent(this, Settings.ACTION_DISPLAY_SETTINGS));
        addRow(rows, R.drawable.ic_av, R.string.quick_sound, 0, SystemSettings.intent(this, Settings.ACTION_SOUND_SETTINGS));
        addRow(rows, R.drawable.ic_apps, R.string.quick_apps, R.string.quick_apps_hint,
                SystemSettings.intent(this, Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
        addRow(rows, R.drawable.ic_settings, R.string.quick_storage, 0, SystemSettings.intent(this, Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
        addRow(rows, R.drawable.ic_settings, R.string.quick_datetime, 0, SystemSettings.intent(this, Settings.ACTION_DATE_SETTINGS));
        addRow(rows, R.drawable.ic_settings, R.string.quick_language, 0, SystemSettings.intent(this, Settings.ACTION_LOCALE_SETTINGS));
        addRow(rows, R.drawable.ic_settings, R.string.quick_keyboard, 0, SystemSettings.intent(this, Settings.ACTION_INPUT_METHOD_SETTINGS));
        addRow(rows, R.drawable.ic_lock, R.string.quick_privacy, 0, SystemSettings.intent(this, Settings.ACTION_PRIVACY_SETTINGS));
        addRow(rows, R.drawable.ic_settings, R.string.quick_location, 0, SystemSettings.intent(this, Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        addRow(rows, R.drawable.ic_picture, R.string.quick_screensaver, 0, SystemSettings.intent(this, Settings.ACTION_DREAM_SETTINGS));
        addRow(rows, R.drawable.ic_settings, R.string.quick_accessibility, 0, SystemSettings.intent(this, Settings.ACTION_ACCESSIBILITY_SETTINGS));
        addRow(rows, R.drawable.ic_settings, R.string.quick_developer, 0, SystemSettings.intent(this, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        addRow(rows, R.drawable.ic_settings, R.string.quick_about, 0, SystemSettings.intent(this, Settings.ACTION_DEVICE_INFO_SETTINGS));
        addRow(rows, R.drawable.ic_settings, R.string.tv_settings, 0, SystemSettings.intent(this, Settings.ACTION_SETTINGS));
        // The list can be taller than the screen: cap the panel at the space under the Settings pill.
        View scroll = findViewById(R.id.scroll);
        int cap = getResources().getDisplayMetrics().heightPixels - Math.round(105 * getResources().getDisplayMetrics().density) - Math.round(40 * getResources().getDisplayMetrics().density);
        scroll.post(() -> {
            if (scroll.getHeight() > cap) scroll.getLayoutParams().height = cap;
            scroll.requestLayout();
        });
        rows.getChildAt(0).requestFocus();
    }

    /** Settings pressed again while the panel is up. */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
    }

    /** Anything else taking the screen (Home, an app, a screensaver) closes the panel. */
    @Override
    protected void onPause() {
        super.onPause();
        finish();
        overridePendingTransition(0, 0);
    }

    private void addRow(LinearLayout rows, int icon, int title, int status, Intent target) {
        View row = getLayoutInflater().inflate(R.layout.item_input, rows, false);
        ((ImageView) row.findViewById(R.id.icon)).setImageResource(icon);
        ((TextView) row.findViewById(R.id.title)).setText(title);
        row.findViewById(R.id.dot).setVisibility(View.GONE);
        TextView hint = row.findViewById(R.id.status);
        if (status != 0) hint.setText(status);
        else hint.setVisibility(View.GONE);
        row.setOnClickListener(v -> {
            try {
                startActivity(target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (ActivityNotFoundException | SecurityException e) {
                Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show();
            }
            finish();
        });
        rows.addView(row);
    }
}
