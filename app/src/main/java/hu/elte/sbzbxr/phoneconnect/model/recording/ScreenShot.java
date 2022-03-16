package hu.elte.sbzbxr.phoneconnect.model.recording;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.NetworkFrame;

public class ScreenShot{
    public static final int JPEG_QUALITY=10;
    private final String name;
    private final Bitmap bitmap;

    public ScreenShot(String name, Bitmap bitmap) {
        this.name = name;
        this.bitmap = bitmap;
    }

    public String getName() {
        return name;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public NetworkFrame toFrame() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(3000000);
        getBitmap().compress(Bitmap.CompressFormat.JPEG,JPEG_QUALITY,byteArrayOutputStream);
        return new FileFrame(FrameType.SEGMENT, getName(),byteArrayOutputStream.toByteArray());
    }
}
