package hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message;

//Version: 1.3
public enum MessageType {
    PING,
    START_OF_STREAM,
    END_OF_STREAM,

    RESTORE_GET_AVAILABLE,
    RESTORE_POST_AVAILABLE,
    RESTORE_START_RESTORE
}