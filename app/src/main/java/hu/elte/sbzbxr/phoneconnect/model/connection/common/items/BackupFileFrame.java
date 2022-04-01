package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

import java.io.IOException;
import java.io.InputStream;

//version 1.1
public class BackupFileFrame extends FileFrame{

    public BackupFileFrame(FrameType type, String filename, Long totalSize, byte[] data, String folderName, Long folderSize) {
        super(type, filename, totalSize, data, folderName, folderSize);
    }

    public static BackupFileFrame deserialize(FrameType type,InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new BackupFileFrame(type,deserializer.getString(),deserializer.getLong(),deserializer.getByteArray(), deserializer.getString(), deserializer.getLong());
    }
}
