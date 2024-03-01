package com.kuta;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

@SpringBootApplication
@Service
public class Main {

    public static void main(String[] args) {
        System.out.println("User directory:"+System.getProperty("user.dir"));
        SpringApplication.run(Main.class,args);
    }
}
