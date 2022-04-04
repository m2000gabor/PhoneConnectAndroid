package hu.elte.sbzbxr.phoneconnect.model.connection;

import android.content.ContentResolver;

import java.io.IOException;
import java.io.InputStream;

import hu.elte.sbzbxr.phoneconnect.model.persistance.MyFileDescriptor;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.FileCutter;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;

public class FileCutterCreator {

    public static FileCutter create(MyFileDescriptor myFileDescriptor, ContentResolver contentResolver, FrameType type, String folderName, long folderSize){
        InputStream inputStream1;
        try  {
            inputStream1 = contentResolver.openInputStream(myFileDescriptor.uri);
        } catch (IOException e) {
            inputStream1 =null;
            e.printStackTrace();
        }
        return new FileCutter(inputStream1,myFileDescriptor.filename, myFileDescriptor.size,type,folderName,folderSize);
    }

}
