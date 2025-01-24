package main.java;
import java.awt.Color;
import java.awt.Graphics;

public class Land extends GameObject {
    
    public Land(int x, int y, int width, int height) {
        super(x, y, width, height);
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
        g.setColor(new Color(139, 69, 19)); // Brown color for land (default)
        g.fillRect(getX() + offsetX, getY(), getWidth(), getHeight());
    }
}