package hu.elte.sbzbxr.phoneconnect.model.connection;

import static hu.elte.sbzbxr.phoneconnect.ui.MainActivity.LOG_SEGMENTS;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.NetworkFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.UdpSegmentFramePart;

public class UdpSender {
    private static final String LOG_TAG = "UdpSender";
    private static final Random random = new Random();
    private static final AtomicLong uniqueFrameId = new AtomicLong(random.nextLong());

    private UdpSender() {}

    public static void send(ConnectionLimiter limiter, DatagramSocket socket, InetAddress address, int port, NetworkFrame networkFrame) throws IOException {
        if(networkFrame.type == FrameType.SEGMENT){((ScreenShotFrame)networkFrame).getScreenShot().addTimestamp("beforeSerialize",System.currentTimeMillis());}
        byte[] toWrite = networkFrame.serialize().getAsBytes();
        if(networkFrame.type == FrameType.SEGMENT){((ScreenShotFrame)networkFrame).getScreenShot().addTimestamp("afterSerialization",System.currentTimeMillis());}
        if(toWrite==null){
            Log.e(LOG_TAG,"Serialization returned null");
            return;
        }

        uniqueFrameId.getAndIncrement();
        long initialId = random.nextLong();
        long sentBytes=0;
        ByteArrayInputStream in = new ByteArrayInputStream(toWrite);

        while(sentBytes<toWrite.length){
            byte[] partBuffer = new byte[UdpSegmentFramePart.MAX_FRAME_PART_SIZE];
            int readBytes = in.read(partBuffer);
            if(readBytes==-1) break;
            UdpSegmentFramePart frameToSend = new UdpSegmentFramePart(
                    uniqueFrameId.get(),initialId++,toWrite.length,
                    readBytes<UdpSegmentFramePart.MAX_FRAME_PART_SIZE,sentBytes==0,partBuffer);
            byte[] toSend = frameToSend.serialize().getAsBytes();
            DatagramPacket packet = new DatagramPacket(toSend, toSend.length, address, port);
            socket.send(packet);
            sentBytes+=readBytes;
        }

        Log.i(LOG_TAG, networkFrame.type.toString() + " ( " + networkFrame.type.toString() + ", " + toWrite.length + " bytes) successfully sent.");
        if(networkFrame.type == FrameType.SEGMENT && LOG_SEGMENTS){
            try {
                Log.d(LOG_TAG,((ScreenShotFrame)networkFrame).getScreenShot().toString());
            }catch (NullPointerException ignore){}
        }
    }
}
