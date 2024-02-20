package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Queue;
import java.util.Scanner;

import com.kuta.util.color.ColorMe;

/**
 * TCPHandler
 */
public class TCPHandler implements Runnable{

    private TCPServer server;
    private Socket peer;
    private Scanner in;
    private PrintWriter out;

    private final String TCPc = ColorMe.purple("TCPc");


    public TCPHandler(TCPServer server, Socket peer) {
        this.server = server;
        this.peer = peer;
    }


    public void handle(){
        server.sysout.println(TCPc+" Accepted connection from:"+peer.getInetAddress()+":"+peer.getPort());
        server.sysout.println(TCPc+peer.getLocalSocketAddress());
    }

    public void setup(){
        try {
            out = new PrintWriter(peer.getOutputStream(),true);
            in = new Scanner(peer.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void run() {
        handle();
    }
}
