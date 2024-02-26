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
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.kuta.Config;
import com.kuta.tcp.TCPServer;
import com.kuta.util.color.ColorMe;
import com.kuta.vendor.GsonParser;

/**
 * UDP Server running on specified network socket.
 * Responsible for broadcasting information, and handling UDP responses from other machines.
 *
 * Implements Runnable interface to be run as a Thread
 * To run the server either create a new Thread and start it or just call the run() method
 */
@Service
public class UDPServer implements Runnable{


    private int port;
    private InterfaceAddress ip;
    private InetAddress broadcastAddr;
    private DatagramSocket socket;
    private TCPServer TCP;

    private boolean running;
    private PrintStream out;
    private byte[] buf = new byte[512];
    private final String UDP = ColorMe.purple("UDP");

    private int broadcastTimer; //Miliseconds
    private int defaultTimeout; //Miliseconds
    private String peerId;
    private final String MSG_SPLIT_REGEX = "^\\s*(\\w)\\s*[:;,-_=]*\\s*";
    public final Gson GSON; 
    public static HashMap<SocketAddress,String> knownPeers;
    public static ReadWriteLock lock;

    /**
     * Main constructor for creating the UDPServer
     *
     * @param running Indicates whether the server is should be running
     * @param ip IPv4 to bind to
     * @param port Network port to bind to
     * @param outStream Output stream for printing information
     * @param peerId Server identification
     * @param broadcastTimer Milisecond interval to send UDP broadcasts
     * @param defaultTimeout Default timeout for sending and receiving packets
     * @throws SocketException
     */
    @Autowired
    public UDPServer(Config config, InterfaceAddress ip, int port, boolean running,TCPServer tcpServer) throws SocketException {
        UDPServer.knownPeers = new HashMap<>();
        UDPServer.lock = new ReentrantReadWriteLock();
        this.out = System.out;
        GSON = GsonParser.parser;

        this.ip = ip;
        this.broadcastAddr = ip.getAddress();
        this.port = port;
        this.peerId = config.peerId;
        this.broadcastTimer = config.broadcastFrequency;
        this.defaultTimeout = config.udpTimeout;
        this.socket = new DatagramSocket(port,ip.getAddress());
        this.TCP = tcpServer;
    }

    public UDPServer setIp(InterfaceAddress ip){
        this.ip = ip;
        return this;
    }
    public UDPServer setPort(int port){
        this.port = port;
        return this;
    }
    public UDPServer setPeerId(String peerId){
        this.peerId= peerId;
        return this;
    }
    public UDPServer setBroadcastTimer(int timer){
        this.broadcastTimer = timer;
        return this;
    }
    public UDPServer setDefaultTimeout(int timeout){
        this.defaultTimeout = timeout;
        return this;
    }
    public UDPServer setSocket() throws SocketException{
        this.socket = new DatagramSocket(this.port,this.ip.getAddress());
        return this;
    }

    public UDPServer setTCP(TCPServer TCP){
        this.TCP = TCP;
        return this;
    }

    /**
     * Broadcasts a hello message to the network broadcast address
     * @throws IOException If socket gets interrupted
     */
    private void broadcastHello() throws IOException{
        DatagramPacket helloPacket = newPacket(ip.getBroadcast(),port,"{\"command\":\"hello\",\"peer_id\":\""+peerId+"\"}");
        socket.send(helloPacket);
    }

    /**
     * Handles a packet containing a hello message command
     * @param question UDPQuestion object created from the inner message in packet
     * @param p The packet received
     * @throws IOException If socket gets interrupted
     */
    private void handleQuestion(UDPQuestion question,DatagramPacket p) throws IOException{
        out.print(UDP+"|Question received");
        out.println("|Sending response to:"+ColorMe.green(question.peerId));
        out.println();
        p = createAnswer(p);
        socket.send(p);
        return;
    }

    /**
     * Handles a packet containing an ok response command
     * @param answer UDPAnswer object created from the inner message in packet
     * @param p The packet received
     * @throws IOException If socket gets interrupted
     */
    private void handleAnswer(UDPAnswer answer, DatagramPacket p) throws IOException{
        out.print(UDP+"|Answer received");
        if(!answer.status.equalsIgnoreCase("ok")) return;
        UDPServer.lock.readLock().lock();
        if(UDPServer.knownPeers.containsKey(p.getSocketAddress())){
            out.println("|known "+ColorMe.green(knownPeers.get(p.getSocketAddress()))+" ");
            UDPServer.lock.readLock().unlock();
            return;
        }
        UDPServer.lock.readLock().unlock();


        UDPServer.lock.writeLock().lock();
        knownPeers.put(p.getSocketAddress(),answer.peerId);
        UDPServer.lock.writeLock().unlock();
        out.println("|Added peer:"+ColorMe.green(answer.peerId)+"@"+ColorMe.green(p.getSocketAddress().toString()));
        p = newPacket(p.getAddress(),p.getPort(),UDP+"|Answer fine,will attempt TCP conn to "+ColorMe.green(answer.peerId));
        socket.send(p);
        TCP.connectClient(p.getAddress(),9876,answer.peerId);
        return;
    }

    /**
     * Handles every packet received by the server
     * First the inner message inside the packet is extracted
     * Second it tries to deserialize the inner message to UDPQuestion object to see if it's a 
     * hello command
     * Third it tries the same thing with the UDPAnswer object to see if it's a response
     * If none match, a smiley is sent back
     * @param p DatagramPacket received by the server
     * @throws IOException If socket gets interrupted
     */
    private void handlePacket(DatagramPacket p) throws IOException{
        if(p.getSocketAddress().equals(socket.getLocalSocketAddress())){
            return;
        }

        String msgRec = new String(p.getData(), 0, p.getLength());

        String content = msgRec.replaceFirst(MSG_SPLIT_REGEX,"");

        try {
            UDPQuestion question = GSON.fromJson(content,UDPQuestion.class);
            if(question.isValid()) { handleQuestion(question,p); return; }
        } catch (Exception e) {
        }
        try {
            UDPAnswer answer= GSON.fromJson(content,UDPAnswer.class);
            if(answer.isValid()) {handleAnswer(answer,p); return;}
        } catch (Exception e) {
        }
        p = newPacket(p.getAddress(),p.getPort(),":]");
        socket.send(p);
        return;
    }

    /**
     * Creates a new packet ready to be sent from input parameters
     * @param ip IPv4 to send to
     * @param port Network port to send to
     * @param msg Message to be sent
     * @return DatagramPacket ready to be sent
     */
    private DatagramPacket newPacket(InetAddress ip, int port,String msg){
        return new DatagramPacket(msg.getBytes(), msg.getBytes().length,ip,port);
    };

    /**
     * Creates a new packet containing the answer in response to a hello command
     * @param question Packet containing the hello command
     * @return DatagramPacket ready to be sent
     */
    private DatagramPacket createAnswer(DatagramPacket question){
        String answerMsg = "{\"status\":\"ok\",\"peer_id\":\""+this.peerId+"\"}";
        return new DatagramPacket(answerMsg.getBytes(),answerMsg.getBytes().length,question.getSocketAddress());
    }

    /**
     * Checks the broadcastAddress for specific conditions that have to be met
     * @throws UnknownHostException 
     */
    public void checkBroadcast() throws UnknownHostException{
        if(broadcastAddr.equals(InetAddress.getByName("0.0.0.0"))) {
            broadcastAddr = InetAddress.getByName("172.16.255.255");
        }
    }

    /**
     * Relases all resources and shuts down the server
     */
    public void tearDown(){
        this.running = false;
        out.println(UDP+"|UDP SERVER TEARING DOWN|");
        socket.close();
    }

    /**
     * Should be used on server startup, sets up necessary parts to run the server
     * @throws SocketException If socket is interrupted
     */
    public void setup() throws SocketException{

        out.println(UDP+"|STARTING UDP SERVER|");
        running = true;
        out.println(UDP+"|UDP SERVER RUNNING ON "+ColorMe.green(ip.getAddress().toString())+":"+ColorMe.green(Integer.toString(port))+"|");
        try {
            checkBroadcast();
        } catch (UnknownHostException e) {
            broadcastAddr = ip.getBroadcast();
        }
        socket.setSoTimeout(defaultTimeout);
    }

    @Override
    public void run() {
        try {
            setup();
        } catch (SocketException e) {
            out.println(UDP+ColorMe.red("|ERROR|")+"|UDP SETUP FAILED|");
            tearDown();
            return;
        }

        DatagramPacket packet = new DatagramPacket(buf,0,buf.length);
        long lastBSTime = System.currentTimeMillis();
        out.println(UDP+"|SENDING FIRST BROADCAST TO "+ColorMe.green(ip.getBroadcast()+":"+Integer.toString(port)));
        try {
            
            broadcastHello();

            while (running) {
                long timeSinceBS = (System.currentTimeMillis()-lastBSTime);
                if(timeSinceBS >= broadcastTimer){
                    lastBSTime = System.currentTimeMillis();
                    out.println(ColorMe.purple("=============="));
                    out.println(UDP+"|SENDING BROADCAST|");
                    out.println(ColorMe.purple("=============="));
                    broadcastHello();
                }
                try {
                    int timeout = Math.abs((int)(broadcastTimer - timeSinceBS));
                    if(timeout <= 0) timeout = 1;
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
