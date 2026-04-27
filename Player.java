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
            items[i] = new Item(0);
        }
        isImmune = false;
    }

    public void takeDamage(int dmg) {
        if (isImmune) {
            isImmune = false;
        } else {
            hp -= dmg;
        }
        System.out.println("ow");
        System.out.println(hp);
    }

    public int getHP() { return hp; }
    public boolean isImmune() { return isImmune; }
    public Item[] getItems() { return items; }
}
