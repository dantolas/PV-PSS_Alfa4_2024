package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kuta.Config;
import com.kuta.util.color.ColorMe;

/**
 * TCP Server running on specified network socket.
 * Responsible for receiving TCP connections, and sending TCP message.
 * 
 * Implements Runnable interface to be run as a Thread
 * To run the server either create a new Thread and start it or just call the run() method
 *
 * Most of the server functionality is run in seperate threads in parallel, so This class itself
 * mostly wraps that functionality together
 *
 */
@Service
public class TCPServer implements Runnable{

    private final String TCP = ColorMe.yellow("TCP");

    private InterfaceAddress ip;
    private int port;
    private ServerSocket server;
    private Socket peer;
    private int listenerTimeout;
    private int clientTimeout;

    public String peerId;
    public volatile boolean running;

    public PrintStream sysout;

    public TreeMap<String,Message> msgHistory;
    public ReadWriteLock historyLocks;
    public int msgLimit;

    public List<TCPConnection> outConnections;
    public ReadWriteLock outLocks;

    public class newMsgLock{
        public String[] msgAndRecipient;
    }
    private newMsgLock msgLock;

    /**
     * Main constructor for creating a new TCPServer
     * 
     * @param running Indicates whether the server and all clients should be running
     * @param ip IPv4 address to bind to
     * @param port Network port to bind to
     * @param out OutputStream to print useful information
     * @param handlerTimeout Timeout for receiving TCP packets
     * @param msgLimit Msg limit per minute for every TCP Client trying to send information to server
     */
    @Autowired
    public TCPServer(Config config,InterfaceAddress ip, int port,boolean running) {
        this.sysout= System.out;
        this.msgHistory = new TreeMap<>((id1,id2)-> Long.compare(Long.parseLong(id1),Long.parseLong(id2))){{
            put("2",new Message("I'm peer","I'm a peer"));
            put("3",new Message("Definitely not peer","I'm not a peer"));
        }};
        this.historyLocks = new ReentrantReadWriteLock();
        //this.msgsToSend = new LinkedList<>();
        //this.sendLocks = new ReentrantReadWriteLock();
        this.outConnections = new ArrayList<>();
        this.outLocks = new ReentrantReadWriteLock();
        this.msgLock = new newMsgLock();
        this.peerId = config.peerId;
        this.ip = ip;
        this.port = port;
        this.listenerTimeout = config.tcpListenerTimeout;
        this.clientTimeout = config.tcpClientTimeout;
        this.msgLimit = config.tcpMsgLimit;
        
    }

    public TCPServer setPeerId(String peerId){
        this.peerId = peerId;
        return this;
    }
    public TCPServer setIp(InterfaceAddress ip){
        this.ip = ip;
        return this;
    }
    public TCPServer setPort(int port){
        this.port = port;
        return this;
    }
    public TCPServer setListenerTimeout(int listenerTimeout){
        this.listenerTimeout = listenerTimeout;
        return this;
    }
    public TCPServer setClientTimeout(int clientTimeout){
        this.clientTimeout = clientTimeout;
        return this;
    }
    public TCPServer setMsgLimit(int limitPerMinute){
        this.msgLimit = limitPerMinute;
        return this;
    }

    public void connectClient(InetAddress ip, int port,String endpointPeerId){
        outLocks.writeLock().lock();
        this.outConnections.add(
            new TCPConnection(ip,port,clientTimeout,endpointPeerId,this.peerId,this.sysout)
            .setMsgHistory(msgHistory, historyLocks)
            .setSelfDeleting(outConnections,outLocks)
            .setup()
        );
        outLocks.writeLock().unlock();
    }

    public void sendMessage(String recipientPeerId, String message){
        synchronized(this.msgLock){
        msgLock.msgAndRecipient = new String[]{recipientPeerId,message};
        msgLock.notify();
        }
    }
    public static void syncMessages(TreeMap<String,Message> msgHistory,TreeMap<String,Message> syncMsgs){
        for(Map.Entry<String,Message> entry : syncMsgs.entrySet()){
            if(msgHistory.keySet().contains(entry.getKey())) continue;
            if(msgHistory.size() >=100) msgHistory.pollFirstEntry();
            msgHistory.put(entry.getKey(),entry.getValue());
        }
    }

    public void printMessages(){
        historyLocks.readLock().lock();
        sysout.println(msgHistory);
        historyLocks.readLock().unlock();
    }
    /**
     * Should be used on server startup, sets up necessary things for server to run
     */
    public void setup(){
        sysout.println(TCP+"|STARTING TCP SERVER|");
        running = true;
        TCPSender sender = new TCPSender(this.running,outConnections, outLocks,msgLock, sysout);
        new Thread(sender).start();
        sysout.println(TCP+"|TCP SERVER LISTENING ON "
            +ColorMe.green(ip.getAddress().toString())+":"+ColorMe.green(Integer.toString(port))+"|");
    }

    /**
     * Relases all resources and shuts down the server
     */
    public void tearDown(){
        sysout.println(TCP+"|TCP SERVER TEARING DOWN|");
        try {
            for (TCPConnection tcpConnection : outConnections) {
                tcpConnection.tearDown();
            }
            server.close();
        } catch (IOException e) {
        }
        sysout.println(TCP+"|TCP SERVER ENDED|");
    }


    @Override
    public void run() {
        setup();
        try {
            server = new ServerSocket(port,0,ip.getAddress());
            while(running){
                peer = server.accept();
                sysout.println(TCP+"|Handing peer to handler:"+ColorMe.green(peer.getInetAddress().toString()+":"+peer.getPort()));
                new Thread(new TCPListener(this,peer,listenerTimeout)).start();
            }
        } catch (IOException e) {
            sysout.println(TCP+ColorMe.red("|Error occured on server"));
            e.printStackTrace();
        }
        finally{
            tearDown();
        }
    }

    
}
