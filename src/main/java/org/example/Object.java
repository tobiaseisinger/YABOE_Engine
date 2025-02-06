package org.example;

import java.awt.*;

public class Object {
    public Vertex[] vertices;
    public int color;
    public Texture texture;

    public Object(Vertex[] vertices, int color, Texture texture) {
        this.vertices = vertices;
        this.color = color;
        this.texture = texture;
    }
}
