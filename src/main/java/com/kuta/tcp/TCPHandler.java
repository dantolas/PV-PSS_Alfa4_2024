package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

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

        TCPHello question = GsonParser.parser.fromJson(msg,TCPHello.class);
        if(!question.isValid()) return;
        readLock.lock();
        HashMap<String,Message> msgHistory = server.msgHistory;
        TCPAnswer answer = new TCPAnswer("ok",msgHistory);
        String jsonAnswer = GsonParser.parser.toJson(answer);
        server.sysout.println(TCPh+"|Answer:"+jsonAnswer);
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
            handleHello(msg);
        } catch (Exception e) {
        }
        try{
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
        in.close();
        out.close();
    }


    @Override
    public void run() {
        try {
            setup();
            while(true){
                String msg = this.in.nextLine();
                handle(msg);

                break;
            }
        } catch (Exception e) {
        }
        finally{
            tearDown();
        }
    }
}
