package com.delta.android.Core.WebApiClient;

import java.util.Vector;

public class BModuleObject {
	private String RequestID;
	private String ModuleID;
	private String BModuleName;
	public Vector<ParameterInfo> params;
	
	public String getRequestID() {
		return RequestID;
	}
	public void setRequestID(String requestID) {
		RequestID = requestID;
	}
	public String getModuleID() {
		return ModuleID;
	}
	public void setModuleID(String moduleID) {
		ModuleID = moduleID;
	}
	public String getBModuleName() {
		return BModuleName;
	}
	public void setBModuleName(String bModuleName) {
		BModuleName = bModuleName;
	}
}
