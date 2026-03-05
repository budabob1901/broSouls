package entity;

import main.GamePanel;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Enemy extends Entity {

    GamePanel gp;

    int patrolMinX;
    int patrolMaxX;
    boolean movingRight = true;

    int chaseRange   = 300;
    int stopDistance = 50;
    int damage       = 10;

    int attackCooldown = 0;
    int attackDelay    = 75;

    private final int size    = 50;
    private final int drawSize = 64; // drawn at 4x (16*4)

    // Animation
    private List<BufferedImage> frames = new ArrayList<>();
    private int animFrame  = 0;
    private int animTimer  = 0;
    private int animSpeed  = 6;
    private boolean facingRight = true;

    public Enemy(GamePanel gp, int x, int y, int patrolMinX, int patrolMaxX) {
        this.gp = gp;
        this.x  = x;
        this.y  = y;

        this.patrolMinX = patrolMinX;
        this.patrolMaxX = patrolMaxX;

        this.speed = 2;

        loadSpriteSheet();
    }

    private void loadSpriteSheet() {
        try {
            BufferedImage sheet = ImageIO.read(
                    getClass().getResourceAsStream("/enemy/Enemy.png"));

            InputStream jsonStream =
                    getClass().getResourceAsStream("/enemy/Enemy.json");

            if (sheet == null || jsonStream == null) {
                System.err.println("Could not load enemy sprite sheet!");
                return;
            }

            String jsonText = new String(jsonStream.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonText);
            JSONObject framesObj = root.getJSONObject("frames");

            int total = framesObj.length();
            BufferedImage[] allFrames = new BufferedImage[total];

            for (String key : framesObj.keySet()) {
                // Key is "Sprite-0002 0." — extract the LAST number (frame index)
                String[] parts = key.trim().split(" ");
                String numStr = parts[parts.length - 1].replaceAll("[^0-9]", "");
                int index = Integer.parseInt(numStr);
                JSONObject frame = framesObj.getJSONObject(key).getJSONObject("frame");
                allFrames[index] = sheet.getSubimage(
                        frame.getInt("x"), frame.getInt("y"),
                        frame.getInt("w"), frame.getInt("h"));
            }

            for (BufferedImage f : allFrames) {
                if (f != null) frames.add(f);
            }

            System.out.println("Enemy frames loaded: " + frames.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {

        int playerX = gp.player.x;
        int playerY = gp.player.y;

        int dx = playerX - x;
        int dy = playerY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < chaseRange) {
            chasePlayer(playerX, playerY, distance);
        } else {
            patrol();
        }

        // Tick animation
        animTimer++;
        if (animTimer >= animSpeed) {
            animTimer = 0;
            animFrame = (animFrame + 1) % Math.max(1, frames.size());
        }

        // Cooldown
        if (attackCooldown > 0) attackCooldown--;

        // Collision damage
        if (getBounds().intersects(gp.player.getBounds())) {
            if (attackCooldown == 0) {
                gp.player.takeHit(damage, x, y);
                attackCooldown = attackDelay;
            }
        }
    }

    private void chasePlayer(int playerX, int playerY, double distance) {
        if (distance <= stopDistance) return;
        if (playerX > x) { x += speed; facingRight = true; }
        if (playerX < x) { x -= speed; facingRight = false; }
        if (playerY > y) y += speed;
        if (playerY < y) y -= speed;
    }

    private void patrol() {
        if (movingRight) {
            x += speed;
            facingRight = true;
            if (x > patrolMaxX) movingRight = false;
        } else {
            x -= speed;
            facingRight = false;
            if (x < patrolMinX) movingRight = true;
        }
    }

    public void draw(Graphics2D g2) {
        if (frames.isEmpty()) {
            // Fallback: draw red square if no sprite loaded
            g2.setColor(Color.red);
            g2.fillRect(x - gp.cameraX, y - gp.cameraY, size, size);
            return;
        }

        int idx = Math.min(animFrame, frames.size() - 1);
        BufferedImage frame = frames.get(idx);

        int screenX = x - gp.cameraX;
        int screenY = y - gp.cameraY;

        if (!facingRight) {
            g2.drawImage(frame,
                    screenX + drawSize, screenY,
                    -drawSize, drawSize, null);
        } else {
            g2.drawImage(frame,
                    screenX, screenY,
                    drawSize, drawSize, null);
        }
    }

    public Rectangle getBounds() {
        // Center hitbox on the sprite (sprite draws at x,y with drawSize width/height)
        int hitW = 40;
        int hitH = 35;
        int hitX = x + (drawSize / 2) - (hitW / 2);
        int hitY = y + (drawSize / 2) - (hitH / 2);
        return new Rectangle(hitX, hitY, hitW, hitH);
    }
}