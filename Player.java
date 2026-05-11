/**
This class contains the player's attributes and statuses. Their inventory as well as related helper methods are also defined here.
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

public class Player {
    public static final int MAX_HP = 4;
    private int hp;
    private int maxRestorableHp;
    private final Item[] items;

    private boolean isImmune;
    private boolean isSkippingNextTurn;

    /**
     * Class constructor.
     * @param pNum
     */
    public Player(int pNum) {
        maxRestorableHp = MAX_HP;
        hp = maxRestorableHp;

        items = new Item[8];
        for (int i = 0; i < 8; i++) {
            items[i] = Item.EMPTY;
        }
        isImmune = false;
        isSkippingNextTurn = false;
    }

    /**
     * Reduces this Player's HP. HP will not lower below 0.
     * @param dmg The damage to be taken
     */
    public void takeDamage(int dmg) {
        hp = Math.max(0, hp - dmg);
    }

    /**
     * Increases this Player's HP. HP will not exceed the maximum restorable HP.
     * @param amount The amount to be healed
     */
    public void heal(int amount) {
        hp = Math.min(maxRestorableHp, hp + amount);
    }

    /**
     * Retrieves the item at the given item slot.
     * @param itemSlot the item slot to retrieve from
     * @return the item stored in the item slot
     */
    public Item getItem(int itemSlot) {
        if (itemSlot < 0 || itemSlot > 7) {
            System.out.printf("Item slot %d is out of bounds.\n", itemSlot);
            return null;
        }
        return items[itemSlot];
    }

    /**
     * Adds an item to an empty slot in this Player's inventory. An item won't be added if there are no empty slots.
     * @param item the item to be added
     */
    public void addItem(Item item) {
        for (int i = 0; i < 8; i++) {
            if (items[i] == Item.EMPTY) {
                items[i] = item;
                break;
            }
        }
    }

    /**
     * Removes the item at the given slot by setting it to EMPTY.
     *
     * @param itemSlot the index of the item slot (0–7)
     */
    public void removeItem(int itemSlot) {
        if (items[itemSlot] == Item.EMPTY) {
            System.out.println("Item slot already empty.");
        }
        items[itemSlot] = Item.EMPTY;
    }

    /**
     * Gets the current HP of the player.
     *
     * @return current health value
     */
    public int getHP() { return hp; }

    /**
     * Gets the maximum restorable HP of the player.
     *
     * @return maximum healable health value
     */
    public int getMaxRestorableHP() { return maxRestorableHp; }

    /**
     * Checks whether the player is immune.
     *
     * @return true if immune, otherwise false
     */
    public boolean isImmune() { return isImmune; }

    /**
     * Checks whether the player will skip their next turn.
     *
     * @return true if skipping, otherwise false
     */
    public boolean isSkippingNextTurn() { return isSkippingNextTurn; }

    /**
     * Checks whether the player has a specific item.
     *
     * @param item the item to check for
     * @return true if the item is present, otherwise false
     */
    public boolean hasItem(Item item) {
        boolean hasitem = false;
        for (Item playerItem : items) {
            if (playerItem == item) { hasitem = true; }
            break;
        }
        return hasitem;
    }

    /**
     * Returns all of this Player's items.
     *
     * @return array of items (size 8)
     */
    public Item[] getItems() { return items; }

    /**
     * Returns the number of non-EMPTY items.
     *
     * @return number of items
     */
    public int getNumItems() {
        int numItems = 0;
        for (Item item : items) {
            if (item != Item.EMPTY) { numItems++; }
        }
        return numItems;
    }

    /**
     * Sets all of this Player's items to EMPTY.
     */
    public void clearItems() {
        for (int i = 0; i < 8; i++) {
            if (items[i] != Item.DISD_CLAWS) {
                items[i] = Item.EMPTY;
            }
        }
    }

    /**
     * Sets whether the player will skip their next turn.
     *
     * @param skip true if skipping false otherwise
     */
    public void setIsSkippingNextTurn(boolean skip) { isSkippingNextTurn = skip; }

    /**
     * Sets whether the player has damage immunity.
     *
     * @param immune true if player is immune
     */
    public void setIsImmune(boolean immune) { isImmune = immune; }

    /**
     * Sets this Player's maximum restorable HP. Lowers HP if it is greater than the max HP.
     *
     * @param maxHP the new maximum restorable HP
     */
    public void setMaxRestorableHP(int maxHP) {
        maxRestorableHp = maxHP;
        if (hp > maxRestorableHp) {
            hp = maxRestorableHp;
        }
    }
}
