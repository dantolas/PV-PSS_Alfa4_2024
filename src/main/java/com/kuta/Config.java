package com.kuta;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.gson.annotations.SerializedName;
import com.kuta.util.IO;
import com.kuta.vendor.GsonParser;

/**
 * Config
 */
public class Config {
    @SerializedName("broadcast_frequency_milis")
    public int broadcastFrequency;
    @SerializedName("peer_id")
    public String peerId;
    @SerializedName("default_timeout_milis")
    public int defaultTimeout;
    @SerializedName("default_tcp_timeout_milis")
    public int tcpTimeout;

    public Config() {
    }

    @Override
    public String toString() {
        return GsonParser.parser.toJson(this);
    }

    public static Config fromFile(String filepath) throws FileNotFoundException, IOException{
        String json = IO.readFileIntoString(filepath);
        return GsonParser.parser.fromJson(json,Config.class);
    }
    
}
