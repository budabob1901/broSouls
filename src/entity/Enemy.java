package entity;

import main.GamePanel;

import java.awt.Rectangle;

public class Enemy extends Entity {

    GamePanel gp;

    int patrolMinX;
    int patrolMaxX;
    boolean movingRight = true;

    int chaseRange  = 20;
    int damage      = 10;

    // Size of enemy hitbox
    private final int size = 40;

    public Enemy(GamePanel gp, int x, int y, int patrolMinX, int patrolMaxX) {
        this.gp = gp;
        this.x  = x;
        this.y  = y;

        this.patrolMinX = patrolMinX;
        this.patrolMaxX = patrolMaxX;

        this.speed     = 2;
        this.direction = "right";
    }

    public void update() {

        int playerX = gp.player.x;
        int playerY = gp.player.y;

        int dx = playerX - x;
        int dy = playerY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < chaseRange) {
            chasePlayer(playerX, playerY);
        } else {
            patrol();
        }

        // Only deal damage when hitboxes actually overlap
        Rectangle enemyBox  = new Rectangle(x, y, size, size);
        Rectangle playerBox = gp.player.getBounds();

        if (enemyBox.intersects(playerBox)) {
            gp.player.takeHit(damage);
        }
    }

    private void chasePlayer(int playerX, int playerY) {
        if (playerX > x) x += speed;
        if (playerX < x) x -= speed;
        if (playerY > y) y += speed;
        if (playerY < y) y -= speed;
    }

    private void patrol() {
        if (movingRight) {
            x += speed;
            if (x > patrolMaxX) movingRight = false;
        } else {
            x -= speed;
            if (x < patrolMinX) movingRight = true;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }
}