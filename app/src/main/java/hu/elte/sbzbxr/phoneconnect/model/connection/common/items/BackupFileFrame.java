package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

import java.io.IOException;
import java.io.InputStream;

//version 1.2
public class BackupFileFrame extends FileFrame{

    public BackupFileFrame(FrameType type, String filename, Long totalSize, String folderName, Long folderSize, byte[] data) {
        super(type, filename, totalSize, folderName, folderSize, data);
    }

    public static BackupFileFrame deserialize(FrameType type,InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new BackupFileFrame(type,deserializer.getString(),deserializer.getLong(), deserializer.getString(), deserializer.getLong(), deserializer.getByteArray());
    }
}
