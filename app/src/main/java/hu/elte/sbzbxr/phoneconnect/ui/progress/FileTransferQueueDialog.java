package hu.elte.sbzbxr.phoneconnect.ui.progress;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import hu.elte.sbzbxr.phoneconnect.model.fileTransferProgress.FrameProgressInfo;

public class FileTransferQueueDialog extends DialogFragment{

        public interface FileTransferDialogListener {
            public void stopAllOutgoingTransfer();
            public void stopAllIncomingTransfer();
        }

        private final Queue<FrameProgressInfo> progressQueue;
        private final List<String> items;
        private FileTransferDialogListener listener;
        private final boolean isOutgoing;

        public FileTransferQueueDialog(Queue<FrameProgressInfo> progressQueue, boolean isOutgoing) {
            this.progressQueue = progressQueue;
            this.items = progressQueue.stream().map(entry->entry.getFilename()+" ("+entry.getFileSize()+" bytes)").collect(Collectors.toList());
            this.isOutgoing = isOutgoing;
        }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            // Verify that the host activity implements the callback interface
            try {
                // Instantiate the NoticeDialogListener so we can send events to the host
                listener = (FileTransferDialogListener) context;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(context.getClass().toString()
                        + " must implement FileTransferDialogListener");
            }
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Ongoing transfers")
                    .setItems(items.toArray(new String[0]), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //listener.onListItemSelected(entries.get(which),items.get(which));
                        }
                    });

            builder.setNegativeButton("Stop all", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(isOutgoing){
                        listener.stopAllOutgoingTransfer();
                    }else{
                        listener.stopAllIncomingTransfer();
                    }
                }
            });
            builder.setPositiveButton("OK", null);
            return builder.create();
        }
}
