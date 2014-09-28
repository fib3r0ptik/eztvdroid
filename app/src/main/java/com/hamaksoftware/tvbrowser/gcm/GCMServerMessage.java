package com.hamaksoftware.tvbrowser.gcm;

import java.util.List;

import info.besiera.api.models.Subscription;

public class GCMServerMessage {
    public GCMServerMessage(){}
    private String type;
    private List<String> content;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getData() {
        return content;
    }

    public void setData(List<String> data) {
        this.content = data;
    }
}
