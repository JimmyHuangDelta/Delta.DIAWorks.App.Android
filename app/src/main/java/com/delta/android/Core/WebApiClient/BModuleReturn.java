package com.delta.android.Core.WebApiClient;

import java.util.HashMap;

import com.delta.android.Core.DataTable.DataTable;

public class BModuleReturn {

    public boolean getSuccess() {
        return Success;
    }

    private boolean Success;

    public HashMap<String, Object> getAckError() {
        return AckError;
    }

    private HashMap<String, Object> AckError;

    public HashMap<String, HashMap<String, DataTable>> getReturnJsonTables() {
        return ReturnJsonTables;
    }

    public HashMap<String, HashMap<String, Object>> getReturnList() {
        return ReturnList;
    }

    private HashMap<String, HashMap<String, DataTable>> ReturnJsonTables;
    private HashMap<String, HashMap<String, Object>> ReturnList;

    public BModuleReturn(boolean success, HashMap<String, Object> ackError, HashMap<String, HashMap<String, DataTable>> returnJsonTables, HashMap<String, HashMap<String, Object>> returnList) {
        Success = success;
        AckError = ackError;
        ReturnJsonTables = returnJsonTables;
        ReturnList = returnList;
    }
}
