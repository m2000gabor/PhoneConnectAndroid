package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

//version 1.4
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
        if(l<0) throw new EOFException("Nothing read");
        return new String(NetworkFrameCreator.readNBytes(inputStream,l).array());
    }

    public byte[] getByteArray() throws IOException{
        int l = NetworkFrameCreator.readLength(inputStream);
        if(l<0) throw new EOFException("Nothing read");
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

    public static long getLong(byte[] arr) throws IOException {
        if(arr.length!=8){throw new IOException("Invalid structure");}
        ByteBuffer bb = ByteBuffer.wrap(arr);
        return bb.getLong();
    }

    public boolean getBool() throws IOException {
        return getInt() > 0;
    }
}