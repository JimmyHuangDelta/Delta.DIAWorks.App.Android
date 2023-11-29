package com.delta.android.Core.Common;

import android.content.Context;

public class ResourceCulture {
	/** 
     * 取得語系文字
     */  
	public static String getResString(Context current,String str){
		int i = current.getResources().getIdentifier(str, "string", current.getPackageName());
		if (i == 0) {
			return str;
		}
		return current.getResources().getString(i);
	}
	
	/**
	 * 在轉換完的最後加入文字
	 * @param current
	 * @param str
	 * @param append
	 * @return
	 */
	public static String getResString(Context current,String str, String append){
		return String.format("%s%s", getResString(current, str), append);
	}
	
	public static String GetResString(Context current, int res)
	{
		return current.getResources().getString(res);
	}
}
