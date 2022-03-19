package hu.elte.sbzbxr.phoneconnect.model.connection.items.message;

import hu.elte.sbzbxr.phoneconnect.model.connection.items.Deserializer;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.Serializer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//version 2.0
public class RestorePostMessageFrame extends MessageFrame{
    private final ArrayList<String> backups;

    public RestorePostMessageFrame(ArrayList<String> backups) {
        super(MessageType.RESTORE_POST_AVAILABLE);
        this.backups = backups;
    }

    @Override
    public Serializer serialize() {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)){
            objectOutputStream.writeObject(backups);
            byte[] array = byteArrayOutputStream.toByteArray();
            return super.serialize().addField(array);
        }catch (IOException e){
            return super.serialize().addField(new byte[0]);
        }
    }

    public static RestorePostMessageFrame deserialize(InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        byte[] array = deserializer.getByteArray();
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(array))){
            ArrayList<String> strings= (ArrayList<String>) objectInputStream.readObject();
            return new RestorePostMessageFrame(strings);
        }catch (IOException | ClassNotFoundException e){
            return new RestorePostMessageFrame(null);
        }
    }

    public static List<String> getBackupList(String msg){
        return Arrays.stream(msg.split(";;;")).collect(Collectors.toList());
    }

    public List<String> getBackups(){
        return new ArrayList<>(backups);
    }
}
