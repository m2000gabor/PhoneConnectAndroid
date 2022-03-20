package hu.elte.sbzbxr.phoneconnect.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import hu.elte.sbzbxr.phoneconnect.databinding.FragmentConnectedBinding;
import hu.elte.sbzbxr.phoneconnect.model.MyFileDescriptor;
import hu.elte.sbzbxr.phoneconnect.model.MyUriQuery;
import hu.elte.sbzbxr.phoneconnect.ui.notifications.NotificationSettings;
import hu.elte.sbzbxr.phoneconnect.ui.progress.FileTransferUI;

public class ConnectedFragment extends Fragment {
    private static final String TAG = ToConnectFragment.class.getName();
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQUEST_FILE_PICKER = 2;
    private FragmentConnectedBinding binding;
    private MainActivityCallback activityCallback;
    private FileTransferUI arrivingFileTransfer;
    private FileTransferUI sendingFileTransfer;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentConnectedBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            activityCallback = (MainActivityCallback) context;
        } catch (ClassCastException castException) {
            Log.e(TAG, castException.getMessage());
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle!=null){
            String ip = bundle.getString(MainActivity.IP_ADDRESS);
            String port = bundle.getString(MainActivity.PORT);
            showConnectedUI(ip,port);
        }else{
            showConnectedUI(null,null);
        }
        arrivingFileTransfer=new FileTransferUI(this,binding.includedFileArrivingPanel);
        sendingFileTransfer=new FileTransferUI(this,binding.includedFileSendingPanel);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Log.i(TAG, "User cancelled");
                Toast.makeText(getContext(), "User cancelled", Toast.LENGTH_SHORT).show();
                binding.includedScreenSharePanel.screenShareSwitch.setChecked(false);
                return;
            }

            Log.i(TAG, "Starting screen capture");
            startScreenCaptureAndRecord(resultCode,data);
        }else if(requestCode == REQUEST_FILE_PICKER && resultCode==Activity.RESULT_OK){
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri;
            if (data != null) {
                uri = data.getData();
                // Perform operations on the document using its URI.
                activityCallback.getServiceController().startFileTransfer(MyUriQuery.querySingleFile(uri,requireContext()));
            }
        }
    }


    public void showConnectedUI(@Nullable String ip, @Nullable String port){
        if(ip != null && port !=null){
            binding.connectedToLabel.setText(String.format("Connected to: %s:%s", ip, port));
        }

        //setup buttons
        binding.sendFilesButton.setOnClickListener(v -> showFilePickerDialog());
        binding.pingButton.setOnClickListener(v -> activityCallback.getServiceController().sendPing());
        binding.saveMediaActionButton.setOnClickListener(v -> onBackupMediaClicked());
        binding.restoreMediaActionButton.setOnClickListener(v -> restoreMedia());
        binding.disconnectButton.setOnClickListener(v -> activityCallback.getServiceController().disconnectFromServer());

        //Setup processes
        binding.includedScreenSharePanel.screenShareSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                startStreamingClicked();
            }else{
                stopScreenCaptureAndRecord();
            }
        });

        binding.includedNotificationPanel.notificationSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationSettings notificationSettings = new NotificationSettings(ConnectedFragment.this);
                notificationSettings.showDialog();
            }
        });

        binding.includedFileSendingPanel.filesSendingLayoutHome.setVisibility(View.GONE);
        binding.includedFileSendingPanel.progressBar.setMax(100);
        binding.includedFileSendingPanel.sendOrArriveLabel.setText("Sending:");
        binding.includedFileSendingPanel.stopButton.setOnClickListener((view)->stopSending());

        binding.includedFileArrivingPanel.filesSendingLayoutHome.setVisibility(View.GONE);
        binding.includedFileArrivingPanel.progressBar.setMax(100);
        binding.includedFileArrivingPanel.sendOrArriveLabel.setText("Saving:");
        binding.includedFileArrivingPanel.stopButton.setOnClickListener((view)->stopSaving());
    }

    private void stopSending(){

    }

    private void stopSaving(){

    }

    public void showFilePickerDialog(){
        //From: https://developer.android.com/training/data-storage/shared/documents-files
        // Request code for selecting a PDF document.
        //Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, REQUEST_FILE_PICKER);
    }


    //For screenRecording
    private void startStreamingClicked(){
        requestScreenCapturePermission();
    }

    private void requestScreenCapturePermission(){
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) requireActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }

    private void startScreenCaptureAndRecord(int resultCode, Intent data){
        activityCallback.getServiceController().startScreenCapture(resultCode,data);
    }

    private void stopScreenCaptureAndRecord(){
        activityCallback.getServiceController().stopScreenCapture();
    }

    //media actions
    private void restoreMedia(){ activityCallback.getServiceController().askRestoreList(); }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app.
                    mediaBackupAccessGranted();
                } else {
                    System.err.println("User declined");
                }
            });

    private void onBackupMediaClicked(){
        requestAccess();
    }

    private void showConfirmationDialog(Runnable confirm, Runnable cancel,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Add the buttons
        builder.setPositiveButton("OK", (dialog, id) -> {confirm.run(); });
        builder.setNegativeButton("Cancel", (dialog, id) -> {cancel.run(); });

        //other dialog params
        builder.setMessage(message)
                .setTitle("Are you sure?");

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void backupData(){
        //ContentResolver contentResolver = requireActivity().getContentResolver();
        ContentResolver contentResolver = requireActivity().getApplicationContext().getContentResolver();
        //ContentResolver contentResolver = getContext().getContentResolver();
        String backupID = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime())
                .replace(':','_').replace(' ','_');

        new Thread(() ->{
                List<MyFileDescriptor> files = new ArrayList<>(MyUriQuery.queryDirectory(contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media._ID)).
                        stream().
                        limit(2).
                        collect(Collectors.toList());
                int totalSize=files.stream().map(d->d.size).reduce(0, Integer::sum);
                getSendingFileTransfer().initFolder(backupID, (long) totalSize);
                files.forEach(myFileDescriptor -> activityCallback.getServiceController().sendBackupFile(myFileDescriptor,backupID));
        }).start();
    }

    private void requestAccess(){
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            mediaBackupAccessGranted();
        }  else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void mediaBackupAccessGranted(){
        showConfirmationDialog(this::backupData,()-> System.err.println("User cancelled"),
                "This process may take hours to complete, and transfers all of your images to you computer. ");
    }

    public void pingSuccessful(String msg) {
        Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
    }

    public void availableToRestore(ArrayList<AbstractMap.SimpleImmutableEntry<String, Long>> backupList) {
        if(backupList.isEmpty()){
            Toast.makeText(getContext(),"There's no available backup to restore",Toast.LENGTH_SHORT).show();
        }else{
            showConfirmationDialog(
                    ()->{
                        AbstractMap.SimpleImmutableEntry<String, Long> chosenBackup = backupList.get(backupList.size()-1);
                        activityCallback.getServiceController().requestRestore(chosenBackup.getKey());
                        arrivingFileTransfer.initFolder(chosenBackup.getKey(),chosenBackup.getValue());
                    },
                    ()-> System.err.println("User cancelled"),
                    "This process may take hours to complete, and restore all of your images from your backup to this phone.");
        }
        //todo finish this
        /*
        1. User click on the restore button -> send request, show dialog wth loading screen
        2. Received list of backups -> populate dialog (This function should do this)
        3. User selects the backup to restore -> ask the windows side to do it
         */
    }


    public FileTransferUI getArrivingFileTransfer() {
        return arrivingFileTransfer;
    }

    public FileTransferUI getSendingFileTransfer() {
        return sendingFileTransfer;
    }
}
