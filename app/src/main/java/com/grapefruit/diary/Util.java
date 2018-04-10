package com.grapefruit.diary;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Util {

    public static String getDate(Long time) {
        return new SimpleDateFormat("yyyy년 MM월 dd일 E요일 a hh:mm", Locale.KOREA).format(time);
    }

    public static Drawable setBackArrowColor(Context context) {
        Drawable upArrow = ContextCompat.getDrawable(context, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        return upArrow;
    }

    public static boolean isNetwork(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
