package com.delta.android.WMS.Param;

import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BGoodReceiptReceiveEditParam extends BStockBaseParam {

    public static final String GrMasterObj = "Unicom.Uniworks.BModule.WMS.INV.Parameter.GoodReceiptReceiveEditParam.GrMasterObj"; //BGoodReceiptReceiveEditParam.GrMasterObj

    public static final String StorageInTempBin = "Unicom.Uniworks.BModule.WMS.INV.Parameter.GoodReceiptReceiveEditParam.StorageInTempBin";
//

    public class GrEditMasterObj{

        private String GrID;
        public String getGrID(){
            return GrID;
        }
        public void setGrID(String grID){
            GrID = grID;
        }

        private String GrTypeID;
        public String getGrTypeID(){
            return GrTypeID;
        }
        public void setGrTypeID(String grTypeID){
            GrTypeID = grTypeID;
        }

        private String GrTypeKey;
        public String getGrTypeKey(){
            return GrTypeKey;
        }
        public void setGrTypeKey(String grTypeKey){
            GrTypeKey = grTypeKey;
        }

        private String GrSource;
        public String getGrSource(){
            return GrSource;
        }
        public void setGrSource(String grSource){
            GrSource = grSource;
        }

        private String VendorID;
        public String getVendorID(){
            return VendorID;
        }
        public void setVendorID(String grVendorID){
            VendorID = grVendorID;
        }

        private String VendorKey;
        public String getVendorKey(){
            return VendorKey;
        }
        public void setVendorKey(String grVendorKey){
            VendorKey = grVendorKey;
        }

        private String VendorShipNO;
        public String getVendorShipNo(){
            return VendorShipNO;
        }
        public void setVendorShipNo(String vendorShipNo){
            VendorShipNO = vendorShipNo;
        }

        private String VendorShipDate;
        public String getVendorShipDate(){
            return VendorShipDate;
        }
        public void setVendorShipDate(String vendorShipDate){
            VendorShipDate = vendorShipDate;
        }

        //20220719 archie add customer
        private String CustomerID;
        public String getCustomerID(){
            return CustomerID;
        }
        public void setCustomerID(String grCustomerID){
            CustomerID = grCustomerID;
        }

        private String CustomerKey;
        public String getCustomerKey(){
            return CustomerKey;
        }
        public void setCustomerKey(String grCustomerKey){
            CustomerKey = grCustomerKey;
        }

        private ArrayList<GrEditDetObj> GrDetails;

        public ArrayList<GrEditDetObj> GrDetails() {return GrDetails;}
        public void setGrDetails(ArrayList<GrEditDetObj> grDetails){ GrDetails = grDetails; }

        public GrEditMasterObj()
        {
        }

        public final GrEditMasterObj GetGrrSheet(DataTable dtMst, DataTable dtDet, DataTable dtSn){

            GrEditMasterObj mst = new GrEditMasterObj();
            GrEditDetObj det = null;

            mst.setGrID(dtMst.Rows.get(0).getValue("GR_ID").toString());
            mst.setGrTypeID(dtMst.Rows.get(0).getValue("GR_TYPE_ID").toString());
            mst.setGrSource(dtMst.Rows.get(0).getValue("GR_SOURCE").toString());
            mst.setVendorID(dtMst.Rows.get(0).getValue("VENDOR_ID").toString());
            mst.setVendorShipNo(dtMst.Rows.get(0).getValue("VENDOR_SHIP_NO").toString());
            mst.setGrTypeKey(dtMst.Rows.get(0).getValue("GR_TYPE_KEY").toString());
            mst.setVendorKey(dtMst.Rows.get(0).getValue("VENDOR_KEY").toString());
            mst.setCustomerID(dtMst.Rows.get(0).getValue("CUSTOMER_ID").toString());
            mst.setCustomerKey(dtMst.Rows.get(0).getValue("CUSTOMER_KEY").toString());

            if (!(dtMst.Rows.get(0).getValue("VENDOR_SHIP_DATE").toString().equals("")))
                mst.setVendorShipDate(dtMst.Rows.get(0).getValue("VENDOR_SHIP_DATE").toString().substring(0,10) + " 00:00:00");

            int seq = 0;
            ArrayList<GrEditDetObj> detList = new ArrayList<>();
            for (DataRow dr: dtDet.Rows){
                seq++;
                det = new GrEditDetObj();

                det.setPOSeq("0"+dr.getValue("PO_SEQ").toString());
                det.setPONO(dr.getValue("PONO").toString());
                det.setStorageId(dr.getValue("STORAGE_ID").toString());
                det.setStorageKey(dr.getValue("STORAGE_KEY").toString());
                det.setItemId(dr.getValue("ITEM_ID").toString());
                det.setItemKey(dr.getValue("ITEM_KEY").toString());
                det.setLotId(dr.getValue("LOT_ID").toString());
                det.setQty("0"+dr.getValue("QTY").toString());
                det.setUom(dr.getValue("UOM").toString());
                det.setCmt(dr.getValue("CMT").toString());
                det.setSkipQc(dr.getValue("SKIP_QC").toString());
                det.setSpecLot(dr.getValue("SPEC_LOT").toString());
                if (!(dr.getValue("VENDOR_ITEM_ID").toString().equals("")))
                    det.setVendorItemId(dr.getValue("VENDOR_ITEM_ID").toString());
                if (!(dr.getValue("SIZE_KEY").toString().equals("")))
                    det.setSizeKey(dr.getValue("SIZE_KEY").toString());
                if (!(dr.getValue("SIZE_ID").toString().equals("")))
                    det.setSizeId(dr.getValue("SIZE_ID").toString());
                if (!(dr.getValue("LOT_CODE").toString().equals("")))
                    det.setLotCode(dr.getValue("LOT_CODE").toString());
                if (!(dr.getValue("MFG_DATE").toString().equals("")))
                    det.setMfgDate(dr.getValue("MFG_DATE").toString().substring(0,10) + " 00:00:00");
                if (!(dr.getValue("EXP_DATE").toString().equals("")))
                    det.setExpDate(dr.getValue("EXP_DATE").toString().substring(0,10) + " 23:59:59");
                if (!(dr.getValue("REC_QRCODE").toString().equals("")))
                    det.setRecQRCode(dr.getValue("REC_QRCODE").toString());
                if (!(dr.getValue("REC_BARCODE").toString().equals("")))
                    det.setRecBarCode(dr.getValue("REC_BARCODE").toString());

                det.setSeq("0"+seq);

                if (dtSn != null && dr.getValue("GRR_DET_SN_REF_KEY") != null){
                    ArrayList<GrrSnObj> lstSn = new ArrayList<>();
                    for (DataRow drSn : dtSn.Rows){
                        if(drSn.getValue("GRR_DET_SN_REF_KEY").toString().equals(dr.getValue("GRR_DET_SN_REF_KEY").toString())){
                            GrrSnObj grSn = new GrrSnObj();
                            grSn.setSnId(drSn.getValue("SN_ID").toString());
                            lstSn.add(grSn);
                        }
                    }
                    det.setGrSns(lstSn);
                }
                det.setTempBin(dr.getValue("TEMP_BIN").toString());
                detList.add(det);
            }
            mst.setGrDetails(detList);
            return mst;
        }
    }

    public class GrEditDetObj{

        private BigDecimal Seq;//
        private BigDecimal GrSeq;
        private String StorageId ;
        private String StorageKey ;
        private String PONO ;
        private BigDecimal POSeq;
        private String ItemId ;
        private String ItemKey;
        private String LotId ;
        private BigDecimal Qty ;
        private Date MfgDate;
        private Date ExpDate;
        private String Uom ;
        private String Cmt ;
        private String SkipQC ;
        private String SpecLot ;
        private String TempBin;
        private String SizeId;
        private String SizeKey;
        private String LotCode;
        private String VendorItemId;
        private String RecBarCode;
        private String RecQRCode;

        private ArrayList<GrrSnObj> GrSns;

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

        public String getPONO() {
            return PONO;
        }
        public void setPONO(String poNo) {
            PONO = poNo;
        }

        public String getSpecLot() {
            return SpecLot;
        }
        public void setSpecLot(String specLot) {
            SpecLot = specLot;
        }

        public BigDecimal getSeq() {
            return Seq;
        }
        public void setSeq(String seq) {
            Seq = new BigDecimal(seq);
        }

        public BigDecimal getGrSeq() {
            return GrSeq;
        }
        public void setGrSeq(String grSeq) {
            GrSeq = new BigDecimal(grSeq);
        }

        public BigDecimal getQty() {
            return Qty;
        }
        public void setQty(String qty) {
            Qty = new BigDecimal(qty);
        }

        public BigDecimal getPOSeq() {
            return POSeq;
        }
        public void setPOSeq(String poSeq) {
            POSeq = new BigDecimal(poSeq);
        }

        public String getSkipQc() {
            return SkipQC;
        }
        public void setSkipQc(String skipQc) {
            SkipQC = skipQc;
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

        public String getLotCode() {
            return LotCode;
        }
        public void setLotCode(String lotCode) {
            LotCode = lotCode;
        }

        public String getSizeKey() {
            return SizeKey;
        }
        public void setSizeKey(String sizeKey) {
            SizeKey = sizeKey;
        }

        public String getSizeId() {
            return SizeId;
        }
        public void setSizeId(String sizeId) {
            SizeId = sizeId;
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

        public ArrayList<GrrSnObj> getGrSns(){return GrSns;}
        public void setGrSns(ArrayList<GrrSnObj> grSns){
            GrSns = grSns;
        }

        public String getTempBin() { return TempBin; }
        public void setTempBin(String tempBin) { TempBin = tempBin; }
    }

    public class GrrSnObj
    {
        public String getSnId() {
            return SnId;
        }
        public void setSnId(String snId) {
            SnId = snId;
        }

        private String SnId ;
    }
}
