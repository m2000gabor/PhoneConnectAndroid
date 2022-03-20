package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

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

    public byte[] getAsBytes(){
        return outputStream.toByteArray();
    }
}
