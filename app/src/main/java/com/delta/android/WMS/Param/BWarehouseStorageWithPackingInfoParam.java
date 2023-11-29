package com.delta.android.WMS.Param;

import com.delta.android.Core.DataTable.DataColumnCollection;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BWarehouseStorageWithPackingInfoParam extends BStockBaseParam {

    public static final String TrxType = "Unicom.Uniworks.BModule.WMS.INV.Parameter.BWarehouseStorageWithPackingInfoParam.TrxType";

    public static final String TrxMode = "Unicom.Uniworks.BModule.WMS.INV.Parameter.BWarehouseStorageWithPackingInfoParam.TrxMode";

    public static final String SheetTypePolicyId = "Unicom.Uniworks.BModule.WMS.INV.Parameter.BWarehouseStorageWithPackingInfoParam.SheetTypePolicyId";

    public static final String WsObj = "Unicom.Uniworks.BModule.WMS.INV.Parameter.BWarehouseStorageWithPackingInfoParam.WsObj";

    // WarehouseVoucherSheet (無單據入庫使用)
    public static final String WarehouseVoucherSheet = "Unicom.Uniworks.BModule.WMS.INV.Parameter.BWarehouseStorageWithPackingInfoParam.WarehouseVoucherSheet";

    public class WarehouseStorageWithPackingInfoMasterObj {

        private String WsID;

        private ArrayList<WarehouseStorageWithPackingInfoDetObj> WsDetails;

        public WarehouseStorageWithPackingInfoMasterObj() {}

        public final WarehouseStorageWithPackingInfoMasterObj getWsSheet(DataRow drMst, DataTable dtDet) {

            DataTable dtMst = drMst.getTable();
            WarehouseStorageWithPackingInfoMasterObj mst = new WarehouseStorageWithPackingInfoMasterObj();
            WarehouseStorageWithPackingInfoDetObj det = null;
            ArrayList<WarehouseStorageWithPackingInfoDetObj> lstDet = new ArrayList<>();

            if (((DataColumnCollection) dtMst.getColumns()).get("MTL_SHEET_ID") != null)
                mst.WsID = drMst.getValue("MTL_SHEET_ID").toString();

            for (DataRow dr : dtDet.Rows) {
                det = new WarehouseStorageWithPackingInfoDetObj();

//                if (((DataColumnCollection)dtDet.getColumns()).get("SEQ") != null)
                if (dr.getValue("SEQ") != null)
                    det.setSeq("0" + dr.getValue("SEQ").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("ITEM_ID") != null)
                if (dr.getValue("ITEM_ID") != null)
                    det.setItemId(dr.getValue("ITEM_ID").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("LOT_ID") != null)
                if (dr.getValue("LOT_ID") != null)
                    det.setLotId(dr.getValue("LOT_ID").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("QTY") != null)
                if (dr.getValue("QTY") != null)
                    det.setQty("0" + dr.getValue("QTY").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("SCRAP_QTY") != null)
                if (dr.getValue("SCRAP_QTY") != null)
                    det.setScarpQty("0" + dr.getValue("SCRAP_QTY").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("TEMP_BIN") != null)
                if (dr.getValue("TEMP_BIN") != null)
                    det.setTempBin(dr.getValue("TEMP_BIN").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("MFG_DATE") != null)
                if (dr.getValue("MFG_DATE") != null)
                    det.setMfgDate(dr.getValue("MFG_DATE").toString().substring(0,10) + " 00:00:00");

//                if (((DataColumnCollection)dtDet.getColumns()).get("EXP_DATE") != null)
                if (dr.getValue("EXP_DATE") != null)
                    det.setExpDate(dr.getValue("EXP_DATE").toString().substring(0,10) + " 23:59:59");

//                if (((DataColumnCollection)dtDet.getColumns()).get("CMT") != null)
                if (dr.getValue("CMT") != null)
                    det.setCmt(dr.getValue("CMT").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("UOM") != null)
                if (dr.getValue("UOM") != null)
                    det.setUom(dr.getValue("UOM").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("EXPOSURE_TIME") != null)
                if (dr.getValue("EXPOSURE_TIME") != null)
                    det.setExposureTime("0" + dr.getValue("EXPOSURE_TIME").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("MSD_AVAILABLE_DATE") != null)
                if (dr.getValue("MSD_AVAILABLE_DATE") != null) {
                    if (!dr.getValue("MSD_AVAILABLE_DATE").toString().equals(""))
                        det.setMsdAvailableDate(dr.getValue("MSD_AVAILABLE_DATE").toString().substring(0,10) + " 00:00:00");
                }

//                if (((DataColumnCollection)dtDet.getColumns()).get("PARENT_LOT_ID") != null)
                if (dr.getValue("PARENT_LOT_ID") != null)
                    det.setParentLotId(dr.getValue("PARENT_LOT_ID").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("ROOT_LOT_ID") != null)
                if (dr.getValue("ROOT_LOT_ID") != null)
                    det.setRootLotId(dr.getValue("ROOT_LOT_ID").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("PALLET_ID") != null)
                if (dr.getValue("PARENT_LOT_ID") != null)
                    det.setParentLotId(dr.getValue("PARENT_LOT_ID").toString());

                if (dr.getValue("PALLET_ID") != null)
                    det.setPalletId(dr.getValue("PALLET_ID").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("BOX3_ID") != null)
                if (dr.getValue("BOX3_ID") != null)
                    det.setBox3Id(dr.getValue("BOX3_ID").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("BOX2_ID") != null)
                if (dr.getValue("BOX2_ID") != null)
                    det.setBox2Id(dr.getValue("BOX2_ID").toString());

//                if (((DataColumnCollection)dtDet.getColumns()).get("BOX1_ID") != null)
                if (dr.getValue("BOX1_ID") != null)
                    det.setBox1Id(dr.getValue("BOX1_ID").toString());

                lstDet.add(det);
            }
            mst.setWsDetails(lstDet);
            return mst;
        }

        public String getWsID() { return WsID; }

        public void setWsID(String wsID) { WsID = wsID; }

        public ArrayList<WarehouseStorageWithPackingInfoDetObj> getWsDetails() { return WsDetails; }

        public void setWsDetails(ArrayList<WarehouseStorageWithPackingInfoDetObj> wsDetails) { WsDetails = wsDetails; }

    }

    public class WarehouseStorageWithPackingInfoDetObj {
        private BigDecimal Seq;
        private String ItemId;
        private String ItemKey;
        private String LotId;
        private BigDecimal Qty;
        private BigDecimal ScarpQty;
        private String TempBin;
        private Date MfgDate;
        private Date ExpDate;
        private String Cmt;
        private String Uom;
        private BigDecimal ExposureTime;
        private Date MsdAvailableDate;
        private String ParentLotId;
        private String RootLotId;
        private String Box1Id;
        private String Box2Id;
        private String Box3Id;
        private String PalletId;

        public WarehouseStorageWithPackingInfoDetObj() {}

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

        public String getCmt() { return Cmt; }

        public void setCmt(String cmt) { Cmt = cmt; }

        public String getUom() { return Uom; }

        public void setUom(String uom) { Uom = uom; }

        public BigDecimal getExposureTime() { return ExposureTime; }

        public void setExposureTime(String exposureTime) { ExposureTime = new BigDecimal(exposureTime); }

        public Date getMsdAvailableDate() { return MsdAvailableDate; }

        public void setMsdAvailableDate(String msdAvailableDate) {
            String pattern = "yyyy-MM-dd HH:mm:ss";
            try {
                MsdAvailableDate = new SimpleDateFormat(pattern).parse(msdAvailableDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public String getParentLotId() { return ParentLotId; }

        public void setParentLotId(String parentLotId) { ParentLotId = parentLotId; }

        public String getRootLotId() { return RootLotId; }

        public void setRootLotId(String rootLotId) { RootLotId = rootLotId; }

        public String getPalletId() { return PalletId; }

        public void setPalletId(String palletId) { PalletId = palletId; }

        public String getBox3Id() { return Box3Id; }

        public void setBox3Id(String box3Id) { Box3Id = box3Id; }

        public String getBox2Id() { return Box2Id; }

        public void setBox2Id(String box2Id) { Box2Id = box2Id; }

        public String getBox1Id() { return Box1Id; }

        public void setBox1Id(String box1Id) { Box1Id = box1Id; }
    }

}
