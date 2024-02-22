package com.kuta.util.color;

/**
 * ColorMe
 * Utility to color console output using ANSI escape codes.
 * Methods return the given String colored by surrounding it with the appropriate ANSI escape codes.
 */
public abstract class ColorMe {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[92m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[96m";
    public static final String ANSI_PURPLE = "\u001B[95m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static String black(String txt){
        return ANSI_BLACK + txt + ANSI_RESET;
    }
    public static String red(String txt){
        return ANSI_RED+ txt + ANSI_RESET;
    }
    public static String green(String txt){
        return ANSI_GREEN+ txt + ANSI_RESET;
    }
    public static String yellow(String txt){
        return ANSI_YELLOW+ txt + ANSI_RESET;
    }
    public static String blue(String txt){
        return ANSI_BLUE+ txt + ANSI_RESET;
    }
    public static String purple(String txt){
        return ANSI_PURPLE+ txt + ANSI_RESET;
    }
    public static String cyan(String txt){
        return ANSI_CYAN+ txt + ANSI_RESET;
    }
    public static String white(String txt){
        return ANSI_WHITE+ txt + ANSI_RESET;
    }
    
}
