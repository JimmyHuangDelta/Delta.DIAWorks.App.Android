package com.delta.android.Core.GenerateJsonNetString;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模擬 MES 的 SerializableDictionaryList類別
 */
@SuppressWarnings("unused")
public class MesSerializableDictionaryList extends VirtualClass {
    private String _generateType = "\"$type\": \"Unicom.Uniworks.UnitBase.SerializableDictionary`2[%1$s],[%2$s], utBase\"";

    private String _jsonType = "";
    private VirtualClass _key = null;
    private VirtualClass _value = null;

    private String _prepareCode = "";
    private Gson gson = null;

    public MesSerializableDictionaryList(VirtualClass Key, VirtualClass Value) {

        String curGenCodeList = "System.Collections.Generic.List`1[[%1s]], %2$s";
        String prepareCodeList = String.format(curGenCodeList, Value.getGenerateCode(), "mscorlib");

        String curGenCode = "Unicom.Uniworks.UnitBase.SerializableDictionary`2[[%1$s],[%2$s]]";
        super.setAssemblyName("utBase");
        super.setClassName(String.format(curGenCode, Key.getGenerateCode(), prepareCodeList));


        _key = Key;
        _value = Value;

        //_prepareCode = String.format(super.getClassName(), _key.getGenerateCode(), prepareCodeList);
        gson = new Gson();
    }

    public final VirtualClass getKey() {
        return _key;
    }

    public final VirtualClass getValue() {
        return _value;
    }


    public final String generateFinalCode(HashMap<String, List<?>> Contents) {
        String dicValues = "";

        StringBuilder sbRtn = new StringBuilder();
        for (Map.Entry<String, List<?>> tempContents : Contents.entrySet()) {
            String contentcode = gson.toJson(tempContents.getValue());
            String KeyName = String.format("\"%1$s\":{", tempContents.getKey());
            sbRtn.append(KeyName).append("\"$values\":").append(contentcode).append("}").append(",");
        }

        dicValues = sbRtn.substring(0, sbRtn.length() - 1);
        StringBuilder sbReql = new StringBuilder();
        sbReql.append("{\"$type\": \"").append(super.getGenerateCode()).append("\",").append(dicValues).append("}");

        return sbReql.toString();
    }

}