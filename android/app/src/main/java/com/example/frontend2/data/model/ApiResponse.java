package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * A generic wrapper for API responses.
 * @param <T> the type of the data object in the response.
 */
public class ApiResponse<T> {

    @SerializedName("EC")
    private int errorCode;

    @SerializedName("DT")
    private T data;

    @SerializedName("EM")
    private String errorMessage;

    public int getErrorCode() {
        return errorCode;
    }

    public T getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isSuccess() {
        return errorCode == 0;
    }
}
