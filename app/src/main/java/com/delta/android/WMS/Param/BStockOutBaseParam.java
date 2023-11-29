package com.delta.android.WMS.Param;

public class BStockOutBaseParam extends BStockBaseParam{

    // 檢查出庫邏輯的物件
    public static final String CheckStockOutByObjs = "Unicom.Uniworks.BModule.WMS.Library.Parameter.BStockOutBaseParam.CheckStockOutByObjs";

    // 檢查出庫邏輯的單據
    public static final String CheckStockOutBySheetIds = "Unicom.Uniworks.BModule.WMS.Library.Parameter.BStockOutBaseParam.CheckStockOutBySheetIds";

    // 回傳已被 Hold 住的訊息 (PDA 用)
    public static final String HoldMessage = "Unicom.Uniworks.BModule.WMS.Library.Parameter.BStockOutBaseParam.HoldMessage";

    // 回傳 Hint 訊息 (PDA 用)
    public static final String HintMessage = "Unicom.Uniworks.BModule.WMS.Library.Parameter.BStockOutBaseParam.HintMessage";

    // 判斷當前作業執行動作: Reserve(欲預約), Pick(欲揀貨), DeliveryNotePick(欲出通單揀貨/QC退回後再換狀態), StockOut(欲出庫), Ship(欲出貨)
    public static final String Mode = "Unicom.Uniworks.BModule.WMS.Library.Parameter.BStockOutBaseParam.Mode";
}
