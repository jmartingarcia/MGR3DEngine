package com.mgr.engine;

import com.mgr.engine.items.GameItem;
import org.joml.Vector3f;


public class Player extends GameItem {

    public Player(){

        super();
        setRadius(1.0f);
        width = 2.0f;
        height = 2.0f;
        depth = 2.0f;
    }

    private void move(final float elapsedSeconds, final Vector3f direction){
        camera.movePositionBy(getPositionOffset(elapsedSeconds, direction));
        position = camera.getPosition(); // The camera movePositionBy takes into account the rotation
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

}
