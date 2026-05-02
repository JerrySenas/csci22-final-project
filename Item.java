import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public enum Item {
    EMPTY (0, ""),
    CIGARETTE (1, "assets/ciggy.png"),
    BEER (2, "assets/beer.png"),
    HANDCUFFS (3, "assets/handcuffs.png"),
    GLASS (4, "assets/glass.png");

    private final int itemNum;
    private BufferedImage sprite;

    private Item(int itemNum, String spritePath) {
        this.itemNum = itemNum;
        if (itemNum == 0) {
            sprite = null;
            return;
        }
        try {
            sprite = ImageIO.read(new File(spritePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Item getItem(int itemNum) {
        for (Item item : Item.values()) {
            if (item.itemNum == itemNum) {
                return item;
            }
        }
        System.out.printf("Item with itemNumber: %d doesn't exist\n", itemNum);
        return null;
    }
    public int getItemNum() { return itemNum; }
    public BufferedImage getSprite() { return sprite; }
}
