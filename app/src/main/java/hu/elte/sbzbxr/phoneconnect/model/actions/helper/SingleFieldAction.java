package hu.elte.sbzbxr.phoneconnect.model.actions.helper;

import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;

public abstract class SingleFieldAction<T> extends NetworkAction {
    private final T field;

    public SingleFieldAction(ActionType type, T field) {
        super(type);
        this.field = field;
    }

    public T getField() {
        return field;
    }
}
