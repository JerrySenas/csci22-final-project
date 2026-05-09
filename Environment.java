
import java.util.ArrayList;
import java.util.Collections;

public enum Environment {
    NORMAL(0, "Normal", "No funny business."),
    ITEM_CARRYOVER(1, "Hoarder", "Items don't disappear when the rack is reset."),
    WHISPER(2, "Whisper", "Every fourth shot is enhanced to either deal double damage or give immunity."),
    RUSSIAN(3, "Russian Roulette", "Down for a good old game of Russian Roulette?"),
    CHAOS(4, "Whims of Chaos", "Only by casting aside reason can one truly gamble.");

    private final int envNum;
    private final String name;
    private final String description;

    private Environment(int num, String name, String desc) {
        envNum = num;
        this.name = name;
        description = desc;
    }

    public void bulletSetup(Game game) {
        int numBullets = (int) ((Math.random() * 7) + 2);
        ArrayList<Boolean> bullets = new ArrayList<>();

        switch (this) {
            case RUSSIAN:
                bullets.add(true);
                for (int i = 0; i < 5; i++) {
                    bullets.add(false);
                }
                Collections.shuffle(bullets);
                game.setBullets(bullets);
                return;
            case WHISPER:
                numBullets = Math.random() < 0.5 ? 4 : 8;
                break;
            default:
                break;
        }

        bullets.add(true);
        bullets.add(false);
        for (int i = 0; i < numBullets - 2; i++) {
            bullets.add(Math.random() < 0.5);
        }

        Collections.shuffle(bullets);
        game.setBullets(bullets);
    }

    public void itemSetup(Game game) {
        if (this != ITEM_CARRYOVER) {
            game.getSelfPlayer(1).clearItems();
            game.getSelfPlayer(2).clearItems();
        }
        int numItems;
        switch (this) {
            case RUSSIAN:
                numItems = 0;
                break;
            case WHISPER:
                numItems = Math.random() < 0.5 ? 4 : 8;
                break;
            default:
                numItems = (int) (Math.random() * 9);
                break;
        }
    
        for (int i = 0; i < numItems; i++) {
            Item item = Item.getRandomItem();
            game.getSelfPlayer(1).addItem(item);
            game.getSelfPlayer(2).addItem(item);
        }
    }

    public void onBulletChange(Game game) {
        switch (this) {
            case WHISPER:
                if (game.getNumBullets() > 0 && game.getNumBullets() % 4 == 1) {
                    game.enhanceBullet();
                }
                break;
            case CHAOS:
                itemSetup(game);
                bulletSetup(game);
                break;

            default:
                break;
        }
    }

    public void onItemUse(Game game, Item item) {
        switch (this) {
            case CHAOS:
                itemSetup(game);
                bulletSetup(game);
                break;
            default:
                break;
        }
    }

    public static Environment getEnvironment(int num) {
        for (Environment env : Environment.values()) {
            if (env.envNum == num) {
                return env;
            }
        }
        System.out.printf("Environment with envNumber: %d doesn't exist\n", num);
        return null;
    }
    public int getEnvNum() { return envNum; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
