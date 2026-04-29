

public class Player {
    private int playerNum;

    private int hp;
    private static final int MAX_HP = 4;
    private Item[] items;

    private boolean isImmune;
    
    public Player(int pNum) {
        playerNum = pNum;
        
        hp = MAX_HP;
        items = new Item[8];
        for (int i = 0; i < 8; i++) {
            items[i] = Item.EMPTY;
        }
        isImmune = false;
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

    public void addItem(Item item) {
        for (int i = 0; i < 8; i++) {
            if (items[i] == Item.EMPTY) {
                items[i] = item;
                break;
            }
        }
    }

    public void removeItem(Item item) {
        for (int i = 7; i >= 0; i--) {
            if (items[i] == item) {
                items[i] = Item.EMPTY;
                break;
            }
        }
    }

    public int getHP() { return hp; }
    public boolean isImmune() { return isImmune; }
    public Item[] getItems() { return items; }
}
