package com.delta.android.PMS.Param.ParamObj;

public class PartTrxObj {
    private String trxDate = "";
    private String partId = "";
    private String partLotId = "";
    private int trxQty = 0;
    private String storageId = "";
    private String storageBinId = "";
    private String trxMode = "";
    private String cmt = "";

    public String getTrxDate(){return trxDate;}
    public void setTrxDate(String date){trxDate = date;}

    public String getPartId(){return partId;}
    public void setPartId(String itemId){partId = itemId;}

    public String getPartLotId(){return partLotId;}
    public void setPartLotId(String lotId){partLotId = lotId;}

    public int getTrxQty(){return trxQty;}
    public void setTrxQty(int qty){trxQty = qty;}

    public String getStorageId(){return storageId;}
    public void setStorageId(String storage){storageId = storage;}

    public String getBinId(){return storageBinId;}
    public void setBinId(String bin){storageBinId = bin;}

    public String getTrxMode(){return trxMode;}
    public void setTrxMode(String mode){trxMode = mode;}

    public String getCmt(){return cmt;}
    public void setCmt(String partCmt){cmt = partCmt;}
}
