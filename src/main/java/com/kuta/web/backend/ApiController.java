package com.kuta.web.backend;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuta.tcp.Message;
import com.kuta.tcp.TCPServer;
@RestController
public class ApiController {
    
    private final TCPServer tcpServer;

    @Autowired
    public ApiController(TCPServer tcpServer){
        this.tcpServer = tcpServer;
    }
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
    @GetMapping("/send")
    public String messages(@RequestParam(name="message", required=true, 
        defaultValue="Default message. Next time provide message value please") String msg){
        System.out.println("MSG INPUT:"+msg);
        tcpServer.sendMessage("all",msg);
        return "{\"status\":\"ok\"}";
    }
}
