package hu.elte.sbzbxr.phoneconnect.model;

import android.os.Handler;
import android.os.Looper;

import java.util.HashSet;

import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;

public class ActionDelivery {
    private final HashSet<ActionObserver> observers = new HashSet<>();

    public void post(NetworkAction action){
        notifyObservers(action);
    }

    private void notifyObservers(NetworkAction action){
        new Handler(Looper.getMainLooper()).post(()->{
            observers.forEach(o -> o.arrived(action));
        });
    }

    public void register(ActionObserver observer){
        observers.add(observer);
    }
    public void unregister(ActionObserver observer){observers.remove(observer);}
}
