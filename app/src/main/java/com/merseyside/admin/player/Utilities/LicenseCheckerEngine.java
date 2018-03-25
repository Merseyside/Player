package com.merseyside.admin.player.Utilities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.merseyside.admin.player.R;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by Admin on 27.04.2017.
 */

public class LicenseCheckerEngine {
    private static String BASE64_PUBLIC_KEY;
    private long time;

    private static final byte[] SALT = new byte[] {
            -46, 65, 30, -128, -103, -57, 75, -64, 51, 36, -95, -45, 77, -117, -16, -113, -43, 32, -64  };

    public interface LicenseListener{
        void licenseResult(boolean result, String message);
    }

    private LicenseListener licenseListener;
    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    private Handler mHandler;
    private Context context;
    private ProgressDialog progress;
    private Settings settings;
    private boolean inBackground;

    public void setLicenseListener(LicenseListener licenseListener){
        this.licenseListener = licenseListener;
    }

    public LicenseCheckerEngine(Context context){
        this.context = context;
        mHandler = new Handler();
        settings = new Settings(context);
        Random random = new Random();
        SALT[15] = (byte) random.nextInt(128);
        String deviceId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        if (Settings.isProVersion()) BASE64_PUBLIC_KEY = context.getString(R.string.pro_key);
        else BASE64_PUBLIC_KEY = context.getString(R.string.free_key);
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        mChecker = new LicenseChecker(context, new ServerManagedPolicy(context, new AESObfuscator(SALT, context.getPackageName(), deviceId)),
                BASE64_PUBLIC_KEY);
    }

    public void checkLicense(boolean inBackground){
        this.inBackground = inBackground;
        if (settings.isOnline()) {
            if (!inBackground) {
                progress = new ProgressDialog(context, R.style.DialogStyle);
                progress.setTitle(context.getString(R.string.please_wait));
                progress.setMessage(context.getString(R.string.receiving_information));
                progress.setCancelable(false);
                progress.show();
            }
            time= System.currentTimeMillis();
            mChecker.checkAccess(mLicenseCheckerCallback);
        } else if (!inBackground) onCreateDialog(1).show();
    }

    protected Dialog onCreateDialog(int id) {
        final boolean bRetry = id == 1;
        return new AlertDialog.Builder(context)
                .setTitle(R.string.unlicensed_dialog_title)
                .setMessage(bRetry ? R.string.unlicensed_dialog_retry_body : R.string.unlicensed_dialog_body)
                .setPositiveButton(bRetry ? R.string.retry_button : R.string.buy_button, new DialogInterface.OnClickListener() {
                    boolean mRetry = bRetry;
                    public void onClick(DialogInterface dialog, int which) {
                        if (mRetry) {
                            checkLicense(false);
                        } else {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "http://market.android.com/details?id=" + context.getPackageName()));
                            context.startActivity(marketIntent);
                        }
                    }
                })
                .setNegativeButton(R.string.free, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://market.android.com/details?id=" + Settings.getTrialVersionPackageName()));
                        context.startActivity(marketIntent);
                    }
                }).create();
    }

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow(int policyReason) {
            if (System.currentTimeMillis() - time < 10) {
                licenseListener.licenseResult(false, context.getString(R.string.dont_allow));
                settings.savePreference(Settings.APP_PREFERENCES_LICENSE, false);
                if (progress != null) {
                    progress.dismiss();
                    progress = null;
                }
                onCreateDialog(0).show();
                return;
            }
            FirebaseEngine.logEvent(context,"LICENSE_ALLOWED", null);
            if (progress != null) {
                progress.dismiss();
                progress = null;
            }
            mHandler.post(new Runnable() {
                public void run() {
                    Log.d("License", "allow");
                    Settings.LICENSE = true;
                    settings.savePreference(Settings.APP_PREFERENCES_LICENSE, true);
                    if (Settings.LICENSE_CHECKED == -1) {
                        Calendar calendar = Calendar.getInstance();
                        int day_of_year = calendar.get(Calendar.DAY_OF_YEAR);
                        settings.savePreference(Settings.APP_PREFERENCES_LICENSE_CHECKED, day_of_year);
                    } else {
                        settings.savePreference(Settings.APP_PREFERENCES_LICENSE_CHECKED, -2);
                    }
                    if (licenseListener != null) licenseListener.licenseResult(true, context.getString(R.string.allow));
                }
            });
        }

        public void dontAllow(int policyReason) {
            if (progress != null){
                progress.dismiss();
                progress = null;
            }
            mHandler.post(new Runnable() {
                public void run() {
                    Log.d("License", "dont allow");
                    Settings.LICENSE = false;
                    settings.savePreference(Settings.APP_PREFERENCES_LICENSE, false);
                    settings.savePreference(Settings.APP_PREFERENCES_LICENSE_CHECKED, -1);
                    onCreateDialog(0).show();
                    if (licenseListener != null)licenseListener.licenseResult(false, context.getString(R.string.dont_allow));
                }
            });
        }

        public void applicationError(int errorCode) {
            Log.d("License", "error");
            if (progress!=null) progress.cancel();
            if (!inBackground) Toast.makeText(context, "Server error. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
