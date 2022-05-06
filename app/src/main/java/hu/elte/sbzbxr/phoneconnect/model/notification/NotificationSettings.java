package hu.elte.sbzbxr.phoneconnect.model.notification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import hu.elte.sbzbxr.phoneconnect.ui.ConnectedFragment;
import hu.elte.sbzbxr.phoneconnect.ui.LoadingDialog;
import hu.elte.sbzbxr.phoneconnect.ui.notifications.NotificationDialog;
import hu.elte.sbzbxr.phoneconnect.ui.notifications.SaveList;

public class NotificationSettings implements SaveList {
    public final static String PREF_FILE_NOTIFICATION = "hu.elte.sbzbxr.phoneconnect.ui.notifications."+"notificationsNotToSend";
    public final static String SET_OF_EXCEPTIONS = "exceptions";
    private final ConnectedFragment connectedFragment;
    private final List<NotificationPair> notificationPairs;

    public NotificationSettings(ConnectedFragment connectedFragment) {
        this.connectedFragment=connectedFragment;
        notificationPairs = new ArrayList<>(200);
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
        connectedFragment.getViewModel().notificationFilter.setAppsToExclude(appsToExclude);
    }

    //From: https://developer.android.com/guide/topics/ui/dialogs#AddingAList
    public void showDialog() {
        LoadingDialog loadingDialog = new LoadingDialog("Loading...");
        loadingDialog.show(connectedFragment.getParentFragmentManager(),"loading");
        new Thread(()->{
            final PackageManager pm = connectedFragment.requireActivity().getPackageManager();
            List<CharSequence> appNames = getAppNames2(pm).stream()
                    .sorted()
                    .distinct()
                    .collect(Collectors.toList());
            List<String> storedExceptions = getDisabledNotifications();
            for(CharSequence ch : appNames){
                notificationPairs.add(new NotificationPair(ch.toString(),!storedExceptions.contains(ch.toString())));
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                // Code here will run in UI thread
                loadingDialog.dismiss();
                new NotificationDialog(notificationPairs,NotificationSettings.this).show(connectedFragment.getParentFragmentManager(),"notificationSettings");
            });
        }).start();
    }

    public List<CharSequence> getAppNames(final PackageManager pm){
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<CharSequence> appNames = packages.stream()
                .filter(ai->ai.flags!=ApplicationInfo.FLAG_SYSTEM)
                .filter(ai->ai.flags!=ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)
                .map(ai -> {
                    try {
                        Log.e("getApplicationLabel of :",ai.className);
                        return pm.getApplicationLabel(ai);
                    }catch (Exception e){
                        e.printStackTrace();
                        return "Unknown";
                    }
                })
                .distinct()
                .collect(Collectors.toList());
        return appNames;
    }

    //Based on: https://www.geeksforgeeks.org/different-ways-to-get-list-of-all-apps-installed-in-your-android-phone/
    public List<CharSequence> getAppNames2(final PackageManager pm){
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        // get list of all the apps installed
        List<ResolveInfo> ril = pm.queryIntentActivities(mainIntent, 0);

        // get size of ril and create a list
        ArrayList<CharSequence> apps = new ArrayList<>(ril.size());
        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                // get package
                Resources res = null;
                try {
                    res = pm.getResourcesForApplication(ri.activityInfo.applicationInfo);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }
                // if activity label res is found
                String name;
                if (ri.activityInfo.labelRes != 0) {
                    name = res.getString(ri.activityInfo.labelRes);
                } else {
                    name = ri.activityInfo.applicationInfo.loadLabel(
                            pm).toString();
                }
                apps.add(name);
            }
        }
        return apps;
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
