package com.mgr.myshooter;

import com.mgr.engine.IGameLogic;
import com.mgr.engine.Window;
import com.mgr.engine.*;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class MGRGame implements IGameLogic {

    private int direction_x,
                direction_y,
                direction_z= 0;
    

    private final Renderer renderer;

    private final RandomMap world;

    private final Integer MAX_NUMBER_ROOMS = 5;

    private final Camera player;


    public MGRGame() {

        renderer = new Renderer();
        world    = new RandomMap();
        player   = new Camera();
    }
    
    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);
        world.generateRandomMap(MAX_NUMBER_ROOMS);
        renderer.setWorld(world);
    }
    
    @Override
    public void input(Window window) {
        direction_x = 0;
        direction_y = 0;
        direction_z = 0;

        if ( window.isKeyPressed(GLFW_KEY_W) ) {
            direction_z = 1;
        } else if ( window.isKeyPressed(GLFW_KEY_S) ) {
            direction_z = -1;
        } else if ( window.isKeyPressed(GLFW_KEY_A) ) {
            direction_x = -1;
        } else if ( window.isKeyPressed(GLFW_KEY_D) ) {
            direction_x = 1;
        } else if ( window.isKeyPressed(GLFW_KEY_SPACE) ) {
            direction_y = 1;
        } else if ( window.isKeyPressed(GLFW_KEY_V) ) {
            direction_y = -1;
        }
    }

    @Override
    public void update(float interval) {
        Vector3f newPosition = player.getPosition();
        newPosition.x += direction_x * 0.01f;
        newPosition.y += direction_y * 0.01f;
        newPosition.z += direction_z * 0.01f;
        player.setPosition(newPosition);
    }

    @Override
    public void render(Window window) {
        window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        renderer.render(window);
    }

    @Override
    public void cleanUp(){
        world.cleanUp();
        renderer.cleanUp();
    }
}
