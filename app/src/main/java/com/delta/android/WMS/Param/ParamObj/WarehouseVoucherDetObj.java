package com.delta.android.WMS.Param.ParamObj;

import java.util.Date;

public class WarehouseVoucherDetObj {

    private double seq = 0;
    private String storageId = "";
    private String storageKey = "";
    private String woId = "";
    private String woKey = "";
    private String itemId = "";
    private String itemKey = "";
    private String lotId = "";
    private String lotCode = "";
    private double qty = 0;
    private double procQty = 0;
    private double scrapQty = 0;
    private double scrapProcQty = 0;
    private Date mfgDate;
    private Date expDate;
    private String uom = "";
    private String cmt = "";
    private String specLot = "";
    private String tempBin = "";
    private String box1Id = "";
    private String box2Id = "";
    private String box3Id = "";
    private String palletId = "";

    public double getSeq() {return seq;}
    public void setSeq(double Seq) {seq = Seq;}

    public String getStorageId() {return storageId;}
    public void setStorageId(String StorageId) {storageId = StorageId;}

    public String getStorageKey() {return storageKey;}
    public void setStorageKey(String StorageKey) {storageKey = StorageKey;}

    public String getWoId() {return woId;}
    public void setWoId(String WoId) {woId = WoId;}

    public String getWoKey() {return woKey;}
    public void setWoKey(String WoKey) {woKey = WoKey;}

    public String getItemId() {return itemId;}
    public void setItemId(String ItemId) {itemId = ItemId;}

    public String getItemKey() {return itemKey;}
    public void setItemKey(String ItemKey) {itemKey = ItemKey;}

    public String getLotId() {return lotId;}
    public void setLotId(String LotId) {lotId = LotId;}

    public double getQty() {return qty;}
    public void setQty(double Qty) {qty = Qty;}

    public double getProcQty() {return procQty;}
    public void setProcQty(double ProcQty) {procQty = ProcQty;}

    public double getScrapQty() {return scrapQty;}
    public void setScrapQty(double ScrapQty) {scrapQty = ScrapQty;}

    public double getScrapProcQty() {return scrapProcQty;}
    public void setScrapProcQty(double ScrapProcQty) {scrapProcQty = ScrapProcQty;}

    public Date getMfgDate() {return mfgDate;}
    public void setMfgDate(Date MfgDate) {mfgDate = MfgDate;}

    public Date getExpDate() {return expDate;}
    public void setExpDate(Date ExpDate) {expDate = ExpDate;}

    public String getUom() {return uom;}
    public void setUom(String Uom) {uom = Uom;}

    public String getCmt() {return cmt;}
    public void setCmt(String Cmt) {cmt = Cmt;}

    public String getSpecLot() {return specLot;}
    public void setSpecLot(String SpecLot) {specLot = SpecLot;}

    public String getTempBin() {return tempBin;}
    public void setTempBin(String TempBin) {tempBin = TempBin;}

    public String getLotCode() {return lotCode;}
    public void setLotCode(String LotCode) {lotCode = LotCode;}

    public String getBox1Id() {return box1Id;}
    public void setBox1Id(String Box1Id) {box1Id = Box1Id;}

    public String getBox2Id() {return box2Id;}
    public void setBox2Id(String Box2Id) {box2Id = Box2Id;}

    public String getBox3Id() {return box3Id;}
    public void setBox3Id(String Box3Id) {box3Id = Box3Id;}

    public String getPalletId() {return palletId;}
    public void setPalletId(String PalletId) {palletId = PalletId;}
}
