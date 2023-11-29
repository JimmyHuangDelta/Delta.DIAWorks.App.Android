package com.delta.android.Core.DataTable;

/**
 * Created by andychen on 2015/9/15.


/**
 * <p>ErrorCodeRequest complex type 的 Java 類別.
 *
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 *
 * <pre>
 * &lt;complexType name="Table">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TableName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Columns" type="{http://www.unicom.com.tw/Uniworks}ColumnData" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Rows" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class Table {
    protected String TableName;
    protected ColumnData[] Columns;
    protected String[] Rows;

    /**
     * 取得 tableName 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTableName() {
        return TableName;
    }

    /**
     * 設定 tableName 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTableName(String value) {
        this.TableName = value;
    }

    /**
     * Gets the value of the columns property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the columns property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getColumns().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ColumnData }
     *
     *
     */
    public ColumnData[] getColumns() {
        if (Columns == null) {
            //Columns = new ArrayList<ColumnData>();
        }
        return this.Columns;
    }

    /**
     * Gets the value of the rows property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rows property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRows().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public String[] getRows() {
        if (Rows == null) {
            //Rows = new ArrayList<String>();
        }
        return this.Rows;
    }
}