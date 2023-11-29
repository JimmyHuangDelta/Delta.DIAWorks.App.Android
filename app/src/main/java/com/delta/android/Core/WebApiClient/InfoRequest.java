package com.delta.android.Core.WebApiClient;

import java.util.HashMap;

public class InfoRequest {
    public String ID;
    public String ModuleName;
    public String FunctionName;

    public InfoRequest(String id, String moduleName, String functionName) {
        ModuleName = moduleName;
        ID = id;
        FunctionName = functionName;
    }

    public HashMap<String, Object> Parameters = new HashMap<String, Object>();

    public void AddParams(String key, Object value) {
        Parameters.put(key, value);
    }
}
