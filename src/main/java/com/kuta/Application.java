package com.kuta;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kuta.tcp.TCPServer;
import com.kuta.udp.UDPServer;
import com.kuta.util.ErrorHandler;

import jakarta.annotation.PostConstruct;

/**
 * Wrapper for the entire application.
 * Create an instance and call the run() method and the application will start
 */
@Component
public class Application {
    private  UDPServer udpServer;
    private  TCPServer tcpServer;
    private  ErrorHandler errorHandler;


    /**
     * Main constructor
     * @param udpServer com.kuta.udp.UDPServer that will start
     * @param tcpServer com.kuta.tcp.TCPServer that will start
     * @param errorHandler ErrorHandler that can log some error
     */
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
        }catch(Exception e){
            errorHandler.handle(e);
        }
    };
    
}
