/**
The abstract class for any class that can be drawn onto the screen. Has support for rotation and translation.
@author Jerry Señas (255351) and Angelico Soriano (255468)
@version May 12, 2026

I have not discussed the Java language code in my program
with anyone other than my instructor or the teaching assistants
assigned to this course.

I have not used Java language code obtained from another student,
or any other unauthorized source, either modified or unmodified.

If any Java language code or documentation used in my program
was obtained from another source, such as a textbook or website,
that has been clearly noted with a proper citation in the comments
of my program.
*/

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public abstract class Sprite {
    private double x;
    private double y;
    private double targetX;
    private double targetY;
    private double angle;

    private BufferedImage image;

    private double translationSpeed;

    /**
     * Class constructor.
     * 
     * @param x The x-coordinate of the sprite
     * @param y The y-coordinate of the sprite
     * @param sprite The sprite's image
     */
    public Sprite(double x, double y, BufferedImage sprite) {
        this.x = x;
        this.y = y;
        targetX = x;
        targetY = y;

        angle = 0;
        translationSpeed = 0.50;

        image = sprite;
    }

    /**
     * Class constructor specifying the filepath of the image instead of the BufferedImage itself.
     */
    public Sprite(double x, double y, String filepath) {
        this(x, y, loadImage(filepath));
    }
    
    /**
     * Retrieves a BufferedImage of the file given by the filepath.
     * 
     * If filepath is an empty string, or if the file is not found or can not be converted to a BufferedImage, null is returned.
     * 
     * @param filepath The filepath of the image
     * @return A BufferedImage of the image file
     */
    public static BufferedImage loadImage(String filepath) {
        if (filepath.equals("")) {
            return null;
        }
        try {
            return ImageIO.read(new File(filepath));
        } catch (IOException e) {
            System.out.println("Image at " + filepath + " could not be loaded.");
            return null;
        }
    }

    /**
     * Moves the sprite towards (targetX, targetY).
     * 
     * The speed of the movement is dictated by translationSpeed. If the sprite's current position is close to the target, it jumps to the target position immediately.
     * 
     * Meant to be run within an animation timer.
     */
    public void update() {
        if (x != targetX || y != targetY) {
            x += (targetX - x) * translationSpeed;
            y += (targetY - y) * translationSpeed;

            if (Math.abs(x - targetX) < 0.5) x = targetX;
            if (Math.abs(y - targetY) < 0.5) y = targetY;
        }
    }

    /**
     * Draws the sprite's image.
     * 
     * Meant to be run within an animation timer.
     * 
     * @param g2d The Graphics2D object passed by GameCanvas.paintComponent
     */
    public void draw(Graphics2D g2d) {
        if (image == null) {
            return;
        }
        AffineTransform reset = g2d.getTransform();
        g2d.rotate(
            Math.toRadians(angle),
            x + image.getWidth() / 2,
            y + image.getHeight() / 2
        );
        g2d.drawImage(image, (int) x, (int) y, null);
        g2d.setTransform(reset);
    }

    /**
     * Returns the sprite's x-coordinate.
     */
    public double getX() {return x;}

    /**
     * Returns the sprite's y-coordinate.
     */
    public double getY() {return y;}

    /**
     * Returns the sprite's angle.
     */
    public double getAngle() {return angle;}

    /**
     * Returns the sprite's image.
     */
    public BufferedImage getImage() {return image;}

    /**
     * Sets the sprite's x-coordinate.
     * @param x The new x-coordinate
     */
    public void setX(double x) {this.x = x;}

    /**
     * Sets the sprite's y-coordinate.
     * @param y The new y-coordinate
     */
    public void setY(double y) {this.y = y;}

    /**
     * Sets the sprite's target x-coordinate.
     * @param tx The target x-coordinate
     */
    public void setTargetX(double tx) {targetX = tx;}

    /**
     * Sets the sprite's target y-coordinate.
     * @param ty The target y-coordinate
     */
    public void setTargetY(double ty) {targetY = ty;}

    /**
     * Sets the sprite's angle.
     * @param a The new angle
     */
    public void setAngle(double a) {angle = a;}

    /**
     * Sets the sprite's translation speed.
     * @param ts The new translation speed
     */
    public void setTranslationSpeed(double ts) { translationSpeed = ts; }

    /**
     * Sets the sprite's image given a BufferedImage.
     * @param newImage The new sprite image
     */
    public void setImage(BufferedImage newImage) {image = newImage;}

    /**
     * Sets the sprite's image given its filepath.
     * @param filepath The new sprite image's filepath
     */
    public void setImage(String filepath) { setImage(loadImage(filepath)); }
}
