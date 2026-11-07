package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;

public class CfResponse<T> {
    @SerializedName("status")
    private String status;

    @SerializedName("comment")
    private String comment;

    @SerializedName("result")
    private T result;

    public CfResponse() {}

    public CfResponse(String status, String comment, T result) {
        this.status = status;
        this.comment = comment;
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public boolean isOk() {
        return "OK".equalsIgnoreCase(status);
    }
}

