package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 1.2
 */
public enum FrameType{
    INVALID(0),
    INTERNAL_MESSAGE(1),
    SEGMENT(2),
    NOTIFICATION(3),
    FILE(4),
    RESTORE_FILE(5),
    BACKUP_FILE(6);

    public final byte v;

    FrameType(int val){this.v=(byte)val;}
}
