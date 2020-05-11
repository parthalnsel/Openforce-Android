package com.openforce.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.openforce.R;
import com.openforce.model.User;
import com.openforce.model.BoundingBoxCoordinate;
import com.openforce.widget.TextDrawable;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class Utils {

    private static final String TAG = "Utils";

    public static final double LONDON_LATITUDE = 51.509865;
    public static final double LONDON_LONGITUDE = -0.118092;

    public static <T> Map<String, T> convertListToMap(List<T> list) {
        HashMap<String, T> hashMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            hashMap.put(i+"", list.get(i));
        }
        return hashMap;
    }


    public static boolean isUserVetted(FirebaseAuth auth, User user) {
        if (auth.getCurrentUser() == null || user == null) {
            return false;
        }

        if (TextUtils.isEmpty(auth.getCurrentUser().getPhoneNumber())) {
            return false;
        }

        if (TextUtils.isEmpty(user.pin)) {
            return false;
        }

        if (user.references == null || user.references.isEmpty()) {
            return false;
        }

        if (user.skills == null || user.skills.isEmpty()) {
            return false;
        }
        if (user.stripe_info == null || user.stripe_info.isEmpty()) {
            return false;
        }

        return true;
    }

    public static void deleteSharedPreferenceFile(Context context, String filename) {
        try {
            File oldFile = new File(context.getCacheDir().getParent() + "/shared_prefs/" + filename + ".xml");
            oldFile.delete();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting sharedPreference", e);
        }
    }

    public static BoundingBoxCoordinate calculateBoundingBoxFromCoordinate(double latitude, double longitude, int distanceMeter) {

        // latitude left to right
        // longitude top to bottom

        double earth = 6378.137;  //radius of the earth in kilometer
        double kmPerDegree = 111.32;   // number of km per degree = ~111km (111.32 in google maps, but range varies between 110.567km at the equator and 111.699km at the poles)

        double pi = Math.PI;
        double m = (1 / ((2 * pi / 360) * earth)) / 1000;  //1 meter in degree

        double newBiggerLatitude = latitude + (distanceMeter * m);
        double newSmallerLatitude = latitude + ((distanceMeter * -1) * m);

        double newBiggerLongitude = longitude + (distanceMeter * m) / Math.cos(latitude * (pi / 180));
        double newSmallerLongitude = longitude + ((distanceMeter * -1) * m) / Math.cos(latitude * (pi / 180));

        LatLng topRight = new LatLng(newBiggerLatitude, newBiggerLongitude);
        LatLng topLeft = new LatLng(newSmallerLatitude, newBiggerLongitude);

        LatLng bottomRight = new LatLng(newBiggerLatitude, newSmallerLongitude);
        LatLng bottomLeft = new LatLng(newSmallerLatitude, newSmallerLongitude);

        BoundingBoxCoordinate boundingBoxCoordinate = new BoundingBoxCoordinate(topRight, topLeft, bottomLeft, bottomRight);
        Log.e("TAG", "POINT 1 - Top Right: " + newBiggerLatitude + "," + newBiggerLongitude); // top right
        Log.e("TAG", "POINT 2 - Top Left: " + newBiggerLatitude + "," + newSmallerLongitude); // top left
        Log.e("TAG", "POINT 3 - Bottom Right: " + newSmallerLatitude + "," + newBiggerLongitude); // bottom right
        Log.e("TAG", "POINT 4 - Bottom left: " + newSmallerLatitude + "," + newSmallerLongitude); // bottom left
        return boundingBoxCoordinate;
    }

    public static Calendar getTodayDate() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    public static String timestampToFormattedDate(long timestamp) {
        Calendar c = fromTimeStamp(timestamp);
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return format1.format(c.getTime());
    }

    public static String timestampToNormalDate(long timestamp) {
        Calendar c = fromTimeStamp(timestamp);
        SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return format1.format(c.getTime());
    }

    public static String timestampToDate(long timestamp) {
        Calendar c = fromTimeStamp(timestamp);
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return format1.format(c.getTime());
    }

    public static String calculateDateFromTimestamp(long timestampStart , long timestampEnd ) {
        Calendar c1 = fromTimeStamp(timestampStart);
        Calendar c2 = fromTimeStamp(timestampEnd);
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDateStr =format1.format(c1.getTime()) ;
        String endDateStr = format1.format(c2.getTime());

        Date start_date = null , end_date = null ;
        long  diffDay= 0;
        try {
            start_date =format1.parse(startDateStr);
            end_date = format1.parse(endDateStr);

            long diff = end_date.getTime() - start_date.getTime();
            diffDay  = diff/(24 * 60 * 60 * 1000);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return String.valueOf(diffDay +1 );
    }




    public static String timestampToFormattedDatePastJob(long timestamp) {
        Calendar c = fromTimeStamp(timestamp);
        SimpleDateFormat format1 = new SimpleDateFormat("d'"+ getDayNumberSuffix(c.get(Calendar.DAY_OF_MONTH)) + "' MMMM yyyy" , Locale.getDefault());
        return format1.format(c.getTime());
    }

    public static String timestampToFormattedDateTime(long timestamp) {
        Calendar c = fromTimeStamp(timestamp);
        SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy hh:mm", Locale.getDefault());
        return format1.format(c.getTime());
    }

    public static Calendar fromTimeStamp(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        return c;
    }

    public static boolean isBetween(Date dateToCheck, Date startDate, Date endDate) {
        return dateToCheck.compareTo(startDate) >= 0 && dateToCheck.compareTo(endDate) <= 0;
    }

    public static AlertDialog getConfirmDialog(String title, String message,
                                         DialogInterface.OnClickListener positiveClick, DialogInterface.OnClickListener negativeClick, Context context) {

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.button_cancel, negativeClick)
                .setPositiveButton(R.string.button_confirm, positiveClick).create();

        return alertDialog;
    }

    public static String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static TextDrawable getPlaceholderForProfile(String name, Context context) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        String[] arrayOfNames = name.trim().split(" ");
        StringBuilder initials = new StringBuilder();
        for (String arrayOfName : arrayOfNames) {
            if (arrayOfName.length() > 0) {
                initials.append(arrayOfName.substring(0,1).toUpperCase());
            }
            if (initials.length() == 2) {
                break;
            }
        }

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .withBorder(context.getResources().getDimensionPixelSize(R.dimen.border_profile), ContextCompat.getColor(context, R.color.placeholder_border_profile))
                .textColor(ContextCompat.getColor(context, R.color.sun_yellow))
                .endConfig()
                .buildRound(initials.toString(), ContextCompat.getColor(context, R.color.background_profile));

        return drawable;
    }
}
