package com.delta.android.Core.Adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.SpinnerAdapter;


public class USpinner extends android.support.v7.widget.AppCompatSpinner {
	private int lastPosition = -1;
	private boolean bFirst = false;
	
	public int getLastPosition()
	{
		return lastPosition;
	}
	
	public USpinner(Context context, int mode) {
		super(context, mode);
		initial();
	}

	public USpinner(Context context, AttributeSet attrs, int defStyle, int mode) {
		super(context, attrs, defStyle, mode);
		initial();
	}

	public USpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initial();
	}

	public USpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		initial();
	}

	public USpinner(Context context) {
		super(context);
		initial();
	}
	
	private void initial()
	{

	}
	
	public void showHint()
	{
		Adapter adapter = this.getAdapter();
		if (adapter == null)
		{
			return;
		}
		
		int dataLength = adapter.getCount();
		try {
			if (adapter.getItem(dataLength) == null)
			{
				setSelection(dataLength);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void setAdapter(SpinnerAdapter adapter) {
		super.setAdapter(adapter);
		if (!bFirst)
		{
			showHint();
			bFirst = true;
		}
	}
	
	@Override
	public void setSelection(int position)
	{
		super.setSelection(position);
		lastPosition = position;
	}

	@Override
    public int getSelectedItemPosition() {		
		if (checkIsHintSelected())
		{
			return 0;
		}
		
        return super.getSelectedItemPosition();
    }
	
	@Override
	public Object getSelectedItem()
	{
		if (checkIsHintSelected())
		{
			return null;
		}
		
		return super.getSelectedItem();
	}
	
	public int getPositionForView(View view) {
		return super.getPositionForView(view);
	}

	private boolean checkIsHintSelected()
	{
		Adapter adapter = this.getAdapter();
		if (adapter == null)
		{
			return true;
		}
		
		int dataLength = adapter.getCount();
		try {
			//提示會加在整個資料的最後一筆，當目前顯示的Item為提示字，則從第0比開始顯示
			if (adapter.getItem(dataLength) == null && dataLength == lastPosition)
			{
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
