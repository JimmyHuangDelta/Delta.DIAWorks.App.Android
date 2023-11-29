package com.delta.android.WMS.Param.ParamObj;

import java.math.BigDecimal;

public class PickDetObj {

    private String sheetId = "";
    private double seq = 0;
    private String storageId= "";
    private String itemId= "";
    private String lotId= "";
    private String binId= "";
    private String uom= "";
    private double qty = 0;

    public String getSheetId() {
        return sheetId;
    }
    public void setSheetId(String SheetId) {
        sheetId = SheetId;
    }

    public double getSeq(){return seq;}
    public void setSeq(double Seq){seq = Seq;}

    public String getStorageId() {
        return storageId;
    }
    public void setStorageId(String StorageId) {
        storageId = StorageId;
    }

    public String getItemId() {
        return itemId;
    }
    public void setItemId(String ItemId) {
        itemId = ItemId;
    }

    public String getLotId() {
        return lotId;
    }
    public void setLotId(String LotId) {
        lotId = LotId;
    }

    public String getBinId() {
        return binId;
    }
    public void setBinId(String BinId) {
        binId = BinId;
    }

    public String getUom() {
        return uom;
    }
    public void setUom(String Uom) {
        uom = Uom;
    }

    public double getQty(){ return qty; }
    public void setQty(double Qty){qty = Qty ;}
}
