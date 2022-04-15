package hu.elte.sbzbxr.phoneconnect.model.connection.fileTransferProgress;

import java.util.Queue;

import hu.elte.sbzbxr.phoneconnect.ui.progress.FrameProgressInfo;

public interface ProgressUi {
    void refresh(Queue<FrameProgressInfo> activeTransfers);
}
