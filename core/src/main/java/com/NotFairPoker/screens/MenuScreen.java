package com.NotFairPoker.screens;

import com.NotFairPoker.Main;
import com.NotFairPoker.audio.MusicManager;
import com.NotFairPoker.audio.SoundManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MenuScreen implements Screen {

    private final Main game;
    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;

    private int selectedAvatar = 1;

    private Texture jakeActiveTex, jakeInactiveTex;
    private Texture lucyActiveTex, lucyInactiveTex;
    private Texture startButtonTex;
    private Texture exitButtonTex;
    private Texture musicOnTex, musicOffTex;
    private Texture soundOnTex, soundOffTex;
    private Texture menuBackgroundTex;

    private ImageButton jakeButton, lucyButton, startButton, exitButton;
    private ImageButton musicToggleButton, soundToggleButton;

    public MenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        MusicManager.getInstance().playMenuMusic();

        menuBackgroundTex = new Texture(Gdx.files.internal("menu_bg.png"));
        jakeActiveTex = new Texture(Gdx.files.internal("menu/1.png"));
        jakeInactiveTex = new Texture(Gdx.files.internal("menu/1b.png"));
        lucyActiveTex = new Texture(Gdx.files.internal("menu/2.png"));
        lucyInactiveTex = new Texture(Gdx.files.internal("menu/2b.png"));
        startButtonTex = new Texture(Gdx.files.internal("menu/startgame.png"));
        exitButtonTex = new Texture(Gdx.files.internal("menu/exit.png"));
        musicOnTex = new Texture(Gdx.files.internal("menu/music_on.png"));
        musicOffTex = new Texture(Gdx.files.internal("menu/music_off.png"));
        soundOnTex = new Texture(Gdx.files.internal("menu/sound_on.png"));
        soundOffTex = new Texture(Gdx.files.internal("menu/sound_off.png"));

        jakeButton = new ImageButton(new TextureRegionDrawable(jakeActiveTex));
        lucyButton = new ImageButton(new TextureRegionDrawable(lucyInactiveTex));
        startButton = new ImageButton(new TextureRegionDrawable(startButtonTex));
        exitButton = new ImageButton(new TextureRegionDrawable(exitButtonTex));
        musicToggleButton = new ImageButton(new TextureRegionDrawable(MusicManager.getInstance().isMusicEnabled() ? musicOnTex : musicOffTex));
        soundToggleButton = new ImageButton(new TextureRegionDrawable(SoundManager.getInstance().isSoundEnabled() ? soundOnTex : soundOffTex));

        jakeButton.setPosition(0, 0);
        lucyButton.setPosition(1320, 0);
        startButton.setPosition(810, 400);
        exitButton.setPosition(810, 270);
        musicToggleButton.setPosition(810, 140);
        soundToggleButton.setPosition(1010, 140);

        jakeButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                selectedAvatar = 1;
                SoundManager.getInstance().playClick();
                updateAvatarSelection();
            }
        });

        lucyButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                selectedAvatar = 2;
                SoundManager.getInstance().playClick();
                updateAvatarSelection();
            }
        });

        startButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playClick();
                String playerName = (selectedAvatar == 1) ? "Jack" : "Lucy";
                game.setScreen(new TableScreen(game, playerName, selectedAvatar));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playClick();
                Gdx.app.exit();
            }
        });

        musicToggleButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                SoundManager.getInstance().playClick();
                boolean enabled = !MusicManager.getInstance().isMusicEnabled();
                MusicManager.getInstance().setMusicEnabled(enabled);

                if (enabled) {
                    MusicManager.getInstance().playMenuMusic();
                }

                musicToggleButton.getStyle().imageUp = new TextureRegionDrawable(enabled ? musicOnTex : musicOffTex);
            }
        });

        soundToggleButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                boolean enabled = !SoundManager.getInstance().isSoundEnabled();
                SoundManager.getInstance().setSoundEnabled(enabled);
                soundToggleButton.getStyle().imageUp = new TextureRegionDrawable(enabled ? soundOnTex : soundOffTex);
            }
        });

        stage.addActor(jakeButton);
        stage.addActor(lucyButton);
        stage.addActor(startButton);
        stage.addActor(exitButton);
        stage.addActor(musicToggleButton);
        stage.addActor(soundToggleButton);

        updateAvatarSelection();
    }

    private void updateAvatarSelection() {
        if (selectedAvatar == 1) {
            jakeButton.getStyle().imageUp = new TextureRegionDrawable(jakeActiveTex);
            lucyButton.getStyle().imageUp = new TextureRegionDrawable(lucyInactiveTex);
        } else {
            jakeButton.getStyle().imageUp = new TextureRegionDrawable(jakeInactiveTex);
            lucyButton.getStyle().imageUp = new TextureRegionDrawable(lucyActiveTex);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0.3f, 0.1f, 1);

        batch.begin();
        batch.draw(menuBackgroundTex, 0, 0);
        //font.draw(batch, "Select your avatar:", 400, 600);
        //font.draw(batch, "Selected: " + (selectedAvatar == 1 ? "Jack" : "Lucy"), 400, 550);
        batch.end();

        stage.act(delta);
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
        jakeActiveTex.dispose();
        jakeInactiveTex.dispose();
        lucyActiveTex.dispose();
        lucyInactiveTex.dispose();
        startButtonTex.dispose();
        exitButtonTex.dispose();
        musicOnTex.dispose();
        musicOffTex.dispose();
        soundOnTex.dispose();
        soundOffTex.dispose();
        menuBackgroundTex.dispose();
        stage.dispose();
    }
}
