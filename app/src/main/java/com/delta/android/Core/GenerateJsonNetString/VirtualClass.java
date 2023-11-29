package com.delta.android.Core.GenerateJsonNetString;

public class VirtualClass {
    private String _className = "";
    private String _assemblyName = "";

    protected VirtualClass() {

    }

    public static VirtualClass create(VirtualClassType virtualClassType) {
        VirtualClass virtualClass = new VirtualClass();
        switch (virtualClassType) {
            case String:
                virtualClass.setClassName("System.String");
                virtualClass.setAssemblyName("mscorlib");
                break;

            case Enum:
                virtualClass.setClassName("System.Enum");
                virtualClass.setAssemblyName("mscorlib");
                break;

            case Decimal:
                virtualClass.setClassName("System.Decimal");
                virtualClass.setAssemblyName("mscorlib");
                break;

            default:
                virtualClass = null;
                break;
        }

        return virtualClass;
    }

    public static VirtualClass create(String className, String assemblyName) {
        VirtualClass virtualClass = new VirtualClass();

        virtualClass.setClassName(className);
        virtualClass.setAssemblyName(assemblyName);

        return virtualClass;
    }

    public final String getClassName() {
        return _className;
    }

    protected final void setClassName(String value) {
        _className = value;
    }

    public final String getAssemblyName() {
        return _assemblyName;
    }

    protected void setAssemblyName(String value) {
        _assemblyName = value;
    }

    public final String getGenerateCode() {
        return String.format("%1$s, %2$s", _className, _assemblyName);
    }

    public enum VirtualClassType {
        String,

        Enum,

        Decimal
    }
}

