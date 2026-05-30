package com.mgr.configuration;

import org.joml.Vector3f;
import com.mgr.engine.shaders.ShaderType;

public class ItemProperties {

    final private PropertiesLoader properties = new PropertiesLoader();

    public ItemProperties(final String itemName){
        properties.init("Items\\"+itemName+".properties");
    }

    public String getModelFileName() {
        return properties.getPropertyValue("item.model_file");
    }

    public String getModelTexturesFolderName() {
        return properties.getPropertyValue("item.model_textures");
    }

    public Float getModelScale() {
        final String valueStr = properties.getPropertyValue("item.scale");
        return Float.parseFloat(valueStr);
    }

    public Vector3f getModelPosition() {
        final String valueStr = properties.getPropertyValue("item.position");
        final String[] values = valueStr.split(",");
        return new Vector3f(Float.parseFloat(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]));
    }

    public Vector3f getModelRotation() {
        final String valueStr = properties.getPropertyValue("item.rotation");
        final String[] values = valueStr.split(",");
        return new Vector3f(Float.parseFloat(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]));
    }

    public ShaderType getShader() {

        return ShaderType.valueOf(properties.getPropertyValue("item.shader_type"));
    }

}