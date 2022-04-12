package hu.elte.sbzbxr.phoneconnect.model.actions;

import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.SingleFieldAction;

public class Action_FailedToConnect extends SingleFieldAction<String> {
    public Action_FailedToConnect(String msg) {
        super(ActionType.FAILED_CONNECT, msg);
    }
}
