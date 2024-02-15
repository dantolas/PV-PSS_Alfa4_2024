package com.kuta;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import com.kuta.UDP.UDPClient;
import com.kuta.UDP.UDPServer;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        try {
            new Thread(new UDPServer(InetAddress.getByName("localhost"),9876,System.out)).start();
            UDPClient client = new UDPClient();

            String answer= "A: {\"status\":\"ok\",\"peer_id\":\"molic-peer1\"}";
            String question = "Q: {\"command\":\"hello\",\"peer_id\":\"molic-peer1\"}";
            while(true){
                if(in.nextLine().equals("a")){
                    client.sendEcho(answer);
                    continue;
                }
                client.sendEcho(question);
            }

        }
        catch (SocketException e) {
            e.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        in.close();

    }
}
