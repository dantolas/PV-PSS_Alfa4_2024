package com.kuta.tcp;

import com.google.gson.annotations.SerializedName;

/**
 * TCPQuestion
 */
public class TCPHello {
    @SerializedName("command")
    private String command;
    @SerializedName("peer_id")
    private String peerId;

    public boolean isValid(){
        return command!=null&&peerId!=null;
    }
}
