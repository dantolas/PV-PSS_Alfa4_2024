package com.kuta.tcp;

import com.google.gson.annotations.SerializedName;

/**
 * TCPMessage
 */
public class Message {
    @SerializedName("peer_id")
    public String peer_id;
    @SerializedName("message")
    public String message;
    public Message(String peer_id, String message) {
        this.peer_id = peer_id;
        this.message = message;
    }

}
