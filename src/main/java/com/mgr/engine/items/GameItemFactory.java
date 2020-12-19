package com.mgr.engine.items;

import com.mgr.engine.player.Crosshair;

import javax.management.AttributeNotFoundException;
import java.security.InvalidParameterException;


public class GameItemFactory {

    public static IGameItem createItem(final GameItemType itemType, final GameItemProperties itemProperties) throws AttributeNotFoundException, InvalidParameterException {

        if (itemType == GameItemType.CROSSHAIR)
            return createCrosshair(itemProperties);
        else
            throw new InvalidParameterException("itemType value is invalid");

    }

    private static IGameItem createCrosshair(final GameItemProperties itemProperties) throws AttributeNotFoundException {

        if (itemProperties.getItemTexture() == null)
            throw new AttributeNotFoundException("Texture is required for Crosshair item");

        if (itemProperties.getWidth() == 0)
            throw new AttributeNotFoundException("Width is required for Crosshair item");

        if (itemProperties.getHeight() == 0)
            throw new AttributeNotFoundException("Height is required for Crosshair item");

        return new Crosshair(itemProperties.getItemTexture(), itemProperties.getWidth(), itemProperties.getHeight());
    }
}
