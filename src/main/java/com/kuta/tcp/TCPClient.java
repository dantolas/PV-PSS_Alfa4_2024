package com.kuta.tcp;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import com.kuta.tcp.TCPConnection.MsgLock;
import com.kuta.util.color.ColorMe;
import com.kuta.vendor.GsonParser;

/**
 * TCPClient
 */
public class TCPClient implements Runnable{
    public boolean running;

    private Socket client;
    private String endpointPeerId;
    private int timeout;
    private PrintStream sysout;
    private PrintWriter out;
    private Scanner in;

    private final String HELLO;

    private final String TCPc;

    private InetAddress ip;
    private int port;
    private MsgLock lock;

    /**
     * Main constructor for instantiating a TCPClient. 
     *
     * @param ip IPv4 to connect to
     * @param port Port number to connect to
     * @param endpointPeerId Peer id of the endpoint connection
     * @param myPeerId Peer id of the server
     * @param sysout System.out for printing info
     */
    public TCPClient(InetAddress ip, int port,int timeout,String endpointPeerId,String myPeerId, PrintStream sysout, String msg,MsgLock lock) {
        this.sysout = (sysout);
        this.timeout = timeout;
        this.ip = ip;
        this.port = port;
        this.endpointPeerId = endpointPeerId;
        this.TCPc = ColorMe.yellow("TCPc-"+endpointPeerId);
        this.HELLO = GsonParser.parser.toJson(new HelloTCP("hello",myPeerId));
        this.lock = lock;
    }

    public void setup() {
        sysout.println(TCPc+"|Attempting connection to "+ColorMe.green(endpointPeerId)+ip+":"+port);
        try {
            client = new Socket(ip, port);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new Scanner(client.getInputStream());
            sysout.println(TCPc+"|Connection established");
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
        return GsonParser.parser.toJson(new NewTCPMessage("new_message", Long.toString(System.currentTimeMillis())
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
            String response = "";
            try {
                response = send(HELLO);
                AnswerTCP answer = GsonParser.parser.fromJson(response,AnswerTCP.class);
                if(answer.isValid()){
                    sysout.println(TCPc+"Was valid");
                }
                sysout.println(TCPc+"Was not valid");
            } catch (Exception e) {
                sysout.println(TCPc+"Was not valid exc");
            }
            while(running){
                synchronized(lock){
                    lock.wait();
                    String msg = lock.msg;
                    sysout.println(TCPc+"|Sending msg:"+msg);
                    response = send(newMessage(msg));
                    sysout.println(TCPc+"|FULL RESPONSE"+response);
                    sysout.println(TCPc+"| Response:"+response);
                }
                Thread.sleep(4000);
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
