package hu.elte.sbzbxr.phoneconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import hu.elte.sbzbxr.phoneconnect.model.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.model.ScreenCapture;
import hu.elte.sbzbxr.phoneconnect.ui.ScreenCaptureCallbacks;

public class MainActivity extends AppCompatActivity implements ScreenCaptureCallbacks {
    static final String TAG = "ScreenCaptureFragment";

    static final String STATE_RESULT_CODE = "result_code";
    static final String STATE_RESULT_DATA = "result_data";

    static final int REQUEST_MEDIA_PROJECTION = 1;
    private ScreenCapture screenCapture=null;
    private SurfaceView mSurfaceView;

    private EditText ipEditText;
    private EditText portEditText;
    private TextView connectedToLabel;
    private TextView receivedMessageLabel;
    private Button mainActionButton;
    private Button secondaryActionButton1;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionManager = new ConnectionManager(this);

        ipEditText = (EditText) findViewById(R.id.ipAddr);
        portEditText = (EditText) findViewById(R.id.portLabel);
        connectedToLabel = (TextView) findViewById(R.id.connectedTo);
        receivedMessageLabel = (TextView) findViewById(R.id.receivedMessageLabel);
        mainActionButton = (Button) findViewById(R.id.mainActionButton);
        secondaryActionButton1 = (Button) findViewById(R.id.secondaryActionButton1);
        mSurfaceView =(SurfaceView) findViewById(R.id.surface);

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

            connectionManager.connect(ip, port);




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
        secondaryActionButton1.setOnClickListener(v -> {
            connectionManager.ping();

        });
    }

    private void startStreamingClicked(){
        //connectionManager.startStreaming();
        screenCapture = new ScreenCapture();
        screenCapture.beforeUserRequest(this);
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
            connectionManager.connect(ip, port);
        });

        secondaryActionButton1.setVisibility(View.INVISIBLE);
        secondaryActionButton1.setOnClickListener(null);
    }

    @Override
    public void screenCaptureStarted() {
        mainActionButton.setText("Stop capture");
        mainActionButton.setOnClickListener(v -> {
            //screenCapture.stopScreenCapture();
            screenCapture.stopSelf();
        });
    }

    @Override
    public void screenCaptureFinished() {
        Toast.makeText(getApplicationContext(), "Screen capture finished", Toast.LENGTH_SHORT).show();
        mainActionButton.setText("Start capture");
        mainActionButton.setOnClickListener(v ->{screenCapture.beforeUserRequest(this);});
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void showPermissionRequest(MediaProjectionManager mMediaProjectionManager) {
        startActivityForResult(
                mMediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Log.i(TAG, "User cancelled");
                Toast.makeText(getActivity(), "User cancelled", Toast.LENGTH_SHORT).show();
                return;
            }
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            Log.i(TAG, "Starting screen capture");
            screenCapture.mResultCode = resultCode;
            screenCapture.mResultData = data;
            startScreenCapture();
        }
    }

    private void startScreenCapture(){
        Context context = getActivity().getApplicationContext();
        Intent intent = new Intent(this.getActivity(),ScreenCapture.class); // Build the intent for the service
        context.startForegroundService(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        screenCapture.stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        screenCapture.stopSelf();
        //screenCapture.tearDownMediaProjection();
    }

    @Override
    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }
}