package com.arpg.game.map;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface MapElement {
    int getCellX();
    int getCellY();

    void render(SpriteBatch batch, BitmapFont font);
}
