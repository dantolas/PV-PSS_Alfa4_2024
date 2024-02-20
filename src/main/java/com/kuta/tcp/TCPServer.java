package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InterfaceAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
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

    public PrintStream sysout;

    private final String TCP = ColorMe.yellow("TCP");

    public Queue<String> msgHistory;
    public ReadWriteLock lock;

    public TCPServer(InterfaceAddress ip, int port, PrintStream out) {
        this.ip = ip;
        this.sysout= (out);
        this.port = port;

        this.msgHistory = new LinkedList<>();
        this.lock = new ReentrantReadWriteLock();
    }




    @Override
    public void run() {
        sysout.println(TCP+"|STARTING TCP SERVER|");
        sysout.println(TCP+"|TCP SERVER LISTENING ON "+ColorMe.green(ip.getAddress().toString())+":"+ColorMe.green(Integer.toString(port))+"|");
        try {
            server = new ServerSocket(port,0,ip.getAddress());
            while(true){
                peer = server.accept();
                new Thread(new TCPHandler(this,new Socket(peer.getInetAddress(),peer.getPort()))).start();
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sysout.println(TCP+"|TCP SERVER ENDING|");
    }

    
}
