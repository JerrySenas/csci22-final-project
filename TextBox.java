/**
A Sprite that essentially functions as a text box. When given an Item or an Environment, it displays its name, description, and flavor text.
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

public class TextBox extends Sprite {
    private String text, flavor;
    private final int width, height;
    private final Rectangle2D.Double outline;
    public TextBox(double x, double y, int w, int h, String txt) {
        super(x, y, "");
        text = txt;
        flavor = "";
        width = w;
        height = h;
        outline = new Rectangle2D.Double(x, y, width, height);
    }

    public void setText(String txt) {
        text = txt;
        flavor = "";
    }

    public void setItemText(Item item) {
        text = item.getName() + "\n";
        text += " - " + item.getDescription() + "\n\n";
        flavor = item.getFlavor();
    }

    public void setEnvText(Environment env) {
        text = "Current environment: " + env.getName() + "\n";
        text += " - " + env.getDescription() + "\n\n";
        flavor = env.getFlavor();
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.draw(outline);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        FontMetrics fm = g2d.getFontMetrics();
        String[] textLines = text.split("\n");
        String[] flavorLines = flavor.split("\n");

        int totalHeight = fm.getHeight() * (textLines.length + flavorLines.length);
        int yOffset = (height - totalHeight) / 2 + fm.getAscent();
        for (String line : textLines) {
            int xOffset = (width - fm.stringWidth(line)) / 2;
            g2d.drawString(line, (int) getX() + xOffset, (int) getY() + yOffset);
            yOffset += fm.getHeight();
        }

        g2d.setFont(new Font("Arial", Font.ITALIC, 14));
        fm = g2d.getFontMetrics();
        for (String line : flavorLines) {
            int xOffset = (width - fm.stringWidth(line)) / 2;
            g2d.drawString(line, (int) getX() + xOffset, (int) getY() + yOffset);
            yOffset += fm.getHeight();
        }
    }
}
