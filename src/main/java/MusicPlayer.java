package main.java;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class MusicPlayer {
    private Clip clip;

    public void playBackgroundMusic() {
        try {
            String musicPath = "src/main/resources/music/" + LevelLoader.getCurrentTheme() + "/background.wav";
            System.out.println("Attempting to play music from: " + musicPath);
            File musicFile = new File(musicPath);
            if (!musicFile.exists()) {
                System.err.println("Could not find music file: " + musicPath);
                return;
            }
            
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
            clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("Background music started successfully");
            
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Audio file format not supported: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error reading music file: " + e.getMessage());
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("Audio line unavailable: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
            System.out.println("Background music stopped");
        }
    }

    public void setVolume(float volume) {
        if (clip != null) {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
                System.out.println("Volume set to: " + volume);
            } catch (Exception e) {
                System.err.println("Error setting volume: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Cannot set volume: clip is null");
        }
    }
}