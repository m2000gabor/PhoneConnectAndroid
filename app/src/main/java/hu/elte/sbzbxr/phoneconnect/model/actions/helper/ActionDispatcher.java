package hu.elte.sbzbxr.phoneconnect.model.actions.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;

public class ActionDispatcher {
    private final HashMap<ActionType, Set<ActionObserver>> map;//type , list of observers

    public ActionDispatcher() {
        this.map = new HashMap<>();
        for(ActionType type : ActionType.values()){
            map.put(type,new HashSet<>());
        }
    }

    public void observe(ActionObserver observer,ActionType toObserver){
        if(observer==null || toObserver==null) {
            System.err.println("Shouldn't be null");
            return;
        }
        if(!map.containsKey(toObserver)){throw new IllegalArgumentException("Unknown type");}
        map.get(toObserver).add(observer);
    }

    public void postAction(NetworkAction action){
        if(!map.containsKey(action.type)) {
            System.err.println("unknown type");
            return;
        }
        switch(action.type){
            case PING_ARRIVED:
                //map.get(ActionType.PING_ARRIVED).forEach( o->o.);
        };
    }
}
