package main;

import entity.Player;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {

    // Kamera
    public int cameraX;
    public int cameraY;

    // Tile settings
    final int originalTileSize = 16;
    final int scale = 5;
    public final int tileSize = originalTileSize * scale;

    // Skærm-størrelse
    final int maxScreenCol = 16;
    final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    // Verden-størrelse
    public int maxWorldCol = 50;
    public int maxWorldRow = 50;
    public int worldWidth = tileSize * maxWorldCol;
    public int worldHeight = tileSize * maxWorldRow;

    // FPS
    int FPS = 60;

    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
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
                remainingTime = remainingTime / 1_000_000;

                if (remainingTime < 0) remainingTime = 0;

                Thread.sleep((long) remainingTime);

                nextDrawTime += drawInterval;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {

        player.update();

        // Kamera følger spilleren normalt
        cameraX = player.x - screenWidth / 2;
        cameraY = player.y - screenHeight / 2;

        // Kamera-grænser
        if (cameraX < 0) cameraX = 0;
        if (cameraY < 0) cameraY = 0;

        if (cameraX > worldWidth - screenWidth)
            cameraX = worldWidth - screenWidth;

        if (cameraY > worldHeight - screenHeight)
            cameraY = worldHeight - screenHeight;
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Baggrund
        g2.setColor(Color.green);
        g2.fillRect(0 - cameraX, 0 - cameraY, worldWidth, worldHeight);

        // Test-firkanter
        g2.setColor(Color.gray);
        g2.fillRect(0 - cameraX, 0 - cameraY, 200, 200);

        g2.setColor(Color.gray);
        g2.fillRect(500 - cameraX, 500 - cameraY, 200, 200);

        g2.setColor(Color.gray);
        g2.fillRect(1000 - cameraX, 500 - cameraY, 200, 200);

        g2.setColor(Color.gray);
        g2.fillRect(500 - cameraX, 1000 - cameraY, 200, 200);

        g2.setColor(Color.gray);
        g2.fillRect(500 - cameraX, 2500 - cameraY, 200, 200);

        // Spiller
        player.draw(g2);

        g2.dispose();
    }
}