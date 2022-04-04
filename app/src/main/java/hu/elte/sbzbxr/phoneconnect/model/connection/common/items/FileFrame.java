package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

import java.io.IOException;
import java.io.InputStream;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 2.6
 */
public class FileFrame extends NetworkFrame{
    public final String filename;
    public final long fileTotalSize;
    public final byte[] data;
    public final String folderName;
    public final long folderSize;

    public FileFrame(FrameType type, String filename, Long fileTotalSize, byte[] data, String folderName, Long folderSize) {
        super(type);
        if(filename==null) filename="";
        if(folderName==null) folderName="";
        this.filename =filename;
        this.fileTotalSize = fileTotalSize;
        this.data = data;
        this.folderName = folderName;
        this.folderSize = folderSize;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(filename).addField(fileTotalSize).addField(data).addField(folderName).addField(folderSize);
    }

    public static FileFrame deserialize(FrameType type,InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new FileFrame(type,deserializer.getString(),deserializer.getLong(),deserializer.getByteArray(), deserializer.getString(), deserializer.getLong());
    }

    public long getFileSize(){return fileTotalSize;}
    public int getDataLength(){return data.length;}

    public byte[] getData() {
        return data;
    }
}
