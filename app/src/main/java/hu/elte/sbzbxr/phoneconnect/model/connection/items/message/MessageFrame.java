package hu.elte.sbzbxr.phoneconnect.model.connection.items.message;

import java.io.IOException;
import java.io.InputStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.items.Deserializer;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.NetworkFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.Serializer;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 4.1
 */
public class MessageFrame extends NetworkFrame {
    public final MessageType messageType;

    public MessageFrame(MessageType messageType) {
        super(FrameType.INTERNAL_MESSAGE);
        this.messageType = messageType;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(messageType.toString());
    }

    public static MessageFrame deserialize(InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new MessageFrame(MessageType.valueOf(deserializer.getString()));
    }
}
