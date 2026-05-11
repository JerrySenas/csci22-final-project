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
import javax.imageio.ImageIO;

public abstract class Sprite {
    private double x;
    private double y;
    private double targetX;
    private double targetY;
    private double angle;

    private BufferedImage image;

    private double translationSpeed;

    public Sprite(double x, double y, BufferedImage sprite) {
        this.x = x;
        this.y = y;
        targetX = x;
        targetY = y;

        angle = 0;
        translationSpeed = 0.25;

        image = sprite;
    }

    public Sprite(double x, double y, String filepath) {
        this(x, y, loadImage(filepath));
    }
    
    public static BufferedImage loadImage(String filepath) {
        if (filepath.equals("")) {
            return null;
        }
        try {
            return ImageIO.read(new File(filepath));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void update() {
        if (x != targetX || y != targetY) {
            x += (targetX - x) * translationSpeed;
            y += (targetY - y) * translationSpeed;

            if (Math.abs(x - targetX) < 0.5) x = targetX;
            if (Math.abs(y - targetY) < 0.5) y = targetY;
        }
    }

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

    public double getX() {return x;}
    public double getY() {return y;}
    public double getAngle() {return angle;}
    public BufferedImage getImage() {return image;}

    public void setX(double x) {this.x = x;}
    public void setY(double y) {this.y = y;}
    public void setTargetX(double tx) {targetX = tx;}
    public void setTargetY(double ty) {targetY = ty;}
    public void setAngle(double a) {angle = a;}
    public void setTranslationSpeed(double ts) { translationSpeed = ts; }
    public void setImage(BufferedImage newImage) {image = newImage;}
    public void setImage(String filepath) { image = loadImage(filepath); }
}
