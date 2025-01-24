package main.java;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class PowerUp extends GameObject {
    public enum Type {
        SPEED_BOOST,
        HEALTH_GAIN,
        DOUBLE_JUMP,
        INVINCIBILITY
    }

    private Type type;
    private static final Random random = new Random();

    public PowerUp(int x, int y, int width, int height, Type type) {
        super(x, y, width, height);
        this.type = type;
    }

    public Type getType() {
        return type;
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
        Color color;
        switch (type) {
            case SPEED_BOOST:
                color = Color.CYAN;
                break;
            case HEALTH_GAIN:
                color = Color.GREEN;
                break;
            case DOUBLE_JUMP:
                color = Color.MAGENTA;
                break;
            case INVINCIBILITY:
                color = Color.YELLOW;
                break;
            default:
                color = Color.WHITE;
        }
        g.setColor(color);
        g.fillOval(getX() - offsetX, getY(), getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.drawOval(getX() - offsetX, getY(), getWidth(), getHeight());
    }

    public static PowerUp getRandomPowerUp(int x, int y) {
        Type[] types = Type.values();
        Type randomType = types[random.nextInt(types.length)];
        return new PowerUp(x, y, 30, 30, randomType);
    }
}