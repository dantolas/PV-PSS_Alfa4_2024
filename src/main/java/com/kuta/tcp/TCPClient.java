package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import com.kuta.util.color.ColorMe;

/**
 * TCPClient
 */
public class TCPClient implements Runnable{
    private Socket client;
    private String serverPeerId;
    private PrintStream sysout;
    private PrintWriter out;
    private Scanner in;

    private final String TCPc;

    private InetAddress ip;
    private int port;

    public TCPClient(InetAddress ip, int port,String serverPeerId, PrintStream sysout) {
        this.sysout = (sysout);
        this.ip = ip;
        this.port = port;
        this.serverPeerId = serverPeerId;
        this.TCPc = ColorMe.yellow("TCPc-"+serverPeerId);
    }

    public void setup() {
        sysout.println(TCPc+"|Attempting connection to "+ColorMe.green(serverPeerId)+ip+":"+port);
        try {
            client = new Socket(ip, port);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new Scanner(client.getInputStream());
            sysout.println(TCPc+"|Connection established");
            sysout.flush();
        } catch (Exception e) {
            sysout.println(TCPc+"|Error here 1");
            e.printStackTrace();
        }
    }

    public String sendMessage(String msg) {
        if(out == null) return "";
        out.println(msg);
        String resp = in.nextLine();
        return resp;
    }

    public void stopConnection() {
        sysout.println(TCPc+"|Closing tcp connection");
        in.close();
        out.close();
        try {
            client.close();
        } catch (IOException e) {
            sysout.println(TCPc+"|Error here 2");
            e.printStackTrace();
        }
        sysout.println(TCPc+"|Connection closed");
    }

    @Override
    public void run() {
        setup();
        stopConnection();
    }
    
}
