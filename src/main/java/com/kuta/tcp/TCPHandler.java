package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;

import com.kuta.util.color.ColorMe;
import com.kuta.vendor.GsonParser;

/**
 * TCPHandler
 */
public class TCPHandler implements Runnable{

    private TCPServer server;
    private Socket peer;
    private String peerId;
    private Scanner in;
    private PrintWriter out;
    private final int ID = new Random().nextInt(1000);
    private int timeout;

    private final Lock readLock;
    private final Lock writeLock;


    private final String TCPh = ColorMe.yellow("TCPh-"+ID);

    public TCPHandler(TCPServer server, Socket peer, int timeout) {
        this.server = server;
        this.peer = peer;
        this.timeout = timeout;
        this.readLock = server.locks.readLock();
        this.writeLock = server.locks.writeLock();
    }

    private void handleHello(String msg){
        TCPHello introduction = GsonParser.parser.fromJson(msg,TCPHello.class);
        if(!introduction.isValid()) return;
        readLock.lock();
        HashMap<String,Message> msgHistory = server.msgHistory;
        TCPAnswer answer = new TCPAnswer("ok",msgHistory);
        String jsonAnswer = GsonParser.parser.toJson(answer);
        server.sysout.println(TCPh+"|Sending answer");
        out.println(jsonAnswer);
        readLock.unlock();
    }

    private void handleNewMessage(String msg){
        TCPNewMessage newMsg = GsonParser.parser.fromJson(msg,TCPNewMessage.class);
        if(!newMsg.isValid()) return;

        if(this.peerId == null){
            server.sysout.println(TCPh+"| Message received without proper manners and introduction");
            String response = "{\"status\":\"bad\",\"reason\":\"Have some manners and introduce yourself first\"}";
            out.write(response,0,response.getBytes().length);
            return;
        }
        writeLock.lock();
        server.msgHistory.put(newMsg.msgId,new Message(peerId,newMsg.msg));
        writeLock.unlock();
    }


    public void handle(String msg){
        try {
            server.sysout.println(TCPh+"|Trying hello");
            handleHello(msg);
        } catch (Exception e) {
        }
        try{
            server.sysout.println(TCPh+"|Trying new message");
            handleNewMessage(msg);
        } catch(Exception e){
        }
    }

    public void setup(){
        try {
            out = new PrintWriter(peer.getOutputStream(),true);
            in = new Scanner(peer.getInputStream());
            server.sysout.println(TCPh+"|Accepted connection from:"+ColorMe.green(peer.getInetAddress()+":"+peer.getPort()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void tearDown(){
        server.sysout.println(TCPh+"|Shutting down|");
    }

    private void checkTimeout(long timePassed) throws TimeoutException{
        if(timePassed > timeout) throw new TimeoutException("Connection timed out");
    };
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
    @Override
    public void run() {
        try {
            setup();
            while(true){
                server.sysout.println(TCPh+"|Waiting for msg");
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
