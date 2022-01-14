package hu.elte.sbzbxr.phoneconnect.model.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketException;

public class Pinger extends RunnableWithHandler {
    private final PrintStream out;
    private final BufferedReader in;
    private String receivedMessage;
    private boolean result=false;
    private final ConnectionManager connectionManager;

    public Pinger(PrintStream out, BufferedReader in, ConnectionManager connectionManager) {
        this.out = out;
        this.in = in;
        this.connectionManager = connectionManager;
    }

    @Override
    void runMain() {
        out.print("Hello server!");

        // Read data from the server until we finish reading the document
        StringBuilder stringBuilder = new StringBuilder();
        String line=null;
        try {
            line = in.readLine();
            while (line != null) {
                System.out.println(line);
                stringBuilder.append(line);
                line = in.readLine();
            }
            receivedMessage=stringBuilder.toString();
            result = true;
        }
        catch(SocketException socketException){//A kapcsolat megszakadt
            socketException.printStackTrace();
            if(line!=null){
                receivedMessage=stringBuilder.toString();
                result = true;
            }
            System.err.println("Disconnected!");
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    void onFinished() {
        connectionManager.pingRequestFinished(result,receivedMessage);
    }
}
