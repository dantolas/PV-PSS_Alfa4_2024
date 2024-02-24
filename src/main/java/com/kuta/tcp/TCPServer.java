package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
public class TCPServer implements Runnable{

    private final String TCP = ColorMe.yellow("TCP");

    private InterfaceAddress ip;
    private int port;
    private ServerSocket server;
    private Socket peer;
    private final int LISTENER_TIMEOUT;
    private final int CLIENT_TIMEOUT;

    public String peerId;
    public volatile boolean running;

    public PrintStream sysout;

    public HashMap<String,Message> msgHistory;
    public ReadWriteLock historyLocks;
    public int receiveMsgLimit;

    private List<TCPConnection> outConnections;
    private ReadWriteLock outLocks;
    public Queue<String[]> msgsToSend;
    public ReadWriteLock sendLocks;

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
    public TCPServer(boolean running,InterfaceAddress ip, int port,String peerId,
        PrintStream out, int clientTimeout,int listenerTimeout,int msgLimit) {
        this.peerId = peerId;
        this.ip = ip;
        this.sysout= (out);
        this.port = port;
        this.LISTENER_TIMEOUT = listenerTimeout;
        this.CLIENT_TIMEOUT = clientTimeout;
        this.msgHistory = new HashMap<>(){{
            put("1",new Message("peer123","I'm a femboy"));
            put("2",new Message("I'm peer","I'm a peer"));
            put("3",new Message("Definitely not peer","I'm not a peer"));
        }};
        this.historyLocks = new ReentrantReadWriteLock();
        this.receiveMsgLimit = msgLimit;
        this.msgsToSend = new LinkedList<>();
        this.sendLocks = new ReentrantReadWriteLock();
        this.outConnections = new ArrayList<>();
        this.outLocks = new ReentrantReadWriteLock();
    }

    public void connectClient(InetAddress ip, int port,String endpointPeerId){
        outLocks.writeLock().lock();
        this.outConnections.add(new TCPConnection(ip,port,CLIENT_TIMEOUT,endpointPeerId,this.peerId,this.sysout));
        outLocks.writeLock().unlock();
    }

    public void sendMessage(String recipientPeerId, String message){
        sendLocks.writeLock().lock();
        msgsToSend.add(new String[]{recipientPeerId,message});
        sendLocks.writeLock().unlock();
    }



    /**
     * Should be used on server startup, sets up necessary things for server to run
     */
    public void setup(){
        sysout.println(TCP+"|STARTING TCP SERVER|");
        TCPSender sender = new TCPSender(outConnections, outLocks, msgsToSend, sendLocks, sysout);
        new Thread(sender).start();
        sysout.println(TCP+"|TCP SERVER LISTENING ON "
            +ColorMe.green(ip.getAddress().toString())+":"+ColorMe.green(Integer.toString(port))+"|");
        running = true;
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
                new Thread(new TCPListener(this,peer,LISTENER_TIMEOUT)).start();
                //new Thread(new TCPHandler(this,new Socket(peer.getInetAddress(),peer.getPort()))).start();
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
