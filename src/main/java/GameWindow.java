package main.java;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;

public class GameWindow extends JFrame {
   private Player player;
   private Level currentLevel;
   private GamePanel gamePanel;
   private StartScreen startScreen;
   private CardLayout cardLayout;
   private JPanel mainPanel;
   private Timer gameTimer;
   private List<PowerUp> activePowerUps = new ArrayList<>();
   private MusicPlayer musicPlayer;

   private static GameWindow instance;
   private static final float DEFAULT_MUSIC_VOLUME = 0.75f;

   public GameWindow() {
       instance = this;

       setTitle("Jump Jump Revolution");
       setSize(1000, 650);
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       cardLayout = new CardLayout();
       mainPanel = new JPanel(cardLayout);

       startScreen = new StartScreen(e -> showLevelSelectionDialog());
       mainPanel.add(startScreen, "StartScreen");

       add(mainPanel);

       musicPlayer = new MusicPlayer();
       musicPlayer.setVolume(DEFAULT_MUSIC_VOLUME);
       System.out.println("MusicPlayer initialized with volume: " + DEFAULT_MUSIC_VOLUME);

       setVisible(true);
   }

   private void setMusicVolume(float volume) {
       if (musicPlayer != null) {
           musicPlayer.setVolume(volume);
           System.out.println("setVolume called on musicPlayer with volume: " + volume);
       } else {
           System.err.println("musicPlayer is null when trying to set volume");
       }
   }

   private void startBackgroundMusic() {
       System.out.println("startBackgroundMusic called");
       try {
           setMusicVolume(DEFAULT_MUSIC_VOLUME);
           musicPlayer.playBackgroundMusic();
           System.out.println("playBackgroundMusic method called on musicPlayer");
       } catch (Exception e) {
           System.err.println("Error playing background music: " + e.getMessage());
           e.printStackTrace();
       }
   }

   private void stopBackgroundMusic() {
       System.out.println("stopBackgroundMusic called");
       musicPlayer.stop();
   }

   private void showLevelSelectionDialog() {
       String[] options = {"Desert Level (Easy)", "Christmas Level (Intermediate)", "Halloween Level (Hard)"};
       int choice = JOptionPane.showOptionDialog(this,
           "Select Level:",
           "Level Selection",
           JOptionPane.DEFAULT_OPTION,
           JOptionPane.QUESTION_MESSAGE,
           null,
           options,
           options[0]);

       switch(choice) {
           case 0:
               startGame("level_1");
               break;
           case 1:
               startGame("level_2");
               break;
           case 2:
               startGame("level_3");
               break;
           default:
               cardLayout.show(mainPanel, "StartScreen");
               break;
       }
   }

   private void startGame(String levelName) {
       System.out.println("Starting game with level: " + levelName);
       try {
           currentLevel = LevelLoader.loadLevel(levelName);
           if (currentLevel == null) {
               System.err.println("Failed to load level: " + levelName);
               JOptionPane.showMessageDialog(this, "Error loading level", "Error", JOptionPane.ERROR_MESSAGE);
               return;
           }

           player = new Player(currentLevel.getPlayerStartX(), currentLevel.getPlayerStartY() - 60, 40, 60);
           currentLevel.setPlayer(player);

           gamePanel = new GamePanel(player, currentLevel, activePowerUps);
           mainPanel.add(gamePanel, "GamePanel");

           gamePanel.addKeyListener(new KeyAdapter() {
               @Override
               public void keyPressed(KeyEvent e) {
                   handleInput(e);
               }

               @Override
               public void keyReleased(KeyEvent e) {
                   handleInputReleased(e);
               }
           });

           cardLayout.show(mainPanel, "GamePanel");
           gamePanel.requestFocusInWindow();

           startBackgroundMusic();

           gameTimer = new Timer(16, e -> {
               updateGame();
               gamePanel.repaint();
           });
           gameTimer.start();

       } catch (Exception e) {
           e.printStackTrace();
           System.err.println("Error in startGame: " + e.getMessage());
           JOptionPane.showMessageDialog(this, "Error starting game: " + e.getMessage(), 
               "Error", JOptionPane.ERROR_MESSAGE);
       }
   }

   private void handleInput(KeyEvent e) {
       switch (e.getKeyCode()) {
           case KeyEvent.VK_LEFT:
               player.setMovement(true, player.isMovingRight());
               break;
           case KeyEvent.VK_RIGHT:
               player.setMovement(player.isMovingLeft(), true);
               break;
           case KeyEvent.VK_UP:
               player.setJumpPressed(true);
               break;
           case KeyEvent.VK_ESCAPE:
               showPauseMenu();
               break;
       }
   }

   private void handleInputReleased(KeyEvent e) {
       switch (e.getKeyCode()) {
           case KeyEvent.VK_LEFT:
               player.setMovement(false, player.isMovingRight());
               break;
           case KeyEvent.VK_RIGHT:
               player.setMovement(player.isMovingLeft(), false);
               break;
           case KeyEvent.VK_UP:
               player.setJumpPressed(false);
               break;
       }
   }

   private void showPauseMenu() {
       gameTimer.stop();
       String[] options = {"Resume", "Return to Menu", "Exit Game"};
       int choice = JOptionPane.showOptionDialog(this,
           "Game Paused",
           "Pause Menu",
           JOptionPane.DEFAULT_OPTION,
           JOptionPane.PLAIN_MESSAGE,
           null,
           options,
           options[0]);

       switch(choice) {
           case 0: // Resume
               gameTimer.start();
               gamePanel.requestFocusInWindow();
               break;
           case 1: // Return to Menu
               stopBackgroundMusic();
               cardLayout.show(mainPanel, "StartScreen");
               break;
           case 2: // Exit Game
               System.exit(0);
               break;
           default:
               gameTimer.start();
               gamePanel.requestFocusInWindow();
               break;
       }
   }

   private void updateGame() {
       player.update(currentLevel);
       int offsetX = Math.max(0, Math.min(player.getX() - 400, 
           currentLevel.getEndDoorX() - getWidth() + currentLevel.getTileSize()));
       currentLevel.update(player.getX(), offsetX);
   
       Iterator<PowerUp> powerUpIterator = activePowerUps.iterator();
       while (powerUpIterator.hasNext()) {
           PowerUp powerUp = powerUpIterator.next();
           if (player.collidesWith(powerUp)) {
               player.applyPowerUp(powerUp);
               powerUpIterator.remove();
           }
       }
   
       if (player.getX() < currentLevel.getPlayerStartX()) {
           player.setPosition(currentLevel.getPlayerStartX(), player.getY());
       }
   
       currentLevel.checkBlockCollision(player);
   
       if (player.getHealth() <= 0) {
           gameOver();
       }
   
       if (player.hasReachedEndDoor()) {
           completeGame();
       }
   }

   private void gameOver() {
       System.out.println("gameOver called");
       gameTimer.stop();
       stopBackgroundMusic();
       SwingUtilities.invokeLater(() -> {
           String message = (player.getY() > currentLevel.getHeight()) ? 
               "Game Over! You fell to your doom!\nYour score: " + player.getScore() :
               "Game Over!\nYour score: " + player.getScore();
           JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
           cardLayout.show(mainPanel, "StartScreen");
       });
   }

   private void completeGame() {
       System.out.println("completeGame called");
       gameTimer.stop();
       stopBackgroundMusic();
       SwingUtilities.invokeLater(() -> {
           JOptionPane.showMessageDialog(this, 
               "Congratulations! You completed the level!\nYour score: " + player.getScore(), 
               "Level Complete", JOptionPane.INFORMATION_MESSAGE);
           cardLayout.show(mainPanel, "StartScreen");
       });
   }

   public static GameWindow getInstance() {
       return instance;
   }

   public static void main(String[] args) {
       SwingUtilities.invokeLater(() -> new GameWindow());
   }
}