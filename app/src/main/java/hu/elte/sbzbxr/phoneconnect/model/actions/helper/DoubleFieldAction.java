package hu.elte.sbzbxr.phoneconnect.model.actions.helper;

import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;

public abstract class DoubleFieldAction<A,B> extends NetworkAction {
    private final A field1;
    private final B field2;

    public A getField1() {
        return field1;
    }

    public B getField2() {
        return field2;
    }

    public DoubleFieldAction(ActionType type, A field1, B field2) {
        super(type);
        this.field1 = field1;
        this.field2 = field2;
    }
}
