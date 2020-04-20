package com.arpg.game.utils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EffectController extends ObjectPool<Effect> {
    @Override
    protected Effect newObject() {
        return new Effect();
    }

    public void setup(float x, float y, int index) {
        getActiveElement().setup(x, y, index);
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < activeList.size(); i++) {
            Effect effect = activeList.get(i);
            batch.draw(effect.getCurrentFrame(), effect.getPosition().x - 30, effect.getPosition().y - 30);
        }
    }

    public void update(float dt) {
        for (int i = 0; i < activeList.size(); i++) {
            activeList.get(i).update(dt);
        }
        checkPool();
    }
}
