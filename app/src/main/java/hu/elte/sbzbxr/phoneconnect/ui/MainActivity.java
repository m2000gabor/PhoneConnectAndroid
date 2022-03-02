package hu.elte.sbzbxr.phoneconnect.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.concurrent.Executors;

import hu.elte.sbzbxr.phoneconnect.R;
import hu.elte.sbzbxr.phoneconnect.controller.ServiceController;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ScreenCaptureFragment";
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQUEST_FILE_PICKER = 2;

    private ServiceController serviceController;

    private EditText ipEditText;
    private EditText portEditText;
    private TextView connectedToLabel;
    private TextView receivedMessageLabel;
    private Button mainActionButton;
    private Button secondaryActionButton1;
    private Button secondaryActionButton2;
    private Button secondaryActionButton3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceController = new ServiceController(this);

        ipEditText = (EditText) findViewById(R.id.ipAddr);
        portEditText = (EditText) findViewById(R.id.portLabel);
        connectedToLabel = (TextView) findViewById(R.id.connectedTo);
        receivedMessageLabel = (TextView) findViewById(R.id.receivedMessageLabel);
        mainActionButton = (Button) findViewById(R.id.mainActionButton);
        secondaryActionButton1 = (Button) findViewById(R.id.secondaryActionButton1);
        secondaryActionButton2 = (Button) findViewById(R.id.secondaryActionButton2);
        secondaryActionButton3 = (Button) findViewById(R.id.secondaryActionButton3);

        showDisconnectedUI();
    }

    private void prefillEditTexts(){
        fillEditTexts("192.168.0.134","5000");//bdh
        //illEditTexts("192.168.0.164","5000");//home
    }

    private void fillEditTexts(String ip,String port){
        ipEditText.setText(ip);
        portEditText.setText(port);
    }

    /**
     * Shows a Toast and writes to serr
     * @param msg Message to show
     */
    public void showFailMessage(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        System.err.println(msg);
    }


    public void showConnectedUI(String ip, int port){
        ipEditText.setVisibility(View.INVISIBLE);
        portEditText.setVisibility(View.INVISIBLE);
        connectedToLabel.setText(String.format("Connected to: %s:%s", ip, port));
        connectedToLabel.setVisibility(View.VISIBLE);
        receivedMessageLabel.setVisibility(View.VISIBLE);

        //update buttons
        mainActionButton.setText("Start Stream");
        mainActionButton.setOnClickListener(v -> startStreamingClicked());

        secondaryActionButton1.setText("Ping");
        secondaryActionButton1.setOnClickListener(v -> serviceController.sendPing());

        secondaryActionButton2.setText("Disconnect");
        secondaryActionButton2.setVisibility(View.VISIBLE);
        secondaryActionButton2.setOnClickListener(v ->{
            serviceController.disconnectFromServer();
            this.afterDisconnect();
            }
        );

        secondaryActionButton3.setText("Send files");
        secondaryActionButton3.setVisibility(View.VISIBLE);
        secondaryActionButton3.setOnClickListener(v ->{
                    showFilePickerDialog();
                }
        );
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

    public void showDisconnectedUI(){
        ipEditText.setVisibility(View.VISIBLE);
        portEditText.setVisibility(View.VISIBLE);
        prefillEditTexts();

        connectedToLabel.setText("Not connected");
        connectedToLabel.setVisibility(View.INVISIBLE);

        receivedMessageLabel.setText("No message");
        receivedMessageLabel.setVisibility(View.INVISIBLE);

        //update buttons
        mainActionButton.setText("Connect");
        mainActionButton.setOnClickListener(v -> {
            String ip = ipEditText.getText().toString();
            int port = -1;
            try {
                port = Integer.parseInt(portEditText.getText().toString());
            }catch (NumberFormatException e){
                Toast.makeText(getApplicationContext(), "Entered port is not a number", Toast.LENGTH_SHORT).show();
                System.err.println("Entered port is not a number");
            }
            if(!serviceController.connectToServer(ip,port)){
                Toast.makeText(getApplicationContext(), "Invalid ip or port", Toast.LENGTH_SHORT).show();
                System.err.println("Invalid ip or port");
            }
        });

        secondaryActionButton1.setVisibility(View.VISIBLE);
        secondaryActionButton1.setText("Scan QR");
        secondaryActionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //serviceController.startNotificationListening();
                //Log.e(getLocalClassName(),"Unimplemented feature");
                startQrReaderActivity();
            }
        });

        secondaryActionButton2.setVisibility(View.INVISIBLE);
        secondaryActionButton2.setOnClickListener(null);

        secondaryActionButton3.setVisibility(View.INVISIBLE);
        secondaryActionButton3.setOnClickListener(null);
    }

    public void startQrReaderActivity(){
        //From: https://stackoverflow.com/questions/8831050/android-how-to-read-qr-code-in-my-application
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.setCaptureActivity(QrReaderActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    public void successfulPing(String msg){
        /*
         * For Toasts:
         * @see https://developer.android.com/guide/topics/ui/notifiers/toasts#java
         */
        Toast.makeText(getApplicationContext(), "Ping was successful", Toast.LENGTH_SHORT).show();
        showReceivedMsgFromServer(msg);
    }

    private void showReceivedMsgFromServer(String msg) {
        receivedMessageLabel.setText(msg);
        receivedMessageLabel.setVisibility(View.VISIBLE);
    }

    public void afterDisconnect(){
        showDisconnectedUI();
    }


    //For screenRecording

    private void startStreamingClicked(){
        requestScreenCapturePermission();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Log.i(TAG, "User cancelled");
                Toast.makeText(this, "User cancelled", Toast.LENGTH_SHORT).show();
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
                serviceController.sendFile(uri);
            }
        }else{
            //qr
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result != null) {
                if(result.getContents() == null) {
                    Log.e("Scan", "Cancelled scan");
                } else {
                    Log.v("Scan", "Scanned: "+result.getContents());
                    String[] scanned = result.getContents().split(":");
                    fillEditTexts(scanned[0],scanned[1]);
                    mainActionButton.callOnClick();
                }
            }
        }
    }


    private void startScreenCaptureAndRecord(int resultCode, Intent data){
        serviceController.startScreenCapture(resultCode,data);
        screenCaptureStarted();
    }

    private void screenCaptureStarted(){
        mainActionButton.setText("Stop recording");
        mainActionButton.setOnClickListener(v -> stopScreenCaptureAndRecord());
    }

    private void stopScreenCaptureAndRecord(){
        serviceController.stopScreenCapture();
        mainActionButton.setText("Start recording");
        mainActionButton.setOnClickListener(v -> startStreamingClicked());
    }

    private void requestScreenCapturePermission(){
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) this.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onStart() {
        super.onStart();
        serviceController.activityBindToConnectionManager();
    }

    @Override
    protected void onStop() {
        serviceController.activityUnbindFromConnectionManager();
        //serviceController.stopNotificationListening();
        super.onStop();
    }
}