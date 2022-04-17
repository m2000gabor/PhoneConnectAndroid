package hu.elte.sbzbxr.phoneconnect.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RestoreListDialog extends DialogFragment {
    public interface NoticeListDialogListener {
        public void onListItemSelected(AbstractMap.SimpleImmutableEntry<String, Long> selectedEntry, String selectedLabel);
        public void onRestoreCancelled();
    }

    private ArrayList<AbstractMap.SimpleImmutableEntry<String, Long>> entries;
    private final List<String> items;
    private NoticeListDialogListener listener;

    public RestoreListDialog(ArrayList<AbstractMap.SimpleImmutableEntry<String, Long>> backupList) {
        this.entries = backupList;
        this.items = backupList.stream().map(entry->entry.getKey()+" ("+entry.getValue()+" bytes)").collect(Collectors.toList());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (NoticeListDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.getClass().toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose backup to restore")
                .setItems(items.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onListItemSelected(entries.get(which),items.get(which));
                    }
                });
        builder.setOnCancelListener((dialogInterface)->listener.onRestoreCancelled());
        return builder.create();
    }
}
