package com.openforce.utils;

import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import java.util.HashMap;

public final class ValidationUtils {

    private static final HashMap<Character, Character> SPECIAL_CHARS = new HashMap<Character, Character>() {
        {
            put('@', '@');
            put('#', '#');
            put('$', '$');
            put('%', '%');
            put('^', '^');
            put('&', '&');
            put('+', '+');
            put('=', '=');
        }

    };

    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }

    public static boolean isValidPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }

        if (password.length() < 8) {
            return false;
        }

        boolean capitalFlag = false;
        boolean lowerCaseFlag = false;
        boolean numberFlag = false;
        for(int i=0;i < password.length();i++) {
            char ch = password.charAt(i);
            if( Character.isDigit(ch)) {
                numberFlag = true;
            }
            else if (Character.isUpperCase(ch)) {
                capitalFlag = true;
            } else if (Character.isLowerCase(ch)) {
                lowerCaseFlag = true;
            }
        }

        if (!capitalFlag || !lowerCaseFlag || !numberFlag) {
            return false;
        }

        return true;
    }

    public static boolean isValidNumber(String number) {
        return PhoneNumberUtils.isGlobalPhoneNumber(number);
    }
}
