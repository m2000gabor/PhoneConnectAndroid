package hu.elte.sbzbxr.phoneconnect.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hu.elte.sbzbxr.phoneconnect.controller.MainViewModel;
import hu.elte.sbzbxr.phoneconnect.databinding.FragmentConnectedBinding;
import hu.elte.sbzbxr.phoneconnect.model.ActionObserver;
import hu.elte.sbzbxr.phoneconnect.model.persistance.MyFileDescriptor;
import hu.elte.sbzbxr.phoneconnect.model.persistance.MyPreferenceManager;
import hu.elte.sbzbxr.phoneconnect.model.persistance.MyUriQuery;
import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;
import hu.elte.sbzbxr.phoneconnect.model.actions.arrived.Action_FilePieceArrived;
import hu.elte.sbzbxr.phoneconnect.model.actions.arrived.Action_LastPieceOfFileArrived;
import hu.elte.sbzbxr.phoneconnect.model.actions.arrived.Action_PingArrived;
import hu.elte.sbzbxr.phoneconnect.model.actions.arrived.Action_RestoreListAvailable;
import hu.elte.sbzbxr.phoneconnect.model.actions.sent.Action_FilePieceSent;
import hu.elte.sbzbxr.phoneconnect.model.actions.sent.Action_LastPieceOfFileSent;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionLimiter;
import hu.elte.sbzbxr.phoneconnect.ui.notifications.NotificationSettings;
import hu.elte.sbzbxr.phoneconnect.ui.progress.FileTransferQueueDialog;
import hu.elte.sbzbxr.phoneconnect.ui.progress.FileTransferUI;

public class ConnectedFragment extends Fragment {
    private static final String TAG = ToConnectFragment.class.getName();
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQUEST_FILE_PICKER = 2;
    private FragmentConnectedBinding binding;
    private MainActivityCallback activityCallback;
    private FileTransferUI incomingFileTransferUi;
    private FileTransferUI outgoingFileTransferUi;
    private MainViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        binding = FragmentConnectedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            activityCallback = (MainActivityCallback) context;
            viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
            viewModel.refreshData(activityCallback.getServiceController());
        } catch (ClassCastException castException) {
            Log.e(TAG, castException.getMessage());
        }
    }

    private final ActionObserver actionObserver = new ActionObserver() {
        @Override
        public void arrived(NetworkAction networkAction) {
            switch (networkAction.type){
                case PING_ARRIVED:
                    pingSuccessful(((Action_PingArrived) networkAction).getField());
                    break;
                case PIECE_OF_FILE_ARRIVED:
                    viewModel.getIncomingFileTransferSummary().pieceOfFile(((Action_FilePieceArrived) networkAction).getField());
                    incomingFileTransferUi.refresh(viewModel.getIncomingFileTransferSummary().getActiveTransfers());
                    break;
                case LAST_PIECE_OF_FILE_ARRIVED:
                    viewModel.getIncomingFileTransferSummary().endOfFile(((Action_LastPieceOfFileArrived) networkAction).getField());
                    incomingFileTransferUi.refresh(viewModel.getIncomingFileTransferSummary().getActiveTransfers());
                    break;
                case RESTORE_LIST_OF_AVAILABLE_BACKUPS:
                    availableToRestore(((Action_RestoreListAvailable) networkAction).getField());
                    break;
                case PIECE_OF_FILE_SENT:
                    viewModel.getOutgoingFileTransferSummary().pieceOfFile(((Action_FilePieceSent) networkAction).getField());
                    outgoingFileTransferUi.refresh(viewModel.getOutgoingFileTransferSummary().getActiveTransfers());
                    break;
                case LAST_PIECE_OF_FILE_SENT:
                    viewModel.getOutgoingFileTransferSummary().endOfFile(((Action_LastPieceOfFileSent) networkAction).getField());
                    outgoingFileTransferUi.refresh(viewModel.getOutgoingFileTransferSummary().getActiveTransfers());
                    break;
            }
        }
    };

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        incomingFileTransferUi =new FileTransferUI(this,binding.includedFileArrivingPanel, false);
        outgoingFileTransferUi =new FileTransferUI(this,binding.includedFileSendingPanel, true);

        viewModel.getActions().register(actionObserver);
        viewModel.getUiData().observe(getViewLifecycleOwner(), new Observer<ConnectedFragmentUIData>() {
            @Override
            public void onChanged(ConnectedFragmentUIData connectedFragmentUIData) {
                showConnectedUI(connectedFragmentUIData);
            }
        });
    }

    @Override
    public void onDestroyView() {
        viewModel.getActions().unregister(actionObserver);
        binding = null;
        super.onDestroyView();
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


    public void showConnectedUI(ConnectedFragmentUIData data){
        if(data.getIp()!=null && data.getPort()!=null){
            binding.connectedToLabel.setText(String.format("Connected to: %s:%s", data.getIp(),data.getPort()));
        }
        //setup buttons
        binding.sendFilesButton.setOnClickListener(v -> showFilePickerDialog());
        binding.pingButton.setOnClickListener(v -> activityCallback.getServiceController().sendPing());
        binding.saveMediaActionButton.setOnClickListener(v -> onBackupMediaClicked());
        binding.restoreMediaActionButton.setOnClickListener(v -> onRestoreMediaClicked());
        binding.disconnectButton.setOnClickListener(v -> activityCallback.getServiceController().disconnectFromServer());

        //Setup processes
        binding.includedScreenSharePanel.screenShareSwitch.setChecked(data.isStreaming());
        binding.includedScreenSharePanel.screenShareSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                startStreamingClicked(binding.includedScreenSharePanel.demoSourceCheckBox.isChecked());
            }else{
                stopScreenCaptureAndRecord();
            }
        });
        binding.includedScreenSharePanel.slowerNetworkCheckBox.setChecked(data.isLimited());
        binding.includedScreenSharePanel.slowerNetworkCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ConnectionLimiter limiter;
                if(isChecked){
                    limiter=ConnectionLimiter.create(getNetworkLimit());
                }else{
                    limiter=ConnectionLimiter.noLimit();
                }
                activityCallback.getServiceController().setNetworkLimit(limiter);
            }
        });
        binding.includedScreenSharePanel.demoSourceCheckBox.setChecked(data.isDemo());
        binding.includedScreenSharePanel.demoSourceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSource(isChecked);
            }
        });

        binding.includedNotificationPanel.notificationSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationSettings notificationSettings = new NotificationSettings(ConnectedFragment.this);
                new Thread(notificationSettings::showDialog).start();
            }
        });

        binding.includedNotificationPanel.notificationSwitch.setChecked(data.isNotificationForwarded());
        binding.includedNotificationPanel.notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                startNotificationService();
            }else{
                stopNotificationService();
            }
        });

        binding.includedFileSendingPanel.filesSendingLayoutHome.setVisibility(View.GONE);
        binding.includedFileSendingPanel.progressBar.setMax(100);
        binding.includedFileSendingPanel.sendOrArriveLabel.setText("Sending:");
        //binding.includedFileSendingPanel.stopButton.setOnClickListener((view)->stopSending());
        binding.includedFileArrivingPanel.showAllFileTransferButton.setOnClickListener(v->showAllOutgoingTransfer());

        binding.includedFileArrivingPanel.filesSendingLayoutHome.setVisibility(View.GONE);
        binding.includedFileArrivingPanel.progressBar.setMax(100);
        binding.includedFileArrivingPanel.sendOrArriveLabel.setText("Saving:");
        // binding.includedFileArrivingPanel.stopButton.setOnClickListener((view)->stopSaving());
        binding.includedFileArrivingPanel.showAllFileTransferButton.setOnClickListener(v->showAllIncomingTransfer());
    }

    public void showAllIncomingTransfer(){
        FileTransferQueueDialog newFragment = new FileTransferQueueDialog(viewModel.getIncomingFileTransferSummary().getActiveTransfers(),false);
        newFragment.show(getChildFragmentManager(), "incomingFiles");
    }
    public void showAllOutgoingTransfer(){
        FileTransferQueueDialog newFragment = new FileTransferQueueDialog(viewModel.getOutgoingFileTransferSummary().getActiveTransfers(),true);
        newFragment.show(getChildFragmentManager(), "outgoingFiles");
    }

    private void setSource(boolean isDemo){

    }

    private long getNetworkLimit(){
        return MyPreferenceManager.getNetworkSpeedLimit(getContext());
    }

    private void stopSending(){

    }

    private void stopSaving(){

    }

    public void pingSuccessful(String msg) {
        Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show();
    }


    //send file functionality
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
    private void startStreamingClicked(boolean checked){
        if(checked){
            startDemoCaptureAndRecord();
        }else{
            requestScreenCapturePermission();
        }

    }

    private void requestScreenCapturePermission(){
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) requireActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }

    private void startScreenCaptureAndRecord(int resultCode, Intent data){
        activityCallback.startScreenCapture(resultCode,data);
    }
    private void startDemoCaptureAndRecord(){
        activityCallback.startDemoCapture();
    }

    private void stopScreenCaptureAndRecord(){
        activityCallback.getServiceController().stopScreenCapture();
    }

    private void startNotificationService(){
        activityCallback.startNotificationListening();
    }

    private void stopNotificationService(){
        activityCallback.stopNotificationListening();
    }

    //media actions
    private void onBackupMediaClicked(){
        //chooseDirectory();
        requestAccess();
    }

    private void onRestoreMediaClicked(){ activityCallback.getServiceController().askRestoreList(); }

    private final ActivityResultLauncher<Intent> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<androidx.activity.result.ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Uri uri;
                    Intent data = result.getData();
                    if (data != null) {
                        uri = data.getData();
                        mediaBackupAccessGranted(uri);
                    }
                }
            });
    private void requestAccess(){
        // You can directly ask for the permission.
        // The registered ActivityResultCallback gets the result of this request.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        requestPermissionLauncher.launch(intent);
    }

    private void mediaBackupAccessGranted(Uri uri){
        showConfirmationDialog(()->backupData(uri),()-> System.err.println("User cancelled"),
                "This process may take a long time to complete, and transfers all of your files located in the selected folder to your computer.");
    }

    private void backupData(Uri uri){
        DocumentFile dfile = DocumentFile.fromTreeUri(requireContext(), uri);
        if(dfile==null) return;
        String backupID = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime())
                .replace(':','_').replace(' ','_');
        DocumentFile[] fileList = getFilesFromDir(dfile);
        long totalSize = Arrays.stream(fileList).map(DocumentFile::length).reduce(0L, Long::sum);
        List<MyFileDescriptor> fileDescriptorList = new ArrayList<>(fileList.length);
        for(DocumentFile documentFile : fileList){
            fileDescriptorList.add(MyUriQuery.querySingleFile(documentFile.getUri(),requireActivity().getApplicationContext()));
        }
        activityCallback.getServiceController().sendBackupFiles(fileDescriptorList,backupID,totalSize);
    }

    private DocumentFile[] getFilesFromDir(DocumentFile parent){
        if(parent==null || !parent.exists()) return new DocumentFile[0];
        if(parent.isDirectory()){
            DocumentFile[] documentFiles = parent.listFiles();
            ArrayList<DocumentFile> ret = new ArrayList<>();
            for(DocumentFile d: documentFiles){
                ret.addAll(Arrays.asList(getFilesFromDir(d)));
            }
            return ret.toArray(new DocumentFile[0]);
        }else{
            return new DocumentFile[]{parent};
        }
    }

    //restore
    private void availableToRestore(ArrayList<AbstractMap.SimpleImmutableEntry<String, Long>> backupList) {
        if(backupList.isEmpty()){
            Toast.makeText(getContext(),"There's no available backup to restore",Toast.LENGTH_SHORT).show();
        }else {
            RestoreListDialog newFragment = new RestoreListDialog(backupList);
            newFragment.show(getChildFragmentManager(), "backupListChooser");
        }
    }

    //etc
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

    public MainViewModel getViewModel() {
        return viewModel;
    }
}
