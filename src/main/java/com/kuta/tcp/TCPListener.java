package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;

import com.kuta.util.color.ColorMe;
import com.kuta.vendor.GsonParser;

/**
 * Class responsible for handling a single TCP connection
 * to the server, receives messages and responds.
 * Does not send messages, only responds.
 * Is created and started as a new Thread, tasked with maintaining the connection it was created
 * with in UDPServer.
 * */
public class TCPListener implements Runnable{

    private TCPServer server;
    private Socket peer;
    private String peerId;
    private Scanner in;
    private PrintWriter out;
    private final int ID = new Random().nextInt(1000);
    private int timeout;
    private int msgLimit;
    private int msgsLastMinute;
    private long stopwatch;

    private final Lock readLock;
    private final Lock writeLock;


    private final String TCPh = ColorMe.yellow("TCPl-"+ID);

    /**
     * Main constructor for handler.
     * @param server TCPServer object that started this handler
     * @param peer The socket conn to be handled
     * @param timeout
     */
    public TCPListener(TCPServer server, Socket peer, int timeout) {
        this.server = server;
        this.peer = peer;
        this.timeout = timeout;
        this.readLock = server.historyLocks.readLock();
        this.writeLock = server.historyLocks.writeLock();
        this.msgLimit = server.msgLimit;
    }

    /**
     * Check listen and send timeout. If timeout limit is reached, the Thread will kill itself
     * @param timePassed Time passed since receive or send start
     * @throws TimeoutException
     */
    private void checkTimeout(long timePassed) throws TimeoutException{
        if(timePassed > timeout) throw new TimeoutException("Connection timed out");
    };
    /**
     * Method that checks spam. If the msg limit has been reached, it will send a timeout msg
     * to the client connected, and sleep until the end of the minute.
     */
    private void checkSpam(){
        long currTime = System.currentTimeMillis();
        long timeSince = currTime - stopwatch;
        if(timeSince > 60_000){
            stopwatch = currTime;
            msgsLastMinute = 0;
            return;
        }

        if(msgsLastMinute < msgLimit)return;

        String response = "{\"status\":\"bad\",\"reason\":\"U have reached the msg limit, wait"+(60_000 - timeSince)/1000+"s\"}";
        out.println(response);
        while(true){
            if((System.currentTimeMillis()-stopwatch) > 60_000) break;
        }

    }
    /**
     * Read response on socket, with some error handling
     * @return String of the packet content
     * @throws TimeoutException
     */
    private String readResponse() throws TimeoutException{
        String resp = null;
        long timeStartedWaiting = System.currentTimeMillis();
        while(resp == null){
            long waitingTime = System.currentTimeMillis() - timeStartedWaiting;
            checkTimeout(waitingTime);
            try {
                resp = in.nextLine();
            } catch (Exception e) {
            }
        }
        return resp;
    }
    private void handleHello(String msg){
        HelloTCP introduction = GsonParser.parser.fromJson(msg,HelloTCP.class);
        if(!introduction.isValid()) return;
        readLock.lock();
        TreeMap<String,Message> msgHistory = server.msgHistory;
        AnswerTCP answer = new AnswerTCP("ok",msgHistory);
        String jsonAnswer = GsonParser.parser.toJson(answer);
        this.peerId = introduction.peerId;
        out.println(jsonAnswer);
        readLock.unlock();
    }

    /**
     * Handles packet that contains a new message command inside
     * @param msg Packet content
     */
    private void handleNewMessage(String msg){
        NewTCPMessage newMsg = GsonParser.parser.fromJson(msg,NewTCPMessage.class);
        if(!newMsg.isValid()) return;

        String response = "{\"status\":\"bad\",\"reason\":\"Have some manners and introduce yourself first\"}";
        if(this.peerId == null){
            server.sysout.println(TCPh+"| Message received without proper manners and introduction");
            out.println(response);
            return;
        }
        writeLock.lock();
        server.sysout.println(TCPh+"|Adding new msg");
        if(server.msgHistory.size() >=100) server.msgHistory.pollFirstEntry();
        server.msgHistory.put(newMsg.msgId,new Message(peerId,newMsg.msg));
        server.sysout.println(server.msgHistory);
        writeLock.unlock();
        msgsLastMinute++;
        response = "{\"status\":\"ok\"}";
        out.println(response);
    }


    /**
     * Handle input read from client
     * @param msg Packet content
     */
    public void handle(String msg){
        try {
            handleHello(msg);
        } catch (Exception e) {
        }
        try{
            handleNewMessage(msg);
        } catch(Exception e){
        }
    }

    /**
     * Setup the TCPListener, should be used before program loop
     */
    public void setup(){
        try {
            out = new PrintWriter(peer.getOutputStream(),true);
            in = new Scanner(peer.getInputStream());
            server.sysout.println(TCPh+"|Accepted connection from:"+ColorMe.green(peer.getInetAddress()+":"+peer.getPort()));
            this.stopwatch = System.currentTimeMillis();
            this.msgsLastMinute = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     * Kill the Listener
     */
    public void tearDown(){
        server.sysout.println(TCPh+"|Shutting down|");
        try {
            out.close();
            in.close();
        } catch (Exception e) {
        }
    }

    @Override
    public void run() {
        try {
            setup();
            while(server.running){
                checkSpam();
                server.sysout.println(TCPh+"|Listening for msg");
                String msg = readResponse();
                server.sysout.println(TCPh+"|Msg received:"+msg);
                handle(msg);
            }
        } catch (TimeoutException e) {
            server.sysout.println(TCPh+"|Connection timed out");
        } catch(Exception e){
            e.printStackTrace();
        }
        finally{
            tearDown();
        }
    }
}
