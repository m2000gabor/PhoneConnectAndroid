package hu.elte.sbzbxr.phoneconnect.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;

import hu.elte.sbzbxr.phoneconnect.R;
import hu.elte.sbzbxr.phoneconnect.controller.ScreenCaptureBuilder;
import hu.elte.sbzbxr.phoneconnect.controller.ServiceController;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "ScreenCaptureFragment";

    static final int REQUEST_MEDIA_PROJECTION = 1;
    //private ScreenCaptureBuilder screenCaptureBuilder =null;
    private ServiceController serviceController;

    private EditText ipEditText;
    private EditText portEditText;
    private TextView connectedToLabel;
    private TextView receivedMessageLabel;
    private Button mainActionButton;
    private Button secondaryActionButton1;
    private Button secondaryActionButton2;

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
        //mSurfaceView =(SurfaceView) findViewById(R.id.surface);


        /**
         * @apiNote
         * When the connect button is clicked, check the input,
         * and ask the ConnectionManager to establish the connection.
         */
        mainActionButton.setOnClickListener(v -> {
            String ip = ipEditText.getText().toString();
            int port = -1;
            try {
                port = Integer.parseInt(portEditText.getText().toString());
            }catch (NumberFormatException e){
                Toast.makeText(getApplicationContext(), "Entered port is not a number", Toast.LENGTH_SHORT).show();
                System.err.println("Entered port is not a number");
            }
            serviceController.connectToServer(ip,port);
        });

        prefillEditTexts();
    }

    private void prefillEditTexts(){
        ipEditText.setText("192.168.0.134");
        portEditText.setText("5000");
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

        secondaryActionButton1.setVisibility(View.VISIBLE);
        secondaryActionButton1.setOnClickListener(v -> serviceController.sendPing());

        secondaryActionButton2.setVisibility(View.VISIBLE);
        secondaryActionButton2.setOnClickListener(v ->{
            serviceController.sendOneSegment();
            }
        );
    }

    public void successfulPing(String msg){
        /**
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
        ipEditText.setVisibility(View.VISIBLE);
        portEditText.setVisibility(View.VISIBLE);
        connectedToLabel.setText("Not connected");
        connectedToLabel.setVisibility(View.INVISIBLE);
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
            serviceController.connectToServer(ip,port);
        });

        secondaryActionButton1.setVisibility(View.INVISIBLE);
        secondaryActionButton1.setOnClickListener(null);

        secondaryActionButton2.setVisibility(View.INVISIBLE);
        secondaryActionButton2.setOnClickListener(null);
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
        super.onStop();
    }

}