package com.kuta.tcp;

import java.util.HashMap;

import com.google.gson.annotations.SerializedName;

/**
 * TCPAnswer
 */
public class TCPAnswer {

    @SerializedName("status")
    public String status;
    @SerializedName("messages")
    public HashMap<String,Message> messages;

    public TCPAnswer(String status, HashMap<String, Message> messages) {
        this.status = status;
        this.messages = messages;
    }

    public boolean isValid(){
        return status != null && messages != null;
    }

}
