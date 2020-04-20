package com.arpg.game.utils;

public enum Direction {
    UP(0, 1, 90.0f, 2), DOWN(0, -1, 270.0f, 3), LEFT(-1, 0, 180.0f, 0), RIGHT(1, 0, 0.0f, 1);

    private int x;
    private int y;
    private int imageIndex;
    private float angle;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getImageIndex() {
        return imageIndex;
    }

    public float getAngle() {
        return angle;
    }

    Direction(int x, int y, float angle, int imageIndex) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.imageIndex = imageIndex;
    }
}
