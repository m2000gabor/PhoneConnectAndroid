package hu.elte.sbzbxr.phoneconnect.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import hu.elte.sbzbxr.phoneconnect.databinding.FragmentConnectedBinding;
import hu.elte.sbzbxr.phoneconnect.model.MyUriQuery;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FrameType;

public class ConnectedFragment extends Fragment {
    private static final String TAG = ToConnectFragment.class.getName();
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQUEST_FILE_PICKER = 2;
    private FragmentConnectedBinding binding;
    private MainActivityCallback activityCallback;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
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
            Uri uri = null;
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
        binding.sendFilesButton.setText("Send files");
        binding.sendFilesButton.setOnClickListener(v ->{
                    showFilePickerDialog();
                }
        );

        binding.pingButton.setOnClickListener(v -> activityCallback.getServiceController().sendPing());

        binding.saveMediaActionButton.setOnClickListener(v -> onBackupMediaClicked());
        binding.restoreMediaActionButton.setOnClickListener(v -> restoreMedia());


        //Setup processes
        binding.includedScreenSharePanel.screenShareSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    startStreamingClicked();
                }else{
                    stopScreenCaptureAndRecord();
                }
            }
        });

        binding.disconnectButton.setOnClickListener(v -> activityCallback.getServiceController().disconnectFromServer());
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
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                confirm.run();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                cancel.run();
            }
            });

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

        new Thread(() ->
                    MyUriQuery.queryDirectory(contentResolver,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,MediaStore.Images.Media._ID).
                    stream().
                    limit(2).
                    forEach(myFileDescriptor -> activityCallback.getServiceController().sendBackupFile(myFileDescriptor,backupID))
                ).start();
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
                "This process may take hours to complete, and tranfers all of your images to you computer. ");
    }

    public void pingSuccessful(String msg) {
        binding.receivedMessageLabel.setText(msg);
    }

    public void availableToRestore(List<String> backupList) {
        if(backupList.isEmpty()){
            Toast.makeText(getContext(),"There's no available backup to restore",Toast.LENGTH_SHORT).show();
        }else{
            showConfirmationDialog(
                    ()->activityCallback.getServiceController().requestRestore(backupList.get(0)),
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
}
