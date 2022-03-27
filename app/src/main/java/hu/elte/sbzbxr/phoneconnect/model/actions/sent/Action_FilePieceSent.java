package hu.elte.sbzbxr.phoneconnect.model.actions.sent;

import static hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType.PIECE_OF_FILE_ARRIVED;
import static hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType.PIECE_OF_FILE_SENT;

import hu.elte.sbzbxr.phoneconnect.model.actions.helper.SingleFieldAction;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;

public class Action_FilePieceSent extends SingleFieldAction<FileFrame> {
    public Action_FilePieceSent(FileFrame field) {
        super(PIECE_OF_FILE_SENT, field);
    }
}
