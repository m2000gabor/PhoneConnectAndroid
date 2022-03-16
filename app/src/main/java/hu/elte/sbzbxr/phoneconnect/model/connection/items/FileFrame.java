package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.IOException;
import java.io.InputStream;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 1.0
 */
public class FileFrame extends NetworkFrame{
    private final byte[] data;

    public FileFrame(FrameType type, String name, byte[] data) {
        super(type,name);
        this.data = data;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(data);
    }

    public static FileFrame deserialize(FrameType type,InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new FileFrame(type,deserializer.getString(),deserializer.getByteArray());
    }

    public int getDataLength(){return data.length;}

    public byte[] getData() {
        return data;
    }
}
