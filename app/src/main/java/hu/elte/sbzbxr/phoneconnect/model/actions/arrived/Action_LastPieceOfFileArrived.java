package hu.elte.sbzbxr.phoneconnect.model.actions.arrived;

import static hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType.LAST_PIECE_OF_FILE_ARRIVED;

import hu.elte.sbzbxr.phoneconnect.model.actions.helper.DoubleFieldAction;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.SingleFieldAction;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;

public class Action_LastPieceOfFileArrived extends SingleFieldAction<FileFrame> {

    public Action_LastPieceOfFileArrived(FileFrame fileFrame) {
        super(LAST_PIECE_OF_FILE_ARRIVED, fileFrame);
    }
}
