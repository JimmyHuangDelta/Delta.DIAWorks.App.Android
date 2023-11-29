package com.delta.android.Core.WebApiClient;

import java.util.ArrayList;

public class FunctionCategoryObject {

    private String functionId;

    private String functionName;

    private String functionKey;

    private String parentKey;

    private String objName;

    private ArrayList<FunctionCategoryObject> subCategories;

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String id) {
        this.functionId = id;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String name) {
        this.functionName = name;
    }

    public String getFunctionKey() {
        return functionKey;
    }

    public void setFunctionKey(String key) {
        this.functionKey = key;
    }

    public String getParentKey() {
        return parentKey;
    }

    public void setParentKey(String pKey) {
        this.parentKey = pKey;
    }

    public String getObjName() {
        return objName;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }

    public ArrayList<FunctionCategoryObject> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(ArrayList<FunctionCategoryObject> categories) { this.subCategories = categories; }
}
