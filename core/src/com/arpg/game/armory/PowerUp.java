package com.arpg.game.armory;

import com.arpg.game.utils.Poolable;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class PowerUp implements Poolable {
    public enum Type {
        POTION(0), COINS(1), WEAPON(2), ARMOR(3);

        int index;

        Type(int index) {
            this.index = index;
        }
    }

    private Type type;
    private int level;
    private boolean active;
    private Vector2 position;
    private Vector2 velocity;

    @Override
    public boolean isActive() {
        return active;
    }

    public int getLevel() {
        return level;
    }

    public Type getType() {
        return type;
    }

    public void deactivate() {
        active = false;
    }

    public Vector2 getPosition() {
        return position;
    }

    public PowerUp() {
        this.active = false;
        this.position = new Vector2(0.0f, 0.0f);
        this.velocity = new Vector2(0.0f, 0.0f);
    }

    public void setup(float x, float y, int level) {
        this.level = level;
        this.position.set(x, y);
        this.velocity.set(MathUtils.random(-60.0f, 60.0f), MathUtils.random(150.0f, 250.0f));
        this.type = Type.values()[MathUtils.random(0, 2)];
        this.active = true;
    }

    public void update(float dt) {
        if (velocity.y > -80.0f) {
            position.mulAdd(velocity, dt);
            velocity.y -= 120.0f * dt;
        }
    }
}
