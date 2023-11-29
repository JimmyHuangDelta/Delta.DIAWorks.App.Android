package com.delta.android.Core.DataTable;

import java.io.Serializable;
import java.util.ArrayList;

public class DataColumnCollection extends ArrayList<DataColumn> implements Serializable {

    /**
     * DataColumnCollection所屬的DataTable，唯讀
     */
    private DataTable Table;

    /**
     * DataColumnCollection被建立時，一定要指定所屬的DataTable
     *
     * @param table
     */
    public DataColumnCollection(DataTable table) {
        this.Table = table;
    }

    /**
     * 取得DataColumnCollection所屬的DataTable
     *
     * @return DataTable
     */
    public DataTable getTable() {
        return this.Table;
    }

    /**
     * 加入一個DataColumn物件，程式碼會設定該DataColumn的DataTable和呼叫Add()方法的DataColumnCollection同一個DataTable
     *
     * @param column
     * @return
     */
    public void addColumn(DataColumn column) {
        column.setTable(this.Table);
        this.add(column);
    }

    /**
     * 給欄位名稱
     * <br/>加入一個DataColumn物件，程式碼會設定該DataColumn的DataTable和呼叫Add()方法的DataColumnCollection同一個DataTable
     *
     * @param columnName
     * @return
     */
    public DataColumn addColumn(String columnName) {
        DataColumn column = new DataColumn(columnName); //20210119 archie 將轉小寫拿掉
        column.setTable(this.Table);
        this.add(column);
        return column;
    }

    /**
     * 依據欄名，取得DataColumn
     *
     * @param columnName 欄名
     * @return DataColumn
     */
    public DataColumn get(String columnName) {
        DataColumn column = null;
        for (DataColumn dataColumn : this) {
            if (dataColumn.ColumnName.equals(columnName)) { //20210119 archie 將轉小寫拿掉
                return dataColumn;
            }
        }
        return column;
    }
}