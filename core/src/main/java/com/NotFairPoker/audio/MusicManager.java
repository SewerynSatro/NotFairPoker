package com.NotFairPoker.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class MusicManager {

    private static MusicManager instance;

    private Music gameMusic;
    private Music menuMusic;

    private boolean musicEnabled = true;
    private String currentTrack = "";

    private MusicManager() {
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("soundtrack.mp3"));
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu_soundtrack.mp3"));

        gameMusic.setVolume(0.2f);
        menuMusic.setVolume(0.2f);

        gameMusic.setLooping(true);
        menuMusic.setLooping(true);
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void playGameMusic() {
        if (!musicEnabled) return;
        if (!currentTrack.equals("game")) {
            stopAll();
            gameMusic.play();
            currentTrack = "game";
        }
    }

    public void playMenuMusic() {
        if (!musicEnabled) return;
        if (!currentTrack.equals("menu")) {
            stopAll();
            menuMusic.play();
            currentTrack = "menu";
        }
    }

    public void stopAll() {
        gameMusic.stop();
        menuMusic.stop();
        currentTrack = "";
    }

    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopAll();
        } else {
            if (currentTrack.equals("game")) gameMusic.play();
            if (currentTrack.equals("menu")) menuMusic.play();
        }
    }

    public void toggleMusic() {
        setMusicEnabled(!musicEnabled);
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void resetAndPlayGameMusic() {
        stopAll();
        if (musicEnabled) {
            gameMusic.play();
            currentTrack = "game";
        }
    }

    public void dispose() {
        gameMusic.dispose();
        menuMusic.dispose();
    }
}
