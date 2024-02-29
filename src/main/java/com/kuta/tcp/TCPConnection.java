package com.kuta.tcp;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;

import com.kuta.udp.UDPServer;


/**
 * Wrapper for a TCP Client, provides the client with more functionality in regards to
 * managing the TCPServer outConnections list of established connections, printing information
 * to standart out and more.
 */
public class TCPConnection implements Comparable<TCPConnection>{

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
    public List<TCPConnection> connections;
    public ReadWriteLock connLocks;
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

    /**
     * @param msgHistory
     * @param historyLocks
     * @return
     */
    public TCPConnection setMsgHistory(TreeMap<String,Message> msgHistory, ReadWriteLock historyLocks){
        this.msgHistory = msgHistory;
        this.historyLocks = historyLocks;
        return this;
    }

    /**
     * @param connectionsList
     * @param lock
     * @return
     */
    public TCPConnection setSelfDeleting(List<TCPConnection> connectionsList,ReadWriteLock lock){
        this.connections = connectionsList;
        this.connLocks = lock;
        return this;
    }

    /**
     * Set the msglock msg field to the new message, and notifies the client waiting for the lock
     * to send it to the other side
     * @param message
     */
    public void sendMessage(String message){
        synchronized(this.lock){
            this.lock.msg = message;
            lock.notify();
        }
    }
    /**
     * @return
     */
    public TCPConnection setup(){
        this.client = new TCPClient(this);
        
        new Thread(client).start();
        return this;
    };
    /**
     * 
     */
    public void tearDown(){
        client.tearDown();
    }
    public void end(){
        try {
            connLocks.writeLock().lock();
            connections.remove(this);
            connLocks.writeLock().unlock();
            client.tearDown();
        } catch (Exception e) {
        }
        try {
            UDPServer.lock.writeLock().lock();
            UDPServer.knownPeers.remove(new InetSocketAddress(ip,port));
            UDPServer.lock.writeLock().unlock();
        } catch (Exception e) {
        }
    }

    @Override
    public int compareTo(TCPConnection o) {
        if(o.endpointPeerId.equals(this.endpointPeerId))return 0;
        return 1;
        
    }

}
