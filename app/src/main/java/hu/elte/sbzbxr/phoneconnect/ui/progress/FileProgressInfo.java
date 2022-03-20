package hu.elte.sbzbxr.phoneconnect.ui.progress;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Objects;

import hu.elte.sbzbxr.phoneconnect.databinding.ServiceItemFileSendingBinding;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;

public class FileProgressInfo {
    private final LinearLayout rootLinearLayout;
    private final ProgressBar fileProgressBar;
    private final TextView progressLabel;
    private final TextView fileNameTextView;

    private String mostRecentFileName;
    private int fullSize;
    private int arrivedBytes;
    private boolean isFinished = true;

    public FileProgressInfo(ServiceItemFileSendingBinding binding) {
        rootLinearLayout = binding.arrivingFileLinearLayout2;
        fileProgressBar = binding.progressBar;
        progressLabel = binding.progressBarLabel;
        fileNameTextView = binding.filenameTextView;
    }

    public void start(String fileName, int fullSize){
        this.mostRecentFileName=fileName;
        this.fullSize=fullSize;
        arrivedBytes=0;
        isFinished=false;

        fileProgressBar.setProgress(0);
        fileProgressBar.setMax(fullSize);
        fileNameTextView.setText(fileName);

        rootLinearLayout.setVisibility(View.VISIBLE);
        fileProgressBar.setVisibility(View.VISIBLE);
        progressLabel.setVisibility(View.VISIBLE);
    }

    public void frameArrived(FileFrame frame) throws NoSuchFieldException {
        if(!Objects.equals(frame.name, mostRecentFileName)){
            throw new NoSuchFieldException("This is from another file");
        }
        arrivedBytes+=frame.getDataLength();
        isFinished = arrivedBytes >= fullSize;
        updateProgressBar();
    }

    private void updateProgressBar(){
        fileProgressBar.setProgress(arrivedBytes);
        progressLabel.setText(FileTransferUI.getPercentage(arrivedBytes, fullSize));
    }

    public boolean isFinished(){
        return isFinished;
    }
}
