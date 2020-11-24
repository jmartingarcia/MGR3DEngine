package com.mgr.engine.player;

import com.mgr.engine.Material;
import com.mgr.engine.Mesh;
import com.mgr.engine.Texture;
import com.mgr.engine.items.GameItem;
import org.joml.Vector3f;

public class Crosshair extends GameItem {

    public Crosshair(final Texture texture, final float width, final float height) {
        super();
        this.width = width;
        this.height = height;
        this.depth = 0.0f;
        position = new Vector3f(0f,0f,0f);

        setMesh(buildMesh(texture));
    }

    private Mesh buildMesh(final Texture texture) {

        // The mesh it's a simple square
        // Two triangles

        float[] vertices = { -width/2, -height/2, 0.0f,   // 0
                              width/2, -height/2, 0.0f,   // 1
                             -width/2, height/2, 0.0f,    // 2
                              width/2, height/2, 0.0f };  // 3

        int[] indices    = { 1, 0, 2, 1, 2, 3};

        float[] textcoords = {1f, 1f,
                              0f, 1f,
                              0f, 0f,
                              1f, 0f};

        final Mesh mesh = new Mesh();
        mesh.init(vertices, indices, new float[0], textcoords);
        mesh.setMaterial(new Material(texture));

        return mesh;
    }

    @Override
    public void update(float interval) {

    }
}
