package hu.elte.sbzbxr.phoneconnect.model.actions;

import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.DoubleFieldAction;

public class Action_OutgoingTransferStarted extends DoubleFieldAction<String,Integer> {
    public Action_OutgoingTransferStarted(String filename, Integer filesize) {
        super(ActionType.OUTGOING_FILE_TRANSFER_STARTED, filename, filesize);
    }

    public String getFileName(){
        return getField1();
    }

    public int getFileSize(){
        return getField2();
    }
}
