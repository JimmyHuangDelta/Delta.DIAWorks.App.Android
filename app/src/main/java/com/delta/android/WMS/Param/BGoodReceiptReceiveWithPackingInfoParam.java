package com.delta.android.WMS.Param;

import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

public class BGoodReceiptReceiveWithPackingInfoParam extends BStockBaseParam {

    public static final String TrxType = "Unicom.Uniworks.BModule.WMS.INV.Parameter.BGoodReceiptReceiveWithPackingInfoParam.TrxType";

    public static final String GrrMasterObj = "Unicom.Uniworks.BModule.WMS.INV.Parameter.BGoodReceiptReceiveWithPackingInfoParam.GrrMasterObj";

    public class GrrWithPackingInfoMasterObj {
        private String GrID;
        private ArrayList<GrrWithPackingInfoDetObj> GrDetails;

        public String getGrID() {
            return GrID;
        }

        public void setGrID(String grID) {
            GrID = grID;
        }

        public ArrayList<GrrWithPackingInfoDetObj> getGrDetails() {
            return GrDetails;
        }

        public void setGrDetails(ArrayList<GrrWithPackingInfoDetObj> grDetails) {
            GrDetails = grDetails;
        }

        public GrrWithPackingInfoMasterObj() {
        }

        public final GrrWithPackingInfoMasterObj GetGrrSheet(DataTable dtMst, DataTable dtDet, DataTable dtSn) {
            GrrWithPackingInfoMasterObj mst = new GrrWithPackingInfoMasterObj();
            mst.setGrID(dtMst.Rows.get(0).getValue("GR_ID").toString());

            GrrWithPackingInfoDetObj det = null;

            ArrayList<GrrWithPackingInfoDetObj> detList = new ArrayList<>();

            for (DataRow dr : dtDet.Rows) {

                det = new GrrWithPackingInfoDetObj();

                if (!(dr.getValue("SEQ").toString().equals("")))
                    det.setSeq("0" + dr.getValue("SEQ").toString());

                if (!(dr.getValue("STORAGE_ID").toString().equals("")))
                    det.setStorageId(dr.getValue("STORAGE_ID").toString());

                if (!(dr.getValue("ITEM_ID").toString().equals("")))
                    det.setItemId(dr.getValue("ITEM_ID").toString());

                if (!(dr.getValue("LOT_ID").toString().equals("")))
                    det.setLotId(dr.getValue("LOT_ID").toString());

                if (!(dr.getValue("QTY").toString().equals("")))
                    det.setQty("0" + dr.getValue("QTY").toString());

                if (!(dr.getValue("UOM").toString().equals("")))
                    det.setUom(dr.getValue("UOM").toString());

                if (!(dr.getValue("CMT").toString().equals("")))
                    det.setCmt(dr.getValue("CMT").toString());

                boolean bCheck = false;
                for (DataColumn dc : dtDet.getColumns()) {
                    if (dc.ColumnName.equals("TEMP_BIN")) {
                        bCheck = true;
                        break;
                    }
                }

                if (bCheck)
                    det.setTempBin(dr.getValue("TEMP_BIN").toString());

                if (!(dr.getValue("SKIP_QC").toString().equals("")))
                    det.setSkipQc(dr.getValue("SKIP_QC").toString());

                if (!(dr.getValue("MFG_DATE").toString().equals("")))
                    det.setMfgDate(dr.getValue("MFG_DATE").toString().substring(0, 10) + " 00:00:00");

                if (!(dr.getValue("EXP_DATE").toString().equals("")))
                    det.setExpDate(dr.getValue("EXP_DATE").toString().substring(0, 10) + " 23:59:59");

                if (!(dr.getValue("LOT_CODE").toString().equals("")))
                    det.setLotCode(dr.getValue("LOT_CODE").toString());

                if (!(dr.getValue("SIZE_ID").toString().equals("")))
                    det.setSizeId(dr.getValue("SIZE_ID").toString());

                if (!(dr.getValue("REC_BARCODE").toString().equals("")))
                    det.setRecBarCode(dr.getValue("REC_BARCODE").toString());

                if (!(dr.getValue("REC_QRCODE").toString().equals("")))
                    det.setRecQRCode(dr.getValue("REC_QRCODE").toString());

                if (!(dr.getValue("VENDOR_ITEM_ID").toString().equals("")))
                    det.setVendorItemId(dr.getValue("VENDOR_ITEM_ID").toString());

                if (!(dr.getValue("BOX1_ID").toString().equals("")))
                    det.setBox1Id(dr.getValue("BOX1_ID").toString());

                if (!(dr.getValue("BOX2_ID").toString().equals("")))
                    det.setBox2Id(dr.getValue("BOX2_ID").toString());

                if (!(dr.getValue("BOX3_ID").toString().equals("")))
                    det.setBox3Id(dr.getValue("BOX3_ID").toString());

                if (!(dr.getValue("PALLET_ID").toString().equals("")))
                    det.setPalletId(dr.getValue("PALLET_ID").toString());

                if (dtSn != null && dr.getValue("GRR_DET_SN_REF_KEY") != null) {//&& dtDet.getColumns().contains("GRR_DET_SN_REF_KEY")
                    ArrayList<GrrWithPackingInfoSnObj> lstSn = new ArrayList<GrrWithPackingInfoSnObj>();
                    for (DataRow drSn : dtSn.Rows) {
                        if (drSn.getValue("GRR_DET_SN_REF_KEY").toString().equals(dr.getValue("GRR_DET_SN_REF_KEY").toString())) {
                            GrrWithPackingInfoSnObj grSn = new GrrWithPackingInfoSnObj();
                            grSn.setSnId(drSn.getValue("SN_ID").toString());
                            lstSn.add(grSn);
                        }
                    }
                    det.setGrSns(lstSn);
                }

                detList.add(det);

            }
            mst.setGrDetails(detList);
            return mst;
        }

    }

    public class GrrWithPackingInfoDetObj {
        private BigDecimal Seq;
        private String StorageId;
        private String StorageKey;
        private String ItemId;
        private String ItemKey;
        private String LotId;
        private BigDecimal Qty;
        private Date MfgDate;
        private Date ExpDate;
        private String Uom;
        private String Cmt;
        private String TempBin;
        private String LotCode;
        private String SizeId;
        private String Po;
        private BigDecimal PoSeq;
        private String VendorItemId;
        private String RecBarCode;
        private String RecQRCode;
        private String Box1Id;
        private String Box2Id;
        private String Box3Id;
        private String PalletId;

        private String SkipQc;

        private LinkedHashMap<String, String> ExtendColumns;
        private ArrayList<GrrWithPackingInfoSnObj> GrSns;

        public String getTempBin() {
            return TempBin;
        }

        public void setTempBin(String tempBin) {
            TempBin = tempBin;
        }

        public String getStorageId() {
            return StorageId;
        }

        public void setStorageId(String storageId) {
            StorageId = storageId;
        }

        public String getStorageKey() {
            return StorageKey;
        }

        public void setStorageKey(String storageKey) {
            StorageKey = storageKey;
        }

        public String getItemId() {
            return ItemId;
        }

        public void setItemId(String itemId) {
            ItemId = itemId;
        }

        public String getItemKey() {
            return ItemKey;
        }

        public void setItemKey(String itemKey) {
            ItemKey = itemKey;
        }

        public String getLotId() {
            return LotId;
        }

        public void setLotId(String lotId) {
            LotId = lotId;
        }

        public String getUom() {
            return Uom;
        }

        public void setUom(String uom) {
            Uom = uom;
        }

        public String getCmt() {
            return Cmt;
        }

        public void setCmt(String cmt) {
            Cmt = cmt;
        }

        public String getLotCode() {
            return LotCode;
        }

        public void setLotCode(String lotCode) {
            LotCode = lotCode;
        }

        public String getSizeId() {
            return SizeId;
        }

        public void setSizeId(String sizeId) {
            SizeId = sizeId;
        }

        public String getPo() {
            return Po;
        }

        public void setPo(String po) {
            Po = po;
        }

        public BigDecimal getPoSeq() {
            return PoSeq;
        }

        public void setPoSeq(String poSeq) {
            PoSeq = new BigDecimal(poSeq);
        }

        public String getVendorItemId() {
            return VendorItemId;
        }

        public void setVendorItemId(String vendorItemId) {
            VendorItemId = vendorItemId;
        }

        public String getRecBarCode() {
            return RecBarCode;
        }

        public void setRecBarCode(String recBarCode) {
            RecBarCode = recBarCode;
        }

        public String getRecQRCode() {
            return RecQRCode;
        }

        public void setRecQRCode(String recQRCode) {
            RecQRCode = recQRCode;
        }

        public String getBox1Id() {
            return Box1Id;
        }

        public void setBox1Id(String box1Id) {
            Box1Id = box1Id;
        }

        public String getBox2Id() {
            return Box2Id;
        }

        public void setBox2Id(String box2Id) {
            Box2Id = box2Id;
        }

        public String getBox3Id() {
            return Box3Id;
        }

        public void setBox3Id(String box3Id) {
            Box3Id = box3Id;
        }

        public String getPalletId() {
            return PalletId;
        }

        public void setPalletId(String palletId) {
            PalletId = palletId;
        }

        public BigDecimal getSeq() {
            return Seq;
        }

        public void setSeq(String seq) {
            Seq = new BigDecimal(seq);
        }

        public BigDecimal getQty() {
            return Qty;
        }

        public void setQty(String qty) {
            Qty = new BigDecimal(qty);
        }

        public String getSkipQc() {
            return SkipQc;
        }

        public void setSkipQc(String skipQc) {
            SkipQc = skipQc;
        }

        public Date getMfgDate() {
            return MfgDate;
        }

        public void setMfgDate(String mfgDate) {
            String pattern = "yyyy-MM-dd HH:mm:ss";
            try {
                MfgDate = new SimpleDateFormat(pattern).parse(mfgDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public Date getExpDate() {
            return ExpDate;
        }

        public void setExpDate(String expDate) {
            String pattern = "yyyy-MM-dd HH:mm:ss";
            try {
                ExpDate = new SimpleDateFormat(pattern).parse(expDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public LinkedHashMap<String, String> getExtend() {
            return ExtendColumns;
        }

        public void setExtend(LinkedHashMap<String, String> extendColumns) {
            ExtendColumns = extendColumns;
        }

        public ArrayList<GrrWithPackingInfoSnObj> getGrSns() {
            return GrSns;
        }

        public void setGrSns(ArrayList<GrrWithPackingInfoSnObj> grSns) {
            GrSns = grSns;
        }
    }

    public class GrrWithPackingInfoSnObj {
        public String getSnId() {
            return SnId;
        }

        public void setSnId(String snId) {
            SnId = snId;
        }

        private String SnId;
    }
}
