package org.example;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Camera implements KeyListener {
    public double x, y, z;
    public double yaw, pitch;

    private boolean[] keys = new boolean[256]; // Tastenstatus

    public Camera(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0;
        this.pitch = 0;
    }

    public void update() {
        double speed = 0.1;
        double sensitivity = 2.0;

        double dirX = Math.cos(Math.toRadians(yaw));
        double dirZ = Math.sin(Math.toRadians(yaw));

        double strafeX = Math.cos(Math.toRadians(yaw + 90));
        double strafeZ = Math.sin(Math.toRadians(yaw + 90));

        if (keys[KeyEvent.VK_D]) { // Rechts
            x += dirX * speed;
            z += dirZ * speed;
        }
        if (keys[KeyEvent.VK_A]) { // Links
            x -= dirX * speed;
            z -= dirZ * speed;
        }
        if (keys[KeyEvent.VK_W]) { // Vor
            x += strafeX * speed;
            z += strafeZ * speed;
        }
        if (keys[KeyEvent.VK_S]) { // Zurück
            x -= strafeX * speed;
            z -= strafeZ * speed;
        }

        if (keys[KeyEvent.VK_RIGHT]) { // Nach rechts drehen
            yaw -= sensitivity;
        }
        if (keys[KeyEvent.VK_LEFT]) { // Nach links drehen
            yaw += sensitivity;
        }
        if (keys[KeyEvent.VK_DOWN]) { // Nach unten schauen
            pitch -= sensitivity;
            if (pitch < -89) pitch = -89;
        }
        if (keys[KeyEvent.VK_UP]) { // Nach oben schauen
            pitch += sensitivity;
            if (pitch > 89) pitch = 89;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
