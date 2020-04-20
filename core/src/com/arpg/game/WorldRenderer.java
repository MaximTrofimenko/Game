package com.arpg.game;

import com.arpg.game.armory.Projectile;
import com.arpg.game.map.Map;
import com.arpg.game.map.MapElement;
import com.arpg.game.units.Unit;
import com.arpg.screens.ScreenManager;
import com.arpg.utils.Assets;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class WorldRenderer {
    private GameController gc;
    private SpriteBatch batch;
    private BitmapFont font14;
    private BitmapFont font20;
    private Camera camera;
    private Vector2 pov;
    private List<MapElement>[] drawables;
    private float worldTimer;

    private FrameBuffer frameBuffer;
    private TextureRegion frameBufferRegion;
    private ShaderProgram shaderProgram;

    public WorldRenderer(GameController gameController, SpriteBatch batch) {
        this.gc = gameController;
        this.font14 = Assets.getInstance().getAssetManager().get("fonts/font14.ttf");
        this.font20 = Assets.getInstance().getAssetManager().get("fonts/font20.ttf");
        this.batch = batch;
        this.camera = ScreenManager.getInstance().getCamera();
        this.pov = new Vector2(0.0f, 0.0f);
        this.drawables = new ArrayList[gc.getMap().MAP_SIZE_Y + 10];

        for (int i = 0; i < drawables.length; i++) {
            drawables[i] = new ArrayList<>();
        }

        this.frameBuffer = new FrameBuffer(Pixmap.Format.RGB888, 1280, 720, false);
        this.frameBufferRegion = new TextureRegion(frameBuffer.getColorBufferTexture());
        this.frameBufferRegion.flip(false, true);
        this.shaderProgram = new ShaderProgram(Gdx.files.internal("shaders/vertex.glsl").readString(), Gdx.files.internal("shaders/fragment.glsl").readString());
        if (!shaderProgram.isCompiled()) {
            throw new IllegalArgumentException("Error compiling shader: " + shaderProgram.getLog());
        }
    }

    public void update(float dt) {
        worldTimer += dt;
    }

    public void render() {
        pov.set(gc.getHero().getPosition());
        if (pov.x - ScreenManager.HALF_WORLD_WIDTH < 0.0f) {
            pov.x = ScreenManager.HALF_WORLD_WIDTH;
        }
        if (pov.y - ScreenManager.HALF_WORLD_HEIGHT < 0.0f) {
            pov.y = ScreenManager.HALF_WORLD_HEIGHT;
        }
        if (pov.x > Map.MAP_SIZE_X_PX - ScreenManager.HALF_WORLD_WIDTH) {
            pov.x = Map.MAP_SIZE_X_PX - ScreenManager.HALF_WORLD_WIDTH;
        }
        if (pov.y > Map.MAP_SIZE_Y_PX - ScreenManager.HALF_WORLD_HEIGHT) {
            pov.y = Map.MAP_SIZE_Y_PX - ScreenManager.HALF_WORLD_HEIGHT;
        }

        camera.position.set(pov, 0.0f);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        int left = (int) (pov.x - ScreenManager.HALF_WORLD_WIDTH - 80) / 80;
        if (left < 0) {
            left = 0;
        }
        int right = (int) (pov.x + ScreenManager.HALF_WORLD_WIDTH + 80) / 80;
        if (right > Map.MAP_SIZE_X) {
            right = Map.MAP_SIZE_X;
        }

        int bottom = (int) (pov.y - ScreenManager.HALF_WORLD_HEIGHT - 80) / 80;
        if (bottom < 0) {
            bottom = 0;
        }
        int top = (int) (pov.y + ScreenManager.HALF_WORLD_HEIGHT + 80) / 80;
        if (top > Map.MAP_SIZE_Y) {
            top = Map.MAP_SIZE_Y;
        }

        for (int i = 0; i < drawables.length; i++) {
            drawables[i].clear();
        }

        for (int i = 0; i < gc.getMonsterController().getActiveList().size(); i++) {
            Unit unit = gc.getMonsterController().getActiveList().get(i);
            int cx = unit.getCellX();
            int cy = unit.getCellY();
            if (cx >= left && cx < right && cy >= bottom && cy < top) {
                drawables[cy - bottom].add(unit);
            }
        }
        {
            int cx = gc.getHero().getCellX();
            int cy = gc.getHero().getCellY();
            if (cx >= left && cx < right && cy >= bottom && cy < top) {
                drawables[cy - bottom].add(gc.getHero());
            }
        }

        for (int i = 0; i < gc.getProjectileController().getActiveList().size(); i++) {
            Projectile p = gc.getProjectileController().getActiveList().get(i);
            int cx = p.getCellX();
            int cy = p.getCellY();
            if (cx >= left && cx < right && cy >= bottom && cy < top) {
                drawables[cy - bottom].add(p);
            }
        }

        frameBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        for (int y = top - 1; y >= bottom; y--) {
            for (int x = left; x < right; x++) {
                gc.getMap().renderGround(batch, x, y);
            }
        }

        for (int y = top - 1; y >= bottom; y--) {
            for (int x = left; x < right; x++) {
                for (int i = 0; i < drawables[y - bottom].size(); i++) {
                    drawables[y - bottom].get(i).render(batch, font14);
                }
            }
            for (int x = left; x < right; x++) {
                gc.getMap().renderWalls(batch, x, y);
            }
        }
        gc.getEffectController().render(batch);
        gc.getPowerUpsController().render(batch);
        gc.getInfoController().render(batch, font20);
        batch.end();
        frameBuffer.end();

        camera.position.set(ScreenManager.HALF_WORLD_WIDTH, ScreenManager.HALF_WORLD_HEIGHT, 0.0f);
        camera.update();
        ScreenManager.getInstance().getViewport().apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.setShader(shaderProgram);
        shaderProgram.setUniformf(shaderProgram.getUniformLocation("time"), worldTimer);
        shaderProgram.setUniformf(shaderProgram.getUniformLocation("px"), pov.x / 1280.0f);
        shaderProgram.setUniformf(shaderProgram.getUniformLocation("py"), pov.y / 720.0f);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.draw(frameBufferRegion, 0, 0);
        batch.end();
        batch.setShader(null);

        batch.begin();
        gc.getHero().renderHUD(batch, font20);
        font20.draw(batch, "Game Level: " + gc.getGameLevel() + "   Game Time: " + (int) gc.getGameTimer(), 0, 700, 1280, 1, false);
        batch.end();
        gc.getStage().draw();
    }
}