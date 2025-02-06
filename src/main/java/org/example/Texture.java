package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

public class Texture {
    private BufferedImage image;
    private int width, height;
    private int[] pixels;

    public Texture(String path) {
        try {
            image = ImageIO.read(new File(path));
            width = image.getWidth();
            height = image.getHeight();
            pixels = image.getRGB(0, 0, width, height, null, 0, width);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPixel(float u, float v) {
        int x = (int) (u * (width - 1));
        int y = (int) (v * (height - 1));

        System.out.println("Fetching pixel at UV: " + u + ", " + v + " -> (" + x + ", " + y + ") = " + Integer.toHexString(pixels[y * width + x]));
        return pixels[y * width + x];
    }
}
