package com.delta.android.Core.Common;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.delta.android.Core.Activity.CrashExceptionActivity;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.FactoryInfo;
import com.delta.android.Core.WebApiClient.MesFunction;
import com.delta.android.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Global extends Application {

    private static Global _context;
    private String _url;//Server的url
    private String _PortalUrl;//Portal的url
    private String _SSOUrl;//SSO Url
    private String _apiConnName;//App連線的ashx
    private String _apiUpdateName;//App更新的ashx
    private int _connectionTimeOut;//連線timeout時間
    private String _resourceType;//語系
    private String _ClientIp; //20201019 archie 設備IP

    private String _userKey;
    private int _userSerialKey;
    private String _userID;
    private String _passWord;
    private String _userName;
    private String _factoryKey; // 20220623 Ikea 記錄當前 FactoryKey
    private String _factoryId; // 20220623 Ikea 記錄當前 Factory
    private String _factoryName; // 20220623 Ikea 記錄當前 FactoryName
    private String[] _functionTypes;
    private String[] _functionSubTypes;
    private HashMap<String, List<MesFunction>> _functions;
    private List<? extends Map<String, Object>> _factories; // 220617 Ikea 記錄廠別清單
    private String _token; //20201019 archie SSO新增
    private int _statusCode = 0; //20201020 archie SSO新增
    private boolean _allowedNotification; // 允許通知

    private SharedPreferences _sharedPreferences;//儲存關閉程式要保留的資料在xml檔裡
    private boolean _debugMode;//是否為debug模式

    @Override
    public void onCreate() {
        super.onCreate();
        _context = this;
        Thread.setDefaultUncaughtExceptionHandler(new CrashExceptionHandler(this));
        //Context context = getApplicationContext();

        _sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
        _debugMode = getResources().getBoolean(R.bool.DEBUG_MODE);
        _functionTypes = getResources().getStringArray(R.array.arrayFunctionType);
        _functionSubTypes = getResources().getStringArray(R.array.arrayFunctionSubType);
        _connectionTimeOut = getResources().getInteger(R.integer.DEFAULT_CONNECTION_TIMEOUT);
        _apiConnName = getResources().getString(R.string.DEFAULT_API_CONNECTION_NAME);
        _apiUpdateName = getResources().getString(R.string.DEFAULT_API_UPDATE_NAME);
        //20201019 archie SSO新增
        _token = "";
        _ClientIp = "";
        _statusCode = 0;

        if (_debugMode) {
            _url = getResources().getString(R.string.DEFAULT_URL);
            _PortalUrl = getResources().getString(R.string.DEFAULT_URL);
			//20201019 archie SSO新增
            _SSOUrl = getResources().getString(R.string.DEFAULT_SSO_URL);
            _resourceType = getResources().getString(R.string.DEFAULT_LANGUAGE);
            _userKey = getResources().getString(R.string.DEBUG_USER_KEY);
            _userID = getResources().getString(R.string.DEBUG_USER_ID);
            _userSerialKey = getResources().getInteger(R.integer.DEBUG_USER_SERIAL_KEY);
            _allowedNotification = getResources().getBoolean(R.bool.DEFAULT_ALLOWED_NOTIFICATION);
            _factoryKey = getResources().getString(R.string.DEFAULT_FACTORY_KEY);
            _factoryId = getResources().getString(R.string.DEFAULT_FACTORY_ID);
            _factoryName = getResources().getString(R.string.DEFAULT_FACTORY_NAME);

        } else {
            _url = _sharedPreferences.getString("Url", getResources().getString(R.string.DEFAULT_URL));//如果沒有URL,後面為預設值
            _PortalUrl = _sharedPreferences.getString("PortalUrl",getResources().getString(R.string.DEFAULT_URL));
			//20201019 archie SSO新增
            _SSOUrl = _sharedPreferences.getString("SSOUrl", getResources().getString(R.string.DEFAULT_SSO_URL));//如果沒有URL,後面為預設值
            _resourceType = _sharedPreferences.getString("ResourceType", getResources().getString(R.string.DEFAULT_LANGUAGE));//如果沒有ResourceType,後面為預設值
            _allowedNotification = _sharedPreferences.getBoolean("AllowedNotification", false); // 如果沒有AllowedNotification, 後面為預設值
            _factoryKey = _sharedPreferences.getString("FactoryKey", ""); // 如果沒有FactoryKey, 後面為預設值
            _factoryId = _sharedPreferences.getString("FactoryId", ""); // 如果沒有FactoryId, 後面為預設值
            _factoryName = _sharedPreferences.getString("FactoryName", ""); // 如果沒有FactoryName, 後面為預設值
        }
    }

    public void Logout() {
        _userID = "";
        _passWord = "";
        _userKey = "";
        _userSerialKey = 0;
        _userName = "";
        _functions = null;
    }

    public static Global getContext() {
        return _context;
    }

    public String getApiConnUrl() {
        return Uri.parse(_url).buildUpon().appendEncodedPath(_apiConnName).build().toString();
    }

    public String getApiUpdateUrl() {
        return Uri.parse(_url).buildUpon().appendEncodedPath(_apiUpdateName).build().toString();
    }

    public String getUserKey() {
        return _userKey;
    }

    public void setUserKey(String userKey) {
        _userKey = userKey;
    }

    public int getUserSerialKey() {
        return _userSerialKey;
    }

    public void setUserSerialKey(int userSerialKey) {
        _userSerialKey = userSerialKey;
    }

    public String getUserName() {
        return _userName;
    }

    public void setUserName(String userName) {
        _userName = userName;
    }

    public String getUserID() {
        return _userID;
    }

    public void setUserID(String userID) {
        _userID = userID;
    }

    public String getPassWord() {
        return _passWord;
    }

    public void setPassWord(String passWord) {
        passWord = passWord;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        _url = url;
        if (!_debugMode) {
            _sharedPreferences.edit().putString("Url", url).apply();
        }
    }

    public String get_PortalUrl() {
        return _PortalUrl;
    }

    public void setPortalUrl(String portalUrl) {
        _PortalUrl = portalUrl;
        if (!_debugMode) {
            _sharedPreferences.edit().putString("PortalUrl", portalUrl).apply();
        }
    }

    //20201019 archie SSO新增
    public String getSSOUrl() {
        return _SSOUrl;
    }

    public void setSSOUrl(String SSOUrl) {
        _SSOUrl = SSOUrl;
        if (!_debugMode) {
            _sharedPreferences.edit().putString("SSOUrl", SSOUrl).apply();
        }
    }

    public String getClientIp() {
        return _ClientIp;
    }

    public void setClientIp(String ClientIp) {
        _ClientIp = ClientIp;
        if (!_debugMode) {
            _sharedPreferences.edit().putString("ClientIp", ClientIp).apply();
        }
    }

    public String getResourceType() {
        return _resourceType;
    }

    public void setResourceType(String resourceType) {
        _resourceType = resourceType;
        if (!_debugMode) {
            _sharedPreferences.edit().putString("ResourceType", resourceType).apply();
        }
    }

    //20201019 archie SSO新增
    public void setToken(String token) {
        _token = token;
    }

    public String getToken() {
        return _token;
    }

    //20201020 archie SSO新增
    public void setStatusCode(int statusCode) {
        _statusCode = statusCode;
    }

    public int getStatusCode() {
        return _statusCode;
    }

    public String[] getFunctionTypes() {
        return _functionTypes;
    }

    public String[] getFunctionSubTypes() {
        return _functionSubTypes;
    }

    public int getConnectionTimeOut() {
        return _connectionTimeOut;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        _connectionTimeOut = connectionTimeOut;
    }

    public HashMap<String, List<MesFunction>> getFunctions() {
        return _functions;
    }

    public void setFunctions(List<MesFunction> functions) {
        _functions = new HashMap<String, List<MesFunction>>();
        for (MesFunction fun : functions) {
            if (!_functions.containsKey(fun.FUNCTION_TYPE))
                _functions.put(fun.FUNCTION_TYPE, new ArrayList<MesFunction>());
            _functions.get(fun.FUNCTION_TYPE).add(fun);
        }
    }

    // region 設置/取得允許通知的設置
    public Boolean getAllowedNotification() {
        return _allowedNotification;
    }

    public void setAllowedNotification(Boolean isAllowed) {
        _allowedNotification = isAllowed;
        if (!_debugMode) {
            _sharedPreferences.edit().putBoolean("AllowedNotification", isAllowed).apply();
        }
    }
    // endregion


    // region 設置/取得廠別資訊
    public String getFactoryKey() {
        return _factoryKey;
    }

    public void setFactoryKey(String currentFactoryKey) {
        _factoryKey = currentFactoryKey;
        if (!_debugMode) {
            _sharedPreferences.edit().putString("FactoryKey", currentFactoryKey).apply();
        }
    }

    public String getFactoryId() {
        return _factoryId;
    }

    public void setFactoryId(String currentFactoryId) {
        _factoryId = currentFactoryId;
        if (!_debugMode) {
            _sharedPreferences.edit().putString("FactoryId", currentFactoryId).apply();
        }
    }

    public String getFactoryName() {
        return _factoryName;
    }

    public void setFactoryName(String currentFactoryName) {
        _factoryName = currentFactoryName;
        if (!_debugMode) {
            _sharedPreferences.edit().putString("FactoryName", currentFactoryName).apply();
        }
    }

    public List<? extends Map<String, Object>> getFactories() {
        return _factories;
    }

    public void setFactories(List<FactoryInfo> factories) {
        DataTable dtFactory = new DataTable();
        DataColumn dcFactoryKey = new DataColumn("FACTORY_KEY");
        DataColumn dcFactoryId = new DataColumn("FACTORY_ID");
        DataColumn dcFactoryName = new DataColumn("FACTORY_NAME");
        dtFactory.addColumn(dcFactoryKey);
        dtFactory.addColumn(dcFactoryId);
        dtFactory.addColumn(dcFactoryName);
        if (factories.size() > 0) {
            for (FactoryInfo factory : factories) {
                DataRow dr = dtFactory.newRow();
                dr.setValue("FACTORY_KEY", factory.FACTORY_KEY);
                dr.setValue("FACTORY_ID", factory.FACTORY_ID);
                dr.setValue("FACTORY_NAME", factory.FACTORY_NAME);
                dtFactory.Rows.add(dr);
            }

            _factories = (List<? extends Map<String, Object>>) dtFactory.toListHashMap();

            for (Map<String, Object> factory : _factories)
            {
                String fId = factory.get("FACTORY_ID").toString();
                String fName = factory.get("FACTORY_NAME").toString();
                factory.put("IDNAME", fId+"_"+fName);
            }
        }
    }
    // endregion

    // 判斷服務是否存在
    public boolean isServiceRunning(String serviceName) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //region crash exception handle

    public class CrashExceptionHandler implements Thread.UncaughtExceptionHandler {

        private Application app = null;

        public CrashExceptionHandler(Application app) {
            Thread.setDefaultUncaughtExceptionHandler(this);
            this.app = app;
        }

        public void uncaughtException(Thread t, Throwable e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            notifyDialog(sw.toString());
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }

        void notifyDialog(String reportText) {
            try {
                Intent dialogIntent = new Intent(app.getApplicationContext(), CrashExceptionActivity.class);
                dialogIntent.putExtra("CRASH_MESSAGE", reportText);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                app.getApplicationContext().startActivity(dialogIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //endregion
}
