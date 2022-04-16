package hu.elte.sbzbxr.phoneconnect.model.connection.fileTransferProgress;

import android.util.Log;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.fileTransferProgress.FileProgressMap;
import hu.elte.sbzbxr.phoneconnect.model.connection.fileTransferProgress.ProgressUi;
import hu.elte.sbzbxr.phoneconnect.ui.progress.FrameProgressInfo;

public class IncomingTransfer {
    private final FileProgressMap map = new FileProgressMap(); //<id,FileSize>
    private final Queue<String> arrived = new ConcurrentLinkedQueue<>(); //use getSetId()
    private final ProgressUi ui;

    public IncomingTransfer(ProgressUi ui) {
        this.ui = ui;
    }

    public void pieceOfFile(FileFrame frame){
        if(previouslyCompleted(frame)) return;

        final String key = getMapKey(frame);
        synchronized (map){
            if(!map.containsKey(key)){
                map.put(key, FrameProgressInfo.newFolder(frame.folderName, frame.filename, frame.folderSize, frame.getFileSize()));
            }

            FrameProgressInfo info = map.get(key);
            if(info.isFileArrived() && !info.isFolderArrived()){
                map.put(key,FrameProgressInfo.startNextFile(frame.folderName, frame.filename, frame.folderSize, frame.getFileSize(),info.getArrivedBytesFromFolder()));
            }

            info = map.get(key);
            map.put(key,FrameProgressInfo.updateProgress(info, frame.getDataLength()));
        }

        refreshUI();
    }

    private boolean previouslyCompleted(FileFrame frame){
        return arrived.contains(getSetId(frame));
    }

    public Queue<FrameProgressInfo> getActiveTransfers() {;
        return map.getActiveTransfers();
    }

    private void refreshUI(){
        ui.refresh(getActiveTransfers());
    }


    public void endOfFile(FileFrame frame){
        arrived.add(getSetId(frame));
        FrameProgressInfo info;

        synchronized (map){
            info = map.remove(getMapKey(frame));
            if(info==null) return;

            if(info.hasFolder()){
                map.put(getMapKey(frame),FrameProgressInfo.arrivedLastPiece(info));
            }
        }


        new Thread(()->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            arrived.remove(getSetId(info.getFolderName(),info.getFilename()));
            refreshUI();
        }).start();

        Log.d(getClass().toString(),"End of file: "+ frame.filename);
    }

    private static String getMapKey(FileFrame frame){
        if(frame.filename==null || frame.folderName.equals("")){
            return frame.filename;
        }else{
            return frame.folderName;
        }
    }

    private static String getSetId(FileFrame frame){
        return getSetId(frame.folderName,frame.filename);
    }

    private static String getSetId(String folderName, String filename){
        String folderPart=folderName;
        String filePart= filename;
        if(filePart==null) filePart="";
        if(folderPart==null) folderPart="";

        return folderPart + "/" + filePart;
    }


    private static int getPercentage(long actual, long total){
        return (int) Math.ceil(100*((double)actual/ (double) total));
    }
}
