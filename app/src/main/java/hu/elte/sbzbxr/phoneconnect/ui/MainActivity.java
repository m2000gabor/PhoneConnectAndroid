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

import hu.elte.sbzbxr.phoneconnect.R;
import hu.elte.sbzbxr.phoneconnect.controller.ServiceController;
import hu.elte.sbzbxr.phoneconnect.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements MainActivityCallback {
    public static final String IP_ADDRESS = "ipAddress";
    public static final String PORT = "port";
    private static final String TAG = MainActivity.class.getName();

    private ServiceController serviceController;

    private ActivityMainBinding binding;

    private final static int FRAGMENT_CONTAINER_ID = R.id.main_fragment_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //View things
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle("Phone Connect");
        myToolbar.setTitleTextColor(Color.WHITE);

        //todo request a serviceManager and determine whether it's connected to the Windows side
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(FRAGMENT_CONTAINER_ID, ToConnectFragment.class, null)
                    .commit();
        }

        //own logic
        serviceController = new ServiceController(this);
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
        switch (item.getItemId()) {
            case R.id.action_settings:
                launchSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static final String CONNECTED_FRAGMENT_TAG="ConnectedFragment";
    public static final String TO_CONNECT_FRAGMENT_TAG="ToConnectFragment";

    public void connectedTo(String ip,int port){
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


    //todo change this
    public void successfulPing(String msg){
        /*
         * For Toasts:
         * @see https://developer.android.com/guide/topics/ui/notifiers/toasts#java
         */
        Toast.makeText(getApplicationContext(), "Ping was successful", Toast.LENGTH_SHORT).show();
        //receivedMessageLabel.setText(msg);
    }

    public void afterDisconnect(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, ToConnectFragment.class, null ,TO_CONNECT_FRAGMENT_TAG)
                .setReorderingAllowed(true)
                .commit();
    }


    @Override
    protected void onStart() {
        super.onStart();
        serviceController.activityBindToConnectionManager();
    }

    @Override
    protected void onStop() {
        serviceController.activityUnbindFromConnectionManager();
        //serviceController.stopNotificationListening();
        super.onStop();
    }

    public ServiceController getServiceController() {
        return serviceController;
    }

}