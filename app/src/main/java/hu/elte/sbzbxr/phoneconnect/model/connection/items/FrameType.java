package hu.elte.sbzbxr.phoneconnect.model.connection.items;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 1.1
 */
public enum FrameType{
    INVALID(0),
    INTERNAL_MESSAGE(1),
    SEGMENT(2),
    NOTIFICATION(3),
    FILE(4);

    public final byte v;

    private FrameType(int val){this.v=(byte)val;}
}

