package com.mgr.engine.player;

import com.mgr.engine.Material;
import com.mgr.engine.Mesh;
import com.mgr.engine.Texture;
import com.mgr.engine.Transformation;
import com.mgr.engine.items.GameItem;
import com.mgr.engine.items.IGameItem;
import com.mgr.engine.light.DirectionalLight;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Crosshair extends GameItem implements IGameItem {

    public Crosshair(final Texture texture, final float width, final float height) {
        super();
        this.width = width;
        this.height = height;
        this.depth = 0.0f;
        position = new Vector3f(0f,0f,0f);

        setMesh(new Mesh[] { buildMesh(texture) } );
    }

    private Mesh buildMesh(final Texture texture) {

        // The mesh it's a simple square
        // Two triangles

        float[] vertices = { 0, 0, 0.0f,   // 0
                              width, 0, 0.0f,   // 1
                              width, height, 0.0f,    // 2
                             0, height, 0.0f };  // 3

        int[] indices    = { 0, 3, 1, 2, 1, 3};

        float[] textcoords = {0f, 1f,
                              1f, 1f,
                              1f, 0f,
                              0f, 0f};

        final Mesh mesh = new Mesh();
        mesh.init(vertices, indices, new float[0], textcoords, null, null);
        mesh.setMaterial(new Material(texture));

        return mesh;
    }

    @Override
    public void update(float interval) {

    }

    public void render(final Matrix4f projectionMatrix, final Matrix4f viewMatrix,
                       final Transformation transformation, final Vector3f ambientLight,
                       final DirectionalLight directionalLight) throws NullPointerException {

        for (Mesh value : mesh)
            value.render();
    }
}
