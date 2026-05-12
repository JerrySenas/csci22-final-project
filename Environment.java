/**
This enum contains all environments in the game. Each environment has a set of triggers that have different effects based on which environment it is.
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
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public enum Environment {
    NORMAL(0, "Normal", "No special rules", "No funny business."),
    HOARDER(1, "Hoarder", "Items don't disappear when the gun is reloaded.", "Spend wisely~"),
    CHAOS(2, "Whims of Chaos", "Every action randomizes the magazine and everyone's items", "Only by casting aside reason can one truly gamble."),
    RUSSIAN(3, "Russian Roulette", "1 live, 5 blanks. No items.", "Don't feel sad about losing, losing in this game is a once-in-a-lifetime opportunity!"),
    WHISPER(4, "Whisper", "Every fourth shot is enhanced to either deal double damage or give immunity.", "\"The trigger on a loaded weapon... it whispers for us to act.\" — Khada Jhin"),

    OMEN_ONE(91, "Omen - One", "Start with only one item each time the gun is reloaded.", "Scarcity will only strengthen you more."),
    OMEN_TWO(92, "Omen - Idolatry", "Offer your items to your idol. Items don't disappear when the gun is reloaded.", "Destroy all other thoughts. Leave nothing but your idol."),
    OMEN_THREE(93, "Omen - Silence", "Every third item used deals 3 damage to yourself.", "Language breeds both good and evil; only silence creates equality."),
    OMEN_FOUR(94, "Omen - Repose", "Shooting a blank to your opponent gives you 4 random items.", "Sink into the deepest slumber and face your mind."),
    OMEN_FIVE(95, "Omen - Disdain", "Taking damage gives you a 'Claws of Ardent Disdain'.", "Crush your opponent for even thinking of facing you."),
    OMEN_SIX(96, "Omen - Unkilling", "Live shells reduce max HP by 1 instead of dealing damage.", "You will wither, rot, and waste away... but you will never die here."),
    OMEN_SEVEN(97, "Omen - Lust", "Shooting yourself enhances the next shot. Shooting yourself with a live bullet won't pass your turn.", "Short of breath and shorter of logic, all reason disappears. This is our ego in its purest."),
    OMEN_EIGHT(98, "Omen - Usurpation", "Shooting anyone randomly steals one of your opponent's items.", "Satisfaction is the enemy. Take what is rightfully yours."),
    OMEN_NINE(99, "Omen - Truth", "The current shell in the chamber is always revealed.", "Deceit — that is, everything but the truth — must be abandoned."),
    OMEN_TEN(100, "Omen - Craving", "Start with a full inventory each time the gun is reloaded.", "Abundance will only leave you craving for more."),
    ;

    private final int envNum;
    private final String name;
    private final String description;
    private final String flavor;
    private int counter;

    /**
    Class constructor.
    @param num Unique identifier
    @param name Name of the environment
    @param desc Description of the environment
    @param flav Flavor text of the environment
    */
    private Environment(int num, String name, String desc, String flav) {
        envNum = num;
        this.name = name;
        description = desc;
        flavor = flav;
        counter = 0;
    }

    /**
     * Retrieves the environment by its numerical identifier.
     * 
     * @param num The environment's unique identifier.
     */
    public static Environment getEnvironment(int num) {
        for (Environment env : Environment.values()) {
            if (env.envNum == num) {
                return env;
            }
        }
        System.out.printf("Environment with envNumber: %d doesn't exist\n", num);
        return null;
    }

    /**
     * Generates shells for the magazine at the start of each set.
     * 
     * By default, it generates anywhere between 2 to 8 shells, with at least one live and one blank.
     * 
     * Some environments overwrite this default functionality.
     * @param game Reference to the current game instance.
     */
    public void shellSetup(Game game) {
        int numShells = ThreadLocalRandom.current().nextInt(2, 9);
        ArrayList<Boolean> shells = new ArrayList<>();

        switch (this) {
            case RUSSIAN:
                shells.add(true);
                for (int i = 0; i < 5; i++) {
                    shells.add(false);
                }
                Collections.shuffle(shells);
                game.setShells(shells);
                return;
            case WHISPER:
                numShells = Math.random() < 0.5 ? 4 : 8;
                break;
            default:
                break;
        }

        shells.add(true);
        shells.add(false);
        for (int i = 0; i < numShells - 2; i++) {
            shells.add(Math.random() < 0.5);
        }

        Collections.shuffle(shells);
        game.setShells(shells);
        if (this == OMEN_NINE) {
            game.revealShell();
        }
    }

    /**
     * Generates the items for both players at the start of each set.
     * 
     * By default, it generates anywhere between 0 to 8 items and sets both players' inventories to those items.
     * 
     * Some environments overwrite this default functionality.
     * @param game Reference to the current game instance.
     */
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
                // Players shouldn't be able to destroy their eggs so this only adds on round start... hopefully
                if (game.getSelfPlayer(1).getNumItems() == 0) {
                    game.getSelfPlayer(1).addItem(Item.DEST_WHITE);
                    game.getSelfPlayer(2).addItem(Item.DEST_WHITE);
                    i++;
                }
                numItems = ThreadLocalRandom.current().nextInt(2, 9);
                break;
            case OMEN_TEN:
                numItems = 8;
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

    /**
     * Executes functionality that trigger whenever the number of shells in the magazine changes.
     * 
     * @param game Reference to the current game instance.
     */
    public void onShellChange(Game game) {
        switch (this) {
            case WHISPER:
                if (game.getNumShells() > 0 && game.getNumShells() % 4 == 1) {
                    game.enhanceShell();
                }
                break;
            case CHAOS:
                itemSetup(game);
                shellSetup(game);
                break;
            case OMEN_NINE:
                game.revealShell();
                break;

            default:
                break;
        }
    }

    /**
     * Executes functionality that trigger whenever an item is used.
     * 
     * @param game Reference to the current game instance.
     * @param item The item used.
     * @param player The player who used the item.
     */
    public void onItemUse(Game game, Item item, Player player) {
        switch (this) {
            case CHAOS:
                itemSetup(game);
                shellSetup(game);
                break;
            case OMEN_THREE:
                counter++;
                if (counter % 3 == 0) {
                    game.dealDamage(3, player);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Executes functionality that trigger whenever a player shoots anyone.
     * 
     * @param game Reference to the current game instance.
     * @param shell Whether the shell was live or a blank.
     * @param shooter The shooter.
     * @param target The target.
     */
    public void onShoot(Game game, boolean shell, Player shooter, Player target) {
        switch (this) {
            case OMEN_FOUR:
                if (!shell && shooter != target) {
                    for (int i = 0; i < 4; i++) {
                        shooter.addItem(Item.getRandomItem());
                    }
                }
                break;
            case OMEN_SEVEN:
                if (shooter == target) {
                    game.enhanceShell();
                }
                break;
            case OMEN_EIGHT:
                Player enemy = game.getOpposingPlayer(shooter);
                ArrayList<Integer> validIdx = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    if (enemy.getItem(i) != Item.EMPTY) {
                        validIdx.add(i);
                    }
                }
                if (validIdx.isEmpty()) {
                    return;
                }
                int randomIdx = ThreadLocalRandom.current().nextInt(0, validIdx.size());
                shooter.addItem(enemy.getItem(randomIdx));
                enemy.removeItem(randomIdx);
                break;
            default:
                break;
        }
    }

    /**
     * Executes functionality that trigger whenever a player takes damage.
     * 
     * @param game Reference to the current game instance.
     * @param target The player who took damage.
     */
    public void onDamageTaken(Game game, Player target) {
        switch (this) {
            case OMEN_FIVE:
                target.addItem(Item.DISD_CLAWS);
                break;
            default:
                break;
        }
    }

    /**
     * Returns this environment's environment number.
     */
    public int getEnvNum() { return envNum; }

    /**
     * Returns this environment's name.
     */
    public String getName() { return name; }

    /**
     * Returns this environment's description.
     */
    public String getDescription() { return description; }

    /**
     * Returns this environment's flavor text.
     */
    public String getFlavor() { return flavor; }
}
