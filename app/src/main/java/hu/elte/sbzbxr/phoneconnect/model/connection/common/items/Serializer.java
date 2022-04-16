package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

//version 1.3
public class Serializer {
    private final ByteArrayOutputStream outputStream;

    public Serializer(){
        outputStream = new ByteArrayOutputStream();
    }

    public Serializer addField(FrameType type){
        outputStream.write(type.v);
        return this;
    }
    public Serializer addField(String str) {
        return addField(str.getBytes());
    }

    public Serializer addField(byte[] data) {
        byte[] length = ByteBuffer.allocate(4).putInt(data.length).array();
        for (byte b : length) outputStream.write(b);
        for (byte b : data) outputStream.write(b);
        return this;
    }

    public Serializer addField(int i) {
        byte[] arr = ByteBuffer.allocate(4).putInt(i).array();
        for(byte b : arr) outputStream.write(b);
        return this;
    }

    public Serializer addField(long i) {
        byte[] arr = ByteBuffer.allocate(8).putLong(i).array();
        for(byte b : arr) outputStream.write(b);
        return this;
    }

    public Serializer addField(boolean i) {
        return addField(i?1:0);
    }

    public byte[] getAsBytes(){
        return outputStream.toByteArray();
    }
}
