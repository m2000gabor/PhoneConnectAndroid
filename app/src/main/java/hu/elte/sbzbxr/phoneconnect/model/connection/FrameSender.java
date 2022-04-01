package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.util.Log;

import java.io.IOException;
import java.io.PrintStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.NetworkFrame;

public class FrameSender {
    private static final String LOG_TAG = "MyFrameSender";

    private FrameSender() {}

    public static void send(ConnectionLimiter limiter, PrintStream out, NetworkFrame networkFrame) {
        byte[] toWrite = networkFrame.serialize().getAsBytes();

        if(limiter.hasLimit()){
            //int i = limiter.canSend();
            //out.write(toWrite,0,i);

            for (byte b : toWrite) {
                limiter.send(b);
                out.write(b);
                if(out.checkError()) break;
            }
        }else{
            try {
                out.write(toWrite);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        out.flush();
        Log.i(LOG_TAG, networkFrame.type.toString() + " ( " + networkFrame.type.toString() + ", " + toWrite.length + " bytes) successfully sent.");
    }
}
