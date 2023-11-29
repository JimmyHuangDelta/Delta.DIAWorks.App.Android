package com.delta.android.WMS.Param;

import com.delta.android.Core.DataTable.DataColumnCollection;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BWarehouseStorageParam extends BStockBaseParam {

    public static final String TrxType = "Unicom.Uniworks.BModule.WMS.INV.Parameter.BWarehouseStorageParam.TrxType";

    public static final String SheetTypePolicyId = "Unicom.Uniworks.BModule.WMS.INV.Parameter.BWarehouseStorageParam.SheetTypePolicyId";

    public static final String WsObj = "Unicom.Uniworks.BModule.WMS.INV.Parameter.GoodReceiptReceiveParam.WsObj";

    // WarehouseVoucherSheet (無單據入庫使用)
    public static final String WarehouseVoucherSheet = "Unicom.Uniworks.BModule.WMS.INV.Parameter.GoodReceiptReceiveParam.WarehouseVoucherSheet";

    public class WarehouseStorageMasterObj {

        private String WsID;

        private ArrayList<WarehouseStorageDetObj> WsDetails;

        public WarehouseStorageMasterObj() {}

        public final WarehouseStorageMasterObj getWsSheet(DataRow drMst, DataTable dtDet) {

            DataTable dtMst = drMst.getTable();
            WarehouseStorageMasterObj mst = new WarehouseStorageMasterObj();
            WarehouseStorageDetObj det = null;
            ArrayList<WarehouseStorageDetObj> lstDet = new ArrayList<>();

            if (((DataColumnCollection) dtMst.getColumns()).get("MTL_SHEET_ID") != null)
                mst.WsID = drMst.getValue("MTL_SHEET_ID").toString();

            for (DataRow dr : dtDet.Rows) {
                det = new WarehouseStorageDetObj();

                if (((DataColumnCollection)dtDet.getColumns()).get("SEQ") != null)
                    det.setSeq("0" + dr.getValue("SEQ").toString());

                if (((DataColumnCollection)dtDet.getColumns()).get("ITEM_ID") != null)
                    det.setItemId(dr.getValue("ITEM_ID").toString());

                if (((DataColumnCollection)dtDet.getColumns()).get("LOT_ID") != null)
                    det.setLotId(dr.getValue("LOT_ID").toString());

                if (((DataColumnCollection)dtDet.getColumns()).get("QTY") != null)
                    det.setQty("0" + dr.getValue("QTY").toString());

                if (((DataColumnCollection)dtDet.getColumns()).get("SCRAP_QTY") != null)
                    det.setScarpQty("0" + dr.getValue("SCRAP_QTY").toString());

                if (((DataColumnCollection)dtDet.getColumns()).get("TEMP_BIN") != null)
                    det.setTempBin(dr.getValue("TEMP_BIN").toString());

                if (((DataColumnCollection)dtDet.getColumns()).get("MFG_DATE") != null)
                    det.setMfgDate(dr.getValue("MFG_DATE").toString().substring(0,10) + " 00:00:00");

                if (((DataColumnCollection)dtDet.getColumns()).get("EXP_DATE") != null)
                    det.setExpDate(dr.getValue("EXP_DATE").toString().substring(0,10) + " 23:59:59");

                lstDet.add(det);
            }
            mst.setWsDetails(lstDet);
            return mst;
        }

        public String getWsID() { return WsID; }

        public void setWsID(String wsID) { WsID = wsID; }

        public ArrayList<WarehouseStorageDetObj> getWsDetails() { return WsDetails; }

        public void setWsDetails(ArrayList<WarehouseStorageDetObj> wsDetails) { WsDetails = wsDetails; }

    }

    public class WarehouseStorageDetObj {
        private BigDecimal Seq;
        private String ItemId;
        private String ItemKey;
        private String LotId;
        private BigDecimal Qty;
        private BigDecimal ScarpQty;
        private String TempBin;
        private Date MfgDate;
        private Date ExpDate;

        public WarehouseStorageDetObj() {}

        public BigDecimal getSeq() { return Seq; }

        public void setSeq(String seq) { Seq = new BigDecimal(seq); }

        public String getItemId() { return ItemId; }

        public void setItemId(String itemId) { ItemId = itemId; }

        public String getItemKey() { return ItemKey; }

        public void setItemKey(String itemKey) { ItemKey = itemKey; }

        public String getLotId() { return LotId; }

        public void setLotId(String lotId) { LotId = lotId; }

        public BigDecimal getQty() { return Qty; }

        public void setQty(String qty) { Qty = new BigDecimal(qty); }

        public BigDecimal getScarpQty() { return ScarpQty; }

        public void setScarpQty(String scrapQty) { ScarpQty = new BigDecimal(scrapQty); }

        public String getTempBin() { return TempBin; }

        public void setTempBin(String tempBin) { TempBin = tempBin; }

        public Date getMfgDate() { return MfgDate; }

        public void setMfgDate(String mfgDate) {
            String pattern = "yyyy-MM-dd HH:mm:ss";
            try {
                MfgDate = new SimpleDateFormat(pattern).parse(mfgDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public Date getExpDate() { return ExpDate; }

        public void setExpDate(String expDate) {
            String pattern = "yyyy-MM-dd HH:mm:ss";
            try {
                ExpDate = new SimpleDateFormat(pattern).parse(expDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

}
