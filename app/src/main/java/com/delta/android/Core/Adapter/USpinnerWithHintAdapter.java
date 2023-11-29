package com.delta.android.Core.Adapter;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.delta.android.Core.Common.ResourceCulture;
import com.delta.android.R;

public class USpinnerWithHintAdapter<T> extends BaseAdapter implements Filterable {
	private Context context;
	private List<T> originalData;
	private List<T> filterData;
	private LayoutInflater inflater = null;
	private int layout = -1;
	private int dropdownLayout = -1;
	private int fieldID = -1;
	private USpinnerWithHintFilter filter;
	private String strHintText = null;
	private List<ISpinnerWithHintEvent> events = new ArrayList<ISpinnerWithHintEvent>();
	private ListViewItemColor colorConvert = null;	

	public static USpinnerWithHintAdapter<CharSequence> createFromResource(Context cont, int textArrayLayoutId,
			int textViewLayoutId) {
		CharSequence[] strings = cont.getResources().getTextArray(textArrayLayoutId);
		
		return new USpinnerWithHintAdapter<CharSequence>(cont, textViewLayoutId, strings);
	}

	public USpinnerWithHintAdapter(Context cont, int layout) {
		init(cont, layout, -1, new ArrayList<T>());
	}

	public USpinnerWithHintAdapter(Context cont, int layout, int textViewLayoutId) {
		init(cont, layout, textViewLayoutId, new ArrayList<T>());
	}

	public USpinnerWithHintAdapter(Context cont, int layout, T[] objects) {
		init(cont, layout, -1, Arrays.asList(objects));
	}

	public USpinnerWithHintAdapter(Context cont, int layout, int textViewLayoutId, T[] objects) {
		init(cont, layout, textViewLayoutId, Arrays.asList(objects));
	}

	public USpinnerWithHintAdapter(Context cont, int layout, List<T> objects) {
		init(cont, layout, -1, objects);
	}

	public USpinnerWithHintAdapter(Context cont, int layout, int textViewLayoutId, List<T> objects) {
		init(cont, layout, textViewLayoutId, objects);
	}

	private void init(Context context, int layout, int textViewLayoutId, List<T> objects) {
		if (objects.contains(null))
		{
			throw new IllegalStateException("Objects Contains null Object");
		}
		
		this.context = context;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.layout = layout;
		this.dropdownLayout = layout;
		this.originalData = new ArrayList<T>(objects);
		this.filterData = new ArrayList<T>(objects);
		this.fieldID = textViewLayoutId;
		this.colorConvert = new ListViewItemColor(context);
	}

	public Context getContext() {
		return context;
	}

	@Override
	public int getCount() {
		if (this.filterData == null) {
			return 0;
		}
		if (!filterData.contains(null))
		{
			return this.filterData.size();
		}
		
		return this.filterData.size() - 1;
	}

	@Override
	public T getItem(int position) {
		if (this.filterData == null) {
			return null;
		}
		
		return this.filterData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (filterData.get(position) == null)
		{
			return createViewFromResource(strHintText, convertView, parent, layout, true, SpinnerWithHintEvent.OnRefresh, position);
		}
		
		return createViewFromResource(filterData.get(position), convertView, parent, layout, false, SpinnerWithHintEvent.OnRefresh, position);
	}

	private View createViewFromResource(Object row, View convertView, ViewGroup parent, int layout,
			boolean bIsHintText, SpinnerWithHintEvent callEvent, int position) {
		View view;
		TextView text;
		
		int iLastSelectedPosition = -1;
		if (parent instanceof USpinner)
		{
			iLastSelectedPosition = ((USpinner)parent).getLastPosition();
		}

		if (convertView == null) {
			view = inflater.inflate(layout, parent, false);
		} else {
			view = convertView;
		}

		try {
			if (fieldID == -1) {
				text = (TextView) view;
			} else {
				text = (TextView) view.findViewById(fieldID);
			}
		} catch (ClassCastException e) {
			throw new IllegalStateException("USpinnerWithHintAdapter requires the resource ID to be a TextView", e);
		}
		
		view = text;
		
		if (callEvent.equals(SpinnerWithHintEvent.OnRefresh) && (iLastSelectedPosition == position || iLastSelectedPosition == -1))
		{
			for (int i = 0; i < events.size(); i++)
			{				
				events.get(i).onRefresh(view, row);
			}
		}
		else if (callEvent.equals(SpinnerWithHintEvent.OnDropDown))
		{
			view = colorConvert.convert(view, position);
			
			for (int i = 0; i < events.size(); i++)
			{				
				events.get(i).onDropDown(view, row);
			}
		}
		
		if (bIsHintText) {
			text.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.common_text_size_medium));
			text.setHint(row.toString());
		} 
			else {
			text.setText(row.toString());
		}
				
		return view;
	}

	public void setDropDownView(int dropdownLayout) {
		this.dropdownLayout = dropdownLayout;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {		
		Object obj = null;
		
		if (filterData.get(position) != null)
		{
			obj = filterData.get(position);
		}
		
		return createViewFromResource(obj, convertView, parent, dropdownLayout, false, SpinnerWithHintEvent.OnDropDown, position);
	}

	public void setHintText(String hint) {
		if (!filterData.contains(null))
		{
			this.filterData.add(null);
		}
		
		this.strHintText = ResourceCulture.getResString(context, hint);
	}
	
	public void setHintText(String hint, int textSize) {
//		this.hintTextSize = textSize;
		setHintText(hint);
	}

	public String getHintText() {
		return strHintText;
	}

	public void clearHintText() {
		this.strHintText = null;
		this.filterData.remove(null);
	}
	
	public void addListener(ISpinnerWithHintEvent event)
	{
		this.events.add(event);
	}

	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new USpinnerWithHintFilter();
		}
		return filter;
	}

	private class USpinnerWithHintFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			if (originalData == null) {
				originalData = new ArrayList<T>();
			}

			if (prefix == null || prefix.length() == 0) {
				ArrayList<T> list = new ArrayList<T>(originalData);

				results.values = list;
				results.count = list.size();
			} else {
				String prefixString = prefix.toString().toLowerCase();

				ArrayList<T> values = new ArrayList<T>(originalData);

				final int count = values.size();
				final ArrayList<T> newValues = new ArrayList<T>();

				for (int i = 0; i < count; i++) {
					final T value = values.get(i);
					final String valueText = value.toString().toLowerCase();

					if (valueText.contains(prefixString)) {
						newValues.add(value);
					} else {
						final String[] words = valueText.split(" ");
						final int wordCount = words.length;

						for (int k = 0; k < wordCount; k++) {
							if (words[k].contains(prefixString)) {
								newValues.add(value);
								break;
							}
						}
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			filterData = (List<T>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
	
	private enum SpinnerWithHintEvent
	{
		OnDropDown, 
		OnRefresh
	}
}
