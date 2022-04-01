package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

import java.io.IOException;
import java.io.InputStream;

//version 1.1
public class SegmentFrame extends FileFrame{

    public SegmentFrame(String filename, Long totalSize, byte[] data, String folderName, Long folderSize) {
        super(FrameType.SEGMENT, filename,totalSize, data, folderName, folderSize);
        System.out.println("Segment created with name: "+ filename);
        if(folderName==null) {folderName="";}
    }

    public SegmentFrame(String filename, byte[] data, String folderName) {
        this(filename, 0L, data, folderName,0L);
    }


    public static SegmentFrame deserialize(FrameType type,InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new SegmentFrame(deserializer.getString(),deserializer.getLong(),deserializer.getByteArray(), deserializer.getString(), deserializer.getLong());
    }
}
