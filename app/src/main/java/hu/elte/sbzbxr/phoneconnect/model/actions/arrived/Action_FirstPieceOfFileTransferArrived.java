package hu.elte.sbzbxr.phoneconnect.model.actions.arrived;

import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.SingleFieldAction;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.BackupFileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;

public class Action_FirstPieceOfFileTransferArrived extends SingleFieldAction<FileFrame> {
    public Action_FirstPieceOfFileTransferArrived(FileFrame fileFrame ) {
        super(ActionType.FILE_TRANSFER_FIRST_ARRIVED, fileFrame);
    }
}
