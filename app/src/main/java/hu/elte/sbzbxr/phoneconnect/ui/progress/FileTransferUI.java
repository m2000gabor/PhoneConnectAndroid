package hu.elte.sbzbxr.phoneconnect.ui.progress;

import android.view.View;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import hu.elte.sbzbxr.phoneconnect.databinding.ServiceItemFileSendingBinding;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.ui.ConnectedFragment;

public class FileTransferUI {
    private final ConnectedFragment connectedFragment;
    private final ServiceItemFileSendingBinding binding;
    private final ConcurrentHashMap<String,FrameProgressInfo> map; //<id,FileSize>


    public FileTransferUI(ConnectedFragment connectedFragment, ServiceItemFileSendingBinding binding) {
        this.connectedFragment = connectedFragment;
        this.binding = binding;
        map = new ConcurrentHashMap<>();
    }

    public void pieceOfFile(FileFrame frame){
        final String key = getMapKey(frame);
        if(!map.containsKey(key)){
            map.put(key,new FrameProgressInfo(frame.folderName, frame.filename, frame.folderSize, frame.getFileSize(), 0,0));
        }

        final FrameProgressInfo info = map.get(key);
        if(info.getFileSize()==-1){
            info.setFilename(frame.filename);
            info.setFileSize(frame.getFileSize());
        }

        info.arrived(frame.getDataLength());

        refreshUI();
    }

    private void refreshUI() {
        runOnUI(()->{
            if(!map.isEmpty()){
                map.entrySet().stream().filter(e->e.getValue().getFileSize()>=0).limit(1).forEach(e->{
                    final FrameProgressInfo info = e.getValue();
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
                });
            }else{
                binding.filesSendingLayoutHome.setVisibility(View.GONE);
            }
        });
    }


    public void endOfFile(FileFrame frame){
        final FrameProgressInfo info = map.remove(getMapKey(frame));
        if(info==null) return;

        if(info.hasFolder()){
            map.put(getMapKey(frame),new FrameProgressInfo(
                    info.getFolderName(),"",info.getFolderSize(),
                    -1,0,info.getArrivedBytesFromFolder()));
        }


        new Thread(()->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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


    private static int getPercentage(long actual, long total){
        return (int) Math.ceil(100*((double)actual/ (double) total));
    }

    void runOnUI(Runnable f){
        connectedFragment.requireActivity().runOnUiThread(f);
    }
}
