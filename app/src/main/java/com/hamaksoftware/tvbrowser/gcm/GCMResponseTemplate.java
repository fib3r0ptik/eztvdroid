package com.hamaksoftware.tvbrowser.gcm;

public class GCMResponseTemplate {
    public GCMResponseTemplate(){}

    private GCMServerMessage message;


    public GCMServerMessage getMessage() {
        return message;
    }

    public void setMessage(GCMServerMessage message) {
        this.message = message;
    }
}
