package com.delta.android.Core.Adapter;



import android.view.View;

public interface ISpinnerWithHintEvent {

	public abstract void onDropDown(View view, Object row);
	
	public abstract void onRefresh(View view, Object row);
}
