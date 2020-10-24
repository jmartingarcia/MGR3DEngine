package com.mgr.engine;

import org.joml.Vector3f;

public class Player {

    //private final Mesh model;

    private final Camera camera;
    private float speed = 35.0f;
    private float rota_speed = 25.0f;


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
        camera.setPosition(position);
    }

    public void walk(final float elapsedSeconds, final Vector3f speedPerAxis){
        camera.movePosition(speedPerAxis.x * speed * elapsedSeconds, speedPerAxis.y * speed * elapsedSeconds, speedPerAxis.z * speed * elapsedSeconds);
    }

    public void turn(final float elapsedSeconds, final Vector3f turnSpeedPerAxis){
        camera.moveRotation(turnSpeedPerAxis.x * rota_speed * elapsedSeconds, turnSpeedPerAxis.y * rota_speed * elapsedSeconds, turnSpeedPerAxis.z * rota_speed * elapsedSeconds);
    }

}
