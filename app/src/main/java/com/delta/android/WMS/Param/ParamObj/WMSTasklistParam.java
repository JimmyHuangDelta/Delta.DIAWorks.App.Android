package com.delta.android.WMS.Param.ParamObj;

import java.util.Vector;

public class WMSTasklistParam {
    private String transportFromPortID = "";
    private String transportToPortID = "";
    private Vector<String> lstObjectID = new Vector<>();

    public String getTransportFromPortID() { return transportFromPortID; }
    public void setTransportFromPortID(String TransportFromPortID) { transportFromPortID = TransportFromPortID; }

    public String getTransportToPortID() { return transportToPortID; }
    public void setTransportToPortID(String TransportToPortID) { transportToPortID = TransportToPortID; }

    public Vector<String> getListObjectID() { return lstObjectID; }
    public void setLstObjectID(Vector<String> LstObjectID) {lstObjectID = LstObjectID; }

}
