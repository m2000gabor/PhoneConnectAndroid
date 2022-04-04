package hu.elte.sbzbxr.phoneconnect.model.actions.networkstate;

import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;

public class Action_NetworkStateDisconnected extends NetworkAction implements NetworkStateAction{
    public Action_NetworkStateDisconnected() {
        super(ActionType.JUST_DISCONNECTED);
    }

    @Override
    public ActionType getType() {
        return type;
    }
}
