package hu.elte.sbzbxr.phoneconnect.model.connection;

import static hu.elte.sbzbxr.phoneconnect.model.recording.ScreenShot.JPEG_QUALITY;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.NetworkFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.SegmentFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.Serializer;
import hu.elte.sbzbxr.phoneconnect.model.recording.ScreenShot;

//version 2.0
public class ScreenShotFrame extends NetworkFrame {
    private final transient ScreenShot screenShot;
    private final String folderName;
    private SegmentFrame segmentFrame = null;

    public ScreenShotFrame(ScreenShot screenShot) {
        super(FrameType.SEGMENT);
        this.screenShot=screenShot;
        this.folderName = screenShot.getStreamId();
    }

    public synchronized void transform(){
        if(isTransformed()) return;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(3000000);
        screenShot.getBitmap().compress(Bitmap.CompressFormat.JPEG,JPEG_QUALITY,byteArrayOutputStream);
        segmentFrame = new SegmentFrame(screenShot.getName(),byteArrayOutputStream.toByteArray(),folderName);
    }

    private boolean isTransformed(){return segmentFrame!=null;}

    public static SegmentFrame deserialize(FrameType type, InputStream inputStream) throws IOException {
        return SegmentFrame.deserialize(type, inputStream);
    }

    @Override
    public Serializer serialize() {
        if(!isTransformed()) transform();
        return segmentFrame.serialize();
    }

    public ScreenShot getScreenShot() {
        return screenShot;
    }

}
