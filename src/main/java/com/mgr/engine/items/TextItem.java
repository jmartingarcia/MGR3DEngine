package com.mgr.engine.items;

import com.mgr.engine.*;
import com.mgr.engine.light.DirectionalLight;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class TextItem extends GameItem implements IGameItem {
    private static final float ZPOS = 0.0f;
    private static final int VERTICES_PER_QUAD = 4;
    private String text;
    private final int numCols;
    private final int numRows;
    private final Texture texture;

    public TextItem(final String text, final String fontFileName, final int numCols, final int numRows) throws Exception {
        super();
        this.text = text;
        this.numCols = numCols;
        this.numRows = numRows;
        texture = new Texture(fontFileName);
        this.setMesh(new Mesh[] { buildMesh(texture, numCols, numRows) });
        setScale(0.3f);
    }

    private Mesh buildMesh(final Texture texture, final int numCols, final int numRows) {

        final byte[] chars = text.getBytes(ISO_8859_1);
        final int numChars = chars.length;

        final List<Float> positions = new ArrayList<>();
        final List<Float> textCoords = new ArrayList<>();
        final List<Integer> indices = new ArrayList<>();

        final float tileWidth = (float) texture.getWidth() / (float) numCols;
        final float tileHeight = (float) texture.getHeight() / (float) numRows;

        for (int i = 0; i < numChars; i++) {
            final byte currChar = chars[i];
            final int col = currChar % numCols;
            final int row = (currChar / numCols);

            //Left top vertex
            positions.add((float) i * tileWidth); //X
            positions.add(0.0f);//Y
            positions.add(ZPOS);
            textCoords.add((float)col/(float)numCols);
            textCoords.add((float)row/(float)numRows);
            indices.add( i * VERTICES_PER_QUAD );

            // Left Bottom vertex
            positions.add((float) i * tileWidth); //X
            positions.add(tileHeight);//Y
            positions.add(ZPOS);
            textCoords.add((float)col/(float)numCols);
            textCoords.add((float)(row + 1)/(float)numRows);
            indices.add( i * VERTICES_PER_QUAD + 1);

            // Right Bottom vertex
            positions.add((float) i * tileWidth + tileWidth); //X
            positions.add(tileHeight);//Y
            positions.add(ZPOS);
            textCoords.add((float)(col + 1)/(float)numCols);
            textCoords.add((float)(row + 1)/(float)numRows);
            indices.add( i * VERTICES_PER_QUAD + 2);

            // Right Top vertex
            positions.add((float) i * tileWidth + tileWidth); //X
            positions.add(0.0f);//Y
            positions.add(ZPOS);
            textCoords.add((float)(col + 1)/(float)numCols);
            textCoords.add((float)row /(float)numRows);
            indices.add( i * VERTICES_PER_QUAD + 3);

            indices.add( i * VERTICES_PER_QUAD );
            indices.add( i * VERTICES_PER_QUAD + 2);

        }

        final float[] posArr        = Utils.listFloatsToArray(positions);
        final float[] textCoordsArr = Utils.listFloatsToArray(textCoords);
        final int[] indicesArr      = Utils.listIntegersToArray(indices);
        final Mesh mesh             = new Mesh(); // We don't need normals for the text
        mesh.init(posArr, indicesArr, new float[0], textCoordsArr, null, null);
        mesh.setMaterial(new Material(texture));

        return mesh;
    }

    public String getText() {

        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.getMesh().cleanUp();
        this.setMesh(new Mesh[] { buildMesh(texture, numCols, numRows) });
    }

    public void cleanUp() {
        super.cleanUp();
        texture.cleanup();
    }

    public void update(final float interval) {
        return;
    }

    public void render(final Matrix4f projectionMatrix, final Matrix4f viewMatrix,
                       final Transformation transformation, final Vector3f ambientLight,
                       final DirectionalLight directionalLight) throws NullPointerException {

        for (Mesh value : mesh)
            value.render();
    }

}