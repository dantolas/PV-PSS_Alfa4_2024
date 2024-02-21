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
import com.kuta.tcp.TCPClient;
import com.kuta.util.color.ColorMe;
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
    private final String UDP = ColorMe.purple("UDP");

    private final int  BROADCAST_TIMER; //Miliseconds
    private final int DEFAULT_TIMEOUT; //Miliseconds
    private final String PEER_ID;
    private final String MSG_SPLIT_REGEX = "^\\s*(\\w)\\s*[:;,-_=]*\\s*";
    private final Pattern pattern = Pattern.compile(MSG_SPLIT_REGEX);
    public final Gson GSON; 
    private HashMap<SocketAddress,String> knownPeers;

    public UDPServer(boolean running,InterfaceAddress ip,int port,PrintStream outStream,String peerId,int broadcastTimer,int defaultTimeout) throws SocketException {
        knownPeers = new HashMap<>();
        this.out = outStream;
        this.port = port;
        this.ip = ip;
        this.PEER_ID = peerId;
        this.BROADCAST_TIMER = broadcastTimer;
        this.DEFAULT_TIMEOUT = defaultTimeout;
        this.socket = new DatagramSocket(port,ip.getAddress());
        GSON = GsonParser.parser;
    }

    private void broadcastHello() throws IOException{
        DatagramPacket helloPacket = newPacket(ip.getBroadcast(),port,"{\"command\":\"hello\",\"peer_id\":\""+PEER_ID+"\"}");
        socket.send(helloPacket);
    }

    private void handlePacket(DatagramPacket p) throws IOException{
        if(p.getSocketAddress().equals(socket.getLocalSocketAddress())){
            return;
        }

        String msgRec = new String(p.getData(), 0, p.getLength());

        String response = "|Received this message:"+ColorMe.green(msgRec);
        out.println(UDP+response);

        Matcher m = pattern.matcher(msgRec);

        String content = msgRec.replaceFirst(MSG_SPLIT_REGEX,"");

        try {
            UDPQuestion question = GSON.fromJson(content,UDPQuestion.class);
            out.println(UDP+"Question valid:"+question.isValid());
        } catch (Exception e) {
            out.println(UDP+"Failed parsing question.");
        }
        try {
            UDPAnswer answer= GSON.fromJson(content,UDPAnswer.class);
            out.println(UDP+"Answer valid:"+answer.isValid());
        } catch (Exception e) {
            out.println(UDP+"Failed parsing answer.");
        }

        if(m.group(1).equalsIgnoreCase("q")){
            out.println(UDP+"|Question received");
            UDPQuestion question = GSON.fromJson(content,UDPQuestion.class);
            if(question.peerId == null || question.peerId.equals("")) return;
            out.println(UDP+"|Sending response to:"+question.peerId);
            p = createAnswer(p);
            socket.send(p);
            return;
        }

        if(m.group(1).equalsIgnoreCase("a")){
            out.println(UDP+"|Answer received");
            UDPAnswer answer = GSON.fromJson(content,UDPAnswer.class);
            if(!answer.status.equalsIgnoreCase("ok")) return;
            if(answer.peerId.length()< 1) return;

            if(knownPeers.containsKey(p.getSocketAddress())) return;
            knownPeers.put(p.getSocketAddress(),answer.peerId);
            out.println(UDP+"|Added peer:"+ColorMe.green(answer.peerId)+"@"+ColorMe.green(p.getSocketAddress().toString()));
            p = newPacket(p.getAddress(),p.getPort(),UDP+"|Answer processed, will attempt TCP conn to "+answer.peerId);
            socket.send(p);
            TCPClient client = new TCPClient(p.getAddress(),9876,out);
            client.startConnection();
            //client.stopConnection();
        }
    }
    
    private DatagramPacket newPacket(InetAddress ip, int port,String msg){
        return new DatagramPacket(msg.getBytes(), msg.getBytes().length,ip,port);
    };

    private DatagramPacket createAnswer(DatagramPacket question){
        String answerMsg = "{\"status\":\"ok\",\"peer_id\":\""+PEER_ID+"\"}";
        return new DatagramPacket(answerMsg.getBytes(),answerMsg.getBytes().length,question.getSocketAddress());
    }

    public void tearDown(){
        this.running = false;
        out.println(UDP+"|UDP SERVER TEARING DOWN|");
        socket.close();
    }

    public void setup() throws SocketException{

        out.println(UDP+"|STARTING UDP SERVER|");
        running = true;
        out.println(UDP+"|UDP SERVER RUNNING ON "+ColorMe.green(ip.getAddress().toString())+":"+ColorMe.green(Integer.toString(port))+"|");
        socket.setSoTimeout(DEFAULT_TIMEOUT);
    }

    @Override
    public void run() {
        try {
            setup();
        } catch (SocketException e) {
            out.println(UDP+"|UDP SETUP FAILED, SERVER TEARING DOWN|");
            tearDown();
            return;
        }

        DatagramPacket packet = new DatagramPacket(buf,0,buf.length);
        long lastBSTime = System.currentTimeMillis();
        out.println(UDP+"|SENDING FIRST BROADCAST TO "+ip.getBroadcast()+":"+port+"|");
        try {
            
            broadcastHello();

            while (running) {
                long timeSinceBS = (System.currentTimeMillis()-lastBSTime);
                if(timeSinceBS >= BROADCAST_TIMER){
                    lastBSTime = System.currentTimeMillis();
                    out.println(UDP+"|SENDING BROADCAST|");
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
            out.println(UDP+"|Couldn't send broadcast.|");
            e.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }finally{
            tearDown();
        }
    }

    


}
