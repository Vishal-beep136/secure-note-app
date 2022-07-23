package com.vishal.secure_note.util;

/**
 * Created by Vishal on 12, Jul, 2022
 */
public class Constant {
    public static final String ROOM_DB_NAME = "SECURE_NOTE_DB";

    // Constants for Add Note Mode
    public static final int NOTE_EDIT_MODE = 1;
    public static final int NOTE_ADD_MODE = 2;

    // Constants for label or priority
    public static final int NORMAL_LABEL = 0, NEEDED_LABEL = 1, IMPORTANT_LABEL = 2;
    // Constants to make note private
    public static final int MOBILE_PASSWORD = 0, APPS_PASSWORD = 1, CUSTOM_PASSWORD = 2;


    // App Preference Constant
    public static final String APP_PASSWORD_KEY = "app_password";
    public static final String CUSTOM_LOGIN_PASSWORD_KEY = "custom_password";
    public static final String PASSWORD_METHOD_KEY = "password_method";
    public static final String NOT_APP_PASSWORD_SET = "Not set";
    public static final int APP_PASSWORD_SET_REQ_CODE = 18;

    public static final int PRIVACY_CODE = 10;
    public static final int TERMS_OF_USE_CODE = 9;

    // filter options
    public static final int FILTER_BY_LATEST_NOTE = 0;
    public static final int FILTER_BY_PRIVATE_NOTE = 1;
    public static final int FILTER_BY_OLDEST_NOTE = 2;
    public static final int FILTER_BY_IMPORTANT_NOTE = 3;
    public static final int FILTER_BY_NEEDED_NOTE = 4;
    public static final int FILTER_BY_PUBLIC_NOTE = 5;


}
