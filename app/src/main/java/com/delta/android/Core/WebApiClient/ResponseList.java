package com.delta.android.Core.WebApiClient;

/**
 * Created by andychen on 2015/9/15.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * <p>ErrorCodeRequest complex type 的 Java 類別.
 *
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 *
 * <pre>
 * &lt;complexType name="ResponseList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TKResponseList" type="{http://www.unicom.com.tw/Uniworks}TKResponse" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="IFResponseList" type="{http://www.unicom.com.tw/Uniworks}IFResponse" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class ResponseList {
    protected List<TKResponse> TKResponseList;
    protected List<IFResponse> IFResponseList;
    protected List<ErrorInfo> AllAckError;
    protected HashMap<String, Object> ReturnJsonDataSet;
    //20201019 archie 修改SSO時新增
    protected int StatusCode;
    protected String Token;
    protected String OtherParameter;

	/**
     * Gets the value of the tkResponseList property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tkResponseList property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTKResponseList().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TKResponse }
     *
     *
     */
    public List<TKResponse> getTKResponseList() {
        if (TKResponseList == null) {
            TKResponseList = new ArrayList<TKResponse>();
        }
        return this.TKResponseList;
    }

    /**
     * Gets the value of the ifResponseList property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ifResponseList property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIFResponseList().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IFResponse }
     *
     *
     */
    public List<IFResponse> getIFResponseList() {
        if (IFResponseList == null) {
            IFResponseList = new ArrayList<IFResponse>();
        }
        return this.IFResponseList;
    }
    


    public List<ErrorInfo> getRequestErrorList() {
		return AllAckError;
	}

	public void setRequestErrorList(List<ErrorInfo> requestErrorList) {
		AllAckError = requestErrorList;
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
        return Token;
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
        this.Token = value;
    }
}
