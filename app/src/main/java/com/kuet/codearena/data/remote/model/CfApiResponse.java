package com.kuet.codearena.data.remote.model;

/**
 * Generic envelope response model for Codeforces API responses.
 */
public class CfApiResponse<T> {
    private String status; // "OK" or "FAILED"
    private String comment;
    private T result;

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
