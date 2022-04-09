package hu.elte.sbzbxr.phoneconnect.model.connection;

import static hu.elte.sbzbxr.phoneconnect.ui.MainActivity.LOG_SEGMENTS;

import android.util.Log;

import java.io.IOException;
import java.io.PrintStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.NetworkFrame;

public class FrameSender {
    private static final String LOG_TAG = "MyFrameSender";

    private FrameSender() {}

    public static void send(ConnectionLimiter limiter, PrintStream out, NetworkFrame networkFrame) {
        if(networkFrame.type == FrameType.SEGMENT){((ScreenShotFrame)networkFrame).getScreenShot().addTimestamp("beforeSerialize",System.currentTimeMillis());}
        byte[] toWrite = networkFrame.serialize().getAsBytes();
        if(networkFrame.type == FrameType.SEGMENT){((ScreenShotFrame)networkFrame).getScreenShot().addTimestamp("afterSerialization",System.currentTimeMillis());}
        if(toWrite==null){
            Log.e(LOG_TAG,"Serialization returned null");
            return;
        }
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
                if(networkFrame.type == FrameType.SEGMENT){((ScreenShotFrame)networkFrame).getScreenShot().addTimestamp("beforeSending",System.currentTimeMillis());}
                out.write(toWrite);
                if(networkFrame.type == FrameType.SEGMENT){((ScreenShotFrame)networkFrame).getScreenShot().addTimestamp("afterSending",System.currentTimeMillis());}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        out.flush();
        Log.i(LOG_TAG, networkFrame.type.toString() + " ( " + networkFrame.type.toString() + ", " + toWrite.length + " bytes) successfully sent.");
        if(networkFrame.type == FrameType.SEGMENT && LOG_SEGMENTS){
            try {
                Log.d(LOG_TAG,((ScreenShotFrame)networkFrame).getScreenShot().toString());
            }catch (NullPointerException ignore){}
        }
    }
}
