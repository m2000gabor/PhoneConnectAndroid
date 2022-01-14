package hu.elte.sbzbxr.phoneconnect;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

import hu.elte.sbzbxr.phoneconnect.model.ScreenCapture;


public class ScreenCaptureBuilder {
    private static final String VIRTUAL_DISPLAY_NAME= "VD";
    private MediaRecorder mediaRecorder;
    private final MainActivity mainActivity;
    DisplayMetrics metrics;
    MediaProjectionManager mediaProjectionManager;
    MediaProjection projection;
    VirtualDisplay mVirtualDisplay;
    ComponentName componentName;

    ScreenCaptureBuilder(MainActivity mainActivity){
        this.mainActivity=mainActivity;
    }

    private void setupMetrics(){
        metrics = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
    }

    public void start(int resultCode, Intent data,MediaProjectionManager manager){
        //mediaProjectionManager=manager;
        setupMetrics();


        Intent intent = new Intent(mainActivity, ScreenCapture.class);
        intent.putExtra("resultCode",resultCode);
        intent.putExtra("data",data);
        intent.putExtra("metrics_width", this.metrics.widthPixels);
        intent.putExtra("metrics_height", this.metrics.heightPixels);
        intent.putExtra("metrics_densityDpi", this.metrics.densityDpi);
        //componentName = mainActivity.startForegroundService(intent, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        //ContextCompat.startForegroundService(mainActivity,intent);
        componentName = mainActivity.startService(intent);

        /*
        mainActivity.startService(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }*/

        /*
        createMediaProjection(resultCode,data);
        createScreenRecorder();
        mirroring();
        mediaRecorder.start();//maybe?
        */
    }

    private void createMediaProjection(int resultCode, Intent data){
        projection = mediaProjectionManager.getMediaProjection(resultCode, data);
    }


    public void createScreenRecorder(){
        mediaRecorder= new MediaRecorder();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameHeight = metrics.heightPixels;
        profile.videoFrameWidth = metrics.widthPixels;
        mediaRecorder.setProfile(profile);
        mediaRecorder.setOutputFile(new File("test"));
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mirroring(){
        mVirtualDisplay = projection.createVirtualDisplay(VIRTUAL_DISPLAY_NAME,
                metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null, null);
    }

    public void stop(){
        mainActivity.stopService(new Intent().setComponent(componentName));

        /*
        mediaRecorder.stop();
        projection.stop();
        mVirtualDisplay.release();
         */
    }
}
