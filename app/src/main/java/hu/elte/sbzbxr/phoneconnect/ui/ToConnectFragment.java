package hu.elte.sbzbxr.phoneconnect.ui;

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
import hu.elte.sbzbxr.phoneconnect.model.persistance.MyPreferenceManager;

public class ToConnectFragment extends Fragment {
    private static final String TAG = ToConnectFragment.class.getName();
    private FragmentToConnectBinding binding;
    private MainActivityCallback activityCallback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = FragmentToConnectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showDisconnectedUI();
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
        //qr
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.e("Scan", "Cancelled scan");
            } else {
                Log.v("Scan", "Scanned: "+result.getContents());
                String[] scanned = result.getContents().split(":");
                if(scanned.length!=2){Log.e("Scan","Invalid data");return;}
                fillEditTexts(scanned[0],scanned[1]);
                binding.connectButton.callOnClick();
            }
        }
    }

    private void prefillEditTexts(){
        fillEditTexts(MyPreferenceManager.getAddress(getContext()),MyPreferenceManager.getPort(getContext()));
        //fillEditTexts("192.168.0.134","5000");//bdh
        //fillEditTexts("192.168.0.164","5000");//home
    }

    private void fillEditTexts(String ip,String port){
        binding.ipAddr.setText(ip);
        binding.portLabel.setText(port);
    }


    public void showDisconnectedUI(){
        prefillEditTexts();

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
            activityCallback.connectToServer(ip,port);
        });

        binding.readQrButton.setText("Scan QR");
        binding.readQrButton.setOnClickListener(v -> startQrReaderActivity());
    }

    //Based on: https://stackoverflow.com/questions/8831050/android-how-to-read-qr-code-in-my-application
    public void startQrReaderActivity(){
        //Docs: https://zxing.github.io/zxing/apidocs/com/google/zxing/integration/android/IntentIntegrator.html#IntentIntegrator-android.app.Fragment-
        IntentIntegrator.forSupportFragment(ToConnectFragment.this)
                .setCaptureActivity(QrReaderActivity.class)
                .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                .setPrompt("Scan")
                .setCameraId(0)
                .setBeepEnabled(false)
                .setBarcodeImageEnabled(false)
                .setOrientationLocked(true)
                .initiateScan();
    }
}
