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
import java.io.*;

public class TextBox extends Sprite {
    private String text, flavor;
    private final int width, height;
    private final Rectangle2D.Double outline;
    private Font mainFont;
    private Font flavorFont;

    /**
     * Class constructor. Flavor text is set as an empty string. Fonts used are loaded.
     * 
     * @param x X-coordinate of the sprite
     * @param y Y-coordinate of the sprite
     * @param w Width
     * @param h Height
     * @param txt Text of the textbox
     */
    public TextBox(double x, double y, int w, int h, String txt) {
        super(x, y, "");
        text = txt;
        flavor = "";
        width = w;
        height = h;
        outline = new Rectangle2D.Double(x, y, width, height);

        try {
            mainFont = Font.createFont(Font.TRUETYPE_FONT, new File("assets/bg/vcr_osd_mono.ttf")).deriveFont(Font.PLAIN, 16);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();   
            ge.registerFont(mainFont);
        } catch (IOException | FontFormatException e){
            e.printStackTrace();
        }

        try {
            flavorFont = Font.createFont(Font.TRUETYPE_FONT, new File("assets/bg/vcr_osd_mono.ttf")).deriveFont(Font.ITALIC, 12);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();   
            ge.registerFont(flavorFont);
        } catch (IOException | FontFormatException e){
            e.printStackTrace();
        }
    }

    /**
     * Sets the text of this TextBox.
     * 
     * Flavor text will be set to an empty string.
     * 
     * @param txt The text to be set to
     */
    public void setText(String txt) {
        text = txt;
        flavor = "";
    }

    /**
     * Sets and formats the item's name, description, and flavor text for this TextBox.
     * 
     * @param item The selected item
     */
    public void setItemText(Item item) {
        text = item.getName() + "\n";
        text += " - " + item.getDescription() + "\n\n";
        flavor = item.getFlavor();
    }

    /**
     * Sets and formats the environment's name, description, and flavor text for this TextBox.
     * 
     * @param env The selected environment
     */
    public void setEnvText(Environment env) {
        text = "Current environment: " + env.getName() + "\n";
        text += " - " + env.getDescription() + "\n\n";
        flavor = env.getFlavor();
    }

    /**
     * Renders the text of this TextBox in VCR OSD Mono 16 and the flavor text in VCR OSD Mono 12 Italic.
     * 
     * The text is centered horizontally and vertically within this TextBox.
     * 
     * Overrides the draw method of the Sprite class.
     * 
     * @param g2d The Graphics2D object passed by GameCanvas.paintComponent
     */
    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.draw(outline);
        g2d.setFont(mainFont);
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
        
        g2d.setFont(flavorFont);
        fm = g2d.getFontMetrics();
        for (String line : flavorLines) {
            int xOffset = (width - fm.stringWidth(line)) / 2;
            g2d.drawString(line, (int) getX() + xOffset, (int) getY() + yOffset);
            yOffset += fm.getHeight();
        }
    }
}
