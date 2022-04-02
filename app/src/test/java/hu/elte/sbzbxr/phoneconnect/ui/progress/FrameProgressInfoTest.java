package hu.elte.sbzbxr.phoneconnect.ui.progress;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.List;

public class FrameProgressInfoTest {

    @Test
    public void testSingleFile_NoLastPieceArrived(){
        byte[] data = "data".getBytes();
        final FrameProgressInfo original = FrameProgressInfo.newFolder("","filename",data.length,data.length);
        fileArrivedNoLastPiece(original,data);
    }

    private FrameProgressInfo fileArrivedNoLastPiece(final FrameProgressInfo original, byte[] data){
        FrameProgressInfo info = original;
        assertFalse(info.isFileArrived());
        assertFalse(info.isFolderArrived());

        info = FrameProgressInfo.updateProgress(info,1);
        assertFalse(info.isFileArrived());
        assertFalse(info.isFolderArrived());
        checkBaseDataHasntChanged(original,info);

        info = FrameProgressInfo.updateProgress(info, data.length-1);
        assertTrue(info.isFileArrived());
        if(!info.hasFolder()) assertTrue(info.isFolderArrived());
        assertEquals(data.length, info.getArrivedBytesFromFile());
        if(!info.hasFolder()) assertEquals(data.length, info.getArrivedBytesFromFolder());
        checkBaseDataHasntChanged(original,info);

        return info;
    }


    private void checkBaseDataHasntChanged(FrameProgressInfo expected, FrameProgressInfo actual){
        assertEquals(expected.getFilename(),actual.getFilename());
        assertEquals(expected.getFolderName(),actual.getFolderName());
        assertEquals(expected.getFileSize(),actual.getFileSize());
        assertEquals(expected.getFolderSize(),actual.getFolderSize());
        assertEquals(expected.hasFolder(),actual.hasFolder());
    }

    @Test
    public void testSingleFile_LastPieceArrivedNormally(){
        byte[] data = "data".getBytes();
        final FrameProgressInfo original = FrameProgressInfo.newFolder("","filename",data.length,data.length);
        FrameProgressInfo info = idealFileArrival(original,data);
        assertTrue(info.isFolderArrived());
    }

    private FrameProgressInfo idealFileArrival(FrameProgressInfo startFrom,byte[] data){
        FrameProgressInfo info = fileArrivedNoLastPiece(startFrom,data);
        info=FrameProgressInfo.arrivedLastPiece(info);
        checkBaseDataHasntChanged(startFrom,info);
        assertTrue(info.isFileArrived());
        return info;
    }

    @Test
    public void testSingleFile_LastPieceArrivedEarlier(){
        byte[] data = "data".getBytes();
        final FrameProgressInfo original = FrameProgressInfo.newFolder("","filename",data.length,data.length);
        FrameProgressInfo info = original;
        assertFalse(info.isFileArrived());
        assertFalse(info.isFolderArrived());

        info = FrameProgressInfo.updateProgress(info,1);
        assertFalse(info.isFileArrived());
        assertFalse(info.isFolderArrived());
        checkBaseDataHasntChanged(original,info);

        info = FrameProgressInfo.arrivedLastPiece(info);
        assertTrue(info.isFileArrived());
        assertTrue(info.isFolderArrived());
        assertEquals(data.length, info.getArrivedBytesFromFile());
        assertEquals(data.length, info.getArrivedBytesFromFolder());
        checkBaseDataHasntChanged(original,info);
    }

    @Test
    public void folderTest(){
        byte[] sampleData = "data".getBytes();
        long numOfFiles=3;
        long folderSize=numOfFiles*sampleData.length;
        long fileSize = sampleData.length;
        String folderName = "folderName";
        FrameProgressInfo info = FrameProgressInfo.newFolder(folderName,"file1",folderSize,fileSize);
        info = idealFileArrival(info,sampleData);
        assertFalse(info.isFolderArrived());

        info = FrameProgressInfo.startNextFile(folderName,"file2",folderSize,fileSize, info.getArrivedBytesFromFolder());
        info = idealFileArrival(info,sampleData);
        assertFalse(info.isFolderArrived());

        info = FrameProgressInfo.startNextFile(folderName,"file3",folderSize,fileSize, info.getArrivedBytesFromFolder());
        info = idealFileArrival(info,sampleData);
        assertTrue(info.isFolderArrived());
    }
}