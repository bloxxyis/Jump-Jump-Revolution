package main.java;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Enemy extends GameObject {
   private int dx, dy;
   private int speed;
   private boolean onGround = false;
   private static final int GRAVITY = 1;
   private static final int TERMINAL_VELOCITY = 10;
   private Player player;
   private BufferedImage texture;
   private long lastShootTime;
   private static final long SHOOT_COOLDOWN = 2000; // 2 seconds
   private List<Projectile> projectiles = new ArrayList<>();
   private static final int SCREEN_WIDTH = 1000;
   private boolean facingRight = true;
   private SoundEffectPlayer soundPlayer;

   public Enemy(int x, int y, int width, int height, int speed, Player player) {
       super(x, y, width, height);
       this.speed = speed;
       this.player = player;
       this.dx = speed;
       this.dy = 0;
       this.lastShootTime = System.currentTimeMillis();
       this.soundPlayer = SoundEffectPlayer.getInstance();

       try {
           String texturePath = "src/main/resources/textures/" + LevelLoader.getCurrentTheme() + "/enemy.png";
           BufferedImage originalTexture = ImageIO.read(new File(texturePath));
           texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
           Graphics2D g2d = texture.createGraphics();
           g2d.drawImage(originalTexture, 0, 0, width, height, null);
           g2d.dispose();
       } catch (IOException e) {
           e.printStackTrace();
           System.err.println("Failed to load enemy texture: " + e.getMessage());
       }
   }

   public void setPlayer(Player player) {
       this.player = player;
   }

   @Override
   public void update() {
   }

   public void update(Level level, int offsetX) {
       // collision check before movement
       boolean willHitWall = false;
       
       if (dx > 0) { // Moving right
           willHitWall = level.isSolid(getX() + getWidth() + dx, getY()) ||
                        level.isSolid(getX() + getWidth() + dx, getY() + getHeight() - 1);
       } else { // Moving left
           willHitWall = level.isSolid(getX() + dx, getY()) ||
                        level.isSolid(getX() + dx, getY() + getHeight() - 1);
       }

       // ground beneath check
       boolean groundAhead;
       if (dx > 0) {
           groundAhead = level.isSolid(getX() + getWidth() + dx, getY() + getHeight() + 1);
       } else {
           groundAhead = level.isSolid(getX() + dx, getY() + getHeight() + 1);
       }

       // Turn around if hit wall / no ground ahead
       if (willHitWall || !groundAhead) {
           dx = -dx;
       } else {
           setX(getX() + dx);
       }

       // Update facing direction based on movement
       if (dx > 0) {
           facingRight = false;
       } else if (dx < 0) {
           facingRight = true;
       }

       // Apply gravity
       if (!onGround) {
           dy += GRAVITY;
           if (dy > TERMINAL_VELOCITY) {
               dy = TERMINAL_VELOCITY;
           }
       }

       // Check vertical collision
       boolean willHitGround = level.isSolid(getX(), getY() + getHeight() + dy) ||
                              level.isSolid(getX() + getWidth(), getY() + getHeight() + dy);
       boolean willHitCeiling = level.isSolid(getX(), getY() + dy) ||
                               level.isSolid(getX() + getWidth(), getY() + dy);

       if (dy > 0 && willHitGround) { // Landing
           setY((getY() + getHeight()) / level.getTileSize() * level.getTileSize() - getHeight());
           onGround = true;
           dy = 0;
       } else if (dy < 0 && willHitCeiling) { // Hitting ceiling
           setY((getY() / level.getTileSize() + 1) * level.getTileSize());
           dy = 0;
       } else {
           setY(getY() + dy);
           onGround = false;
       }

       // Shoot projectile if cooldown has passed and enemy on screen
       if (System.currentTimeMillis() - lastShootTime > SHOOT_COOLDOWN && isOnScreen(offsetX)) {
           shootProjectile();
           lastShootTime = System.currentTimeMillis();
       }

       // Update projectiles
       for (int i = projectiles.size() - 1; i >= 0; i--) {
           Projectile projectile = projectiles.get(i);
           projectile.update(level);
           if (projectile.isDestroyed() || projectile.isOutOfBounds(level) || !projectile.isOnScreen(offsetX)) {
               projectiles.remove(i);
           } else if (projectile.collidesWith(player)) {
               player.takeDamage(25);
               soundPlayer.playSound("projectile_hit");
               projectiles.remove(i);
           }
       }
   }

   private boolean isOnScreen(int offsetX) {
       return getX() >= offsetX && getX() <= offsetX + SCREEN_WIDTH;
   }

   private void shootProjectile() {
       int projectileSpeed = 5;
       int direction = facingRight ? 1 : -1;
       Projectile projectile = new Projectile(
           getX() + getWidth() / 2,
           getY() + getHeight() / 2,
           10, 10,
           projectileSpeed * -direction,
           facingRight
       );
       projectiles.add(projectile);
       soundPlayer.playSound("enemy_shoot");
   }

   @Override
   public void render(Graphics g) {
       render(g, 0);
   }

   @Override
   public void render(Graphics g, int offsetX) {
       if (isOnScreen(offsetX)) {
           Graphics2D g2d = (Graphics2D) g;
           if (texture != null) {
               AffineTransform transform = new AffineTransform();
               transform.translate(getX() - offsetX, getY());
               if (!facingRight) {
                   transform.translate(getWidth(), 0);
                   transform.scale(-1, 1);
               }
               g2d.drawImage(texture, transform, null);
           } else {
               // Fallback rendering if texture failed to load
               g.setColor(Color.RED);
               g.fillRect(getX() - offsetX, getY(), getWidth(), getHeight());
           }

           // Render projectiles
           for (Projectile projectile : projectiles) {
               projectile.render(g, offsetX);
           }
       }
   }

   // Getters & setters
   public int getDx() { return dx; }
   public int getDy() { return dy; }
   public void setDx(int dx) { this.dx = dx; }
   public void setDy(int dy) { this.dy = dy; }
   public int getSpeed() { return speed; }
   public void setSpeed(int speed) { this.speed = speed; }
   public boolean isOnGround() { return onGround; }
}