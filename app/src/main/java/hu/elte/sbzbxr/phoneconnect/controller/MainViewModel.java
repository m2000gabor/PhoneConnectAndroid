package hu.elte.sbzbxr.phoneconnect.controller;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.net.Socket;

import hu.elte.sbzbxr.phoneconnect.model.ActionDelivery;
import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;
import hu.elte.sbzbxr.phoneconnect.model.actions.networkstate.Action_NetworkStateConnected;
import hu.elte.sbzbxr.phoneconnect.model.actions.networkstate.Action_NetworkStateDisconnected;
import hu.elte.sbzbxr.phoneconnect.model.actions.networkstate.NetworkStateAction;
import hu.elte.sbzbxr.phoneconnect.model.fileTransferProgress.FileTransferSummary;
import hu.elte.sbzbxr.phoneconnect.model.notification.NotificationFilter;
import hu.elte.sbzbxr.phoneconnect.ui.ConnectedFragmentUIData;

public class MainViewModel extends AndroidViewModel {
    private ActionDelivery incomeAction;
    private MutableLiveData<NetworkStateAction> connectionData;
    private MutableLiveData<ConnectedFragmentUIData> uiData;
    public final NotificationFilter notificationFilter = new NotificationFilter();
    private final FileTransferSummary incomingFileTransferSummary = new FileTransferSummary();
    private final FileTransferSummary outgoingFileTransferSummary = new FileTransferSummary();

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<NetworkStateAction> getConnectionData(){
        if (connectionData == null) {
            connectionData = new MutableLiveData<>();
        }
        return connectionData;
    }

    public ActionDelivery getActions() {
        if (incomeAction == null) {
            incomeAction = new ActionDelivery();
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
            ConnectedFragmentUIData newestData = serviceController.getConnectedUIData();
            if(!newestData.equals(uiData.getValue())){
                uiData.postValue(newestData);
            }
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
        NetworkStateAction oldVal = connectionData.getValue();
        NetworkStateAction newVal;
        Socket s = serviceController.getSocket();
        if(ServiceController.isConnected(s)){
            Log.d(MainViewModel.class.toString(),"Determine whether it is connected. Result: connected");
            newVal=new Action_NetworkStateConnected(s.getInetAddress().getHostAddress(),s.getPort());
        }else{
            Log.d(MainViewModel.class.toString(),"Determine whether it is connected. Result: disconnected");
            newVal=new Action_NetworkStateDisconnected();
        }

        if(oldVal==null || oldVal.getType()!=newVal.getType()){
            connectionData.postValue(newVal);
        }
    }

    public void postAction(NetworkAction networkAction){
        if(networkAction instanceof NetworkStateAction){
            postNetworkAction((NetworkStateAction) networkAction);
        }else{
            incomeAction.post(networkAction);
        }
    }

    private void postNetworkAction(NetworkStateAction action){
        connectionData.postValue(action);
    }

    public FileTransferSummary getIncomingFileTransferSummary() {
        return incomingFileTransferSummary;
    }

    public FileTransferSummary getOutgoingFileTransferSummary() {
        return outgoingFileTransferSummary;
    }
}
