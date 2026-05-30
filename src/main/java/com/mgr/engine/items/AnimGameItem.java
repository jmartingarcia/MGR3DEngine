package com.mgr.engine.items;

import com.mgr.engine.Mesh;
import com.mgr.engine.Transformation;
import com.mgr.engine.collision.BoundingBox;
import com.mgr.engine.items.GameItem;
import com.mgr.engine.light.DirectionalLight;
import com.mgr.engine.loaders.AnimatedFrame;
import com.mgr.engine.loaders.Animation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

public class AnimGameItem extends GameItem {

    private Map<String, Animation> animations;

    private Animation currentAnimation;


    public AnimGameItem(Mesh[] meshes, Map<String, Animation> animations) {
        super(meshes);
        this.animations = animations;
        Optional<Map.Entry<String, Animation>> entry = animations.entrySet().stream().findFirst();
        currentAnimation = entry.isPresent() ? entry.get().getValue() : null;

        radius = 2.0f;

    }

    public Animation getAnimation(String name) {
        return animations.get(name);
    }

    public Animation getCurrentAnimation() {
        return currentAnimation;
    }

    public void setCurrentAnimation(Animation currentAnimation) {
        this.currentAnimation = currentAnimation;
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

        final AnimatedFrame frame = currentAnimation.getCurrentFrame();
        shaderProgram.setUniform("jointsMatrix", frame.getJointMatrices());

        for (Mesh value : mesh)
            value.render();

        shaderProgram.unbind();

        // TODO - Calculate the time that has passed against the frame duration to see if we really need to change the frame
        currentAnimation.nextFrame();
    }
}
