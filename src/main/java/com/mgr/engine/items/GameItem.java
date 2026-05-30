package com.mgr.engine.items;

import com.mgr.engine.Camera;
import com.mgr.engine.Material;
import com.mgr.engine.Mesh;
import com.mgr.engine.Transformation;
import com.mgr.engine.collision.BoundingBox;
import com.mgr.engine.light.DirectionalLight;
import com.mgr.engine.shaders.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameItem implements IGameItem {

    protected ShaderProgram shaderProgram;
    protected Mesh[] mesh;
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

    protected float specularPower = 1.0f;

    protected List<BoundingBox> boundingBoxes;

    public GameItem() {
        position = new Vector3f();
        scale = 1;
        rotation = new Vector3f();
        rotation.x = 0.0f;
        rotation.y = 0.0f;
        rotation.z = 0.0f;
        speed = 85.0f;
        rota_speed = 45.0f;
        radius = 2.0f;
        camera = new Camera();
        width = 1.0f;
        height = 1.0f;
        depth = 1.0f;
        interactionDistance = 30.0f;
        specularPower = 1.0f;
    }
    
    public GameItem(final Mesh[] mesh) {
        this();
        this.mesh = mesh;
    }

    public Vector3f getPosition() {

        return new Vector3f(position);
    }

    public void setPosition(final Vector3f position) {
        this.position = new Vector3f(position);
        camera.setPosition(new Vector3f(position));

        // Everytime the item moves, the bounding boxes needs to be recalculated to the new position
        boundingBoxes = calculateBoundingBoxes();
    }

    public void setShaderProgram(final ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
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
        return mesh[0].getMaterial();
    }

    public List<Material> getAllMaterials() {
        ArrayList<Material> materials = new ArrayList<>();
        for (Mesh value : mesh) {
            materials.add(value.getMaterial());
        }
        return materials;
    }

    public Vector3f getRotation() {
        return new Vector3f(rotation);
    }

    public void setRotation(final Vector3f rotation) {
        this.rotation = rotation;
        camera.setRotation(rotation);
    }

    public Mesh getMesh() {
        return mesh[0];
    }

    public Mesh[] getAllMeshes() {
        return mesh;
    }

    public void setMesh(Mesh[] mesh) {
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

    public void render(final Matrix4f projectionMatrix, final Matrix4f viewMatrix,
                       final Transformation transformation, final Vector3f ambientLight,
                       final DirectionalLight directionalLight) throws NullPointerException {


        if (shaderProgram == null)
            throw new NullPointerException("Game Item Shader not defined");

        shaderProgram.bind();

        shaderProgram.setUniform("projectionMatrix",projectionMatrix);
        shaderProgram.setUniform("modelViewMatrix", transformation.getModelViewMatrix(this, viewMatrix));

        shaderProgram.setUniform("material", getMaterial());
        shaderProgram.setUniform("ambientLight", ambientLight);
        shaderProgram.setUniform("specularPower", specularPower);
        shaderProgram.setUniform("texture_sampler", 0);
        shaderProgram.setUniform("normalMap", 1);

        for (Mesh value : mesh)
            value.render();

        shaderProgram.unbind();
    }

    public void cleanUp() {
        for (Mesh value : mesh)
            value.cleanUp();
        mesh = null;
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

    public List<BoundingBox> getBoundingBox()  {
        final BoundingBox box = new BoundingBox();
        box.add(new Vector3f(position.x - width/2 - radius/2, position.y - height/2 - radius/2, position.z - depth/2 - radius/2 ));
        box.add(new Vector3f(position.x + width/2 + radius/2, position.y + height/2 +  radius/2, position.z + depth/2 +  radius/2 ));
        return List.of(box);
        //return boundingBoxes;
    }

    public BoundingBox getBoundingBoxAtPosition(final Vector3f newPosition)  {
        final BoundingBox box = new BoundingBox();
        box.add(new Vector3f(newPosition.x - width/2 - radius/2, newPosition.y - height/2 - radius/2, newPosition.z - depth/2 - radius/2 ));
        box.add(new Vector3f(newPosition.x + width/2 + radius/2, newPosition.y + height/2 +  radius/2, newPosition.z + depth/2 +  radius/2 ));
        return box;
    }

    public int getNumberVertices() {
        int totalVertices = 0;
        for (Mesh value : mesh){
            totalVertices += value.getNumberVertices();
        }
        return totalVertices;
    }

    public int getNumberIndices() {
        int totalIndices = 0;
        for (Mesh value : mesh){
            totalIndices += value.getNumberIndices();
        }
        return totalIndices;
    }

    public void update(final float interval) {

    }

    /*
    This method is just calculating the bounding boxes of each mesh but is not taking into account the movement
    as part of the animation
    TODO -- We need to take into account the new position of the vertices during animation
     */
    protected List<BoundingBox> calculateBoundingBoxes()  {

        if (getAllMeshes() == null) return null;

        final ArrayList<BoundingBox> result = new ArrayList<>();
        final Matrix4f translationMatrix = new Matrix4f();
        translationMatrix.translate(position);

        Arrays.stream(getAllMeshes()).forEach(
                (mesh) -> {
                    final BoundingBox box =  mesh.getBoundingBoxCopy();
                    // Need to translate the box the current item's position
                    box.setToTransformedBox(box,translationMatrix);
                    result.add(box);
                }
        );

        return result;
    }

}