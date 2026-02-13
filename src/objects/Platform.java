package objects;

import java.awt.*;

public class Platform {

    public Rectangle area;

    public Platform(int x, int y, int width, int height) {
        this.area = new Rectangle(x, y, width, height);
    }
}