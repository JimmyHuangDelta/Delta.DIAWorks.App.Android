package com.delta.android.Core.WebApiClient;

/**
 * Created by andychen on 2015/9/15.
 */
import java.util.ArrayList;
import java.util.List;


/**
 * <p>ErrorInfo complex type 的 Java 類別.
 *
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 *
 * <pre>
 * &lt;complexType name="ErrorInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Message" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Stack" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ErrorParameter" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class ErrorInfo {
    protected String Message;
    protected String Code;
    protected String Stack;
    protected List<String> ErrorParameter;

    /**
     * 取得 message 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMessage() {
        return Message;
    }

    /**
     * 設定 message 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMessage(String value) {
        this.Message = value;
    }

    /**
     * 取得 code 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCode() {
        return Code;
    }

    /**
     * 設定 code 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCode(String value) {
        this.Code = value;
    }

    /**
     * 取得 stack 特性的值.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStack() {
        return Stack;
    }

    /**
     * 設定 stack 特性的值.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStack(String value) {
        this.Stack = value;
    }

    /**
     * Gets the value of the errorParameter property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the errorParameter property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getErrorParameter().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List<String> getErrorParameter() {
        if (ErrorParameter == null) {
            ErrorParameter = new ArrayList<String>();
        }
        return this.ErrorParameter;
    }

}

