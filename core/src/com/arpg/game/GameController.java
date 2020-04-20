package com.arpg.game;

import com.arpg.game.armory.*;
import com.arpg.game.map.Map;
import com.arpg.game.units.*;
import com.arpg.game.utils.EffectController;
import com.arpg.game.utils.InfoController;
import com.arpg.screens.ScreenManager;
import com.arpg.utils.Assets;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class GameController {
    private Map map;
    private Hero hero;
    private Bestiary bestiary;
    private PowerUpsController powerUpsController;
    private MonsterController monsterController;
    private InfoController infoController;
    private EffectController effectController;
    private ProjectileController projectileController;
    private Vector2 mouse;
    private Vector2 tmp;
    private int gameLevel;
    private float gameTimer;
    private float gameLevelTimer;
    private Stage stage;
    private boolean paused;

    private static int RULE_MONSTER_PER_WAVE = 10;
    private static float LEVEL_DURATION = 60.0f;

    public MonsterController getMonsterController() {
        return monsterController;
    }

    public PowerUpsController getPowerUpsController() {
        return powerUpsController;
    }

    public EffectController getEffectController() {
        return effectController;
    }

    public Bestiary getBestiary() {
        return bestiary;
    }

    public InfoController getInfoController() {
        return infoController;
    }

    public ProjectileController getProjectileController() {
        return projectileController;
    }

    public Stage getStage() {
        return stage;
    }

    public Map getMap() {
        return map;
    }

    public Hero getHero() {
        return hero;
    }

    public int getGameLevel() {
        return gameLevel;
    }

    public float getGameTimer() {
        return gameTimer;
    }

    public GameController(SpriteBatch batch) {
        this.map = new Map();
        this.hero = new Hero(this);
        this.bestiary = new Bestiary();
        this.powerUpsController = new PowerUpsController();
        this.monsterController = new MonsterController(this);
        this.projectileController = new ProjectileController();
        this.gameLevel = 1;
        for (int i = 0; i < 25; i++) {
            this.monsterController.setup(MathUtils.random(gameLevel, gameLevel + 2));
        }
        this.infoController = new InfoController();
        this.effectController = new EffectController();
        this.mouse = new Vector2(0.0f, 0.0f);
        this.tmp = new Vector2(0.0f, 0.0f);
        this.createGUI(batch);
    }

    public void gameUpdate(float dt) {
        gameLevelTimer += dt;
        gameTimer += dt;
        if (gameLevelTimer > LEVEL_DURATION) {
            gameLevelTimer = 0.0f;
            gameLevel++;
            int monsterSpawnMinLevel = 1 + gameLevel / 2;
            for (int i = 0; i < RULE_MONSTER_PER_WAVE; i++) {
                this.monsterController.setup(MathUtils.random(monsterSpawnMinLevel, monsterSpawnMinLevel + 3));
            }
        }
    }

    public void update(float dt) {
        if (!paused) {
            if (map.isOut(hero.getPosition())) {
                monsterController.freeAll();
                infoController.freeAll();
                projectileController.freeAll();
                powerUpsController.freeAll();
                map = new Map();
                gameLevel += 3;
                for (int i = 0; i < 5; i++) {
                    monsterController.setup(MathUtils.random(gameLevel, gameLevel + 2));
                }
                hero.setSafePosition();
            }

            gameUpdate(dt);
            mouse.set(Gdx.input.getX(), Gdx.input.getY());
            ScreenManager.getInstance().getViewport().unproject(mouse);
            if (hero.isActive()) {
                hero.update(dt);
            } else {
                ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.GAME_OVER, hero);
            }
            monsterController.update(dt);

            for (int i = 0; i < monsterController.getActiveList().size(); i++) {
                Unit m = monsterController.getActiveList().get(i);
                collideUnits(hero, m);
            }
            for (int i = 0; i < monsterController.getActiveList().size() - 1; i++) {
                Unit u1 = monsterController.getActiveList().get(i);
                for (int j = i + 1; j < monsterController.getActiveList().size(); j++) {
                    Unit u2 = monsterController.getActiveList().get(j);
                    collideUnits(u1, u2);
                }
            }

            for (int i = 0; i < projectileController.getActiveList().size(); i++) {
                Projectile p = projectileController.getActiveList().get(i);

                if (!map.isCellPassableForProjectiles(p.getPosition())) {
                    p.deactivate();
                    continue;
                }

                for (int j = 0; j < monsterController.getActiveList().size(); j++) {
                    Monster m = monsterController.getActiveList().get(j);
                    if (p.getUnit() == m) {
                        continue;
                    }
                    if (m.getArea().contains(p.getPosition())) {
                        p.deactivate();
                        m.takeDamage(p.getUnit(), BattleCalc.calculateDamage(p.getUnit(), m, p.getDamage()), Color.WHITE);
                    }
                }
                if (p.getUnit() != hero) {
                    if (hero.getArea().contains(p.getPosition())) {
                        p.deactivate();
                        hero.takeDamage(p.getUnit(), BattleCalc.calculateDamage(p.getUnit(), hero, p.getDamage()), Color.WHITE);
                    }
                }
            }

            for (int i = 0; i < powerUpsController.getActiveList().size(); i++) {
                PowerUp p = powerUpsController.getActiveList().get(i);
                if (hero.getArea().contains(p.getPosition())) {
                    hero.consume(p);
                }
            }

            projectileController.update(dt);
            powerUpsController.update(dt);
            effectController.update(dt);
            infoController.update(dt);
        }
        stage.act(dt);
    }

    public void collideUnits(Unit u1, Unit u2) {
        if (u1.getArea().overlaps(u2.getArea())) {
            tmp.set(u1.getArea().x, u1.getArea().y);
            tmp.sub(u2.getArea().x, u2.getArea().y);
            float halfInterLen = ((u1.getArea().radius + u2.getArea().radius) - tmp.len()) / 2.0f;
            tmp.nor();

            u1.getPosition().mulAdd(tmp, halfInterLen);
            u2.getPosition().mulAdd(tmp, -halfInterLen);

            if (!map.isCellPassable(u1.getPosition())) {
                u1.getPosition().mulAdd(tmp, -halfInterLen);
            }
            if (!map.isCellPassable(u2.getPosition())) {
                u2.getPosition().mulAdd(tmp, halfInterLen);
            }

            u1.getArea().setPosition(u1.getPosition());
            u2.getArea().setPosition(u2.getPosition());
        }
    }

    public void dispose() {
        hero.dispose();
    }

    public void createGUI(SpriteBatch batch) {
        this.stage = new Stage(ScreenManager.getInstance().getViewport(), batch);
        Gdx.input.setInputProcessor(stage);
        Skin skin = new Skin();
        skin.addRegions(Assets.getInstance().getAtlas());
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.getDrawable("smButton");
        textButtonStyle.font = Assets.getInstance().getAssetManager().get("fonts/font20.ttf");
        skin.add("smallButtonStyle", textButtonStyle);
        Button btnPauseGame = new TextButton("Pause", skin, "smallButtonStyle");
        Button btnToMenu = new TextButton("Menu", skin, "smallButtonStyle");
        Group menuGroup = new Group();
        menuGroup.addActor(btnPauseGame);
        menuGroup.addActor(btnToMenu);
        btnPauseGame.setPosition(0, 0);
        btnToMenu.setPosition(140, 0);

        btnPauseGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                paused = !paused;
            }
        });
        btnToMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.MENU);
            }
        });

        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            Group controlGroup = new Group();
            Button buttonLeft = new TextButton("L", skin, "smallButtonStyle");
            Button buttonRight = new TextButton("R", skin, "smallButtonStyle");
            buttonLeft.setPosition(0, 0);
            buttonRight.setPosition(140, 0);
            controlGroup.addActor(buttonLeft);
            controlGroup.addActor(buttonRight);

            buttonLeft.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    hero.getPosition().add(-10, 0);
                }
            });
            buttonRight.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    hero.getPosition().add(10, 0);
                }
            });

            controlGroup.setPosition(100, 100);
            stage.addActor(controlGroup);
        }

        menuGroup.setPosition(980, 660);
        stage.addActor(menuGroup);
        skin.dispose();
    }
}
