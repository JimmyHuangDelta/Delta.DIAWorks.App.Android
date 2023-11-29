package com.delta.android.WMS.Param;

public class BDeliveryNoteShipParam extends BStockOutBaseParam {

    // 出貨作業上傳參數
    public static final String PickDetObj = "BDeliveryNoteShipParam.PickDetObj";

    //出貨派車Mater資訊
    public static final String OrderMst = "BDeliveryNoteShipParam.OrderMst";

    //出貨派車明細
    public static final String OrderDet = "BDeliveryNoteShipParam.OrderDet";

    public static final String ReleaseOrderObj = "BDeliveryNoteShipParam.ReleaseOrderObj";

    //儲存: SAVE, 確認: CONFIRM, 取消: CANCEL
    public static final String Action = "BDeliveryNoteShipParam.Action";

    public static final String TempBin = "BDeliveryNoteShipParam.TempBin";

    public static final String DnId = "BDeliveryNoteShipParam.DnId";

    public static final String FromPda = "BDeliveryNoteShipParam.FromPda";

    // 回傳 Hold 住的物料代碼
    public static final String HoldItemID = "BDeliveryNoteShipParam.HoldItemID";
}
