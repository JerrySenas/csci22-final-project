
import java.util.ArrayList;
import java.util.Collections;

public enum Environment {
    NORMAL("Normal", "No funny business."),
    ITEM_CARRYOVER("Hoarder", "Items don't disappear when bullets run out."),
    WHISPER("Whisper", "Every fourth shot is enhanced."),
    RUSSIAN("Russian Roulette", "The classic.");

    private String name;
    private String description;

    private Environment(String name, String desc) {
        this.name = name;
        description = desc;
    }

    public ArrayList<Boolean> bulletSetup(GameServer game) {
        int numBullets = (int) ((Math.random() * 7) + 2);
        ArrayList<Boolean> bullets = new ArrayList<>();

        switch (this) {
            case RUSSIAN:
                numBullets = 6;
                bullets.add(true);
                for (int i = 0; i < 5; i++) {
                    bullets.add(false);
                }
                break;
            default:
                bullets.add(true);
                bullets.add(false);
                for (int i = 0; i < numBullets - 2; i++) {
                    bullets.add(Math.random() < 0.5);
                }

            }
        Collections.shuffle(bullets);
        game.setNumBullets(numBullets);
        return bullets;
    }

    public void itemSetup(GameServer game) {
        if (this != ITEM_CARRYOVER) {
            game.getSelfPlayer(1).clearItems();
            game.getSelfPlayer(2).clearItems();
        }
        switch (this) {
            case RUSSIAN:
                break;
            default:
                int numItems = (int) (Math.random() * 9);
                for (int i = 0; i < numItems; i++) {
                    Item item = Item.getItem((int) (Math.random() * 5));
                    game.getSelfPlayer(1).addItem(item);
                    game.getSelfPlayer(2).addItem(item);
                }
        }
    }

    public static Environment getEnvironment(int envIdx) { return Environment.values()[envIdx]; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
