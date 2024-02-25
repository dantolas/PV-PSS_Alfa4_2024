package com.kuta;

import java.io.IOException;
import java.net.InterfaceAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kuta.tcp.TCPServer;
import com.kuta.udp.UDPServer;
import com.kuta.util.ErrorHandler;
import com.kuta.util.NetworkPicker;

import jakarta.annotation.PostConstruct;

/**
 * Application
 */
@Component
public class Application {
    private  UDPServer udpServer;
    private  TCPServer tcpServer;
    private  Config config;
    private  ErrorHandler errorHandler;
    private  NetworkPicker picker;

    private final ConfigService configService;

    @Autowired
    public Application(
        UDPServer udpServer, TCPServer tcpServer, NetworkPicker picker
        , Config config,ConfigService configService,ErrorHandler errorHandler
        
    ) {
        this.udpServer = udpServer;
        this.tcpServer = tcpServer;
        this.picker = picker;
        this.config = config;
        this.configService = configService;
        this.errorHandler = errorHandler;
        
    }
    @PostConstruct
    public void run(){
        try {
            config = configService.loadConfig();
            InterfaceAddress ip = picker.pickAddress();
            this.tcpServer = tcpServer
            .setPeerId(config.peerId)
            .setIp(ip)
            .setPort(9876)
            .setMsgLimit(config.tcpMsgLimit)
            .setClientTimeout(config.tcpClientTimeout)
            .setListenerTimeout(config.tcpListenerTimeout);
            
            this.udpServer = udpServer
            .setPort(9876)
            .setIp(ip)
            .setPeerId(config.peerId)
            .setSocket()
            .setBroadcastTimer(config.broadcastFrequency)
            .setDefaultTimeout(config.udpTimeout)
            .setTCP(tcpServer);

            tcpServer.setup();
            udpServer.setup();

            new Thread(tcpServer).start();
            new Thread(udpServer).start();
        } catch (IOException e) {
            errorHandler.handle(e);
        }
    };
    
}
