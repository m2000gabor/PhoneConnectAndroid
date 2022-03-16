package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class Deserializer {
    private final InputStream inputStream;

    public Deserializer(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public FrameType getFrameType() throws IOException {
        int i = inputStream.read();
        if(i<0) throw new EOFException("Nothing read");
        return NetworkFrameCreator.getFrameTypeFromByte((byte)i);
    }
    public String getString() throws IOException{
        int l = NetworkFrameCreator.readLength(inputStream);
        return new String(NetworkFrameCreator.readNBytes(inputStream,l).array());
    }

    public byte[] getByteArray() throws IOException{
        int l = NetworkFrameCreator.readLength(inputStream);
        return NetworkFrameCreator.readNBytes(inputStream,l).array();
    }
}
