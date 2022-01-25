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
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.nio.Buffer;
import java.text.DateFormat;
import java.util.Calendar;

import hu.elte.sbzbxr.phoneconnect.model.ScreenShot;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

public class ScreenCapture2 extends Service {
    private static final String LOG_TAG ="ScreenCapture2";
    private static final String VIRTUAL_DISPLAY_NAME= "VirtualDisplay";
    MediaProjection projection;
    MediaProjectionManager mediaProjectionManager;
    VirtualDisplay mVirtualDisplay;
    ImageReader imageReader;
    ConnectionManager connectionManager;

    public ScreenCapture2(){}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Intent intent1 = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent1 = PendingIntent.getActivity(this, 0, intent1, 0);

        Notification notification1 = new NotificationCompat.Builder(this, "ScreenRecorder")
                .setContentTitle("Screen capturing")
                .setContentText("In progress...")
                .setContentIntent(pendingIntent1).build();


        startForeground(1, notification1);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("ScreenRecorder", "Screen capturing",
                NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        createMediaProjection(
                intent.getIntExtra("resultCode",0),
                intent.getParcelableExtra("data"),
                intent.getIntExtra("metrics_width",-1),
                intent.getIntExtra("metrics_height",-1),
                intent.getIntExtra("metrics_densityDpi", -1)
        );

        if(!mBound){
            Intent i = new Intent(this, ConnectionManager.class);
            bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        imageReader.close();
        projection.stop();
        mVirtualDisplay.release();
        super.onDestroy();
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
        imageReader = ImageReader.newInstance(metrics_width,metrics_height, PixelFormat.RGBA_8888,50);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                try (Image im = reader.acquireLatestImage()){
                    if(im == null || connectionManager==null){return;}
                    Image.Plane[] planes = im.getPlanes();
                    Buffer imageBuffer = planes[0].getBuffer().rewind();

                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * metrics_width;

                    // create bitmap
                    Bitmap bitmap = Bitmap.createBitmap(metrics_width + rowPadding / pixelStride, metrics_height,
                            Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(imageBuffer);
                    im.close();
                    imageBuffer.clear();

                    connectionManager.sendScreenShot(new ScreenShot(
                            getScreenShotName(), bitmap));
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

    boolean mBound=false;
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            ConnectionManager.LocalBinder binder = (ConnectionManager.LocalBinder) service;
            connectionManager = binder.getService();
            mBound = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e("ScreenCapture", "onServiceDisconnected");
            mBound = false;
        }
    };

}
