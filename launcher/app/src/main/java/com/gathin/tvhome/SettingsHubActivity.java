package com.gathin.tvhome;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A full-screen Settings app in the style of Apple TV: a large gear and the TV's name on the left, and on the
 * right one list of every settings page with its current state (network, storage, memory, display, volume,
 * time, language, software). Each row opens the TV's own page for it, so a page that misbehaves in the TV's
 * Settings app is still one click away. Opened from the dock's Settings icon and the Settings pill.
 */
public final class SettingsHubActivity extends Activity {

    private float density;
    private LinearLayout rows;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        density = getResources().getDisplayMetrics().density;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        // Translucent: Home's wallpaper shows through a light scrim, as in the launcher's own glass panels.
        root.setBackgroundColor(0x99000000);
        root.setPadding(dp(56), dp(40), dp(56), dp(32));

        // Left: the big gear, the title and the TV's identity.
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setGravity(Gravity.CENTER);
        ImageView gear = new ImageView(this);
        gear.setImageResource(R.drawable.ic_settings);
        gear.setColorFilter(0xFFD8DCE3);
        left.addView(gear, new LinearLayout.LayoutParams(dp(180), dp(180)));
        left.addView(text(getString(R.string.hub_title), 34, 0xFFFFFFFF, true), wrap(dp(20)));
        left.addView(text(Build.MANUFACTURER.toUpperCase(Locale.ROOT) + " " + Build.MODEL, 16, 0xFF9AA1AC, false), wrap(dp(8)));
        left.addView(text("Android " + Build.VERSION.RELEASE + " · " + Build.DISPLAY, 14, 0xFF6F7682, false), wrap(dp(4)));
        // Its own glass panel too, so Home's clock and dock behind it do not show through the title.
        left.setBackgroundResource(R.drawable.glass_panel);
        LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        leftLp.setMarginEnd(dp(24));
        root.addView(left, leftLp);

        // Right: the list.
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(0, dp(8), 0, dp(8));
        scroll.addView(rows);
        // The list sits on the launcher's frosted glass panel.
        scroll.setBackgroundResource(R.drawable.glass_panel);
        scroll.setPadding(dp(6), dp(6), dp(6), dp(6));
        scroll.setClipToPadding(false);
        root.addView(scroll, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.25f));

        setContentView(root);
    }

    /** Rebuilt on every return, so a change made in the TV's page shows when Back comes here. */
    @Override
    protected void onResume() {
        super.onResume();
        int focused = rows.indexOfChild(rows.getFocusedChild());
        rows.removeAllViews();

        header(R.string.hub_section_launcher);
        row(R.drawable.ic_home_settings, R.string.launcher_settings, "", new Intent(this, MainActivity.class).setAction(MainActivity.ACTION_LAUNCHER_SETTINGS));

        header(R.string.hub_section_network);
        row(R.drawable.ic_wifi, R.string.quick_wifi, networkState(), sys(Settings.ACTION_WIFI_SETTINGS));
        row(R.drawable.ic_ethernet, R.string.quick_network, ipAddress(), sys(Settings.ACTION_WIRELESS_SETTINGS));
        row(R.drawable.ic_input, R.string.quick_bluetooth, getString(R.string.hub_bluetooth_hint), sys(Settings.ACTION_BLUETOOTH_SETTINGS));

        header(R.string.hub_section_video_audio);
        row(R.drawable.ic_picture, R.string.quick_picture, resolution(), sys(Settings.ACTION_DISPLAY_SETTINGS));
        row(R.drawable.ic_av, R.string.quick_sound, volume(), sys(Settings.ACTION_SOUND_SETTINGS));
        row(R.drawable.ic_picture, R.string.quick_screensaver, "", sys(Settings.ACTION_DREAM_SETTINGS));

        header(R.string.hub_section_apps);
        row(R.drawable.ic_apps, R.string.quick_apps, appCount(), sys(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
        row(R.drawable.ic_swap_vert, R.string.quick_storage, storage(), sys(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
        row(R.drawable.ic_swap_vert, R.string.hub_memory, memory(), sys(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));

        header(R.string.hub_section_general);
        row(R.drawable.ic_settings, R.string.quick_datetime, DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date())
                + " · " + TimeZone.getDefault().getID(), sys(Settings.ACTION_DATE_SETTINGS));
        row(R.drawable.ic_settings, R.string.quick_language, Locale.getDefault().getDisplayName(), sys(Settings.ACTION_LOCALE_SETTINGS));
        row(R.drawable.ic_settings, R.string.quick_keyboard, "", sys(Settings.ACTION_INPUT_METHOD_SETTINGS));
        row(R.drawable.ic_lock, R.string.quick_privacy, "", sys(Settings.ACTION_PRIVACY_SETTINGS));
        row(R.drawable.ic_settings, R.string.quick_location, "", sys(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        row(R.drawable.ic_settings, R.string.quick_accessibility, "", sys(Settings.ACTION_ACCESSIBILITY_SETTINGS));

        header(R.string.hub_section_system);
        row(R.drawable.ic_livetv, R.string.quick_about, Build.MODEL, sys(Settings.ACTION_DEVICE_INFO_SETTINGS));
        row(R.drawable.ic_settings, R.string.quick_developer, "", sys(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        row(R.drawable.ic_settings, R.string.tv_settings, "", sys(Settings.ACTION_SETTINGS));

        View target = focused >= 0 && focused < rows.getChildCount() ? rows.getChildAt(focused) : null;
        if (target == null || !target.isFocusable()) target = firstRow();
        if (target != null) target.requestFocus();
    }

    private View firstRow() {
        for (int i = 0; i < rows.getChildCount(); i++) if (rows.getChildAt(i).isFocusable()) return rows.getChildAt(i);
        return null;
    }

    private Intent sys(String action) {
        return SystemSettings.intent(this, action);
    }

    private void header(int title) {
        TextView t = text(getString(title).toUpperCase(Locale.getDefault()), 13, 0xFF6F7682, true);
        t.setLetterSpacing(0.08f);
        t.setPadding(dp(20), dp(rows.getChildCount() == 0 ? 0 : 22), dp(20), dp(6));
        rows.addView(t);
    }

    private void row(int iconRes, int title, String value, Intent target) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setFocusable(true);
        row.setClickable(true);
        row.setPadding(dp(20), dp(14), dp(20), dp(14));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(20));
        bg.setColor(Color.TRANSPARENT);
        row.setBackground(bg);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(0xFFE8EAED);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(26), dp(26));
        iconLp.setMarginEnd(dp(16));
        row.addView(icon, iconLp);
        TextView name = text(getString(title), 19, 0xFFE8EAED, false);
        row.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView state = text(value, 16, 0xFF9AA1AC, false);
        state.setSingleLine(true);
        state.setGravity(Gravity.END);
        row.addView(state, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView chevron = text("›", 24, 0xFF6F7682, false);
        chevron.setPadding(dp(14), 0, 0, 0);
        row.addView(chevron);

        // Apple TV's focus: the row turns white and lifts a little, its text dark.
        row.setOnFocusChangeListener((v, has) -> {
            bg.setColor(has ? 0xE6F2F3F5 : Color.TRANSPARENT);
            name.setTextColor(has ? 0xFF111317 : 0xFFE8EAED);
            icon.setColorFilter(has ? 0xFF111317 : 0xFFE8EAED);
            state.setTextColor(has ? 0xFF4A505A : 0xFF9AA1AC);
            chevron.setTextColor(has ? 0xFF4A505A : 0xFF6F7682);
            v.animate().scaleX(has ? 1.03f : 1f).scaleY(has ? 1.03f : 1f).setDuration(120).start();
        });
        row.setOnClickListener(v -> {
            try {
                startActivity(target);
            } catch (ActivityNotFoundException | SecurityException e) {
                Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(6), dp(2), dp(6), dp(2));
        rows.addView(row, lp);
    }

    // ---- the values shown on the right of each row --------------------------------------------------

    private String networkState() {
        ConnectivityManager cm = getSystemService(ConnectivityManager.class);
        Network n = cm == null ? null : cm.getActiveNetwork();
        NetworkCapabilities caps = n == null ? null : cm.getNetworkCapabilities(n);
        if (caps == null) return getString(R.string.hub_not_connected);
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return getString(R.string.hub_connected_wifi);
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return getString(R.string.hub_connected_ethernet);
        return getString(R.string.hub_connected);
    }

    private String ipAddress() {
        ConnectivityManager cm = getSystemService(ConnectivityManager.class);
        Network n = cm == null ? null : cm.getActiveNetwork();
        LinkProperties lp = n == null ? null : cm.getLinkProperties(n);
        if (lp == null) return "";
        for (LinkAddress a : lp.getLinkAddresses()) {
            if (a.getAddress() instanceof Inet4Address) return a.getAddress().getHostAddress();
        }
        return "";
    }

    private String resolution() {
        DisplayMetrics m = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(m);
        return m.widthPixels + " × " + m.heightPixels;
    }

    private String volume() {
        AudioManager am = getSystemService(AudioManager.class);
        if (am == null) return "";
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int now = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        return max > 0 ? Math.round(now * 100f / max) + "%" : "";
    }

    private String appCount() {
        Intent tvApps = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        int n = getPackageManager().queryIntentActivities(tvApps, PackageManager.MATCH_DEFAULT_ONLY).size();
        return getString(R.string.hub_apps_count, n);
    }

    private String storage() {
        StatFs fs = new StatFs(Environment.getDataDirectory().getPath());
        return getString(R.string.hub_free_of, gb(fs.getAvailableBytes()), gb(fs.getTotalBytes()));
    }

    private String memory() {
        ActivityManager am = getSystemService(ActivityManager.class);
        if (am == null) return "";
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return getString(R.string.hub_free_of, mb(mi.availMem), mb(mi.totalMem));
    }

    private static String gb(long bytes) {
        return String.format(Locale.getDefault(), "%.1f GB", bytes / 1e9);
    }

    private static String mb(long bytes) {
        return Math.round(bytes / 1048576d) + " MB";
    }

    // ---- small helpers ----------------------------------------------------------------------------------

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private static LinearLayout.LayoutParams wrap(int top) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = top;
        return lp;
    }

    private int dp(int v) {
        return Math.round(v * density);
    }
}
