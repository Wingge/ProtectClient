package com.keeplive.client;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.keeplive.server.IServerAidlInterface;

import static com.keeplive.client.LockScreenReceiver.CHANNEL_ID;
import static com.keeplive.client.LockScreenReceiver.CHANNEL_NAME;
import static com.keeplive.client.LockScreenReceiver.NOTIFICATION_ID;

public class LocalService extends Service {

    private static final String TAG = "LocalService";

    private LockScreenReceiver mReceiver;
    private boolean mIsBound;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: 创建 LocalService");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(this);
        } else {
            startHideForeground();
        }
        try {
            clientAidlInterface.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: ");
        bindRemoteService();
        return START_STICKY;
    }
    private void startHideForeground() {
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ForegroundEnablingService.startForeground(this);
            startServiceCompat(this, new Intent(this, ForegroundEnablingService.class));
        } else {
            startForeground(NOTIFICATION_ID, new Notification());
        }
//        }
    }

    public static void startForeground(Service service) {
        String channelId = null;
//        Notification notification;
        Notification.Builder notificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channelId = createNotificationChannel(service, CHANNEL_ID, CHANNEL_NAME);
            notificationBuilder = new Notification.Builder(service, channelId);
            notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(Notification.PRIORITY_HIGH);
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
//            notification = notificationBuilder.build();
        } else {
            notificationBuilder = new Notification.Builder(service);
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
//            notification = notificationBuilder.getNotification();
        }
        notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_HIGH)
        ;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
        }
        service.startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    public static void startServiceCompat(Context context, Intent service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                context.startForegroundService(service);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        context.startService(service);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private static String createNotificationChannel(Context context, String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: 绑定 LocalService");
        return clientAidlInterface;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "======onUnbind:RemoteService");
        boolean xx = super.onUnbind(intent);
        mIsBound = false;
        bindRemoteService();
        return xx;
    }
    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: 销毁 LocalService");
        unbindService(connection);
        super.onDestroy();
    }

    IClientAidlInterface.Stub clientAidlInterface = new IClientAidlInterface.Stub() {
        private IntentFilter mIntentFilter = new IntentFilter();

        @Override
        public void start() throws RemoteException {
            registerReceiverr();
        }

        @Override
        public void bindSuccess() throws RemoteException {
            Log.e(TAG,"======bindSuccess: RemoteService 绑定 LocalService 成功");
            bindRemoteService();
        }

        @Override
        public void unbind() throws RemoteException {

        }

        private void registerReceiverr() {
            //动态注册
            mIntentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
            mIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            mIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
            mIntentFilter.addAction(Intent.ACTION_USER_PRESENT);
            mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
            mIntentFilter.addAction(LockScreenReceiver.ACTION_START);
            mIntentFilter.addAction(LockScreenReceiver.ACTION_DISCONNECTED);
            mIntentFilter.addAction(LockScreenReceiver.ACTION_CONNECTED);

            mIntentFilter.setPriority(Integer.MAX_VALUE);
            if (null == mReceiver) {
                mReceiver = new LockScreenReceiver();
                mIntentFilter.setPriority(Integer.MAX_VALUE);
                registerReceiver(mReceiver, mIntentFilter);
            }
        }
    };

    private void bindRemoteService() {
        if (mIsBound)
            return;
        try {
            Intent intent = new Intent("wing.android.keep_alive.service");
            intent.setPackage("com.keeplive.server");
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected: RemoteService 链接成功");
            mIsBound = true;
            IServerAidlInterface serverAidlInterface = IServerAidlInterface.Stub.asInterface(service);
            try {
                serverAidlInterface.bindSuccess();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected: RemoteService 断开连接，重新启动");
            mIsBound = false;
            bindRemoteService();
        }
    };
}
