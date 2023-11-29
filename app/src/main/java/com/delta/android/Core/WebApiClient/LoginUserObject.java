package com.delta.android.Core.WebApiClient;

public class LoginUserObject {
    private String UserID;
    private String Password;
    private String LoginSource = "Android";
    private String ResourceType;

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

    public String getResourceType() {
        return ResourceType;
    }

    public void setResourceType(String resourceType) {
        ResourceType = resourceType;
    }
}
