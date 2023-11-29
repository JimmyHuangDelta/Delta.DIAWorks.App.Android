package com.delta.android.Core.Adapter;


import android.view.View;

public abstract class USpinnerWithHintListener implements ISpinnerWithHintEvent {

	/**
	 * Spinner下拉事件
	 * @return 
	 */
	@Override
	public abstract void onDropDown(View view, Object row) ;
	
	@Override
	public abstract void onRefresh(View view, Object row) ;
	
}
