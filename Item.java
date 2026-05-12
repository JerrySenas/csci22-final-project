/**
This enum contains all items in the game. This enum only contains the items' information, functionality is in Game.
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

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


public enum Item {
    EMPTY (0, "", "", true, "", "", ""),
    CIGARETTE (1, "assets/ciggy.png", "assets/sfx/ciggy.wav", true, "Healthy Cigarette Pack", "Heals 1 HP.", "Recommended by all 10 doctors!"),
    BEER (2, "assets/beer.png", "assets/sfx/beer.wav", true, "Saint Michael's Legally Distinct Beer", "Ejects the current shell.", "Any similarity to products produced by a certain Philippine conglomerate is probably coincidental."),
    HANDCUFFS (3, "assets/handcuffs.png", "assets/sfx/handcuffs.wav", true, "Overused Handcuffs", "Skip the opponent's next turn.", "\"Business is booming!\" — Officer Abbacchio"),
    GLASS (4, "assets/glass.png", "assets/sfx/glass.wav", true, "Seldomly-Used Magnifying Glass", "Reveals the current shell in the chamber.", "\"Business isn't doing too well these days...\" — Detective Vaughn"),
    REVERSE (5, "assets/reverse.png", "assets/sfx/reverse.wav", true, "Seija's Calling Card", "Swaps the polarity of the current shell in the chamber.", "What is this? Some sort of Reverse Ideology?"),
    CASING (6, "assets/casing.png", "assets/sfx/casing.wav", true, "The Rest of the Shell", "If the current shell is live, it deals 1 more damage.", "\"65% more bullet per bullet!\" — Cave Johnson"),
    MEDICINE (7, "assets/medicine.png", "", true, "Expired SSRIs", "May heal 2 HP, deal 1 to yourself, or heal 2 HP and give yourself immunity.", "Just as effective."),
    
    DEST_WHITE(90, "assets/white_egg.png", "assets/sfx/dest_white.wav", false, "Destruction in White", "Destroy all other items. If at least 2 were destroyed, heal 1 HP and turn this item into a Destruction in Black.", "Remember nothing but her voice."),
    DEST_BLACK(91, "assets/black_egg.png", "assets/sfx/dest_black.wav", false, "Destruction in Black", "Destroy all other items. If at least 2 were destroyed, deal 1 damage and turn this item into a Destruction in White.", "Forget all but her voice."),
    DISD_CLAWS(92, "assets/claws.png", "assets/sfx/claws.wav", false, "Claws of Ardent Disdain", "If the current bullet is live, it deals 1 more damage. Doesn't disappear when the gun is reloaded.", "\"I'll crush it to rubble, until even the ruins are just ashes and dust!\" — The Omen of Disdain"),
    ;

    private final int itemNum;
    private final String spritePath;
    private final String soundPath;
    private final boolean randomAccessible;
    private final String name;
    private final String description;
    private final String flavor;

    /**
     * Class constructor.
     * 
     * @param itemNum unique numerical identifier
     * @param path filepath for this item's image
     * @param randAccess whether this item can be selected by getRandomItem
     * @param name this item's name
     * @param desc this item's description
     * @param flav this item's flavor text
     */
    private Item(int itemNum, String path, String sound, boolean randAccess, String name, String desc, String flav) {
        this.itemNum = itemNum;
        spritePath = path;
        soundPath = sound;
        randomAccessible = randAccess;
        this.name = name;
        description = desc;
        flavor = flav;
    }

    /**
     * Retrieves the item given its unique numerical identifier.
     * 
     * @param itemNum the unique identifier
     * @return the matching Item
     */
    public static Item getItem(int itemNum) {
        for (Item item : Item.values()) {
            if (item.itemNum == itemNum) {
                return item;
            }
        }
        System.out.printf("Item with itemNumber: %d doesn't exist\n", itemNum);
        return null;
    }

    /**
     * Retrieves an item randomly. Items with a randomAccessible value of false will not be included.
     * @return a randomly selected Item
     */
    public static Item getRandomItem() {
        ArrayList<Item> validItems = new ArrayList<>();
        for (Item item : Item.values()) {
            if (item.randomAccessible) { validItems.add(item); }
        }
        return getItem(ThreadLocalRandom.current().nextInt(validItems.size()));
    }

    /**
     * Retrieves the unique numerical identifier of this Item
     * @return the unique identifier
     */
    public int getItemNum() { return itemNum; }

    /**
     * Returns the filepath of this item's sprite
     * @return filepath
     */
    public String getSpritePath() { return spritePath; }

    /**
     * Returns the filepath of this item's sound effect
     * @return filepath
     */
    public String getSoundPath() { return soundPath; }

    /**
     * Returns this Item's name
     * @return name
     */
    public String getName() { return name; }

    /**
     * Returns this Item's description
     * @return description
     */
    public String getDescription() { return description; }

    /**
     * Returns this Item's flavor text
     * @return flavor text
     */
    public String getFlavor() { return flavor; }
}
