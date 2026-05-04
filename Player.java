

public class Player {
    private int playerNum;

    private int hp;
    public static final int MAX_HP = 4;
    private Item[] items;

    private boolean isImmune;
    private boolean isSkippingNextTurn;
    
    public Player(int pNum) {
        playerNum = pNum;
        
        hp = MAX_HP;
        items = new Item[8];
        for (int i = 0; i < 8; i++) {
            items[i] = Item.EMPTY;
        }
        isImmune = false;
        isSkippingNextTurn = false;
    }

    public void takeDamage(int dmg) {
        if (isImmune) {
            isImmune = false;
        } else {
            hp -= dmg;
        }
    }

    public void heal(int amount) {
        hp = Math.min(MAX_HP, hp + amount);
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
    public boolean isImmune() { return isImmune; }
    public boolean isSkippingNextTurn() { return isSkippingNextTurn; }
    public Item[] getItems() { return items; }

    public void clearItems() {
        for (int i = 0; i < 8; i++) {
            items[i] = Item.EMPTY;
        }
    }
    public void setIsSkippingNextTurn(boolean skip) { isSkippingNextTurn = skip; }
}
