package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.IOException;
import java.io.InputStream;

//version 1.1
public class BackupFileFrame extends FileFrame{
    public final String folderName;

    public BackupFileFrame(FrameType type, String filename,int totalSize, byte[] data, String folderName) {
        super(type, filename, totalSize, data);
        if(folderName==null) {folderName="";}
        this.folderName = folderName;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(folderName);
    }

    public static BackupFileFrame deserialize(FrameType type, InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new BackupFileFrame(type,deserializer.getString(), deserializer.getInt(), deserializer.getByteArray(), deserializer.getString());
    }
}
