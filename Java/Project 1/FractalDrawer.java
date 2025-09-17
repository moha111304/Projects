// FractalDrawer class draws a fractal of a shape indicated by user input
import java.awt.Color;
import java.util.Scanner;

public class FractalDrawer {
    private double totalArea=0;  // member variable for tracking the total area

    public FractalDrawer() {}  // contructor

    // drawFractal method takes a 'type' parameter (circle, triangle, or rectangle) and draws the corresponding fractal
    public double drawFractal(String type) {
        Canvas can =  new Canvas(); // creates a new Canvas object
        Color c = new Color(255, 0, 0, 255); // creates a new Color object and sets the color of the largest level as red
       if (type.equals("circle")) {
            drawCircleFractal(100, 400, 400, c, can, 8);
            }
       if (type.equals("triangle")) {
            drawTriangleFractal(200, 300, 275, 400, c, can, 8);
            }
       if (type.equals("rectangle")) {
            drawRectangleFractal(150, 300, 300, 250, c, can, 8);
            }    
        return totalArea; // returns the area of the fractal
    }


    // drawTriangleFractal draws a triangle fractal using recursive techniques
    public void drawTriangleFractal(double width, double height, double x, double y, Color c, Canvas can, int level){
        if (level == 0) {
            return; // Base case: Stop recursion when the level reaches 0
        }

        Triangle myTriangle = new Triangle(x, y, height, width);

        // Set color based on the level
        if (level == 1) {
            c = Color.BLUE;
        }
        if (level == 2) {
            c = Color.RED;
        }
        if (level == 3) {
            c = Color.BLUE;
        }
        if (level == 4) {
            c = Color.GREEN;
        }
        if (level == 5) {
            c = Color.RED;
        }
        if (level == 6) {
            c = Color.BLUE;
        }
        if (level == 7) {
            c = Color.GREEN;
        }
        
        myTriangle.setColor(c);
        can.drawShape(myTriangle);
        totalArea += myTriangle.calculateArea();

        width = width / 2;
        height = height / 2;

        // Recursively draw smaller triangles
        drawTriangleFractal(width, height, x + (width/2), y + height, c, can, level - 1);
        drawTriangleFractal(width, height, x + (2 * width) , y, c, can, level - 1);
        drawTriangleFractal(width, height, x - width, y, c, can, level - 1);
    }

    
    // drawCircleFractal draws a circle fractal using recursive techniques
    public void drawCircleFractal(double radius, double x, double y, Color c, Canvas can, int level) {
        if (level == 0) {
            return; // Base case: Stop recursion when the level reaches 0
        }

        Circle myCircle = new Circle(x, y, radius);

        // Set color based on the level
        if (level == 1) {
            c = Color.BLUE;
        }
        if (level == 2) {
            c = Color.RED;
        }
        if (level == 3) {
            c = Color.BLUE;
        }
        if (level == 4) {
            c = Color.GREEN;
        }
        if (level == 5) {
            c = Color.RED;
        }
        if (level == 6) {
            c = Color.BLUE;
        }
        if (level == 7) {
            c = Color.GREEN;
        }
       
        myCircle.setColor(c);
        can.drawShape(myCircle);
        totalArea += myCircle.calculateArea();

        radius = radius / 2;
        double corner = 3 * radius;

        // Recursively draw smaller circles
        drawCircleFractal(radius, x - corner, y - corner, c, can, level - 1);
        drawCircleFractal(radius, x + corner, y - corner, c, can, level - 1);
        drawCircleFractal(radius, x - corner, y + corner, c, can, level - 1);
        drawCircleFractal(radius, x + corner, y + corner, c, can, level - 1);
    }


    // drawRectangleFractal draws a rectangle fractal using recursive techniques
    public void drawRectangleFractal(double width, double height, double x, double y, Color c, Canvas can, int level) {
        if (level == 0) {
            return; // Base case: Stop recursion when the level reaches 0
        }
        
        Rectangle myRectangle = new Rectangle(x, y, height, width);
        
        // Set color based on the level
        if (level == 1) {
            c = Color.BLUE;
        }
        if (level == 2) {
            c = Color.RED;
        }
        if (level == 3) {
            c = Color.BLUE;
        }
        if (level == 4) {
            c = Color.GREEN;
        }
        if (level == 5) {
            c = Color.RED;
        }
        if (level == 6) {
            c = Color.BLUE;
        }
        if (level == 7) {
            c = Color.GREEN;
        }
       
        myRectangle.setColor(c);
        can.drawShape(myRectangle);
        totalArea += myRectangle.calculateArea(); 

        width = width / 2;
        height = height / 2;

        // Recursively draw smaller rectangles
        drawRectangleFractal(width, height, x - (width/2), y - (height/2), c, can, level - 1);
        drawRectangleFractal(width, height, x + (1.5 * width), y - (height/2), c, can, level - 1);
        drawRectangleFractal(width, height, x - (width/2), y + (1.5 * height), c, can, level - 1);
        drawRectangleFractal(width, height, x + (1.5 * width), y + (1.5 * height), c, can, level - 1);
    }


    // main asks user for shape input, and then draw the corresponding fractal.
    public static void main(String[] args){
        FractalDrawer fractal = new FractalDrawer();
        System.out.println("(Choices: “circle”, “triangle”, or “rectangle”) ");
        Scanner myScanner = new Scanner(System.in);
        String input = myScanner.nextLine();
        System.out.println("Chosen: " + input); 
        System.out.println(fractal.drawFractal(input)); //prints area of fractal
        myScanner.close();
    }
}

// Written by Ayub Mohamoud, moha1660