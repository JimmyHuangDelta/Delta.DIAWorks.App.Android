package com.delta.android.WMS.Param.ParamObj;

public class CountInvObj {
    private String countId = "";
    private double serialNo = 0;
    private String itemId = "";
    private String lotId = "";
    private String inventoryStatus = "";
    private String custStatus = "";
    private double inventoryQty = 0;
    private double firstCountQty = 0;
    private double secondCountQty = 0;
    private String userAdjustFlg = "N";
    private String binId = "";

    public String getCountId(){return countId;}
    public void setCountId(String CountId){countId = CountId;}

    public double getSerialNo(){return serialNo;}
    public void setSerialNo(String SerialNo){countId = SerialNo;}

    public String getItemId(){return itemId;}
    public void setItemId(String ItemId){itemId = ItemId;}

    public String getLotId(){return lotId;}
    public void setLotId(String LotId){lotId = LotId;}

    public String getInventoryStatus(){return inventoryStatus;}
    public void setInventoryStatus(String InventoryStatus){inventoryStatus = InventoryStatus;}

    public String getCustStatus(){return custStatus;}
    public void setCustStatus(String CustStatus){custStatus = CustStatus;}

    public double getInventoryQty (){return inventoryQty;}
    public void setInventoryQty(double InventoryQty){inventoryQty = InventoryQty;}

    public double getFirstCountQty(){return firstCountQty;}
    public void setFirstCountQty(double FirstCountQty){ firstCountQty = FirstCountQty;}

    public double getSecondCountQty(){return secondCountQty;}
    public void setSecondCountQty(double SecondCountQty){secondCountQty = SecondCountQty;}

    public String getUserAdjustFlg(){return userAdjustFlg;}
    public void setUserAdjustFlg(String UserAdjustFlg){userAdjustFlg = UserAdjustFlg;}

    public String getBinId(){return binId;}
    public void setBinId(String BinId){binId = BinId;}

}
