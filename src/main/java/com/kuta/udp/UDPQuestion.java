package com.kuta.udp;

import com.google.gson.annotations.SerializedName;

public class UDPQuestion{
    @SerializedName("command")
    public String command;
    @SerializedName("peer_id")
    public String peerId;
    @Override
    public String toString() {
        return "";
    }
    public UDPQuestion(String command, String peerId) {
        this.command = command;
        this.peerId = peerId;
    }
    public UDPQuestion() {
    }

    public boolean isValid(){
        return command != null && peerId != null;
    }


}
