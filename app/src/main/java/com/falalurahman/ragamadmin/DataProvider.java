package com.falalurahman.ragamadmin;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class DataProvider {

    public static final String RAGAM_WEBSITE_PREFIX = "https://ragam.org.in/AndroidAdmin/";

    public static final String GET_WORKSHOP_REGISTRATION = RAGAM_WEBSITE_PREFIX + "getWorkshopRegistration.php";
    public static final String GET_WORKSHOP_PRESENT = RAGAM_WEBSITE_PREFIX + "getWorkshopPresent.php";
    public static final String GET_WORKSHOP_USER = RAGAM_WEBSITE_PREFIX + "getWorkshopUser.php";
    public static final String SET_WORKSHOP_ADD = RAGAM_WEBSITE_PREFIX + "setWorkshopAdd.php";

    public static final String GET_EVENT_REGISTRATION = RAGAM_WEBSITE_PREFIX + "getEventRegistration.php";
    public static final String GET_EVENT_PRESENT = RAGAM_WEBSITE_PREFIX + "getEventPresent.php";
    public static final String SET_EVENT_DELETE = RAGAM_WEBSITE_PREFIX + "setEventDelete.php";
    public static final String GET_EVENT_TEAM_MEMBERS = RAGAM_WEBSITE_PREFIX + "getEventTeamMembers.php";
    public static final String SET_EVENT_TEAM_MEMBER_DELETE = RAGAM_WEBSITE_PREFIX + "setEventTeamMemberDelete.php";
    public static final String GET_EVENT_USER_1 = RAGAM_WEBSITE_PREFIX + "getEventUser1.php";
    public static final String GET_EVENT_USER_2 = RAGAM_WEBSITE_PREFIX + "getEventUser2.php";
    public static final String GET_EVENT_USER_3 = RAGAM_WEBSITE_PREFIX + "getEventUser3.php";
    public static final String SET_EVENT_ADD_TEAM_MEMBER = RAGAM_WEBSITE_PREFIX + "setEventAddTeamMember.php";
    public static final String SET_EVENT_SAVE_TEAM_MEMBERS = RAGAM_WEBSITE_PREFIX + "setEventSaveTeamMembers.php";

    public static final String GET_USER_DETAILS = RAGAM_WEBSITE_PREFIX + "getUserDetails.php";
    public static final String SET_USER_DETAILS = RAGAM_WEBSITE_PREFIX + "setUserDetails.php";

    public static final String GET_WORKSHOP_REGISTRATION_TAG = "GetWorkshopRegistrationTag";
    public static final String GET_WORKSHOP_PRESENT_TAG = "GetWorkshopPresentTag";
    public static final String GET_WORKSHOP_USER_TAG = "GetWorkshopUserTag";
    public static final String SET_WORKSHOP_ADD_TAG = "SetWorkshopUserTag";

    public static final String GET_EVENT_REGISTRATION_TAG = "GetEventRegistrationTag";
    public static final String GET_EVENT_PRESENT_TAG = "GetEventPresentTag";
    public static final String SET_EVENT_DELETE_TAG = "SetEventDeleteTag";
    public static final String GET_EVENT_TEAM_MEMBERS_TAG = "GetEventTeamMembersTag";
    public static final String SET_EVENT_TEAM_MEMBER_DELETE_TAG = "SetEventTeamMemberDeleteTag";
    public static final String GET_EVENT_USER_TAG = "GetEventUserTag";
    public static final String SET_EVENT_ADD_TEAM_MEMBER_TAG = "SetEventsAddTeamMemberTag";
    public static final String SET_EVENT_SAVE_TEAM_MEMBERS_TAG = "SetEventSaveTeamMembersTag";

    public static final String GET_USER_DETAILS_TAG = "GetUserDetails";
    public static final String SET_USER_DETAILS_TAG = "SetUserDetails";

    private static DataProvider mInstance;
    private RequestQueue mRequestQueue;
    private static Context mContext;

    private DataProvider(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized DataProvider getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DataProvider(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public void cancelRequest(String TAG) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
    }

}
