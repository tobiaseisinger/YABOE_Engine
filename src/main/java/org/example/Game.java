package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class Game extends JFrame implements Runnable {
    public int width, height;
    private Thread thread;
    private boolean running = true;
    private BufferedImage img;
    public int[] pixels;
    private Camera camera;

    public Game(int width, int height) {
        this.width = width;
        this.height = height;
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        camera = new Camera(0, 0, -5);
        addKeyListener(camera);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, height);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
        start();
    }

    public synchronized void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        Vertex[] cube = {
                new Vertex(-1, -1, 3),
                new Vertex(1, -1, 3),
                new Vertex(1, 1, 3),
                new Vertex(-1, 1, 3)
        };
        Object obj = new Object(cube, 2);

        renderObject(obj);

        Graphics g = bs.getDrawGraphics();
        g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
        bs.show();
    }

    private void pixel(int x, int y, int c) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }

        int[][] colors = {
                {255, 255, 0},   // Gelb
                {160, 160, 0},   // Dunkelgelb
                {0, 255, 0},     // Grün
                {0, 160, 0},     // Dunkelgrün
                {0, 255, 255},   // Cyan
                {0, 160, 160},   // Dunkelcyan
                {160, 100, 0},   // Braun
                {110, 50, 0},    // Dunkelbraun
                {0, 60, 130}     // Blau
        };

        int[] rgb = (c >= 0 && c < colors.length) ? colors[c] : new int[]{255, 255, 255};
        pixels[y * width + x] = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    private void renderObject(Object obj) {
        int numVertices = obj.vertices.length;
        int[] screenX = new int[numVertices];
        int[] screenY = new int[numVertices];
        boolean[] inFront = new boolean[numVertices];

        for (int i = 0; i < numVertices; i++) {
            Vertex v = obj.vertices[i];

            double dx = v.x - camera.x;
            double dy = v.y - camera.y;
            double dz = v.z - camera.z;

            double tempX = dx * Math.cos(Math.toRadians(-camera.yaw)) - dz * Math.sin(Math.toRadians(-camera.yaw));
            double tempZ = dx * Math.sin(Math.toRadians(-camera.yaw)) + dz * Math.cos(Math.toRadians(-camera.yaw));

            double tempY = dy * Math.cos(Math.toRadians(-camera.pitch)) - tempZ * Math.sin(Math.toRadians(-camera.pitch));
            tempZ = dy * Math.sin(Math.toRadians(-camera.pitch)) + tempZ * Math.cos(Math.toRadians(-camera.pitch));

            if (tempZ > 0) {
                screenX[i] = (int) ((tempX / tempZ) * width + width / 2);
                screenY[i] = (int) ((tempY / tempZ) * height + height / 2);
                inFront[i] = true;
            } else {
                inFront[i] = false;
            }
        }

        for (int i = 0; i < numVertices; i++) {
            int next = (i + 1) % numVertices;
            if (inFront[i] && inFront[next]) {
                drawLine(screenX[i], screenY[i], screenX[next], screenY[next], obj.color);
            }
        }
    }


    private void drawLine(int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            pixel(x1, y1, color);

            if (x1 == x2 && y1 == y2) break;
            int e2 = err * 2;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private void clearBackground() {
        int x,y;
        for (y = 0; y < height; y++) {
            for (x = 0; x < width; x++) {
                pixel(x, y, 8);
            }
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                delta--;
                camera.update();
                clearBackground();
            }
            render();
        }
    }
}
