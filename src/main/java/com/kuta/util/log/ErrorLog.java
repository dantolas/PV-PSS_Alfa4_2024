package com.kuta.util.log;
import com.google.gson.annotations.SerializedName;
import com.kuta.Config;

/**
 * Class representing Error logs for serialization
 */
public class ErrorLog {

    @SerializedName("cas")
    public String time;
    @SerializedName("id")
    public String id;
    @SerializedName("nazev_chyby")
    public String exceptionName;
    @SerializedName("text_chyby")
    public String exceptionText;
    @SerializedName("stacktrace")
    public String stacktrace;
    @SerializedName("konfigurace")
    public Config config;

    public ErrorLog(){

    }

    public ErrorLog(String id, String time, String exceptionName,String exceptionText,String stacktrace,Config config){
        this.id = id;
        this.exceptionName = exceptionName;
        this.exceptionText = exceptionText;
        this.stacktrace = stacktrace;
        this.time = time;
        this.config = config;
    }

    @Override
    public String toString() {
        return "{\nCas:" + time + ",\nid:" + id + ",\nazev_chyby:" + exceptionName + ",\n=text_chyby"
                + exceptionText + ",\nstacktrace:" + stacktrace.substring(0,40) +"\nkonfigurace:\n"+config+"\n}";
    }

    

    
    
}
