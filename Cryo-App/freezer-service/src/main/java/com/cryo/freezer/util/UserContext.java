package com.cryo.freezer.util;

public class UserContext {

    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> emailHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> mobileNumberHolder = new ThreadLocal<>();

    public static void setUserId(String userId) {
        userIdHolder.set(userId); // this is ownerUserId like C00006
    }

    public static String getUserId() {
        return userIdHolder.get();
    }

    public static void setEmail(String email) {
        emailHolder.set(email);
    }

    public static String getEmail() {
        return emailHolder.get();
    }

    public static void setMobileNumber(String mobileNumber) {
        mobileNumberHolder.set(mobileNumber);
    }

    public static String getMobileNumber() {
        return mobileNumberHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
        emailHolder.remove();
        mobileNumberHolder.remove();
    }
}
