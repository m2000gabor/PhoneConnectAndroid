package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.IOException;
import java.io.InputStream;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 1.0
 */
public class PingFrame extends NetworkFrame{
    public PingFrame(String msg) {
        super(FrameType.PING, msg);
    }

    public static PingFrame deserialize(InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new PingFrame(deserializer.getString());
    }
}
