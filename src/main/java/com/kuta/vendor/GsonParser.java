package com.kuta.vendor;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Serves as a static access point to com.google.gson.Gson.
 * - see https://github.com/google/gson
 * GSON is a JSON parser for java.
 */
@Component
public abstract class GsonParser {
    public static Gson parser = new GsonBuilder().create();
}
