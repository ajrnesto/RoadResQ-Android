package com.vulcanizingapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.vulcanizingapp.AuthenticationActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Utils {
    public static String capitalizeEachWord(String str) {
        StringBuilder builder = new StringBuilder();
        for (String s : str.split(" ")) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
            builder.append(cap + " ");
        }
        return builder.toString();
    }

    public static String indentParagraphs(String text) {
        StringBuilder indentedParagraphs = new StringBuilder();
        String[] textArray = text.split("\n\n");
        for (String txt : textArray) {
            indentedParagraphs.append("\t\t\t").append(txt).append("\n\n");
        }
        indentedParagraphs.setLength(indentedParagraphs.length() - 1);
        return indentedParagraphs.toString();
    }
    public static String initialsFromName(String name) {
        StringBuilder initials = new StringBuilder();
        for (String s : name.split(" ")) {
            initials.append(s.charAt(0));
        }
        return initials.toString();
    }

    public static String addressBuilder(String locality, String subAdminArea) {
        StringBuilder addressBuilder = new StringBuilder();
        if (locality.isEmpty()) {
            addressBuilder.append("Unspecifiable, ");
        }
        else {
            addressBuilder.append(locality).append(", ");
        }

        if (subAdminArea.isEmpty()) {
            addressBuilder.append("Unspecifiable");
        }
        else {
            addressBuilder.append(subAdminArea);
        }

        return addressBuilder.toString();
    }

    public static String addressSeparator(String address) {
        StringBuilder addressStringBuilder = new StringBuilder();
        String[] addressArray = address.split("\\s*,\\s*");
        for (String addr : addressArray) {
            addressStringBuilder.append(addr).append(",").append("\n");
        }
        addressStringBuilder.setLength(addressStringBuilder.length() - 2);
        return addressStringBuilder.toString();
    }

    public static void hideKeyboard(Activity activity) {
        View view1 = activity.getCurrentFocus();
        if (view1 != null) {
            InputMethodManager imm = (InputMethodManager)activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
        }
    }

    public static int randomNumberBetween(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
    public static void basicDialog(Context context, String title, String button){
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
        materialAlertDialogBuilder.setTitle(title);
        materialAlertDialogBuilder.setPositiveButton(button, (dialogInterface, i) -> { });
        materialAlertDialogBuilder.show();
    }
    public static void simpleDialog(Context context, String title, String message, String button){
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
        materialAlertDialogBuilder.setTitle(title);
        materialAlertDialogBuilder.setMessage(message);
        materialAlertDialogBuilder.setPositiveButton(button, (dialogInterface, i) -> { });
        materialAlertDialogBuilder.show();
    }
    public static void loginRequiredDialog(Context context, BottomNavigationView bottom_navbar, String message){
        MaterialAlertDialogBuilder dialogLoginRequired = new MaterialAlertDialogBuilder(context);
        dialogLoginRequired.setTitle("Log In");
        dialogLoginRequired.setMessage(message);
        dialogLoginRequired.setPositiveButton("Log in", (dialogInterface, i) -> {
            context.startActivity(new Intent(context, AuthenticationActivity.class));
            ((Activity)context).finish();
        });
        dialogLoginRequired.setNeutralButton("Back", (dialogInterface, i) -> { });
        dialogLoginRequired.setOnDismissListener(dialogInterface -> bottom_navbar.getMenu().getItem(1).setChecked(true));
        dialogLoginRequired.show();
    }

    public static class Cache {
        public static void removeKey(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().remove(key).apply();
        }

        public static String getString(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getString(key, "");
        }

        public static void setString(Context context, String key, String value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(key, value).apply();
        }

        public static int getInt(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getInt(key, 0);
        }

        public static void setInt(Context context, String key, int value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putInt(key, value).apply();
        }

        public static double getDouble(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            return Double.longBitsToDouble(sharedPreferences.getLong(key, 0));
        }

        public static void setDouble(Context context, String key, double value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putLong(key, Double.doubleToRawLongBits(value)).apply();
        }

        public static long getLong(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getLong(key, 0);
        }

        public static void setLong(Context context, String key, long value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putLong(key, value).apply();
        }

        public static boolean getBoolean(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean(key, false);
        }

        public static void setBoolean(Context context, String key, boolean value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(key, value).apply();
        }

        public static Set<String> getStringSet(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getStringSet(key, new HashSet<>());
        }

        public static void setStringSet(Context context, String key, Set<String> set){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putStringSet(key, set).apply();
        }
    }

    public static class DoubleFormatter {

        public static String currencyFormat(double dbl){
            if (dbl == 0) {
                return "0.00";
            }
            else {
                DecimalFormat formatter = new DecimalFormat("#,###.00");
                return formatter.format(dbl);
            }
        }

    }
}
