package com.delta.android.Core.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.delta.android.Core.AppUpdate.CheckUpdateService;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.LoginUserClientReturn;
import com.delta.android.Core.WebApiClient.LoginUserForPortalObject;
import com.delta.android.Core.WebApiClient.LoginUserObject;
import com.delta.android.Core.WebApiClient.LoginUserReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.Core.WebApiClient.WebAPILoginEvent;
import com.delta.android.R;
import com.delta.android.WMS.Param.BIUserLoginFactoryParam;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Vector;

public class LoginActivity extends BaseActivity {

    //更新app的服務
    Intent updateService = null;
    private String Msg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_login);

        //20201103 archie 新增顯示被登出原因
        Msg = getIntent().getStringExtra("MSG");

        if (Msg == null) Msg = "";

        if (!Msg.equals(""))
        {
            ShowMessage(Msg);
        }

        // 20201019 archie 取得設備IP
        String ip = getIPAddress(this);

        getGlobal().setClientIp(ip);

        final EditText etUserID = findViewById(R.id.etUserID);
        final EditText etPassword = findViewById(R.id.etPassword);
        final Button btnLogin = findViewById(R.id.btnLogin);

        etUserID.setFocusable(true);
        etUserID.requestFocusFromTouch();

        etUserID.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER) {
                    return false;
                }

                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return true;
                }

                etPassword.setFocusable(true);
                etPassword.requestFocusFromTouch();
                return true;
            }
        });


        etPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER) {
                    return false;
                }

                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return true;
                }

                LoginCheck(etUserID.getText().toString(), etPassword.getText().toString());
                return true;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginCheck(etUserID.getText().toString(), etPassword.getText().toString());
            }
        });

        if (getResources().getBoolean(R.bool.DEBUG_MODE) == false) {//debug時不啓動
            //啓動更新App服務
            updateService = new Intent(LoginActivity.this, CheckUpdateService.class);
            startService(updateService);
        }
    }

    private void LoginCheck(final String userID, final String password) {

        //20201019 archie SSO機制修改
        final LoginUserForPortalObject loginUserForPortalObj = new LoginUserForPortalObject();
        final LoginUserObject loginUserObj = new LoginUserObject();

        loginUserForPortalObj.setUserID(userID);
        loginUserForPortalObj.setPassword(password);
        loginUserForPortalObj.setIsPermanent(false);
        loginUserForPortalObj.setClientIP(getGlobal().getClientIp());
        loginUserForPortalObj.setClientName(getGlobal().getClientIp());

        GetToken(loginUserForPortalObj, new WebAPILoginEvent() {
            @Override
            public void onPostBack(Object userReturn) {
                LoginUserClientReturn loginUserClientReturn = (LoginUserClientReturn) userReturn;

                if (loginUserClientReturn == null) {
                    ShowMessage(((LoginUserClientReturn) userReturn).ErrorMessage);
                    return;
                }

                if (!loginUserClientReturn.IsSuccess)
                {
                    ShowMessage(loginUserClientReturn.ErrorMessage);
                }
                else
                {
                    getGlobal().setToken(loginUserClientReturn.Data);

                    loginUserObj.setUserID(userID);
                    loginUserObj.setPassword(password);
                    loginUserObj.setResourceType(getGlobal().getResourceType());

                    LoginForToken(loginUserObj);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_core_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_config_setting:
                Intent intentSetting = new Intent(LoginActivity.this, ConfigSettingActivity.class);
                startActivity(intentSetting);
                return true;
            case R.id.action_version_update:
                Intent intentUpdate = new Intent(LoginActivity.this, VersionUpdateActivity.class);
                startActivity(intentUpdate);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (updateService != null)
            stopService(updateService);
        super.onDestroy();
    }

	//20201020 archie SSO機制新增,取得Token
    private void LoginForToken(final LoginUserObject obj) {
        Login(obj, new WebAPILoginEvent() {
            @Override
            public void onPostBack(Object userReturn) {
                LoginUserReturn loginUserReturn = (LoginUserReturn)userReturn;

                if (loginUserReturn.LoginSuccess) {
                    getGlobal().setUserID(loginUserReturn.UserID);
                    getGlobal().setUserKey(loginUserReturn.UserKey);
                    getGlobal().setUserSerialKey(loginUserReturn.UserSerialKey);
                    getGlobal().setUserName(loginUserReturn.UserName);
                    getGlobal().setFactories(loginUserReturn.Factories); // 儲存廠別清單

                    /*
                    if (getGlobal().getFactoryKey().equals("")) {
                        getGlobal().setFactoryId(loginUserReturn.FactoryID);
                        getGlobal().setFactoryKey(loginUserReturn.FactoryKey);
                        getGlobal().setFactoryName(loginUserReturn.FactoryName);
                        //getGlobal().setFunctions(loginUserReturn.Functions);
                    }*/

                    getPreUserLoginFactory();

                    //Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    //startActivity(intent);
                } else {
                    ShowMessage(loginUserReturn.Error);
                }
            }
        });
    }

    // 20201019 archie 取得設備IP
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//當前使用2G/3G/4G網路
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }


            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//當前使用無線網路
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //當前無網路連線,請在設定中開啟網路
        }
        return null;
    }

    private void getPreUserLoginFactory() {
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.Authorization.BIUser");
        bmObj.setModuleID("GetPreUserLoginFactory");
        bmObj.setRequestID("GetPreUserLoginFactory");
        bmObj.params = new Vector<>();

        ParameterInfo userParam = new ParameterInfo();
        userParam.setParameterID(BIUserLoginFactoryParam.UserId);
        userParam.setParameterValue(getGlobal().getUserID());
        bmObj.params.add(userParam);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                DataTable dtPreLoginFactory = bModuleReturn.getReturnJsonTables().get("GetPreUserLoginFactory").get("*");
                if (dtPreLoginFactory != null && dtPreLoginFactory.Rows.size() > 0) { // 有上次登入廠別的紀錄
                    getGlobal().setFactoryKey(dtPreLoginFactory.Rows.get(0).getValue("FACTORY_KEY").toString());
                    getGlobal().setFactoryId(dtPreLoginFactory.Rows.get(0).getValue("FACTORY_ID").toString());
                    getGlobal().setFactoryName(dtPreLoginFactory.Rows.get(0).getValue("FACTORY_NAME").toString());
                }
                Intent intent = new Intent(LoginActivity.this, MenuNewActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 將得到的int型別的IP轉換為String型別
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }
}
