package hu.elte.sbzbxr.phoneconnect.model;

import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;

public interface ActionObserver {
    void arrived(NetworkAction action);
}
