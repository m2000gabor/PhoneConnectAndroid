package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.IOException;
import java.io.InputStream;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 2.0
 */
public class MessageFrame extends NetworkFrame{
    public final String message;
    public MessageFrame(MessageType messageType, String msg) {
        super(FrameType.INTERNAL_MESSAGE, messageType.toString());
        message=msg;
    }

    public static MessageFrame deserialize(InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new MessageFrame(MessageType.valueOf(deserializer.getString()),deserializer.getString());
    }
}
