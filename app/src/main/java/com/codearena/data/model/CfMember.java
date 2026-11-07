package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CfMember implements Serializable {
    @SerializedName("handle")
    private String handle;

    @SerializedName("name")
    private String name;

    public CfMember() {}

    public CfMember(String handle, String name) {
        this.handle = handle;
        this.name = name;
    }

    public String getHandle() { return handle; }
    public void setHandle(String handle) { this.handle = handle; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

