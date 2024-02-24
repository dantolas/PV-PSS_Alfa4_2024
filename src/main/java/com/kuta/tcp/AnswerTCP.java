package com.kuta.tcp;

import java.util.HashMap;

import com.google.gson.annotations.SerializedName;

/**
 * Class used for JSON serialization of the TCP Answer command
 */
public class AnswerTCP {

    @SerializedName("status")
    public String status;
    @SerializedName("messages")
    public HashMap<String,Message> messages;

    public AnswerTCP(String status, HashMap<String, Message> messages) {
        this.status = status;
        this.messages = messages;
    }

    public boolean isValid(){
        return status != null && messages != null;
    }

}
