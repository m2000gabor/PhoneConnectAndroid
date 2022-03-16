package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 1.0
 */
public class FileFrame extends NetworkFrame{
    private final byte[] data;

    public FileFrame(FrameType type, String name, byte[] data) {
        super(type,name);
        this.data = data;
    }

    public FileFrame(NetworkFrame networkFrame,InputStream inputStream) {
        super(networkFrame);
        byte[] tmp = new byte[0];
        try {
            int dataLength = NetworkFrameCreator.readLength(inputStream);
            tmp = NetworkFrameCreator.readNBytes(inputStream, dataLength).array();
        }catch (IOException e){
            e.printStackTrace();
        }
        data = tmp;
    }

    @Override
    public InputStream getData() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(getDataAsByteArray());
            outputStream.write(data.length);
            outputStream.write(data);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }catch (IOException e){
            e.printStackTrace();
            return super.getData();
        }
    }

    public int getDataLength(){return data.length;}

}
