package com.arpg.game.armory;


import com.arpg.game.units.Unit;
import com.arpg.game.utils.ObjectPool;

public class ProjectileController extends ObjectPool<Projectile> {
    @Override
    protected Projectile newObject() {
        return new Projectile();
    }

    public void update(float dt) {
        for (int i = 0; i < activeList.size(); i++) {
            activeList.get(i).update(dt);
        }
        checkPool();
    }

    public void setup(Unit unit, float x, float y, float speed, int type, float maxRange, float angle, int damage) {
        getActiveElement().setup(unit, x, y, (float)Math.cos(Math.toRadians(angle)), (float)Math.sin(Math.toRadians(angle)), speed, type, maxRange, angle, damage);
    }
}
