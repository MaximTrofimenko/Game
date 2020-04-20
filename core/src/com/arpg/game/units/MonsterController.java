package com.arpg.game.units;

import com.arpg.game.GameController;
import com.arpg.game.utils.ObjectPool;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class MonsterController extends ObjectPool<Monster> {
    private GameController gc;

    @Override
    protected Monster newObject() {
        return new Monster(gc);
    }

    public MonsterController(GameController gc) {
        this.gc = gc;
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        for (int i = 0; i < activeList.size(); i++) {
            activeList.get(i).render(batch, font);
        }
    }

    private String[] types = {"Tiger", "Bomber"};

    public void setup(int level) {
        int currentLevel = MathUtils.random(level, level + 2);
        getActiveElement().setup(currentLevel, -1, -1, gc.getBestiary().getPatternFromTitle(types[MathUtils.random(0, 1)]));
    }

    public void update(float dt) {
        for (int i = 0; i < activeList.size(); i++) {
            activeList.get(i).update(dt);
        }
        checkPool();
    }
}
