package com.delta.android.Core.Adapter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;

/**
 * Adapter
 * @author Admin
 *
 */
public class UAdapter extends BaseAdapter implements Filterable {
	private Context context = null;//Context
	private int layout = -1;//Bind到ListView的Row View
	private String[] displayColumns = null;//顯示欄位
	private int[] viewComponents = null;//Row View中的元件id
	private List<? extends Map<String, ?>> originalData;//原始資料
	private List<Map<String, ?>> filterData;//目前顯示資料
	private String[] filterColumns = null;//Filter欄位
	private LayoutInflater inflater = null;
	private ViewBinder viewBinder = null;
	private List<IAdapterEvent> events = null;
	private ListViewItemColor colorConvert = null;

	/**
	 * 建構式
	 * @param cont Context
	 * @param data Bind到View上的資料
	 * @param layout 顯示的Layout
	 * @param columns 顯示資料的欄位
	 * @param components Bind到View上的元件ID
	 */
	public UAdapter(Context cont, List<? extends Map<String, ?>> data, int layout, String[] columns, int[] components) {
		InitialAdapter(cont, data, layout, columns, components);
	}
	
	public UAdapter(Context cont, DataTable data, int layout, String[] columns, int[] components) {
		InitialAdapter(cont, data.toListHashMap(), layout, columns, components);
	}
	
	private void InitialAdapter(Context cont, List<? extends Map<String, ?>> data, int layout, String[] columns, int[] components )
	{
		this.context = cont;
		this.layout = layout;
		this.displayColumns = columns;
		this.viewComponents = components;
		this.originalData = data;
		this.filterData = new ArrayList<Map<String, ?>>();
		this.filterData.addAll(this.originalData);
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.events = new ArrayList<IAdapterEvent>();
		this.colorConvert = new ListViewItemColor(cont);
	}

	/**
	 * 取得目前顯示的比數
	 */
	@Override
	public int getCount() {
		return this.filterData.size();
	}

	/**
	 * 根據位置取得該筆資料
	 */
	@Override
	public Object getItem(int position) {
		return this.filterData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 根據位置取得View
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = createViewFromResource(position, convertView, parent, layout);
		
		for (IAdapterEvent event : events) {
			event.onViewRefresh(view, filterData, position, displayColumns, viewComponents);
		}
		
		return view;
	}

	private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
		View view;
		if (convertView == null) {
			view = inflater.inflate(resource, parent, false);
		} else {
			view = convertView;
		}
		
		view = colorConvert.convert(view, position);

		bindView(position, view);

		return view;
	}

	private void bindView(int position, View view) {
        final Map<String, ?> dataSet = filterData.get(position);
        
        if (dataSet == null) {
            return;
        }

        for (int i = 0; i < viewComponents.length; i++) {
            final View v = view.findViewById(viewComponents[i]);
            if (v != null) {
                final Object data = dataSet.get(displayColumns[i]);
                String text = data == null ? "" : data.toString();
                if (text == null) {
                    text = "";
                }

                boolean bound = false;
                if (viewBinder != null) {
                    bound = viewBinder.setViewValue(v, data, text);
                }

                if (!bound) {
                    if (v instanceof Checkable) {
                        if (data instanceof Boolean) {
                            ((Checkable) v).setChecked((Boolean) data);
                        } else if (v instanceof TextView) {
                        	setViewText((TextView) v, text);
                        } else {
                            throw new IllegalStateException(
                            		String.format("%s  should be bound to a Boolean, not a %s", 
                            		v.getClass().getName(), 
                            		(data == null ? "<unknown type>" : data.getClass())));
                        }
                    } else if (v instanceof TextView) {
                    	setViewText((TextView) v, text);
                    } else if (v instanceof ImageView) {
                        if (data instanceof Integer) {
                            setViewImage((ImageView) v, (Integer) data);                            
                        } else {
                            setViewImage((ImageView) v, text);
                        }
                    } else {
                        throw new IllegalStateException(
                        		String.format("%s is not a view that can be bounds by this SimpleAdapter",
                        		v.getClass().getName()));
                    }
                }
            }
        }
    }

	/**
	 * Filter 資料
	 */
	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();

				List<Map<String, ?>> filteredArrList = new ArrayList<Map<String, ?>>();

				if (originalData == null) {
					originalData = new ArrayList<Map<String, ?>>();
				}

				if (constraint == null || constraint.length() == 0 || filterColumns == null) {
					results.count = originalData.size();
					results.values = originalData;

					return results;
				}

				Locale locale = Locale.getDefault();
				constraint = ((String) constraint).toUpperCase(locale);

				boolean bExist = false;

				for (int i = 0; i < originalData.size(); i++) {
					Map<String, ?> obj = originalData.get(i);
					bExist = false;

					for (String column : filterColumns) {
						if (obj.get(column).toString().toUpperCase(locale).contains((String) constraint)) {
							bExist = true;
							break;
						}
					}

					if (bExist) {
						filteredArrList.add(obj);
					}
				}

				results.count = filteredArrList.size();
				results.values = filteredArrList;

				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				filterData.clear();
				filterData.addAll((List<? extends Map<String, ?>>) results.values);

				notifyDataSetChangedInner();
			}
		};
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return getCount() == 0;
	}

	public void setViewText(TextView v, String text) {
		v.setText(text);
	}
	
	@SuppressWarnings("unchecked")
	public List<HashMap<String, Object>> getData()
	{
		return (List<HashMap<String, Object>>) this.originalData;
	}

	public void setViewImage(ImageView v, String value) {
		try {
			v.setImageResource(Integer.parseInt(value));
		} catch (NumberFormatException nfe) {
			v.setImageURI(Uri.parse(value));
		}
	}

	public void setViewImage(ImageView v, int value) {
		v.setImageResource(value);
	}

	public void setFilterColumn(String[] columns) {
		this.filterColumns = columns;
	}

	public void setFilterColumn(String column) {
		setFilterColumn(new String[] { column });
	}
	
	public ViewBinder getViewBinder() {
        return viewBinder;
    }
	
    public void setViewBinder(ViewBinder viewBinder) {
        this.viewBinder = viewBinder;
    }
    
    public void addAdapterEvent(IAdapterEvent iAdapterEvent)
    {
    	this.events.add(iAdapterEvent);
    }
    
    private void notifyDataSetChangedInner()
    {
        notifyDataSetChanged();
    }
    
    @Override
    public void notifyDataSetChanged()
    {
    	super.notifyDataSetChanged();
    	//this.filterData.clear();
    	//this.filterData.addAll(this.originalData);
    }
}
