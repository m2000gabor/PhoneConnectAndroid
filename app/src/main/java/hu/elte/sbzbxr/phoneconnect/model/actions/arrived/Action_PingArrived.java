package hu.elte.sbzbxr.phoneconnect.model.actions.arrived;

import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.SingleFieldAction;

public class Action_PingArrived extends SingleFieldAction<String> {
    public Action_PingArrived(String field) {
        super(ActionType.PING_ARRIVED, field);
    }
}
