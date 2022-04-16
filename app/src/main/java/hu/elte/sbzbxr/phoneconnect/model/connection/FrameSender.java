package hu.elte.sbzbxr.phoneconnect.model.connection;

import static hu.elte.sbzbxr.phoneconnect.ui.MainActivity.LOG_SEGMENTS;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.NetworkFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.PingMessageFrame;

public class FrameSender {
    private static final String LOG_TAG = "MyFrameSender";

    private FrameSender() {}

    public static void send(ConnectionLimiter limiter, BufferedOutputStream out, NetworkFrame networkFrame) {
        if(networkFrame.type == FrameType.SEGMENT){((ScreenShotFrame)networkFrame).getScreenShot().addTimestamp("beforeSerialize",System.currentTimeMillis());}
        if(networkFrame instanceof PingMessageFrame){((PingMessageFrame)networkFrame).rightBeforeRequest();}
        byte[] toWrite = networkFrame.serialize().getAsBytes();
        if(networkFrame.type == FrameType.SEGMENT){((ScreenShotFrame)networkFrame).getScreenShot().addTimestamp("afterSerialization",System.currentTimeMillis());}
        if(toWrite==null){
            Log.e(LOG_TAG,"Serialization returned null");
            return;
        }
        if(limiter.hasLimit()){
            try {
                if(networkFrame.type == FrameType.SEGMENT){((ScreenShotFrame)networkFrame).getScreenShot().addTimestamp("beforeSending",System.currentTimeMillis());}
                for (byte b : toWrite) {
                    limiter.send(b);
                    out.write(b);
                }
                out.flush();
                if(networkFrame.type == FrameType.SEGMENT){((ScreenShotFrame)networkFrame).getScreenShot().addTimestamp("afterSending",System.currentTimeMillis());}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                if(networkFrame.type == FrameType.SEGMENT){((ScreenShotFrame)networkFrame).getScreenShot().addTimestamp("beforeSending",System.currentTimeMillis());}
                out.write(toWrite);
                out.flush();
                if(networkFrame.type == FrameType.SEGMENT){((ScreenShotFrame)networkFrame).getScreenShot().addTimestamp("afterSending",System.currentTimeMillis());}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.i(LOG_TAG, networkFrame.type.toString() + " ( " + networkFrame.type.toString() + ", " + toWrite.length + " bytes) successfully sent.");
        if(networkFrame.type == FrameType.SEGMENT && LOG_SEGMENTS){
            try {
                Log.d(LOG_TAG,((ScreenShotFrame)networkFrame).getScreenShot().toString());
            }catch (NullPointerException ignore){}
        }
    }
}
