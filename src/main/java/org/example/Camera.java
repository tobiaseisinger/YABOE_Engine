package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class Camera implements KeyListener, MouseMotionListener {
    public double x, y, z;
    public double yaw, pitch;
    private int lastMouseX, lastMouseY;
    private boolean firstMouse = true;
    private Robot robot;
    private int centerX, centerY;
    private Component component;

    private boolean[] keys = new boolean[256];

    public Camera(double x, double y, double z, Component c) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0;
        this.pitch = 0;
        this.component = c;

        try {
            robot = new Robot();
            centerX = component.getWidth() / 2;
            centerY = component.getHeight() / 2;
            robot.mouseMove(centerX, centerY);
        } catch (AWTException e) {
            e.printStackTrace();
        }
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

        if (keys[KeyEvent.VK_SPACE]) { // Aufsteigen
            y -= speed;
        }
        if (keys[KeyEvent.VK_SHIFT]) { // Sinken
            y += speed;
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

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        if (firstMouse) {
            firstMouse = false;
            resetMouse();
            return;
        }

        Point center = new Point(component.getWidth() / 2, component.getHeight() / 2);
        SwingUtilities.convertPointToScreen(center, component);

        int dx = e.getXOnScreen() - center.x;
        int dy = e.getYOnScreen() - center.y;

        double sensitivity = 0.2;
        yaw -= dx * sensitivity;
        pitch -= dy * sensitivity;

        if (pitch > 89) pitch = 89;
        if (pitch < -89) pitch = -89;

        hideCursor();
        resetMouse();
    }

    private void resetMouse() {
        Point center = new Point(component.getWidth() / 2, component.getHeight() / 2);
        SwingUtilities.convertPointToScreen(center, component);
        robot.mouseMove(center.x, center.y);
    }

    private void hideCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.createImage(new byte[0]);
        Cursor invisibleCursor = toolkit.createCustomCursor(image, new Point(0, 0), "invisibleCursor");
        component.setCursor(invisibleCursor);
    }
}
