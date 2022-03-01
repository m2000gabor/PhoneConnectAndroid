package hu.elte.sbzbxr.phoneconnect.model;

import hu.elte.sbzbxr.phoneconnect.model.connection.MyNetworkProtocolFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.Sendable;

public class SendableFile implements Sendable {
    private final String name;
    private final byte[] data;

    /*
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
    }*/

    public SendableFile(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    @Override
    public MyNetworkProtocolFrame toFrame() {
        return new MyNetworkProtocolFrame(
                MyNetworkProtocolFrame.FrameType.PROTOCOL_FILE,
                name,data);
    }

    @Override
    public String getTypeName() {
        return "file";
    }
}
