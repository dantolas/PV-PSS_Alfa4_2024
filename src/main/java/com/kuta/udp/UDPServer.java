package com.kuta.udp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.kuta.tcp.TCPClient;
import com.kuta.vendor.GsonParser;

/**
 * UDPServer
 */
public class UDPServer implements Runnable{


    private int port;
    private InterfaceAddress ip;
    private DatagramSocket socket;


    private boolean running;
    private PrintStream out;
    private byte[] buf = new byte[512];

    private final int  BROADCAST_TIMER; //Miliseconds
    private final int DEFAULT_TIMEOUT = 5000; //Miliseconds
    private final String PEER_ID;
    private final String MSG_SPLIT_REGEX = "^\\s*(\\w)\\s*[:;,-_=]*\\s*";
    public final Gson GSON; 
    private HashMap<SocketAddress,String> knownPeers;

    public UDPServer(InterfaceAddress ip,int port,PrintStream outStream,String peerId,int broadcastTimer) throws SocketException {
        knownPeers = new HashMap<>();
        this.out = outStream;
        this.port = port;
        this.ip = ip;
        this.PEER_ID = peerId;
        this.BROADCAST_TIMER = broadcastTimer;
        this.socket = new DatagramSocket(port,ip.getAddress());
        socket.setSoTimeout(DEFAULT_TIMEOUT);
        GSON = GsonParser.parser;
    }
    private void broadcastHello() throws IOException{
        DatagramPacket helloPacket = newPacket(ip.getBroadcast(),port,"Q: {\"command\":\"hello\",\"peer_id\":\""+PEER_ID+"\"}");
        socket.send(helloPacket);
    }

    private void handlePacket(DatagramPacket p) throws IOException{
        if(p.getSocketAddress().equals(socket.getLocalSocketAddress())){
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

        String content = msgRec.replaceFirst(MSG_SPLIT_REGEX,"");
        if(m.group(1).equalsIgnoreCase("q")){
            out.println("Question received");
            UDPQuestion question = GSON.fromJson(content,UDPQuestion.class);
            if(question.peerId == null || question.peerId.equals("")) return;
            out.println("Sending response to:"+question.peerId);
            p = createAnswer(p);
            socket.send(p);
            return;
        }

        if(m.group(1).equalsIgnoreCase("a")){
            out.println("Answer received");
            UDPAnswer answer = GSON.fromJson(content,UDPAnswer.class);
            if(!answer.status.equalsIgnoreCase("ok")) return;
            if(answer.peerId.length()< 1) return;

            if(knownPeers.containsKey(p.getSocketAddress())) return;
            knownPeers.put(p.getSocketAddress(),answer.peerId);
            out.println("Added peer:"+answer.peerId+"@"+p.getSocketAddress());
            TCPClient client = new TCPClient(p.getAddress(),9876,out);
            client.startConnection();
            out.println(client.sendMessage("Hello there"));
            client.stopConnection();
            socket.send(p);

        }
    }
    

    @Override
    public void run() {
        out.println("|STARTING UDP SERVER|");
        running = true;
        DatagramPacket packet = new DatagramPacket(buf,0,buf.length);
        out.println("|UDP SERVER RUNNING ON "+ip.getAddress()+":"+port+"|");
        long lastBSTime = System.currentTimeMillis();
        out.println("|SENDING FIRST BROADCAST TO "+ip.getBroadcast()+":"+port+"|");
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
                    continue;
                } 
            } 
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }



        out.println("|UDP SERVER CLOSING|");
        
        socket.close();
    }

    private DatagramPacket newPacket(InetAddress ip, int port,String msg){
        return new DatagramPacket(msg.getBytes(), msg.getBytes().length,ip,port);
    };

    private DatagramPacket createAnswer(DatagramPacket question){
        String answerMsg = "A: {\"status\":\"ok\",\"peer_id\":\""+PEER_ID+"\"}";
        return new DatagramPacket(answerMsg.getBytes(),answerMsg.getBytes().length,question.getSocketAddress());
    }

    private class UDPAnswer{
        @SerializedName("status")
        public String status;
        @SerializedName("peer_id")
        public String peerId;
        @Override
        public String toString() {
            return GSON.toJson(this.getClass());
        }
        public UDPAnswer() {
        }
        public UDPAnswer(String status, String peerId) {
            this.status = status;
            this.peerId = peerId;
        }



       
    }

    private class UDPQuestion{
        @SerializedName("command")
        public String command;
        @SerializedName("peer_id")
        public String peerId;
        @Override
        public String toString() {
            return GSON.toJson(this.getClass());
        }
        public UDPQuestion(String command, String peerId) {
            this.command = command;
            this.peerId = peerId;
        }
        public UDPQuestion() {
        }


    }
    
}
