package hu.elte.sbzbxr.phoneconnect.controller;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.net.Socket;
import java.util.logging.Logger;

import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;
import hu.elte.sbzbxr.phoneconnect.model.actions.arrived.Action_FilePieceArrived;
import hu.elte.sbzbxr.phoneconnect.model.actions.arrived.Action_LastPieceOfFileArrived;
import hu.elte.sbzbxr.phoneconnect.model.actions.arrived.Action_PingArrived;
import hu.elte.sbzbxr.phoneconnect.model.actions.arrived.Action_RestoreListAvailable;
import hu.elte.sbzbxr.phoneconnect.model.actions.networkstate.Action_NetworkStateConnected;
import hu.elte.sbzbxr.phoneconnect.model.actions.networkstate.Action_NetworkStateDisconnected;
import hu.elte.sbzbxr.phoneconnect.model.actions.networkstate.NetworkStateAction;
import hu.elte.sbzbxr.phoneconnect.model.actions.sent.Action_FilePieceSent;
import hu.elte.sbzbxr.phoneconnect.model.actions.sent.Action_LastPieceOfFileSent;
import hu.elte.sbzbxr.phoneconnect.model.notification.NotificationFilter;
import hu.elte.sbzbxr.phoneconnect.ui.ConnectedFragmentUIData;

public class MainViewModel extends AndroidViewModel {
    private MutableLiveData<NetworkAction> incomeAction;
    private MutableLiveData<NetworkStateAction> connectionData;
    private MutableLiveData<ConnectedFragmentUIData> uiData;
    public final NotificationFilter notificationFilter = new NotificationFilter();

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<NetworkStateAction> getConnectionData(){
        if (connectionData == null) {
            connectionData = new MutableLiveData<>();
        }
        return connectionData;
    }

    public LiveData<NetworkAction> getActions() {
        if (incomeAction == null) {
            incomeAction = new MutableLiveData<>();
        }
        return incomeAction;
    }

    public LiveData<ConnectedFragmentUIData> getUiData() {
        if (uiData == null) {
            uiData = new MutableLiveData<>();
        }
        return uiData;
    }

    private static void loadUiData(MutableLiveData<ConnectedFragmentUIData> uiData,ServiceController serviceController){
        if(serviceController==null) {
            Log.d(MainViewModel.class.getName(),"ServiceController is null, cannot fetch uiData");
        }else{
            uiData.postValue(serviceController.getConnectedUIData());
        }
    }

    public void refreshData(@Nullable ServiceController controller){
        if(controller==null) return;
        if (connectionData == null) uiData = new MutableLiveData<>();
        if (uiData == null) uiData = new MutableLiveData<>();
        loadConnectionData(connectionData, controller);
        loadUiData(uiData,controller);
    }

    private static void loadConnectionData(MutableLiveData<NetworkStateAction> connectionData,ServiceController serviceController) {
        if(serviceController==null) {
            Log.d(MainViewModel.class.getName(),"ServiceController is null, cannot fetch ConnectionData");
            return;
        }
        Socket s = serviceController.isConnected();
        if(s!=null){
            connectionData.postValue(new Action_NetworkStateConnected(s.getInetAddress().getHostAddress(),s.getPort()));
        }else{
            connectionData.postValue(new Action_NetworkStateDisconnected());
        }
    }

    public void postAction(NetworkAction networkAction){
        if(networkAction instanceof NetworkStateAction){
            postNetworkAction((NetworkStateAction) networkAction);
        }else{
            incomeAction.postValue(networkAction);
        }
    }

    private void postNetworkAction(NetworkStateAction action){
        connectionData.postValue(action);
    }
}
