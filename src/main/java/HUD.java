package main.java;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class HUD {
    private Player player;

    public HUD(Player player) {
        this.player = player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void render(Graphics g) {
        // Render health bar
        g.setColor(Color.GRAY);
        g.fillRect(10, 10, 200, 20);
        g.setColor(Color.RED);
        int healthWidth = (int) ((player.getHealth() / (double) player.getMaxHealth()) * 200);
        g.fillRect(10, 10, healthWidth, 20);
        g.setColor(Color.WHITE);
        g.drawRect(10, 10, 200, 20);

        // Render health text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        String healthText = player.getHealth() + "/" + player.getMaxHealth();
        g.drawString(healthText, 85, 27);

        // Render score
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + player.getScore(), 10, 50);
    }
}