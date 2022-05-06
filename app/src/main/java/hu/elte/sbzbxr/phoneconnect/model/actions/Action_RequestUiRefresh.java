package hu.elte.sbzbxr.phoneconnect.model.actions;

import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;

public class Action_RequestUiRefresh extends NetworkAction{
    public Action_RequestUiRefresh() {
        super(ActionType.REQUEST_UI_REFRESH);
    }
}
