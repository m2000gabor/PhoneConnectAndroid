package hu.elte.sbzbxr.phoneconnect.model.connection;

import static hu.elte.sbzbxr.phoneconnect.ui.PickLocationActivity.FILENAME_TO_CREATE;
import static hu.elte.sbzbxr.phoneconnect.ui.PickLocationActivity.FOLDERNAME_TO_CREATE;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import hu.elte.sbzbxr.phoneconnect.model.persistance.FileInFolderDescriptor;
import hu.elte.sbzbxr.phoneconnect.ui.PickLocationActivity;

public class FileOutputStreamProvider {
    private final Map<FileInFolderDescriptor, OutputStream> map = new ConcurrentHashMap<>();
    private final Map<String,Uri> gotFolderAccesses = new ConcurrentHashMap<>();
    private final ConnectionManager connectionManager;

    public FileOutputStreamProvider(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void endOfFileStreaming(final FileInFolderDescriptor key){
        Optional.ofNullable(map.get(key)).ifPresent(outputStream -> {
            try {
                outputStream.close();
                System.err.println("OutputStream closed for: "+key.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            map.remove(key);
        });
    }

    public OutputStream getOutputStream(final FileInFolderDescriptor key) {
        synchronized (map) {
            if (map.containsKey(key)) return map.get(key);
            if (gotFolderAccesses.containsKey(key.getFolderName())){
                createStream(key,gotFolderAccesses.get(key.getFolderName()));
            }else{
                askForSaveLocation(key.getFilename(),key.getFolderName());
            }
            try {
                while (map.get(key) == null) {
                    map.wait();
                }
                return map.get(key);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void createStream(final FileInFolderDescriptor desc, Uri uri) {
        try {
            DocumentFile pickedDir = DocumentFile.fromTreeUri(connectionManager, uri);
            // Create a new file and write into it
            DocumentFile newFile = pickedDir.createFile("*/*", desc.getFilename());
            OutputStream fileSavingOutputStream = connectionManager.getContentResolver().openOutputStream(newFile.getUri());
            synchronized (map){
                map.put(desc,fileSavingOutputStream);
                map.notifyAll();
            }
            if(desc.hasFolder()) gotFolderAccesses.put(desc.getFolderName(),uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("PickLocationActivity","No access to this location; cannot save the file");
        }

    }

    private void askForSaveLocation(String filename, String folderName){
        Intent intent = new Intent();
        intent.setClass(connectionManager.getApplicationContext(), PickLocationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FILENAME_TO_CREATE,filename);
        intent.putExtra(FOLDERNAME_TO_CREATE,folderName);
        connectionManager.startActivity(intent);
    }

}
