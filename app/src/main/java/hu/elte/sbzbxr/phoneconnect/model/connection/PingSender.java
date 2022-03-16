package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.util.Log;

import java.io.IOException;
import java.io.PrintStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.items.FrameType;

//todo transform to SendableMessage.class
@Deprecated
public class PingSender extends RunnableWithHandler {
    private static final String LOG_TAG= "Pinger";
    private final PrintStream out;
    private final ConnectionManager connectionManager;

    @Deprecated
    public PingSender(PrintStream out, ConnectionManager connectionManager) {
        this.out = out;
        this.connectionManager = connectionManager;
    }

    @Override
    void runMain() {
        String pingMessage = "Hello server!";
        MyNetworkProtocolFrame frame = new MyNetworkProtocolFrame(FrameType.PING,pingMessage);
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
