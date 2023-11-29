package com.delta.android.Core.WebApiClient;

import java.math.BigDecimal;
import java.util.Vector;

/**
 * Created by andychen on 2015/9/11.
 */
public class RequestList {
    protected String resourceType;
    protected String userKey;
    protected int userSerialKey;
    protected String userID;
    protected String userPassword;
    protected boolean writeLog;
    protected String senderFullName;
    protected String processGuid;
    protected boolean returnDataSet;
    //20201020 archie 修改SSO時新增
    protected String token;
    protected String otherParam;
    protected String systemID;
    protected String linkID;
    protected String factoryID;

	@SuppressWarnings("rawtypes")
	public Vector tkRequestList;
    @SuppressWarnings("rawtypes")
	public Vector ifRequestList;

    /**
     * 取得 resourceType 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * 設定 resourceType 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setResourceType(String value) {
        this.resourceType = value;
    }

    /**
     * 取得 userKey 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserKey() {
        return userKey;
    }

    /**
     * 設定 userKey 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserKey(String value) {
        this.userKey = value;
    }

    /**
     * 取得 userSerialKey 特性的值.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public int getUserSerialKey() {
        return userSerialKey;
    }

    /**
     * 設定 userSerialKey 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setUserSerialKey(int value) {
        this.userSerialKey = value;
    }

    /**
     * 取得 userID 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserID() {
        return userID;
    }

    /**
     * 設定 userID 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserID(String value) {
        this.userID = value;
    }

    /**
     * 取得 userPassword 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * 設定 userPassword 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserPassword(String value) {
        this.userPassword = value;
    }

    /**
     * 取得 writeLog 特性的值.
     *
     */
    public boolean isWriteLog() {
        return writeLog;
    }

    /**
     * 設定 writeLog 特性的值.
     *
     */
    public void setWriteLog(boolean value) {
        this.writeLog = value;
    }

    /**
     * 取得 senderFullName 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSenderFullName() {
        return senderFullName;
    }

    /**
     * 設定 senderFullName 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSenderFullName(String value) {
        this.senderFullName = value;
    }

    /**
     * 取得 processGuid 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getProcessGuid() {
        return processGuid;
    }

    /**
     * 設定 processGuid 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setProcessGuid(String value) {
        this.processGuid = value;
    }

    /**
     * 取得 returnDataSet 特性的值.
     *
     */
    public boolean isReturnDataSet() {
        return returnDataSet;
    }

    /**
     * 設定 returnDataSet 特性的值.
     *
     */
    public void setReturnDataSet(boolean value) {
        this.returnDataSet = value;
    }

    /**
     * Gets the value of the tkRequestList property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tkRequestList property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTKRequestList().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RequestBase }
     *
     *
     */
    @SuppressWarnings("rawtypes")
	public Vector getTKRequestList() {
        if (tkRequestList == null) {
            tkRequestList = new Vector<Object>();
        }
        return this.tkRequestList;
    }

    /**
     * Gets the value of the ifRequestList property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ifRequestList property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIFRequestList().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IFRequest }
     *
     *
     */
    @SuppressWarnings("rawtypes")
	public Vector getIFRequestList() {
        if (ifRequestList == null) {
            ifRequestList = new Vector<Object>();
        }
        return this.ifRequestList;
    }

    //20201019 archie 修改SSO時新增
    /**
     * 取得 Token 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getToken() {
        return token;
    }

    /**
     * 設定 Token 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setToken(String value) {
        this.token = value;
    }

    /**
     * 取得 OtherParam 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getOtherParam() {
        return otherParam;
    }

    /**
     * 設定 OtherParam 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setOtherParam(String value) {
        this.otherParam = value;
    }

    /**
     * 取得 SystemID 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSystemID() {
        return systemID;
    }

    /**
     * 設定 SystemID 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSystemID(String value) {
        this.systemID = value;
    }

    /**
     * 取得 LinkID 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLinkID() {
        return linkID;
    }

    /**
     * 設定 LinkID 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLinkID(String value) {
        this.linkID = value;
    }

    /**
     * 取得 FactoryID 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFactoryID() {
        return factoryID;
    }

    /**
     * 設定 FactoryID 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFactoryID(String value) {
        this.factoryID = value;
    }
}