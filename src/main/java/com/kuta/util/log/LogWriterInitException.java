package com.kuta.util.log;

/**
 * LogWriterInitException
 * Used for specific error handling if LogWriter.Init() method fails
 */
public class LogWriterInitException extends Exception{

    public LogWriterInitException(String msg){
        super(msg);
    }
}
