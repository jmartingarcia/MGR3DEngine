package com.mgr.engine;

import com.mgr.engine.collision.BoundingBox;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.Vector3f;

public class GameItem {

    protected Mesh mesh;
    protected Vector3f position;
    protected float scale;
    protected Vector3f rotation;

    protected final Camera camera;
    protected float speed = 85.0f;
    protected float rota_speed = 45.0f;


    public GameItem() {
        position = new Vector3f();
        scale = 1;
        rotation = new Vector3f();
        rotation.x = 0.0f;
        rotation.y = 0.0f;
        rotation.z = 0.0f;
        camera = new Camera();
    }
    
    public GameItem(Mesh mesh) {
        super();
        this.mesh = mesh;
        camera = new Camera();
    }

    public Vector3f getPosition() {

        return new Vector3f(position);
    }

    public void setPosition(final Vector3f position) {
        this.position = position;
        camera.setPosition(position);
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Vector3f getRotation() {
        return new Vector3f(rotation);
    }

    public void setRotation(final Vector3f rotation) {
        this.rotation = rotation;
        camera.setRotation(rotation);
    }
    
    public Mesh getMesh() {
        return mesh;
    }
    
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(final float speed) {
        this.speed = speed;
    }

    public float getRota_speed() {
        return rota_speed;
    }

    public void setRota_speed(final float rota_speed) {
        this.rota_speed = rota_speed;
    }

    public Vector3f getUpvector() {
        return camera.getUpvector();
    }

    public void setUpvector(final Vector3f upvector) {
        camera.setUpvector(upvector);
    }

    public Camera getCamera() {
        return camera;
    }

    public void render() {
        mesh.render();
    }

    public void cleanUp() {
        mesh.cleanUp();
    }

    // Calculates the next position the item will move but it won't actually move it
    public Vector3f getNextPosition(final float elapsedSeconds, final Vector3f direction) {
        final Vector3f original_position = new Vector3f(camera.getPosition());
        camera.movePositionBy(getPositionOffset(elapsedSeconds, direction));
        final Vector3f new_position = new Vector3f(camera.getPosition());
        camera.setPosition(original_position); // Returns camera to original position.
        return new_position;
    }

    private Vector3f getPositionOffset(final float elapsedSeconds, final Vector3f direction) {
        return new Vector3f(direction.x * speed * elapsedSeconds,
                direction.y * speed * elapsedSeconds,
                direction.z * speed * elapsedSeconds);
    }

    public void move(final float elapsedSeconds, final Vector3f direction){
        camera.movePositionBy(getPositionOffset(elapsedSeconds, direction));
        position = camera.getPosition(); // The camera movePositionBy takes into account the rotation
    }

    public void turn(final float elapsedSeconds, final Vector3f direction){
        final Vector3f offset_rotation = new Vector3f(direction.x * rota_speed * elapsedSeconds, direction.y * rota_speed * elapsedSeconds, direction.z * rota_speed * elapsedSeconds);
        rotation.x += offset_rotation.x;
        rotation.y += offset_rotation.y;
        rotation.z += offset_rotation.z;
        camera.setRotation(rotation);
    }

    public BoundingBox getBoundingBox() throws NotImplementedException {
        throw new NotImplementedException("Method not implemented yet");
    }

    public BoundingBox getBoundingBoxAtPosition(final Vector3f position) throws NotImplementedException {
        throw new NotImplementedException("Method not implemented yet");
    }
}