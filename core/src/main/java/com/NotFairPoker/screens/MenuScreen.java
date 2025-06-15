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

    private Texture avatar1Tex, avatar2Tex;
    private Texture startButtonTex;
    private Texture exitButtonTex;
    private Texture musicOnTex, musicOffTex;
    private Texture soundOnTex, soundOffTex;

    private ImageButton avatarButton1, avatarButton2, startButton, exitButton;
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

        avatar1Tex = new Texture(Gdx.files.internal("avatars/1.png"));
        avatar2Tex = new Texture(Gdx.files.internal("avatars/2.png"));
        startButtonTex = new Texture(Gdx.files.internal("buttons/startgame.png"));
        exitButtonTex = new Texture(Gdx.files.internal("buttons/exit.png"));
        musicOnTex = new Texture(Gdx.files.internal("buttons/music_on.png"));
        musicOffTex = new Texture(Gdx.files.internal("buttons/music_off.png"));
        soundOnTex = new Texture(Gdx.files.internal("buttons/sound_on.png"));
        soundOffTex = new Texture(Gdx.files.internal("buttons/sound_off.png"));

        avatarButton1 = new ImageButton(new TextureRegionDrawable(avatar1Tex));
        avatarButton2 = new ImageButton(new TextureRegionDrawable(avatar2Tex));
        startButton = new ImageButton(new TextureRegionDrawable(startButtonTex));
        exitButton = new ImageButton(new TextureRegionDrawable(exitButtonTex));
        musicToggleButton = new ImageButton(new TextureRegionDrawable(MusicManager.getInstance().isMusicEnabled() ? musicOnTex : musicOffTex));
        soundToggleButton = new ImageButton(new TextureRegionDrawable(SoundManager.getInstance().isSoundEnabled() ? soundOnTex : soundOffTex));

        avatarButton1.setPosition(300, 300);
        avatarButton2.setPosition(600, 300);
        startButton.setPosition(450, 100);
        exitButton.setPosition(450, 20);
        musicToggleButton.setPosition(50, 50);
        soundToggleButton.setPosition(150, 50);

        avatarButton1.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                selectedAvatar = 1;
                SoundManager.getInstance().playClick();
            }
        });
        avatarButton2.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                selectedAvatar = 2;
                SoundManager.getInstance().playClick();
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
                    MusicManager.getInstance().playMenuMusic();  // Bo jesteśmy w menu screen
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

        stage.addActor(avatarButton1);
        stage.addActor(avatarButton2);
        stage.addActor(startButton);
        stage.addActor(exitButton);
        stage.addActor(musicToggleButton);
        stage.addActor(soundToggleButton);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0.3f, 0.1f, 1);

        batch.begin();
        font.draw(batch, "Select your avatar:", 400, 600);
        font.draw(batch, "Selected: " + (selectedAvatar == 1 ? "Jack" : "Lucy"), 400, 550);
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
        avatar1Tex.dispose();
        avatar2Tex.dispose();
        startButtonTex.dispose();
        exitButtonTex.dispose();
        musicOnTex.dispose();
        musicOffTex.dispose();
        soundOnTex.dispose();
        soundOffTex.dispose();
        stage.dispose();
    }
}
