package com.arpg.game.units;

import com.arpg.game.*;
import com.arpg.game.armory.*;
import com.arpg.game.map.Map;
import com.arpg.game.utils.Direction;
import com.arpg.utils.Assets;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class Hero extends Unit {
    private int score;
    private Weapon rangedWeapon;
    private Inventory inventory;
    private Sound soundSwordSwipe;

    public int getScore() {
        return score;
    }

    public boolean isActive() {
        return stats.getHp() > 0;
    }

    public Hero(GameController gameController) {
        super(gameController);
        this.inventory = new Inventory(this);
        this.inventory.add(new Potion("HP Potion", Potion.Type.HP, 15));
        this.inventory.add(new Potion("HP Potion", Potion.Type.HP, 15));
        this.inventory.add(new Potion("HP Potion", Potion.Type.HP, 15));
        this.texture = new TextureRegion(Assets.getInstance().getAtlas().findRegion("Hero")).split(80, 80);
        do {
            this.position.set(MathUtils.random(0, Map.MAP_SIZE_X_PX), MathUtils.random(0, Map.MAP_SIZE_Y_PX));
        } while (!gc.getMap().isCellPassable(position));
        this.area.setPosition(position);
        this.stats = new Stats(1, 1, 1, 50, 1, 1, 10, 320.0f);
        this.weapon = new Weapon("Short Sword", Weapon.Type.MELEE,0.5f, 90.0f, 2, 6);
        this.rangedWeapon = new Weapon("Bow", Weapon.Type.RANGED, 1.0f, 500.0f, 2, 4);
        this.soundSwordSwipe = Gdx.audio.newSound(Gdx.files.internal("audio/swordSwipe.mp3"));
    }

    public void setSafePosition() {
        do {
            changePosition(MathUtils.random(0, Map.MAP_SIZE_X_PX), MathUtils.random(0, Map.MAP_SIZE_Y_PX));
        } while (!gc.getMap().isCellPassable(position));
    }

    @Override
    public void update(float dt) {
        float speedMod = 1.0f;
        attackTime += dt;

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            speedMod = 1.2f;
        }

        if (damageTimer > 0.0f) {
            damageTimer -= dt;
        }

        boolean btnPressed = false;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            direction = Direction.LEFT;
            btnPressed = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            direction = Direction.RIGHT;
            btnPressed = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            direction = Direction.DOWN;
            btnPressed = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            direction = Direction.UP;
            btnPressed = true;
        }

        if (btnPressed) {
            moveForward(dt, speedMod);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            inventory.selectPrev();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            inventory.selectNext();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Item item = inventory.getCurrentInventoryEntry().getItem();
            if (item.isUsable()) {
                inventory.destroyCurrentItem();
                if (item.getItemType() == Item.Type.POTION) {
                    Potion p = (Potion) item;
                    if (p.getType() == Potion.Type.HP) {
                        int restored = stats.restoreHp(p.getPower());
                        gc.getInfoController().setup(position.x, position.y, "HP +" + restored, Color.GREEN);
                    }
                }
            }
            if (item.isWearable()) {
                if (item.getItemType() == Item.Type.WEAPON) {
                    inventory.takeCurrentWeapon();
                }
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.F)) {
            attack();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.G)) {
            rangedAttack();
        }
    }

    public void renderHUD(SpriteBatch batch, BitmapFont font) {
        font.draw(batch, "SCORE: " + score + "\nLEVEL: " + stats.getLevel() + "\nHP: " + stats.getHp() + " / " + stats.getHpMax() + "\nCOINS: " + inventory.getCoins(), 20, 700);
        inventory.render(batch, font);
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
    }

    public void consume(PowerUp p) {
        switch (p.getType()) {
            case COINS:
                int amount = MathUtils.random(1, 3);
                gc.getInfoController().setup(position.x, position.y, "+" + amount + "G", Color.YELLOW);
                inventory.addCoins(amount);
                break;
            case POTION:
                inventory.add(new Potion("HP Potion", Potion.Type.HP, 15));
                break;
            case WEAPON:
                int minDmg = MathUtils.random(1, p.getLevel());
                int maxDmg = MathUtils.random(2, p.getLevel() * 2);
                for (int i = 0; i < 10; i++) {
                    if (MathUtils.random() < 0.03f) {
                        maxDmg += MathUtils.random(0, p.getLevel());
                    }
                }
                if (maxDmg < minDmg) {
                    maxDmg = minDmg;
                }
                inventory.add(new Weapon("Sword", Weapon.Type.MELEE,0.5f, 90.0f, minDmg, maxDmg));
                break;
        }
        p.deactivate();
    }

    public void rangedAttack() {
        if (attackTime > rangedWeapon.getAttackPeriod()) {
            attackTime = 0.0f;
            gc.getProjectileController().setup(this, position.x, position.y + 15, 400.0f, 0, 500.0f, direction.getAngle() + MathUtils.random(-10, 10), rangedWeapon.getDamage());
        }
    }

    public void attack() {
        if (attackTime > weapon.getAttackPeriod()) {
            attackTime = 0.0f;

            Monster nearestMonster = null;
            float minDist = Float.MAX_VALUE;
            for (int i = 0; i < gc.getMonsterController().getActiveList().size(); i++) {
                Monster m = gc.getMonsterController().getActiveList().get(i);
                float dst = position.dst(m.getPosition());
                if (dst < minDist && dst <= weapon.getAttackRange()) {
                    if (direction == Direction.LEFT && position.x > m.getPosition().x ||
                            direction == Direction.RIGHT && position.x < m.getPosition().x ||
                            direction == Direction.UP && position.y < m.getPosition().y ||
                            direction == Direction.DOWN && position.y > m.getPosition().y) {
                        nearestMonster = m;
                        minDist = dst;
                    }
                }
            }

            if (nearestMonster != null) {
                nearestMonster.takeDamage(this, BattleCalc.calculateDamage(this, nearestMonster, weapon.getDamage()), Color.WHITE);
                gc.getEffectController().setup(nearestMonster.getPosition().x, nearestMonster.getPosition().y, 0);
            } else {
                tmp.set(position.x + direction.getX() * 60, position.y + direction.getY() * 60);
                gc.getEffectController().setup(tmp.x, tmp.y, 0);
            }
            soundSwordSwipe.play();
        }
    }

    public void dispose() {
        soundSwordSwipe.dispose();
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }
}
