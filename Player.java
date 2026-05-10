

public class Player {
    public static final int MAX_HP = 4;
    private int hp;
    private int maxRestorableHp;
    private Item[] items;

    private boolean isImmune;
    private boolean isSkippingNextTurn;
    
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

    public void takeDamage(int dmg) {
        hp = Math.max(0, hp - dmg);
    }

    public void heal(int amount) {
        hp = Math.min(maxRestorableHp, hp + amount);
    }

    public Item getItem(int itemSlot) {
        if (itemSlot < 0 || itemSlot > 7) {
            System.out.printf("Item slot %d is out of bounds.\n", itemSlot);
            return null;
        }
        return items[itemSlot];
    }

    public void addItem(Item item) {
        for (int i = 0; i < 8; i++) {
            if (items[i] == Item.EMPTY) {
                items[i] = item;
                break;
            }
        }
    }

    public void removeItem(int itemSlot) {
        if (items[itemSlot] == Item.EMPTY) {
            System.out.println("Item slot already empty.");
        }
        items[itemSlot] = Item.EMPTY;
    }

    public int getHP() { return hp; }
    public int getMaxRestorableHP() { return maxRestorableHp; }
    public boolean isImmune() { return isImmune; }
    public boolean isSkippingNextTurn() { return isSkippingNextTurn; }

    public boolean hasItem(Item item) {
        boolean hasitem = false;
        for (Item playerItem : items) {
            if (playerItem == item) { hasitem = true; }
            break;
        }
        return hasitem;
    }
    public Item[] getItems() { return items; }
    public int getNumItems() {
        int numItems = 0;
        for (Item item : items) {
            if (item != Item.EMPTY) { numItems++; }
        }
        return numItems;
    }

    public void clearItems() {
        for (int i = 0; i < 8; i++) {
            if (items[i] != Item.DISD_CLAWS) {
                items[i] = Item.EMPTY;
            }
        }
    }
    public void setIsSkippingNextTurn(boolean skip) { isSkippingNextTurn = skip; }
    public void setIsImmune(boolean immune) { isImmune = true; }
    public void setMaxRestorableHP(int maxHP) {
        maxRestorableHp = maxHP;
        if (hp > maxRestorableHp) {
            hp = maxRestorableHp;
        }
    }
}
