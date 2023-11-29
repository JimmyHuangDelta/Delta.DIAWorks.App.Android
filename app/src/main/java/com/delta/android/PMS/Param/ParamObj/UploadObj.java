package com.delta.android.PMS.Param.ParamObj;

public class UploadObj {
    private String WoId = "";
    private String FileGuid = "";
    private String FileType = "";
    private String FileName = "";
    private String FileDesc = "";
    private String UserId = "";
    private String UploadDate = "";
    private String FilePath = "";

    public String getMroWoId(){return WoId;}
    public void setMroWoId(String woId){WoId = woId;}

    public String getUploadFileName(){return FileName;}
    public void setUploadFileName(String fileName){FileName = fileName;}

    public String getUploadFileDesc(){return FileDesc;}
    public void setUploadFileDesc(String fileDesc){FileDesc = fileDesc;}

    public String getFileGuid(){return FileGuid;}
    public void setFileGuid(String fileDesc){FileGuid = fileDesc;}

    public String getFileType(){return FileType;}
    public void setFileType(String fileDesc){FileType = fileDesc;}

    public String getUploadUserId(){return UserId;}
    public void setUploadUserId(String userId){UserId = userId;}

    public String getUploadDate(){return UploadDate;}
    public void setUploadDate(String date){UploadDate = date;}

    public String getFilePath(){return FilePath;}
    public void setFilePath(String path){FilePath = path;}
}
