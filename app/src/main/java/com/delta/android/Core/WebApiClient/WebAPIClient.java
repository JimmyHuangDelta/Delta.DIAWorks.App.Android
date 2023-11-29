package com.delta.android.Core.WebApiClient;

import android.app.Activity;
import android.app.Service;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.delta.android.Core.Common.Global;
import com.delta.android.Core.DataTable.DataTable;

public class WebAPIClient {
    private HashMap<String, HashMap<String, Object>> ReturnList;
    private HashMap<String, Object> AckError;
    private HashMap<String, List<String>> ActErrorParam;
    private HashMap<String, HashMap<String, DataTable>> ReturnJsonTables;
    private Activity activity;
    private Gson gson = null;
    private Global global = null;

    public WebAPIClient(Activity activityCurrent) {
        activity = activityCurrent;
        global = (Global) activityCurrent.getApplication();
        gson = new Gson();
    }

    //region property

    public HashMap<String, HashMap<String, Object>> getReturnList() {
        return ReturnList;
    }

    public HashMap<String, Object> getAckError() {
        return AckError;
    }

    public HashMap<String, HashMap<String, DataTable>> getReturnJsonTable() {
        return ReturnJsonTables;
    }

    //endregion

    //region CallModule

    public void CallBIModule(BModuleObject object, WebAPIClientEvent event) {
        List<BModuleObject> bobjs = new ArrayList<BModuleObject>();
        bobjs.add(object);
        CallBIModule(bobjs, event);
    }

    public void CallBIModule(final List<BModuleObject> object, final WebAPIClientEvent event) {
        Thread t = new Thread(new Runnable() {
            private boolean bSuccess = false;

            public void run() {
                try {
                    bSuccess = sendRequest(object, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        event.onPostBack(new BModuleReturn(bSuccess,
                                getAckError(),
                                getReturnJsonTable(),
                                getReturnList()));
                    }
                });
            }
        });
        t.start();
    }

    public void CallBModule(BModuleObject object, WebAPIClientEvent event) {
        List<BModuleObject> bobjs = new ArrayList<BModuleObject>();
        bobjs.add(object);
        CallBModule(bobjs, event);
    }

    public void CallBModule(final List<BModuleObject> object, final WebAPIClientEvent event) {
        Thread t = new Thread(new Runnable() {
            private boolean bSuccess = false;

            public void run() {
                try {
                    bSuccess = sendRequest(object, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        event.onPostBack(new BModuleReturn(bSuccess,
                                getAckError(),
                                getReturnJsonTable(),
                                getReturnList()));
                    }
                });
            }
        });
        t.start();
    }

    public BModuleReturn CallBIModule(BModuleObject object) {
        List<BModuleObject> bobjs = new ArrayList<BModuleObject>();
        bobjs.add(object);
        return CallBIModule(bobjs);
    }

    public BModuleReturn CallBIModule(final List<BModuleObject> object) {
        boolean bSuccess = false;
        try {
            bSuccess = sendRequest(object, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BModuleReturn(
                bSuccess,
                getAckError(),
                getReturnJsonTable(),
                getReturnList()
        );
    }

    public BModuleReturn CallBModule(BModuleObject object) {
        List<BModuleObject> bobjs = new ArrayList<BModuleObject>();
        bobjs.add(object);
        return CallBModule(bobjs);
    }

    public BModuleReturn CallBModule(final List<BModuleObject> object) {
        boolean bSuccess = false;

        try {
            bSuccess = sendRequest(object, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BModuleReturn(
                bSuccess,
                getAckError(),
                getReturnJsonTable(),
                getReturnList()
        );
    }

    //endregion

    //region Login

    public void Login(final LoginUserObject user, final WebAPILoginEvent event) {
        Thread t = new Thread(new Runnable() {
            private LoginUserReturn loginResult = null;

            public void run() {
                try {
                    loginResult = sendLoginRequest(user);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        event.onPostBack(loginResult);
                    }
                });
            }
        });
        t.start();
    }

    public LoginUserReturn Login(final LoginUserObject user) {
        LoginUserReturn loginResult = null;

        try {
            loginResult = sendLoginRequest(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loginResult;
    }

    //20201019 archie 取SSO用
    public Object LoginSSO(final Object user) {
        Object loginResult = null;

        try {
            loginResult = sendLoginSSORequest(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loginResult;
    }

    //endregion

    //region SedWebApi

    private boolean sendRequest(List<BModuleObject> object, boolean isBModule) {
        Vector<Object> req = new Vector<Object>();
        for (BModuleObject bModuleObject : object) {
            Vector<ParameterInfo> params = bModuleObject.params;
            if (params == null) {
                params = new Vector<ParameterInfo>();
            }

            RequestBase reqBase = new RequestBase();
            reqBase.parameterList = params;
            reqBase.setModuleID(bModuleObject.getModuleID());
            reqBase.setBModuleName(bModuleObject.getBModuleName());
            reqBase.setRequestID(bModuleObject.getRequestID());
            req.add(reqBase);
        }

        RequestList reqLst = new RequestList();
        reqLst.setResourceType(global.getResourceType());
        reqLst.setUserID(global.getUserID());
        reqLst.setUserKey(global.getUserKey());
        reqLst.setUserSerialKey(global.getUserSerialKey());
        reqLst.setWriteLog(false);
        reqLst.setSenderFullName(global.getUserName());
        reqLst.setProcessGuid(null);
        reqLst.setReturnDataSet(false);// 為true則回傳ReturnDataSet為Byte[]
        reqLst.setToken(global.getToken());
        reqLst.setFactoryID(global.getFactoryId()); // 設定當前FactoryID

        if (isBModule) {
            reqLst.tkRequestList = req;
        } else {
            reqLst.ifRequestList = req;
        }

        if (AckError == null) {
            AckError = new HashMap<String, Object>();
        } else {
            AckError.clear();
        }

        if (ReturnList == null) {
            ReturnList = new HashMap<String, HashMap<String, Object>>();
        } else {
            ReturnList.clear();
        }

        if (ActErrorParam == null) {
            ActErrorParam = new HashMap<String, List<String>>();
        } else {
            ActErrorParam.clear();
        }

        if (ReturnJsonTables == null) {
            ReturnJsonTables = new HashMap<String, HashMap<String, DataTable>>();
        } else {
            ReturnJsonTables.clear();
        }

        ResponseList response = null;
        String strRespData = "";
        try {
            strRespData = sendWebApi(gson.toJson(reqLst), "CallServer");
            response = gson.fromJson(strRespData, ResponseList.class);
        } catch (Exception ex) {
            AckError.put("ErrorCode:xxx", String.format("SendWebApi Error:%s", strRespData));
        }

        if (response == null) {
            return false;
        }

        if (response.ReturnJsonDataSet != null) {
            for (String key : response.ReturnJsonDataSet.keySet()) {
                StringMap<?> obj = (StringMap<?>) response.ReturnJsonDataSet.get(key);
                HashMap<String, Object> hmTables = DataTable.convertStringMapToHashMap(obj);
                HashMap<String, DataTable> hmJsonTables = new HashMap<String, DataTable>();
                for (String keyTable : hmTables.keySet()) {
                    @SuppressWarnings("unchecked")
                    DataTable dt = DataTable.fromArrayList((ArrayList<StringMap<?>>) hmTables.get(keyTable));
                    dt.TableName = keyTable;
                    hmJsonTables.put(keyTable, dt);
                }
                ReturnJsonTables.put(key, hmJsonTables);
            }
        }

        if (!isBModule) {
            if (response.AllAckError != null) {
                for (ErrorInfo reqErr : response.AllAckError) {
                    AckError.put(reqErr.getCode(), reqErr.getMessage());
                    ActErrorParam.put(reqErr.getCode(), reqErr.getErrorParameter());
                }
            }
            for (int i = 0; i < response.getIFResponseList().size(); i++) {
                // AckError
                if (response.getIFResponseList().get(i).AckError != null) {
                    for (ErrorInfo err : response.IFResponseList.get(i).AckError) {
                        AckError.put(err.getCode(), err.getMessage());
                    }
                }
                // ReturnList
                if (response.getIFResponseList().get(i).ReturnList != null) {
                    HashMap<String, Object> hm = new HashMap<String, Object>();
                    for (HashMap<String, Object> hmRtn : response.getIFResponseList().get(i).ReturnList) {
                        String key = (String) hmRtn.get("Key");
                        String value = (String) hmRtn.get("Value");
                        hm.put(key, value);
                    }
                    ReturnList.put(response.getIFResponseList().get(i).InfoRequestID, hm);
                }
            }
        } else {
            if (response.AllAckError != null) {
                for (ErrorInfo reqErr : response.AllAckError) {
                    AckError.put(reqErr.getCode(), reqErr.getMessage());
                    ActErrorParam.put(reqErr.getCode(), reqErr.getErrorParameter());
                }
            }
            for (int i = 0; i < response.getTKResponseList().size(); i++) {
                // AckError
                if (response.TKResponseList.get(i).AckError != null) {
                    for (ErrorInfo err : response.TKResponseList.get(i).AckError) {
                        AckError.put(err.getCode(), err.getMessage());
                    }
                }
                // ReturnList
                if (response.TKResponseList.get(i).ReturnList != null) {
                    HashMap<String, Object> hm = new HashMap<String, Object>();
                    for (HashMap<String, Object> hmRtn : response.TKResponseList.get(i).ReturnList) {
                        String key = (String) hmRtn.get("Key");
                        String value = (String) hmRtn.get("Value");
                        hm.put(key, value);
                    }

                    ReturnList.put(response.TKResponseList.get(i).TKRequestID, hm);
                }
            }
        }

        if (AckError == null || AckError.size() == 0) {
            return true;
        }

        return false;
    }

    private String sendWebApi(String postData, String method) {
        try {

            String strSendData = URLEncoder.encode(postData, "UTF-8");// 以UTF8編碼，避免傳送至Server中文變亂碼
            String strPostData = String.format("functype=%s&funcsubtype=%s&method=%s&request=%s&ClientIP=%s&ClientName=%s",
                    gson.toJson(global.getFunctionTypes()),
                    gson.toJson(global.getFunctionSubTypes()),
                    method,
                    strSendData,
                    global.getClientIp(), //20201019 archie 將ClientIP傳至Server端
                    global.getClientIp()); //20201019 archie 將ClientIP傳至Server端

            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, global.getConnectionTimeOut());
            HttpConnectionParams.setSoTimeout(params, global.getConnectionTimeOut());
            HttpClient httpClient = new DefaultHttpClient(params);
            //20201021 Hans 修正傳入參數 For PDA SSO登入使用
            HttpPost httpPost = new HttpPost(global.getApiConnUrl() + "?ClientIP=" + global.getClientIp() + "&ClientName=" + global.getClientIp());
            httpPost.addHeader("Content-type", "application/x-www-form-urlencoded; charset=text/xml;UTF-8");
            //20201019 archie SSO機制修改,Header加入Authorization及Token值
            httpPost.addHeader("Authorization", "bearer"+global.getToken());
            httpPost.setEntity(new StringEntity(strPostData, HTTP.UTF_8));
            HttpResponse response = httpClient.execute(httpPost);

            //20201019 archie SSO機制修改,將Server端傳回Token做更新
            if (response.getHeaders("Authorization").length > 0)
            {
                HeaderElement[] header = response.getFirstHeader("Authorization").getElements();

                if (header.length > 0)
                {
                    String token = header[0].toString();

                    // 20201019 archie 防止傳回來的Token是空的
                    if (token.length() > "Bearer ".length())
                    {
                        token = token.substring("Bearer ".length());

                        if (!token.equals(global.getToken()))
                        {
                            global.setToken(token);
                        }
                    }
                }
            }

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200)//正常server會回200
                return EntityUtils.toString(response.getEntity());
            else
                return String.format("%s\rServer return error:%s", global.getApiConnUrl(), statusCode);
        } catch (SocketTimeoutException timeoutException) {
            return "SocketTimeout";//SocketTimeout
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    //20201020 archie 連線SSO Server
    private String sendWebApiToClient(String postData, String method) {
        try {
            //String url = activity.getResources().getString(R.string.DEFAULT_CLIENT_URL) + method;

            String url = global.getSSOUrl() + method;

            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, global.getConnectionTimeOut());
            HttpConnectionParams.setSoTimeout(params, global.getConnectionTimeOut());
            HttpClient httpClient = new DefaultHttpClient(params);
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity(postData, HTTP.UTF_8));
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200)//正常server會回200
                return EntityUtils.toString(response.getEntity());
            else
                return String.format("%s\rServer return error:%s", url, statusCode);
        } catch (SocketTimeoutException timeoutException) {
            return "SocketTimeout";//SocketTimeout
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    private LoginUserReturn sendLoginRequest(LoginUserObject user) {
        LoginUserReturn loginResult = null;
        String strRespData = "";
        try {
            strRespData = sendWebApi(gson.toJson(user), "Login");
            loginResult = gson.fromJson(strRespData, LoginUserReturn.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (loginResult == null) {
            loginResult = new LoginUserReturn();
            loginResult.LoginSuccess = false;
            loginResult.Error = strRespData;
        }

        return loginResult;
    }

    //20201019 archie 取SSO用
    private Object sendLoginSSORequest(Object user) {
        Object loginResult = null;
        String strRespData = "";

        LoginUserClientReturn result = null;

        try {
            strRespData = sendWebApiToClient(gson.toJson(user), "api/SSO/LoginAlternative");
            result = gson.fromJson(strRespData, LoginUserClientReturn.class);
        } catch (Exception e) {

        }

        if (result == null) {
            result = new LoginUserClientReturn();
            result.IsSuccess= false;
            result.ErrorMessage = strRespData;
        }

        loginResult = result;


        return loginResult;
    }

    //endregion
}