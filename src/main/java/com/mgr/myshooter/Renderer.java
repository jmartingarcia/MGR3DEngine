package com.mgr.myshooter;

import com.mgr.engine.RandomMap;
import com.mgr.engine.ShaderProgram;
import com.mgr.engine.Utils;
import com.mgr.engine.Window;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;


public class Renderer {

    private ShaderProgram shaderProgram;

    private RandomMap world;

    // Field of view in radians
    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;

    private Matrix4f projectionMatrix;


    public Renderer() {

    }

    public RandomMap getWorld() {

        return world;
    }

    public void setWorld(RandomMap world) {

        this.world = world;
    }

    public void init(Window window) throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/fragment.fs"));
        shaderProgram.link();

        calcProjectionMatrix(window);
        shaderProgram.createUniform("projectionMatrix");
    }

    private void calcProjectionMatrix(Window window){
        projectionMatrix = new Matrix4f().perspective(FOV, (float) window.getWidth() / window.getHeight(), Z_NEAR, Z_FAR);
    }

    public void clear() {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window) {

        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);
        glFrontFace(GL_CCW);

        shaderProgram.bind();
        shaderProgram.setUniform("projectionMatrix",projectionMatrix);

        world.drawMap();

        shaderProgram.unbind();

    }

    public void cleanUp() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }
}
