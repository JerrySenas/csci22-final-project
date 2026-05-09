import java.awt.*;
import java.awt.geom.*;

public class TextBox extends Sprite {
    private String text;
    private final int width, height;
    private final Rectangle2D.Double outline;
    public TextBox(double x, double y, int w, int h, String txt) {
        super(x, y, "");
        text = txt;
        width = w;
        height = h;
        outline = new Rectangle2D.Double(x, y, width, height);
    }

    public void setText(String txt) { text = txt; }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.draw(outline);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        FontMetrics fm = g2d.getFontMetrics();
        String[] lines = text.split("\n");

        int totalHeight = fm.getHeight() * lines.length;
        int yOffset = (height - totalHeight) / 2 + fm.getAscent();
        for (String line : lines) {
            int xOffset = (width - fm.stringWidth(line)) / 2;
            g2d.drawString(line, (int) getX() + xOffset, (int) getY() + yOffset);
            yOffset += fm.getHeight();
        }
    }
}
