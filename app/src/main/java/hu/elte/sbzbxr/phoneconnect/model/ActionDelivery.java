package hu.elte.sbzbxr.phoneconnect.model;

import java.util.HashSet;

import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;

public class ActionDelivery {
    private final HashSet<ActionObserver> observers = new HashSet<>();

    public void post(NetworkAction action){
        notifyObservers(action);
    }

    private void notifyObservers(NetworkAction action){
        observers.forEach(o -> o.arrived(action));
    }

    public void register(ActionObserver observer){
        observers.add(observer);
    }

}
