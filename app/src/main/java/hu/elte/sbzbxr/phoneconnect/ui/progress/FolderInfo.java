package hu.elte.sbzbxr.phoneconnect.ui.progress;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Objects;

import hu.elte.sbzbxr.phoneconnect.databinding.ServiceItemFileSendingBinding;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.BackupFileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;

public class FolderInfo {
    private final LinearLayout rootLinearLayout;
    private final ProgressBar folderProgressBar;
    private final TextView progressLabel;

    private String mostRecentFolderName;
    private int folderTotalSize;
    private int folderTotalArrived;
    private boolean isFinished = true;

    public FolderInfo(ServiceItemFileSendingBinding binding) {
        rootLinearLayout = binding.arrivingFileLinearLayout3Total;
        folderProgressBar = binding.totalProgressBar;
        progressLabel = binding.totalProgressBarLabel;
    }

    public void start(String mostRecentFolderName, int folderTotalSize){
        this.mostRecentFolderName=mostRecentFolderName;
        this.folderTotalSize=folderTotalSize;
        folderTotalArrived=0;

        folderProgressBar.setProgress(0);
        folderProgressBar.setMax(folderTotalSize);

        rootLinearLayout.setVisibility(View.VISIBLE);
        folderProgressBar.setVisibility(View.VISIBLE);
        progressLabel.setVisibility(View.VISIBLE);

        isFinished=false;
    }

    public void frameArrived(BackupFileFrame frame) throws NoSuchFieldException {
        if(!Objects.equals(frame.folderName, mostRecentFolderName)){
            throw new NoSuchFieldException("This is from another folder");
        }
        folderTotalArrived+=frame.getDataLength();
        isFinished = folderTotalArrived>=folderTotalSize;
        updateProgressBar();
    }

    private void updateProgressBar(){
        folderProgressBar.setProgress(folderTotalArrived);
        progressLabel.setText(FileTransferUI.getPercentage(folderTotalArrived,folderTotalSize));
    }

    public boolean isFinished(){
        return isFinished;
    }

    public void hide(){
        rootLinearLayout.setVisibility(View.GONE);
    }
}
