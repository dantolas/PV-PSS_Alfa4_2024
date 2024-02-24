package com.kuta.tcp;

import java.io.PrintStream;
import java.net.InetAddress;


/**
 * TCPConnection
 */
public class TCPConnection {

    public class MsgLock{
        public String msg;

        public MsgLock(){
            msg = "";
        }
    }

    public String endpointPeerId;
    public TCPClient client;
    public volatile String message;
    public MsgLock lock;

    public TCPConnection(InetAddress ip, int port,int timeout, String endpointPeerId,String serverPeerId,PrintStream sysout) {
        this.endpointPeerId = endpointPeerId;
        lock = new MsgLock();
        this.client = new TCPClient(ip, port,timeout, endpointPeerId, serverPeerId, sysout, message,lock);
        new Thread(client).start();
    }

    public void sendMessage(String message){
        synchronized(this.lock){
            this.lock.msg = message;
            lock.notify();
        }
    }

    public void tearDown(){
        client.tearDown();
    }

}
