package com.delta.android.Core.WebApiClient;

import com.google.gson.Gson;

import com.delta.android.Core.DataTable.DataTable;

/**
 * Created by andychen on 2015/9/11.
 */
public class ParameterInfo{
    protected String parameterID;
    protected Object parameterValue;

    /**
     * 取得 parameterID 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getParameterID() {
        return parameterID;
    }

    /**
     * 設定 parameterID 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setParameterID(String value) {
        this.parameterID = value;
    }

    /**
     * 取得 parameterValue 特性的值.
     *
     * @return
     *     possible object is
     *     {@link Object }
     *
     */
    public Object getParameterValue() {
        return parameterValue;
    }

    /**
     * 設定 parameterValue 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link Object }
     *
     */
    public void setParameterValue(Object value) {
        String strValue = new Gson().toJson(value);

        if (value instanceof DataTable)
        {
            String strRows = "\"Rows\":";
            int iRosPos = strValue.indexOf(strRows);
            iRosPos += strRows.length();
            strValue = strValue.substring(iRosPos, strValue.length() - 1);
        }
        this.parameterValue = strValue;
    }
    
    public void setNetParameterValue(String value)
    {
    	this.parameterValue = value;
    }

    public void setNetParameterValue2(Object value)
    {
        String strValue = new Gson().toJson(value);
        this.parameterValue = value;
    }
}
