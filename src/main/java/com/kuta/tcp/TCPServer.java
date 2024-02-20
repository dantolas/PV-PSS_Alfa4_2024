package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InterfaceAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

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

    


    public TCPServer(InterfaceAddress ip, int port, PrintStream out) {
        this.ip = ip;
        this.sysout= (out);
        this.port = port;
    }




    @Override
    public void run() {
        sysout.println("|STARTING TCP SERVER|");
        try {
            sysout.println("|TCP SERVER LISTENING ON "+ip.getAddress()+":"+port+"|");
            server = new ServerSocket(port,0,ip.getAddress());
            peer = server.accept();
            sysout.println("Accepted connection from:"+peer.getInetAddress()+":"+peer.getPort());
            out = new PrintWriter(peer.getOutputStream(),true);
            in = new Scanner(peer.getInputStream());
            out.println("Hello there :]");
            String msg = in.nextLine();
            out.println("U have sent:"+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}
