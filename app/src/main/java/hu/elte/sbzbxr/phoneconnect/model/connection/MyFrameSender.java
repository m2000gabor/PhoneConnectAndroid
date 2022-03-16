package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.items.NetworkFrame;

public class MyFrameSender {
    private static final String LOG_TAG = "MyFrameSender";

    private MyFrameSender() {}

    public static void send(PrintStream out, NetworkFrame networkFrame) {
        try {
            InputStream inputStream = networkFrame.getData();
            int b = inputStream.read();
            int written = 0;
            while (b>=0){
                out.write(b);
                written++;
                b = inputStream.read();
            }
            out.flush();
            inputStream.close();
            Log.i(LOG_TAG, networkFrame.name+" ( "+networkFrame.type.toString()+", "+ written+" bytes) successfully sent.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(LOG_TAG,"FileNotFound");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
