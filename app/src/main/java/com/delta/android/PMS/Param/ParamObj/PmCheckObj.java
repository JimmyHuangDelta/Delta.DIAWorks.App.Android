package com.delta.android.PMS.Param.ParamObj;

public class PmCheckObj {

    private String checkId = "";
    private String checkValue = "";
    private String checkResult = "";
    private String cmt = "";
    private String checkUserKey = "";

    public String getCheckId(){return checkId;}
    public void setCheckId(String id){checkId = id;}

    public String getCheckValue(){return checkValue;}
    public void setCheckValue(String value){checkValue = value;}

    public String getCheckResult(){return checkResult;}
    public void setCheckResult(String result){checkResult = result;}

    public String getCmt(){return cmt;}
    public void setCmt(String checkCmt){cmt = checkCmt;}

    public String getCheckUserKey(){return checkUserKey;}
    public void setCheckUserKey(String userKey){checkUserKey = userKey;}
}
