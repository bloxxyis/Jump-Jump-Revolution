package main.java;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JPanel;

class GamePanel extends JPanel {
    private HUD hud;
    private Player player;
    private Level currentLevel;
    private List<PowerUp> activePowerUps;
    private long startTime;

    public GamePanel(Player player, Level currentLevel, List<PowerUp> activePowerUps) {
        this.player = player;
        this.currentLevel = currentLevel;
        this.activePowerUps = activePowerUps;
        this.hud = new HUD(player);
        setFocusable(true);
        setRequestFocusEnabled(true);
        startTime = System.currentTimeMillis();
    }

    @Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    // Render sky background
    BufferedImage skyTexture = currentLevel.getSkyTexture();
    if (skyTexture != null) {
        int offsetX = Math.max(0, Math.min(player.getX() - 400, 
            currentLevel.getEndDoorX() - getWidth() + currentLevel.getTileSize()));
        int skyOffsetX = offsetX / 2;  // Parallax effect

        // Draws default sky background color
        g.setColor(new Color(0, 0, 51));  // Dark blue background
        g.fillRect(0, 0, getWidth(), getHeight());

        // decorative default layer 
        int treeLineHeight = skyTexture.getHeight();
        int yPosition = getHeight() - treeLineHeight;

        g.drawImage(skyTexture, 
                   -skyOffsetX % skyTexture.getWidth(), 
                   yPosition, 
                   skyTexture.getWidth(), 
                   treeLineHeight,
                   null);
                   
        for (int x = skyTexture.getWidth() - (skyOffsetX % skyTexture.getWidth()); 
             x < getWidth(); 
             x += skyTexture.getWidth()) {
            g.drawImage(skyTexture, 
                       x, 
                       yPosition, 
                       skyTexture.getWidth(), 
                       treeLineHeight,
                       null);
        }
    } else {
        g.setColor(new Color(135, 206, 235));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    // Rest of the rendering (level, player, power-ups, etc.)
    int offsetX = Math.max(0, Math.min(player.getX() - 400, 
        currentLevel.getEndDoorX() - getWidth() + currentLevel.getTileSize()));
    currentLevel.render(g, offsetX);

    // Render player
    player.render(g, player.getX() - offsetX);

    // Render power-ups
    for (PowerUp powerUp : activePowerUps) {
        powerUp.render(g, offsetX);
    }

    // Render HUD
    hud.render(g);

    // Render timer
    long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
    String timeString = String.format("%02d:%02d", elapsedTime / 60, elapsedTime % 60);
    g.setColor(Color.WHITE);
    g.setFont(g.getFont().deriveFont(20f));
    int timerWidth = g.getFontMetrics().stringWidth(timeString);
    g.drawString(timeString, (getWidth() - timerWidth) / 2, 30);
}

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocus();
    }

    public void setCurrentLevel(Level level) {
        this.currentLevel = level;
    }

    public void setPlayer(Player player) {
        this.player = player;
        this.hud.setPlayer(player);
    }

    public void setActivePowerUps(List<PowerUp> powerUps) {
        this.activePowerUps = powerUps;
    }

    public void resetTimer() {
        startTime = System.currentTimeMillis();
    }
}