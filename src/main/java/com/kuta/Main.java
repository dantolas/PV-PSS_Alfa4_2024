package com.kuta;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Scanner;

import com.kuta.udp.UDPServer;
import com.kuta.util.log.LogWriterInitException;
import com.kuta.util.ErrorHandler;
import com.kuta.util.NetworkPicker;
import com.kuta.util.log.LogWriter;
import com.kuta.udp.UDPClient;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Config config = null;
        String wd = System.getProperty("user.dir");
        if(wd == null){
            System.out.println("Couldn't acquire the working directory, the program can't run");
            in.close();
            return;
        }
        ErrorHandler handler = new ErrorHandler(System.out);

        try {
            NetworkPicker picker = new NetworkPicker(System.out,System.in);
            InterfaceAddress picked = picker.pickAddress();
            config = Config.fromFile(wd+"/conf/config.json");
            LogWriter.Init(config);
            new Thread(new UDPServer(picked,9876,System.out,config.peerId,config.broadcastFrequency)).start();
            UDPClient client = new UDPClient();

            String answer= "A: {\"status\":\"ok\",\"peer_id\":\"molic-peer1\"}";
            String question = "Q: {\"command\":\"hello\",\"peer_id\":\"molic-peer1\"}";
            while(true){
                if(in.nextLine().equals("a")){
                    client.sendEcho(answer);
                    continue;
                }
                System.out.println(client.sendEcho(question));
            }

        }
        catch (SocketException e) {
            handler.handle(e);
        } 
        catch (IOException e) {
            try {
                String newLogId = LogWriter.writeErrorLog(e);
                System.out.println("|Error| An IO System error has occured. Please refer to log with id:"+newLogId);
            } catch (IOException e1) {
                System.out.println("===================================");
                System.out.println("Couldn't write an error log, multiple errors occured.");
                System.out.println("===================================");
                System.out.println("|ERROR 1:|"+e.getMessage());
                System.out.println("===================================");
                System.out.println("|ERROR 2:|"+e1.getMessage());
            }
        } catch (LogWriterInitException e) {
            System.out.println("|ERROR| Logwriter couldn't be initialized."+e.getMessage());
        }
        in.close();

    }
}
