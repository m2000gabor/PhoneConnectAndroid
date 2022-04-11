package hu.elte.sbzbxr.phoneconnect.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

import java.util.Optional;

import hu.elte.sbzbxr.phoneconnect.R;
import hu.elte.sbzbxr.phoneconnect.controller.MainViewModel;
import hu.elte.sbzbxr.phoneconnect.controller.ServiceController;
import hu.elte.sbzbxr.phoneconnect.databinding.ActivityMainBinding;
import hu.elte.sbzbxr.phoneconnect.model.actions.Action_FailMessage;
import hu.elte.sbzbxr.phoneconnect.model.actions.helper.ActionType;
import hu.elte.sbzbxr.phoneconnect.model.actions.networkstate.Action_NetworkStateConnected;

public class MainActivity extends AppCompatActivity implements MainActivityCallback {
    public static final boolean LOG_SEGMENTS=false;
    public static final String IP_ADDRESS = "ipAddress";
    public static final String PORT = "port";
    private static final String TAG = MainActivity.class.getName();

    private ActivityMainBinding binding;

    private final static int FRAGMENT_CONTAINER_ID = R.id.main_fragment_container;
    public static final String CONNECTED_FRAGMENT_TAG="ConnectedFragment";
    public static final String TO_CONNECT_FRAGMENT_TAG="ToConnectFragment";

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

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getActions().observe(this, action -> {
            // update UI
            switch(action.type){
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
            }
        });

        viewModel.getConnectionData().observe(this,networkStateAction -> {
            if(networkStateAction.getType()== ActionType.JUST_CONNECTED){
                Action_NetworkStateConnected a = (Action_NetworkStateConnected) networkStateAction;
                System.err.println("connected to:"+a.getIp()+":"+a.getPort());
                connectedTo(a.getIp(),a.getPort());
            }else if(networkStateAction.getType()== ActionType.JUST_DISCONNECTED){
                //Action_NetworkStateDisconnected a = (Action_NetworkStateDisconnected) networkStateAction;
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
            getServiceController().stopNotificationListening(this);
            getServiceController().stopScreenCapture();
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
        viewModel.bindConnectionManager(this);
    }

    @Override
    protected void onStop() {
        viewModel.unbindConnectionManager(this);
        super.onStop();
    }

    public ServiceController getServiceController() {
        return viewModel.getServiceController();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Optional<ConnectedFragment> getConnectedFragment(){
        return Optional.ofNullable((ConnectedFragment) getSupportFragmentManager().findFragmentByTag(CONNECTED_FRAGMENT_TAG));
    }

    @Override
    public void startNotificationListening() {
        viewModel.getServiceController().startNotificationListening(this);
    }

    @Override
    public void stopNotificationListening() {
        viewModel.getServiceController().stopNotificationListening(this);
    }

    @Override
    public void startScreenCapture(int resultCode, Intent data) {
        viewModel.getServiceController().startRealScreenCapture(resultCode,data,this);
    }

    @Override
    public void startDemoCapture() {
        viewModel.getServiceController().startDemoScreenCapture(this);
    }
}