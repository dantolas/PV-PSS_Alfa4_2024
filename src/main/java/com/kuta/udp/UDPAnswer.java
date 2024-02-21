package com.kuta.udp;

import com.google.gson.annotations.SerializedName;

public class UDPAnswer{

    @SerializedName("status")
    public String status;
    @SerializedName("peer_id")
    public String peerId;
    @Override
    public String toString() {
        return "";
    }
    public UDPAnswer() {
    }
    public UDPAnswer(String status, String peerId) {
        this.status = status;
        this.peerId = peerId;
    }

    public boolean isValid(){
        return status != null && peerId != null;
    }




}
