package hu.elte.sbzbxr.phoneconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import hu.elte.sbzbxr.phoneconnect.model.ConnectionManager;

public class MainActivity extends AppCompatActivity {
    private EditText ipEditText;
    private EditText portEditText;
    private Button connectButton;
    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionManager = new ConnectionManager();

        ipEditText = (EditText) findViewById(R.id.ipAddress);
        portEditText = (EditText) findViewById(R.id.portLabel);
        connectButton = (Button) findViewById(R.id.connectButton);

        //ipLabel.setText("Hello");

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipEditText.getText().toString();
                int port = Integer.parseInt(portEditText.getText().toString());

                if(connectionManager.connect(ip,port)){
                    ipEditText.setVisibility(View.INVISIBLE);
                    portEditText.setText("Connected");
                }else{
                    System.err.println("Could not establish the connection!");
                }


            }
        });
    }
}