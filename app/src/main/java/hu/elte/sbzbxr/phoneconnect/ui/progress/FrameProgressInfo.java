package hu.elte.sbzbxr.phoneconnect.ui.progress;

import java.util.Objects;

public class FrameProgressInfo {
    private final String folderName;
    private String filename;
    private final long folderSize;
    private long fileSize;

    private long arrivedBytesFromFile;
    private long arrivedBytesFromFolder;


    public FrameProgressInfo(String folderName, String filename, long folderSize, long fileSize, long arrivedBytesFromFile, long arrivedBytesFromFolder) {
        this.folderName = folderName;
        this.filename = filename;
        this.folderSize = folderSize;
        this.fileSize = fileSize;
        this.arrivedBytesFromFile = arrivedBytesFromFile;
        this.arrivedBytesFromFolder = arrivedBytesFromFolder;
    }

    public void arrived(long bytes){
        arrivedBytesFromFile+=bytes;
        arrivedBytesFromFolder+=bytes;
    }

    public boolean isFileArrived(){
        return fileSize<=arrivedBytesFromFile;
    }

    public boolean isFolderArrived(){
        return folderSize<=arrivedBytesFromFolder;
    }

    public boolean hasFolder(){
        return folderName!=null && folderSize>0 && !Objects.equals(folderName, "");
    }

    public String getFolderName() {
        return folderName;
    }

    public String getFilename() {
        return filename;
    }

    public long getFolderSize() {
        return folderSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getArrivedBytesFromFile() {
        return arrivedBytesFromFile;
    }

    public long getArrivedBytesFromFolder() {
        return arrivedBytesFromFolder;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
