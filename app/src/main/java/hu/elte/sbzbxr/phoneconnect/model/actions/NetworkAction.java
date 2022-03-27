package hu.elte.sbzbxr.phoneconnect.model.actions;

import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;

public class NetworkAction {
    public final ActionType type;

    public NetworkAction(ActionType type) {
        this.type = type;
    }
}
