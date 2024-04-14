package it.jacopo.nave;

public class Circle {
    private int x, y;  // Circle's center position
    private int radius;  // Circle's radius
    private int speed;  // Speed in pixels per update interval

    public Circle(int startX, int startY, int radius, int speed) {
        this.x = startX;
        this.y = startY;
        this.radius = radius;
        this.speed = speed;
    }

    // Method to update position of the circle
    public void updatePosition() {
        this.x -= speed;  // Move circle to the left
    }

    // Getters for properties to send to clients
    public int getX() { return x; }
    public int getY() { return y; }
    public int getRadius() { return radius; }
}
