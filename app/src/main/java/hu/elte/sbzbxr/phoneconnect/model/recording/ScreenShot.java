package hu.elte.sbzbxr.phoneconnect.model.recording;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.NetworkFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.SegmentFrame;

public class ScreenShot{
    public static final int JPEG_QUALITY=10;
    private final String name;
    private final Bitmap bitmap;
    private final String streamId;

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

    public SegmentFrame toFrame() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(3000000);
        getBitmap().compress(Bitmap.CompressFormat.JPEG,JPEG_QUALITY,byteArrayOutputStream);
        return new SegmentFrame(getName(),byteArrayOutputStream.toByteArray(),streamId);
    }
}
