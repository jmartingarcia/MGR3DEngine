package com.mgr.engine.items;

import com.mgr.engine.Texture;

public class GameItemProperties {

    private Texture itemTexture;
    private float width;
    private float height;

    public Texture getItemTexture() {
        return itemTexture;
    }

    public void setItemTexture(Texture itemTexture) {
        this.itemTexture = itemTexture;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

}
