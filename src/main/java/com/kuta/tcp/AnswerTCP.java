package com.kuta.tcp;

import java.util.TreeMap;

import com.google.gson.annotations.SerializedName;

/**
 * Class used for JSON serialization of the TCP Answer command
 */
public class AnswerTCP {

    @SerializedName("status")
    public String status;
    @SerializedName("messages")
    public TreeMap<String,Message> messages;

    public AnswerTCP(String status, TreeMap<String, Message> messages) {
        this.status = status;
        this.messages = messages;
    }

    public boolean isValid(){
        return status != null && messages != null;
    }

}
