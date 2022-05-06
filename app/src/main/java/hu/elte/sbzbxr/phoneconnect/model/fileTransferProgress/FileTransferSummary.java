package hu.elte.sbzbxr.phoneconnect.model.fileTransferProgress;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FileFrame;

public class FileTransferSummary {
    private final ConcurrentLinkedDeque<FrameProgressInfo> inProgressFiles = new ConcurrentLinkedDeque<>(); //use getSetId()
    private final Queue<String> alreadyArrivedFiles = new ConcurrentLinkedQueue<>(); //use getSetId()

    public FileTransferSummary() {}

    public void pieceOfFile(FileFrame frame){
        if(previouslyCompleted(frame)) return;

        FrameProgressInfo toInsert = FrameProgressInfo.newFolder(frame.folderName, frame.filename, frame.getFileSize());
        FrameProgressInfo toRemove = null;
        for(FrameProgressInfo info : inProgressFiles) {
            if (Objects.equals(info.getFilename(), frame.filename) && Objects.equals(info.getFolderName(), frame.folderName)) {
                toInsert = FrameProgressInfo.updateProgress(info, frame.getData().length);
                toRemove = info;
            }
        }

        if(toRemove!=null) inProgressFiles.remove(toRemove);
        inProgressFiles.addFirst(toInsert);
    }

    private boolean previouslyCompleted(FileFrame frame){
        return alreadyArrivedFiles.contains(getUniqueId(frame));
    }

    public Queue<FrameProgressInfo> getActiveTransfers() {
        return new ArrayDeque<>(inProgressFiles);
    }

    public void endOfFile(FileFrame frame){
        alreadyArrivedFiles.add(getUniqueId(frame));

        FrameProgressInfo oldInfo = inProgressFiles.stream().filter(f -> Objects.equals(f.getFilename(), frame.filename)).findFirst().orElse(null);
        if(oldInfo==null){
            System.err.println("This file end belongs to an unknown transfer");
        }else{
            inProgressFiles.remove(oldInfo);
        }
        new Thread(()->{
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            alreadyArrivedFiles.remove(getUniqueId(frame));
        }).start();

        Log.d(getClass().toString(),"End of file: "+ frame.filename);
    }

    private static String getUniqueId(FileFrame frame){
        return getUniqueId(frame.folderName,frame.filename);
    }
    private static String getUniqueId(String folderName, String filename){
        String folderPart=folderName;
        String filePart= filename;
        if(filePart==null) filePart="";
        if(folderPart==null) folderPart="";

        return folderPart + "/" + filePart;
    }
}
