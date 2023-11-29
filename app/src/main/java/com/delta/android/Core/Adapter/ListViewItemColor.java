package com.delta.android.Core.Adapter;


import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import com.delta.android.R;

public class ListViewItemColor {
	private int iOddRowColor = -1;
	private int iEvenRowColor = -1;
	private int iListViewItemCornerSize = 0;
	
	public ListViewItemColor(Context context)
	{
		iOddRowColor = context.getResources().getColor(R.color.ListViewOddRow);
		iEvenRowColor = context.getResources().getColor(R.color.ListViewEvenRow);
		iListViewItemCornerSize = (int)context.getResources().getDimension(R.dimen.ListViewItemCornerSize);
	}
	
	public View convert(View v, int position)
	{
		GradientDrawable shape =  new GradientDrawable();
		shape.setCornerRadius(iListViewItemCornerSize);
		
		if ((position + 1) % 2 == 0)
		{
			shape.setColor(iOddRowColor);
		}
		else
		{
			shape.setColor(iEvenRowColor);
		}
		
		v.setBackground(shape);
		return v;
	}
}
