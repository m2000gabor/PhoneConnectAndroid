package hu.elte.sbzbxr.phoneconnect.model.connection;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.persistance.MyFileDescriptor;

public class FileToSend {
    public final MyFileDescriptor descriptor;
    public final FrameType fileType;
    public final String backupId;
    public final long folderSize;

    public FileToSend(MyFileDescriptor descriptor, FrameType fileType, String backupId, long folderSize) {
        this.descriptor = descriptor;
        this.fileType = fileType;
        this.backupId = backupId;
        this.folderSize = folderSize;
    }
}
