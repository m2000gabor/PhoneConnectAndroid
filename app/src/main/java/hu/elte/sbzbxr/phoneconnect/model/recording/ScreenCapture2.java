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
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Calendar;

import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

public class ScreenCapture2 extends Service {
    private static final String LOG_TAG ="MediaRecorder";
    private static final String VIRTUAL_DISPLAY_NAME= "VirtualDisplay";
    MediaProjection projection;
    MediaProjectionManager mediaProjectionManager;
    VirtualDisplay mVirtualDisplay;
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
        projection.stop();
        mVirtualDisplay.release();

        super.onDestroy();
    }

    /*
    https://stackoverflow.com/questions/5524672/is-it-possible-to-use-camcorderprofile-without-audio-source/34045905
    https://stackoverflow.com/questions/61276730/media-projections-require-a-foreground-service-of-type-serviceinfo-foreground-se
     */
    private void createMediaProjection(int resultCode, Intent data,
                                       int metrics_width,int metrics_height,int metrics_densityDpi){
        mediaProjectionManager = (MediaProjectionManager) this.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        //create media projection
        projection = mediaProjectionManager.getMediaProjection(resultCode, data);

        //create screen recorder

        // Let MediaProjection callback use the SurfaceTextureHelper thread.
        //projection.registerCallback(mediaProjectionCallback, surfaceTextureHelper.getHandler());


        @SuppressLint("WrongConstant")
        ImageReader imageReader = ImageReader.newInstance(metrics_width,metrics_height, PixelFormat.RGBA_8888,10);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image im = reader.acquireLatestImage();
                if(im == null){return;}
                try {
                    File fileLocation = getFileLocation_Continuously();
                    FileOutputStream output = new FileOutputStream(fileLocation);

                    Image.Plane[] planes = im.getPlanes();
                    Buffer imageBuffer = planes[0].getBuffer().rewind();

                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * metrics_width;

                    // create bitmap
                    Bitmap bitmap = Bitmap.createBitmap(metrics_width + rowPadding / pixelStride, metrics_height,
                            Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(imageBuffer);

                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,output);

                    output.flush();
                    output.close();
                    Log.d(LOG_TAG,fileLocation.getName() + " saved");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                im.close();
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
    private String directoryPath;
    private String fileBaseName ="";
    private String oldFileName="";
    private String newFileName="";
    // Saves to: /data/user/0/hu.elte.sbzbxr.phoneconnect/files/**timeDate**.mp4
    private File getFileLocation_Continuously(){
        if(filenameCounter==0){
            String timestamp= DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime())
                    .replace(':','_').replace(' ','_');
            fileBaseName ="PhoneC_"+timestamp;
            File vidDirectory = new File(getApplicationContext().getFilesDir(), fileBaseName);
            if(vidDirectory.mkdirs()){
                Log.d(LOG_TAG,"New directory created");
            }else{
                Log.e(LOG_TAG,"Cannot create new directory");
            }
            directoryPath=vidDirectory.getPath();
            listenOnFinishDir(vidDirectory);
        }
        String fileExtension= getFileExtension();
        String partNum = "__part"+ filenameCounter;
        String finalFileName= fileBaseName +partNum+fileExtension;
        filenameCounter++;
        oldFileName = newFileName;
        newFileName = finalFileName;
        File ret =new File(directoryPath, finalFileName );
        try {
            if(ret.createNewFile()){
                Log.d(LOG_TAG,"File created");
                //listenOnFinish(ret);
            }else{
                Log.e(LOG_TAG,"File name already in use");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private String getFileExtension(){
        return ".jpg";
    }

    private static FileObserver fileObserver;
    private void listenOnFinishDir(File dir) {
        /*
        fileObserver = new FileObserver(dir.getPath(),FileObserver.CLOSE_WRITE) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                //if(event!=2){ Log.d("FileObserver","Event on file: "+f.getName()+" event: "+event); }
                if(event==FileObserver.CLOSE_WRITE){
                    if(path==null){return;}
                    File actualFile = new File(dir,path);
                    try {
                        if(Files.size(Paths.get(actualFile.getPath()))==0){return;}
                        Log.d("FileObserver", "FileSize:"+Files.size(Paths.get(actualFile.getPath())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.d("FileObserver", "called sendSegment on :"+actualFile.getName());
                    connectionManager.sendSegment(actualFile.getPath());
                }


        };
        fileObserver.startWatching();}*/
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
