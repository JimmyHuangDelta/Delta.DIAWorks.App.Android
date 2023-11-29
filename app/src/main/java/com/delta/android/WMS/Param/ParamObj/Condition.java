package com.delta.android.WMS.Param.ParamObj;

public class Condition {

    public String getAliasTable() {
        return aliasTable;
    }
    public void setAliasTable(String AliasTable) {
        aliasTable = AliasTable;
    }
    public String getColumnName() {
        return colnumName;
    }
    public void setColumnName(String ColnumName) {
        colnumName = ColnumName;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String Value) {
        value = Value;
    }
    public String getValueBetween() {
        return valueBetween;
    }
    public void setValueBetween(String ValueBetween) {
        valueBetween = ValueBetween;
    }
    public String getDataType() {
        return dataType;
    }
    public void setDataType(String DataType) {
        dataType = DataType;
    }
    public Boolean getIsDBNull() {
        return isDBNull;
    }
    public void setIsDBNull(Boolean IsDBNull) {
        isDBNull = IsDBNull;
    }

    private String aliasTable ;
    private String colnumName ;
    private String value ;
    private String valueBetween ;
    private String dataType ;
    private Boolean isDBNull = false;

}
