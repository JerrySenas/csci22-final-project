
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public enum Environment {
    NORMAL(0, "Normal", "No funny business."),
    HOARDER(1, "Hoarder", "Items don't disappear when the rack is reset."),
    CHAOS(2, "Whims of Chaos", "Only by casting aside reason can one truly gamble."),
    RUSSIAN(3, "Russian Roulette", "Down for a good old game of Russian Roulette?"),
    WHISPER(4, "Whisper", "Every fourth shot is enhanced to either deal double damage or give immunity."),
    OMEN_ONE(11, "Omen: One", "One."),
    OMEN_TWO(12, "Omen: Idolatry", "Offer your items to your idol. Items don't disappear when the rack is reset."),
    OMEN_THREE(13, "Omen: Silence", "Every third item use deals 3 damage to yourself."),
    OMEN_FOUR(14, "Omen: Repose", "Shooting a blank to your opponent gives you 4 random items."),
    OMEN_FIVE(15, "Omen: Disdain", "Taking damage gives you a 'Claws of Ardent Disdain'."),
    ;

    private final int envNum;
    private final String name;
    private final String description;
    private int counter;

    private Environment(int num, String name, String desc) {
        envNum = num;
        this.name = name;
        description = desc;
        counter = 0;
    }

    public void bulletSetup(Game game) {
        int numBullets = ThreadLocalRandom.current().nextInt(2, 9);
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
        if (this != HOARDER && this != OMEN_TWO) {
            game.getSelfPlayer(1).clearItems();
            game.getSelfPlayer(2).clearItems();
        }
        int numItems = 0;
        int i = 0;
        switch (this) {
            case RUSSIAN:
                break;
            case WHISPER:
                numItems = Math.random() < 0.5 ? 4 : 8;
                break;
            case OMEN_ONE:
                numItems = 1;
                break;
            case OMEN_TWO:
                if (numItems == 0) {
                    game.getSelfPlayer(1).addItem(Item.DEST_WHITE);
                    game.getSelfPlayer(2).addItem(Item.DEST_WHITE);
                    i++;
                }
                numItems = ThreadLocalRandom.current().nextInt(2, 9);
                break;
            default:
                numItems = ThreadLocalRandom.current().nextInt(0, 9);
                break;
        }
    
        for (int j = i; j < numItems; j++) {
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

    public void onItemUse(Game game, Item item, int playerNum) {
        switch (this) {
            case CHAOS:
                itemSetup(game);
                bulletSetup(game);
                break;
            case OMEN_THREE:
                counter++;
                if (counter % 3 == 0) {
                    game.getSelfPlayer(playerNum).takeDamage(3);
                }
                break;
            default:
                break;
        }
    }

    public void onShoot(Game game, boolean bullet, Player shooter, Player target) {
        switch (this) {
            case OMEN_FOUR:
                if (!bullet && shooter != target) {
                    for (int i = 0; i < 4; i++) {
                        shooter.addItem(Item.getRandomItem());
                    }
                }
                break;
            default:
                break;
        }
    }

    public void onDamageTaken(Game game, Player target) {
        switch (this) {
            case OMEN_FIVE:
                target.addItem(Item.DISD_CLAWS);
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
