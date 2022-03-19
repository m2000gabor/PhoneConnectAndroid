package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.content.ContentResolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import hu.elte.sbzbxr.phoneconnect.model.MyFileDescriptor;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.BackupFileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.SegmentFrame;

//version: 1.4
public class FileCutter {
    private static final int FILE_FRAME_MAX_SIZE=32000;//in bytes
    private final InputStream inputStream;
    private final String filename;
    private boolean hadClosingPart =false;
    private boolean isEnd=false;
    private FileFrame current;
    private final FrameType fileType;
    private final String backupID;

    public FileCutter(MyFileDescriptor myFileDescriptor, ContentResolver contentResolver,FrameType type, String backupID){
        this.fileType=type;
        this.backupID=backupID;
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

    public FileFrame current(){
        return current;
    }

    public void next(){
        if(isEnd) return;
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(FILE_FRAME_MAX_SIZE);
            int writtenBytes=0;
            int read = inputStream.read();
            if(read==-1){
                if(hadClosingPart){
                    isEnd = true;
                    inputStream.close();
                }else{
                    current = getEndOfFileFrame();
                    hadClosingPart =true;
                }
            }else{
                while(writtenBytes<FILE_FRAME_MAX_SIZE && read>=0){
                    byteArrayOutputStream.write(read);
                    writtenBytes++;
                    read = inputStream.read();
                }
                if(read>=0){byteArrayOutputStream.write(read);}
                switch (fileType){
                    case BACKUP_FILE:
                    case RESTORE_FILE:
                        current = new BackupFileFrame(fileType,filename,byteArrayOutputStream.toByteArray(),backupID);
                        break;
                    case FILE:
                        current = new FileFrame(fileType,filename,byteArrayOutputStream.toByteArray());
                        break;
                    case SEGMENT:
                        current = new SegmentFrame(filename,byteArrayOutputStream.toByteArray(),backupID);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            current=null;
        }
    }

    public boolean isEnd(){
        return isEnd;
    }

    private FileFrame getEndOfFileFrame(){
        switch (fileType){
            default:return new FileFrame(fileType,filename,new byte[0]);
            case BACKUP_FILE: return new BackupFileFrame(fileType,filename,new byte[0],backupID);
            case SEGMENT: return new SegmentFrame(filename,new byte[0],backupID);
        }
    }
}
