package hu.elte.sbzbxr.phoneconnect.ui.notifications;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import hu.elte.sbzbxr.phoneconnect.R;
import hu.elte.sbzbxr.phoneconnect.model.notification.NotificationPair;

public class NotificationDialog extends DialogFragment {
    private final List<NotificationPair> notificationPairs;
    private final SaveList callbackForSaving;

    public NotificationDialog(List<NotificationPair> notificationPairs,SaveList callbackForSaving) {
        this.notificationPairs = notificationPairs.stream().sorted(Comparator.comparing(a -> a.app)).collect(Collectors.toList());
        this.callbackForSaving = callbackForSaving;
    }

    private void saveList(){
        callbackForSaving.saveNotificationPairs(notificationPairs);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View customView = inflater.inflate(R.layout.notification_settings,null);
        ListView listView = (ListView) customView.findViewById(R.id.listView);

        MyListAdapter adapter = new MyListAdapter(requireActivity(),notificationPairs);
        listView.setAdapter(adapter);

        ((Button) customView.findViewById(R.id.selectAllButton)).setOnClickListener((v)->{
            notificationPairs.forEach(pair -> pair.enabled=true);
            adapter.notifyDataSetChanged();
        });

        ((Button) customView.findViewById(R.id.unselectAllButton)).setOnClickListener((v)->{
            notificationPairs.forEach(pair -> pair.enabled=false);
            adapter.notifyDataSetChanged();
        });

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(customView)
                // Add action buttons
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        saveList();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NotificationDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

}
