package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import hu.elte.sbzbxr.phoneconnect.model.ScreenShot;

public class BitMapSender{
    private static final String LOG_TAG = "FILE_SENDER";

    private BitMapSender() {}

    public static void send(PrintStream out, ScreenShot screenShot) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(3000000);
        screenShot.getBitmap().compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        try {
            MyNetworkProtocolFrame outFrame = new MyNetworkProtocolFrame(
                    MyNetworkProtocolFrame.FrameType.PROTOCOL_SEGMENT,
                    screenShot.getName(),byteArrayOutputStream.toByteArray());
            out.write(outFrame.getAsBytes());
            out.flush();
            Log.i(LOG_TAG,"File ("+ outFrame.getDataLength()+" bytes) successfully sent.");
            sendingFinished(screenShot.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(LOG_TAG,"FileNotFound");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendingFinished(String name) {
        Log.i(LOG_TAG, "Bitmap sent:" + name);
        //sendMessageToActivity(getFileNameFromPath(path));
    }
}
