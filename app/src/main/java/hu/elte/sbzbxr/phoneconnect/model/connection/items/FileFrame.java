package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.IOException;
import java.io.InputStream;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 2.0
 */
public class FileFrame extends NetworkFrame{
    public final String name;
    public final int totalSize;
    public final byte[] data;

    public FileFrame(FrameType type, String filename, int totalSize, byte[] data) {
        super(type);
        this.name=filename;
        this.totalSize = totalSize;
        this.data = data;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(name).addField(totalSize).addField(data);
    }

    public static FileFrame deserialize(FrameType type,InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new FileFrame(type,deserializer.getString(),deserializer.getInt(),deserializer.getByteArray());
    }

    public int getDataLength(){return data.length;}

    public byte[] getData() {
        return data;
    }
}
