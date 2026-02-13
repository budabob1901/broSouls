package main;

import objects.Door;
import objects.Platform;
import entity.Enemy;

import java.awt.*;

public class LevelManager {

    GamePanel gp;
    public Level[] levels;
    public int currentLevel = 0;

    public LevelManager(GamePanel gp) {
        this.gp = gp;

        levels = new Level[3];

        // Opret levels
        levels[0] = new Level("Start Area", "map1.txt", 200, 600, new Color(60, 120, 60));
        levels[1] = new Level("Forest", "map2.txt", 150, 550, new Color(40, 80, 40));
        levels[2] = new Level("Boss Room", "bossroom.txt", 250, 500, new Color(80, 40, 40));

        // Døre
        levels[0].addDoor(new Door(700, 500, 50, 100, 1, 100, 300));
        levels[1].addDoor(new Door(-50, 300, 50, 100, 0, 600, 300));
        levels[1].addDoor(new Door(400, 700, 100, 50, 2, 300, 200));
        levels[2].addDoor(new Door(300, 0, 100, 50, 1, 300, 500));

        // Platforme
        levels[0].addPlatform(new Platform(200, 400, 300, 20));

        // Fjender (med patrol AI)
        levels[0].addEnemy(new Enemy(gp, 300, 300, 200, 500));
        levels[1].addEnemy(new Enemy(gp, 400, 200, 350, 600));
        levels[2].addEnemy(new Enemy(gp, 250, 250, 200, 450));
    }

    public Level getLevel() {
        return levels[currentLevel];
    }

    public void switchTo(int index) {
        currentLevel = index;
    }
}