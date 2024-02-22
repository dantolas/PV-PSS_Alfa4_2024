package com.kuta.tcp;

/**
 * Exception used for specific error handling of TCP sockets timing out
 */
public class TimeoutException extends Exception{

    public TimeoutException(String msg){
        super(msg);
    }
    
}
