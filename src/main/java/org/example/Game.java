package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

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
        camera = new Camera(0, 0, -5, this);
        addKeyListener(camera);
        addMouseMotionListener(camera);
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

    private int frameCounter = 0;

    public void render() {
        frameCounter++;
        System.out.println("Render Frame: " + frameCounter);
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
        Object obj = new Object(cube, 2, new Texture("src/main/resources/brick.png"));

        renderObject(obj);

        pixels[width / 2 + height / 2 * width] = 0xFF0000;

        Graphics g = bs.getDrawGraphics();
        g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);

        g.dispose();
        bs.show();
    }

    private void pixel(int x, int y, int c) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            pixels[y * width + x] = c;
        }
    }

    private void renderObject(Object obj) {
        int numVertices = obj.vertices.length;
        if (numVertices < 3) return; // Keine gültige Fläche

        int[] screenX = new int[numVertices];
        int[] screenY = new int[numVertices];
        float[] texU = {0, 1, 1, 0};
        float[] texV = {0, 0, 1, 1};
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

        /** for (int i = 0; i < numVertices; i++) {
            int next = (i + 1) % numVertices;
            if (inFront[i] && inFront[next]) {
                drawLine(screenX[i], screenY[i], screenX[next], screenY[next], obj.color);
            }
        }**/

        for (int i = 1; i < numVertices - 1; i++) {
            if (inFront[0] && inFront[i] && inFront[i + 1]) {
                fillTexturedTriangle(screenX[0], screenY[0], texU[0], texV[0],
                        screenX[i], screenY[i], texU[i], texV[i],
                        screenX[i + 1], screenY[i + 1], texU[i + 1], texV[i + 1], obj.texture);
            }
        }
    }

    private void fillTexturedTriangle(int x0, int y0, float u0, float v0,
                                      int x1, int y1, float u1, float v1,
                                      int x2, int y2, float u2, float v2, Texture tex) {
        int minX = Math.min(x0, Math.min(x1, x2));
        int maxX = Math.max(x0, Math.max(x1, x2));
        int minY = Math.min(y0, Math.min(y1, y2));
        int maxY = Math.max(y0, Math.max(y1, y2));

        float denom = (x1 - x0) * (y2 - y0) - (y1 - y0) * (x2 - x0);
        if (denom == 0) return;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float w0 = ((x1 - x) * (y2 - y) - (y1 - y) * (x2 - x)) / denom;
                float w1 = ((x2 - x) * (y0 - y) - (y2 - y) * (x0 - x)) / denom;
                float w2 = 1.0f - w0 - w1;

                if (w0 >= 0 && w1 >= 0 && w2 >= 0) {  // Punkt ist innerhalb des Dreiecks
                    float u = w0 * u0 + w1 * u1 + w2 * u2;
                    float v = w0 * v0 + w1 * v1 + w2 * v2;
                    pixel(x, y, tex.getPixel(u, v));
                }
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

    // Der scheiß hat 1 1/2 stunden oder so gebraucht und ich check gefühlt nix ich kann nicht mehr (Scanline stuff)
    private void fillTriangle(int x0, int y0, int x1, int y1, int x2, int y2, int color) {
        if (y0 > y1) { int t; t = y0; y0 = y1; y1 = t; t = x0; x0 = x1; x1 = t; }
        if (y0 > y2) { int t; t = y0; y0 = y2; y2 = t; t = x0; x0 = x2; x2 = t; }
        if (y1 > y2) { int t; t = y1; y1 = y2; y2 = t; t = x1; x1 = x2; x2 = t; }

        int totalHeight = y2 - y0;

        for (int i = 0; i < totalHeight; i++) {
            boolean secondHalf = i > y1 - y0 || y1 == y0;
            int segmentHeight = secondHalf ? y2 - y1 : y1 - y0;
            float alpha = (float) i / totalHeight;
            float beta = (float) (i - (secondHalf ? y1 - y0 : 0)) / segmentHeight;

            int Ax = (int) (x0 + (x2 - x0) * alpha);
            int Bx = secondHalf ? (int) (x1 + (x2 - x1) * beta) : (int) (x0 + (x1 - x0) * beta);

            if (Ax > Bx) {
                int t = Ax; Ax = Bx; Bx = t;
            }

            for (int j = Ax; j <= Bx; j++) {
                pixel(j, y0 + i, color);
            }
        }
    }

    private void clearBackground() {
        Arrays.fill(pixels, 0x003C82);
    }

    private void renderTextToPixels(String text, int x, int y, int color) {
        BufferedImage textImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = textImage.createGraphics();

        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(Color.WHITE);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontMetrics fm = g2d.getFontMetrics();
        int textX = x;
        int textY = y + fm.getAscent();
        g2d.drawString(text, textX, textY);
        g2d.dispose();

        int[] textPixels = ((DataBufferInt) textImage.getRaster().getDataBuffer()).getData();
        for (int ty = 0; ty < height; ty++) {
            for (int tx = 0; tx < width; tx++) {
                int pixel = textPixels[ty * width + tx];

                if ((pixel >> 24) != 0x00) {
                    pixels[ty * width + tx] = color;
                }
            }
        }
    }

    private void renderUI() {
        renderTextToPixels("YABOE alpha v0.1", 10, 30, 0xFFFFFF);
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1_000_000_000.0 / 60.0;
        double delta = 0;

        long lastTimer = System.currentTimeMillis();
        int frames = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            boolean shouldRender = false;

            while (delta >= 1) {
                delta--;
                camera.update();
                clearBackground();
                shouldRender = true;
            }

            if (shouldRender) {
                render();
                frames++;
            }

            // FPS Limit & Display
            if (System.currentTimeMillis() - lastTimer >= 1000) {
                System.out.println("FPS: " + frames);
                lastTimer += 1000;
                frames = 0;
            }

            try {
                Thread.sleep(2); // Kleine Pause, um CPU-Auslastung zu reduzieren
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
