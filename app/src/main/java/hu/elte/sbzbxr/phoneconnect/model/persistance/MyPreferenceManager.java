package hu.elte.sbzbxr.phoneconnect.model.persistance;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;

import hu.elte.sbzbxr.phoneconnect.R;

public class MyPreferenceManager {

    private static String readSharedPref(Context context, @StringRes int resId, @StringRes int default_resId){
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        return p.getString(context.getString(resId),context.getString(default_resId));
    }

    private static SharedPreferences writeSharedPref(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getAddress(Context context){
        return readSharedPref(context,R.string.ip_address,R.string.default_ip_address);
    }

    public static String getPort(Context context){
        return readSharedPref(context,R.string.port,R.string.default_port);
    }

    public static void saveAddressAndPort(Context context, String ipAddress, String port){
        writeSharedPref(context).edit().putString(context.getString(R.string.ip_address),ipAddress).putString(context.getString(R.string.port),port).apply();
    }

    public static Long getNetworkSpeedLimit(Context context){
        try{
            return Long.valueOf(readSharedPref(context,R.string.network_speed,R.string.default_network_speed));
        }catch (NumberFormatException n){
            return 100L;
        }
    }

}
