package hu.elte.sbzbxr.phoneconnect;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import hu.elte.sbzbxr.phoneconnect.ui.ScreenCaptureCallbacks;

public class ScreenCaptureBuilder extends Service {
    public static final String TAG = "ScreenCaptureFragment";
    private int mScreenDensity;
    public int mResultCode;
    public Intent mResultData;


    private Surface mSurface;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;


    private ScreenCaptureCallbacks viewCallback;

    private Activity getActivity(){return viewCallback.getActivity();}

    public ScreenCaptureBuilder(){}

    private void init(){
        Activity activity = getActivity();
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mMediaProjectionManager = (MediaProjectionManager)
                activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mSurface=viewCallback.getSurfaceView().getHolder().getSurface();
    }

    private void setUpMediaProjection() {
        /*
        Context context = getActivity().getApplicationContext();
        Intent intent = new Intent(getActivity(),this); // Build the intent for the service
        context.startForegroundService(intent);*/
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        System.err.println("Teared down");
    }

    public void beforeUserRequest(ScreenCaptureCallbacks main) {
        viewCallback= main;
        init();
        Log.i(TAG, "Requesting confirmation");
        // This initiates a prompt dialog for the user to confirm screen projection.
        viewCallback.showPermissionRequest(mMediaProjectionManager);
        /*
        Activity activity = getActivity();
        if (mSurface == null || activity == null) {
            return;
        }
        if (mMediaProjection != null) {
            setUpVirtualDisplay();
        } else if (mResultCode != 0 && mResultData != null) {
            setUpMediaProjection();
            setUpVirtualDisplay();
        } else {
            Log.i(TAG, "Requesting confirmation");
            // This initiates a prompt dialog for the user to confirm screen projection.
            viewCallback.showPermissionRequest(mMediaProjectionManager);
        }*/

    }

    private void setUpVirtualDisplay() {
        Log.i(TAG, "Setting up a VirtualDisplay: " +
                viewCallback.getSurfaceView().getWidth() + "x" + viewCallback.getSurfaceView().getHeight() +
                " (" + mScreenDensity + ")");
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                viewCallback.getSurfaceView().getWidth(), viewCallback.getSurfaceView().getHeight(), mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mSurface, null, null);

    }

    private void stopScreenCapture() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        System.err.println("Screen ca" +
                "pture stopped");
        viewCallback.screenCaptureFinished();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        setUpMediaProjection();
        setUpVirtualDisplay();
        viewCallback.screenCaptureStarted();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopScreenCapture();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
