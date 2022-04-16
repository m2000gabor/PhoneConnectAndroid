package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

import java.io.ByteArrayInputStream;
import java.io.IOException;

//version 1.6
public class UdpSegmentFramePart extends NetworkFrame {
    public static final int MAX_FRAME_PART_SIZE =60000;

    public final long originalFrameId;
    public final long originalFramePartId;
    public final int totalFrameSize;
    public final boolean isLastPiece;
    public final boolean isHeadPiece;
    public final byte[] data;

    public UdpSegmentFramePart(long originalFrameId, long originalFramePartId, int totalFrameSize, boolean isLastPiece, boolean isHeadPiece, byte[] data) throws IOException {
        super(FrameType.SEGMENT_PART_UDP);
        if(data.length>MAX_FRAME_PART_SIZE) throw new IOException("Too big for a single udp packet");
        this.originalFrameId = originalFrameId;
        this.originalFramePartId = originalFramePartId;
        this.totalFrameSize = totalFrameSize;
        this.isLastPiece = isLastPiece;
        this.isHeadPiece = isHeadPiece;
        this.data = data;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(originalFrameId).addField(originalFramePartId).addField(totalFrameSize).addField(isLastPiece).addField(isHeadPiece).addField(data);
    }

    public static UdpSegmentFramePart deserialize(byte[] buffer) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
        Deserializer d = new Deserializer(inputStream);
        FrameType type = d.getFrameType();
        if(type!=FrameType.SEGMENT_PART_UDP) throw new IOException("Type field corrupted");
        return new UdpSegmentFramePart(d.getLong(),d.getLong(),d.getInt(),d.getBool(),d.getBool(),d.getByteArray());
    }
}
