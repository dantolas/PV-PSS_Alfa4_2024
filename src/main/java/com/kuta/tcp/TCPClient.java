package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import com.kuta.util.color.ColorMe;
import com.kuta.vendor.GsonParser;

/**
 * TCPClient
 */
public class TCPClient implements Runnable{
    private Socket client;
    private String serverPeerId;
    private boolean running;
    private int timeout;
    private PrintStream sysout;
    private PrintWriter out;
    private Scanner in;

    private final String HELLO;

    private final String TCPc;

    private InetAddress ip;
    private int port;

    public TCPClient(InetAddress ip, int port,String serverPeerId,String myPeerId, PrintStream sysout) {
        this.sysout = (sysout);
        this.ip = ip;
        this.port = port;
        this.serverPeerId = serverPeerId;
        this.TCPc = ColorMe.yellow("TCPc-"+serverPeerId);
        this.HELLO = GsonParser.parser.toJson(new TCPHello("hello",myPeerId));
    }

    public void setup() {
        sysout.println(TCPc+"|Attempting connection to "+ColorMe.green(serverPeerId)+ip+":"+port);
        try {
            client = new Socket(ip, port);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new Scanner(client.getInputStream());
            sysout.println(TCPc+"|Connection established");
            sysout.flush();
            running = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void tearDown() {
        sysout.println(TCPc+"|Closing tcp connection");
        in.close();
        out.close();
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sysout.println(TCPc+"|Connection closed");
    }
    public String newMessage(String msg){
        return GsonParser.parser.toJson(new TCPNewMessage("new_message", Long.toString(System.currentTimeMillis())
            ,msg));
       
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
            resp = in.nextLine();
        }
        return resp;
    }
    public String send(String txt) throws TimeoutException {
        if(out == null) return "";
        out.println(txt);
        String resp = readResponse();
        return resp;
    }
    @Override
    public void run() {
        setup();
        try {
            sysout.println(TCPc+"|Sending this msg to server:"+HELLO);
            sysout.println(TCPc+"|"+send(HELLO));
            while(true){
                Thread.sleep(1000);
                sysout.println(TCPc+"|Sending msg");
                String response = send(newMessage("Hello there"));
                sysout.println(TCPc+"|"+response);
            }
        } catch (TimeoutException e) {
            sysout.println(TCPc+"|Connection timed out");
        } catch (Exception e){
            e.printStackTrace();
        }
        finally{
            tearDown();
        }
        
    }
    
}
