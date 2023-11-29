package com.delta.android.PMS.Param.ParamObj;

public class UserDataObj {
    private String userId = "";
    private String StartDate = "";
    private String EndDate = "";
    private String Cmt = "";

    public String getUserId(){return userId;}
    public void setUserId(String userDataId){userId = userDataId;}

    public String getStartDt(){return StartDate;}
    public void setStartDt(String userStart){StartDate = userStart;}

    public String getEndDt(){return EndDate;}
    public void setEndDt(String userEnd){EndDate = userEnd;}

    public String getUserCmt(){return Cmt;}
    public void setUserCmt(String cmt){Cmt = cmt;}
}
