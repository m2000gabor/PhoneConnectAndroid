package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

//version 3.0
public class SegmentFrame extends FileFrame{
    public final LinkedList<AbstractMap.SimpleEntry<String,String>> timestamps = new LinkedList<>();

    public SegmentFrame(String filename, Long totalSize, String folderName, Long folderSize, byte[] data) {
        super(FrameType.SEGMENT, filename,totalSize, folderName, folderSize, data);
        System.out.println("Segment created with name: "+ filename);
        if(folderName==null) {folderName="";}
    }

    public SegmentFrame(String filename, String folderName, byte[] data) {
        this(filename, 0L, folderName, 0L, data);
    }


    public static SegmentFrame deserialize(FrameType type,InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new SegmentFrame(deserializer.getString(),deserializer.getLong(), deserializer.getString(), deserializer.getLong(), deserializer.getByteArray());
    }

    public void addTimestamp(String label, Long timeInMillis){
        String readableTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(timeInMillis);
        timestamps.add(new AbstractMap.SimpleEntry<>(label,readableTime));
    }

    @Override
    public String toString() {
        return "SegmentFrame{" +
                "filename='" + filename + '\'' +
                ", fileTotalSize=" + fileTotalSize +
                ", data=" + Arrays.toString(data) +
                ", folderName='" + folderName + '\'' +
                ", folderSize=" + folderSize +
                ", type=" + type +
                ", timestamps=\n" + getMapAsString(timestamps) +
                '}';
    }

    private static String getMapAsString(LinkedList<AbstractMap.SimpleEntry<String,String>> timestamps){
        StringBuilder sb = new StringBuilder();
        timestamps.forEach((item)->{
            sb.append("key=").append(item.getKey()).append("; value=").append(item.getValue()).append("\n");
        });
        return sb.toString();
    }

    public static SegmentFrame createFromParts(List<UdpSegmentFramePart> arrivedParts) throws IOException {
        arrivedParts.sort((o1, o2) -> (int) (o1.originalFramePartId - o2.originalFramePartId));
        if(arrivedParts.size()<1 || !arrivedParts.get(0).isHeadPiece) throw new IOException("No head part arrived");

        ByteArrayOutputStream out = new ByteArrayOutputStream(arrivedParts.get(0).totalFrameSize);
        for (UdpSegmentFramePart p : arrivedParts) {
            out.write(p.data);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        FrameType type = NetworkFrameCreator.getType(in);
        if(type!=FrameType.SEGMENT_PART_UDP) throw new IOException("Type field is corrupted");
        return SegmentFrame.deserialize(type,in);
    }
}
