package hu.elte.sbzbxr.phoneconnect.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.DisplayMetrics;

import androidx.annotation.Nullable;

import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenCapture2;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;


public class ScreenCaptureManager {
    private Intent lastInitIntent;
    private final ServiceController serviceController;

    public ScreenCaptureManager(ServiceController serviceController){
        this.serviceController = serviceController;
    }

    private DisplayMetrics setupMetrics(MainActivity mainActivity){
        DisplayMetrics metrics = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        return metrics;
    }

    public void startRealLive(MainActivity mainActivity,int resultCode, Intent data){
        start(mainActivity,resultCode,data,true);
    }

    private void start(MainActivity mainActivity, int resultCode, Intent data, boolean real){
        new Thread(() -> serviceController.bindService(new Intent(serviceController, ScreenCapture2.class),connection,Context.BIND_AUTO_CREATE)).start();

        DisplayMetrics lastMetrics = setupMetrics(mainActivity);

        lastInitIntent = new Intent();
        lastInitIntent.putExtra("resultCode",resultCode);
        lastInitIntent.putExtra("data",data);
        lastInitIntent.putExtra("metrics_width", lastMetrics.widthPixels);
        lastInitIntent.putExtra("metrics_height", lastMetrics.heightPixels);
        lastInitIntent.putExtra("metrics_densityDpi", lastMetrics.densityDpi);
        lastInitIntent.putExtra("isDemo",!real);
    }

    public void startDemo(ServiceController controller,MainActivity mainActivity){
        start(mainActivity,-1,null,false);
    }

    public void stop(){
        try {
            if(mBound) serviceController.unbindService(connection);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Nullable private ScreenCapture2 screenCapture2;
    private boolean mBound = false;
    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ScreenCapture2.LocalBinder binder = (ScreenCapture2.LocalBinder) service;
            screenCapture2 = binder.getService();
            mBound = true;
            screenCapture2.start(serviceController, serviceController.getNotification(), lastInitIntent);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            screenCapture2 = null;
        }
    };

    public boolean isRunning(){
        return mBound;
    }
}
