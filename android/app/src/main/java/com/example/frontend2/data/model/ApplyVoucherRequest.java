package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

public class ApplyVoucherRequest {

    @SerializedName("code")
    private String code;

    public ApplyVoucherRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
