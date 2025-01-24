package main.java;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

public class Level {
    private int[][] levelData;
    private int tileSize;
    private int width;
    private int height;
    private int playerStartX, playerStartY;
    private List<Enemy> enemies;
    private List<GameObject> platforms;
    private Random random;
    private int endDoorX;
    private boolean isEndDoorPlaced = false;
    private static final int END_DOOR_X = 5000;
    private int totalWidth;

    public static final int EMPTY = 0;
    public static final int GROUND = 1;
    public static final int PLATFORM = 2;
    public static final int PLAYER_START = 3;
    public static final int ENEMY_SPAWN = 4;
    public static final int END_DOOR = 5;
    public static final int POWER_UP_BLOCK = 6;
    public static final int SCORE_BLOCK = 7;

    private static final int MAX_JUMP_HEIGHT = 4;
    private static final int MIN_PLATFORM_DISTANCE = 3;
    private static final int SCORE_POINTS = 25;

    private static final String SECRET_KEY = "your-secret-key";
    private static final String SALT = "your-salt";

    private Player player;

    private BufferedImage groundTexture;
    private BufferedImage platformTexture;
    private BufferedImage powerUpBlockTexture;
    private BufferedImage scoreBlockTexture;
    private BufferedImage endDoorTexture;
    private BufferedImage skyTexture;

    private SoundEffectPlayer soundPlayer;

    // Constructor for custom level
    public Level(int[][] levelData, int tileSize, String groundTexturePath, String platformTexturePath, 
                 String powerUpBlockTexturePath, String scoreBlockTexturePath, 
                 String endDoorTexturePath, String skyTexturePath) {
        System.out.println("Creating custom level with tile size: " + tileSize);
        this.tileSize = tileSize;
        this.levelData = levelData;
        this.height = levelData.length;
        this.width = levelData[0].length;
        this.enemies = new ArrayList<>();
        this.platforms = new ArrayList<>();
        this.random = new Random();
        this.totalWidth = width * tileSize;
        
        loadTextures(groundTexturePath, platformTexturePath, powerUpBlockTexturePath, 
                     scoreBlockTexturePath, endDoorTexturePath, skyTexturePath);
        initializeObjects();
        findEndDoor();
        this.soundPlayer = SoundEffectPlayer.getInstance();
        
        System.out.println("Custom level created. Dimensions: " + width + "x" + height);
        printLevelSample();
    }

    // Constructor for default level
    public Level(int tileSize, String groundTexturePath, String platformTexturePath, 
                 String powerUpBlockTexturePath, String scoreBlockTexturePath, 
                 String endDoorTexturePath, String skyTexturePath) {
        System.out.println("Creating default level with tile size: " + tileSize);
        this.tileSize = tileSize;
        this.width = END_DOOR_X / tileSize;
        this.height = 13;
        this.enemies = new ArrayList<>();
        this.platforms = new ArrayList<>();
        this.random = new Random();
        this.totalWidth = END_DOOR_X;
        
        loadTextures(groundTexturePath, platformTexturePath, powerUpBlockTexturePath, 
                     scoreBlockTexturePath, endDoorTexturePath, skyTexturePath);
        generateDefaultLevel();
        this.soundPlayer = SoundEffectPlayer.getInstance();
        
        System.out.println("Default level created. Dimensions: " + width + "x" + height);
        printLevelSample();
    }

    private void printLevelSample() {
        System.out.println("Level data sample (first 10x10 or max available):");
        for (int y = 0; y < Math.min(height, 10); y++) {
            for (int x = 0; x < Math.min(width, 10); x++) {
                System.out.print(levelData[y][x] + " ");
            }
            System.out.println();
        }
    }

    public int getTileTypeDirectly(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return levelData[y][x];
        }
        return EMPTY;
    }

    public int getTileTypeAt(int x, int y) {
        int tileX = x / tileSize;
        int tileY = y / tileSize;
        
        if (tileX < 0 || tileX >= width || tileY < 0 || tileY >= height) {
            return EMPTY;
        }
        
        return levelData[tileY][tileX];
    }

    private void loadTextures(String groundTexturePath, String platformTexturePath, 
                            String powerUpBlockTexturePath, String scoreBlockTexturePath, 
                            String endDoorTexturePath, String skyTexturePath) {
        try {
            groundTexture = ImageIO.read(new File(groundTexturePath));
            platformTexture = ImageIO.read(new File(platformTexturePath));
            powerUpBlockTexture = ImageIO.read(new File(powerUpBlockTexturePath));
            scoreBlockTexture = ImageIO.read(new File(scoreBlockTexturePath));
            endDoorTexture = ImageIO.read(new File(endDoorTexturePath));
            
            File skyFile = new File(skyTexturePath);
            if (skyFile.exists()) {
                skyTexture = ImageIO.read(skyFile);
            }
        } catch (IOException e) {
            System.err.println("Failed to load one or more textures: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateDefaultLevel() {
        levelData = new int[height][width];
        
        // Fills bottom row with ground
        for (int x = 0; x < width; x++) {
            levelData[height - 1][x] = GROUND;
        }

        int lastPlatformX = -MIN_PLATFORM_DISTANCE;
        int lastPlatformY = height - 2;

        // Generates platforms and other elements
        for (int x = 0; x < width; x++) {
            if (x - lastPlatformX >= MIN_PLATFORM_DISTANCE && random.nextDouble() < 0.3) {
                int platformLength = random.nextInt(3) + 3;
                int minY = Math.max(4, lastPlatformY - MAX_JUMP_HEIGHT);
                int maxY = Math.min(height - 3, lastPlatformY + MAX_JUMP_HEIGHT);
                int platformY = random.nextInt(maxY - minY + 1) + minY;

                // Place platform
                for (int px = x; px < Math.min(x + platformLength, width); px++) {
                    levelData[platformY][px] = PLATFORM;
                }

                // Add power-up or score blocks sometimes
                if (platformY > 3 && random.nextDouble() < 0.3) {
                    int blockX = x + platformLength / 2;
                    if (blockX < width) {
                        levelData[platformY - 3][blockX] = random.nextBoolean() ? POWER_UP_BLOCK : SCORE_BLOCK;
                    }
                }

                // Add enemies sometimes
                if (random.nextDouble() < 0.3 && x > MIN_PLATFORM_DISTANCE) {
                    levelData[platformY - 1][x] = ENEMY_SPAWN;
                }

                lastPlatformX = x + platformLength - 1;
                lastPlatformY = platformY;
                x = lastPlatformX;
            }
        }

        // Place player start and end door
        levelData[height - 2][1] = PLAYER_START;
        levelData[height - 2][width - 1] = END_DOOR;
        endDoorX = (width - 1) * tileSize;
        isEndDoorPlaced = true;

        initializeObjects();
    }

    private void initializeObjects() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelX = x * tileSize;
                int pixelY = y * tileSize;
                
                switch (levelData[y][x]) {
                    case PLAYER_START:
                        playerStartX = pixelX;
                        playerStartY = pixelY;
                        break;
                    case ENEMY_SPAWN:
                        enemies.add(new Enemy(pixelX, pixelY, tileSize, tileSize, 2, player));
                        break;
                    case GROUND:
                    case PLATFORM:
                        platforms.add(new Land(pixelX, pixelY, tileSize, tileSize));
                        break;
                }
            }
        }
    }

    private void findEndDoor() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (levelData[y][x] == END_DOOR) {
                    endDoorX = x * tileSize;
                    isEndDoorPlaced = true;
                    return;
                }
            }
        }
    }

    public void update(int playerX, int offsetX) {
        for (Enemy enemy : enemies) {
            enemy.update(this, offsetX);
        }
    }

    public void render(Graphics g, int offsetX) {
        int startX = Math.max(0, offsetX / tileSize);
        int endX = Math.min(width, (offsetX + 1000) / tileSize + 1);
    
        for (int y = 0; y < height; y++) {
            for (int x = startX; x < endX; x++) {
                int pixelX = x * tileSize - offsetX;
                int pixelY = y * tileSize;
                
                int tileType = levelData[y][x];
                if (tileType != EMPTY && tileType != PLAYER_START && tileType != ENEMY_SPAWN) {
                    BufferedImage texture = getTextureForTileType(tileType);
                    if (texture != null) {
                        g.drawImage(texture, pixelX, pixelY, tileSize, tileSize, null);
                    } else {
                        g.setColor(getColorForTileType(tileType));
                        g.fillRect(pixelX, pixelY, tileSize, tileSize);
                    }
                }
            }
        }
    
        // Render enemies
        for (Enemy enemy : enemies) {
            enemy.render(g, offsetX);
        }
    }

    private BufferedImage getTextureForTileType(int tileType) {
        switch (tileType) {
            case GROUND: return groundTexture;
            case PLATFORM: return platformTexture;
            case POWER_UP_BLOCK: return powerUpBlockTexture;
            case SCORE_BLOCK: return scoreBlockTexture;
            case END_DOOR: return endDoorTexture;
            default: return null;
        }
    }

    private Color getColorForTileType(int tileType) {
        switch (tileType) {
            case GROUND: return new Color(139, 69, 19);
            case PLATFORM: return new Color(34, 139, 34);
            case POWER_UP_BLOCK: return new Color(255, 105, 180);
            case SCORE_BLOCK: return new Color(255, 215, 0);
            case END_DOOR: return Color.RED;
            case PLAYER_START: return Color.BLUE;
            case ENEMY_SPAWN: return Color.ORANGE;
            default: return Color.WHITE;
        }
    }

    public boolean isSolid(int x, int y) {
        if (x >= totalWidth) {
            return true;
        }
        
        int tileX = x / tileSize;
        int tileY = y / tileSize;
        
        if (tileX < 0 || tileX >= width || tileY < 0 || tileY >= height) {
            return false;
        }
        
        int tileType = levelData[tileY][tileX];
        return tileType == GROUND || tileType == PLATFORM || 
               tileType == POWER_UP_BLOCK || tileType == SCORE_BLOCK;
    }

    public void checkBlockCollision(Player player) {
        int playerTileY = player.getY() / tileSize;
        int playerTileX = player.getX() / tileSize;

        if (player.getDy() < 0) {
            if (isTileType(playerTileX, playerTileY - 1, POWER_UP_BLOCK)) {
                setTileType(playerTileX, playerTileY - 1, EMPTY);
                PowerUp powerUp = PowerUp.getRandomPowerUp(playerTileX * tileSize, (playerTileY - 1) * tileSize);
                player.applyPowerUp(powerUp);
                soundPlayer.playSound("powerup_block");
            } else if (isTileType(playerTileX, playerTileY - 1, SCORE_BLOCK)) {
                setTileType(playerTileX, playerTileY - 1, EMPTY);
                player.addScore(SCORE_POINTS);
                soundPlayer.playSound("score_block");
            }
            // Check the tile above and to the right of the player's head
            else if (isTileType(playerTileX + 1, playerTileY - 1, POWER_UP_BLOCK)) {
                setTileType(playerTileX + 1, playerTileY - 1, EMPTY);
                PowerUp powerUp = PowerUp.getRandomPowerUp((playerTileX + 1) * tileSize, (playerTileY - 1) * tileSize);
                player.applyPowerUp(powerUp);
                soundPlayer.playSound("powerup_block");
            } else if (isTileType(playerTileX + 1, playerTileY - 1, SCORE_BLOCK)) {
                setTileType(playerTileX + 1, playerTileY - 1, EMPTY);
                player.addScore(SCORE_POINTS);
                soundPlayer.playSound("score_block");
            }
        }
    }

    private boolean isTileType(int x, int y, int tileType) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return levelData[y][x] == tileType;
        }
        return false;
    }

    private void setTileType(int x, int y, int tileType) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            levelData[y][x] = tileType;
        }
    }

    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < getTotalWidth() && y >= 0 && y < getHeight();
    }

    public void addEnemy(int x, int y) {
        enemies.add(new Enemy(x, y, tileSize, tileSize, 2, player));
        System.out.println("Added enemy at position: (" + x + ", " + y + ")");
    }

    public void removeEnemy(Enemy enemy) {
        enemies.remove(enemy);
    }

    public void addPlatform(int x, int y) {
        platforms.add(new Land(x, y, tileSize, tileSize));
    }

    public void clearObjects() {
        enemies.clear();
        platforms.clear();
    }

    public void reset() {
        clearObjects();
        initializeObjects();
    }

    public static int[][] loadFromFile(String filePath) throws Exception {
        System.out.println("Loading level from file: " + filePath);
        File file = new File(filePath);
        String encryptedData = new String(Files.readAllBytes(file.toPath()));
        int[][] levelData = decryptLevel(encryptedData);
        System.out.println("Level data loaded successfully");
        return levelData;
    }

    private static int[][] decryptLevel(String encryptedData) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);

        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        String decryptedString = new String(decryptedBytes);
        return convertStringToLevel(decryptedString);
    }

    private static int[][] convertStringToLevel(String levelString) {
        String[] rows = levelString.split(";");
        int[][] levelData = new int[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            String[] tiles = rows[i].split(",");
            levelData[i] = new int[tiles.length];
            for (int j = 0; j < tiles.length; j++) {
                levelData[i][j] = Integer.parseInt(tiles[j]);
            }
        }
        return levelData;
    }

    public static Level loadFromFile(String filePath, int tileSize, String texturePath) {
        try {
            int[][] levelData = loadFromFile(filePath);
            System.out.println("Creating level from loaded data. Dimensions: " + 
                             levelData[0].length + "x" + levelData.length);
            return new Level(levelData, tileSize, 
                           texturePath + "ground.png",
                           texturePath + "platform.png",
                           texturePath + "power_up_block.png",
                           texturePath + "score_block.png",
                           texturePath + "end_door.png",
                           texturePath + "sky.png");
        } catch (Exception e) {
            System.err.println("Error loading level from file: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Falling back to default level");
            return new Level(tileSize, 
                           texturePath + "ground.png",
                           texturePath + "platform.png",
                           texturePath + "power_up_block.png",
                           texturePath + "score_block.png",
                           texturePath + "end_door.png",
                           texturePath + "sky.png");
        }
    }

    public void debugPrintLevel() {
        System.out.println("\nLevel Debug Information:");
        System.out.println("Dimensions: " + width + "x" + height);
        System.out.println("Tile Size: " + tileSize);
        System.out.println("Total Width: " + totalWidth);
        System.out.println("End Door X: " + endDoorX);
        System.out.println("Player Start: (" + playerStartX + "," + playerStartY + ")");
        System.out.println("Number of Enemies: " + enemies.size());
        System.out.println("Number of Platforms: " + platforms.size());
        
        System.out.println("\nLevel Data Sample (first 10x10 or max available):");
        for (int y = 0; y < Math.min(height, 10); y++) {
            for (int x = 0; x < Math.min(width, 10); x++) {
                System.out.print(levelData[y][x] + " ");
            }
            System.out.println();
        }
    }

    // Helper method for printing level data
    public void printLevelData() {
        System.out.println("Full Level Data:");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(levelData[y][x] + " ");
            }
            System.out.println();
        }
    }

    // Getters
    public int getPlayerStartX() { return playerStartX; }
    public int getPlayerStartY() { return playerStartY; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<GameObject> getPlatforms() { return platforms; }
    public int getTileSize() { return tileSize; }
    public int getEndDoorX() { return endDoorX; }
    public boolean isEndDoorPlaced() { return isEndDoorPlaced; }
    public BufferedImage getSkyTexture() { return skyTexture; }
    
    // Dimension getters
    public int getWidth() { return width * tileSize; }
    public int getHeight() { return height * tileSize; }
    public int getTotalWidth() { return totalWidth; }
    public int getWidthInTiles() { return width; }
    public int getHeightInTiles() { return height; }

    public void setPlayer(Player player) {
        this.player = player;
        for (Enemy enemy : enemies) {
            enemy.setPlayer(player);
        }
    }
}