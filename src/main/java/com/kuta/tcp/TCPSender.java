package com.kuta.tcp;

import java.io.PrintStream;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReadWriteLock;

import com.kuta.util.color.ColorMe;

/**
 * TCPSender
 */
public class TCPSender implements Runnable{

    private final String TCPs;

    private PrintStream sysout;
    public List<TCPConnection> outConnections;
    public ReadWriteLock outLocks;
    public Queue<String[]> msgsToSend;
    public ReadWriteLock sendLocks;
    private boolean running;

    public TCPSender(boolean running,List<TCPConnection> outConnections,ReadWriteLock outLocks, Queue<String[]> msgsToSend,
        ReadWriteLock sendLocks, PrintStream sysout) {
        this.running = running;
        this.outConnections = outConnections;
        this.outLocks = outLocks;
        this.msgsToSend = msgsToSend;
        this.sendLocks = sendLocks;
        this.sysout = sysout;
        TCPs = ColorMe.yellow("TCPs");
    }

    private void sendMessage(String[] recipientAndMsg){
        String recipientPeerId = recipientAndMsg[0];
        String msg = recipientAndMsg[1];
        if(recipientPeerId.equalsIgnoreCase("all")){
            outLocks.readLock().lock();
            for (TCPConnection tcpConnection : outConnections) {
                tcpConnection.sendMessage(msg);
            }
            outLocks.readLock().unlock();
            return;
        }

        outLocks.readLock().lock();
        for (TCPConnection tcpConnection : outConnections) {
            if(!tcpConnection.endpointPeerId.equals(recipientPeerId)) continue;
            tcpConnection.sendMessage(msg);
            outLocks.readLock().unlock();
            return;
        }
        outLocks.readLock().unlock();
        sysout.println(TCPs+"|Didn't find message recipient " + ColorMe.green(recipientPeerId));
    }

    private void checkMsgsToSend(){
        
        sendLocks.readLock().lock();
        if(msgsToSend.peek() == null) {
            sendLocks.readLock().unlock();
            return;
        }
        sendMessage(msgsToSend.poll());
        sendLocks.readLock().unlock();
    }

    public void setup(){
        sysout.println(TCPs+"|Starting TCP Sender");
    }

    public void tearDown(){
        sysout.println(TCPs+"|Ending TCP Sender");
    }

    @Override
    public void run() {
        setup();
        while(running){
            checkMsgsToSend();
        }
        tearDown();
    }
}
