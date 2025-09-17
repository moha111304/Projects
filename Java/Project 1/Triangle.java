import java.awt.Color;

public class Triangle {

    private double x;
    private double y;
    private double height;
    private double width;
    private Color color;

    // Constructor with arguments xPostion, yPosition, height, and width
    public Triangle(double x, double y, double height, double width) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }
    // Method to calculate and return the perimeter of the triangle
    public double calculatePerimeter() {
        return (this.width + (Math.sqrt((this.width * this.width) + (4 * (this.height * this.height)))));
    }
    // Method to calculate the area of the triangle
    public double calculateArea() {
        return (this.width * this.height) * (1.0/2);
    }
    // Method to set the color of the triangle  
    public void setColor(Color newColor) { 
        this.color = newColor;
    }
    // Method to set the position (x and y coordinates) of the triangle 
    public void setPos(double newXPosition, double newYPosition) {
        this.x = newXPosition;
        this.y = newYPosition;
    }
    // Method to set the height of the triangle 
    public void setHeight(double newHeight) {
        this.height = newHeight;
    }
    // Method to set the width of the triangle 
    public void setWidth(double newWidth) {
        this.width = newWidth;
    }
    // Method to get the color of the triangle 
    public Color getColor() {
        return this.color;
    }
    // Method to get the x position of the triangle 
    public double getXPos() {
        return this.x;
    }
    // Method to get the y position of the triangle 
    public double getYPos() {
        return this.y;
    }
    // Method to get the height of the triangle
    public double getHeight() {
        return this.height;
    }
    // Method to get the width of the triangle
    public double getWidth() {
        return this.width;
    }
}

// Written by Ayub Mohamoud, moha1660