package entity;

import java.awt.image.BufferedImage;

public class Entity {
    public int x, y;
    public int speed;
    public int runSpeed;

    // Animation frames
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public BufferedImage attackUp1, attackUp2, attackDown1, attackDown2;
    public BufferedImage attackLeft1, attackLeft2, attackRight1, attackRight2;
    public BufferedImage heavyAttackUp, heavyAttackDown, heavyAttackLeft, heavyAttackRight;

    public String direction;

    // Combat stats (Dark Souls 3 style)
    public int maxHealth;
    public int health;
    public int stamina;
    public int maxStamina;
    public int staminaRegen;
    public int attackPower;
    public int heavyAttackPower;
    public int defense;
    public int staminaCost;
    public int heavyStaminaCost;

    // State management
    public boolean attacking = false;
    public boolean heavyAttacking = false;
    public boolean isRunning = false;
    public boolean isHit = false;
    public boolean isDead = false;

    // Animation
    public int spriteCounter = 0;
    public int spriteNum = 1;
    public int attackCounter = 0;
    public int hitCounter = 0;
}