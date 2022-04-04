package hu.elte.sbzbxr.phoneconnect.model.actions.arrived;

import static hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType.PIECE_OF_FILE_ARRIVED;

import hu.elte.sbzbxr.phoneconnect.model.actions.helper.SingleFieldAction;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FileFrame;

public class Action_FilePieceArrived extends SingleFieldAction<FileFrame> {
    public Action_FilePieceArrived(FileFrame field) {
        super(PIECE_OF_FILE_ARRIVED, field);
    }
}
