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
    public long defaultTimeout;

    public Config() {
    }

    public static Config fromFile(String filepath) throws FileNotFoundException, IOException{
        String json = IO.readFileIntoString(filepath);
        return GsonParser.parser.fromJson(json,Config.class);
    }
    
}
