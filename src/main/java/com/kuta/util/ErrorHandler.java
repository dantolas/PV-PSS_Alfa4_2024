package com.kuta.util;

import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketException;

import org.springframework.stereotype.Service;

import com.kuta.util.color.ColorMe;
import com.kuta.util.log.LogWriter;
import com.kuta.util.log.LogWriterInitException;

/**
 * Used for error handling and writing logs of exceptions during runtime.
 * Isn't used everywhere in the program, mainly used in the Main method to catch runtime errors
 * and inform the user.
 *
 * Handling is achieved by using a specific handle() method with the specific Exception
 * for different implementation
 */
@Service
public class ErrorHandler {

    private final PrintStream out;

    private final String ERROR = ColorMe.red("|ERROR|");

    public ErrorHandler(){
        this.out = System.out;
    }

    public void handle(SocketException e){

        out.println(ERROR+" A socket error has occured.");
        String newLogId;
        try {
            newLogId = LogWriter.writeErrorLog(e);
            System.out.println("Please refer to log with id:"+newLogId);
        } catch (IOException e1) {
            out.println("Couldn't write log, trying to reinitialize...");
        }
    }

    public void handle(IOException e){
        out.println(ERROR+" An IO error has occured.");
        String newLogId;
        try {
            newLogId = LogWriter.writeErrorLog(e);
            System.out.println("Please refer to log with id:"+newLogId);
        } catch (IOException e1) {
            out.println("Couldn't write log, trying to reinitialize...");
        }
    }
    public void handle(Exception e){
        out.println(ERROR+"A general exception occured");
        String newLogId;
        try {
            newLogId = LogWriter.writeErrorLog(e);
            System.out.println("Please refer to log with id:"+newLogId);
        } catch (IOException e1) {
            out.println("Couldn't write log, trying to reinitialize...");
        }
    }

    public void handle(LogWriterInitException e){
        out.println(ERROR+" Cannot find or access the error log file. Please ensure that the file"+
            " log/errorLog.json exists and has correct permissions and restart the program");
    }


    private boolean reInitLogWriter(){
        return false;
    }

    
}
