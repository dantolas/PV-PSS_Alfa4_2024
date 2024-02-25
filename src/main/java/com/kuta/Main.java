package com.kuta;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    static volatile boolean RUNNING = true;

    public static void main(String[] args) {
        SpringApplication.run(Main.class,args);

    }
}
