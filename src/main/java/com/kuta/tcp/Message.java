package com.kuta.tcp;

import com.google.gson.annotations.SerializedName;

/**
 * Class used for storing message information
 */
public class Message {
    @SerializedName("peer_id")
    public String peerId;
    @SerializedName("message")
    public String message;
    public Message(String peerId, String message) {
        this.peerId = peerId;
        this.message = message;
    }
    @Override
    public String toString() {
        return peerId+":"+message;
    }

}
