package com.mgr.engine;

import org.joml.Vector3f;

public class Player {

    //private final Mesh model;

    private final Camera camera;
    private float speed = 1.5f;
    private float rota_speed = 1.5f;


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

    public Vector3f getRotation() {
        return camera.getRotation();
    }

    public float getRota_speed() {

        return rota_speed;
    }

    public void setRota_speed(float rota_speed) {

        this.rota_speed = rota_speed;
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

    public void setPosition(final Vector3f position) {
        camera.movePosition(position.x, position.y, position.z);
    }

    public void walk(final Vector3f speedPerAxis){
        camera.movePosition(speedPerAxis.x * speed, speedPerAxis.y * speed, speedPerAxis.z * speed);
    }

    public void turn(final Vector3f turnSpeedPerAxis){
        camera.moveRotation(turnSpeedPerAxis.x * rota_speed, turnSpeedPerAxis.y * rota_speed, turnSpeedPerAxis.z * rota_speed);
    }

}
