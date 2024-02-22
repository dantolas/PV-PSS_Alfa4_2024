package com.kuta.vendor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class GsonParser {
    public static Gson parser = new GsonBuilder().create();
}
