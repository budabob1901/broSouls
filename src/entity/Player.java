package entity;

import main.GamePanel;
import main.KeyHandler;
import org.json.JSONObject;
import org.json.JSONArray;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class Player extends Entity {

    GamePanel gp;
    KeyHandler keyH;

    private BufferedImage spriteSheet;

    private List<BufferedImage> idleFrames   = new ArrayList<>();
    private List<BufferedImage> runFrames    = new ArrayList<>();
    private List<BufferedImage> attackFrames = new ArrayList<>();
    private List<BufferedImage> hitFrames    = new ArrayList<>();
    private List<BufferedImage> deathFrames  = new ArrayList<>();

    private enum State { IDLE, RUN, ATTACK, HIT, DEAD }
    private State currentState = State.IDLE;

    private int animFrame  = 0;
    private int animTimer  = 0;
    private int animSpeed  = 6;
    private boolean facingRight = true;

    private int attackTimer = 0;
    private int hitTimer    = 0;

    // Each frame is 192x192, drawn at half size (96x96) so it fits nicely
    private final int drawSize = 250;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp   = gp;
        this.keyH = keyH;
        setDefaultValues();
        loadSpriteSheet();
    }

    public void setDefaultValues() {
        x         = 100;
        y         = 300;
        speed     = 4;
        direction = "right";
        maxHealth = 100;
        health    = maxHealth;
        isDead    = false;
    }

    private void loadSpriteSheet() {
        try {
            spriteSheet = ImageIO.read(
                    Objects.requireNonNull(
                            getClass().getResourceAsStream("/player/Skull Boy.png")));

            InputStream jsonStream =
                    getClass().getResourceAsStream("/player/Skull Boy.json");

            if (jsonStream == null) {
                System.err.println("Could not find Skull Boy.json!");
                return;
            }

            String jsonText = new String(jsonStream.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonText);

            // Build ordered list of all frames (frame 0, 1, 2 ... 45)
            JSONObject framesObj = root.getJSONObject("frames");
            int totalFrames = framesObj.length(); // 46 frames (0-45)
            BufferedImage[] allFrames = new BufferedImage[totalFrames];

            for (String key : framesObj.keySet()) {
                // Key is "Skull Boy 0.aseprite", "Skull Boy 1.aseprite" etc.
                // Extract the number from the key
                String numStr = key.replace("Skull Boy ", "").replace(".aseprite", "").trim();
                int index = Integer.parseInt(numStr);

                JSONObject entry = framesObj.getJSONObject(key);
                JSONObject frame = entry.getJSONObject("frame");
                int fx = frame.getInt("x");
                int fy = frame.getInt("y");
                int fw = frame.getInt("w");
                int fh = frame.getInt("h");
                allFrames[index] = spriteSheet.getSubimage(fx, fy, fw, fh);
            }

            // Read tags — from/to are 1-based in this JSON, so subtract 1
            JSONArray tags = root.getJSONObject("meta").getJSONArray("frameTags");

            for (int t = 0; t < tags.length(); t++) {
                JSONObject tag  = tags.getJSONObject(t);
                String     name = tag.getString("name").toLowerCase();
                int        from = tag.getInt("from") - 1; // convert to 0-based
                int        to   = tag.getInt("to")   - 1;

                List<BufferedImage> target = switch (name) {
                    case "idle"   -> idleFrames;
                    case "run"    -> runFrames;
                    case "attack" -> attackFrames;
                    case "hit"    -> hitFrames;
                    case "death"  -> deathFrames;
                    default       -> null;
                };

                if (target != null) {
                    for (int i = from; i <= to; i++) {
                        if (allFrames[i] != null) target.add(allFrames[i]);
                    }
                }
            }

            System.out.println("Loaded: idle=" + idleFrames.size()
                    + " run="    + runFrames.size()
                    + " attack=" + attackFrames.size()
                    + " hit="    + hitFrames.size()
                    + " death="  + deathFrames.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {

        if (isDead) {
            currentState = State.DEAD;
            tickAnim(deathFrames, false);
            return;
        }

        if (isHit) {
            hitTimer--;
            if (hitTimer <= 0) {
                isHit = false;
            } else {
                currentState = State.HIT;
                tickAnim(hitFrames, true);
                return;
            }
        }

        if (attacking) {
            attackTimer--;
            if (attackTimer <= 0) {
                attacking = false;
            } else {
                currentState = State.ATTACK;
                tickAnim(attackFrames, false);
                return;
            }
        }

        // Trigger attack on Space
        if (keyH.attackPressed && !attacking) {
            attacking    = true;
            attackTimer  = Math.max(1, attackFrames.size()) * animSpeed;
            animFrame    = 0;
            animTimer    = 0;
            currentState = State.ATTACK;
            return;
        }

        // Movement
        boolean moving = false;

        if (keyH.upPressed)    { y -= speed; moving = true; }
        if (keyH.downPressed)  { y += speed; moving = true; }
        if (keyH.leftPressed)  { x -= speed; facingRight = false; moving = true; }
        if (keyH.rightPressed) { x += speed; facingRight = true;  moving = true; }

        if (y < 200) y = 200;
        if (y > 600) y = 600;

        if (moving) {
            if (currentState != State.RUN) { animFrame = 0; animTimer = 0; }
            currentState = State.RUN;
            tickAnim(runFrames, true);
        } else {
            if (currentState != State.IDLE) { animFrame = 0; animTimer = 0; }
            currentState = State.IDLE;
            tickAnim(idleFrames, true);
        }
    }

    private void tickAnim(List<BufferedImage> frames, boolean loop) {
        if (frames.isEmpty()) return;
        animTimer++;
        if (animTimer >= animSpeed) {
            animTimer = 0;
            animFrame++;
            if (animFrame >= frames.size()) {
                animFrame = loop ? 0 : frames.size() - 1;
            }
        }
    }

    public void draw(Graphics2D g2) {

        List<BufferedImage> currentAnim = switch (currentState) {
            case DEAD   -> deathFrames;
            case HIT    -> hitFrames;
            case ATTACK -> attackFrames;
            case RUN    -> runFrames;
            default     -> idleFrames;
        };

        if (currentAnim.isEmpty()) return;

        int idx = Math.min(animFrame, currentAnim.size() - 1);
        BufferedImage frame = currentAnim.get(idx);

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
        // Tight hitbox around the character
        int hitW = 50;
        int hitH = 60;
        int hitX = x + (drawSize / 2) - (hitW / 2);
        int hitY = y + 80; // character sits about 80px down in the 192px frame
        return new Rectangle(hitX, hitY, hitW, hitH);
    }

    public void takeHit(int damage) {
        if (isDead || isHit) return;
        health -= damage;
        if (health <= 0) {
            health       = 0;
            isDead       = true;
            currentState = State.DEAD;
            animFrame    = 0;
        } else {
            isHit        = true;
            currentState = State.HIT;
            hitTimer     = Math.max(1, hitFrames.size()) * animSpeed;
            animFrame    = 0;
        }
    }
}