package com.delta.android.Core.WebApiClient;

public class LoginUserForPortalObject {
    private String UserID;
    private String Password;
    private boolean IsPermanent;
    private String ClientIP;
    private String ClientName;

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public boolean getIsPermanent() {
        return IsPermanent;
    }

    public void setIsPermanent(boolean isPermanent) {
        IsPermanent = isPermanent;
    }

    public String getClientIP() {
        return ClientIP;
    }

    public void setClientIP(String clientIP) {
        ClientIP = clientIP;
    }

    public String getClientName() {
        return ClientName;
    }

    public void setClientName(String clientName) {
        ClientName = clientName;
    }
}
