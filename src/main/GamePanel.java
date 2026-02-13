package main;

import entity.Player;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    // Screen Setting
    public int cameraX;
    public int cameraY;
    final int originalTileSize = 16; //16X16 tile
    final int scale = 3;

    public final int tileSize = originalTileSize * scale; // 48x48
    final int maxScreenCol = 16;
    final int maxScreenRow = 12;
    final int screenWidth = tileSize * maxScreenCol; //768 pixels
    final int screenHeight = tileSize * maxScreenRow;  // 576 pixels




    //FPS
    int FPS = 60;

    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    Player player = new Player(this,keyH);

    // set player default position

    int playerX = 100;
    int playerY = 100;
    int playerSpeed = 4;

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
        double drawInterval = 1000000000/FPS; // TEGNER BILLEDET HERT 0,01666 SEKUND
        double nextDrawTime = System.nanoTime() + drawInterval;

     while(gameThread != null){
         // 1 UPDATE: update information such as charchter position
         update();

         // 2 DRAW: draw the screen with the updated information
         repaint();



         try {
             double remainingTime = nextDrawTime - System.nanoTime();
             remainingTime = remainingTime/1000000;

             if(remainingTime < 0) {
                 remainingTime = 0;
             }
             Thread.sleep((long) remainingTime);

             nextDrawTime += drawInterval;

         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }

    }

     // i jave starter kordnator 0x, 0y i vesnte øverste side af hjørnet.
    public void update() {

        player.update();

        cameraX = player.x - screenWidth / 2 + this.tileSize / 2;
        cameraY = player.y - screenHeight / 2 + this.tileSize /  2;


    }
    //Graphic is a class methods to draw object on our screen
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        // Graphic 2D extends the graphic class :)
        Graphics2D g2 = (Graphics2D)g;
        // 2 LINJER TIL AT SE HAN BEVÆGER SIG
        g2.setColor(Color.gray);
        g2.fillRect(0 - cameraX, 0 - cameraY, 200, 200);


        player.draw(g2);

        //program works without this but save memory
        g2.dispose();


    }

}