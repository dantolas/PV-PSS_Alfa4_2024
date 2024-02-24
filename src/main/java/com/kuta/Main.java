package com.kuta;
import java.io.IOException;
import java.net.InterfaceAddress;
import java.net.SocketException;
import java.util.Scanner;

import com.kuta.tcp.TCPServer;
import com.kuta.udp.UDPClient;
import com.kuta.udp.UDPServer;
import com.kuta.util.ErrorHandler;
import com.kuta.util.NetworkPicker;
import com.kuta.util.color.ColorMe;
import com.kuta.util.log.LogWriter;
import com.kuta.util.log.LogWriterInitException;

public class Main {
    static volatile boolean RUNNING = true;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Config config = null;
        String wd = System.getProperty("user.dir");
        System.out.println("Working directory:"+ColorMe.green(wd));
        System.out.println("Program starting...");
        if(wd == null){
            System.out.println("Couldn't acquire the working directory, the program can't run");
            in.close();
            return;
        }
        ErrorHandler handler = new ErrorHandler(System.out);

        try {
            System.out.println("Checking network interfaces...");
            NetworkPicker picker = new NetworkPicker(System.out,System.in);
            InterfaceAddress picked = null;
            while(true){

                picked = picker.pickAddress();
                if(picked != null) break;
                System.out.println("Retry now? (Y)es / (N)o");
                if(in.nextLine().startsWith("y") || in.nextLine().startsWith("y")) continue;
                return;
            }

            config = Config.fromFile(wd+"/conf/config.json");
            LogWriter.Init(config);
            TCPServer TCP = new TCPServer(RUNNING,picked, 9876,config.peerId, System.out,config.tcpClientTimeout,
                config.tcpListenerTimeout,config.tcpMsgLimit);
            UDPServer UDP = new UDPServer(RUNNING,picked,9876,System.out,config.peerId,config.broadcastFrequency,config.udpTimeout)
            .setTCP(TCP);

            new Thread(UDP).start();
            new Thread(TCP).start();
            UDPClient client = new UDPClient(picked);

            String answer= "A: {\"status\":\"ok\",\"peer_id\":\"peer\"}";
            String question = "Q: {\"command\":\"hello\",\"peer_id\":\"peer\"}";
            while(RUNNING){
                String input = in.nextLine();
                if(input.equals("a")){
                    client.sendEcho(answer);
                    continue;
                }
                if(input.equals("q")){
                    client.sendEcho(question);
                    continue;
                }
                if(input.equals("m")){
                    System.out.println("Enter the message:");
                    String message = in.nextLine();
                    TCP.sendMessage("all",message);
                }
                if(input.equals("p")){
                    TCP.printMessages();
                }
                if(input.equals("e")){
                    RUNNING = false;
                }
                //System.out.println(client.sendEcho("Random msg :]"));
            }

        }
        catch (SocketException e) {
            handler.handle(e);
        } 
        catch (IOException e) {
            handler.handle(e);
        } catch (LogWriterInitException e) {
            handler.handle(e);
        }
        finally{
            System.out.println(ColorMe.red("Program shutting down"));
            RUNNING = false;
            in.close();
        }

    }
}
