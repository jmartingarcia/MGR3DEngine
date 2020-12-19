package com.mgr.engine.items;

import com.mgr.engine.Camera;
import com.mgr.engine.Material;
import com.mgr.engine.Mesh;
import com.mgr.engine.collision.BoundingBox;
import org.joml.Vector3f;

public interface IGameItem {
        float getWidth();
        float getHeight();
        float getDepth();
        Vector3f getPosition();
        void setPosition(final Vector3f position);
        float getScale();
        void setScale(float scale);
        void setRadius(float radius);
        float getRadius();
        Material getMaterial();
        Vector3f getRotation();
        void setRotation(final Vector3f rotation);
        Mesh getMesh();
        void setMesh(Mesh mesh);
        float getSpeed();
        void setSpeed(final float speed);
        float getRota_speed();
        void setRota_speed(final float rota_speed);
        Vector3f getUpvector();
        void setUpvector(final Vector3f upvector);
        Camera getCamera();
        void render();
        void cleanUp();
        Vector3f getNextPosition(final float elapsedSeconds, final Vector3f direction);
        Vector3f getPositionOffset(final float elapsedSeconds, final Vector3f direction);
        BoundingBox getBoundingBox();
        BoundingBox getBoundingBoxAtPosition(final Vector3f newPosition);
        int getNumberVertices();
        int getNumberIndices();
        void update(final float interval);
}
