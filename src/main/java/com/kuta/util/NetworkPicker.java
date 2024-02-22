package com.kuta.util;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import com.kuta.util.color.ColorMe;

/**
 * Used for gathering network interfaces information, and then selecting a specific
 * interface to be used with the rest of the program
 */
public class NetworkPicker {

    private PrintStream out;
    private InputStream in;
    private Scanner scanner;
    public NetworkPicker(PrintStream out, InputStream in) {
        this.out = out;
        this.in = in;
        this.scanner = new Scanner(this.in);
    }


    public InterfaceAddress pickAddress() throws SocketException{
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        ArrayList<NetworkInterface> nifList = Collections.list(interfaces);

        List<InterfaceAddress> options = new ArrayList<>();
        for (NetworkInterface ni: nifList) {
            if(ni.isLoopback()) continue;
            for(InterfaceAddress ip : ni.getInterfaceAddresses()){
                if(!(ip.getAddress() instanceof Inet4Address)) continue;
                if(ip.getBroadcast() == null) continue;
                options.add(ip);
            }
        }

        if(options.isEmpty()){
            out.println("No viable ipv4 address found. Make sure you are connected to a network.");
            return null;
        }

        int i = 1;
        for (InterfaceAddress ip: options) {
            out.println("##################");
            out.println("("+i+")");
            out.println("- "+ColorMe.green("Hostname")+":"+ip.getAddress().getHostName());
            out.println("- "+ColorMe.green("IPv4")+":"+ip.getAddress());
            out.println("- "+ColorMe.green("Broadcast IP")+":"+ip.getBroadcast());
            i++;
        }
        out.println(ColorMe.blue("Please pick the ip addr to listen to."));
        int input = readInputInt(1,options.size());
        return options.get(input-1);
    }

    private int readInputInt(int min, int max){
        int number = 0;
        while(true){
            try {
                String input = scanner.nextLine();
                if(input.toLowerCase().equals("exit")) return max+1;
                number = Integer.parseInt(input);
                if(number >= min && number <= max)break;
                out.println("Number must be within "+min+"-"+max);
                
            } catch (Exception e) {
                out.println("Please enter a number.");
            }
        }
        return number;
    }

}
