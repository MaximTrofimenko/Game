package com.arpg.game.armory;

import com.arpg.game.map.MapElement;
import com.arpg.game.units.Unit;
import com.arpg.game.utils.Poolable;
import com.arpg.utils.Assets;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Projectile implements Poolable, MapElement {
    private Unit unit;
    private TextureRegion texture;
    private Vector2 position;
    private Vector2 dir;
    private float range;
    private int damage;
    private float maxRange;
    private float speed;
    private float angle;
    private int type;
    private boolean active;

    @Override
    public int getCellX() {
        return (int) (position.x / 80);
    }

    @Override
    public int getCellY() {
        return (int) (position.y / 80);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public Unit getUnit() {
        return unit;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getAngle() {
        return angle;
    }

    public int getDamage() {
        return damage;
    }

    public int getType() {
        return type;
    }

    public Projectile() {
        this.position = new Vector2(0, 0);
        this.dir = new Vector2(0, 0);
        this.texture = Assets.getInstance().getAtlas().findRegion("arrow");
    }

    public void deactivate() {
        active = false;
    }

    public void setup(Unit unit, float x, float y, float vx, float vy, float speed, int type, float maxRange, float angle, int damage) {
        this.unit = unit;
        this.position.set(x, y);
        this.dir.set(vx, vy);
        this.speed = speed;
        this.type = type;
        this.maxRange = maxRange;
        this.range = 0.0f;
        this.angle = angle;
        this.damage = damage;
        this.active = true;
    }

    @Override
    public void render(SpriteBatch batch, BitmapFont font) {
        batch.draw(texture, position.x - 30, position.y - 30, 30, 30, 60, 60, 1, 1, angle);
    }

    public void update(float dt) {
        position.mulAdd(dir, speed * dt);
        range += speed * dt;
        if (range > maxRange) {
            active = false;
        }
    }
}
