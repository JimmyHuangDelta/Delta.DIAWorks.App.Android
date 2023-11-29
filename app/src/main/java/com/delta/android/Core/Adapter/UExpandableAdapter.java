package com.delta.android.Core.Adapter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

public class UExpandableAdapter extends BaseExpandableListAdapter implements ExpandableListAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<ExpandListViewRow> originalData;
	private int groupLayout = -1;
	private int childLayout = -1;
	private int[] groupLayoutComp = null;
	private String[] groupLayoutColumn = null;
	private int[] childLayoutComp = null;
	private String[] childLayoutColumn = null;
	private List<HashMap<String, Object>> groupData = null;
	private List<IExpandableEvent> events = null;
	private ListViewItemColor colorConvert = null;

	public UExpandableAdapter(Context context, List<ExpandListViewRow> data, int groupLayout, int childLayout,
			int[] groupComp, String[] groupColumn, int[] childComp, String[] childColumn) {
		this.originalData = data;
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.groupLayout = groupLayout;
		this.childLayout = childLayout;
		this.groupLayoutComp = groupComp;
		this.groupLayoutColumn = groupColumn;
		this.childLayoutComp = childComp;
		this.childLayoutColumn = childColumn;
		this.events = new ArrayList<IExpandableEvent>();
		this.groupData = new ArrayList<HashMap<String, Object>>();
		this.colorConvert = new ListViewItemColor(context);
		
		for (ExpandListViewRow row : this.originalData)
		{
			groupData.add(row.getGroup());
		}
	}

	@Override
	public int getGroupCount() {
		return this.groupData.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this.originalData.get(groupPosition).getChild().Rows.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.originalData.get(groupPosition).getGroup();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {

		return this.originalData.get(groupPosition).getChild().Rows.get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {

		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View view = convertView;

		if (view == null) {
			if (groupLayout == -1) {
				return null;
			}

			view = inflater.inflate(groupLayout, parent, false);
		}
		
		for (int i = 0; i < groupLayoutComp.length; i++)
		{
			View vComp = view.findViewById(groupLayoutComp[i]);
			if (vComp == null)
			{
				continue;
			}
			
			if (vComp instanceof TextView || vComp instanceof EditText)
			{
				if (!groupData.get(groupPosition).containsKey(groupLayoutColumn[i]))
				{
					continue;
				}
				((TextView)vComp).setText(groupData.get(groupPosition).get(groupLayoutColumn[i]).toString());
			}
		}

		// GroupView Refresh Event
		//View, RowNum, RowData, isExpanded, groupLayoutColumn, groupLayoutComp

//		view = colorConvert.Convert(view, groupPosition);
		
		for (IExpandableEvent event : events)
		{
			event.onGroupRefresh(view, isExpanded, groupPosition, originalData.get(groupPosition).getGroup(), groupLayoutColumn, groupLayoutComp, originalData.get(groupPosition).getChild());
		}
		
		return view;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {

		View view = convertView;

		if (view == null) {
			if (childLayout == -1) {
				return null;
			}

			view = inflater.inflate(childLayout, parent, false);
		}
		
		for (int i = 0; i < childLayoutComp.length; i++)
		{
			View vComp = view.findViewById(childLayoutComp[i]);
			if (vComp == null)
			{
				continue;
			}
			
			if (vComp instanceof TextView || vComp instanceof EditText)
			{
				if (!this.originalData.get(groupPosition).getChild().Rows.get(childPosition).containsKey(childLayoutColumn[i]))
				{
					continue;
				}
				((TextView)vComp).setText(this.originalData.get(groupPosition).getChild().Rows.get(childPosition).get(childLayoutColumn[i]).toString());
			}
		}

		// ChildView Refresh Event
		//View, groupPosition, childPosition, RowData, childLayoutColumn, childLayoutComp
		
		view = colorConvert.convert(view, childPosition);
		
		for (IExpandableEvent event : events)
		{
			event.onChildRefresh(view, groupPosition, childPosition, this.originalData.get(groupPosition).getChild().Rows.get(childPosition), childLayoutColumn, childLayoutComp);
		}

		return view;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	
	public Context getContext()
	{
		return this.context;
	}
	
	public List<ExpandListViewRow> getData() {
		return this.originalData;
	}
	
	public void addAdapterEvent(IExpandableEvent iExpandableEvent)
    {
    	this.events.add(iExpandableEvent);
    }
	
	@Override
	public void notifyDataSetChanged()
	{
		super.notifyDataSetChanged();
	}
}