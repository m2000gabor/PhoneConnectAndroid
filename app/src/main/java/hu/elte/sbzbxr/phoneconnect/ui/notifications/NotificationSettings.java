package hu.elte.sbzbxr.phoneconnect.ui.notifications;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import hu.elte.sbzbxr.phoneconnect.ui.ConnectedFragment;

public class NotificationSettings implements SaveList {
    public final static String PREF_FILE_NOTIFICATION = "hu.elte.sbzbxr.phoneconnect.ui.notifications."+"notificationsNotToSend";
    public final static String SET_OF_EXCEPTIONS = "exceptions";
    private final ConnectedFragment connectedFragment;
    private final List<NotificationPair> notificationPairs;

    public NotificationSettings(ConnectedFragment connectedFragment) {
        this.connectedFragment=connectedFragment;

        final PackageManager pm = connectedFragment.requireActivity().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<CharSequence> appNames = packages.stream().map(pm::getApplicationLabel).collect(Collectors.toList());
        List<String> storedExceptions = getDisabledNotifications();
        notificationPairs = new ArrayList<>(appNames.size());
        for(CharSequence ch : appNames){
            notificationPairs.add(new NotificationPair(ch.toString(),!storedExceptions.contains(ch.toString())));
        }
    }

    private List<String> getDisabledNotifications(){
        Context context = connectedFragment.requireActivity().getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE_NOTIFICATION, Context.MODE_PRIVATE);
        Set<String> set = sharedPref.getStringSet(SET_OF_EXCEPTIONS,new HashSet<>());
        return new ArrayList<>(set);
    }

    private void savePrefs(Set<String> appsToExclude){
        Context context = connectedFragment.requireActivity().getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE_NOTIFICATION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(SET_OF_EXCEPTIONS,appsToExclude);
        editor.apply();
    }

    //From: https://developer.android.com/guide/topics/ui/dialogs#AddingAList
    public void showDialog() {

        //get a list of installed apps.

        //harSequence[] installedApps = packages.stream().map(p->p.packageName).collect(Collectors.toList()).toArray(new CharSequence[0]);
        new NotificationDialog(notificationPairs,this).show(connectedFragment.getParentFragmentManager(),"notificationSettings");
    }

    @Override
    public void saveNotificationPairs(List<NotificationPair> list) {
        HashSet<String> toExclude = new HashSet<>(list.size());
        for(NotificationPair pair : list){
            if(!pair.enabled) toExclude.add(pair.app);
        }
        savePrefs(toExclude);
    }
}
