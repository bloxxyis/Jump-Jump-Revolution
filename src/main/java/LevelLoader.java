package main.java;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LevelLoader {
    private static final int TILE_SIZE = 50;
    
    // Base resource paths
    private static final String BASE_RESOURCE_PATH = "src/main/resources/";
    private static final String TEXTURES_PATH = BASE_RESOURCE_PATH + "textures/";
    private static final String LEVELS_PATH = BASE_RESOURCE_PATH + "levels/";
    private static final String MUSIC_PATH = BASE_RESOURCE_PATH + "music/";
    private static final String SFX_PATH = BASE_RESOURCE_PATH + "sfx/";

    // Theme management
    private static String currentTheme = "halloween"; // Default theme

    public static Level loadLevel(String levelName) {
        try {
            String filePath = LEVELS_PATH + levelName + ".dat";
            System.out.println("Loading level from file: " + filePath);
            int[][] levelData = Level.loadFromFile(filePath);
            
            // Sets theme based on level name
            setThemeFromLevelName(levelName);
            
            // Gets current theme path
            String themePath = TEXTURES_PATH + currentTheme + "/";
            
            // Creates level with themed textures
            return new Level(levelData, TILE_SIZE, 
                themePath + "ground.png",
                themePath + "platform.png",
                themePath + "power_up_block.png",
                themePath + "score_block.png",
                themePath + "end_door.png",
                themePath + "sky.png");

        } catch (Exception e) {
            System.err.println("Error loading level: " + e.getMessage());
            e.printStackTrace();
            return createDefaultLevel();
        }
    }

    private static void setThemeFromLevelName(String levelName) {
        switch (levelName) {
            case "level_1":
                currentTheme = "desert";
                break;
            case "level_2":
                currentTheme = "christmas";
                break;
            case "level_3":
                currentTheme = "halloween";
                break;
            default:
                currentTheme = "desert"; // Default theme
        }
        System.out.println("Set theme to: " + currentTheme);
    }

    public static Level createDefaultLevel() {
        String themePath = TEXTURES_PATH + currentTheme + "/";
        return new Level(TILE_SIZE,
            themePath + "ground.png",
            themePath + "platform.png",
            themePath + "power_up_block.png",
            themePath + "score_block.png",
            themePath + "end_door.png",
            themePath + "sky.png");
    }

    public static String getCurrentTheme() {
        return currentTheme;
    }

    public static String getMusicPath() {
        return MUSIC_PATH + currentTheme + "/background.wav";
    }

    public static String getSfxPath() {
        return SFX_PATH;
    }

    public static boolean verifyResources() {
        String themePath = TEXTURES_PATH + currentTheme + "/";
        
        // List of required texture files
        String[] requiredTextures = {
            "ground.png",
            "platform.png",
            "power_up_block.png",
            "score_block.png",
            "end_door.png",
            "sky.png",
            "person.png",
            "enemy.png",
            "projectile.png"
        };

        // List of required sound files
        String[] requiredSounds = {
            "walk.wav",
            "jump.wav",
            "player_hit.wav",
            "enemy_defeat.wav",
            "enemy_shoot.wav",
            "powerup_block.wav",
            "score_block.wav",
            "level_complete.wav"
        };

        // Verify texture files
        File themeDir = new File(themePath);
        if (!themeDir.exists() || !themeDir.isDirectory()) {
            System.err.println("Theme directory not found: " + themePath);
            return false;
        }

        for (String texture : requiredTextures) {
            File textureFile = new File(themePath + texture);
            if (!textureFile.exists()) {
                System.err.println("Missing texture: " + themePath + texture);
                return false;
            }
        }

        // Verify sound files
        File sfxDir = new File(SFX_PATH);
        if (!sfxDir.exists() || !sfxDir.isDirectory()) {
            System.err.println("SFX directory not found: " + SFX_PATH);
            return false;
        }

        for (String sound : requiredSounds) {
            File soundFile = new File(SFX_PATH + sound);
            if (!soundFile.exists()) {
                System.err.println("Missing sound: " + SFX_PATH + sound);
                return false;
            }
        }

        // Verify music file
        File musicFile = new File(getMusicPath());
        if (!musicFile.exists()) {
            System.err.println("Missing music: " + getMusicPath());
            return false;
        }

        return true;
    }

    public static List<String> getAvailableLevels() {
        List<String> levels = new ArrayList<>();
        File levelsDir = new File(LEVELS_PATH);
        if (levelsDir.exists() && levelsDir.isDirectory()) {
            File[] files = levelsDir.listFiles((dir, name) -> name.endsWith(".dat"));
            if (files != null) {
                for (File file : files) {
                    levels.add(file.getName().replace(".dat", ""));
                }
            }
        }
        System.out.println("Available levels: " + levels);
        return levels;
    }

    public static void createResourceDirectories() {
        // Creates main resource directories
        new File(BASE_RESOURCE_PATH).mkdirs();
        new File(TEXTURES_PATH).mkdirs();
        new File(LEVELS_PATH).mkdirs();
        new File(MUSIC_PATH).mkdirs();
        new File(SFX_PATH).mkdirs();

        // Creates theme directories
        String[] themes = {"desert", "christmas", "halloween"};
        for (String theme : themes) {
            new File(TEXTURES_PATH + theme).mkdirs();
            new File(MUSIC_PATH + theme).mkdirs();
        }

        System.out.println("Resource directories created successfully");
    }

    public static void verifyAndPrintResourceStructure() {
        System.out.println("\nResource Structure Verification:");
        System.out.println("--------------------------------");
        System.out.println("Current theme: " + currentTheme);
        System.out.println("Base resource path: " + new File(BASE_RESOURCE_PATH).getAbsolutePath());
        
        // Check directories
        verifyDirectory("Textures", TEXTURES_PATH);
        verifyDirectory("Levels", LEVELS_PATH);
        verifyDirectory("Music", MUSIC_PATH);
        verifyDirectory("SFX", SFX_PATH);
        
        // Checks theme directories
        String[] themes = {"desert", "christmas", "halloween"};
        for (String theme : themes) {
            verifyDirectory("Theme: " + theme, TEXTURES_PATH + theme);
            verifyDirectory("Music: " + theme, MUSIC_PATH + theme);
        }
    }

    private static void verifyDirectory(String name, String path) {
        File dir = new File(path);
        System.out.println(name + " directory:");
        System.out.println("  Path: " + dir.getAbsolutePath());
        System.out.println("  Exists: " + dir.exists());
        System.out.println("  Is Directory: " + dir.isDirectory());
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                System.out.println("  Contents:");
                for (File file : files) {
                    System.out.println("    - " + file.getName());
                }
            }
        }
        System.out.println();
    }
}