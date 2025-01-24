package main.java;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Player extends GameObject {
   private BufferedImage texture;
   private int dx;
   private float dy;
   private int speed = 5;
   private boolean onGround = false;
   private static final float JUMP_SPEED = -15f;
   private static final float GRAVITY = 0.7f;
   private static final float TERMINAL_VELOCITY = 10f;
   private static final int KNOCKBACK_SPEED = 15;
   private static final int KNOCKBACK_DURATION = 15;
   private int knockbackTimer = 0;

   private int health;
   private int maxHealth;
   private int score;
   private static final int DAMAGE_PER_HIT = 50;
   private static final int SCORE_FOR_ENEMY_KILL = 100;

   private boolean reachedEndDoor = false;

   private boolean doubleJumpActive = false;
   private long doubleJumpEndTime = 0;
   private boolean hasDoubleJumped = false;
   private static final long DOUBLE_JUMP_DURATION = 5000;

   private boolean invincible = false;
   private long invincibilityEndTime = 0;
   private long speedBoostEndTime = 0;
   private int normalSpeed;

   private boolean healthPowerUpActive = false;
   private long healthPowerUpEndTime = 0;
   private static final long HEALTH_INDICATOR_DURATION = 1000;

   private boolean movingLeft = false;
   private boolean movingRight = false;
   private boolean facingRight = true;

   private static final int DEATH_ZONE_OFFSET = 100;
   private static final long POWER_UP_DURATION = 5000;
   private static final int HEALTH_GAIN_AMOUNT = 50;

   private boolean jumpPressed = false;
   private boolean canJump = true;

   private long lastDamageTime;
   private static final long DAMAGE_INVINCIBILITY_DURATION = 250;

   private SoundEffectPlayer soundPlayer;

   public Player(int x, int y, int width, int height) {
       super(x, y, width, height);
       this.dx = 0;
       this.dy = 0;
       this.maxHealth = 100;
       this.health = maxHealth;
       this.score = 0;
       this.normalSpeed = speed;
       this.soundPlayer = SoundEffectPlayer.getInstance();

       try {
           String texturePath = "src/main/resources/textures/" + LevelLoader.getCurrentTheme() + "/person.png";
           BufferedImage originalTexture = ImageIO.read(new File(texturePath));
           texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
           Graphics2D g2d = texture.createGraphics();
           g2d.drawImage(originalTexture, 0, 0, width, height, null);
           g2d.dispose();
           System.out.println("Successfully loaded player texture from: " + texturePath);
       } catch (IOException e) {
           e.printStackTrace();
           System.err.println("Failed to load player texture");
       }
   }

   public void setPosition(int x, int y) {
       setX(x);
       setY(y);
   }

   public void setMovement(boolean left, boolean right) {
       movingLeft = left;
       movingRight = right;
       updateHorizontalSpeed();
   }

   public boolean isMovingLeft() {
       return movingLeft;
   }

   public boolean isMovingRight() {
       return movingRight;
   }

   private void updateHorizontalSpeed() {
       if (movingLeft && !movingRight) {
           dx = -speed;
           facingRight = false;
       } else if (movingRight && !movingLeft) {
           dx = speed;
           facingRight = true;
       } else {
           dx = 0;
       }
   }

   public int getSpeed() {
       return speed;
   }

   public void setJumpPressed(boolean pressed) {
       this.jumpPressed = pressed;
   }

   @Override
   public void update() {
   }

   public void update(Level level) {
       if (knockbackTimer > 0) {
           knockbackTimer--;
       } else {
           updateHorizontalSpeed();
       }
       
       handleJump();
       applyGravity();
       move(level);

       checkEnemyCollisions(level);
       checkEndDoorCollision(level);
       
       if (getY() > level.getHeight() + DEATH_ZONE_OFFSET) {
           health = 0;
       }

       updatePowerUps();

       level.checkBlockCollision(this);

       if (onGround) {
           canJump = true;
           hasDoubleJumped = false;
       }

       if (isDamageInvincible() && System.currentTimeMillis() - lastDamageTime >= DAMAGE_INVINCIBILITY_DURATION) {
           lastDamageTime = 0;
       }

       if ((movingLeft || movingRight) && onGround) {
           soundPlayer.playSound("walk");
       }
   }

   private void updatePowerUps() {
       long currentTime = System.currentTimeMillis();

       if (currentTime > speedBoostEndTime) {
           speed = normalSpeed;
       }

       if (currentTime > invincibilityEndTime) {
           invincible = false;
       }

       if (currentTime > doubleJumpEndTime) {
           doubleJumpActive = false;
       }

       if (currentTime > healthPowerUpEndTime) {
           healthPowerUpActive = false;
       }
   }

   private void applyGravity() {
       if (!onGround) {
           dy += GRAVITY;
           if (dy > TERMINAL_VELOCITY) {
               dy = TERMINAL_VELOCITY;
           }
       }
   }

   private void handleJump() {
       if (jumpPressed) {
           if (onGround) {
               dy = JUMP_SPEED;
               onGround = false;
               hasDoubleJumped = false;
               jumpPressed = false;
               soundPlayer.playSound("jump");
           } else if (doubleJumpActive && !hasDoubleJumped) {
               dy = JUMP_SPEED;
               hasDoubleJumped = true;
               jumpPressed = false;
               soundPlayer.playSound("jump");
           }
       }
   }

   private void move(Level level) {
       int newX = getX() + dx;
       if (!checkCollision(newX, getY(), level)) {
           setX(newX);
       } else {
           while (!checkCollision(getX() + Integer.signum(dx), getY(), level)) {
               setX(getX() + Integer.signum(dx));
           }
       }

       int newY = getY() + Math.round(dy);
       if (!checkCollision(getX(), newY, level)) {
           setY(newY);
           onGround = false;
       } else {
           if (dy > 0) {
               while (!checkCollision(getX(), getY() + 1, level)) {
                   setY(getY() + 1);
               }
               onGround = true;
               hasDoubleJumped = false;
           } else if (dy < 0) {
               while (!checkCollision(getX(), getY() - 1, level)) {
                   setY(getY() - 1);
               }
           }
           dy = 0;
       }
   }

   private boolean checkCollision(int x, int y, Level level) {
       for (int i = x; i < x + getWidth(); i++) {
           for (int j = y; j < y + getHeight(); j++) {
               if (level.isSolid(i, j)) {
                   return true;
               }
           }
       }
       return false;
   }

   private void checkEnemyCollisions(Level level) {
       for (Enemy enemy : level.getEnemies()) {
           if (this.collidesWith(enemy) && knockbackTimer == 0) {
               if (this.dy > 0 && this.getY() + this.getHeight() < enemy.getY() + enemy.getHeight() / 2) {
                   level.removeEnemy(enemy);
                   addScore(SCORE_FOR_ENEMY_KILL);
                   dy = JUMP_SPEED / 2;
                   soundPlayer.playSound("enemy_defeat");
               } else {
                   applyKnockback(enemy);
                   takeDamage(DAMAGE_PER_HIT);
                   soundPlayer.playSound("player_hit");
               }
               break;
           }
       }
   }

   private void checkEndDoorCollision(Level level) {
       if (level.isEndDoorPlaced() && 
           getX() < level.getEndDoorX() + level.getTileSize() &&
           getX() + getWidth() > level.getEndDoorX()) {
           reachedEndDoor = true;
           soundPlayer.playSound("level_complete");
       }
   }

   private void applyKnockback(Enemy enemy) {
       int knockbackDirectionX = getX() < enemy.getX() ? -1 : 1;
       int knockbackDirectionY = getY() < enemy.getY() ? -1 : 1;
       dx = knockbackDirectionX * KNOCKBACK_SPEED;
       dy = knockbackDirectionY * KNOCKBACK_SPEED / 2;
       knockbackTimer = KNOCKBACK_DURATION;
       onGround = false;
   }

   @Override
   public void render(Graphics g) {
       render(g, 0);
   }

   public void render(Graphics g, int x) {
       Graphics2D g2d = (Graphics2D) g;
       if (texture != null) {
           AffineTransform transform = new AffineTransform();
           transform.translate(x, getY());
           if (!facingRight) {
               transform.translate(getWidth(), 0);
               transform.scale(-1, 1);
           }
           g2d.drawImage(texture, transform, null);
       } else {
           g.setColor(Color.BLUE);
           g.fillRect(x, getY(), getWidth(), getHeight());
       }

       if (invincible || isDamageInvincible()) {
           g.setColor(new Color(255, 255, 0, 128));
           g.fillRect(x, getY(), getWidth(), getHeight());
       }
       if (doubleJumpActive) {
           g.setColor(Color.WHITE);
           g.drawString("DJ", x, getY() - 5);
       }
       if (System.currentTimeMillis() <= speedBoostEndTime) {
           g.setColor(Color.CYAN);
           g.drawString("FAST", x, getY() - 20);
       }
       if (healthPowerUpActive) {
           g.setColor(Color.GREEN);
           g.drawString("HP UP", x, getY() - 35);
       }
   }

   public int getHealth() { return health; }
   public int getMaxHealth() { return maxHealth; }
   public int getScore() { return score; }

   public void takeDamage(int damage) {
       if (!isInvincible() && !isDamageInvincible()) {
           health -= damage;
           if (health > 0) {
               lastDamageTime = System.currentTimeMillis();
               soundPlayer.playSound("player_hit");
           }
           if (health < 0) health = 0;
       }
   }

   private boolean isDamageInvincible() {
       return System.currentTimeMillis() - lastDamageTime < DAMAGE_INVINCIBILITY_DURATION;
   }

   public void heal(int amount) {
       health += amount;
       if (health > maxHealth) health = maxHealth;
       soundPlayer.playSound("player_heal");
   }

   public void addScore(int points) {
       score += points;
       soundPlayer.playSound("score_increase");
   }

   public boolean hasReachedEndDoor() {
       return reachedEndDoor;
   }

   public void resetEndDoorStatus() {
       reachedEndDoor = false;
   }

   public void applyPowerUp(PowerUp powerUp) {
       long currentTime = System.currentTimeMillis();
       switch (powerUp.getType()) {
           case SPEED_BOOST:
               speed = normalSpeed * 2;
               speedBoostEndTime = currentTime + POWER_UP_DURATION;
               soundPlayer.playSound("powerup_speed");
               break;
           case HEALTH_GAIN:
               maxHealth += HEALTH_GAIN_AMOUNT;
               heal(HEALTH_GAIN_AMOUNT);
               healthPowerUpActive = true;
               healthPowerUpEndTime = currentTime + HEALTH_INDICATOR_DURATION;
               soundPlayer.playSound("powerup_health");
               break;
           case DOUBLE_JUMP:
               doubleJumpActive = true;
               doubleJumpEndTime = currentTime + POWER_UP_DURATION;
               hasDoubleJumped = false;
               soundPlayer.playSound("powerup_doublejump");
               break;
           case INVINCIBILITY:
               invincible = true;
               invincibilityEndTime = currentTime + POWER_UP_DURATION;
               soundPlayer.playSound("powerup_invincibility");
               break;
       }
   }

   public float getDy() {
       return dy;
   }

   private boolean isInvincible() {
       return invincible;
   }
}