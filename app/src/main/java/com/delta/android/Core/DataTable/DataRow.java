package com.delta.android.Core.DataTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class DataRow extends HashMap<String, Object> implements Serializable {

    /**
     * 在getValue()和setValue()時候，程式碼須透過此成員的欄位名稱來找出Map字典裡的物件
     */
    private ArrayList<DataColumn> columns;

    /**
     * 此資料列所屬的DataTable，唯讀
     */
    private DataTable table;

    /**
     * DataRow被建立時，必須指定所屬的DataTable
     *
     */
    public DataRow(DataTable table) {
        this.table = table;
    }

    public DataRow(DataTable dt, HashMap<String, Object> hm) {
        this.table = dt;
        for (String key : hm.keySet()) {
            this.put(key, hm.get(key));
        }
    }

    /**
     * 取得DataRow所屬的DataTable
     *
     * @return DataTable
     */
    public DataTable getTable() {
        return this.table;
    }

    /**
     * 設定該列該行的值
     *
     * @param columnindex 行索引(從0算起)
     * @param value       要設定的值
     */
    public void setValue(int columnindex, Object value) {
        setValue(this.columns.get(columnindex), value);
    }

    /**
     * 設定該列該行的值
     *
     * @param columnName 行名稱
     * @param value      要設定的值
     */
    public void setValue(String columnName, Object value) {
        this.put(columnName, value);
    }

    /**
     * 設定該列該行的值
     *
     * @param column DataColumn物件
     * @param value  要設定的值
     */
    private void setValue(DataColumn column, Object value) {
        if (column != null) {
            String columnName = column.ColumnName; //20210119 archie 將轉小寫拿掉
            if (this.containsKey(columnName))
                this.remove(columnName);
            this.put(columnName, value);
        }
    }

    /**
     * 取得該列該行的值
     *
     * @param columnIndex 行索引(從0算起)
     * @return Object
     */
    public Object getValue(int columnIndex) {
        String columnName = this.columns.get(columnIndex).ColumnName;//取得Key
        return this.get(columnName);
    }

    /**
     * 取得該列該行的值
     *
     * @param columnName 行名稱
     * @return Object
     */
    public Object getValue(String columnName) {
        return this.get(columnName);//利用欄名(Key)來取值
    }

    /**
     * 取得該列該行的值
     *
     * @param column DataColumn物件
     * @return Object
     */
    public Object getValue(DataColumn column) {
        return this.get(column.ColumnName);//利用欄名(Key)來取值
    }

    /**
     * 因Value值可能會是Null，故塞空字串處理
     *
     * @param key ID值  value 對應值
     * @return Object
     */
    @Override
    public Object put(String key, Object value) {
        if (value == null) {
            value = "";
        }
        return super.put(key, value);
    }
}