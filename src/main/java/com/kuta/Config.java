package com.kuta;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.stereotype.Component;

import com.google.gson.annotations.SerializedName;
import com.kuta.util.IO;
import com.kuta.vendor.GsonParser;

import jakarta.annotation.PostConstruct;

/**
 * Object for JSON serialization and deserialization
 */
@Component
public class Config {
    @SerializedName("broadcast_frequency_milis")
    public int broadcastFrequency;
    @SerializedName("peer_id")
    public String peerId;
    @SerializedName("udp_timeout")
    public int udpTimeout;
    @SerializedName("tcp_client_timeout")
    public int tcpClientTimeout;
    @SerializedName("tcp_listener_timeout")
    public int tcpListenerTimeout;
    @SerializedName("msg_limit_minute")
    public int tcpMsgLimit;
    @SerializedName("ipv4_addr")
    public String ip;


    public Config(){}

    @PostConstruct
    public void setup() throws FileNotFoundException, IOException{
        Config conf = fromFile("conf/config.json");
        this.broadcastFrequency = conf.broadcastFrequency;
        this.tcpClientTimeout = conf.tcpClientTimeout;
        this.udpTimeout = conf.udpTimeout;
        this.tcpMsgLimit = conf.tcpMsgLimit;
        this.tcpListenerTimeout = conf.tcpListenerTimeout;
        this.peerId = conf.peerId;
        this.ip = conf.ip;
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
