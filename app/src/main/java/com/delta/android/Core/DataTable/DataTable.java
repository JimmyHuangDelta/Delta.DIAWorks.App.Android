package com.delta.android.Core.DataTable;

import com.google.gson.internal.StringMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author andychen
 */
public class DataTable implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3154295168104233966L;
    /*
     * 保存DataRow的集合，在DataTable初始化時，便會建立
     */
    public ArrayList<DataRow> Rows;
    /**
     * 保存DataColumn的集合，在DataTable初始化時，便會建立
     */
    private ArrayList<DataColumn> Columns;
    /**
     * DataTable的名稱，沒什麼用到
     */
    public String TableName;

    /**
     * 初始化DataTable，並建立DataColumnCollection，DataRowCollection
     */
    public DataTable() {
        this.Columns = new DataColumnCollection(this);
        this.Rows = new DataRowCollection(this);

    }

    public ArrayList<DataColumn> getColumns() {
        return this.Columns;
    }

    public void addColumn(DataColumn dc) {
        this.Columns.add(dc);
    }

    /**
     * 除了初始化DataTable， 可以指定DataTable的名字(沒什麼意義)
     */
    public DataTable(String tableName) {
        this();
        this.TableName = tableName;
    }


    /**
     * 由此DataTable物件來建立一個DataRow物件
     *
     * @return DataRow
     */
    public DataRow newRow() {

        return new DataRow(this);
    }


    /**
     * 把DataTable當做二維陣列，給列索引和行索引，設定值的方法
     * <br/>(發佈者自行寫的方法)
     *
     * @param rowIndex    列索引(從0算起)
     * @param columnIndex 行索引(從0算起)
     * @param value       要給的值
     */
    public void setValue(int rowIndex, int columnIndex, Object value) {
        this.Rows.get(rowIndex).setValue(columnIndex, value);
    }

    /**
     * 把DataTable當做二維陣列，給列索引和行名稱，設定值的方法
     * <br/>(發佈者自行寫的方法)
     *
     * @param rowIndex   列索引(從0算起)
     * @param columnName 行名稱
     * @param value      要給的值
     */
    public void setValue(int rowIndex, String columnName, Object value) {
        this.Rows.get(rowIndex).setValue(columnName, value); //20210119 archie 將轉小寫拿掉
    }


    /**
     * 把DataTable當做二維陣列，給列索引和行索引，取得值的方法
     * <br/>(發佈者自行寫的方法)
     *
     * @param rowIndex    列索引(從0算起)
     * @param columnIndex 行索引(從0算起)
     * @return 回傳該位置的值
     */
    public Object getValue(int rowIndex, int columnIndex) {
        return this.Rows.get(rowIndex).getValue(columnIndex);
    }


    /**
     * 把DataTable當做二維陣列，給列索引和行名稱，取得值的方法
     * <br/>(發佈者自行寫的方法)
     *
     * @param rowindex   列索引(從0算起)
     * @param columnName 行名稱
     * @return 回傳該位置的值
     */
    public Object getValue(int rowindex, String columnName) {
        return this.Rows.get(rowindex).getValue(columnName); //20210119 archie 將轉小寫拿掉
    }

    public static DataTable fromArrayList(ArrayList<StringMap<?>> arr) {
        DataTable dt = new DataTable();

        for (StringMap<?> child : arr) {
            HashMap<String, Object> hm = convertStringMapToHashMap(child);
            DataRow dr = new DataRow(dt, hm);
            dt.Rows.add(dr);
        }

        if (dt.Rows.size() > 0) {
            for (String col : dt.Rows.get(0).keySet()) {
                DataColumn dc = new DataColumn(col);
                dc.setTable(dt);
                dt.addColumn(dc);
            }
        }

        return dt;
    }

    public static HashMap<String, Object> convertStringMapToHashMap(StringMap<?> stringMap) {
        HashMap<String, Object> res = new HashMap<String, Object>();


        for (String key : stringMap.keySet()) {
            Object value = stringMap.get(key);

            if (StringMap.class.isInstance(value)) {
                HashMap<String, Object> newValue = convertStringMapToHashMap((StringMap<?>) value);
                res.put(key, newValue);
            } else {
                res.put(key, value);
            }
        }
        return res;
    }

    public List<? extends Map<String, ?>> toListHashMap() {
        return (List<? extends Map<String, ?>>) this.Rows;
    }
}
