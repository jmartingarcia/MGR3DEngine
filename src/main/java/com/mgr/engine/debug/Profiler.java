package com.mgr.engine.debug;


import com.mgr.configuration.GameProperties;
import com.mgr.engine.*;
import com.mgr.engine.items.IGameItem;
import com.mgr.engine.items.TextItem;
import com.mgr.engine.shaders.ShaderProgram;
import com.mgr.engine.shaders.StaticShaderFactory;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profiler {


    private final Map<String, TextItem> profilerInfo = new HashMap<>();;
    private GameProperties props;
    private ShaderProgram shaderProgram;



    public void init(GameProperties props) throws Exception {

        this.props = props;

        shaderProgram = StaticShaderFactory.getSimpleShaderOrthographic();
    }

    public List<TextItem> getTextItems() {
        return new ArrayList<TextItem>(profilerInfo.values());
    }

    public void displayProfileData() {
        for (String key : profilerInfo.keySet()) {
           System.out.println(key + " : " + profilerInfo.get(key));
        }
    }


    public void setProfilerEntry(String key, String value) {
         if (profilerInfo.containsKey(key)) {
            TextItem item = profilerInfo.get(key);
            item.setText(value);
        } else {
             try {
                 TextItem item = new TextItem(value, this.props.getBaseTexturesFolder() + "\\font_texture.png", 16, 16);
                 profilerInfo.put(key, item);
                 //Set position of text on screen
                 float posy = (profilerInfo.values().size())*30.0f;
                 item.setPosition(new Vector3f(0.0f, posy, 0));
             } catch (Exception ex){
               System.out.println("Could not set profile entry with key " + key + ". Error = " + ex.getMessage());
             }
        }
    }

    public void render(final Window window, final Transformation transformation) throws NullPointerException {

        if (shaderProgram == null)
            throw new NullPointerException("Game Item Shader not defined");

        shaderProgram.bind();

        shaderProgram.setUniform("texture_sampler", 0);

        Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, (float)window.getWidth(), (float)window.getHeight(), 0);

        for (TextItem textItem : getTextItems()) {
            // Set orthographic and model matrix for this text item
            textItem.setShaderProgram(shaderProgram);
            Matrix4f projModelMatrix = transformation.getOrtoProjModelMatrix(textItem, ortho);
            shaderProgram.setUniform("projModelMatrix", projModelMatrix);
            shaderProgram.setUniform("color", textItem.getMesh().getMaterial().getAmbientColor());

            // Render the mesh for this HUD item
            textItem.render(null, null, null, null, null);
        }

        shaderProgram.unbind();
    }

    public void cleanUp() {
        for (String key : profilerInfo.keySet()) {
            TextItem item = profilerInfo.get(key);
            if (item != null) item.cleanUp();
        }
    }

}
