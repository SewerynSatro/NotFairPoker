package com.NotFairPoker.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {

    private static SoundManager instance;

    private Sound clickSound;
    private boolean soundEnabled = true;
    private float soundVolume = 1.0f;  // stała globalna głośność dźwięków

    private SoundManager() {
        clickSound = Gdx.audio.newSound(Gdx.files.internal("sounds/button.mp3"));
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void playClick() {
        if (soundEnabled) {
            clickSound.play(soundVolume);
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void toggleSound() {
        this.soundEnabled = !this.soundEnabled;
    }

    // NAJWAŻNIEJSZA METODA — "na sztywno" jak w MusicManager
    public void setVolume(float volume) {
        this.soundVolume = volume;
    }

    public float getVolume() {
        return soundVolume;
    }

    public void dispose() {
        clickSound.dispose();
    }
}
