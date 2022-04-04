package hu.elte.sbzbxr.phoneconnect.model.connection;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FileOutputStreamProvider {
    private final Map<String, OutputStream> map = new ConcurrentHashMap<>();

    public FileOutputStreamProvider() {}

    public void endOfFileStreaming(String filename){
        Optional.ofNullable(map.get(filename)).ifPresent(outputStream -> {
            try {
                outputStream.close();
                System.err.println("OutputStream closed for: "+filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
            map.remove(filename);
        });
    }

    public OutputStream getOutputStream(ConnectionManager connectionManager, String name) {
        synchronized (map) {
            if (map.containsKey(name)) return map.get(name);
            connectionManager.askForSaveLocation(name);
            try {
                while (map.get(name) == null) {
                    map.wait();
                }
                return map.get(name);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean contains(String name){return map.containsKey(name);}

    public void onStreamCreated(String name, OutputStream fileSavingOutputStream) {
        synchronized (map){
            map.put(name,fileSavingOutputStream);
            map.notifyAll();
        }
    }
}
