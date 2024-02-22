package com.kuta.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InterfaceAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Mainly used for testing purposed, 
 * can be used to send UDP packets to specified UDP socket listening
 */
public class UDPClient {

    private DatagramSocket socket;
    private InterfaceAddress address;

    private byte[] buf = new byte[1024];

    public UDPClient(InterfaceAddress addr) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = addr;
        socket.setSoTimeout(4000);
    }

    public String sendEcho(String msg){
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address.getAddress(), 9876);
        try {
            socket.send(packet);
        }
        catch(SocketTimeoutException e){

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
        } catch (Exception e) {
        }
        String received = new String(
            packet.getData(), 0, packet.getLength());
        return received;
    }

    public void close() {
        socket.close();
    }
}
