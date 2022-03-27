package hu.elte.sbzbxr.phoneconnect.model.actions.arrived;

import static hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType.RESTORE_LIST_OF_AVAILABLE_BACKUPS;

import java.util.AbstractMap;
import java.util.ArrayList;

import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.SingleFieldAction;

public class Action_RestoreListAvailable extends SingleFieldAction<ArrayList<AbstractMap.SimpleImmutableEntry<String, Long>>> {

    public Action_RestoreListAvailable(ArrayList<AbstractMap.SimpleImmutableEntry<String, Long>> field) {
        super(RESTORE_LIST_OF_AVAILABLE_BACKUPS, field);
    }
}
