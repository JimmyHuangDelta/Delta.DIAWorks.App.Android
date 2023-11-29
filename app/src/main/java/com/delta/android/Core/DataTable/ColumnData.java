package com.delta.android.Core.DataTable;

/**
 * Created by andychen on 2015/9/15.
 */


/**
 * <p>ColumnData complex type 的 Java 類別.
 *
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 *
 * <pre>
 * &lt;complexType name="ColumnData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ColumnName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DataType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ColumnSeq" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class ColumnData {
    protected String ColumnName;
    protected String DataType;
    protected int ColumnSeq;

    /**
     * 取得 columnName 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getColumnName() {
        return ColumnName;
    }

    /**
     * 設定 columnName 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setColumnName(String value) {
        this.ColumnName = value;
    }

    /**
     * 取得 dataType 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDataType() {
        return DataType;
    }

    /**
     * 設定 dataType 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDataType(String value) {
        this.DataType = value;
    }

    /**
     * 取得 columnSeq 特性的值.
     *
     */
    public int getColumnSeq() {
        return ColumnSeq;
    }

    /**
     * 設定 columnSeq 特性的值.
     *
     */
    public void setColumnSeq(int value) {
        this.ColumnSeq = value;
    }

}