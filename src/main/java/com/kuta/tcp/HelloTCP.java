package com.kuta.tcp;

import com.google.gson.annotations.SerializedName;

/**
 * Class used for JSON serialization of the TCP Hello command
 */
public class HelloTCP {
    @SerializedName("command")
    public String command;
    @SerializedName("peer_id")
    public String peerId;

    public HelloTCP(String command, String peerId) {
        this.command = command;
        this.peerId = peerId;
    }

    public boolean isValid(){
        return command!=null&&peerId!=null;
    }
}
