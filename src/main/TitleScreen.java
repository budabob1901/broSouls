package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import javax.sound.sampled.*;

public class TitleScreen extends JPanel {

    private static final int SCREEN_WIDTH = 768;
    private static final int SCREEN_HEIGHT = 576;

    // Menu states - tracks which menu the player is in
    private enum MenuState {
        MAIN_MENU,
        SYSTEM_MENU
    }
    private MenuState currentState = MenuState.MAIN_MENU;

    // Main menu options and selection
    private String[] mainMenuOptions = {"CONTINUE", "LOAD GAME", "NEW GAME", "SYSTEM", "ONLINE"};
    private int selectedOption = 0;

    // System menu options and selection
    private String[] systemMenuOptions = {"VOLUME", "BRIGHTNESS", "CONTROLS", "AUDIO", "BACK"};
    private int selectedSystemOption = 0;

    // Volume control variables
    private int volumeLevel = 70; // 0-100%
    private boolean adjustingVolume = false; // True when using LEFT/RIGHT to adjust
    private Timer volumeBlinkTimer; // Makes volume bar blink when adjusting
    private boolean volumeBarVisible = true; // Toggles for blink effect

    // UI states
    private boolean pressAnyKeyVisible = true; // For blinking PRESS ANY KEY text
    private boolean showMenu = false; // True after pressing any key

    // Animations
    private Timer blinkTimer; // Controls PRESS ANY KEY blinking
    private Timer animationTimer; // Controls subtle title movement
    private float titleY = 100; // Y position of title (animates slightly)

    // Audio clips
    private Clip backgroundMusic; // Loops on title screen
    private Clip menuMoveSound;   // Plays when navigating menus (UP/DOWN)
    private Clip menuSelectSound; // Plays when selecting (ENTER/SPACE)
    private FloatControl volumeControl; // Controls music volume

    // References to other game components
    private GamePanel gamePanel; // The actual game
    private JFrame window;       // The main window

    /**
     * Constructor - Sets up the title screen
     * Called from: Main.java
     */
    public TitleScreen(JFrame window, GamePanel gamePanel) {
        this.window = window;
        this.gamePanel = gamePanel;

        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null);

        // Force audio system to wake up (fixes sound delay issue)
        try {
            AudioSystem.getClip();
        } catch (Exception e) {}

        loadSounds();      // Load all sound effects
        setupAnimations(); // Start menu animations
        playTitleMusic();  // Start background music
        setupInput();      // Set up keyboard controls
    }

    /**
     * Loads all sound files from the /res/sound/ folder
     * Called from: Constructor
     * Files needed: menu_move.wav, menu_select.wav
     */
    private void loadSounds() {
        try {
            // Load menu movement sound (UP/DOWN navigation)
            InputStream moveStream = getClass().getResourceAsStream("/sound/menu_move.wav");
            if (moveStream != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(moveStream);
                menuMoveSound = AudioSystem.getClip();
                menuMoveSound.open(audioIn);

                // Prime the sound - prevents delay on first play
                menuMoveSound.start();
                menuMoveSound.stop();
                menuMoveSound.setFramePosition(0);

                // Reset to beginning when sound finishes
                menuMoveSound.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        menuMoveSound.setFramePosition(0);
                    }
                });

                System.out.println("✓ Menu move sound loaded!");
            }

            // Load menu select sound (ENTER/SPACE)
            InputStream selectStream = getClass().getResourceAsStream("/sound/menu_select.wav");
            if (selectStream != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(selectStream);
                menuSelectSound = AudioSystem.getClip();
                menuSelectSound.open(audioIn);

                // Prime the sound
                menuSelectSound.start();
                menuSelectSound.stop();
                menuSelectSound.setFramePosition(0);

                menuSelectSound.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        menuSelectSound.setFramePosition(0);
                    }
                });

                System.out.println("✓ Menu select sound loaded!");
            }

        } catch (Exception e) {
            System.out.println("Error loading sounds: " + e.getMessage());
        }
    }

    /**
     * Starts the background music and sets up volume control
     * Called from: Constructor
     * File needed: title_screen.wav
     */
    private void playTitleMusic() {
        try {
            InputStream musicStream = getClass().getResourceAsStream("/sound/title_screen.wav");
            if (musicStream != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(musicStream);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioIn);

                // Get volume control for this clip
                if (backgroundMusic.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    volumeControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
                    setVolume(volumeLevel); // Apply saved volume setting
                }

                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); // Loop forever
                System.out.println("✓ Title music playing!");
            }
        } catch (Exception e) {
            System.out.println("Could not play music: " + e.getMessage());
        }
    }

    /**
     * Changes the music volume
     * Called from: handleSystemMenuInput() when LEFT/RIGHT pressed
     * @param volume 0-100 (0% = mute, 100% = 0dB)
     */
    private void setVolume(int volume) {
        if (volumeControl != null) {
            // Convert percentage to decibels (logarithmic scale)
            float min = volumeControl.getMinimum();

            float db;
            if (volume <= 0) {
                db = min; // Mute
            } else {
                // Map 1-100 to -40dB to 0dB
                db = (float)(20 * Math.log10(volume / 100.0));
                if (db < min) db = min;
            }

            volumeControl.setValue(db);
        }
    }

    /**
     * Plays a sound effect
     * Called from: handleMainMenuInput(), handleSystemMenuInput()
     * @param sound The Clip to play (menuMoveSound or menuSelectSound)
     */
    private void playSound(Clip sound) {
        if (sound != null) {
            try {
                if (sound.isRunning()) {
                    sound.stop();
                }
                sound.setFramePosition(0); // Rewind to beginning
                sound.start(); // Play once
            } catch (Exception e) {
                System.out.println("Error playing sound: " + e.getMessage());
            }
        }
    }

    /**
     * Sets up all animation timers
     * Called from: Constructor
     */
    private void setupAnimations() {
        // Blinks the "PRESS ANY KEY" text every 600ms
        blinkTimer = new Timer(600, e -> {
            pressAnyKeyVisible = !pressAnyKeyVisible;
            repaint();
        });
        blinkTimer.start();

        // Makes the title float up and down slightly
        animationTimer = new Timer(50, e -> {
            titleY = 100 + (float)(Math.sin(System.currentTimeMillis() / 500.0) * 3);
            repaint();
        });
        animationTimer.start();

        // Blinks the volume bar when adjusting (300ms interval)
        volumeBlinkTimer = new Timer(300, e -> {
            if (adjustingVolume) {
                volumeBarVisible = !volumeBarVisible;
                repaint();
            }
        });
    }

    /**
     * Sets up keyboard controls for the entire title screen
     * Called from: Constructor
     */
    private void setupInput() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();

                // Initial "PRESS ANY KEY" screen
                if (!showMenu) {
                    showMenu = true;
                    pressAnyKeyVisible = true;
                    repaint();
                    return;
                }

                // Route input to the correct menu handler
                switch (currentState) {
                    case MAIN_MENU:
                        handleMainMenuInput(code);
                        break;
                    case SYSTEM_MENU:
                        handleSystemMenuInput(code);
                        break;
                }
            }
        });

        // Mouse click also triggers "PRESS ANY KEY"
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!showMenu) {
                    showMenu = true;
                    repaint();
                }
            }
        });
    }

    /**
     * Handles keyboard input for the main menu
     * Called from: setupInput()
     * Controls: UP/DOWN to navigate, ENTER/SPACE to select
     */
    private void handleMainMenuInput(int code) {
        switch (code) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                selectedOption--;
                if (selectedOption < 0) selectedOption = mainMenuOptions.length - 1;
                playSound(menuMoveSound); // Navigation sound
                repaint();
                break;

            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                selectedOption++;
                if (selectedOption >= mainMenuOptions.length) selectedOption = 0;
                playSound(menuMoveSound); // Navigation sound
                repaint();
                break;

            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                playSound(menuSelectSound); // Selection sound

                // Handle different menu options
                if (selectedOption == 3) { // SYSTEM selected
                    currentState = MenuState.SYSTEM_MENU;
                    selectedSystemOption = 0; // Reset to first option
                    repaint();
                } else if (selectedOption == 2) { // NEW GAME selected
                    startGame();
                }
                // CONTINUE, LOAD GAME, ONLINE are placeholders for now
                break;
        }
    }

    /**
     * Handles keyboard input for the system menu
     * Called from: setupInput()
     * Controls: UP/DOWN to navigate, LEFT/RIGHT to adjust volume,
     *           ENTER/SPACE/ESC to go back
     */
    private void handleSystemMenuInput(int code) {
        switch (code) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (!adjustingVolume) { // Can't navigate while adjusting volume
                    selectedSystemOption--;
                    if (selectedSystemOption < 0) selectedSystemOption = systemMenuOptions.length - 1;
                    playSound(menuMoveSound);
                    repaint();
                }
                break;

            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if (!adjustingVolume) {
                    selectedSystemOption++;
                    if (selectedSystemOption >= systemMenuOptions.length) selectedSystemOption = 0;
                    playSound(menuMoveSound);
                    repaint();
                }
                break;

            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if (selectedSystemOption == 0) { // VOLUME selected
                    adjustingVolume = true;
                    volumeLevel = Math.max(0, volumeLevel - 5); // Decrease by 5%
                    setVolume(volumeLevel);
                    playSound(menuMoveSound);
                    repaint();

                    if (!volumeBlinkTimer.isRunning()) {
                        volumeBlinkTimer.start();
                    }
                }
                break;

            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if (selectedSystemOption == 0) { // VOLUME selected
                    adjustingVolume = true;
                    volumeLevel = Math.min(100, volumeLevel + 5); // Increase by 5%
                    setVolume(volumeLevel);
                    playSound(menuMoveSound);
                    repaint();

                    if (!volumeBlinkTimer.isRunning()) {
                        volumeBlinkTimer.start();
                    }
                }
                break;

            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                playSound(menuSelectSound);

                if (selectedSystemOption == systemMenuOptions.length - 1) { // BACK selected
                    currentState = MenuState.MAIN_MENU;
                    adjustingVolume = false;
                    volumeBarVisible = true;
                    volumeBlinkTimer.stop();
                    repaint();
                }
                break;

            case KeyEvent.VK_ESCAPE:
                playSound(menuSelectSound);
                currentState = MenuState.MAIN_MENU;
                adjustingVolume = false;
                volumeBarVisible = true;
                volumeBlinkTimer.stop();
                repaint();
                break;
        }

        // Exit volume adjustment mode when moving away from volume option
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN ||
                code == KeyEvent.VK_W || code == KeyEvent.VK_S) {
            adjustingVolume = false;
            volumeBarVisible = true;
            volumeBlinkTimer.stop();
        }
    }

    /**
     * Transitions from title screen to the actual game
     * Called from: handleMainMenuInput() when NEW GAME is selected
     */
    private void startGame() {
        // Stop all sounds
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
        if (menuMoveSound != null) menuMoveSound.close();
        if (menuSelectSound != null) menuSelectSound.close();

        // Stop all timers
        blinkTimer.stop();
        animationTimer.stop();
        if (volumeBlinkTimer != null) volumeBlinkTimer.stop();

        // Switch to game panel
        window.setContentPane(gamePanel);
        window.revalidate();
        gamePanel.requestFocusInWindow();
        gamePanel.startGameThread();
    }

    /**
     * Main paint method - draws everything on screen
     * Called from: repaint() and automatically by Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Smooth text rendering
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Pure black background (Elden Ring style)
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Very subtle film grain effect
        g2.setColor(new Color(255, 255, 255, 3));
        for (int i = 0; i < getHeight(); i += 4) {
            g2.drawLine(0, i, getWidth(), i);
        }

        // Draw the appropriate screen
        if (!showMenu) {
            drawPressAnyKeyScreen(g2);
        } else {
            switch (currentState) {
                case MAIN_MENU:
                    drawMainMenu(g2);
                    break;
                case SYSTEM_MENU:
                    drawSystemMenu(g2);
                    break;
            }
        }

        // Draw version number at bottom
        drawVersion(g2);
        g2.dispose();
    }

    /**
     * Draws the initial "PRESS ANY KEY" screen
     * Called from: paintComponent() when showMenu is false
     */
    private void drawPressAnyKeyScreen(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        if (pressAnyKeyVisible) {
            g2.setFont(new Font("Arial", Font.PLAIN, 16));
            g2.setColor(new Color(180, 180, 180)); // Light gray
            String text = "PRESS ANY KEY";
            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(text)) / 2;
            int y = h - 80;
            g2.drawString(text, x, y);
        }
    }

    /**
     * Draws the main menu with BRO SOULS title and options
     * Called from: paintComponent() when in MAIN_MENU state
     */
    private void drawMainMenu(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        // Draw "BRO SOULS" title (Elden Ring style)
        g2.setFont(new Font("Times New Roman", Font.BOLD, 56));
        g2.setColor(Color.WHITE);
        String title = "BRO SOULS";
        FontMetrics fm = g2.getFontMetrics();
        int titleX = (w - fm.stringWidth(title)) / 2;
        int titleY = (int)this.titleY; // Animated Y position
        g2.drawString(title, titleX, titleY);

        // Draw menu options
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        int startY = 250;
        int spacing = 40;

        for (int i = 0; i < mainMenuOptions.length; i++) {
            int y = startY + (i * spacing);

            if (i == selectedOption) {
                g2.setColor(Color.WHITE); // Selected option is white
                g2.drawString(">", w/2 - 100, y); // Cursor
                g2.drawString(mainMenuOptions[i], w/2 - 70, y);
            } else {
                g2.setColor(new Color(120, 120, 120)); // Unselected are gray
                g2.drawString(mainMenuOptions[i], w/2 - 70, y);
            }
        }

        // Bottom text (Dark Souls style)
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(100, 100, 100));
        g2.drawString("CALIBRATION", 30, h - 50);
    }

    /**
     * Draws the system menu with volume control
     * Called from: paintComponent() when in SYSTEM_MENU state
     */
    private void drawSystemMenu(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        // Draw "SYSTEM" title
        g2.setFont(new Font("Times New Roman", Font.BOLD, 48));
        g2.setColor(Color.WHITE);
        String title = "SYSTEM";
        FontMetrics fm = g2.getFontMetrics();
        int titleX = (w - fm.stringWidth(title)) / 2;
        g2.drawString(title, titleX, 120);

        // Draw system menu options
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        int startY = 200;
        int spacing = 45;

        for (int i = 0; i < systemMenuOptions.length; i++) {
            int y = startY + (i * spacing);

            if (i == selectedSystemOption) {
                g2.setColor(Color.WHITE);
                g2.drawString(">", w/2 - 150, y);
                g2.drawString(systemMenuOptions[i], w/2 - 120, y);
            } else {
                g2.setColor(new Color(120, 120, 120));
                g2.drawString(systemMenuOptions[i], w/2 - 120, y);
            }

            // Draw volume bar next to VOLUME option
            if (i == 0) { // VOLUME
                drawVolumeBar(g2, w/2 + 50, y - 5, i == selectedSystemOption);
            }
        }

        // Draw control hints at bottom
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(100, 100, 100));
        String hint = "← → ADJUST    ESC/ENTER BACK";
        fm = g2.getFontMetrics();
        int hintX = (w - fm.stringWidth(hint)) / 2;
        g2.drawString(hint, hintX, h - 80);
    }

    /**
     * Draws the volume bar and percentage
     * Called from: drawSystemMenu()
     * @param x X position of the bar
     * @param y Y position of the bar
     * @param isSelected Whether the volume option is currently selected
     */
    private void drawVolumeBar(Graphics2D g2, int x, int y, boolean isSelected) {
        int barWidth = 150;
        int barHeight = 10;
        int fillWidth = (int)((volumeLevel / 100.0) * barWidth);

        // Background bar
        g2.setColor(new Color(60, 60, 60));
        g2.fillRect(x, y, barWidth, barHeight);

        // Filled portion
        if (volumeLevel > 0) {
            // Color changes based on volume level
            if (volumeLevel <= 30) {
                g2.setColor(new Color(150, 150, 150)); // Low - gray
            } else if (volumeLevel <= 70) {
                g2.setColor(new Color(200, 200, 200)); // Medium - light gray
            } else {
                g2.setColor(Color.WHITE); // High - white
            }
            g2.fillRect(x, y, fillWidth, barHeight);
        }

        // Border
        g2.setColor(new Color(100, 100, 100));
        g2.drawRect(x, y, barWidth, barHeight);

        // Volume percentage text
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        if (isSelected && adjustingVolume && volumeBarVisible) {
            g2.setColor(Color.WHITE); // Blinking white when adjusting
        } else {
            g2.setColor(new Color(150, 150, 150)); // Gray when not adjusting
        }
        g2.drawString(volumeLevel + "%", x + barWidth + 15, y + barHeight);
    }

    /**
     * Draws the version number at bottom left (Elden Ring style)
     * Called from: paintComponent()
     */
    private void drawVersion(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(80, 80, 80));
        g2.drawString("AppVer.1.02.3", 30, h - 30);
    }

    /**
     * Clean up method - stops all sounds and timers
     * Called from: GamePanel when exiting
     */
    public void stopMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.close();
        }
        if (menuMoveSound != null) menuMoveSound.close();
        if (menuSelectSound != null) menuSelectSound.close();
        if (blinkTimer != null) blinkTimer.stop();
        if (animationTimer != null) animationTimer.stop();
        if (volumeBlinkTimer != null) volumeBlinkTimer.stop();
    }
}