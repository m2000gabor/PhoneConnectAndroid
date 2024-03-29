package hu.elte.sbzbxr.phoneconnect.model.recording;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.LinkedList;

public class ScreenShot{
    public static final int JPEG_QUALITY=10;
    private final String name;
    private final Bitmap bitmap;
    private final String streamId;
    public final LinkedList<AbstractMap.SimpleEntry<String,String>> timestamps = new LinkedList<>();

    public ScreenShot(String name, Bitmap bitmap, String streamId) {
        this.name = name;
        this.bitmap = bitmap;
        this.streamId=streamId;
    }

    public String getName() {
        return name;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getStreamId() {
        return streamId;
    }

    public void addTimestamp(String label, Long timeInMillis){
        @SuppressLint("SimpleDateFormat") String readableTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(timeInMillis);
        timestamps.add(new AbstractMap.SimpleEntry<>(label,readableTime));
    }


    @NonNull
    @Override
    public String toString() {
        return "ScreenShot{" +
                "name='" + name + '\'' +
                ", bitmap=" + bitmap +
                ", streamId='" + streamId + '\'' +
                ", timestamps=\n" + getMapAsString() +
                '}';
    }

    private String getMapAsString(){
        StringBuilder sb = new StringBuilder();
        timestamps.forEach((item)->{
            sb.append("key=").append(item.getKey()).append("; value=").append(item.getValue()).append("\n");
        });
        return sb.toString();
    }
}
