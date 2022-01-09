package hu.elte.sbzbxr.phoneconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import hu.elte.sbzbxr.phoneconnect.model.ConnectionManager;

public class MainActivity extends AppCompatActivity {
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

        /**
         * @apiNote
         * When the connect button is clicked, check the input,
         * and ask the ConnectionManager to establish the connection.
         */
        mainActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipEditText.getText().toString();
                int port = -1;
                try {
                    port = Integer.parseInt(portEditText.getText().toString());
                }catch (NumberFormatException e){
                    Toast.makeText(getApplicationContext(), "Entered port is not a number", Toast.LENGTH_SHORT).show();
                    System.err.println("Entered port is not a number");
                }

                connectionManager.connect(ip, port);




            }
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
        mainActionButton.setOnClickListener(v -> connectionManager.startStreaming());

        secondaryActionButton1.setVisibility(View.VISIBLE);
        secondaryActionButton1.setOnClickListener(v -> {
            connectionManager.ping();

        });
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
}