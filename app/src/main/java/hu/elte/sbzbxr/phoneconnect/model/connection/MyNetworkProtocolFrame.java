package hu.elte.sbzbxr.phoneconnect.model.connection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Optional;

/**
 * @apiNote Must be the same for both Android and Windows side
 * @version 4.2
 */
public class MyNetworkProtocolFrame {
    public enum FrameType{
        PROTOCOL_PING(1),
        PROTOCOL_SEGMENT(2);

        public final byte v;

        private FrameType(int val){this.v=(byte)val;}
    }

    private final FrameType type;
    private final int nameLength;
    private final String name;
    private final int dataLength;
    private final byte[] data;

    public MyNetworkProtocolFrame(FrameType type, String data) {
        this(type,"ping\n", data.getBytes());
    }

    public MyNetworkProtocolFrame(FrameType type, String name, byte[] data) {
        this.type = type;
        this.nameLength=name.getBytes().length;
        this.name= name;
        this.dataLength = data.length;
        this.data = data;

    }

    public byte[] getAsBytes(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(1+4+nameLength+4+ dataLength);
        byteBuffer.put(type.v);
        byteBuffer.putInt(nameLength);
        byteBuffer.put(name.getBytes());
        byteBuffer.putInt(dataLength);
        byteBuffer.put(data);
        return byteBuffer.array();
    }

    public static MyNetworkProtocolFrame inputStreamToFrame(InputStream in) throws IOException{
        final FrameType type;
        final int nameLength;
        final String name;
        final int dataLength;

        int readByteAsInt = in.read();
        //determine type
        if(readByteAsInt==-1){
            throw new IOException("Nothing received");
        }else if(Arrays.stream(FrameType.values()).noneMatch(x -> x.v== readByteAsInt)){
            throw new IOException("Not a valid type");
        }
        type=getFrameTypeFromByte((byte) readByteAsInt);
        System.out.println("Something read");

        //read nameLength
        nameLength=readLength(in);

        //read name
        name = new String(readNBytes(in,nameLength).array());

        //read the int dataLength
        dataLength=readLength(in);

        //read data
        ByteBuffer byteBuffer = readNBytes(in,dataLength);

        return new MyNetworkProtocolFrame(type,name, byteBuffer.array());
    }

    public static FrameType getFrameTypeFromByte(byte b){
        Optional<FrameType> optional = Arrays.stream(FrameType.values()).filter(frameType -> frameType.v==b).findFirst();
        if (optional.isPresent()){
            return optional.get();
        }else{
            throw new InvalidParameterException("This byte is not represent a FrameType");
        }
    }

    private static int readLength(InputStream in) throws IOException {
        byte[] len = new byte[4];
        int readBytes = in.read(len);
        if(readBytes!=4){throw new IOException("Invalid structure");}
        ByteBuffer bb = ByteBuffer.wrap(len);
        int length= bb.getInt();
        if(length<0){throw new IOException("Negative length");}
        return length;
    }

    private static ByteBuffer readNBytes(InputStream in,int dataLength) throws IOException {
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

    public FrameType getType() {
        return type;
    }

    public int getDataLength() {
        return dataLength;
    }

    public byte[] getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public int getNameLength() {
        return nameLength;
    }
}