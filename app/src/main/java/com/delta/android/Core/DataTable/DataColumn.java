package com.delta.android.Core.DataTable;

import java.io.Serializable;

public class DataColumn implements Serializable {

    /**
     * DataColumn所屬的DataTable
     */
    private DataTable table;

    /**
     * DataColumn的欄位名稱
     */
    public String ColumnName; // 欄名，當做DataRow的key

    /**
     * DataColumn被建立時，一定要指定欄名
     *
     * @param columnName 欄名
     */
    public DataColumn(String columnName) {
        this.ColumnName = columnName;
    }

    /**
     * 給DataColumnCollection加入DataColumn時設定所屬的DataTable的方法，同一個package才用到
     *
     * @param table
     */
    void setTable(DataTable table) {
        this.table = table;
    }

    /**
     * 取得DataColumn所屬的DataTable，唯讀
     *
     * @return DataTable
     */
    public DataTable getTable() {
        return this.table;
    }

    /**
     * DataColumn物件的toString()，會回傳自己的欄名
     *
     * @return
     */
    @Override
    public String toString() {
        return this.ColumnName;
    }

    public void setColumnName(String columnName) {
        for (int i = 0; i < this.table.Rows.size(); i++) {
            this.table.Rows.get(i).put(columnName, this.table.Rows.get(i).remove(this.ColumnName));
        }
        this.ColumnName = columnName;
    }
}