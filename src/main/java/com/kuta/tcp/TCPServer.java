package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InterfaceAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.Scanner;

import com.kuta.util.color.ColorMe;

/**
 * TCPSocket
 */
public class TCPServer implements Runnable{

    private InterfaceAddress ip;
    private int port;
    private ServerSocket server;
    private Socket peer;

    private PrintStream sysout;
    private PrintWriter out;
    private Scanner in;

    private final String TCP = ColorMe.yellow("TCP");
    private Queue<String> msgHistory;

    public TCPServer(InterfaceAddress ip, int port, PrintStream out) {
        this.ip = ip;
        this.sysout= (out);
        this.port = port;
    }




    @Override
    public void run() {
        sysout.println(TCP+"|STARTING TCP SERVER|");
        sysout.println(TCP+"|TCP SERVER LISTENING ON "+ColorMe.green(ip.getAddress().toString())+":"+ColorMe.green(Integer.toString(port))+"|");
        try {
            server = new ServerSocket(port,0,ip.getAddress());
            while(true){
                peer = server.accept();
                sysout.println(TCP+peer.getLocalSocketAddress());
                sysout.println(TCP+" Accepted connection from:"+peer.getInetAddress()+":"+peer.getLocalPort());
                out = new PrintWriter(peer.getOutputStream(),true);
                in = new Scanner(peer.getInputStream());
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sysout.println(TCP+"|TCP SERVER ENDING|");
    }

    
}
