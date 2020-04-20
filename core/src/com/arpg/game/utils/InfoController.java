package com.arpg.game.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class InfoController extends ObjectPool<InfoText> {
    @Override
    protected InfoText newObject() {
        return new InfoText();
    }

    public void setup(float x, float y, String text, Color color) {
        InfoText infoText = getActiveElement();
        infoText.setup(x, y, text, color);
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        for (int i = 0; i < activeList.size(); i++) {
            InfoText infoText = activeList.get(i);
            font.setColor(infoText.getColor());
            font.draw(batch, infoText.getText(), infoText.getPosition().x, infoText.getPosition().y);
        }
        font.setColor(Color.WHITE);
    }

    public void update(float dt) {
        for (int i = 0; i < activeList.size(); i++) {
            InfoText infoText = activeList.get(i);
            infoText.update(dt);
        }
        checkPool();
    }
}
