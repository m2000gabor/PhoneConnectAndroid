package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.items.FrameType;

@Deprecated
public class FileSender extends RunnableWithHandler{
    private static final boolean DELETE_AFTER_SENT = false;
    private static final String FILE_SENDER_LOG_TAG = "FILE_SENDER";
    private final PrintStream out;
    private final String path;

    public FileSender(PrintStream out, String path) {
        this.out = out;
        this.path = path;
    }

    @Override
    void runMain() {
        //Based on: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets
        File fileToBeSent = new File(path);
        byte[] buffer  = new byte[(int)fileToBeSent.length()];
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToBeSent));
            int readBytes = bis.read(buffer);
            System.out.println("Sending " + path + "(" + readBytes + " bytes)");

            MyNetworkProtocolFrame outFrame = new MyNetworkProtocolFrame(
                    FrameType.SEGMENT,
                    getFileNameFromPath(path),buffer);
            out.write(outFrame.getAsBytes());
            out.flush();
            bis.close();
            Log.i(FILE_SENDER_LOG_TAG,"File ("+ outFrame.getDataLength()+" bytes) successfully sent.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(FILE_SENDER_LOG_TAG,"FileNotFound");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    void onFinished() {
        Log.i(FILE_SENDER_LOG_TAG, "File sending ended");
        deleteFile(new File(path));
        //sendMessageToActivity(getFileNameFromPath(path));
    }

    private String getFileNameFromPath(String path){
        String[] splittedPath = path.split("/");
        return splittedPath[splittedPath.length-1];
    }

    /**
     * Delete file after its sent
     */
    private void deleteFile(File file){//todo implement properly
        if(DELETE_AFTER_SENT) {
            /*
            FileObserver fileObserver = new FileObserver(file.getPath()) { //listening on wrong event type
                @Override
                public void onEvent(int event, @Nullable String path) {
                    System.out.println(event);
                    if(event==FileObserver.CLOSE_WRITE){
                        Log.d(getClass().toString(), "Delete:"+ file.getName());
                        if(file.delete()){
                            Log.w(getClass().toString(),"Unable to delete file");
                        }
                        this.stopWatching();
                    }

                }
            };
            fileObserver.startWatching();
           */

            Log.d("DELETE", "Delete: "+ file.getName());
            if(!file.delete()){
                Log.w("DELETE","Unable to delete file");
            }else{
                Log.d("DELETE", "Deleted: "+ file.getName());
            }
        }
    }
}
