package main;

import javax.swing.JFrame;

public class Main {

    public static void main(String[] args) {

        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setTitle("broSouls");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        // Gør vinduet fullscreen, men behold titelbar og kryds
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);

        window.setVisible(true);

        gamePanel.startGameThread();
    }
}