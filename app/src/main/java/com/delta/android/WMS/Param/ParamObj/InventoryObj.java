package com.delta.android.WMS.Param.ParamObj;

public class InventoryObj {

    public String getSheetId() {
        return sheetId;
    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public double getSeq() {
        return seq;
    }

    public void setSeq(double seq) {
        this.seq = seq;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    public double getInventoryQty() {
        return inventoryQty;
    }

    public void setInventoryQty(double inventoryQty) {
        this.inventoryQty = inventoryQty;
    }

    public String getFromBinId() {
        return fromBinId;
    }

    public void setFromBinId(String FromBinId) {
        this.fromBinId = FromBinId;
    }

    public String getBinId() {
        return binId;
    }

    public void setBinId(String binId) {
        this.binId = binId;
    }

    public String getSheetTypePolicyId() {
        return sheetTypePolicyId;
    }

    public void setSheetTypePolicyId(String sheetTypePolicyId) {
        this.sheetTypePolicyId = sheetTypePolicyId;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String PortId) {
        this.portId = PortId;
    }

    public String getFromBinType() {
        return fromBinType;
    }

    public void setFromBinType(String FromBinType) {
        this.fromBinType = FromBinType;
    }

    public String getTempBin() {
        return tempBin;
    }

    public void setTempBin(String TempBin) {
        this.tempBin = TempBin;
    }

    public double getRegisterSerialKey() {
        return registerSerialKey;
    }

    public void setRegisterSerialKey(double RegisterSerialKey) {
        this.registerSerialKey = RegisterSerialKey;
    }

    private String sheetId = "";
    private double seq = 0;
    private String storageId = "";
    private String itemId = "";
    private String lotId = "";
    private double inventoryQty = 0;
    private String binId = "";
    private String sheetTypePolicyId = "";
    private String fromBinId = "";
    private String portId = "";
    private String fromBinType = "";
    private String  tempBin = "";
    private double registerSerialKey = 0;
}
