package com.kuta.web.backend;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuta.tcp.Message;
import com.kuta.tcp.TCPServer;
/**
 * This class serves as the controller for the API Endpoints 
 */
@RestController
public class ApiController {
    
    private final TCPServer tcpServer;

    @Autowired
    public ApiController(TCPServer tcpServer){
        this.tcpServer = tcpServer;
    }
    /**
     * Returns the current msg history maintaned by the TCP server
     * @return JSON representation of the msg history
     */
    @GetMapping("/messages")
    public String messages(){
        tcpServer.historyLocks.readLock().lock();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<MessageModel> msgs = new ArrayList<>();
        for(Map.Entry<String,Message> entry : tcpServer.msgHistory.entrySet()){
            msgs.add(new MessageModel(entry.getValue().peerId,entry.getValue().message,entry.getKey()));
        }
        String response = gson.toJson(msgs);
        tcpServer.historyLocks.readLock().unlock();
        return response;
    }
    /**
     * Endpoint for sending a message
     * @param msg Message to be sent, has a default value
     * @param recipient Peer to send the message to, default to everyone
     * @return Status message 
     */
    @GetMapping("/send")
    public String messages(@RequestParam(name="message", required=true, 
        defaultValue="Default message. Next time provide message value please") String msg,@RequestParam(name="peer", required=true, 
        defaultValue="all") String recipient){
        System.out.println("MSG INPUT:"+msg);
        tcpServer.sendMessage(recipient,msg);
        return "{\"status\":\"ok\"}";
    }
}
