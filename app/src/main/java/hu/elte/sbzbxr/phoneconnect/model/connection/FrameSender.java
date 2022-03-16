package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.items.NetworkFrame;

public class FrameSender {
    private static final String LOG_TAG = "MyFrameSender";

    private FrameSender() {}

    public static void send(PrintStream out, NetworkFrame networkFrame) {
        try {
            byte[] toWrite = networkFrame.serialize().getAsBytes();
            out.write(toWrite);
            out.flush();
            Log.i(LOG_TAG, networkFrame.name+" ( "+networkFrame.type.toString()+", "+ toWrite.length+" bytes) successfully sent.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(LOG_TAG,"FileNotFound");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
