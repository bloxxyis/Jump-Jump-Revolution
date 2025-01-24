package main.java;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

public class SoundEffectPlayer {
    private static SoundEffectPlayer instance;
    private Map<String, Clip> soundEffects;
    private float volume = 1.0f;

    private SoundEffectPlayer() {
        soundEffects = new HashMap<>();
        loadSoundEffects();
    }

    public static SoundEffectPlayer getInstance() {
        if (instance == null) {
            instance = new SoundEffectPlayer();
        }
        return instance;
    }

    private void loadSoundEffects() {
        // Player sounds
        loadSound("walk", "walk.wav");
        loadSound("jump", "jump.wav");
        loadSound("player_hit", "player_hit.wav");
        loadSound("player_heal", "player_heal.wav");

        // Enemy sounds
        loadSound("enemy_defeat", "enemy_defeat.wav");
        loadSound("enemy_shoot", "enemy_shoot.wav");
        loadSound("projectile_hit", "projectile_hit.wav");

        // Level sounds
        loadSound("powerup_block", "powerup_block.wav");
        loadSound("score_block", "score_block.wav");
        loadSound("level_complete", "level_complete.wav");

        // Power-up sounds
        loadSound("powerup_speed", "powerup_speed.wav");
        loadSound("powerup_health", "powerup_health.wav");
        loadSound("powerup_doublejump", "powerup_doublejump.wav");
        loadSound("powerup_invincibility", "powerup_invincibility.wav");

        // Miscellaneous sounds
        loadSound("score_increase", "score_increase.wav");
    }

    private void loadSound(String key, String fileName) {
        try {
            String soundPath = "src/main/resources/sfx/" + fileName;
            File soundFile = new File(soundPath);
            
            if (!soundFile.exists()) {
                System.err.println("Sound file not found: " + soundPath);
                return;
            }
            
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            soundEffects.put(key, clip);
            System.out.println("Loaded sound effect: " + key + " from " + soundPath);
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound effect " + key + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void playSound(String key) {
        Clip clip = soundEffects.get(key);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            setClipVolume(clip, volume);
            clip.start();
        } else {
            System.err.println("Sound effect not found: " + key);
        }
    }

    public void stopSound(String key) {
        Clip clip = soundEffects.get(key);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void stopAllSounds() {
        for (Clip clip : soundEffects.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
        }
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        for (Clip clip : soundEffects.values()) {
            setClipVolume(clip, this.volume);
        }
    }

    private void setClipVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }

    public float getVolume() {
        return volume;
    }
}