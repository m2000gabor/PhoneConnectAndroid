package hu.elte.sbzbxr.phoneconnect.controller;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.net.Socket;
import java.util.Optional;

import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;
import hu.elte.sbzbxr.phoneconnect.model.actions.networkstate.Action_NetworkStateConnected;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;
import hu.elte.sbzbxr.phoneconnect.model.actions.networkstate.Action_NetworkStateDisconnected;
import hu.elte.sbzbxr.phoneconnect.model.actions.networkstate.NetworkStateAction;
import hu.elte.sbzbxr.phoneconnect.model.notification.NotificationFilter;
import hu.elte.sbzbxr.phoneconnect.ui.MainActivity;

public class MainViewModel extends AndroidViewModel {
    private final Application application;
    private final ServiceController serviceController;
    private MutableLiveData<NetworkAction> incomeAction;
    private MutableLiveData<NetworkStateAction> connectionData;
    public final NotificationFilter notificationFilter = new NotificationFilter();

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.application=application;
        serviceController = new ServiceController(this);
    }

    public LiveData<NetworkStateAction> getConnectionData(){
        if (connectionData == null) {
            connectionData = new MutableLiveData<>();
            loadConnectionData(connectionData, serviceController);
        }
        return connectionData;
    }

    public LiveData<NetworkAction> getActions() {
        if (incomeAction == null) {
            incomeAction = new MutableLiveData<>();
            loadActions();
        }
        return incomeAction;
    }

    public void bindConnectionManager(MainActivity mainActivity){
        serviceController.activityBindToConnectionManager(mainActivity);
    }
    public void unbindConnectionManager(MainActivity mainActivity) {
        serviceController.activityUnbindFromConnectionManager(mainActivity);
    }


    private void loadActions() {
        // Do an asynchronous operation to fetch users.
    }

    private static void loadConnectionData(MutableLiveData<NetworkStateAction> connectionData, ServiceController serviceController) {
        Socket s = serviceController.isConnected();
        if(s!=null){
            connectionData.postValue(new Action_NetworkStateConnected(s.getInetAddress().getHostAddress(),s.getPort()));
        }else{
            connectionData.postValue(new Action_NetworkStateDisconnected());
        }
    }

    public ServiceController getServiceController() {
        return serviceController;
    }

    public void postAction(NetworkAction action){
        if(action instanceof NetworkStateAction){
            postNetworkAction((NetworkStateAction) action);
        }else{
            incomeAction.postValue(action);
        }

    }

    private void postNetworkAction(NetworkStateAction action){
        connectionData.postValue(action);
    }
}
