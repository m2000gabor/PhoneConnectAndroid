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
    private Button mainActionButton;
    private Button secondaryActionButton1;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionManager = new ConnectionManager();

        ipEditText = (EditText) findViewById(R.id.ipAddr);
        portEditText = (EditText) findViewById(R.id.portLabel);
        connectedToLabel = (TextView) findViewById(R.id.connectedTo);
        mainActionButton = (Button) findViewById(R.id.mainActionButton);
        secondaryActionButton1 = (Button) findViewById(R.id.secondaryActionButton1);

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


                if(connectionManager.connect(ip,port)){
                    showConnectedUI(ip,port);
                }else{
                    Toast.makeText(getApplicationContext(), "Could not establish the connection!", Toast.LENGTH_SHORT).show();
                    System.err.println("Could not establish the connection!");
                }


            }
        });
    }

    private void showConnectedUI(String ip, int port){
        ipEditText.setVisibility(View.INVISIBLE);
        portEditText.setVisibility(View.INVISIBLE);
        connectedToLabel.setText("Connected to: "+ip+":"+ Integer.toString(port));
        connectedToLabel.setVisibility(View.VISIBLE);

        //update buttons
        mainActionButton.setText("Start Stream");
        mainActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionManager.startStreaming();
            }
        });

        secondaryActionButton1.setVisibility(View.VISIBLE);
        secondaryActionButton1.setOnClickListener(v -> {
            boolean pingResult= connectionManager.ping();
            String msg = pingResult ? "Ping was successful" : "Ping was NOT successful";

            //From: https://developer.android.com/guide/topics/ui/notifiers/toasts#java
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }
}