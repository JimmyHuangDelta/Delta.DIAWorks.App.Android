package com.delta.android.WMS.Param.ParamObj;

public class DispatchOrderObj {
    private String itemId = "";
    private String lot = "";
    private int qty = 0;
    private String storage = "";
    private boolean isRecommend = true;

    public String getItemId() {return  itemId;}
    public void setItemId(String ItemId) {itemId = ItemId;}

    public String getLot() {return  lot;}
    public void setLot(String Lot) {lot = Lot;}

    public int getQty() {return qty;}
    public void setQty(int Qty) {qty = Qty;}

    public String getStorage() {return storage;}
    public void setStorage(String Storage) {storage = Storage;}

    public boolean getIsRecommend() { return  isRecommend; }
    public void setIsRecommend(boolean IsRecommend) { isRecommend = IsRecommend; }
}
