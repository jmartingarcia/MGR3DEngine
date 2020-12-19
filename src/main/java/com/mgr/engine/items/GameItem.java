package com.mgr.engine.items;

import com.mgr.engine.Camera;
import com.mgr.engine.Material;
import com.mgr.engine.Mesh;
import com.mgr.engine.collision.BoundingBox;
import org.joml.Vector3f;

public abstract class GameItem implements IGameItem {

    protected Mesh mesh;
    protected Vector3f position;
    protected float scale;
    protected Vector3f rotation;

    protected float width;
    protected float height;
    protected float depth;

    protected final Camera camera;
    protected float speed;
    protected float rota_speed;
    protected float radius; // Used for collision detection

    protected float interactionDistance; // Max distance to be able to interact with the object


    public GameItem() {
        position = new Vector3f();
        scale = 1;
        rotation = new Vector3f();
        rotation.x = 0.0f;
        rotation.y = 0.0f;
        rotation.z = 0.0f;
        speed = 85.0f;
        rota_speed = 45.0f;
        radius = 10.0f;
        camera = new Camera();
        width = 1.0f;
        height = 1.0f;
        depth = 1.0f;
        interactionDistance = 30.0f;
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
        this.position = new Vector3f(position);
        camera.setPosition(new Vector3f(position));
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getRadius() {
        return radius;
    }

    public Material getMaterial() {
        return mesh.getMaterial();
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

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getDepth() {
        return depth;
    }

    // Calculates the next position the item will move but it won't actually move it
    public Vector3f getNextPosition(final float elapsedSeconds, final Vector3f direction) {
        final Vector3f original_position = new Vector3f(camera.getPosition());
        camera.movePositionBy(getPositionOffset(elapsedSeconds, direction));
        final Vector3f new_position = new Vector3f(camera.getPosition());
        camera.setPosition(original_position); // Returns camera to original position.
        return new_position;
    }

    public Vector3f getPositionOffset(final float elapsedSeconds, final Vector3f direction) {
        return new Vector3f(direction.x * speed * elapsedSeconds,
                direction.y * speed * elapsedSeconds,
                direction.z * speed * elapsedSeconds);
    }

    public BoundingBox getBoundingBox()  {
        final BoundingBox box = new BoundingBox();
        box.add(new Vector3f(position.x - width/2 - radius/2, position.y - height/2 - radius/2, position.z - depth/2 - radius/2 ));
        box.add(new Vector3f(position.x + width/2 + radius/2, position.y + height/2 +  radius/2, position.z + depth/2 +  radius/2 ));
        return box;
    }

    public BoundingBox getBoundingBoxAtPosition(final Vector3f newPosition)  {
        final BoundingBox box = new BoundingBox();
        box.add(new Vector3f(newPosition.x - width/2 - radius/2, newPosition.y - height/2 - radius/2, newPosition.z - depth/2 - radius/2 ));
        box.add(new Vector3f(newPosition.x + width/2 + radius/2, newPosition.y + height/2 +  radius/2, newPosition.z + depth/2 +  radius/2 ));
        return box;
    }

    public int getNumberVertices() {
        return mesh.getNumberVertices();
    }

    public int getNumberIndices() {
        return mesh.getNumberIndices();
    }

    public abstract void update(final float interval);

}