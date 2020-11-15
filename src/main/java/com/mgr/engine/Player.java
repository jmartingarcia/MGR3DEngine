package com.mgr.engine;

import com.mgr.engine.collision.BoundingBox;
import org.joml.Vector3f;

public class Player extends GameItem {

    private final float radius = 10.0f; // Used for collision detection

    public Player(){
        super();
    }

    public BoundingBox getBoundingBox()  {
        final BoundingBox box = new BoundingBox();
        box.add(new Vector3f(position.x - radius/2, position.y - radius/2, position.z - radius/2 ));
        box.add(new Vector3f(position.x + radius/2, position.y + radius/2, position.z + radius/2 ));
        return box;
    }

    public BoundingBox getBoundingBoxAtPosition(final Vector3f newPosition)  {
        final BoundingBox box = new BoundingBox();
        box.add(new Vector3f(newPosition.x - radius/2, newPosition.y - radius/2, newPosition.z - radius/2 ));
        box.add(new Vector3f(newPosition.x + radius/2, newPosition.y + radius/2, newPosition.z + radius/2 ));
        return box;
    }
}
