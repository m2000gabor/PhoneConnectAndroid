package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Optional;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 1.0
 */
public class NetworkFrameCreator {

    public static NetworkFrame create(InputStream in) {
        final FrameType type;
        final int nameLength;
        final String name;

        try {

            int readByteAsInt = in.read();
            //determine type
            if (readByteAsInt == -1) {
                throw new IOException("Nothing received");
            } else if (Arrays.stream(FrameType.values()).noneMatch(x -> x.v == readByteAsInt)) {
                throw new IOException("Not a valid type");
            }
            type = getFrameTypeFromByte((byte) readByteAsInt);
            //System.out.println("Something read");

            //read nameLength
            nameLength = readLength(in);

            //read name
            name = new String(readNBytes(in, nameLength).array());
            return new NetworkFrame(type,name);
        }catch (IOException e){
            e.printStackTrace();
            System.err.println("Cannot create NetworkFrame due to IOException");
            return new NetworkFrame(FrameType.INVALID,"");
        }
    }

    static FrameType getFrameTypeFromByte(byte b){
        Optional<FrameType> optional = Arrays.stream(FrameType.values()).filter(frameType -> frameType.v==b).findFirst();
        if (optional.isPresent()){
            return optional.get();
        }else{
            throw new InvalidParameterException("This byte is not represent a FrameType");
        }
    }

    static int readLength(InputStream in) throws IOException {
        byte[] len = new byte[4];
        int readBytes = in.read(len);
        if(readBytes!=4){throw new IOException("Invalid structure");}
        ByteBuffer bb = ByteBuffer.wrap(len);
        int length= bb.getInt();
        if(length<0){throw new IOException("Negative length");}
        return length;
    }

    static ByteBuffer readNBytes(InputStream in,int dataLength) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(dataLength);
        int readBytesCounter=0;
        while (readBytesCounter<dataLength){
            int res = in.read();
            if(res<0){System.err.println("End of stream?");break;}
            byte b = (byte) res;
            byteBuffer.put(b);
            readBytesCounter++;
        }
        return byteBuffer;
    }

    static String readNextStringField(InputStream in) throws IOException {
        return new String(NetworkFrameCreator.readNBytes(in,NetworkFrameCreator.readLength(in)).array());
    }
}