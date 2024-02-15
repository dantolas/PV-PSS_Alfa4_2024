package com.kuta.UDP;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UDPServer
 */
public class UDPServer implements Runnable{


    private int port;
    private InetAddress ip;
    private DatagramSocket socket;
    private SocketAddress sockAddr;


    private boolean running;
    private PrintStream out;
    private byte[] buf = new byte[512];

    private final int BROADCAST_TIMER = 5000; //Miliseconds
    private final String PEER_ID = "kuta";
    private final String MSG_SPLIT_REGEX = "^\\s*(\\w)\\s*[:;,-_=]*\\s*";
    private List<InetAddress> knownPeers;

    public UDPServer(InetAddress ip,int port,PrintStream outStream) throws SocketException {
        knownPeers = new ArrayList<>();
        this.out = outStream;
        this.port = port;
        this.ip = ip;
        this.socket = new DatagramSocket(port,ip);
        socket.setSoTimeout(BROADCAST_TIMER);
    }
    private void broadcastHello() throws IOException{
        DatagramPacket helloPacket = newPacket(ip,port,"Q: {\"command\":\"hello\",\"peer_id\":\""+PEER_ID+"\"}");
        socket.send(helloPacket);
    }

    private void handlePacket(DatagramPacket p){
        if(p.getSocketAddress().equals(socket.getLocalSocketAddress())){
            out.println("Skipping own message");
            return;
        }
        String msgRec = new String(p.getData(), 0, p.getLength());

        int resPort = p.getPort();
        InetAddress resAddr = p.getAddress();

        if (msgRec.equals("end-kuta")) {
            try {
                p = newPacket(resAddr,resPort,"Server will now shut down");
                socket.send(p);
                running = false;
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String response = "UDP Server received this message:"+msgRec;
        out.println(response);

        Matcher m = Pattern.compile(MSG_SPLIT_REGEX).matcher(msgRec);
        if(!m.find()){out.println("Matcher didn't match");return;};
        if(m.group(1).equalsIgnoreCase("a")) out.println("Answer received");
        if(m.group(1).equalsIgnoreCase("q")) out.println("Question received");
        p = newPacket(resAddr,resPort,response);

        try {
            socket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    @Override
    public void run() {
        out.println("|UDP SERVER STARTED|");
        running = true;
        out.println("|UDP SERVER RUNNING ON PORT:"+port+"|");
        DatagramPacket packet = new DatagramPacket(buf, buf.length);


        long lastBSTime = System.currentTimeMillis();
        out.println("|SENDING FIRST BROADCAST|");
        try {
            
            broadcastHello();

            while (running) {
                long timeSinceBS = (System.currentTimeMillis()-lastBSTime);
                if(timeSinceBS >= BROADCAST_TIMER){
                    lastBSTime = System.currentTimeMillis();
                    out.println("|SENDING BROADCAST|");
                    broadcastHello();
                }
                try {
                    int timeout = Math.abs((int)(BROADCAST_TIMER - timeSinceBS));
                    socket.setSoTimeout(timeout);
                    socket.receive(packet);
                    handlePacket(packet);

                }catch (SocketTimeoutException e){
                    out.println("No msg received.");
                    continue;
                } 
            } 
        } catch (IOException e) {
            e.printStackTrace();
        }


        out.println("|UDP SERVER CLOSING|");
        
        socket.close();
    }

    private DatagramPacket newPacket(InetAddress ip, int port,String msg){
        return new DatagramPacket(msg.getBytes(), msg.getBytes().length,ip,port);
    };

    
}
