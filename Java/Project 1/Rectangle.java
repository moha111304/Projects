import java.awt.Color;

public class Rectangle {

    private double x;
    private double y;
    private double height;
    private double width;
    private Color color;

    // Constructor with arguments xPosition, yPosition, height, and width
    public Rectangle(double x, double y, double height, double width){
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }
    // Method to calculate the perimeter of the rectangle using it's height and width
    public double calculatePerimeter() {
        return ((this.height * 2) + (this.width * 2));
    }
    // Method to calculate the area of the rectangle using it's height and width
    public double calculateArea() {
        return this.width * this.height;
    }
    // Method to set the color of the rectangle 
    public void setColor(Color newColor) {
        this.color = newColor;
    }
    // Method to set the the position (x and y coordinates) of the rectangle 
    public void setPos(double newXPosition, double newYPosition) {
        this.x = newXPosition;
        this.y = newYPosition;
    }
    // Method to set the the height of the rectangle 
    public void setHeight(double newHeight) {
        this.height = newHeight;
    }
    // Method to set the width of the rectangle 
    public void setWidth(double newWidth) {
        this.width = newWidth;
    }
    // Method to get the the color of the rectangle 
    public Color getColor() {
        return this.color;
    }
    // Method to get the x position of the rectangle 
    public double getXPos() {
        return this.x;
    }
    // Method to get the y position of the rectangle 
    public double getYPos() {
        return this.y;
    }
    // Method to get the height of the rectangle 
    public double getHeight() {
        return this.height;
    }
    // Method to get the width of the rectangle 
    public double getWidth() {
        return this.width;
    }
}

// Written by Ayub Mohamoud, moha1660