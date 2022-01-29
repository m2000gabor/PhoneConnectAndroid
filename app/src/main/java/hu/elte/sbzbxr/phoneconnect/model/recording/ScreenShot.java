package hu.elte.sbzbxr.phoneconnect.model.recording;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import hu.elte.sbzbxr.phoneconnect.model.connection.MyNetworkProtocolFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.Sendable;

public class ScreenShot implements Sendable {
    private static final int JPEG_QUALITY=10;
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

    @Override
    public MyNetworkProtocolFrame toFrame() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(3000000);
        getBitmap().compress(Bitmap.CompressFormat.JPEG,JPEG_QUALITY,byteArrayOutputStream);
        return new MyNetworkProtocolFrame(
                MyNetworkProtocolFrame.FrameType.PROTOCOL_SEGMENT,
                getName(),byteArrayOutputStream.toByteArray());
    }

    @Override
    public String getTypeName() {
        return "Screenshot";
    }
}
