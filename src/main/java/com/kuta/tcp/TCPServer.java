package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InterfaceAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.kuta.util.color.ColorMe;

/**
 * TCPSocket
 */
public class TCPServer implements Runnable{

    private InterfaceAddress ip;
    private int port;
    private ServerSocket server;
    private Socket peer;
    private boolean running;
    private final int HANDLER_TIMEOUT;

    public PrintStream sysout;

    private final String TCP = ColorMe.yellow("TCP");

    public HashMap<String,Message> msgHistory;
    public ReadWriteLock locks;
    public int msgLimit;

    public TCPServer(boolean running,InterfaceAddress ip, int port, PrintStream out, int handlerTimeout,int msgLimit) {
        this.ip = ip;
        this.sysout= (out);
        this.port = port;
        this.HANDLER_TIMEOUT = handlerTimeout;
        this.msgHistory = new HashMap<>(){{
            put("1",new Message("peer123","I'm a femboy"));
            put("2",new Message("I'm peer","I'm a peer"));
            put("3",new Message("Definitely not peer","I'm not a peer"));
        }};
        this.locks = new ReentrantReadWriteLock();
        this.msgLimit = msgLimit;
    }


    public void setup(){
        sysout.println(TCP+"|STARTING TCP SERVER|");
        sysout.println(TCP+"|TCP SERVER LISTENING ON "+ColorMe.green(ip.getAddress().toString())+":"+ColorMe.green(Integer.toString(port))+"|");
        running = true;
    }

    public void tearDown(){
        sysout.println(TCP+"|TCP SERVER TEARING DOWN|");
        try {
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
                new Thread(new TCPHandler(this,peer,HANDLER_TIMEOUT)).start();
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
