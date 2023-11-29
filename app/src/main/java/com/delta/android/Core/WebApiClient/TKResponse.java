package com.delta.android.Core.WebApiClient;

/**
 * Created by andychen on 2015/9/15.
 */


/**
 * <p>ErrorCodeRequest complex type 的 Java 類別.
 *
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 *
 * <pre>
 * &lt;complexType name="TKResponse">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.unicom.com.tw/Uniworks}ResponseBase">
 *       &lt;sequence>
 *         &lt;element name="TKRequestID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class TKResponse
        extends ResponseBase
{
    protected String TKRequestID;

    /**
     * 取得 TKRequestID 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTKRequestID() {
        return TKRequestID;
    }

    /**
     * 設定 TKRequestID 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTKRequestID(String value) {
        this.TKRequestID = value;
    }

}