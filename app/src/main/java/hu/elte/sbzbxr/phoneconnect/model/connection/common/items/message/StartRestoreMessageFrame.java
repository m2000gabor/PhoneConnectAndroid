package hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message;

import java.io.IOException;
import java.io.InputStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.Deserializer;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.Serializer;

public class StartRestoreMessageFrame extends MessageFrame{
    public final String backupId;

    public StartRestoreMessageFrame(String backupId) {
        super(MessageType.RESTORE_START_RESTORE);
        this.backupId = backupId;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(backupId);
    }

    public static StartRestoreMessageFrame deserialize(InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new StartRestoreMessageFrame(deserializer.getString());
    }
}
