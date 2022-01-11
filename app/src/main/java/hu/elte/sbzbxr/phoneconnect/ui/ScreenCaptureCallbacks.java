package hu.elte.sbzbxr.phoneconnect.ui;

import android.app.Activity;
import android.media.projection.MediaProjectionManager;
import android.view.SurfaceView;

public interface ScreenCaptureCallbacks {
    void screenCaptureStarted();
    void screenCaptureFinished();
    Activity getActivity();
    void showPermissionRequest(MediaProjectionManager mMediaProjectionManager);
    SurfaceView getSurfaceView();
}
