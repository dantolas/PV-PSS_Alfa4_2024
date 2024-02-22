package com.kuta.tcp;

import com.google.gson.annotations.SerializedName;

/**
 * Class used for JSON serialization of the TCP Hello command
 */
public class TCPHello {
    @SerializedName("command")
    public String command;
    @SerializedName("peer_id")
    public String peerId;

    public TCPHello(String command, String peerId) {
        this.command = command;
        this.peerId = peerId;
    }

    public boolean isValid(){
        return command!=null&&peerId!=null;
    }
}
