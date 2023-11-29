package com.delta.android.Core.GenerateJsonNetString;

import com.google.gson.Gson;

public class MesClass extends VirtualClass {
    private VirtualClass _value = null;
    private Gson gson = null;

    public MesClass(VirtualClass Key) {
        super.setAssemblyName(Key.getAssemblyName());
        super.setClassName(Key.getClassName());

        _value = Key;
        gson = new Gson();
    }

    public final VirtualClass getValue() {
        return _value;
    }

    public final String generateFinalCode(Object obj) {
        String tempValue = gson.toJson(obj);
        tempValue = tempValue.substring(1, tempValue.length() - 1);

        StringBuilder sbReql = new StringBuilder();
        sbReql.append("{\"$type\": \"").append(super.getGenerateCode()).append("\",").append(tempValue).append("}");

        return sbReql.toString();
    }
}