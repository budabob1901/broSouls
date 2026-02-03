package main;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    // Screen Setting
    final int originalTileSize = 16; //16X16 tile
    final int scale = 3;

    final int tileSize = originalTileSize * scale; // 48x48
    final int maxScreenCol = 16;
    final int maxScreenRow = 12;
    final int screenWidth = tileSize * maxScreenCol; //768 pixels
    final int screenHeight = tileSize * maxScreenRow;  // 576 pixels

    Thread gameThread;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);

    }

    public void startGameThread() {

        gameThread = new Thread(this);
        gameThread.start();

    }

    @Override
    public void run() {
     while(gameThread != null){

       //  System.out.println("The game loop is running");
         // 1 UPDATE: update information such as charchter position
         update();
         // 2 DRAW: draw the screen with the updated information
         repaint();
     }

    }

    public void update() {

    }
    //Graphic is a class methods to draw object on our screen
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        // Graphic 2D extends the graphic class :)
        Graphics2D g2 = (Graphics2D)g;

        g2.setColor(Color.white);
        //DRAWS A RECTANGLE
        g2.fillRect(100, 100, tileSize, tileSize);
    }
}