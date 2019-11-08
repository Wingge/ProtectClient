package com.keeplive.client;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LockScreenReceiver extends BroadcastReceiver {
    public static final String ACTION_START = "wing.android.lock_news.action_start";
    public static final String ACTION_DISCONNECTED = "wing.android.lock_news.action_disconnected";
    public static final String ACTION_CONNECTED = "wing.android.lock_news.action_connected";
    public static final String TAG = LockScreenReceiver.class.getSimpleName();
    public final static String CHANNEL_ID = "daemon";
    public final static String CHANNEL_NAME = "daemon_client";
    public final static int NOTIFICATION_ID = 111;//must different from the server notification id

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        //daemon by self
        if (ACTION_DISCONNECTED.equals(action)) {
            Log.d(TAG, "disconnected,restart by receiver.");
            Intent serviceIntent = new Intent("wing.android.lock_news.LockScreenService");
            intent.setPackage("wing.android.lock_news");
            LocalService.startServiceCompat(context, serviceIntent);
        } else {
            if (Intent.ACTION_USER_PRESENT.equals(action)) {
                Log.e(TAG, "====ACTION_USER_PRESENT");

            }
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Log.e(TAG, "====ACTION_SCREEN_ON");

            }
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Log.e(TAG, "=====ACTION_SCREEN_OFF");
//            if (Parser.sPhoneCallState != TelephonyManager.CALL_STATE_IDLE) { // not calling state
                for (Activity activity : Parser.KEY_GUARD_INSTANCES) {
                    activity.finish();
                }

                Intent lockScreen = new Intent(context, OnePiexlActivity.class);
                lockScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                context.startActivity(lockScreen);
//            }
            }
        }
    }
}



