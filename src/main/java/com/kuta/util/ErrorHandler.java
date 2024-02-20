package com.kuta.util;

import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketException;

import com.kuta.util.log.LogWriter;
import com.kuta.util.log.LogWriterInitException;

/**
 * errorHandler
 */
public class ErrorHandler {

    private PrintStream out;

    public ErrorHandler(PrintStream outputStream){
        this.out = outputStream;
    }

    public void handle(SocketException e){

        out.println("|ERROR| A socket error has occured.");
        String newLogId;
        try {
            newLogId = LogWriter.writeErrorLog(e);
            System.out.println("Please refer to log with id:"+newLogId);
        } catch (IOException e1) {
            out.println("Couldn't write log, trying to reinitialize...");
        }
    }

    public void handle(IOException e){
        out.println("|ERROR| An IO error has occured.");
        String newLogId;
        try {
            newLogId = LogWriter.writeErrorLog(e);
            System.out.println("Please refer to log with id:"+newLogId);
        } catch (IOException e1) {
            out.println("Couldn't write log, trying to reinitialize...");
        }
    }

    public void handle(LogWriterInitException e){
        out.println("|ERROR| Cannot find or access the error log file. Please ensure that the file"+
            " log/errorLog.json exists and has correct permissions and restart the program");
    }


    private boolean reInitLogWriter(){
        return false;
    }

    
}
