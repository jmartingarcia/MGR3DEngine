package com.mgr.engine;

import org.joml.Vector3f;

public class Camera {

    private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f rotation = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f upvector = new Vector3f(0.0f, 1.0f, 0.0f);

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public Vector3f getUpvector() {
        return upvector;
    }

    public void setUpvector(Vector3f upvector) {
        this.upvector = upvector;
    }


}
