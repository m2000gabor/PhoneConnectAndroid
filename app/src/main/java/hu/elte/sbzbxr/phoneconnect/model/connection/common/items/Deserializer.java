package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

//version 1.2
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

    public int getInt() throws IOException {
        byte[] len = new byte[4];
        int readBytes = inputStream.read(len);
        if(readBytes!=4){throw new IOException("Invalid structure");}
        ByteBuffer bb = ByteBuffer.wrap(len);
        return bb.getInt();
    }

    public long getLong() throws IOException {
        byte[] len = new byte[8];
        int readBytes = inputStream.read(len);
        if(readBytes!=8){throw new IOException("Invalid structure");}
        ByteBuffer bb = ByteBuffer.wrap(len);
        return bb.getLong();
    }
}
