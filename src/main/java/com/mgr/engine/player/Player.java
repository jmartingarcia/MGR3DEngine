package com.mgr.engine.player;

import com.mgr.engine.collision.BoundingBox;
import com.mgr.engine.shaders.ShaderProgram;
import com.mgr.engine.Texture;
import com.mgr.engine.Transformation;
import com.mgr.engine.Utils;
import com.mgr.engine.items.*;
import com.mgr.engine.shaders.StaticShaderFactory;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Player extends GameItem {

    private IGameItem crosshair;
    private boolean isMainPlayer;
    private ShaderProgram shaderProgram;

    public Player(){

        super();
        setRadius(1.0f);
        width = 2.0f;
        height = 2.0f;
        depth = 2.0f;
        crosshair = null;
        isMainPlayer = false;
    }

    public Player(boolean isMainPlayer) {
        this();
        this.isMainPlayer = isMainPlayer;
    }

    public void createCrosshair(final Texture crosshairTexture) throws Exception {

        final GameItemProperties itemProperties = new GameItemProperties();
        itemProperties.setWidth(100.0f);
        itemProperties.setHeight(100.0f);
        itemProperties.setItemTexture(crosshairTexture);

        crosshair =  GameItemFactory.createItem(GameItemType.CROSSHAIR, itemProperties);

        shaderProgram = StaticShaderFactory.getSimpleShaderOrthographic();
    }

    private void move(final float elapsedSeconds, final Vector3f direction){
        camera.movePositionBy(getPositionOffset(elapsedSeconds, direction));
        position = camera.getPosition(); // The camera movePositionBy takes into account the rotation
    }

    public List<BoundingBox> getBoundingBox()  {
          return boundingBoxes;
    }

    protected List<BoundingBox> calculateBoundingBoxes()  {
        final BoundingBox box = new BoundingBox();
        box.add(new Vector3f(position.x - width/2 - radius/2, position.y - height/2 - radius/2, position.z - depth/2 - radius/2 ));
        box.add(new Vector3f(position.x + width/2 + radius/2, position.y + height/2 +  radius/2, position.z + depth/2 +  radius/2 ));
        return List.of(box);
    }


    private void turn(final float elapsedSeconds, final Vector3f direction){
        final Vector3f offset_rotation = new Vector3f(direction.x * rota_speed * elapsedSeconds, direction.y * rota_speed * elapsedSeconds, direction.z * rota_speed * elapsedSeconds);
        rotation.x += offset_rotation.x;
        rotation.y += offset_rotation.y;
        rotation.z += offset_rotation.z;
        camera.setRotation(rotation);
    }

    public void update(final float interval) {
        return;
    }

    public void updatePlayer(final float interval, final Vector3f translation, final Vector3f rotation) {
        move(interval, translation);
        turn(interval, rotation);
    }

    public IGameItem getCrosshair() {
        return crosshair;
    }

    public void cleanUp() {
        if (crosshair != null) {
            crosshair.cleanUp();
        }
    }

    public void render(final Transformation transformation, final float windowWidth, final float windowHeight) {

        if (isMainPlayer) {
            renderCrosshair(transformation, windowWidth, windowHeight);
        }
    }

    private void renderCrosshair(final Transformation transformation, final float windowWidth, final float windowHeight) {

        shaderProgram.bind();

        shaderProgram.setUniform("texture_sampler", 0);

        // Set crosshair to the center of the screen
        crosshair.setPosition(new Vector3f(windowWidth/2 - crosshair.getWidth()/2, windowHeight/2 - crosshair.getHeight()/2,0));

        Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, windowWidth, windowHeight, 0);

        // Set orthographic and model matrix for this text item
        Matrix4f projModelMatrix = transformation.getOrtoProjModelMatrix(crosshair, ortho);
        shaderProgram.setUniform("projModelMatrix", projModelMatrix);
        shaderProgram.setUniform("color", crosshair.getMesh().getMaterial().getAmbientColor());

        crosshair.render(null, null, null,null,null);

        shaderProgram.unbind();
    }

}
