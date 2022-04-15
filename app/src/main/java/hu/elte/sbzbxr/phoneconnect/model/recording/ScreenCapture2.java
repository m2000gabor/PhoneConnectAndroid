package hu.elte.sbzbxr.phoneconnect.model.recording;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.nio.Buffer;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import hu.elte.sbzbxr.phoneconnect.controller.ServiceController;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

public class ScreenCapture2 extends Service {
    private static final String LOG_TAG ="ScreenCapture2";
    private static final String VIRTUAL_DISPLAY_NAME= "VirtualDisplay";
    MediaProjection projection;
    MediaProjectionManager mediaProjectionManager;
    VirtualDisplay mVirtualDisplay;
    ImageReader imageReader;
    private ServiceController serviceController;

    public ScreenCapture2(){}

    private final IBinder binder = new ScreenCapture2.LocalBinder();
    public class LocalBinder extends Binder {
        public ScreenCapture2 getService() {
            // Return this instance of LocalService so clients can call public methods
            return ScreenCapture2.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("ScreenRecorder", "Screen capturing",
                NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        imageReader.close();
        projection.stop();
        mVirtualDisplay.release();
        super.onDestroy();
    }

    public void start(ServiceController controller, Notification notification, Intent intent){
        this.serviceController=controller;
        createNotificationChannel();
        startForeground(1, notification);
        createMediaProjection(
                intent.getIntExtra("resultCode",0),
                intent.getParcelableExtra("data"),
                intent.getIntExtra("metrics_width",-1),
                intent.getIntExtra("metrics_height",-1),
                intent.getIntExtra("metrics_densityDpi", -1)
        );
    }

    /*
    https://stackoverflow.com/questions/5524672/is-it-possible-to-use-camcorderprofile-without-audio-source/34045905
    https://stackoverflow.com/questions/61276730/media-projections-require-a-foreground-service-of-type-serviceinfo-foreground-se
     */
    @SuppressLint("WrongConstant")
    private void createMediaProjection(int resultCode, Intent data,
                                       int metrics_width, int metrics_height, int metrics_densityDpi){
        mediaProjectionManager = (MediaProjectionManager) this.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        //create media projection
        projection = mediaProjectionManager.getMediaProjection(resultCode, data);

        // Let MediaProjection callback use the SurfaceTextureHelper thread.
        //projection.registerCallback(mediaProjectionCallback, surfaceTextureHelper.getHandler());

        //From: https://stackoverflow.com/questions/37143968/how-to-handle-image-capture-with-mediaprojection-on-orientation-change
        imageReader = ImageReader.newInstance(metrics_width,metrics_height, PixelFormat.RGBA_8888,5);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                try (Image im = reader.acquireLatestImage()){
                    if(im == null){return;}

                    long timeStamp_firstSeen = System.currentTimeMillis();

                    Image.Plane[] planes = im.getPlanes();
                    Buffer imageBuffer = planes[0].getBuffer().rewind();

                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * metrics_width;

                    // create bitmap
                    Bitmap bitmap = Bitmap.createBitmap(metrics_width + rowPadding / pixelStride, metrics_height,
                            Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(imageBuffer);

                    long timeStamp_bitmapCreated = System.currentTimeMillis();
                    ScreenShot screenShot = new ScreenShot(
                            getScreenShotName(), bitmap, fileBaseName);
                    screenShot.addTimestamp("firstSeen",timeStamp_firstSeen);
                    screenShot.addTimestamp("bitmapCreated",timeStamp_bitmapCreated);
                    serviceController.getConnectionManager().sendScreenShot(screenShot);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },null);
        Surface virtualSurface = imageReader.getSurface();

        //mirroring
        mVirtualDisplay = projection.createVirtualDisplay(VIRTUAL_DISPLAY_NAME,
                metrics_width, metrics_height, metrics_densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC | DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                virtualSurface, null, null);
    }

    private int filenameCounter =0;
    private String fileBaseName ="";
    private String getScreenShotName(){
        if(filenameCounter==0){
            String timestamp= DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime())
                    .replace(':','_').replace(' ','_');
            fileBaseName ="PhoneC_"+timestamp;
        }
        String fileExtension= getFileExtension();
        String partNum = "__part"+ filenameCounter;
        String finalFileName= fileBaseName +partNum+fileExtension;
        filenameCounter++;
        return finalFileName;
    }

    private String getFileExtension(){
        return ".jpg";
    }
}
