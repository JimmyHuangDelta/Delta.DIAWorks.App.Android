package com.delta.android.Sample.Param.ParamObj;

import java.util.ArrayList;
import java.util.List;

public class ClassAObj {
    public ClassAObj() {
        B = new ArrayList<ClassBObj>();
    }
    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public int getSeq() {
        return Seq;
    }

    public void setSeq(int seq) {
        Seq = seq;
    }

    private String Id;

    private int Seq;

    private List<ClassBObj> B;

    public List<ClassBObj> getB() {
        return B;
    }

    public void setB(List<ClassBObj> b) {
        B = b;
    }
}
