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

    int chaseRange    = 300;
    int attackRange   = 60;
    int stopDistance  = 50;
    int damage        = 10;

    int attackCooldown = 0;
    int attackDelay    = 75;

    private final int size     = 50;
    private final int drawSize = 64;

    private List<BufferedImage> idleFrames   = new ArrayList<>();
    private List<BufferedImage> chaseFrames  = new ArrayList<>();
    private List<BufferedImage> attackFrames = new ArrayList<>();

    private enum State { IDLE, CHASE, ATTACK }
    private State currentState = State.IDLE;

    private int animFrame  = 0;
    private int animTimer  = 0;
    private int animSpeed  = 6; // ticks per frame
    private boolean facingRight = true;

    public Enemy(GamePanel gp, int x, int y, int patrolMinX, int patrolMaxX) {
        this.gp = gp;
        this.x  = x;
        this.y  = y;

        this.patrolMinX = patrolMinX;
        this.patrolMaxX = patrolMaxX;
        this.speed = 2;

        loadFrames(idleFrames,   "/enemy/Enemyidle.json",   "/enemy/Enemyidle.png");
        loadFrames(chaseFrames,  "/enemy/EnemyJump.json",   "/enemy/EnemyJump.png");
        loadFrames(attackFrames, "/enemy/EnemyAttack.json", "/enemy/EnemyAttack.png");
    }

    private int extractNumber(String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)(?!.*\\d)").matcher(key);
        if (m.find()) return Integer.parseInt(m.group());
        return 0;
    }

    private void loadFrames(List<BufferedImage> target, String jsonPath, String imgPath) {
        try {
            InputStream imgStream  = getClass().getResourceAsStream(imgPath);
            InputStream jsonStream = getClass().getResourceAsStream(jsonPath);

            if (imgStream == null || jsonStream == null) {
                System.err.println("Could not load: " + imgPath);
                return;
            }

            BufferedImage sheet = ImageIO.read(imgStream);
            String jsonText = new String(jsonStream.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonText);
            JSONObject framesObj = root.getJSONObject("frames");

            int total = framesObj.length();
            BufferedImage[] allFrames = new BufferedImage[total];

            List<String> sortedKeys = new ArrayList<>(framesObj.keySet());
            sortedKeys.sort((a, b) -> Integer.compare(extractNumber(a), extractNumber(b)));

            for (int i = 0; i < sortedKeys.size(); i++) {
                String key = sortedKeys.get(i);
                JSONObject frame = framesObj.getJSONObject(key).getJSONObject("frame");
                allFrames[i] = sheet.getSubimage(
                        frame.getInt("x"), frame.getInt("y"),
                        frame.getInt("w"), frame.getInt("h"));
            }

            for (BufferedImage f : allFrames) {
                if (f != null) target.add(f);
            }

            System.out.println("Loaded " + target.size() + " frames from " + imgPath);

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

        // Decide state and move
        if (distance < attackRange) {
            if (currentState != State.ATTACK) { animFrame = 0; animTimer = 0; }
            currentState = State.ATTACK;
        } else if (distance < chaseRange) {
            if (currentState != State.CHASE) { animFrame = 0; animTimer = 0; }
            currentState = State.CHASE;
            chasePlayer(playerX, playerY, distance);
        } else {
            if (currentState != State.IDLE) { animFrame = 0; animTimer = 0; }
            currentState = State.IDLE;
            patrol();
        }

        tickAnim();

        if (attackCooldown > 0) attackCooldown--;

        if (getBounds().intersects(gp.player.getBounds())) {
            if (attackCooldown == 0) {
                gp.player.takeHit(damage, x, y);
                attackCooldown = attackDelay;
            }
        }
    }

    private void tickAnim() {
        List<BufferedImage> frames = getCurrentFrames();
        if (frames.isEmpty()) return;

        animTimer++;
        if (animTimer >= animSpeed) {
            animTimer = 0;
            animFrame++;
            if (animFrame >= frames.size()) {
                animFrame = 0;
            }
        }
    }

    private List<BufferedImage> getCurrentFrames() {
        return switch (currentState) {
            case ATTACK -> attackFrames.isEmpty() ? idleFrames : attackFrames;
            case CHASE  -> chaseFrames.isEmpty()  ? idleFrames : chaseFrames;
            default     -> idleFrames;
        };
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
        List<BufferedImage> frames = getCurrentFrames();

        if (frames.isEmpty()) {
            g2.setColor(Color.red);
            g2.fillRect(x - gp.cameraX, y - gp.cameraY, size, size);
            return;
        }

        int idx = Math.min(animFrame, frames.size() - 1);
        BufferedImage frame = frames.get(idx);

        int screenX = x - gp.cameraX;
        int screenY = y - gp.cameraY;

        if (!facingRight) {
            g2.drawImage(frame, screenX + drawSize, screenY, -drawSize, drawSize, null);
        } else {
            g2.drawImage(frame, screenX, screenY, drawSize, drawSize, null);
        }
    }

    public Rectangle getBounds() {
        int hitW = 40;
        int hitH = 35;
        int hitX = x + (drawSize / 2) - (hitW / 2);
        int hitY = y + (drawSize / 2) - (hitH / 2);
        return new Rectangle(hitX, hitY, hitW, hitH);
    }
}