package com.delta.android.Core.WebApiClient;

import java.util.List;

public class LoginUserReturn {
    public String UserKey;
    public String UserID;
    public String UserName;
    public int UserSerialKey;
    public Boolean LoginSuccess;
    public String Error;
    public List<MesFunction> Functions;

    // 20220623 Ikea 傳回廠別相關資訊
    public List<FactoryInfo> Factories;
    public String FactoryKey;
    public String FactoryID;
    public String FactoryName;
}
