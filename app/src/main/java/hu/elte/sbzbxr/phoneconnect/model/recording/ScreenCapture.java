package hu.elte.sbzbxr.phoneconnect.model.recording;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

public class ScreenCapture extends Service {
    private static final String MEDIA_RECORDER_LOG ="MediaRecorder ";
    private static final String VIRTUAL_DISPLAY_NAME= "VD";
    MediaProjection projection;
    MediaProjectionManager mediaProjectionManager;
    MediaRecorder mediaRecorder;
    VirtualDisplay mVirtualDisplay;

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
                .setContentTitle("yNote studios")
                .setContentText("Filming...")
                .setContentIntent(pendingIntent1).build();



        startForeground(1, notification1);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("ScreenRecorder", "Foreground notification",
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
                    Log.i(MEDIA_RECORDER_LOG, "Max filesize approaching");
                }else if(what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED){
                    Log.i(MEDIA_RECORDER_LOG, "Max filesize reached");
                    mr.setOutputFile(getFileLocation_Continuously());
                }else if(what==MediaRecorder.MEDIA_RECORDER_INFO_NEXT_OUTPUT_FILE_STARTED){
                    Log.i(MEDIA_RECORDER_LOG, "Next outputfile started");
                    try {
                        mr.setNextOutputFile(getFileLocation_Continuously());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finishedFile();
                }
            }
        });
        setRecorderProfileMP4(mediaRecorder, metrics_width, metrics_height);

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

    private void setRecorderProfileMP4(MediaRecorder mediaRecorder, int metrics_width, int metrics_height) {
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoFrameRate(profile.videoFrameRate);
        mediaRecorder.setVideoSize(metrics_width, metrics_height);
        mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        mediaRecorder.setVideoEncoder(profile.videoCodec);
        //mediaRecorder.setOutputFile(getFileLocation2(".mp4"));
        mediaRecorder.setOutputFile(getFileLocation_Continuously());

        mediaRecorder.setMaxFileSize(5000000);
    }


    // Saves to: /data/user/0/hu.elte.sbzbxr.phoneconnect/files/**timeDate**.mp4
    private @Deprecated File getFileLocation2(String fileExtension){
        File f = new File(getApplicationContext().getFilesDir(), "testFile_"+ Calendar.getInstance().get(Calendar.DATE)+fileExtension);
        return f;
    }


    private int filenameCounter =0;
    private String videoBaseName="";
    private String oldFileName="";
    private String newFileName="";
    private File getFileLocation_Continuously(){
        if(filenameCounter==0){
            String timestamp= DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()).toString().replace(':','_');
            videoBaseName="PhoneC_"+timestamp;}
        String fileExtension= ".mp4";
        String partNum = "__part"+ filenameCounter;
        String finalFileName=videoBaseName+partNum+fileExtension;
        filenameCounter++;
        oldFileName = newFileName;
        newFileName = finalFileName;
        return new File(getApplicationContext().getFilesDir(), finalFileName );
    }

    private void finishedFile(){
        sendMessageToActivity(oldFileName);
    }

    //From: https://stackoverflow.com/questions/30629071/sending-a-simple-message-from-service-to-activity
    private void sendMessageToActivity(String str) {
        Intent intent = new Intent("SegmentFinished");
        intent.putExtra("filename", str);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
