package com.mgr.configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    private final Properties prop = new Properties();

    public void init(final String propertiesFilePath) {

        try (InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream(propertiesFilePath)) {

            if (input == null) {
                throw new FileNotFoundException("Sorry, unable to find " + propertiesFilePath);
            }

            //load a properties file from class path, inside static method
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getPropertyValue(final String propertyKey) {
        return prop.getProperty(propertyKey);
    }

}
