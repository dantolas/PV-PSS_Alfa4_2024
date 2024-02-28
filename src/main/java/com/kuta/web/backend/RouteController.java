package com.kuta.web.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kuta.tcp.Message;
import com.kuta.tcp.TCPConnection;
import com.kuta.tcp.TCPServer;

/**
 * Controller for static web content
 */
@Controller
public class RouteController {

    private final TCPServer tcpServer;
    private final List<MessageModel> msgs;
    private final List<String> peers;
    @Autowired
    public RouteController(TCPServer server){
        this.peers = new ArrayList<>(){{
            add("molic-peer-1");
            add("molic-peer-2");
            add("molic-peer-3");
        }};
        this.msgs = new ArrayList<>();
        this.tcpServer = server;
    }

	@GetMapping("/greeting")
	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
		model.addAttribute("msgs", msgs);
		return "greeting";
	}
	@GetMapping("/")
	public String index(Model model) { 
        
        updateMsgs();
        updatePeers();
        model.addAttribute("msgs",msgs);
        model.addAttribute("peers",peers);
		return "index";
	}

    private void updatePeers(){
        this.peers.clear();
        tcpServer.outLocks.readLock().lock();
        for(TCPConnection activeConn : tcpServer.outConnections){
            this.peers.add(activeConn.endpointPeerId);
        }
        tcpServer.outLocks.readLock().unlock();
    }

    private void updateMsgs(){
        msgs.clear();
        tcpServer.historyLocks.readLock().lock();
        for(Map.Entry<String,Message> entry : tcpServer.msgHistory.entrySet()){
            msgs.add(new MessageModel(entry.getValue().peerId,entry.getValue().message,entry.getKey()));
        }
        tcpServer.historyLocks.readLock().unlock();
    }

}
