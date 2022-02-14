package hu.elte.sbzbxr.phoneconnect.controller;

import android.content.ComponentName;
import android.content.Intent;
import android.util.DisplayMetrics;

import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenCapture2;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;
import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenCapture;


public class ScreenCaptureBuilder {
    private final MainActivity mainActivity;
    DisplayMetrics metrics;
    ComponentName componentName;

    public ScreenCaptureBuilder(MainActivity mainActivity){
        this.mainActivity=mainActivity;
    }

    private void setupMetrics(){
        metrics = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
    }

    public void start(int resultCode, Intent data){
        setupMetrics();

        Intent intent = new Intent(mainActivity, ScreenCapture2.class);
        intent.putExtra("resultCode",resultCode);
        intent.putExtra("data",data);
        intent.putExtra("metrics_width", this.metrics.widthPixels);
        intent.putExtra("metrics_height", this.metrics.heightPixels);
        intent.putExtra("metrics_densityDpi", this.metrics.densityDpi);

        //componentName = mainActivity.startService(intent); //if i dont need a new thread, use this
        Thread thread = new Thread(() -> {
            componentName = mainActivity.startService(intent);
        });
        thread.start();
    }

    public void stop(){mainActivity.stopService(new Intent().setComponent(componentName));}
}
