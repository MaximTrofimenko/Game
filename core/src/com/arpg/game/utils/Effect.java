package com.arpg.game.utils;

import com.arpg.utils.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Effect implements Poolable {
    private TextureRegion[][] texture;
    private boolean active;
    private Vector2 position;
    private float time;
    private float maxTime;
    private float timePerFrame;
    private int index;

    @Override
    public boolean isActive() {
        return active;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Effect() {
        this.active = false;
        this.texture = new TextureRegion(Assets.getInstance().getAtlas().findRegion("wanim")).split(60, 60);
        this.position = new Vector2(0.0f, 0.0f);
        this.timePerFrame = 0.05f;
    }

    public void setup(float x, float y, int index) {
        this.position.set(x, y);
        this.active = true;
        this.time = 0.0f;
        this.index = index;
        this.maxTime = texture[index].length * timePerFrame;
    }

    public TextureRegion getCurrentFrame() {
        return texture[index][(int)(time / timePerFrame)];
    }

    public void update(float dt) {
        time += dt;
        if (time >= maxTime) {
            active = false;
        }
    }
}
