package com.delta.android.Core.WebApiClient;

/**
 * Created by andychen on 2015/9/15.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.delta.android.Core.DataTable.Table;


/**
 * <p>ErrorCodeRequest complex type 的 Java 類別.
 *
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 *
 * <pre>
 * &lt;complexType name="ResponseBase">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Result" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ReturnDataSet" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element name="ReturnTables" type="{http://www.unicom.com.tw/Uniworks}ArrayOfTable" minOccurs="0"/>
 *         &lt;element name="ReturnList" type="{http://www.unicom.com.tw/Uniworks}DictionaryData" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="AckError" type="{http://www.unicom.com.tw/Uniworks}ErrorInfo" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class ResponseBase {
    protected String Result;
    protected ArrayList<Table> ReturnDataSet;
    protected ArrayList<Table> ReturnTables;
    protected List<HashMap<String, Object>> ReturnList;
    protected List<ErrorInfo> AckError;

    /**
     * 取得 result 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getResult() {
        return Result;
    }

    /**
     * 設定 result 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setResult(String value) {
        this.Result = value;
    }

    /**
     * 取得 returnDataSet 特性的值.
     *
     * @return
     *     possible object is
     *     {@link Object }
     *
     */
    public ArrayList<Table> getReturnDataSet() {
        return ReturnDataSet;
    }

    /**
     * 取得 returnDataSet 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link Object }
     *
     */
    public void setReturnDataSet(ArrayList<Table> value) {
        this.ReturnDataSet = value;
    }

    /**
     * 取得 returnTables 特性的值.
     *
     * @return
     *     possible object is
     *
     */
    public  ArrayList<Table> getReturnTables() {
        return ReturnTables;
    }

    /**
     * 設定 returnTables 特性的值.
     *
     * @param value
     *     allowed object is
     *
     */
    public void setReturnTables( ArrayList<Table> value) {
        this.ReturnTables = value;
    }

    /**
     * Gets the value of the returnList property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the returnList property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReturnList().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     *
     *
     */
    public List<HashMap<String, Object>> getReturnList() {
        if (ReturnList == null) {
            ReturnList = new ArrayList<HashMap<String, Object>>();
        }
        return this.ReturnList;
    }

    /**
     * Gets the value of the ackError property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ackError property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAckError().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ErrorInfo }
     *
     *
     */
    public List<ErrorInfo> getAckError() {
        if (AckError == null) {
            AckError = new ArrayList<ErrorInfo>();
        }
        return this.AckError;
    }

}
