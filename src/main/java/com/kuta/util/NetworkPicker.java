package com.kuta.util;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.kuta.Config;
import com.kuta.util.color.ColorMe;

import jakarta.annotation.PostConstruct;

/**
 * Used for gathering network interfaces information, and then selecting a specific
 * interface to be used with the rest of the program
 */
@Service
public class NetworkPicker {

    private PrintStream out;
    private InputStream in;
    private Scanner scanner;

    @Autowired
    public NetworkPicker(Config config) throws SocketException, UnknownHostException {
        this.out = System.out;
        this.in = System.in;
        this.scanner = new Scanner(this.in);
        if(config.ip.equalsIgnoreCase("auto")){
            this.ipPicked = pickAddressAuto();
            return;
        }
        if(config.ip.equalsIgnoreCase("manual")){
            this.ipPicked = pickAddress();
            return;
        }

        this.ipPicked = pickAddress(config.ip);

        

    }

    private InterfaceAddress ipPicked;


    @Bean
    public InterfaceAddress ip(NetworkPicker picker) throws SocketException{
        return this.ipPicked;
    }


    /**
     * Tries to match the given ip string to an ip currently assigned on one of the device network
     * interfaces. It attempts to parse the ipString to an InetAddress, and then compares it.
     * @param ipString String representation of an IPv4 addr
     * @return InterfaceAddress if it finds one, null if it doesn't
     * @throws SocketException
     * @throws UnknownHostException
     */
    public InterfaceAddress pickAddress(String ipString) throws SocketException, UnknownHostException{
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        ArrayList<NetworkInterface> nifList = Collections.list(interfaces);

        out.println(ColorMe.blue("|Automatically choosing network address|"));
        List<InterfaceAddress> options = new ArrayList<>();
        for (NetworkInterface ni: nifList) {
            if(ni.isLoopback()) continue;
            for(InterfaceAddress ip : ni.getInterfaceAddresses()){
                if(!(ip.getAddress() instanceof Inet4Address)) continue;
                if(ip.getBroadcast() == null) continue;
                if(!ip.getAddress().equals(InetAddress.getByName(ipString))) continue;
                out.println(ColorMe.blue("|Address Picked|)"));
                out.println("- "+ColorMe.green("Hostname")+":"+ip.getAddress().getHostName());
                out.println("- "+ColorMe.green("IPv4")+":"+ip.getAddress());
                out.println("- "+ColorMe.green("Broadcast IP")+":"+ip.getBroadcast());
                this.ipPicked = ip;
                return ip;
            }
        }

        if(options.isEmpty()){
            out.println("No viable ipv4 address found. Make sure you are connected to a network.");
        }
        return null;
    }
    /**
     * Automatically grabs the first IPv4 ip it finds on any of the network interfaces.
     * @return InterfaceAddress 
     * @throws SocketException
     */
    public InterfaceAddress pickAddressAuto() throws SocketException{
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        ArrayList<NetworkInterface> nifList = Collections.list(interfaces);

        out.println(ColorMe.blue("|Automatically choosing network address|"));
        List<InterfaceAddress> options = new ArrayList<>();
        for (NetworkInterface ni: nifList) {
            if(ni.isLoopback()) continue;
            for(InterfaceAddress ip : ni.getInterfaceAddresses()){
                if(!(ip.getAddress() instanceof Inet4Address)) continue;
                if(ip.getBroadcast() == null) continue;
                out.println("##################");
                out.println(ColorMe.blue("|Address Picked|)"));
                out.println("- "+ColorMe.green("Hostname")+":"+ip.getAddress().getHostName());
                out.println("- "+ColorMe.green("IPv4")+":"+ip.getAddress());
                out.println("- "+ColorMe.green("Broadcast IP")+":"+ip.getBroadcast());
                this.ipPicked = ip;
                return ip;
            }
        }

        if(options.isEmpty()){
            out.println("No viable ipv4 address found. Make sure you are connected to a network.");
        }
        return null;
    }

    /**
     * Used for more thorough manual selection of the used IP address.
     * Checks all interfaces and lets the user pick by reading user input
     * @return Selected InterfaceAddress
     * @throws SocketException
     */
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
