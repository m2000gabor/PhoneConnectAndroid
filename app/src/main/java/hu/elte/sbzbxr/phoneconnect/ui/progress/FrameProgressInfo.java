package hu.elte.sbzbxr.phoneconnect.ui.progress;

import java.util.Objects;

public class FrameProgressInfo {
    private final String folderName;
    private final String filename;
    private final long folderSize;
    private final long fileSize;

    private final long arrivedBytesFromFile;
    private final long arrivedBytesFromFolder;

    private final boolean isFileArrived;
    private final boolean isFolderArrived;

    private FrameProgressInfo(String folderName, String filename, long folderSize, long fileSize, long arrivedBytesFromFile,
                              long arrivedBytesFromFolder, boolean isFileArrived, boolean isFolderArrived) {
        this.folderName = folderName;
        this.filename = filename;
        this.folderSize = folderSize;
        this.fileSize = fileSize;
        this.arrivedBytesFromFile = arrivedBytesFromFile;
        this.arrivedBytesFromFolder = arrivedBytesFromFolder;
        this.isFileArrived = isFileArrived;
        this.isFolderArrived = isFolderArrived;
    }

    public static FrameProgressInfo newFolder(String folderName, String filename, long folderSize, long fileSize) {
        return new FrameProgressInfo(folderName,filename,folderSize,fileSize,0,0, false, false);
    }

    public static FrameProgressInfo startNextFile(String folderName, String filename, long folderSize, long fileSize, long arrivedBytesFromFolder) {
        return new FrameProgressInfo(folderName,filename,folderSize,fileSize,0,arrivedBytesFromFolder, false, false);
    }

    public static FrameProgressInfo updateProgress(FrameProgressInfo toUpdate, long arrivedBytes){
        if(toUpdate.isFileArrived()) return toUpdate;

        long tmpArrivedFileBytes = toUpdate.getArrivedBytesFromFile()+arrivedBytes;
        long tmpArrivedFolderBytes = toUpdate.getArrivedBytesFromFolder()+arrivedBytes;
        boolean tmpIsFileArrived=tmpArrivedFileBytes >= toUpdate.getFileSize();
        boolean tmpIsFolderArrived=tmpArrivedFolderBytes >= toUpdate.getFolderSize();

        return new FrameProgressInfo(toUpdate.getFolderName(), toUpdate.getFilename(), toUpdate.getFolderSize(),toUpdate.getFileSize(),
                tmpArrivedFileBytes, tmpArrivedFolderBytes, tmpIsFileArrived, tmpIsFolderArrived);
    }

    public static FrameProgressInfo arrivedLastPiece(FrameProgressInfo toEnd){
        long tmpArrivedFolderByte = toEnd.getArrivedBytesFromFolder() + Math.abs(toEnd.getFileSize() - toEnd.getArrivedBytesFromFile());
        boolean tmpIsFolderArrived = tmpArrivedFolderByte >= toEnd.getFolderSize();
        return new FrameProgressInfo(toEnd.getFolderName(), toEnd.getFilename(), toEnd.getFolderSize(),
                toEnd.getFileSize(),toEnd.getFileSize(),tmpArrivedFolderByte,true,tmpIsFolderArrived);
    }

    public boolean isFileArrived(){
        return isFileArrived;
    }

    public boolean isFolderArrived(){
        return isFolderArrived;
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
}
