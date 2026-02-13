package main;

import java.io.*;
import java.awt.*;

public class TileManager {

    GamePanel gp;
    public int[][] mapData;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        mapData = new int[gp.maxScreenCol][gp.maxScreenRow];
    }

    public void loadMap(String fileName) {
        try {
            InputStream is = getClass().getResourceAsStream("/maps/" + fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int row = 0;

            while (row < gp.maxScreenRow) {
                String line = br.readLine();
                String[] numbers = line.split(" ");

                for (int col = 0; col < gp.maxScreenCol; col++) {
                    mapData[col][row] = Integer.parseInt(numbers[col]);
                }

                row++;
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {
        for (int row = 0; row < gp.maxScreenRow; row++) {
            for (int col = 0; col < gp.maxScreenCol; col++) {

                int tileNum = mapData[col][row];

                // midlertidig tegning
                if (tileNum == 1) {
                    g2.setColor(Color.darkGray);
                } else {
                    g2.setColor(Color.green);
                }

                int x = col * gp.tileSize - gp.cameraX;
                int y = row * gp.tileSize - gp.cameraY;

                g2.fillRect(x, y, gp.tileSize, gp.tileSize);
            }
        }
    }
}