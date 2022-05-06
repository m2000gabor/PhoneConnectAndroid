package hu.elte.sbzbxr.phoneconnect.model.actions.helper;

public enum ActionType {
    JUST_CONNECTED,
    JUST_DISCONNECTED,
    FAIL_MESSAGE,
    FAILED_CONNECT,
    
    RESTORE_LIST_OF_AVAILABLE_BACKUPS,
    PING_ARRIVED,
    
    PIECE_OF_FILE_ARRIVED,
    LAST_PIECE_OF_FILE_ARRIVED,
    
    PIECE_OF_FILE_SENT,
    LAST_PIECE_OF_FILE_SENT,

    REQUEST_UI_REFRESH
}
