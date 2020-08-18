package com.mgr.myshooter;

import com.mgr.engine.IGameLogic;
import com.mgr.engine.Window;
import com.mgr.engine.*;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class MGRGame implements IGameLogic {

    private int direction_x,
                direction_y,
                direction_z,
                rotation_x,
                rotation_y,
                rotation_z  = 0;
    

    private final Renderer renderer;

    private final RandomMap world;

    private final Integer MAX_NUMBER_ROOMS = 5;

    private final Player player;




    public MGRGame() {

        renderer = new Renderer();
        world    = new RandomMap();
        player   = new Player();
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
        rotation_x  = 0;
        rotation_y  = 0;
        rotation_z  = 0;

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
        } else if ( window.isKeyPressed(GLFW_KEY_RIGHT) ) {
            rotation_y = 1;
        } else if ( window.isKeyPressed(GLFW_KEY_LEFT) ) {
            rotation_y = -1;
        }
    }

    @Override
    public void update(float interval) {
        //Translation
        Vector3f newPosition = player.getPosition();
        newPosition.x += direction_x * player.getSpeed();
        newPosition.y += direction_y * player.getSpeed();
        newPosition.z += direction_z * player.getSpeed();
        player.setPosition(newPosition);

        //Rotation
        Vector3f newRotation = player.getRotation();
        newRotation.x += rotation_x * player.getRota_speed();
        newRotation.y += rotation_y * player.getRota_speed();
        newRotation.z += rotation_z * player.getRota_speed();
        player.setRotation(newRotation);
    }

    @Override
    public void render(Window window) {
        window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        renderer.render(player.getCamera(), window);
    }

    @Override
    public void cleanUp(){
        world.cleanUp();
        renderer.cleanUp();
    }
}
