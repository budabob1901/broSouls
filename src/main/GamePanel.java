package main;

import entity.Player;
import objects.Door;
import objects.Platform;
import entity.Enemy;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {

    // Kamera
    public int cameraX;
    public int cameraY;

    // Tile settings
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale;

    // Skærm
    final int maxScreenCol = 16;
    final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    // FPS
    int FPS = 60;

    // Systemer
    public LevelManager levelManager = new LevelManager(this);
    KeyHandler keyH = new KeyHandler();
    Thread gameThread;

    // Player
    public Player player = new Player(this, keyH);

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {

        double drawInterval = 1000000000 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {

            update();
            repaint();

            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime /= 1_000_000;

                if (remainingTime < 0) remainingTime = 0;

                Thread.sleep((long) remainingTime);

                nextDrawTime += drawInterval;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {

        // Update player
        player.update();

        // Kamera følger spilleren
        cameraX = player.x - screenWidth / 2 + tileSize / 2;
        cameraY = player.y - screenHeight / 2 + tileSize / 2;

        // Door-collision
        Level level = levelManager.getLevel();

        for (Door d : level.doors) {
            if (player.getBounds().intersects(d.area)) {
                levelManager.switchTo(d.targetLevel);
                player.x = d.spawnX;
                player.y = d.spawnY;
            }
        }
        for (Enemy e : level.enemies) {
            e.update();
        }

    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        Level level = levelManager.getLevel();

        // BAGGRUND
        g2.setColor(level.backgroundColor);
        g2.fillRect(0 - cameraX, 0 - cameraY, level.width, level.height);

        // PLATFORME
        g2.setColor(Color.darkGray);
        for (Platform p : level.platforms) {
            g2.fillRect(
                    p.area.x - cameraX,
                    p.area.y - cameraY,
                    p.area.width,
                    p.area.height
            );
        }

        // DØRE
        g2.setColor(Color.blue);
        for (Door d : level.doors) {
            g2.fillRect(
                    d.area.x - cameraX,
                    d.area.y - cameraY,
                    d.area.width,
                    d.area.height
            );
        }

        // FJENDER
        g2.setColor(Color.red);
        for (Enemy e : level.enemies) {
            g2.fillRect(
                    e.x - cameraX,
                    e.y - cameraY,
                    40,
                    40
            );
        }

        // PLAYER
        player.draw(g2);

        g2.dispose();
    }
}