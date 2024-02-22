package com.kuta.util.log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import com.kuta.Config;
import com.kuta.util.IO;
import com.kuta.vendor.GsonParser;

/**
 * This class writes both Operation and Error logs.
 * All of the functionality is static, so no instances need to be created.
 * !!! HOWEVER, the class needs to be initialized with the init() function.
 * Paths to the directories where the logs should be, have to be provided to the init() function.
 * 
 */
public class LogWriter {

    private static final String ERROR_FILE_NAME = "errorLog.json";
    private static String errorLogPath;
    private static Config config;
    public static boolean isInitialized = false;



    /**
     * Writes a new entry to the error log file.
     * It first reads and loads all the error logs
     * currently in the file to an ArrayList.
     * 
     * Then a new log is created and appended to the list.
     * 
     * The list of logs is then serialized and written back to the file.
     * 
     * @param e - Exception to be written to the log
     * @return - UUID (String) of the newly created log
     * @throws IOException
     */
    public static String writeErrorLog(Exception e) throws IOException{
        String filePath = errorLogPath +ERROR_FILE_NAME;

        String logFileText = IO.readFileIntoString(filePath);
        ArrayList<ErrorLog> logs = new ArrayList<>(Arrays.asList(jsonToErrorLogArray(logFileText)));
        ErrorLog newLog = createNewErrorLog(e);
        logs.add(newLog);

        logFileText = GsonParser.parser.toJson(logs);
        IO.overWriteFile(logFileText, filePath);
        return newLog.id;
    }
    
    /**
     * Helper method to transform json String to ErrorLog array.
     * 
     * 
     * Utilizes Google Gson open library
     * @param json - Json to be transformed
     * @return - ErrorLog[]
     */
    private static ErrorLog[] jsonToErrorLogArray(String json){

        ErrorLog[] errorLogs = GsonParser.parser.fromJson(json,ErrorLog[].class);
        if(errorLogs == null) errorLogs = new ErrorLog[0];
        return errorLogs;
    }

    /**
     * Helper method to make and return a new ErrorLog object.
     * 
     * @param e - Exception to create the object with
     * @return - ErrorLog instance
     */
    private static ErrorLog createNewErrorLog(Exception e){
        String id = UUID.randomUUID().toString();
        String systemTime = DateTimeFormatter.ofPattern("dd.MM.yyyy_HH-mm-ss").format(LocalDateTime.now());
        String exceptionName = e.getClass().getName();
        String exceptionMessage = e.getMessage();
        String stacktrace = StackTraceToString(e);
        com.kuta.Config config = LogWriter.config;
        ErrorLog newLog = new ErrorLog(id,systemTime,exceptionName,exceptionMessage,stacktrace,config);
        return newLog;

    }

    /**
     * Initializes all of the static functionality of the LogWriter.
     * This method must be ran before trying to do work with the LogWriter.
     * 
     * Sets the directories where logs should be placed, checks if the necessary files are there and if not creates them.
     * 
     * @param errorLogPath - Path leading to the directory where error logs should be.
     * @param operationLogPath - Path leading to the directory where operation logs should be.
     * @throws LogWriterInitException - If any exception occurs, this special exception is thrown. 
     */
    public static void Init(Config config) throws LogWriterInitException{
        String errorLogPath = System.getProperty("user.dir")+"/log/";
        try {
            if(!IO.isDirectory(errorLogPath))
            throw new LogWriterInitException("Poskytnuta cesta pro umisteni error logu nekonci adresarem. :"+errorLogPath);

            if(!errorLogPath.endsWith("/")){
                errorLogPath += "/";
            }


            LogWriter.errorLogPath = errorLogPath;
            checkFilesExist(errorLogPath);
            LogWriter.config = config;
            LogWriter.isInitialized = true;

        } catch (SecurityException e) {
            throw new LogWriterInitException("Nastal problém s přístupem k souborům. Zkontrolujte prosím že jsou všechny nakonfigurované soubory a složky správně přístupné.");
        }
        

        
       
    }

    /**
     * Helper method to check if the default files exist in provided directories.
     * If they don't, they are created.
     * @param errorLogPath
     * @param operationLogPath
     * @throws LogWriterInitException
     */
    private static void checkFilesExist(String errorLogPath) throws LogWriterInitException{
        try {
            if(!IO.isFile(errorLogPath+ERROR_FILE_NAME)){
            IO.createFile(errorLogPath+ERROR_FILE_NAME);
            IO.overWriteFile("[]", errorLogPath+ERROR_FILE_NAME);
            }
        }
        catch (IOException e) {
            throw new LogWriterInitException("Chyba při kontrole log souborů. Zkontrolujte že soubor errorLog.json je v /log adresari.");
        }


    }

    public static ErrorLog[] getErrorLogArray() throws FileNotFoundException, IOException{
        String json = IO.readFileIntoString(errorLogPath+ERROR_FILE_NAME);
        return jsonToErrorLogArray(json);
    }

    /**
     * Helper method that turns Exception.stacktrace() to a string, because it's not provided by default.
     * That's java for you :]
     * @param e
     * @return
     */
    private static String StackTraceToString(Exception e){
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    
}
