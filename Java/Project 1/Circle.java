import java.awt.Color;

public class Circle {

    private double radius;
    private double y;
    private double x;
    private Color color;

    // Constructor with arguments xPosition, yPosition, and radius
    public Circle(double x, double y, double radius){
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
    // Method to calcalute perimeter of the circle using it's radius
    public double calculatePerimeter() {
        return (this.radius * Math.PI);
    }
    // Method to calcalute area of the circle using it's radius
    public double calculateArea() { 
        return (this.radius * this.radius) * Math.PI;
    }
    // Method to set the color of the circle
    public void setColor(Color newColor) {
        this.color = newColor;
    }
    // Method to set the set the position (x and y coordinates) of the circle 
    public void setPos(double newXPosition, double newYPosition) {
        this.x = newXPosition;
        this.y = newYPosition;
    }
    // Method to set the radius of the circle
    public void setRadius(double newRadius) {
        this.radius = newRadius;
    }
    // Method to get the color of the circle
    public Color getColor() {
        return this.color;
    }
    // Method to get the x position of the circle
    public double getXPos() {
        return this.x;

    }
    // Method to get the y position of the circle
    public double getYPos() {
        return this.y;
    }
    // Method to get the radius of the circle
    public double getRadius() {
        return this.radius;
    }
}

// Written by Ayub Mohamoud, moha1660