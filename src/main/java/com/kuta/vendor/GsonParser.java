package com.kuta.vendor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Serves as a static access point to com.google.gson.Gson.
 * - see https://github.com/google/gson
 * GSON is a JSON parser for java.
 */
public abstract class GsonParser {
    public static Gson parser = new GsonBuilder().create();
}
