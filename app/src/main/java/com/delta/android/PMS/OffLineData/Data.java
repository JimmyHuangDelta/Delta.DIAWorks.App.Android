package com.delta.android.PMS.OffLineData;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.provider.BaseColumns._ID;

public class Data extends SQLiteOpenHelper {

    private static final String dbName = "PMS_ANDROID.db";//DB名稱
    private static final int version = 11; //DB版本。

    public Data(Context context) {
        super(context, dbName, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

//人員主檔
        db.execSQL("CREATE TABLE SBRM_USER" +
                "(" +
                "USER_ID CHAR PRIMARY KEY, " +
                "USER_NAME CHAR, " +
                "USER_SERIAL_KEY INTEGER " +
                ")");

        //sop主檔
        db.execSQL("CREATE TABLE SBRM_EMS_SOP" +
                "(" +
                "SOP_ID CHAR PRIMARY KEY, " +
                "SOP_NAME CHAR, " +
                "DOC_NO CHAR, " +
                "URL CHAR, " +
                "SOP_SERIAL_KEY INTEGER " +
                ")");

        //故障現象主檔
        db.execSQL("CREATE TABLE SBRM_EMS_FAIL" +
                "(" +
                "FAIL_ID CHAR PRIMARY KEY, " +
                "FAIL_NAME CHAR, " +
                "FAIL_SERIAL_KEY INTEGER " +
                ")");

        //故障原因主檔
        db.execSQL("CREATE TABLE SBRM_EMS_FAIL_REASON" +
                "(" +
                "FAIL_REASON_ID CHAR PRIMARY KEY, " +
                "FAIL_REASON_NAME CHAR, " +
                "FAIL_REASON_SERIAL_KEY INTEGER " +
                ")");

        //故障處置主檔
        db.execSQL("CREATE TABLE SBRM_EMS_FAIL_STRATEGY" +
                "(" +
                "FAIL_STRATEGY_ID CHAR PRIMARY KEY, " +
                "FAIL_STRATEGY_NAME CHAR, " +
                "FAIL_STRATEGY_SERIAL_KEY INTEGER " +
                ")");

        //保養工具主檔
        db.execSQL("CREATE TABLE SBRM_EMS_FIX_TOOL" +
                "(" +
                "FIX_TOOL_ID CHAR PRIMARY KEY, " +
                "FIX_TOOL_NAME CHAR, " +
                "FIX_TOOL_SERIAL_KEY INTEGER " +
                ")");

        //保養方法主黨
        db.execSQL("CREATE TABLE SBRM_EMS_PM_METHOD" +
                "(" +
                "PM_METHOD_ID CHAR PRIMARY KEY, " +
                "PM_METHOD_NAME CHAR, " +
                "PM_METHOD_SERIAL_KEY INTEGER " +
                ")");

        //保養耗材主檔
        db.execSQL("CREATE TABLE SBRM_EMS_CONSUMABLE_LIST" +
                "(" +
                "CONSUMABLE_LIST_ID CHAR PRIMARY KEY, " +
                "CONSUMABLE_LIST_NAME CHAR, " +
                "CONSUMABLE_TYPE_ID CHAR, " +
                "CONSUMABLE_TYPE_NAME CHAR, " +
                "CONSUMABLE_LIST_SERIAL_KEY INTEGER " +
                ")");

        //零件清單
        db.execSQL("CREATE TABLE SBRM_PART" +
                "(" +
                "PART_ID CHAR PRIMARY KEY, " +
                "PART_NAME CHAR, " +
                "PART_GROUP_ID CHAR, " +
                "PART_GROUP_NAME CHAR, " +
                "PART_SERIAL_KEY INTEGER " +
                ")");

        //零件群組
        db.execSQL("CREATE TABLE SBRM_PART_GROUP" +
                "(" +
                "PART_GROUP_ID CHAR PRIMARY KEY, " +
                "PART_GROUP_NAME CHAR, " +
                "PART_GROUP_SERIAL_KEY INTEGER " +
                ")");

        //倉庫主檔
        db.execSQL("CREATE TABLE SBRM_STORAGE" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "STORAGE_ID CHAR, " +
                "STORAGE_NAME CHAR, " +
                "STORAGE_TYPE CHAR, " +
                "USER_ID CHAR, " +
                "STORAGE_SERIAL_KEY INTEGER " +
                ")");

        //儲位主檔
        db.execSQL("CREATE TABLE SBRM_BIN" +
                "(" +
                "BIN_ID CHAR PRIMARY KEY, " +
                "BIN_NAME CHAR, " +
                "STORAGE_ID CHAR, " +
                "STORAGE_NAME CHAR, " +
                "BIN_SERIAL_KEY INTEGER " +
                ")");

        //人員對應機台主檔
        db.execSQL("CREATE TABLE SBRM_EMS_EQP_SUB_ROLE" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "USER_ID CHAR, " +
                "USER_NAME CHAR, " +
                "EQP_ID CHAR, " +
                "TECH_TYPE CHAR, " +
                "EQP_SUB_ROLE_SERIAL_KEY INTEGER " +
                ")");

        //庫存零件
        db.execSQL("CREATE TABLE SMTL_INVENTORY" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ITEM_ID CHAR, " +
                "MTL_LOT_ID CHAR, " +
                "STORAGE_ID CHAR, " +
                "BIN_ID CHAR, " +
                "QTY INTEGER " +
                ")");

        db.execSQL("CREATE TABLE SBRM_EMS_CALL_FIX_TYPE" +
                "(" +
                "CALL_FIX_TYPE_ID CHAR PRIMARY KEY, " +
                "CALL_FIX_TYPE_NAME CHAR, " +
                "CALL_FIX_TYPE_SERIAL_KEY INTEGER " +
                ")");

        //                                                       保養相關                                                       //
        //保養項目對應保養耗材
        db.execSQL("CREATE TABLE SBRM_EMS_CHECK_CONSUMABLE" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "CONSUMABLE_LIST_ID CHAR, " +
                "CONSUMABLE_LIST_NAME CHAR, " +
                "CONSUMABLE_TYPE_ID CHAR, " +
                "CONSUMABLE_TYPE_NAME CHAR, " +
                "CHECK_ID CHAR, " +
                "CHECK_NAME CHAR," +
                "MRO_WO_ID CHAR " +
                ")");

        //保養項目對應保養工具
        db.execSQL("CREATE TABLE SBRM_EMS_CHECK_FIX_TOOL" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "FIX_TOOL_ID CHAR, " +
                "FIX_TOOL_NAME CHAR, " +
                "CHECK_ID CHAR, " +
                "CHECK_NAME CHAR," +
                "MRO_WO_ID CHAR " +
                ")");


        //保養項目對應保養方法
        db.execSQL("CREATE TABLE SBRM_EMS_CHECK_METHOD" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "PM_METHOD_ID CHAR, " +
                "PM_METHOD_NAME CHAR, " +
                "CHECK_ID CHAR, " +
                "CHECK_NAME CHAR," +
                "MRO_WO_ID CHAR " +
                ")");


        //保養工單_SOP
        db.execSQL("CREATE TABLE SBRM_EMS_PM_SOP" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "SOP_ID CHAR, " +
                "SOP_NAME CHAR, " +
                "DOC_NO CHAR, " +
                "URL CHAR, " +
                "FULL_FILE_NAME CHAR, " +
                "MRO_WO_ID CHAR" +
                ")");


        //保養工單_檢查項目
        db.execSQL("CREATE TABLE SEMS_PM_WO_CHECK" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MRO_WO_ID CHAR, " +
                "CHECK_ID CHAR, " +
                "CHECK_NAME CHAR, " +
                "CHECK_TYPE CHAR, " +
                "USL CHAR, " +
                "LSL CHAR, " +
                "TARGET CHAR, " +
                "DESC_TYPE CHAR, " +
                "UOM CHAR, " +
                "CHECK_VALUE CHAR, " +
                "CHECK_RESULT CHAR," +
                "CHECK_USER_KEY CHAR," +
                "STD_HOUR CHAR," +
                "CMT CHAR" +
                ")");


        //                                                       維修相關                                                       //
        //維修工單_故障現象
        db.execSQL("CREATE TABLE SEMS_REPAIR_FAIL" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MRO_WO_ID CHAR, " +
                "FAIL_ID CHAR, " +
                "FAIL_NAME CHAR, " +
                "FAIL_CMT CHAR " +
                ")");


        //維修工單_故障原因
        db.execSQL("CREATE TABLE SEMS_REPAIR_WO_RSN" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MRO_WO_ID CHAR, " +
                "FAIL_REASON_ID CHAR, " +
                "FAIL_REASON_NAME CHAR, " +
                "FAIL_REASON_CMT CHAR " +
                ")");


        //維修工單_故障處置
        db.execSQL("CREATE TABLE SEMS_REPAIR_WO_STY" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MRO_WO_ID CHAR, " +
                "FAIL_STRATEGY_ID CHAR, " +
                "FAIL_STRATEGY_NAME CHAR, " +
                "FAIL_STRATEGY_CMT CHAR " +
                ")");


        //                                                       保養維修相關                                                       //
        //零件交易履歷
        db.execSQL("CREATE TABLE SEMS_MRO_PART_TRX" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TRX_DATE DATETIME, " +
                "MRO_WO_ID CHAR, " +
                "PART_ID CHAR, " +
                "PART_LOT_ID CHAR, " +
                "PART_QTY NUMERIC(10,0), " +
                "IS_NEW CHAR DEFAULT 'N', " +
                "STORAGE_ID CHAR, " +
                "BIN_ID CHAR, " +
                "TRX_MODE CHAR, " +
                "CMT CHAR" +
                ")");


        //人員工時
        db.execSQL("CREATE TABLE SEMS_MRO_WO_WH" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MRO_WO_ID CHAR, " +
                "USER_ID CHAR, " +
                "USER_NAME CHAR, " +
                "START_DT DATETIME, " +
                "END_DT DATETIME, " +
                "CMT CHAR" +
                ")");


        //機台零件
        db.execSQL("CREATE TABLE SEMS_EQP_PART" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "EQP_ID CHAR, " +
                "PART_ID CHAR, " +
                "PART_LOT_ID CHAR, " +
                "PART_QTY NUMERIC(10,0), " +
                "CMT CHAR" +
                ")");


        //檔案上傳
        db.execSQL("CREATE TABLE SEMS_FILE" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MRO_WO_ID CHAR, " +
                "FILE_NAME CHAR, " +
                "FILE_DESC CHAR, " +
                "LOCAL_FILE_PATH CHAR, " +
                "UPLOAD_USER_ID CHAR, " +
                "UPLOAD_DATE DATETIME, " +
                "ERROR_MSG CHAR" +
                ")");


        //保養維修工單
        db.execSQL("CREATE TABLE SEMS_MRO_WO" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MRO_WO_SERIAL_KEY NUMERIC(10,0), " +
                "MRO_WO_ID CHAR, " +
                "MRO_WO_TYPE CHAR, " +
                "WO_STATUS CHAR, " +
                "EQP_ID CHAR, " +
                "EQP_NAME CHAR, " +
                "PLAN_DT DATETIME, " +
                "START_DT DATETIME, " +
                "END_DT DATETIME, " +
                "FAIL_END_DT DATETIME, " +
                "PLAN_END_DT DATETIME, " +
                "PM_ID CHAR, " +
                "PM_NAME CHAR, " +
                "CALL_FIX_TYPE_ID CHAR, " +
                "CALL_FIX_TYPE_NAME CHAR, " +
                "IS_CHANGE CHAR default 'N', " +
                "NEED_UPLOAD CHAR default 'N', " + //online上船失敗時，用來判斷是否需要上傳的依據
                "CMT CHAR, " +
                "TTL_MAN_HOUR NUMERIC(10,0), " +
                "EXC_MAN_HOUR NUMERIC(10,0)" +
                ")");

        //保養單轉拋維修單 (sqlite用，純紀錄要轉拋維修工單的 "保養單號、故障現象")
        db.execSQL("CREATE TABLE TEMP_REPAIR" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MRO_WO_ID CHAR, " +
                "CALL_FIX_TYPE_ID CHAR, " +
                "FAIL_DT CHAR, " +
                "CMT CHAR, " +
                "FAIL_ID CHAR " +
                ")");

        //紀錄工單與人員的關係 (用在卡控權限)
        db.execSQL("CREATE TABLE USER_WO" +
                "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MRO_WO_ID CHAR, " +
                "USER_ID CHAR, " +
                "MRO_WO_TYPE CHAR " +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //region 開發用更新方式，後續release更新方式應該用板號判斷要執行那些script
//        db.execSQL("DROP TABLE IF EXISTS SBRM_USER");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_SOP");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_FAIL");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_FAIL_REASON");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_FAIL_STRATEGY");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_FIX_TOOL");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_PM_METHOD");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_CONSUMABLE_LIST");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_PART");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_PART_GROUP");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_STORAGE");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_BIN");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_EQP_USER_GROUP");
//        db.execSQL("DROP TABLE IF EXISTS SMTL_INVENTORY");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_CHECK_CONSUMABLE");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_CHECK_FIX_TOOL");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_CHECK_METHOD");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_PM_SOP");
//        db.execSQL("DROP TABLE IF EXISTS SEMS_PM_WO_CHECK");
//        db.execSQL("DROP TABLE IF EXISTS SEMS_REPAIR_FAIL");
//        db.execSQL("DROP TABLE IF EXISTS SEMS_REPAIR_WO_RSN");
//        db.execSQL("DROP TABLE IF EXISTS SEMS_REPAIR_WO_STY");
//        db.execSQL("DROP TABLE IF EXISTS SEMS_MRO_PART_TRX");
//        db.execSQL("DROP TABLE IF EXISTS SEMS_MRO_WO_WH");
//        db.execSQL("DROP TABLE IF EXISTS SEMS_EQP_PART");
//        db.execSQL("DROP TABLE IF EXISTS SEMS_FILE");
//        db.execSQL("DROP TABLE IF EXISTS SEMS_MRO_WO");
//        db.execSQL("DROP TABLE IF EXISTS TEMP_REPAIR");
//        db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_CALL_FIX_TYPE");
//        db.execSQL("DROP TABLE IF EXISTS USER_WO");
//
//        onCreate(db); //重新建立DB
        //endregion

        for (int i = oldVersion + 1; i <= newVersion; i++) {
            ExecUpdateScript(i, db);
        }
    }

    public void ExecUpdateScript(int version, SQLiteDatabase db) {
        switch (version) {
            case 11:
                db.execSQL("DROP TABLE IF EXISTS SBRM_EMS_EQP_USER_GROUP");
                //人員對應機台主檔
                db.execSQL("CREATE TABLE SBRM_EMS_EQP_SUB_ROLE" +
                        "(" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "USER_ID CHAR, " +
                        "USER_NAME CHAR, " +
                        "EQP_ID CHAR, " +
                        "TECH_TYPE CHAR, " +
                        "EQP_SUB_ROLE_SERIAL_KEY INTEGER " +
                        ")");
                break;

            case 12:
                break;

            case 13:
                break;
        }
    }
}
