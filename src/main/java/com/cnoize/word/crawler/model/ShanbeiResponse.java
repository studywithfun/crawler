package com.cnoize.word.crawler.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sixu on 17/6/21.
 */

public class ShanbeiResponse<T> {
    @JsonProperty("msg")
    private String message;

    @JsonProperty("status_code")
    private int statusCode;

    @JsonProperty("data")
    private T data;

    public final boolean isSuccessful() {
        return this.getStatusCode() == 0;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
    }

    public T getData() {
        return data;
    }

    public void setData(final T data) {
        this.data = data;
    }
}
