package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.ByteArrayOutputStream;

public class Serializer {
    private final ByteArrayOutputStream byteArray;

    public Serializer(){
        byteArray = new ByteArrayOutputStream();
    }

    public Serializer addField(FrameType type){
        byteArray.write(type.v);
        return this;
    }
    public Serializer addField(String str) {
        byteArray.write(str.getBytes().length);
        for (byte b : str.getBytes()) byteArray.write(b);
        return this;
    }

    public Serializer addField(byte[] data) {
        byteArray.write(data.length);
        for (byte b : data) byteArray.write(b);
        return this;
    }

    public byte[] getAsBytes(){
        return byteArray.toByteArray();
    }
}
