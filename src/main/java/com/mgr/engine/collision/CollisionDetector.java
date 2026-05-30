package com.mgr.engine.collision;

import java.util.List;

public class CollisionDetector {

    public static boolean willItemCollideAgainstAABB(final BoundingBox box, final List<BoundingBox> boxes) {
        for(final BoundingBox aabb : boxes){
            if (box.intersectAABB(aabb))
                return true;
        }
        return false;
    }

}
