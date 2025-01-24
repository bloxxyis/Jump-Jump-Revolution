package main.java;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Projectile extends GameObject {
   private int speed;
   private static final int SCREEN_WIDTH = 1000;
   private int distanceTraveled = 0;
   private static final int MAX_RANGE = 300; // 6 blocks * 50 pixel tile size
   private int startX;
   private BufferedImage texture;
   private boolean facingRight;
   private boolean destroyed = false;

   public Projectile(int x, int y, int width, int height, int speed, boolean facingRight) {
       super(x, y, width, height);
       this.speed = speed;
       this.startX = x;
       this.facingRight = facingRight;
       
       try {
           String texturePath = "src/main/resources/textures/" + LevelLoader.getCurrentTheme() + "/projectile.png";
           BufferedImage originalTexture = ImageIO.read(new File(texturePath));
           texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
           Graphics2D g2d = texture.createGraphics();
           g2d.drawImage(originalTexture, 0, 0, width, height, null);
           g2d.dispose();
           System.out.println("Successfully loaded projectile texture from: " + texturePath);
       } catch (IOException e) {
           System.err.println("Failed to load projectile texture: " + e.getMessage());
           e.printStackTrace();
       }
   }

   public void update(Level level) {
       // Calculate next position
       int nextX = getX() + speed;

       // Check for collision with solid blocks
       if (level.isSolid(nextX, getY()) || 
           level.isSolid(nextX + getWidth(), getY()) ||
           level.isSolid(nextX, getY() + getHeight()) ||
           level.isSolid(nextX + getWidth(), getY() + getHeight())) {
           destroyed = true;
           return;
       }

       // Move if no collision
       setX(nextX);
       distanceTraveled = Math.abs(getX() - startX);

       // Check if projectile has exceeded maximum range
       if (isOutOfRange()) {
           destroyed = true;
       }
   }

   public boolean isOutOfBounds(Level level) {
       return getX() < 0 || getX() > level.getWidth();
   }

   public boolean isOnScreen(int offsetX) {
       return getX() >= offsetX && getX() <= offsetX + SCREEN_WIDTH;
   }

   public boolean isOutOfRange() {
       return distanceTraveled >= MAX_RANGE;
   }

   public boolean isDestroyed() {
       return destroyed;
   }

   @Override
   public void update() {
   }

   @Override
   public void render(Graphics g) {
       render(g, 0);
   }

   @Override
   public void render(Graphics g, int offsetX) {
       if (isOnScreen(offsetX) && !destroyed) {
           Graphics2D g2d = (Graphics2D) g;
           if (texture != null) {
               AffineTransform transform = new AffineTransform();
               transform.translate(getX() - offsetX, getY());
               
               // Flips texture based on direction
               if (!facingRight) {
                   transform.translate(getWidth(), 0);
                   transform.scale(-1, 1);
               }
               
               g2d.drawImage(texture, transform, null);
           } else {
               // Fallback rendering if texture failed to load
               g.setColor(Color.ORANGE);
               g.fillOval(getX() - offsetX, getY(), getWidth(), getHeight());
           }
       }
   }

   // Getters
   public int getSpeed() { return speed; }
   public int getDistanceTraveled() { return distanceTraveled; }
   public boolean isFacingRight() { return facingRight; }
}