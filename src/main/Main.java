package main;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Bro Souls");

        // Create game panel but don't start it yet
        GamePanel gamePanel = new GamePanel();

        // Create title screen
        TitleScreen titleScreen = new TitleScreen(window, gamePanel);

        // Add title screen to window
        window.add(titleScreen);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}