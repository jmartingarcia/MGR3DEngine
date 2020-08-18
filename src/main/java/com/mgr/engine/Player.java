package com.mgr.engine;

import org.joml.Vector3f;

public class Player {

    //private final Mesh model;

    private final Camera camera;
    private float speed = 0.5f;

    private float rota_speed = 0.09f; // About 5 degrees


    public Player(){
        camera = new Camera();
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public Vector3f getPosition() {

        return camera.getPosition();
    }

    public void setPosition(Vector3f position) {
        camera.setPosition(position);
    }

    public Vector3f getRotation() {
        return camera.getRotation();
    }

    public float getRota_speed() {
        return rota_speed;
    }

    public void setRota_speed(float rota_speed) {
        this.rota_speed = rota_speed;
    }

    public void setRotation(Vector3f rotation) {
        camera.setPosition(rotation);
    }

    public Vector3f getUpvector() {
        return camera.getUpvector();
    }

    public void setUpvector(Vector3f upvector) {
        camera.setUpvector(upvector);
    }

    public Camera getCamera() {
        return camera;
    }


}
