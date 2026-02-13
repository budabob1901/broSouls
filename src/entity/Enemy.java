package entity;

import main.GamePanel;

public class Enemy extends Entity {

    GamePanel gp;

    // Patrol område
    int patrolMinX;
    int patrolMaxX;
    boolean movingRight = true;

    // AI ranges
    int chaseRange = 200;   // hvor tæt spilleren skal være før jagt
    int attackRange = 30;   // collision kill

    public Enemy(GamePanel gp, int x, int y, int patrolMinX, int patrolMaxX) {
        this.gp = gp;
        this.x = x;
        this.y = y;

        this.patrolMinX = patrolMinX;
        this.patrolMaxX = patrolMaxX;

        this.speed = 2;
        this.direction = "right";
    }

    public void update() {

        int playerX = gp.player.x;
        int playerY = gp.player.y;

        // Distance til spilleren
        int dx = playerX - x;
        int dy = playerY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // 1. Jagt spilleren hvis han er tæt nok
        if (distance < chaseRange) {
            chasePlayer(playerX, playerY);
        }
        // 2. Ellers patruljer
        else {
            patrol();
        }

        // 3. Collision kill
        if (distance < attackRange) {
            gp.player.health = 0;
            gp.player.isDead = true;
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
}