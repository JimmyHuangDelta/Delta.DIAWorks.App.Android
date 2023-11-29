package com.delta.android.Core.WebApiClient;

import java.util.Vector;

/**
 * Created by andychen on 2015/9/11.
 */
public class RequestBase{
    protected String requestID;
    protected String bModuleName;
    @SuppressWarnings("rawtypes")
	public Vector parameterList;

    protected String ModuleID;

    /**
     * 取得 moduleID 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getModuleID() {
        return ModuleID;
    }

    /**
     * 設定 moduleID 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setModuleID(String value) {
        this.ModuleID = value;
    }
    
    /**
     * 取得 requestID 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRequestID() {
        return requestID;
    }

    /**
     * 設定 requestID 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRequestID(String value) {
        this.requestID = value;
    }

    /**
     * 取得 bModuleName  特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getBModuleName() {
        return bModuleName;
    }

    /**
     * 設定 bModuleName 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setBModuleName(String value) {
        this.bModuleName = value;
    }

    /**
     * Gets the value of the parameterList property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parameterList property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParameterList().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ParameterInfo }
     *
     *
     */
    @SuppressWarnings("rawtypes")
	public Vector getParameterList() {
        if (parameterList == null) {
            parameterList = new Vector<Object>();
        }
        return this.parameterList;
    }
}
