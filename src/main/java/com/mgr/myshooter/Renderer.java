package com.mgr.myshooter;

import com.mgr.engine.*;
import com.mgr.engine.debug.Profiler;
import com.mgr.engine.player.Player;
import org.joml.Matrix4f;


import java.util.List;

import static org.lwjgl.opengl.GL11.*;


public class Renderer  {

    private World world;

    // Field of view in radians
    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 10000.f;

    private Matrix4f projectionMatrix;

    private final Transformation transformation;

    private Profiler profiler;


    public Renderer() {

        transformation = new Transformation();
    }

    public void init(Window window, Profiler profiler, World world) throws Exception {

        this.world = world;
        this.profiler = profiler;

        projectionMatrix =  transformation.getProjectionMatrix(FOV, (float) window.getWidth(),  (float)window.getHeight(),  Z_NEAR, Z_FAR);
    }


    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
    }

    private void enableCullFace() {
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
    }

    private void enableTextureBackgroundColorBlending() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void render(final List<Player> players, final Camera currentCamera, final Window window, final boolean showProfilerData) {

        clear();

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
            projectionMatrix =  transformation.getProjectionMatrix(FOV, (float) window.getWidth(),  (float)window.getHeight(),  Z_NEAR, Z_FAR);
        }

        enableCullFace();

        enableTextureBackgroundColorBlending();


        //Render Scene
        renderScene(currentCamera, projectionMatrix, transformation);

        //Render players
        for (Player p : players)
            p.render(transformation, (float) window.getWidth(),  (float)window.getHeight());

        // Finally show debugging data on screen if enabled
        if (showProfilerData) {
            glDisable(GL_BLEND);
            renderProfiler(window, transformation);
        }

    }

    private void renderScene(final Camera camera, final Matrix4f projectionMatrix, final Transformation transformation) {

        world.render(camera, projectionMatrix, transformation);
    }

    private void renderProfiler(final Window window, final Transformation transformation) {
         profiler.render(window, transformation);
    }


    public void cleanUp() {

    }

}
