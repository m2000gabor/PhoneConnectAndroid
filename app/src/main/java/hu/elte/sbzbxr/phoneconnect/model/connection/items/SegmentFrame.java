package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.IOException;
import java.io.InputStream;

public class SegmentFrame extends FileFrame{
    public final String folderName;

    public SegmentFrame(String filename, byte[] data, String folderName) {
        super(FrameType.SEGMENT, filename, data);
        System.out.println("Segment created with name: "+ filename);
        if(folderName==null) {folderName="";}
        this.folderName = folderName;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(folderName);
    }

    public static SegmentFrame deserialize(FrameType type, InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new SegmentFrame(deserializer.getString(),deserializer.getByteArray(), deserializer.getString());
    }
}
