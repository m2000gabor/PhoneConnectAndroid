package hu.elte.sbzbxr.phoneconnect.model.connection.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.BackupFileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.SegmentFrame;

//version: 1.6
public class FileCutter {
    static final int FILE_FRAME_MAX_SIZE=320000;//in bytes
    private final InputStream inputStream;
    private final String filename;
    private boolean hadClosingPart =false;
    private boolean isEnd=false;
    private FileFrame current;
    private final FrameType fileType;
    private final long fileTotalSize;
    private final String folderName;
    private final long folderSize;


    public FileCutter(InputStream inputStream, String filename, Long fileTotalSize, FrameType type, String folderName, Long folderSize){
        this.fileType=type;
        this.folderName = folderName;
        this.folderSize = folderSize;
        this.filename = filename;
        this.fileTotalSize = fileTotalSize;
        this.inputStream = inputStream;
        next();
    }

    public FileFrame current(){
        return current;
    }

    public void next(){
        if(isEnd) return;
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(FILE_FRAME_MAX_SIZE);){
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
                        current = new BackupFileFrame(fileType,filename,fileTotalSize, folderName, folderSize, byteArrayOutputStream.toByteArray());
                        break;
                    case FILE:
                        current = new FileFrame(fileType,filename,fileTotalSize, null, 0L,byteArrayOutputStream.toByteArray());
                        break;
                    case SEGMENT:
                        current = new SegmentFrame(filename, folderName, byteArrayOutputStream.toByteArray());
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
            default:return new FileFrame(fileType,filename,0L, folderName, folderSize,new byte[0]);
            case BACKUP_FILE: return new BackupFileFrame(fileType,filename, 0L, folderName, folderSize, new byte[0]);
            case SEGMENT: return new SegmentFrame(filename, folderName, new byte[0]);
        }
    }

    public void close(){
        current=getEndOfFileFrame();
        isEnd=true;
        try {
            inputStream.close();
        } catch (IOException ignore) {

        }
    }
}
