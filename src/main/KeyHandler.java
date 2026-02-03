package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    //instans datatype boolean
    public boolean upPressed, downPressed, leftPressed, rightPressed;


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
      int code = e.getKeyCode();

      if (code ==KeyEvent.VK_W){
          upPressed = true;
      }
      if (code ==KeyEvent.VK_S){
          downPressed = true;
        }
      if (code ==KeyEvent.VK_A){
          leftPressed = true;
        }
      if (code ==KeyEvent.VK_D){
         rightPressed = true;
        }
    }
    // til at registere at knappen er blevet sluppet
    @Override
    public void keyReleased(KeyEvent e) {
         int code = e.getKeyCode();

        if (code ==KeyEvent.VK_W){
            upPressed = false;
        }
        if (code ==KeyEvent.VK_S){
            downPressed = false;
        }
        if (code ==KeyEvent.VK_A){
            leftPressed = false;
        }
        if (code ==KeyEvent.VK_D){
            rightPressed = false;
        }
    }
}
