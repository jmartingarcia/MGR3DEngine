package com.mgr.configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    private final Properties prop = new Properties();

    public void init() {

        try (InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new FileNotFoundException("Sorry, unable to find config.properties");
            }

            //load a properties file from class path, inside static method
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public String getBaseTexturesFolder() {
         return prop.getProperty("base.folder.textures");
    }

    public String getBaseShadersFolder() {
        return prop.getProperty("base.folder.shaders");
    }
}
