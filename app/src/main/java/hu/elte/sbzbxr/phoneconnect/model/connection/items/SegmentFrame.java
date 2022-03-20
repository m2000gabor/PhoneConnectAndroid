package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.IOException;
import java.io.InputStream;

//version 1.1
public class SegmentFrame extends FileFrame{
    public final String folderName;

    public SegmentFrame(String filename, int totalSize, byte[] data, String folderName) {
        super(FrameType.SEGMENT, filename,totalSize, data);
        System.out.println("Segment created with name: "+ filename);
        if(folderName==null) {folderName="";}
        this.folderName = folderName;
    }

    public SegmentFrame(String filename, byte[] data, String folderName) {
        this(filename, 0, data, folderName);
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(folderName);
    }

    public static SegmentFrame deserialize(FrameType type, InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new SegmentFrame(deserializer.getString(), deserializer.getInt(), deserializer.getByteArray(), deserializer.getString());
    }
}
