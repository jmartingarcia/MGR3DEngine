package com.mgr.engine;

import org.joml.Vector3f;

public class Camera {

    private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f rotation = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f upvector = new Vector3f(0.0f, 1.0f, 0.0f);

    public Vector3f getPosition() {

        return new Vector3f(position);
    }

    public Vector3f getRotation() {

        return new Vector3f(rotation);
    }

    public void setRotation(final Vector3f newRotation) {

        rotation = newRotation;
    }

    public Vector3f getUpvector() {

        return new Vector3f(upvector);
    }

    public void setUpvector(final Vector3f upvector) {

        this.upvector = upvector;
    }

    public void setPosition(final Vector3f newPosition) {
        this.position = newPosition;
    }

    public void moveRotationBy(final Vector3f offset) {
        rotation.x += offset.x;
        rotation.y += offset.y;
        rotation.z += offset.z;
    }

    public void movePositionBy(final Vector3f offset) {
        if ( offset.z != 0 ) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y)) * -1.0f * offset.z;
            position.z += (float)Math.cos(Math.toRadians(rotation.y)) * offset.z;
        }
        if ( offset.x != 0) {
            position.x += (float)Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offset.x;
            position.z += (float)Math.cos(Math.toRadians(rotation.y - 90)) * offset.x;
        }
        position.y += offset.y;
    }
}
