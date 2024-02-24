package com.kuta.tcp;

import java.io.PrintStream;
import java.net.InetAddress;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


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
    public String serverPeerId;
    public String endpointPeerId;
    public TCPClient client;
    public MsgLock lock;
    public TreeMap<String,Message> msgHistory;
    public ReadWriteLock historyLocks;
    public InetAddress ip;
    public int port;
    public int timeout;
    public PrintStream sysout;

    public TCPConnection(InetAddress ip, int port
        ,int timeout, String endpointPeerId,String serverPeerId,PrintStream sysout) {
        this.serverPeerId = serverPeerId;
        this.endpointPeerId = endpointPeerId;
        this.ip = ip;
        this.port = port;
        this.timeout = timeout;
        this.sysout = sysout;
        lock = new MsgLock();
    }

    public TCPConnection setMsgHistory(TreeMap<String,Message> msgHistory, ReadWriteLock historyLocks){
        this.msgHistory = msgHistory;
        this.historyLocks = historyLocks;
        return this;
    }

    public void sendMessage(String message){
        synchronized(this.lock){
            this.lock.msg = message;
            lock.notify();
        }
    }
    public TCPConnection setup(){
        this.client = new TCPClient(this);
        new Thread(client).start();
        return this;
    };
    public void tearDown(){
        client.tearDown();
    }

}
