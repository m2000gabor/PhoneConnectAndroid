package hu.elte.sbzbxr.phoneconnect.ui.progress;

import static org.junit.Assert.*;

import org.junit.Test;

import hu.elte.sbzbxr.phoneconnect.model.fileTransferProgress.FrameProgressInfo;

public class FrameProgressInfoTest {

    @Test
    public void testSingleFile_NoLastPieceArrived(){
        byte[] data = "data".getBytes();
        final FrameProgressInfo original = FrameProgressInfo.newFolder("","filename",data.length);
        fileArrivedNoLastPiece(original,data);
    }

    private FrameProgressInfo fileArrivedNoLastPiece(final FrameProgressInfo original, byte[] data){
        FrameProgressInfo info = original;
        assertFalse(info.isFileArrived());

        info = FrameProgressInfo.updateProgress(info,1);
        assertFalse(info.isFileArrived());
        checkBaseDataHasntChanged(original,info);

        info = FrameProgressInfo.updateProgress(info, data.length-1);
        assertTrue(info.isFileArrived());
        assertEquals(data.length, info.getArrivedBytesFromFile());
        checkBaseDataHasntChanged(original,info);

        return info;
    }


    private void checkBaseDataHasntChanged(FrameProgressInfo expected, FrameProgressInfo actual){
        assertEquals(expected.getFilename(),actual.getFilename());
        assertEquals(expected.getFolderName(),actual.getFolderName());
        assertEquals(expected.getFileSize(),actual.getFileSize());
    }

    @Test
    public void testSingleFile_LastPieceArrivedNormally(){
        byte[] data = "data".getBytes();
        final FrameProgressInfo original = FrameProgressInfo.newFolder("","filename",data.length);
        FrameProgressInfo info = idealFileArrival(original,data);
    }

    private FrameProgressInfo idealFileArrival(FrameProgressInfo startFrom,byte[] data){
        FrameProgressInfo info = fileArrivedNoLastPiece(startFrom,data);
        checkBaseDataHasntChanged(startFrom,info);
        assertTrue(info.isFileArrived());
        return info;
    }

    @Test
    public void testSingleFile_LastPieceArrivedEarlier(){
        byte[] data = "data".getBytes();
        final FrameProgressInfo original = FrameProgressInfo.newFolder("","filename",data.length);
        FrameProgressInfo info = original;
        assertFalse(info.isFileArrived());

        info = FrameProgressInfo.updateProgress(info,1);
        assertFalse(info.isFileArrived());
        checkBaseDataHasntChanged(original,info);

        info = FrameProgressInfo.updateProgress(info,data.length-1);
        assertTrue(info.isFileArrived());
        assertEquals(data.length, info.getArrivedBytesFromFile());
        checkBaseDataHasntChanged(original,info);
    }
}