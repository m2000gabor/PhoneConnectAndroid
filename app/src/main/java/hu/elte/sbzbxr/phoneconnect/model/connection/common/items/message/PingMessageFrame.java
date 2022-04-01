package hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message;

import java.io.IOException;
import java.io.InputStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.Deserializer;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.Serializer;

public class PingMessageFrame extends MessageFrame{
    public final String message;

    public PingMessageFrame(String message) {
        super(MessageType.PING);
        this.message = message;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(message);
    }

    public static PingMessageFrame deserialize(InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new PingMessageFrame(deserializer.getString());
    }
}
