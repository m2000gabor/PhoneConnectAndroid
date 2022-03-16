package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 1.0
 */
public class NetworkFrame {
    public final FrameType type;
    public final String name;

    protected NetworkFrame(NetworkFrame from) {
        this.type = from.type;
        this.name = from.name;
    }

    protected NetworkFrame(FrameType type, String name) {
        this.type = type;
        this.name = name;
    }

    public InputStream getData(){
        return new ByteArrayInputStream(getDataAsByteArray());
    }

    protected final byte[] getDataAsByteArray(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(1+4+name.getBytes().length);
        byteBuffer.put(type.v);
        byteBuffer.putInt(name.getBytes().length);
        byteBuffer.put(name.getBytes());
        return byteBuffer.array();
    }

    public boolean invalid(){return type==FrameType.INVALID;}
}
