
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


public enum Item {
    EMPTY (0, "", true, "", ""),
    CIGARETTE (1, "assets/ciggy.png", true, "Cigarette Pack", "Heals 1 HP."),
    BEER (2, "assets/beer.png", true, "Beer", "Ejects the current shell."),
    HANDCUFFS (3, "assets/handcuffs.png", true, "Handcuffs", "Skip the opponent's next turn."),
    GLASS (4, "assets/glass.png", true, "Magnifying Glass", "Check the current round in the chamber."),
    REVERSE (5, "assets/reverse.png", true, "Seija's Calling Card", "Swaps the polarity of the current round in the chamber."),
    CASING (6, "assets/casing.png", true, "The Rest of the Bullet", "If the current bullet is live, it deals 1 more damage."),
    
    DEST_WHITE(90, "assets/white_egg.png", false, "Destruction in White", "Destroy all other items. If at least 2 were destroyed, heal 1 HP and turn this item into a Destruction in Black."),
    DEST_BLACK(91, "assets/black_egg.png", false, "Destruction in Black", "Destroy all other items. If at least 2 were destroyed, deal 1 damage and turn this item into a Destruction in White."),
    DISD_CLAWS(92, "assets/claws.png", false, "Claws of Ardent Disdain", "If the current bullet is live, it deals 1 more damage. Doesn't disappear when the rack is reset."),
    ;

    private final int itemNum;
    private final String spritePath;
    private final boolean randomAccessible;
    private final String name;
    private final String description;

    private Item(int itemNum, String path, boolean randAccess, String name, String desc) {
        this.itemNum = itemNum;
        spritePath = path;
        randomAccessible = randAccess;
        this.name = name;
        description = desc;
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
    public static Item getRandomItem() {
        ArrayList<Item> validItems = new ArrayList<>();
        for (Item item : Item.values()) {
            if (item.randomAccessible) { validItems.add(item); }
        }
        return getItem(ThreadLocalRandom.current().nextInt(validItems.size()));
    }
    public int getItemNum() { return itemNum; }
    public String getSpritePath() { return spritePath; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
