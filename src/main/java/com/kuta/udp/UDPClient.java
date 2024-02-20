package com.kuta.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * UDPClient
 */
public class UDPClient {

    private DatagramSocket socket;
    private InterfaceAddress address;

    private byte[] buf = new byte[512];

    public UDPClient(InterfaceAddress addr) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = addr;
    }

    public String sendEcho(String msg) throws IOException {
        buf = msg.getBytes();
        DatagramPacket packet 
        = new DatagramPacket(buf, buf.length, address.getAddress(), 9876);
        socket.send(packet);
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(
            packet.getData(), 0, packet.getLength());
        return received;
    }

    public void close() {
        socket.close();
    }
}
