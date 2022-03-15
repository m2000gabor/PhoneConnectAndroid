package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import hu.elte.sbzbxr.phoneconnect.model.MyFileDescriptor;
import hu.elte.sbzbxr.phoneconnect.model.SendableFile;

//version: 1.1
public class FileCutter {
    private static final int FILE_FRAME_MAX_SIZE=32000;//in bytes
    private final InputStream inputStream;
    private final String filename;
    private boolean isClosingPart=false;
    private boolean isEnd=false;
    private SendableFile current;

    public FileCutter(MyFileDescriptor myFileDescriptor, ContentResolver contentResolver){
        InputStream inputStream1;
        filename = myFileDescriptor.filename;// + "." + myFileDescriptor.fileExtension;
        try  {
            inputStream1 = contentResolver.openInputStream(myFileDescriptor.uri);
        } catch (IOException e) {
            inputStream1 =null;
            e.printStackTrace();
        }
        inputStream = inputStream1;
        next();
    }

    public SendableFile current(){
        return current;
    }

    public void next(){
        if(isEnd) return;
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(FILE_FRAME_MAX_SIZE);
            int writtenBytes=0;
            int read = inputStream.read();
            if(read==-1){
                if(isClosingPart){
                    isEnd = true;
                }else{
                    current = getEndOfFileFrame();
                    isClosingPart=true;
                }
            }else{
                while(writtenBytes<FILE_FRAME_MAX_SIZE && read>=0){
                    byteArrayOutputStream.write(read);
                    writtenBytes++;
                    read = inputStream.read();
                }
                if(read>=0){byteArrayOutputStream.write(read);}
                current = new SendableFile(filename,byteArrayOutputStream.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
            current=null;
        }
    }

    public boolean isEnd(){
        return isEnd;
    }

    private SendableFile getEndOfFileFrame(){
        return new SendableFile(filename,new byte[0]);
    }
}
