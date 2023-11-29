package com.delta.android.Core.Adapter;


import java.util.HashMap;

import android.view.View;

import com.delta.android.Core.DataTable.DataTable;

public abstract class UExpandableAdapterListener implements IExpandableEvent{

	public abstract void onGroupRefresh(View view, boolean isExpanded, int groupNumber, HashMap<String, Object> groupData, String[] groupLayoutColumns, int[] groupLayoutComponents, DataTable childData);
	
	public abstract void onChildRefresh(View view, int groupNumber, int childNumber, HashMap<String, Object> childData, String[] childLayoutColumns, int[] childLayoutComponents);
}
