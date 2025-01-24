package main.java;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class StartScreen extends JPanel {
    private JButton playButton;
    private BufferedImage backgroundImage;
    private static final String BACKGROUND_PATH = "src/main/resources/textures/menu/menu_background.png";

    public StartScreen(ActionListener playButtonListener) {
        setLayout(new GridBagLayout());

        // Loads background image
        try {
            File backgroundFile = new File(BACKGROUND_PATH);
            if (backgroundFile.exists()) {
                backgroundImage = ImageIO.read(backgroundFile);
                System.out.println("Menu background loaded successfully from: " + BACKGROUND_PATH);
            } else {
                System.err.println("Could not find menu background at: " + BACKGROUND_PATH);
            }
        } catch (IOException e) {
            System.err.println("Failed to load menu background: " + e.getMessage());
            e.printStackTrace();
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Game title
        JLabel titleLabel = new JLabel("Jump Jump Revolution", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.BLACK);
        gbc.insets = new Insets(20, 0, 100, 0); // top, left, bottom, right
        add(titleLabel, gbc);

        // Play button
        playButton = new JButton("Play");
        playButton.setFont(new Font("Arial", Font.BOLD, 24));
        playButton.addActionListener(playButtonListener);
        playButton.setPreferredSize(new Dimension(200, 60));
        gbc.insets = new Insets(0, 0, 0, 0);
        add(playButton, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw background image or fallback color
        if (backgroundImage != null) {
            // Scale image to fit panel
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Fallback background color with gradient
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(25, 25, 112), // Dark blue
                0, getHeight(), new Color(0, 0, 51)  // Very dark blue
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}