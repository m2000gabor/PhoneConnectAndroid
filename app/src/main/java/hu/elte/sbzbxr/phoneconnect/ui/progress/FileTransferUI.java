package hu.elte.sbzbxr.phoneconnect.ui.progress;

import android.view.View;
import android.widget.Toast;

import hu.elte.sbzbxr.phoneconnect.databinding.ServiceItemFileSendingBinding;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.BackupFileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.ui.ConnectedFragment;

public class FileTransferUI {
    private final ConnectedFragment connectedFragment;
    private final ServiceItemFileSendingBinding binding;

    //only for folders
    private final FolderInfo folderInfo;
    private final FileProgressInfo fileInfo;


    public FileTransferUI(ConnectedFragment connectedFragment, ServiceItemFileSendingBinding binding) {
        this.connectedFragment = connectedFragment;
        this.binding = binding;
        folderInfo = new FolderInfo(binding);
        fileInfo = new FileProgressInfo(binding);
    }


    public void initFolder(String folderName, Long folderTotalSize){
        runOnUI(()->folderInfo.start(folderName,folderTotalSize.intValue()));
    }

    public void incomingFileTransferStarted(FileFrame fileFrame){
        incomingFileTransferStarted(fileFrame.type, fileFrame.name, fileFrame.totalSize);
    }

    public void incomingFileTransferStarted(BackupFileFrame fileFrame){
        folderInfo.start(fileFrame.folderName,fileFrame.totalSize);
        incomingFileTransferStarted(fileFrame.type, fileFrame.name, fileFrame.totalSize);
    }

    private void incomingFileTransferStarted(FrameType type, String name, int totalSize){
        runOnUI(()->{
            binding.filesSendingLayoutHome.setVisibility(View.VISIBLE);
            if(type!=FrameType.BACKUP_FILE){folderInfo.hide();}
            fileInfo.start(name,totalSize);
        });
    }

    public void pieceOfFileArrived(FileFrame fileFrame) {
        runOnUI(()-> {
            try {
                if (fileFrame.type == FrameType.FILE) {
                    fileInfo.frameArrived(fileFrame);
                } else if (fileFrame.type == FrameType.RESTORE_FILE || fileFrame.type == FrameType.BACKUP_FILE) {
                    folderInfo.frameArrived((BackupFileFrame) fileFrame);
                    fileInfo.frameArrived(fileFrame);
                }
            } catch (NoSuchFieldException e) {
                System.err.println("Unexpected file arrived, cleared UI");
                incomingFileTransferStarted(fileFrame);
            }
        });
    }

    public void incomingFileTransferStopped(FileFrame fileFrame, boolean isIncoming){
        if(isIncoming){
            connectedFragment.requireActivity().runOnUiThread(()-> {
                Toast.makeText(connectedFragment.getContext(),"File arrived: "+fileFrame.name,Toast.LENGTH_SHORT).show();
            });
        }

        if(folderInfo.isFinished() && fileInfo.isFinished()){
            runOnUI(()->binding.stopButton.setVisibility(View.GONE));

            new Thread(()->{
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(folderInfo.isFinished() && fileInfo.isFinished()){
                    runOnUI(()->binding.filesSendingLayoutHome.setVisibility(View.GONE));
                }
            }).start();
        }
    }

    static String getPercentage(int actual,int total){
        int percentage =(int) Math.round(100*((double)actual/ (double) total));
        return percentage + "%";
    }

    void runOnUI(Runnable f){
        connectedFragment.requireActivity().runOnUiThread(f);
    }
}
