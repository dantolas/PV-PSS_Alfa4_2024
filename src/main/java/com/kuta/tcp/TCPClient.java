package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * TCPClient
 */
public class TCPClient {
    private Socket client;
    private PrintStream sysout;
    private PrintWriter out;
    private Scanner in;

    private InetAddress ip;
    private int port;

    public TCPClient(InetAddress ip, int port, PrintStream sysout) {
        this.sysout = (sysout);
        this.ip = ip;
        this.port = port;
    }

    public void startConnection() {
        sysout.println("Attempting connection to:"+ip+":"+port);
        try {
            client = new Socket(ip, port);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new Scanner(client.getInputStream());
            sysout.println("Connection established");
            sysout.println(in.nextLine());
        } catch (Exception e) {
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
        in.close();
        out.close();
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
