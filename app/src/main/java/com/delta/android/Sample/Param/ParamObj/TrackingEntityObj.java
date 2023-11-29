package com.delta.android.Sample.Param.ParamObj;

public class TrackingEntityObj {
    public TrackingEntityObj() {
        this.setEntityId("");
        this.setEntitySerialKey(0);
        this.setPathId("");
    }

    public String getEntityId() {
        return EntityId;
    }

    public void setEntityId(String entityId) {
        EntityId = entityId;
    }

    private String EntityId;

    public int getEntitySerialKey() {
        return EntitySerialKey;
    }

    public void setEntitySerialKey(int entitySerialKey) {
        EntitySerialKey = entitySerialKey;
    }

    private int EntitySerialKey;

    public String getPathId() {
        return PathId;
    }

    public void setPathId(String pathId) {
        PathId = pathId;
    }

    private String PathId;
}
