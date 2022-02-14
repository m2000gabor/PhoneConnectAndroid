package hu.elte.sbzbxr.phoneconnect.model;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.MyNetworkProtocolFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.Sendable;

//Todo: really costly operation (in time and ram as well), store only the stream, not the byte[]
public class SendableFile implements Sendable {
    private final String path;
    private byte[] data;

    public SendableFile(Uri uri, ContentResolver contentResolver){
        String[] strings =uri.getPath().split("/");
        this.path=strings[strings.length-1];
        try (InputStream inputStream =contentResolver.openInputStream(uri);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ) {
            int read=inputStream.read();
            while (read != -1) {
                byteArrayOutputStream.write(read);
                read=inputStream.read();
            }
            data=byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MyNetworkProtocolFrame toFrame() {
        return new MyNetworkProtocolFrame(
                MyNetworkProtocolFrame.FrameType.PROTOCOL_FILE,
                path,data);
    }

    @Override
    public String getTypeName() {
        return "file";
    }
}
