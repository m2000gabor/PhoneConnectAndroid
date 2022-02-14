package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.util.Log;

import java.io.IOException;
import java.io.PrintStream;

//todo transform to SendableMessage.class
public class PingSender extends RunnableWithHandler {
    private static final String LOG_TAG= "Pinger";
    private final PrintStream out;
    private final ConnectionManager connectionManager;

    public PingSender(PrintStream out, ConnectionManager connectionManager) {
        this.out = out;
        this.connectionManager = connectionManager;
    }

    @Override
    void runMain() {
        String pingMessage = "Hello server!";
        MyNetworkProtocolFrame frame = new MyNetworkProtocolFrame(MyNetworkProtocolFrame.FrameType.PROTOCOL_PING,pingMessage);
        try {
            out.write(frame.getAsBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG,"Disconnected");
            connectionManager.pingRequestFinished(false,null);
        }
    }

    @Override
    void onFinished() {
        Log.i(LOG_TAG,"ping request sent");
    }
}
