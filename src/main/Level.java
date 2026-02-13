package main;

import objects.Door;
import objects.Platform;
import entity.Enemy;

import java.awt.*;
import java.util.ArrayList;

public class Level {

    // Map info
    public String name;
    public String mapFile;        // fx "map1.txt"
    public int width;
    public int height;

    // Player movement limits
    public int topLimit;
    public int bottomLimit;

    // Visuals
    public Color backgroundColor;

    // Objects in this level
    public ArrayList<Door> doors = new ArrayList<>();
    public ArrayList<Platform> platforms = new ArrayList<>();
    public ArrayList<Enemy> enemies = new ArrayList<>();

    public Level(String name, String mapFile, int topLimit, int bottomLimit, Color backgroundColor) {
        this.name = name;
        this.mapFile = mapFile;
        this.topLimit = topLimit;
        this.bottomLimit = bottomLimit;
        this.backgroundColor = backgroundColor;

        // Default size (kan ændres når du loader map)
        this.width = 1000;
        this.height = 1000;
    }

    // Adders
    public void addDoor(Door d) {
        doors.add(d);
    }

    public void addPlatform(Platform p) {
        platforms.add(p);
    }

    public void addEnemy(Enemy e) {
        enemies.add(e);
    }
}