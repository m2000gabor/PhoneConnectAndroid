package hu.elte.sbzbxr.phoneconnect.model.actions.sent;

import static hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType.LAST_PIECE_OF_FILE_SENT;

import hu.elte.sbzbxr.phoneconnect.model.actions.helper.SingleFieldAction;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;

public class Action_LastPieceOfFileSent extends SingleFieldAction<FileFrame> {

    public Action_LastPieceOfFileSent(FileFrame fileFrame) {
        super(LAST_PIECE_OF_FILE_SENT, fileFrame);
    }
}
