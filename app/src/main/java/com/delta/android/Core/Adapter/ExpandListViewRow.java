package com.delta.android.Core.Adapter;

import com.delta.android.Core.DataTable.DataTable;

import java.io.Serializable;
import java.util.HashMap;

public class ExpandListViewRow implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4066554986452738727L;

	private HashMap<String, Object> group;

	private DataTable child;
	
	public HashMap<String, Object> getGroup() {
		return group;
	}

	public void setGroup(HashMap<String, Object> group) {
		this.group = group;
	}

	public DataTable getChild() {
		return child;
	}

	public void setChild(DataTable child) {
		this.child = child;
	}
	
	public ExpandListViewRow()
	{
		
	}

	public ExpandListViewRow(HashMap<String, Object> groupData, DataTable childData)
	{
		this.group = groupData;
		this.child = childData;
	}
}
