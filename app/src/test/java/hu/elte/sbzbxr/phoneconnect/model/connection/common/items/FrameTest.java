package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class FrameTest {

    @Test
    public void basicFileFrameTest() throws IOException {
        FileFrame before = new FileFrame(FrameType.FILE,"filename",(long)"data".getBytes().length,"data".getBytes(), null, 0L);
        byte[] transformed = before.serialize().getAsBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(transformed);
        FrameType readType = NetworkFrameCreator.getType(inputStream); //readType
        FileFrame after = FileFrame.deserialize(readType,inputStream);

        assertEquals(before.filename,after.filename);
        for(int i =0; i<before.data.length;i++){
            assertEquals(before.data[i],after.data[i]);
        }
        assertEquals(before.type,after.type);
    }

    @Test
    public void basicBackupFrameTest() throws IOException {
        BackupFileFrame before = new BackupFileFrame(FrameType.FILE,"filename",(long)"data".getBytes().length,"data".getBytes(),"folderName",1L);
        byte[] transformed = before.serialize().getAsBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(transformed);
        FrameType readType = NetworkFrameCreator.getType(inputStream); //readType
        BackupFileFrame after = BackupFileFrame.deserialize(readType,inputStream);

        assertEquals(before.filename,after.filename);
        for(int i =0; i<before.data.length;i++){
            assertEquals(before.data[i],after.data[i]);
        }
        assertEquals(before.type,after.type);
        assertEquals(before.folderName,after.folderName);
    }


    @Test
    public void basicSegmentFrameTest() throws IOException {
        SegmentFrame before = new SegmentFrame("filename","data".getBytes(),"folderName");
        byte[] transformed = before.serialize().getAsBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(transformed);
        FrameType readType = NetworkFrameCreator.getType(inputStream); //readType
        SegmentFrame after = SegmentFrame.deserialize(readType,inputStream);

        assertEquals(before.filename,after.filename);
        for(int i =0; i<before.data.length;i++){
            assertEquals(before.data[i],after.data[i]);
        }
        assertEquals(before.type,after.type);
        assertEquals(before.folderName,after.folderName);
    }
}