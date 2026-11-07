package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ClistResponse implements Serializable {
    @SerializedName("objects")
    private List<ClistContest> objects = new ArrayList<>();

    public ClistResponse() {}

    public List<ClistContest> getObjects() { return objects != null ? objects : new ArrayList<>(); }
    public void setObjects(List<ClistContest> objects) { this.objects = objects != null ? objects : new ArrayList<>(); }
}

