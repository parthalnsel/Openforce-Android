package com.openforce.providers;

import android.content.Context;
import android.text.TextUtils;

import com.andreacioccarelli.cryptoprefs.CryptoPrefs;
import com.google.gson.Gson;
import com.openforce.model.User;
import com.openforce.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import kotlin.Pair;
import kotlin.PublishedApi;

public class SecureSharedPreference {

    private static final String TAG = "SecureSharedPreference";

    private static final String SHARED_PREF_USER_INFO = "UserInfoSharedPref";

    public static final String SECURE_PREFERENCE_FILE_NAME_UID = "secure_preference_uid";
    public static final String SECURE_PREFERENCE_FILE_NAME_PIN = "secure_preference_pin";
    public static final String SECURE_PREF_CURRENT_JOB = "secure_preference_current_job";
    public static final String SECURE_PREFERENCE_STRIPE_INFO = "secure_preference_uid";


    private CryptoPrefs securePrefs;
    private Context context;
    private Gson gson;

    public SecureSharedPreference(Context context, String password, Gson gson, boolean isPasswordPin) {
        this.context = context;
        this.gson = gson;
        securePrefs = new CryptoPrefs(context, isPasswordPin ? SECURE_PREFERENCE_FILE_NAME_PIN : SECURE_PREFERENCE_FILE_NAME_UID, password, true);
    }

    public void handleSecurePasswordChange(String newPassword) {
        Map<String, String> propertyToMigrate = new HashMap<>();
        for (Pair<String, String> keyValuePair : securePrefs.getAllPrefsList()) {
            propertyToMigrate.put(keyValuePair.component1(), keyValuePair.component2());
        }
        securePrefs.erase();
        Utils.deleteSharedPreferenceFile(context, SECURE_PREFERENCE_FILE_NAME_UID);
        Utils.deleteSharedPreferenceFile(context, SECURE_PREFERENCE_FILE_NAME_PIN);
        CryptoPrefs cryptoPrefs = new CryptoPrefs(context, SECURE_PREFERENCE_FILE_NAME_PIN, newPassword, true);
        for (Map.Entry<String, String> entry : propertyToMigrate.entrySet()) {
            cryptoPrefs.put(entry.getKey(), entry.getValue());
        }

        securePrefs = cryptoPrefs;
    }

    public void setUserInfo(User userInfo) {
        securePrefs.put(SHARED_PREF_USER_INFO, gson.toJson(userInfo));
    }

    public void setStripeInfo(String stripe_account){
        securePrefs.put(SECURE_PREFERENCE_STRIPE_INFO, stripe_account);
    }

    public String getStripeInfo() {
        return securePrefs.getString("SECURE_PREFERENCE_STRIPE_INFO","");
//        return SECURE_PREFERENCE_STRIPE_INFO;
    }

    public User getUserInfo() {
        User user = null;
        String userJson = securePrefs.getString(SHARED_PREF_USER_INFO, "");
        if (!TextUtils.isEmpty(userJson)) {
            user = gson.fromJson(userJson, User.class);
        }
        return user;
    }

    public void removePassword(){
        securePrefs.erase();
    }
}
