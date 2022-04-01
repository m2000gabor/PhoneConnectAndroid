package hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.Deserializer;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.Serializer;

import java.io.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//version 2.2
public class RestorePostMessageFrame extends MessageFrame{
    private final ArrayList<AbstractMap.SimpleImmutableEntry<String,Long>> backups; //folder name, size in bytes

    public RestorePostMessageFrame(ArrayList<AbstractMap.SimpleImmutableEntry<String,Long>> backups) {
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
            ArrayList<AbstractMap.SimpleImmutableEntry<String,Long>> strings =
                    (ArrayList<AbstractMap.SimpleImmutableEntry<String,Long>>) objectInputStream.readObject();
            return new RestorePostMessageFrame(strings);
        }catch (IOException | ClassNotFoundException e){
            return new RestorePostMessageFrame(null);
        }
    }

    public List<String> getNames(){
        return backups.stream().map(AbstractMap.SimpleImmutableEntry::getKey).collect(Collectors.toList());
    }

    public List<Long> getFolderSizes(){
        return backups.stream().map(AbstractMap.SimpleImmutableEntry::getValue).collect(Collectors.toList());
    }

    public ArrayList<AbstractMap.SimpleImmutableEntry<String, Long>> getBackups() {
        return backups;
    }
}
