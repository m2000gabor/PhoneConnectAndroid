package hu.elte.sbzbxr.phoneconnect.model.recording;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

public class ScreenCapture extends Service {
    private static final String LOG_TAG ="MediaRecorder ";
    private static final String VIRTUAL_DISPLAY_NAME= "VD";
    MediaProjection projection;
    MediaProjectionManager mediaProjectionManager;
    MediaRecorder mediaRecorder;
    VirtualDisplay mVirtualDisplay;
    ConnectionManager connectionManager;
    private enum Profile{HIGH,LOW,OGG,Mpeg_2,ThreeGpp}
    private static final Profile recordingProfile = Profile.ThreeGpp;

    public ScreenCapture(){}

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
        mediaRecorder.stop();
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
        mediaRecorder= new MediaRecorder();
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if(what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING){
                    Log.i(LOG_TAG, "Max filesize approaching");
                    //mr.setOutputFile(getFileLocation_Continuously());
                }else if(what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED){
                    Log.i(LOG_TAG, "Max filesize reached");
                    //Pointless to do anything here, this means the end of story
                }else if(what==MediaRecorder.MEDIA_RECORDER_INFO_NEXT_OUTPUT_FILE_STARTED){
                    Log.i(LOG_TAG, "Next outputfile started");
                    try {
                        File newFile= getFileLocation_Continuously();
                        mr.setNextOutputFile(newFile);
                        //listenOnFinish( new File(getApplicationContext().getFilesDir(), oldFileName ));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //finishedFile();
                }
            }
        });
        setRecorderProfile(mediaRecorder, metrics_width, metrics_height);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //mirroring
        mVirtualDisplay = projection.createVirtualDisplay(VIRTUAL_DISPLAY_NAME,
                metrics_width, metrics_height, metrics_densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null, null);


        mediaRecorder.start();

        try {
            mediaRecorder.setNextOutputFile(getFileLocation_Continuously());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setRecorderProfile(MediaRecorder mediaRecorder, int metrics_width, int metrics_height) {
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        CamcorderProfile profile;
        switch (recordingProfile){
            case LOW:
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setMaxFileSize(3000000);
                break;
            case OGG:
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.OGG);
                }else{
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                }
                mediaRecorder.setMaxFileSize(10000000);
                break;
            case Mpeg_2:
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS);
                mediaRecorder.setMaxFileSize(10000000);
                break;
            case ThreeGpp:
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setMaxFileSize(10000000);
                break;
            default:
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setMaxFileSize(10000000);
                break;
        }

        mediaRecorder.setVideoFrameRate(profile.videoFrameRate);
        mediaRecorder.setVideoSize(metrics_width, metrics_height);
        mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        mediaRecorder.setVideoEncoder(profile.videoCodec);
        mediaRecorder.setOutputFile(getFileLocation_Continuously());


        //mediaRecorder.setMaxFileSize(10000000);
    }


    private int filenameCounter =0;
    private String directoryPath;
    private String videoBaseName="";
    private String oldFileName="";
    private String newFileName="";
    // Saves to: /data/user/0/hu.elte.sbzbxr.phoneconnect/files/**timeDate**.mp4
    private File getFileLocation_Continuously(){
        if(filenameCounter==0){
            String timestamp= DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime())
                    .replace(':','_').replace(' ','_');
            videoBaseName="PhoneC_"+timestamp;
            File vidDirectory = new File(getApplicationContext().getFilesDir(),videoBaseName);
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
        String finalFileName=videoBaseName+partNum+fileExtension;
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
        switch (recordingProfile){
            case OGG:
                return ".ogg";
            case Mpeg_2:
                return ".m2t";
                case ThreeGpp:
                    return ".3gp";
            default:
                return ".mp4";
        }
    }

    private static FileObserver fileObserver;
    private void listenOnFinishDir(File dir) {
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
                    connectionManager.sendFile(actualFile.getPath());
                }

            }
        };
        fileObserver.startWatching();
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
