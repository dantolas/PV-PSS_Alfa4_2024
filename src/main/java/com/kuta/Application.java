package com.kuta;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kuta.tcp.TCPServer;
import com.kuta.udp.UDPServer;
import com.kuta.util.ErrorHandler;

import jakarta.annotation.PostConstruct;

/**
 * Application
 */
@Component
public class Application {
    private  UDPServer udpServer;
    private  TCPServer tcpServer;
    private  ErrorHandler errorHandler;


    @Autowired
    public Application(
        UDPServer udpServer, TCPServer tcpServer,ErrorHandler errorHandler) {
        this.udpServer = udpServer;
        this.tcpServer = tcpServer;
        this.errorHandler = errorHandler;
        
    }
    @PostConstruct
    public void run(){
        try {
            tcpServer.setup();
            udpServer.setup();

            new Thread(tcpServer).start();
            new Thread(udpServer).start();
        } catch (IOException e) {
            errorHandler.handle(e);
        }
    };
    
}
