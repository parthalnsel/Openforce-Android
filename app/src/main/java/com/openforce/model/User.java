package com.openforce.model;

import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.openforce.utils.ConstantUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    private static final String TAG = "User";

    // need to be public for firebase
    public String uid;
    public String firstName;
    public String lastName;
    public String email;
    public String cvImageUrl = "";
    public String balance ="0.00";

    public String type = "employee";
    public String pin;
    public Double overallAverage;
    public List<Reference> references;
    public List<Skill> skills;
    public String profileImg;
    public String imagePublicId;
    public List<StripeInfo> stripe_info;
    public User(String firstName, String lastName, String email, String uid) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.uid = uid;
    }

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", type='" + type + '\'' +
                ", pin='" + pin + '\'' +
                ", overallAverage=" + overallAverage +
                ", references=" + references +
                ", stripe_info=" + stripe_info +
                ", skills=" + skills +
                ", profileImg='" + profileImg + '\'' +

                '}';
    }

    public static User fromJSON(JSONObject jsonObject, Gson gson) {
        System.out.println("parthaObject: " +jsonObject.toString());
        User user = new User();
        user.uid = jsonObject.optString("uid");
        user.balance = jsonObject.optString("balance");
        user.cvImageUrl = jsonObject.optString("cvImageUrl");
        user.firstName = jsonObject.optString("firstName");
        user.lastName = jsonObject.optString("lastName");
        user.email = jsonObject.optString("email");

        user.type = jsonObject.optString("type");
        user.pin = jsonObject.optString("pin");
        user.profileImg = jsonObject.optString("profileImg", null);
        ConstantUtil.CvImage=jsonObject.optString("cvImageUrl");
        user.overallAverage = jsonObject.optDouble("overallAverage", -1) != -1 ? jsonObject.optDouble("overallAverage") : null;
        user.imagePublicId = jsonObject.optString("imagePublicId", null);

        JSONObject references = jsonObject.optJSONObject("references");
        if (references != null) {
            Map<String, Reference> map = new Gson().fromJson(
                    references.toString(), new TypeToken<HashMap<String, Reference>>() {}.getType());
                user.references = new ArrayList<>(map.values());
        }

        JSONArray stripe_info_array = jsonObject.optJSONArray("stripe_info");
        Log.d(TAG, "fromJSON: stripe" + stripe_info_array.toString());

        List<StripeInfo> info = new ArrayList<>();
        if (stripe_info_array.optJSONObject(0)!=null){
           JSONObject stripeJSON = stripe_info_array.optJSONObject(0);
           StripeInfo stripeInfo = new StripeInfo(stripeJSON.optString("name"),
                   stripeJSON.optString("user_id"),
                   stripeJSON.optString("token"));
           info.add(stripeInfo);
        }

        user.stripe_info = info;


        JSONArray jsonArray = jsonObject.optJSONArray("skills");
        List<Skill> skills = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject skillJSON = jsonArray.optJSONObject(i);
                Skill skill = new Skill(skillJSON.optString("id"),
                        skillJSON.optString("name"),
                        skillJSON.optString("payRate"),
                        skillJSON.optInt("level"));
                skills.add(skill);
            }
        }
        user.skills = skills;
        return user;
    }
}
