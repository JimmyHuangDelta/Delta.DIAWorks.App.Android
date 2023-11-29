package com.delta.android.Core.WebApiClient;

/**
 * Created by andychen on 2015/9/15.
 */


/**
 * <p>IFResponse complex type 的 Java 類別.
 *
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 *
 * <pre>
 * &lt;complexType name="IFResponse">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.unicom.com.tw/Uniworks}ResponseBase">
 *       &lt;sequence>
 *         &lt;element name="InfoRequestID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class IFResponse
        extends ResponseBase
{
    protected String InfoRequestID;

    /**
     * 取得 infoRequestID 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInfoRequestID() {
        return InfoRequestID;
    }

    /**
     * 設定 infoRequestID 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInfoRequestID(String value) {
        this.InfoRequestID = value;
    }

}

