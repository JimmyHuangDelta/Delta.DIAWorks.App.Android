package com.delta.android.Core.GenerateJsonNetString;

import java.util.*;

import com.google.gson.Gson;

/** 模擬 Microsoft 的 List 物件
 
*/
public class MList extends VirtualClass
{
	private VirtualClass _value = null;
	private Gson gson = null;
	
	@SuppressWarnings("unused")
	private String _prepareCode = "";

	public MList(VirtualClass Key)
	{
		String curGenCode = "System.Collections.Generic.List`1[[%s]]";
		super.setAssemblyName("mscorlib");
		super.setClassName(String.format(curGenCode, Key.getGenerateCode(), super.getAssemblyName()));

		_value = Key;
        _prepareCode = String.format(getClassName(), getValue().getGenerateCode(), super.getAssemblyName());
		gson = new Gson();
	}

	public final VirtualClass getValue()
	{
		return _value;
	}

	public final String generateFinalCode(List<?> obj)
	{
		String tempValue = gson.toJson(obj);

		StringBuilder sbReql = new StringBuilder();
		sbReql.append("{\"$type\": \"").append(super.getGenerateCode()).append("\",").append("\"$values\":").append(tempValue).append("}");

		return sbReql.toString();
	}

}