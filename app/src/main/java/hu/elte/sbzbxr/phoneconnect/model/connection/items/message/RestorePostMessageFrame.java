package hu.elte.sbzbxr.phoneconnect.model.connection.items.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import hu.elte.sbzbxr.phoneconnect.model.connection.items.Deserializer;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.Serializer;

public class RestorePostMessageFrame extends MessageFrame{
    private final List<String> backups;

    public RestorePostMessageFrame(List<String> backups) {
        super(MessageType.RESTORE_POST_AVAILABLE);
        this.backups = backups;
    }

    @Override
    public Serializer serialize() {
        String listAsString = backups.stream().reduce("", (s, s2) -> s +";;;"+s2);
        return super.serialize().addField(listAsString);
    }

    public static RestorePostMessageFrame deserialize(InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new RestorePostMessageFrame(getBackupList(deserializer.getString()));
    }

    public static List<String> getBackupList(String msg){
        return Arrays.stream(msg.split(";;;")).collect(Collectors.toList());
    }

    public List<String> getBackups(){
        return new ArrayList<>(backups);
    }
}
