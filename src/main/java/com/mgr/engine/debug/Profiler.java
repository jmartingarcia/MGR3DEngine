package com.mgr.engine.debug;


import com.mgr.configuration.PropertiesLoader;
import com.mgr.engine.*;
import com.mgr.engine.items.IGameItem;
import com.mgr.engine.items.TextItem;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profiler {


    private final Map<String, TextItem> profilerInfo = new HashMap<>();;
    private PropertiesLoader props;
    private ShaderProgram shaderProgram;



    public void init(PropertiesLoader props) throws Exception {

        this.props = props;
        createShaderProgram();
    }

    public List<TextItem> getTextItems() {
        return new ArrayList<TextItem>(profilerInfo.values());
    }

    public void displayProfileData() {
        for (String key : profilerInfo.keySet()) {
           System.out.println(key + " : " + profilerInfo.get(key));
        }
    }

    private void createShaderProgram() throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/Shaders/simple_vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/Shaders/simple_fragment.fs"));
        shaderProgram.link();

        // Create uniforms for Ortographic-model projection matrix and base color
        shaderProgram.createUniform("projModelMatrix");
        shaderProgram.createUniform("color");
        shaderProgram.createUniform("texture_sampler");
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

    public void render(final Window window, final Transformation transformation) {
        shaderProgram.bind();

        shaderProgram.setUniform("texture_sampler", 0);

        Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, (float)window.getWidth(), (float)window.getHeight(), 0);

        for (IGameItem textItem : getTextItems()) {
            // Set orthographic and model matrix for this text item
            Matrix4f projModelMatrix = transformation.getOrtoProjModelMatrix(textItem, ortho);
            shaderProgram.setUniform("projModelMatrix", projModelMatrix);
            shaderProgram.setUniform("color", textItem.getMesh().getMaterial().getAmbientColor());

            // Render the mesh for this HUD item
            textItem.render();
        }

        shaderProgram.unbind();
    }

    public void cleanUp() {
        for (String key : profilerInfo.keySet()) {
            TextItem item = profilerInfo.get(key);
            if (item != null) item.cleanUp();
        }
        shaderProgram.cleanup();
    }

}
