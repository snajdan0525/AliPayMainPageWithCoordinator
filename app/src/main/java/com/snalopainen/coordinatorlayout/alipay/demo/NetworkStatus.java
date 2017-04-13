package com.snalopainen.coordinatorlayout.alipay.demo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by jinyan on 16/8/26.
 */
public class NetworkStatus {

    public final static int NET_TYPE_WIFI = 0;
    public final static int NET_TYPE_MOBIL = 1;
    public final static int NET_TYPE_NO_NET = 3;

    public static int netType(Context ctx) {
        ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info == null || !info.isAvailable()) {
            // 无网络可用
            return NET_TYPE_NO_NET;
        }
        final int type = info.getType();
        if (type == ConnectivityManager.TYPE_WIFI || type == ConnectivityManager.TYPE_ETHERNET
                || type == ConnectivityManager.TYPE_WIMAX) {
            // 0WIFI
            return NET_TYPE_WIFI;
        }
        if (type == ConnectivityManager.TYPE_MOBILE) {
            // 1 手机
            return NET_TYPE_MOBIL;
        }
        // 默认无网络
        return NET_TYPE_NO_NET;
    }

    public static boolean isNetworkConnected(Context context) {
        return netType(context) != NET_TYPE_NO_NET;
    }

}