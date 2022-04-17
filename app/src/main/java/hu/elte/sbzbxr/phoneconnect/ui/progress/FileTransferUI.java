package hu.elte.sbzbxr.phoneconnect.ui.progress;

import android.util.Log;
import android.view.View;

import java.util.Queue;

import hu.elte.sbzbxr.phoneconnect.databinding.ServiceItemFileSendingBinding;
import hu.elte.sbzbxr.phoneconnect.model.fileTransferProgress.FrameProgressInfo;
import hu.elte.sbzbxr.phoneconnect.ui.ConnectedFragment;

public class FileTransferUI {
    private final ConnectedFragment connectedFragment;
    private final ServiceItemFileSendingBinding binding;
    private final boolean isOutgoing;

    public FileTransferUI(ConnectedFragment connectedFragment, ServiceItemFileSendingBinding binding, boolean isOutgoing) {
        this.connectedFragment = connectedFragment;
        this.binding = binding;
        this.isOutgoing = isOutgoing;
    }

    public void refresh(Queue<FrameProgressInfo> activeTransfers) {
        runOnUI(()->{
            final FrameProgressInfo info = activeTransfers.peek();
            if(info==null){
                binding.filesSendingLayoutHome.setVisibility(View.GONE);
            }else{
                binding.filesSendingLayoutHome.setVisibility(View.VISIBLE);
                final int percentage = getPercentage(info.getArrivedBytesFromFile(),info.getFileSize());
                binding.progressBar.setProgress(percentage);
                binding.progressBarLabel.setText(percentage+"%");
                binding.filenameTextView.setText(info.getFilename());
                binding.progressBar.setVisibility(View.VISIBLE);

                if(activeTransfers.size()>1){
                    binding.showAllFileTransferButton.setVisibility(View.VISIBLE);
                    if(isOutgoing){
                        binding.showAllFileTransferButton.setOnClickListener(v->connectedFragment.showAllOutgoingTransfer());
                    }else{
                        binding.showAllFileTransferButton.setOnClickListener(v->connectedFragment.showAllIncomingTransfer());
                    }

                }else{
                    binding.showAllFileTransferButton.setVisibility(View.GONE);
                }
            }
        });
    }

    public static int getPercentage(long actual, long total){
        return (int) Math.ceil(100*((double)actual/ (double) total));
    }

    void runOnUI(Runnable f){
        try{
            connectedFragment.requireActivity().runOnUiThread(f);
        }catch (IllegalStateException e){
            Log.d(this.getClass().getName(),"This fragment is not attached to an Activity");
        }
    }
}
