package hu.elte.sbzbxr.phoneconnect.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import hu.elte.sbzbxr.phoneconnect.databinding.FragmentToConnectBinding;

public class ToConnectFragment extends Fragment {
    private static final String TAG = ToConnectFragment.class.getName();
    private static final int REQUEST_QR_READING = 3;
    private FragmentToConnectBinding binding;
    private MainActivityCallback activityCallback;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentToConnectBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showDisconnectedUI();
        /*
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ToConnectFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== REQUEST_QR_READING && resultCode==Activity.RESULT_OK){
            //qr
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result != null) {
                if(result.getContents() == null) {
                    Log.e("Scan", "Cancelled scan");
                } else {
                    Log.v("Scan", "Scanned: "+result.getContents());
                    String[] scanned = result.getContents().split(":");
                    fillEditTexts(scanned[0],scanned[1]);
                    binding.connectButton.callOnClick();
                }
            }
        }
    }

    private void prefillEditTexts(){
        fillEditTexts("192.168.0.134","5000");//bdh
        //illEditTexts("192.168.0.164","5000");//home
    }

    private void fillEditTexts(String ip,String port){
        binding.ipAddr.setText(ip);
        binding.portLabel.setText(port);
    }


    public void showDisconnectedUI(){
        prefillEditTexts();

        /*
        connectedToLabel.setText("Not connected");
        connectedToLabel.setVisibility(View.INVISIBLE);

        receivedMessageLabel.setText("No message");
        receivedMessageLabel.setVisibility(View.INVISIBLE);*/

        //update buttons
        binding.connectButton.setText("Connect");
        binding.connectButton.setOnClickListener(v -> {
            String ip = binding.ipAddr.getText().toString();
            int port = -1;
            try {
                port = Integer.parseInt(binding.portLabel.getText().toString());
            }catch (NumberFormatException e){
                Toast.makeText(getContext(), "Entered port is not a number", Toast.LENGTH_SHORT).show();
                System.err.println("Entered port is not a number");
            }
            if(!activityCallback.getServiceController().connectToServer(ip,port)){
                Toast.makeText(getContext(), "Invalid ip or port", Toast.LENGTH_SHORT).show();
                System.err.println("Invalid ip or port");
            }
        });

        binding.readQrButton.setText("Scan QR");
        binding.readQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //serviceController.startNotificationListening();
                //Log.e(getLocalClassName(),"Unimplemented feature");
                startQrReaderActivity();
            }
        });
    }


    public void startQrReaderActivity(){
        //From: https://stackoverflow.com/questions/8831050/android-how-to-read-qr-code-in-my-application
        IntentIntegrator integrator = new IntentIntegrator(ToConnectFragment.this.getActivity());
        integrator.setCaptureActivity(QrReaderActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }
}
