package hu.elte.sbzbxr.phoneconnect.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import java.util.AbstractMap;

import hu.elte.sbzbxr.phoneconnect.R;
import hu.elte.sbzbxr.phoneconnect.controller.MainViewModel;
import hu.elte.sbzbxr.phoneconnect.controller.ServiceController;
import hu.elte.sbzbxr.phoneconnect.databinding.ActivityMainBinding;
import hu.elte.sbzbxr.phoneconnect.model.ActionObserver;
import hu.elte.sbzbxr.phoneconnect.model.actions.Action_FailMessage;
import hu.elte.sbzbxr.phoneconnect.model.actions.NetworkAction;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;
import hu.elte.sbzbxr.phoneconnect.model.actions.networkstate.Action_NetworkStateConnected;

public class MainActivity extends AppCompatActivity implements MainActivityCallback, ListDialog.NoticeListDialogListener {
    public static final boolean LOG_SEGMENTS=false;
    public static final String IP_ADDRESS = "ipAddress";
    public static final String PORT = "port";
    private static final String TAG = MainActivity.class.getName();

    private ActivityMainBinding binding;

    private final static int FRAGMENT_CONTAINER_ID = R.id.main_fragment_container;
    public static final String CONNECTED_FRAGMENT_TAG="ConnectedFragment";
    public static final String TO_CONNECT_FRAGMENT_TAG="ToConnectFragment";
    public static final String LOADING_FRAGMENT_TAG="LoadingFragment";

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //View things
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //toolbar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle("Phone Connect");
        myToolbar.setTitleTextColor(Color.WHITE);

        startForegroundService(new Intent(this,ServiceController.class));

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getActions().register(new ActionObserver() {
            @Override
            public void arrived(NetworkAction action) {
                switch (action.type){
                case JUST_CONNECTED:
                    Log.e(TAG,"Shouldnt be called, use LiveData<NetworkStateAction>> instead form serviceController.getConnectionData()!");
                    Action_NetworkStateConnected actionNetworkStateConnected = (Action_NetworkStateConnected) action;
                    connectedTo(actionNetworkStateConnected.getIp(), actionNetworkStateConnected.getPort());
                    break;
                case JUST_DISCONNECTED:
                    Log.e(TAG,"Shouldnt be called, use LiveData<NetworkStateAction>> instead form serviceController.getConnectionData()!");
                    afterDisconnect();
                    break;
                case FAIL_MESSAGE:
                    showFailMessage(((Action_FailMessage) action).getField());
                    break;
                case FAILED_CONNECT:
                    afterDisconnect();
                    Toast.makeText(MainActivity.this,"Cannot connect",Toast.LENGTH_SHORT).show();
                    break;
            }
            }
        });

        viewModel.getConnectionData().observe(this,networkStateAction -> {
            if(networkStateAction.getType()== ActionType.JUST_CONNECTED){
                Action_NetworkStateConnected a = (Action_NetworkStateConnected) networkStateAction;
                System.err.println("connected to:"+a.getIp()+":"+a.getPort());
                connectedTo(a.getIp(),a.getPort());
            }else if(networkStateAction.getType()== ActionType.JUST_DISCONNECTED){
                afterDisconnect();
            }else{
                throw new IllegalArgumentException("unknown networkStateAction type");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.action_settings) {
            launchSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void connectedTo(String ip,int port){
        Bundle bundle = new Bundle();
        bundle.putString(IP_ADDRESS,ip);
        bundle.putString(PORT,String.valueOf(port));

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, ConnectedFragment.class, bundle,CONNECTED_FRAGMENT_TAG)
                .setReorderingAllowed(true)
                .commit();
    }

    private void launchSettings(){
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }


    public void showFailMessage(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG,msg);
    }

    private void afterDisconnect(){
        try {
            if(getServiceController()!=null){
                getServiceController().stopNotificationListening(this);
                getServiceController().stopScreenCapture();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(FRAGMENT_CONTAINER_ID, ToConnectFragment.class, null, TO_CONNECT_FRAGMENT_TAG)
                    .setReorderingAllowed(true)
                    .commit();
        }catch (IllegalStateException e){
            e.printStackTrace();
            Log.e(TAG,"Activity is paused or destroyed");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ServiceController.class);
        bindService(intent, connection, Context.BIND_IMPORTANT);
    }

    @Override
    protected void onStop() {
        unbindService(connection);
        mBound = false;
        super.onStop();
    }

    @Nullable private ServiceController serviceController;
    private boolean mBound = false;
    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ServiceController.LocalBinder binder = (ServiceController.LocalBinder) service;
            serviceController = binder.getService();
            serviceController.refreshData(viewModel);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            serviceController = null;
        }
    };

    @Override @Nullable
    public ServiceController getServiceController() {
        return serviceController;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void startNotificationListening() {
        if (getServiceController() != null) {
            getServiceController().startNotificationListening(this);
        }
    }

    @Override
    public void stopNotificationListening() {
        if (getServiceController() != null) {
            getServiceController().stopNotificationListening(this);
        }
    }

    @Override
    public void startScreenCapture(int resultCode, Intent data) {
        if (getServiceController() != null) {
            getServiceController().startRealScreenCapture(resultCode,data,this);
        }
    }

    @Override
    public void startDemoCapture() {
        if (getServiceController() != null) {
            getServiceController().startDemoScreenCapture(this);
        }
    }

    @Override
    public boolean connectToServer(String ip, int port) {
        boolean r = getServiceController().connectToServer(ip, port);
        LoadingDialog loadingDialog = new LoadingDialog("Connecting...");
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, loadingDialog, LOADING_FRAGMENT_TAG)
                .setReorderingAllowed(true)
                .commit();
        return r;
    }

    @Override
    public void onListItemSelected(AbstractMap.SimpleImmutableEntry<String, Long> selectedEntry, String selectedLabel) {
        if (getServiceController() != null) {
            getServiceController().requestRestore(selectedEntry.getKey());
        }
    }

    @Override
    public void onRestoreCancelled() {
        System.err.println("User cancelled");
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this,ServiceController.class));
        super.onDestroy();
    }
}