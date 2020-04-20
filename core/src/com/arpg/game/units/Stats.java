package com.arpg.game.units;

public class Stats {
    private int level;

    private int att;
    private int def;
    private int hpMax;

    private int attBase;
    private int defBase;
    private int hpMaxBase;

    private int attPL;
    private int defPL;
    private int hpMaxPL;

    private float speed;

    private int hp;
    private int exp;
    private int[] expTo = {1_000, 2_000, 4_000, 8_000, 16_000, 32_000, 80_000, 150_000, 200_000, 400_000, 800_000, 1_600_000, 3_200_000};

    public int getAtt() {
        return att;
    }

    public int getDef() {
        return def;
    }

    public int getLevel() {
        return level;
    }

    public float getSpeed() {
        return speed;
    }

    public int getHp() {
        return hp;
    }

    public int getHpMax() {
        return hpMax;
    }

    public Stats() {
    }

    public Stats(int level, int attBase, int defBase, int hpMaxBase, int attPL, int defPL, int hpMaxPL, float speed) {
        this.level = level;
        this.attBase = attBase;
        this.defBase = defBase;
        this.hpMaxBase = hpMaxBase;
        this.attPL = attPL;
        this.defPL = defPL;
        this.hpMaxPL = hpMaxPL;
        this.speed = speed;
        this.calculate();
        this.fillHp();
    }

    public void set(int level, Stats stats) {
        this.level = level;
        this.attBase = stats.attBase;
        this.defBase = stats.defBase;
        this.hpMaxBase = stats.hpMaxBase;
        this.attPL = stats.attPL;
        this.defPL = stats.defPL;
        this.hpMaxPL = stats.hpMaxPL;
        this.speed = stats.speed;
        this.calculate();
        this.fillHp();
    }

    public void decreaseHp(int amount) {
        hp -= amount;
    }

    public void fillHp() {
        hp = hpMax;
    }

    public int restoreHp(int amount) {
        int hp0 = hp;
        hp += amount;
        if (hp > hpMax) {
            hp = hpMax;
        }
        return hp - hp0;
    }

    public void addExp(int amount) {
        exp += amount;
        if (exp >= expTo[level - 1]) {
            exp = 0;
            level++;
            calculate();
            fillHp();
        }
    }

    public void calculate() {
        att = attBase + level * attPL;
        def = defBase + level * defPL;
        hpMax = hpMaxBase + level * hpMaxPL;
    }
}
