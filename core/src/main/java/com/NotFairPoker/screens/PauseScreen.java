package com.NotFairPoker.screens;

import com.NotFairPoker.Main;
import com.NotFairPoker.audio.MusicManager;
import com.NotFairPoker.audio.SoundManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class PauseScreen implements Screen {

    private final Main game;
    private final TableScreen tableScreen;

    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;

    private Texture bgTex, resumeTex, newGameTex, menuTex, exitTex;
    private Texture soundOnTex, soundOffTex, musicOnTex, musicOffTex;

    private ImageButton soundButton, musicButton;

    public PauseScreen(Main game, TableScreen tableScreen) {
        this.game = game;
        this.tableScreen = tableScreen;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Wczytanie grafik
        bgTex = new Texture(Gdx.files.internal("pause_bg.png"));
        resumeTex = new Texture(Gdx.files.internal("buttons/resume.png"));
        newGameTex = new Texture(Gdx.files.internal("buttons/newgame.png"));
        menuTex = new Texture(Gdx.files.internal("buttons/menu.png"));
        exitTex = new Texture(Gdx.files.internal("buttons/exit.png"));
        soundOnTex = new Texture(Gdx.files.internal("buttons/sound_on.png"));
        soundOffTex = new Texture(Gdx.files.internal("buttons/sound_off.png"));
        musicOnTex = new Texture(Gdx.files.internal("buttons/music_on.png"));
        musicOffTex = new Texture(Gdx.files.internal("buttons/music_off.png"));

        float centerX = 1920 / 2f - 300;
        float centerY = 1080 / 2f - 300;

        ImageButton resumeButton = new ImageButton(new TextureRegionDrawable(resumeTex));
        ImageButton newGameButton = new ImageButton(new TextureRegionDrawable(newGameTex));
        ImageButton menuButton = new ImageButton(new TextureRegionDrawable(menuTex));
        ImageButton exitButton = new ImageButton(new TextureRegionDrawable(exitTex));

        soundButton = new ImageButton(new TextureRegionDrawable(SoundManager.getInstance().isSoundEnabled() ? soundOnTex : soundOffTex));
        musicButton = new ImageButton(new TextureRegionDrawable(MusicManager.getInstance().isMusicEnabled() ? musicOnTex : musicOffTex));

        resumeButton.setPosition(centerX, centerY + 400);
        newGameButton.setPosition(centerX, centerY + 300);
        menuButton.setPosition(centerX, centerY + 200);
        exitButton.setPosition(centerX, centerY + 100);
        soundButton.setPosition(centerX + 320, centerY + 400);
        musicButton.setPosition(centerX + 320, centerY + 300);

        resumeButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playClick();
                tableScreen.regainInputFocus();
                game.setScreen(tableScreen);
            }
        });

        newGameButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playClick();
                tableScreen.startNewGame();
                tableScreen.regainInputFocus();
                game.setScreen(tableScreen);
            }
        });

        menuButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playClick();
                game.setScreen(new MenuScreen(game));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playClick();
                Gdx.app.exit();
            }
        });

        soundButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().toggleSound();
                soundButton.getStyle().imageUp = new TextureRegionDrawable(SoundManager.getInstance().isSoundEnabled() ? soundOnTex : soundOffTex);
            }
        });

        musicButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                boolean enabled = !MusicManager.getInstance().isMusicEnabled();
                MusicManager.getInstance().setMusicEnabled(enabled);
                musicButton.getStyle().imageUp = new TextureRegionDrawable(
                    enabled ? musicOnTex : musicOffTex
                );
                if (enabled) {
                    MusicManager.getInstance().playGameMusic();
                }
            }
        });

        stage.addActor(resumeButton);
        stage.addActor(newGameButton);
        stage.addActor(menuButton);
        stage.addActor(exitButton);
        stage.addActor(soundButton);
        stage.addActor(musicButton);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(bgTex,0, 0);
        batch.end();
        stage.act();
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        stage.dispose();
        bgTex.dispose();
        resumeTex.dispose();
        newGameTex.dispose();
        menuTex.dispose();
        exitTex.dispose();
        soundOnTex.dispose();
        soundOffTex.dispose();
        musicOnTex.dispose();
        musicOffTex.dispose();
    }
}
