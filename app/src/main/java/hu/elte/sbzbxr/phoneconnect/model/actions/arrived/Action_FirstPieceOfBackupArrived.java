package hu.elte.sbzbxr.phoneconnect.model.actions.arrived;

import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.SingleFieldAction;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.BackupFileFrame;

public class Action_FirstPieceOfBackupArrived extends SingleFieldAction<BackupFileFrame> {
    public Action_FirstPieceOfBackupArrived(BackupFileFrame fileFrame ) {
        super(ActionType.BACKUP_FILE_FIRST_ARRIVED, fileFrame);
    }
}
