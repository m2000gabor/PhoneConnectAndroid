package hu.elte.sbzbxr.phoneconnect.model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import hu.elte.sbzbxr.phoneconnect.MainActivity;

public class ScreenCapture extends Service {
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
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        //profile.videoFrameHeight = metrics_height;
        //profile.videoFrameWidth = metrics_width;
        //mediaRecorder.setProfile(profile);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoFrameRate(profile.videoFrameRate);
        mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        mediaRecorder.setVideoEncoder(profile.videoCodec);
        mediaRecorder.setOutputFile(getFileLocation2());
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
    }

    //From: https://developer.android.com/training/data-storage/shared/media#add-item
    private FileDescriptor getFileLocation(){
        // Add a media item that other apps shouldn't see until the item is
        // fully written to the media store.
        ContentResolver resolver = getApplicationContext()
                .getContentResolver();

        // Find all video files on the primary external storage device.
        Uri videoCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            videoCollection = MediaStore.Video.Media
                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            videoCollection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues newSongDetails = new ContentValues();
        newSongDetails.put(MediaStore.Video.Media.DISPLAY_NAME,
                "My Workout Playlist.mp3");

        Uri songContentUri = resolver
                .insert(videoCollection, newSongDetails);

        try (
                ParcelFileDescriptor pfd =
                     resolver.openFileDescriptor(songContentUri, "rw", null)) {
            // Write data into the pending audio file.
            return pfd.getFileDescriptor();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        // Now that we're finished, release the "pending" status, and allow other apps
        // to play the audio track.
        newSongDetails.clear();
        newSongDetails.put(MediaStore.Audio.Media.IS_PENDING, 0);
        resolver.update(songContentUri, newSongDetails, null, null);*/
        return null;
    }

    private File getFileLocation2(){
        File f = new File(getApplicationContext().getFilesDir(), "testFile.mp4");
        return f;
    }

}
