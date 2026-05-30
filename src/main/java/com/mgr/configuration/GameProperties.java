package com.mgr.configuration;

import org.joml.Vector3f;

public class GameProperties {

   final private PropertiesLoader properties = new PropertiesLoader();

   public GameProperties(){
        properties.init("config.properties");
    }

    public String getBaseTexturesFolder() {
        return properties.getPropertyValue("base.folder.textures");
    }

    public String getBaseShadersFolder() {
        return properties.getPropertyValue("base.folder.shaders");
    }

    public String getBaseModelsFolder() {
        return properties.getPropertyValue("base.folder.models");
    }

}


