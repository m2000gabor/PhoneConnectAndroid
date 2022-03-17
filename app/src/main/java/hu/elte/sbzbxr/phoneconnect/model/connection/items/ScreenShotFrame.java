package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import static hu.elte.sbzbxr.phoneconnect.model.recording.ScreenShot.JPEG_QUALITY;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenShot;

public class ScreenShotFrame extends NetworkFrame{
    private final transient ScreenShot screenShot;
    private byte[] data;

    public ScreenShotFrame(ScreenShot screenShot) {
        super(FrameType.SEGMENT, screenShot.getName());
        this.screenShot=screenShot;
        data=null;
    }

    public void transform(){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(3000000);
        screenShot.getBitmap().compress(Bitmap.CompressFormat.JPEG,JPEG_QUALITY,byteArrayOutputStream);
        data = byteArrayOutputStream.toByteArray();
    }

    @Override
    public Serializer serialize() {
        transform();
        return super.serialize().addField(data);
    }
}
