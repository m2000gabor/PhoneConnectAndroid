package hu.elte.sbzbxr.phoneconnect.model.notification;

import java.util.HashSet;
import java.util.Set;

public class NotificationFilter {
    private Set<String> appsToExclude;

    public NotificationFilter(){
        appsToExclude=new HashSet<>();
    }

    public synchronized void setAppsToExclude(Set<String> appsToExclude) {
        this.appsToExclude = appsToExclude;
    }

    public synchronized boolean toForward(String appName){
        return !appsToExclude.contains(appName);
    }

    public synchronized Set<String> getAppsToExclude() {
        return appsToExclude;
    }
}
