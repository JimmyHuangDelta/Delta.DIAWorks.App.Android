package com.delta.android.Core.Adapter;


import java.util.List;
import java.util.Map;

import android.view.View;

public interface IAdapterEvent {
	/**
	 * View 刷新事件
	 * @param view 目前Item的View
	 * @param filterData 目前顯示的所有資料
	 * @param position Item位置
	 * @param displayColumns 顯示在View上的欄位
	 * @param viewColumns 顯示在View上的欄位ID
	 */
	public abstract void onViewRefresh(View view, List<Map<String, ?>> filterData, int position, String[] displayColumns, int[] viewColumns);  
}
