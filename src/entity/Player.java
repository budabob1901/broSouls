package entity;

import main.GamePanel;
import main.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Player extends Entity {

    GamePanel gp;
    KeyHandler keyH;

    public Player(GamePanel gp, KeyHandler keyH){

        this.gp = gp;
        this.keyH = keyH;

        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {

        x = 100;
        y = 300; // Start midt i den lille zone
        speed = 12;
        direction = "down";
    }

    public void getPlayerImage(){
        try{

            up1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/player/r.piskel2.png")));
            up2 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/player/frem1.png")));
            down1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/player/r.piskel2.png")));
            down2 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/player/frem1.png")));
            left1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/player/left.r.png")));
            left2 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/player/left.r.png")));
            right1 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/player/right.r.png")));
            right2 = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/player/right.r.png")));

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void update() {

        if(keyH.upPressed) {
            direction = "up";
            y -= speed;
        }
        else if (keyH.downPressed){
            direction = "down";
            y += speed;
        }
        else if (keyH.leftPressed) {
            direction = "left";
            x -= speed;
        }
        else if (keyH.rightPressed) {
            direction = "right";
            x += speed;
        }

        // Verdens grænser i X
        if (x < 0) x = 0;
        if (x > gp.worldWidth - gp.tileSize)
            x = gp.worldWidth - gp.tileSize;

        // ⭐ MEGET mindre bevægelsesplads i Y
        int topLimit = 200;     // hvor højt op han må gå
        int bottomLimit = 600;  // hvor langt ned han må gå

        if (y < topLimit) y = topLimit;
        if (y > bottomLimit) y = bottomLimit;
    }

    public void draw(Graphics2D g2) {

        int screenX = x - gp.cameraX;
        int screenY = y - gp.cameraY;

        BufferedImage image = null;

        switch (direction) {
            case "up": image = up1; break;
            case "down": image = down1; break;
            case "left": image = left1; break;
            case "right": image = right1; break;
        }

        g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
    }
}