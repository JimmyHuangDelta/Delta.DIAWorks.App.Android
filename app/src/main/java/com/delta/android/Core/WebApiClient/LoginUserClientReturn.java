package com.delta.android.Core.WebApiClient;

import java.util.List;

public class LoginUserClientReturn {
    public boolean IsSuccess;
    public String ErrorMessage;
    public String StackTrace;
    public String ErrorCode;
    public List<String> ErrorParam;
    public boolean IsTokenExpired;
    public boolean OccurHttp401;
    public String Data;
    public String OtherData;
}
