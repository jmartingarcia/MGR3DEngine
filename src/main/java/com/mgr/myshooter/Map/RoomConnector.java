package com.mgr.myshooter.Map;

import com.mgr.engine.Material;


import javax.management.InvalidAttributeValueException;
import java.util.Map;

/*
The RoomConnector is a tunnel that connects to rooms. Is basically a room with no front and back walls.
 */
public class RoomConnector extends Room {

    private final WallOrientation destRoomDirection;

    public RoomConnector(final Integer index, final float scale,
                final int maxPointLights, final int maxSpotLights,
                final int roomWidth, final int roomHeight, final int roomDepth,
                final int doorWidth, final Map<WallOrientation, Material> materialsMap,
                final WallOrientation destRoomDirection){

        super(index, scale, maxPointLights, maxSpotLights, roomWidth, roomHeight, roomDepth, doorWidth, materialsMap);

        this.destRoomDirection = destRoomDirection;
    }

    public void init() throws InvalidAttributeValueException {

        for (final WallOrientation dir : WallOrientation.values()){
            if ((destRoomDirection == WallOrientation.BACK || destRoomDirection == WallOrientation.FRONT) &&
                    (dir == WallOrientation.BACK || dir == WallOrientation.FRONT)) continue;
            else if ((destRoomDirection == WallOrientation.LEFT || destRoomDirection == WallOrientation.RIGHT) &&
                    (dir == WallOrientation.LEFT || dir == WallOrientation.RIGHT)) continue;
            walls.put(dir,getWallWithNoDoor(dir));
        }
    }

}
