package main.java;

public class TextureSet {
    private String groundTexturePath;
    private String platformTexturePath;
    private String powerUpBlockTexturePath;
    private String scoreBlockTexturePath;
    private String endDoorTexturePath;
    private String skyTexturePath;
    private String enemyTexturePath;
    private String projectileTexturePath;
    private String musicPath;

    public TextureSet(String themeName) {
        String baseTexturePath = "/textures/" + themeName + "/";
        String baseMusicPath = "/music/" + themeName + "/";
        
        this.groundTexturePath = baseTexturePath + "ground.png";
        this.platformTexturePath = baseTexturePath + "platform.png";
        this.powerUpBlockTexturePath = baseTexturePath + "power_up_block.png";
        this.scoreBlockTexturePath = baseTexturePath + "score_block.png";
        this.endDoorTexturePath = baseTexturePath + "end_door.png";
        this.skyTexturePath = baseTexturePath + "sky.png";
        this.enemyTexturePath = baseTexturePath + "enemy.png";
        this.projectileTexturePath = baseTexturePath + "projectile.png";
        this.musicPath = baseMusicPath + "background.wav";
    }

    public String getGroundTexturePath() { return groundTexturePath; }
    public String getPlatformTexturePath() { return platformTexturePath; }
    public String getPowerUpBlockTexturePath() { return powerUpBlockTexturePath; }
    public String getScoreBlockTexturePath() { return scoreBlockTexturePath; }
    public String getEndDoorTexturePath() { return endDoorTexturePath; }
    public String getSkyTexturePath() { return skyTexturePath; }
    public String getEnemyTexturePath() { return enemyTexturePath; }
    public String getProjectileTexturePath() { return projectileTexturePath; }
    public String getMusicPath() { return musicPath; }

    public static TextureSet getDesertTheme() {
        return new TextureSet("desert");
    }

    public static TextureSet getChristmasTheme() {
        return new TextureSet("christmas");
    }

    public static TextureSet getHalloweenTheme() {
        return new TextureSet("halloween");
    }

    public static TextureSet fromLevelName(String levelName) {
        if (levelName == null) return getDesertTheme();
        
        switch (levelName) {
            case "level_1":
                return getDesertTheme();
            case "level_2":
                return getChristmasTheme();
            case "level_3":
                return getHalloweenTheme();
            default:
                return getDesertTheme();
        }
    }
}