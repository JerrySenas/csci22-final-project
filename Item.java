
public enum Item {
    EMPTY (0, "", ""),
    CIGARETTE (1, "assets/ciggy.png", "Heals 1 HP."),
    BEER (2, "assets/beer.png", "Ejects the current shell."),
    HANDCUFFS (3, "assets/handcuffs.png", "Skip the opponent's next turn."),
    GLASS (4, "assets/glass.png", "Check the current round in the chamber.");

    private final int itemNum;
    private final String spritePath;
    private final String description;

    private Item(int itemNum, String path, String desc) {
        this.itemNum = itemNum;
        spritePath = path;
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
    public int getItemNum() { return itemNum; }
    public String getSpritePath() { return spritePath; }
    public String getDescription() { return description; }
}
