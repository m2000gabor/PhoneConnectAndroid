package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

import java.io.IOException;
import java.io.InputStream;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 2.8
 */
public class FileFrame extends NetworkFrame{
    public final String filename;
    public final long fileTotalSize;
    public final String folderName;
    public final long folderSize;
    public final byte[] data;

    public FileFrame(FrameType type, String filename, Long fileTotalSize, String folderName, Long folderSize, byte[] data) {
        super(type);
        if(filename==null) filename="";
        if(folderName==null) folderName="";
        if(folderSize==null) folderSize=0L;
        this.filename =filename;
        this.fileTotalSize = fileTotalSize;
        this.data = data;
        this.folderName = folderName;
        this.folderSize = folderSize;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(filename).addField(fileTotalSize).addField(folderName).addField(folderSize).addField(data);
    }

    public static FileFrame deserialize(FrameType type,InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new FileFrame(type,deserializer.getString(),deserializer.getLong(), deserializer.getString(), deserializer.getLong(), deserializer.getByteArray());
    }

    public long getFileSize(){return fileTotalSize;}
    public int getDataLength(){return data.length;}

    public byte[] getData() {
        return data;
    }
}
