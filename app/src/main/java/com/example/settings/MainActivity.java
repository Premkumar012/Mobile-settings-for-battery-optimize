package com.example.settings;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BATTERY_OPTIMIZATIONS = 1;
    private static final String AUTO_START_ENABLED_KEY = "auto_start_enabled";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check if battery optimization is enabled for this app
        checkBatteryOptimizations();
    }

    private boolean isBackgroundActivityRestricted() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            return !powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return false;
    }

    private void showAutoEnableSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Auto Enable Settings");
        builder.setMessage("To ensure proper functionality, please enable background activity for this app.");
        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                navigateToAutoEnableSettings();
            }
        });
        builder.show();
    }
    private void navigateToAutoEnableSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private boolean isAutoEnableSettingsEnabled() {
        // Retrieve the SharedPreferences
        SharedPreferences preferences = getSharedPreferences("auto_enable_settings", MODE_PRIVATE);

        // Retrieve the stored value indicating whether auto-start is enabled
        return preferences.getBoolean(AUTO_START_ENABLED_KEY, false);
    }

    // Method to update the auto-start enabled status in SharedPreferences
    private void updateAutoStartEnabledStatus(boolean isEnabled) {
        SharedPreferences preferences = getSharedPreferences("auto_enable_settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AUTO_START_ENABLED_KEY, isEnabled);
        editor.apply();
    }

    private void showAutoEnableSettingsDialog1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Auto Enable Settings");
        builder.setMessage("To ensure proper functionality, please navigate to auto-enable settings.");
        builder.setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                navigateToAutoEnableSettings1();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void navigateToAutoEnableSettings1() {
        try {
            // Create an intent with the action for auto-enable settings
            Intent autoEnableIntent = new Intent();
            autoEnableIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");

            // Start the activity with the auto-enable intent
            startActivity(autoEnableIntent);

            // Update the auto-start enabled status in SharedPreferences
            updateAutoStartEnabledStatus(true);
        } catch (ActivityNotFoundException e) {
            // Handle the case where the activity is not found
            Toast.makeText(this, "Auto-enable settings not available", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }







    private void checkBatteryOptimizations() {

        String manufacturer = android.os.Build.MANUFACTURER;

        if ("oppo".equalsIgnoreCase(manufacturer)) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (powerManager != null && powerManager.isPowerSaveMode()) {
                // Device is in battery saver mode
                if (!isIgnoringBatteryOptimizations()) {
                    // Check if background activity is enabled
                    if (!isBackgroundActivityEnabled()) {
                        // Ask the user to enable background activity
                        showEnableBackgroundActivityDialog();
                    } else {
                        // Ask the user to ignore battery optimizations for this app
                        showIgnoreBatteryOptimizationsDialog();
                    }
                }
            }
        } else if ("xiaomi".equalsIgnoreCase(manufacturer)) {
            if (isBackgroundActivityRestricted()) {
                // Prompt the user to navigate to auto-enable screen
                showAutoEnableSettingsDialog();
            }
            if (!isRedmiAutostartEnabled()) {
                // Prompt the user to navigate to auto-enable screen
                showAutoEnableSettingsDialog1();
            }

        }
        else if ("vivo".equalsIgnoreCase(manufacturer) || "samsung".equalsIgnoreCase(manufacturer)) {
            if (isBackgroundActivityRestricted()) {
                // Prompt the user to navigate to auto-enable screen
                showAutoEnableSettingsDialog();
            }
        }
    }

    private boolean isBackgroundActivityEnabled() {
        try {
            return getPackageManager().getApplicationInfo(getPackageName(), 0).targetSdkVersion >= 29;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean isIgnoringBatteryOptimizations() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            return powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return false;
    }

    private void requestIgnoreBatteryOptimizations() {
        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_BATTERY_OPTIMIZATIONS);
    }

    private void showIgnoreBatteryOptimizationsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Battery Optimization");
        builder.setMessage("To ensure proper functionality, please ignore battery optimizations for this app.");
        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestIgnoreBatteryOptimizations();
                // Navigate to battery optimization settings
                openBatteryOptimizationSettings();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false); // Prevent dialog from being canceled by tapping outside of it
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BATTERY_OPTIMIZATIONS) {
            if (resultCode == RESULT_OK) {
                // User granted battery optimization exemption
                // Handle it here
            } else {
                // User denied battery optimization exemption
                // Handle it here
            }
        }
    }

    private void openBatteryOptimizationSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }


    private void showEnableBackgroundActivityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Background Activity");
        builder.setMessage("To ensure proper functionality, please enable background activity for this app.");
        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openBackgroundActivitySettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle cancel action or dismiss dialog
                dialog.dismiss();
            }
        });
        builder.setCancelable(false); // Prevent dialog from being canceled by tapping outside of it
        builder.show();
    }

    private void openBackgroundActivitySettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
    private boolean isRedmiAutostartEnabled() {
        try {
            // Create an intent with the action for autostart settings
            Intent autoStartIntent = new Intent();
            autoStartIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");

            // Check if the activity is available
            PackageManager packageManager = getPackageManager();
            ComponentName componentName = autoStartIntent.resolveActivity(packageManager);
            return componentName != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}