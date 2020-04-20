package com.arpg.game.units;

import com.arpg.game.*;
import com.arpg.game.armory.Weapon;
import com.arpg.game.utils.Direction;
import com.arpg.game.utils.Poolable;
import com.arpg.utils.Assets;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class Monster extends Unit implements Poolable {
    public enum State {
        HUNT, IDLE, WALK
    }

    private State state;
    private Unit target;
    private String title;
    private float aiTimer;
    private Unit damageTakedFrom;

    public String getTitle() {
        return title;
    }

    @Override
    public boolean isActive() {
        return stats.getHp() > 0;
    }

    public Monster(GameController gameController) {
        super(gameController);
        this.stats = new Stats();
        this.weapon = new Weapon("Bite", Weapon.Type.MELEE, 0.8f, 90.0f, 2, 5);
    }

    // ___title________,__base_att__,__base_def__,__base_hp__,__att_pl__,__def_pl__,__hp_pl__,__speed__
    public Monster(String line) {
        super(null);
        String[] tokens = line.split(",");
        this.title = tokens[0].trim();
        this.texture = new TextureRegion(Assets.getInstance().getAtlas().findRegion(title)).split(80, 80);
        this.stats = new Stats(
                0,
                Integer.parseInt(tokens[1].trim()),
                Integer.parseInt(tokens[2].trim()),
                Integer.parseInt(tokens[3].trim()),
                Integer.parseInt(tokens[4].trim()),
                Integer.parseInt(tokens[5].trim()),
                Integer.parseInt(tokens[6].trim()),
                Float.parseFloat(tokens[7].trim())
        );
        this.weapon = new Weapon("Bite", Weapon.Type.MELEE, 0.8f, 90.0f, 2, 5);
    }

    public void setup(int level, float x, float y, Monster pattern) {
        this.stats.set(level, pattern.stats);
        this.title = pattern.title;
        this.texture = pattern.texture;
        if (x < 0 && y < 0) {
            this.gc.getMap().setRefVectorToEmptyPoint(position);
        } else {
            this.position.set(x, y);
        }
        this.area.setPosition(position);
        if (pattern.getTitle().equals("Tiger")) {
            weapon = new Weapon("Bite", Weapon.Type.MELEE, 0.8f, 90.0f, 2, 5);
        } else {
            weapon = new Weapon("Bow", Weapon.Type.RANGED, 0.6f, 500.0f, 3, 8);
        }
    }

    @Override
    public void update(float dt) {
        stateMachine(dt);
        attackTime += dt;

        if (damageTimer > 0.0f) {
            damageTimer -= dt;
        }


        if (state == State.HUNT && !canIHitTarget()) {
            if (Math.abs(position.x - target.getPosition().x) < 20.0f) {
                if (target.getPosition().y < position.y && direction != Direction.DOWN) {
                    direction = Direction.DOWN;
                }
                if (target.getPosition().y > position.y && direction != Direction.UP) {
                    direction = Direction.UP;
                }
            }
            if (Math.abs(position.y - target.getPosition().y) < 20.0f) {
                if (target.getPosition().x < position.x && direction != Direction.LEFT) {
                    direction = Direction.LEFT;
                }
                if (target.getPosition().x > position.x && direction != Direction.RIGHT) {
                    direction = Direction.RIGHT;
                }
            }
        }

        tryToAttack();

        if (shouldIMove()) {
            moveForward(dt, 1.0f);
        }
    }

    @Override
    public void takeDamage(Unit attacker, int amount, Color color) {
        super.takeDamage(attacker, amount, color);
        damageTakedFrom = attacker;
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        if (damageTimer > 0.0f) {
            batch.setColor(1.0f, 1.0f - damageTimer, 1.0f - damageTimer, 1.0f);
        }
        batch.draw(getCurrentTexture(), position.x - 40, position.y - 20);
        if (stats.getHp() < stats.getHpMax()) {
            batch.setColor(1.0f, 1.0f, 1.0f, 0.9f);
            batch.draw(hpTexture, position.x - 40, position.y + 40, 80 * ((float) stats.getHp() / stats.getHpMax()), 12);
        }
        batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        font.draw(batch, "" + stats.getLevel() + ". " + state.name(), position.x - 40, position.y + 52, 80,1,false);
    }

    public boolean shouldIMove() {
        if (state == State.HUNT && canIHitTarget()) {
            return false;
        }
        return true;
    }

    public void stateMachine(float dt) {
        aiTimer -= dt;

        if (aiTimer <= 0.0f) {
            aiTimer = MathUtils.random(2.0f, 4.0f);
            direction = Direction.values()[MathUtils.random(0, 3)];
            state = State.values()[MathUtils.random(0, 1)];
            if (state == State.IDLE) {
                aiTimer /= 4.0f;
            }
        }

        if (target != null && !target.isAlive()) {
            target = null;
        }

        if (state == State.HUNT && target == null) {
            state = State.WALK;
            aiTimer = 1.0f;
        }

        if (damageTakedFrom != null && damageTakedFrom.isAlive()) {
            if (target == null && MathUtils.random(0, 100) < 20) {
                this.state = State.HUNT;
                this.target = damageTakedFrom;
                this.aiTimer = 30.0f;
            }
            damageTakedFrom = null;
        }
    }

    public boolean canIHitTarget() {
        if (target == null) {
            return false;
        }
        if (position.dst(target.getPosition()) < weapon.getAttackRange()) {
            if (direction == Direction.LEFT && position.x > target.getPosition().x ||
                    direction == Direction.RIGHT && position.x < target.getPosition().x ||
                    direction == Direction.UP && position.y < target.getPosition().y ||
                    direction == Direction.DOWN && position.y > target.getPosition().y
            ) {
                return true;
            }
        }
        return false;
    }

    public void tryToAttack() {
        if (attackTime > weapon.getAttackPeriod()) {
            attackTime = 0.0f;

            if (weapon.getType() == Weapon.Type.MELEE) {
                if (canIHitTarget()) {
                    gc.getEffectController().setup(target.getPosition().x, target.getPosition().y, 1);
                    target.takeDamage(this, BattleCalc.calculateDamage(this, target, weapon.getDamage()), Color.WHITE);
                }
            }

            if (weapon.getType() == Weapon.Type.RANGED) {
                if (target == null) {
                    gc.getProjectileController().setup(this, position.x, position.y + 15, 400.0f, 0, weapon.getAttackRange(), direction.getAngle() + MathUtils.random(-5, 5), weapon.getDamage());
                } else {
                    if (canIHitTarget()) {
                        float angle = (float) Math.toDegrees(Math.atan2(-position.y + target.getPosition().y, -position.x + target.getPosition().x));
                        gc.getProjectileController().setup(this, position.x, position.y + 15, 400.0f, 0, weapon.getAttackRange(), angle + MathUtils.random(-5, 5), weapon.getDamage());
                    }
                }
            }
        }
    }
}
