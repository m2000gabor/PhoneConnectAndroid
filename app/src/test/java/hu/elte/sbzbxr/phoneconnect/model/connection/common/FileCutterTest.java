package hu.elte.sbzbxr.phoneconnect.model.connection.common;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import hu.elte.sbzbxr.phoneconnect.model.connection.FileCutterCreator;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;

public class FileCutterTest {

    @Test
    public void noCutRequired() {
        byte[] data = "noCutNeeded".getBytes(StandardCharsets.UTF_8);
        FileFrame originalFrame = new FileFrame(FrameType.FILE,"filename",(long)data.length,null,null,data);

        InputStream inputStream = new ByteArrayInputStream(originalFrame.getData());
        FileCutter fileCutter = new FileCutter(inputStream,originalFrame.filename,
                originalFrame.fileTotalSize, originalFrame.type,originalFrame.folderName,originalFrame.folderSize);
        assertFalse(fileCutter.isEnd());

        FileFrame current = fileCutter.current();
        testBaseInfo(originalFrame,current);
        Assert.assertArrayEquals(originalFrame.data, current.data);

        assertFalse(fileCutter.isEnd());
        fileCutter.next();
        current = fileCutter.current();
        assertEquals(originalFrame.filename,current.filename);
        assertEquals(originalFrame.folderName,current.folderName);
        assertEquals(originalFrame.type,current.type);
        assertEquals(current.getDataLength(),0);
        assertFalse(fileCutter.isEnd());
        fileCutter.next();
        assertTrue(fileCutter.isEnd());
    }

    private void testBaseInfo( FileFrame original, FileFrame current){
        assertEquals(original.filename,current.filename);
        assertEquals(original.fileTotalSize,current.fileTotalSize);
        assertEquals(original.folderSize,current.folderSize);
        assertEquals(original.folderName,current.folderName);
    }

    @Test
    public void oneCutRequired() throws IOException {
        byte[] data = new byte[FileCutter.FILE_FRAME_MAX_SIZE+5];
        new Random().nextBytes(data); //Fill the array with random bytes
        FileFrame originalFrame = new FileFrame(FrameType.FILE,"filename",
                (long)data.length,null,null,data);

        InputStream originalStream = new ByteArrayInputStream(data);
        FileCutter fileCutter = new FileCutter(originalStream,originalFrame.filename,
                originalFrame.fileTotalSize, originalFrame.type,originalFrame.folderName,originalFrame.folderSize);
        assertFalse(fileCutter.isEnd());

        //first piece
        FileFrame current = fileCutter.current();
        testBaseInfo(originalFrame,current);
        for(int i=0;i<FileCutter.FILE_FRAME_MAX_SIZE;i++){
            assertEquals(data[i],current.data[i]);
        }

        //second piece
        fileCutter.next();
        current = fileCutter.current();
        testBaseInfo(originalFrame,current);
        for(int i=0;i<4;i++){
            assertEquals(data[FileCutter.FILE_FRAME_MAX_SIZE+1+i],current.data[i]);
        }

        //end of file piece
        assertFalse(fileCutter.isEnd());
        fileCutter.next();
        current = fileCutter.current();
        assertEquals(originalFrame.filename,current.filename);
        assertEquals(originalFrame.folderName,current.folderName);
        assertEquals(originalFrame.type,current.type);
        assertEquals(current.getDataLength(),0);
        assertFalse(fileCutter.isEnd());

        //end of iteration
        fileCutter.next();
        assertTrue(fileCutter.isEnd());
        assertEquals(-1,originalStream.read());
    }
}