package hu.elte.sbzbxr.phoneconnect.ui;

import android.content.Intent;

import hu.elte.sbzbxr.phoneconnect.controller.ServiceController;

public interface MainActivityCallback {
    ServiceController getServiceController();

    void startNotificationListening();
    void stopNotificationListening();

    void startScreenCapture(int resultCode, Intent data);

    void startDemoCapture();
}
