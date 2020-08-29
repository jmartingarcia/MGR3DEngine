package com.mgr.engine;

import org.joml.Vector3f;

public class Camera {

    private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f rotation = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f upvector = new Vector3f(0.0f, 1.0f, 0.0f);

    public Vector3f getPosition() {

        return position;
    }

    public Vector3f getRotation() {

        return rotation;
    }

    public Vector3f getUpvector() {

        return upvector;
    }

    public void setUpvector(Vector3f upvector) {

        this.upvector = upvector;
    }

    public void moveRotation(float offsetX, float offsetY, float offsetZ) {
        rotation.x += offsetX;
        rotation.y += offsetY;
        rotation.z += offsetZ;
    }

    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        if ( offsetZ != 0 ) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            position.z += (float)Math.cos(Math.toRadians(rotation.y)) * offsetZ;
        }
        if ( offsetX != 0) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
            position.z += (float)Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
        }
        position.y += offsetY;
    }
}
