package hu.elte.sbzbxr.phoneconnect.model.recording;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.nio.Buffer;
import java.text.DateFormat;
import java.util.Calendar;

import hu.elte.sbzbxr.phoneconnect.R;
import hu.elte.sbzbxr.phoneconnect.controller.ServiceController;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

@Deprecated //Not finished yet
public class ScreenCapture_Refactored extends Service {
    private static final String LOG_TAG ="ScreenCapture3";
    private static final String VIRTUAL_DISPLAY_NAME= "VirtualDisplay";
    VirtualDisplay mVirtualDisplay;
    ImageReader imageReader;
    ConnectionManager connectionManager;

    public ScreenCapture_Refactored(){}

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

        boolean isDemo = intent.getBooleanExtra("isDemo",false);
        int width = intent.getIntExtra("metrics_width",-1);
        int height = intent.getIntExtra("metrics_height",-1);
        int densityDpi = intent.getIntExtra("metrics_densityDpi", -1);

        if(isDemo){
            createDemoProjection(width,height);
        }else{
            createRealProjection(
                    intent.getIntExtra("resultCode",0),
                    intent.getParcelableExtra("data"),
                    width,
                    height,
                    densityDpi
            );
        }

        if(!mBound){
            Intent i = new Intent(this, ConnectionManager.class);
            bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        imageReader.close();
        //projection.stop();
        mVirtualDisplay.release();
        super.onDestroy();
    }

    /*
    Used:
    https://stackoverflow.com/questions/5524672/is-it-possible-to-use-camcorderprofile-without-audio-source/34045905
    https://stackoverflow.com/questions/61276730/media-projections-require-a-foreground-service-of-type-serviceinfo-foreground-se
    https://stackoverflow.com/questions/37143968/how-to-handle-image-capture-with-mediaprojection-on-orientation-change
     */
    private void createRealProjection(int resultCode, Intent data, int metrics_width, int metrics_height, int metrics_densityDpi){
        imageReader = createImageReader(this,metrics_width,metrics_height);

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) this.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        MediaProjection projection = mediaProjectionManager.getMediaProjection(resultCode, data);

        mVirtualDisplay = createVirtualDisplay(projection,imageReader.getSurface(), metrics_width, metrics_height, metrics_densityDpi);
    }

    private void createDemoProjection(int metrics_width, int metrics_height){
        imageReader = createImageReader(this,metrics_width,metrics_height);
        createMediaPlayer(this,imageReader.getSurface());
    }

    @SuppressLint("WrongConstant")
    private static ImageReader createImageReader(ScreenCapture_Refactored instance, int metrics_width, int metrics_height){
        ImageReader imageReader = ImageReader.newInstance(metrics_width,metrics_height, PixelFormat.RGBA_8888,50);
        imageReader.setOnImageAvailableListener(reader -> instance.onImageAvailable(reader,metrics_width,metrics_height),null);
        return imageReader;
    }

    private static VirtualDisplay createVirtualDisplay(MediaProjection projection,Surface virtualSurface, int metrics_width, int metrics_height, int metrics_densityDpi){
        return projection.createVirtualDisplay(VIRTUAL_DISPLAY_NAME,
                metrics_width, metrics_height, metrics_densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC | DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                virtualSurface, null, null);
    }

    private static void createMediaPlayer(Context context,Surface surface){
        Resources resources = context.getResources();
        Uri uri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(R.raw.demovideo))
                .appendPath(resources.getResourceTypeName(R.raw.demovideo))
                .appendPath(resources.getResourceEntryName(R.raw.demovideo))
                .build();

        MediaPlayer player=new MediaPlayer();
        player.setSurface(surface);
        try {
            player.setDataSource(context, uri);
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    player.start();
                    player.setLooping(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onImageAvailable(ImageReader reader,int metrics_width,int metrics_height){
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
                    getScreenShotName(), bitmap, fileBaseName));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    private final ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            ServiceController.LocalBinder binder = (ServiceController.LocalBinder) service;
            connectionManager = binder.getService().getConnectionManager();
            mBound = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e("ScreenCapture", "onServiceDisconnected");
            mBound = false;
        }
    };

}
