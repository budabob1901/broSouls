package entity;

import main.GamePanel;
import java.awt.Rectangle;

public class Enemy extends Entity {

    GamePanel gp;

    int patrolMinX;
    int patrolMaxX;
    boolean movingRight = true;

    int chaseRange   = 300;
    int stopDistance = 50;   // perfekt afstand til player hitbox
    int damage       = 10;

    int attackCooldown = 0;
    int attackDelay    = 75; // 1.25 sek cooldown

    private final int size = 50; // centreret hitbox

    public Enemy(GamePanel gp, int x, int y, int patrolMinX, int patrolMaxX) {
        this.gp = gp;
        this.x  = x;
        this.y  = y;

        this.patrolMinX = patrolMinX;
        this.patrolMaxX = patrolMaxX;

        this.speed = 2;
    }

    public void update() {

        int playerX = gp.player.x;
        int playerY = gp.player.y;

        int dx = playerX - x;
        int dy = playerY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Chase player
        if (distance < chaseRange) {
            chasePlayer(playerX, playerY, distance);
        } else {
            patrol();
        }

        // Cooldown
        if (attackCooldown > 0) attackCooldown--;

        // Collision damage
        if (getBounds().intersects(gp.player.getBounds())) {
            if (attackCooldown == 0) {

                // SEND BEGGE KOORDINATER TIL PLAYER
                gp.player.takeHit(damage, x, y);

                attackCooldown = attackDelay;
            }
        }
    }

    private void chasePlayer(int playerX, int playerY, double distance) {

        // Stop tæt på spilleren
        if (distance <= stopDistance) return;

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
        return new Rectangle(
                x - size/2,
                y - size/2,
                size,
                size
        );
    }
}