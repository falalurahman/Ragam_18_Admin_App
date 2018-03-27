package com.falalurahman.ragamadmin;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceController {

    public static String APP_NAME = "RagamAdmin";
    public static String COMMITEE_NAME_CONSTANT = "Committee";
    public static String EVENT_ID_CONSTANT = "EventId";
    public static String EVENT_NAME_CONSTANT = "EventName";

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor sharedPreferencesEditor;

    public static void initializeSharedPreferences(Activity activity){
        if(sharedPreferences == null){
            sharedPreferences = activity.getSharedPreferences(APP_NAME,Context.MODE_PRIVATE);
        }
        if(sharedPreferencesEditor == null){
            sharedPreferencesEditor = sharedPreferences.edit();
        }
    }

    public static String getSharedPreference(String key){
        return sharedPreferences.getString(key,"");
    }

    public static int getIntSharedPreference(String key){
        return sharedPreferences.getInt(key,-1);
    }

    public static void putSharedPreference(String key, String value){
        sharedPreferencesEditor.putString(key, value);
        sharedPreferencesEditor.commit();
    }

    public static void putIntSharedPreference(String key, int value){
        sharedPreferencesEditor.putInt(key, value);
        sharedPreferencesEditor.commit();
    }
}
