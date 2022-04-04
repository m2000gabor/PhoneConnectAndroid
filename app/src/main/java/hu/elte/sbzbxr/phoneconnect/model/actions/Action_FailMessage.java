package hu.elte.sbzbxr.phoneconnect.model.actions;

import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.SingleFieldAction;

public class Action_FailMessage extends SingleFieldAction<String> {
    public Action_FailMessage(String msg) {
        super(ActionType.FAIL_MESSAGE, msg);
    }
}
