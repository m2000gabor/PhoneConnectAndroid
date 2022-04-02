package hu.elte.sbzbxr.phoneconnect.ui.progress;

import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import hu.elte.sbzbxr.phoneconnect.databinding.ServiceItemFileSendingBinding;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.ui.ConnectedFragment;

public class FileTransferUI {
    private final ConnectedFragment connectedFragment;
    private final ServiceItemFileSendingBinding binding;
    private final Map<String,FrameProgressInfo> map; //<id,FileSize>
    private final Queue<String> arrived; //use getSetId()


    public FileTransferUI(ConnectedFragment connectedFragment, ServiceItemFileSendingBinding binding) {
        this.connectedFragment = connectedFragment;
        this.binding = binding;
        map = new HashMap<>();
        arrived = new ConcurrentLinkedQueue<>();
    }

    public void pieceOfFile(FileFrame frame){
        if(previouslyCompleted(frame)) return;

        final String key = getMapKey(frame);
        synchronized (map){
            if(!map.containsKey(key)){
                map.put(key,FrameProgressInfo.newFolder(frame.folderName, frame.filename, frame.folderSize, frame.getFileSize()));
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

    private void refreshUI() {;
        Optional<Map.Entry<String, FrameProgressInfo>> optional;
        synchronized (map){
            optional =  map.entrySet().stream().filter(e->!e.getValue().isFileArrived()).findFirst();
        }

        runOnUI(()->{
            if(!optional.isPresent()){
                binding.filesSendingLayoutHome.setVisibility(View.GONE);
            }else{
                final FrameProgressInfo info = optional.get().getValue();
                binding.filesSendingLayoutHome.setVisibility(View.VISIBLE);

                if(info.hasFolder()){
                    final int percentage = getPercentage(info.getArrivedBytesFromFolder(),info.getFolderSize());
                    binding.totalProgressBar.setProgress(percentage);
                    binding.totalProgressBarLabel.setText(percentage+"%");
                    binding.arrivingFileLinearLayout3Total.setVisibility(View.VISIBLE);
                }else{
                    binding.arrivingFileLinearLayout3Total.setVisibility(View.GONE);
                }

                final int percentage = getPercentage(info.getArrivedBytesFromFile(),info.getFileSize());
                binding.progressBar.setProgress(percentage);
                binding.progressBarLabel.setText(percentage+"%");
                binding.filenameTextView.setText(info.getFilename());
                binding.progressBar.setVisibility(View.VISIBLE);
            }
        });
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

        System.out.println("file fully arrived: "+ frame.filename);
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

    void runOnUI(Runnable f){
        connectedFragment.requireActivity().runOnUiThread(f);
    }
}
