package hu.elte.sbzbxr.phoneconnect.controller;

import android.content.ComponentName;
import android.content.Intent;
import android.util.DisplayMetrics;

import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenCapture2;
import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenCapture3;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;


public class ScreenCaptureBuilder {
    private final MainActivity mainActivity;
    ComponentName componentName;

    public ScreenCaptureBuilder(MainActivity mainActivity){
        this.mainActivity=mainActivity;
    }

    private DisplayMetrics setupMetrics(){
        DisplayMetrics metrics = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        return metrics;
    }

    public void startRealLive(int resultCode, Intent data){
        start(resultCode,data,true);
    }

    private void start(int resultCode, Intent data, boolean real){
        DisplayMetrics metrics = setupMetrics();

        Intent intent ;
        if(real){
            intent = new Intent(mainActivity, ScreenCapture2.class);
        }else{
            intent = new Intent(mainActivity, ScreenCapture3.class);
        }

        intent.putExtra("resultCode",resultCode);
        intent.putExtra("data",data);
        intent.putExtra("metrics_width", metrics.widthPixels);
        intent.putExtra("metrics_height", metrics.heightPixels);
        intent.putExtra("metrics_densityDpi", metrics.densityDpi);
        intent.putExtra("isDemo",!real);

        //componentName = mainActivity.startService(intent); //if i dont need a new thread, use this
        Thread thread = new Thread(() -> {
            componentName = mainActivity.startService(intent);
        });
        thread.start();
    }

    public void startDemo(){
        start(-1,null,false);
    }

    public void stop(){
        mainActivity.stopService(new Intent().setComponent(componentName));
    }
}
