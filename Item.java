
public enum Item {
    EMPTY (0, "", "", ""),
    CIGARETTE (1, "assets/ciggy.png", "Cigarette Pack", "Heals 1 HP."),
    BEER (2, "assets/beer.png", "Beer", "Ejects the current shell."),
    HANDCUFFS (3, "assets/handcuffs.png", "Handcuffs", "Skip the opponent's next turn."),
    GLASS (4, "assets/glass.png", "Magnifying Glass", "Check the current round in the chamber."),
    REVERSE (5, "assets/reverse.png", "Seija's Calling Card", "Swaps the polarity of the current round in the chamber."),
    CASING (6, "assets/casing.png", "The Rest of the Bullet", "If the current bullet is live, it deals 1 more damage.");

    private final int itemNum;
    private final String spritePath;
    private final String name;
    private final String description;

    private Item(int itemNum, String path, String name, String desc) {
        this.itemNum = itemNum;
        spritePath = path;
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
        return getItem((int) (Math.random() * (values().length - 1)) + 1 );
    }
    public int getItemNum() { return itemNum; }
    public String getSpritePath() { return spritePath; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
