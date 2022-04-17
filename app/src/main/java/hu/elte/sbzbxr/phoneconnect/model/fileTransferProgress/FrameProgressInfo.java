package hu.elte.sbzbxr.phoneconnect.model.fileTransferProgress;

public class FrameProgressInfo {
    private final String folderName;
    private final String filename;
    private final long fileSize;

    private final long arrivedBytesFromFile;
    private final boolean isFileArrived;

    private FrameProgressInfo(String folderName, String filename, long fileSize, long arrivedBytesFromFile, boolean isFileArrived) {
        this.folderName = folderName;
        this.filename = filename;
        this.fileSize = fileSize;
        this.arrivedBytesFromFile = arrivedBytesFromFile;
        this.isFileArrived = isFileArrived;
    }

    public static FrameProgressInfo newFolder(String folderName, String filename, long fileSize) {
        return new FrameProgressInfo(folderName,filename,fileSize,0, false);
    }

    public static FrameProgressInfo updateProgress(FrameProgressInfo toUpdate, long arrivedBytes){
        if(toUpdate.isFileArrived()) return toUpdate;

        long tmpArrivedFileBytes = toUpdate.getArrivedBytesFromFile()+arrivedBytes;
        boolean tmpIsFileArrived=tmpArrivedFileBytes >= toUpdate.getFileSize();

        return new FrameProgressInfo(toUpdate.getFolderName(), toUpdate.getFilename(),toUpdate.getFileSize(),
                tmpArrivedFileBytes, tmpIsFileArrived);
    }

    public boolean isFileArrived(){
        return isFileArrived;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getFilename() {
        return filename;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getArrivedBytesFromFile() {
        return arrivedBytesFromFile;
    }
}
