package com.kuta.tcp;

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
    private TCPConnection connection;


    public TCPClient(TCPConnection connection){
        this.sysout = connection.sysout;
        this.timeout = connection.timeout;
        this.ip = connection.ip;
        this.port = connection.port;
        this.endpointPeerId = connection.endpointPeerId;
        this.TCPc = ColorMe.yellow("TCPc-"+endpointPeerId);
        this.HELLO = GsonParser.parser.toJson(new HelloTCP("hello",connection.serverPeerId));
        this.lock = connection.lock;
        this.connection = connection;
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
            sysout.println(TCPc+"|Couldn't establish connection with "
                +ColorMe.green(endpointPeerId)+ip+":"+port);
        }
    }


    public void tearDown() {
        sysout.println(TCPc+"|Closing tcp connection");
        try {
            in.close();
            out.close();
            client.close();
        } catch (Exception e) {
        } 
        sysout.println(TCPc+"|Connection closed");
        connection.end();
    }
    public NewTCPMessage newMessage(String msg){
        return new NewTCPMessage("new_message", Long.toString(System.currentTimeMillis()),msg);
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
                    sysout.println(TCPc+"|Syncinc message histories");
                    connection.historyLocks.writeLock().lock();
                    if(connection.msgHistory.size() >=100) connection.msgHistory.pollFirstEntry();
                    TCPServer.syncMessages(connection.msgHistory,answer.messages);
                    connection.historyLocks.writeLock().unlock();
                }
            } catch (Exception e) {
            }
            while(running){
                synchronized(lock){
                    lock.wait();
                    NewTCPMessage msgObj = newMessage(lock.msg);
                    sysout.println(TCPc+"|Sending msg:"+lock.msg);
                    response = send(GsonParser.parser.toJson(msgObj));
                    try {
                        AnswerTCP answer = GsonParser.parser.fromJson(response,AnswerTCP.class);
                        if(answer.status.equalsIgnoreCase("ok")){
                            connection.historyLocks.writeLock().lock();
                            connection.msgHistory.
                                put(msgObj.msgId,new Message(connection.serverPeerId,msgObj.msg));
                            connection.historyLocks.writeLock().unlock();
                        }
                    } catch (Exception e) {
                    }
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
