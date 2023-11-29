package com.delta.android.WMS.Param;

public class BIWMSPackingInfoParam {

    // 收料單代碼
    public static final String GrId = "Unicom.Uniworks.BModule.WMS.Library.Parameter.BIWMSPackingInfoParam.GrId";

    // 存貨層級；Box1、Box2、Box3、Entity、Pallet
    public static final String SkuLevel = "Unicom.Uniworks.BModule.WMS.Library.Parameter.BIWMSPackingInfoParam.SkuLevel";

    // 存貨編號
    public static final String SkuNum = "Unicom.Uniworks.BModule.WMS.Library.Parameter.BIWMSPackingInfoParam.SkuNum";

    // 收料條碼類型:QRCODE, BARCODE
    public static final String BarCodeType = "Unicom.Uniworks.BModule.WMS.Library.Parameter.BIWMSPackingInfoParam.BarCodeType";

    // 收料條碼類型代碼
    public static final String BarCodeTypeId = "Unicom.Uniworks.BModule.WMS.Library.Parameter.BIWMSPackingInfoParam.BarCodeTypeId";

    // 收料條碼
    public static final String BarCodeValue = "Unicom.Uniworks.BModule.WMS.Library.Parameter.BIWMSPackingInfoParam.BarCodeValue";

    // 查詢中間表(SWM_WGR_CONT)是否已收料: Y(已收料)、T(暫收料)、N(未收料)
    public static final String IsReceived = "Unicom.Uniworks.BModule.WMS.Library.Parameter.BIWMSPackingInfoParam.IsReceived";

    // 回傳是否有外層包裝 (供詢問是否要以外包箱入庫)
    public static final String ChangeSkuLevel = "Unicom.Uniworks.BModule.WMS.Library.Parameter.BIWMSPackingInfoParam.ChangeSkuLevel";
}
