package objects;

import java.awt.*;

public class Door {

    public Rectangle area;
    public int targetLevel;
    public int spawnX, spawnY;

    public Door(int x, int y, int w, int h, int targetLevel, int spawnX, int spawnY) {
        this.area = new Rectangle(x, y, w, h);
        this.targetLevel = targetLevel;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }
}