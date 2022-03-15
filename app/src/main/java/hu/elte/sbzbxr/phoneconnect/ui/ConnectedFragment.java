package hu.elte.sbzbxr.phoneconnect.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import hu.elte.sbzbxr.phoneconnect.databinding.FragmentConnectedBinding;

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
        }else if(requestCode== REQUEST_FILE_PICKER && resultCode==Activity.RESULT_OK){
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                // Perform operations on the document using its URI.
                activityCallback.getServiceController().sendFile(uri);
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
}
