package com.kuta;

import java.io.IOException;

import org.springframework.stereotype.Service;

/**
 * ConfigService
 */
@Service
public class ConfigService {

    public Config loadConfig() throws IOException {
        return Config.fromFile("conf/config.json");
    }
}
