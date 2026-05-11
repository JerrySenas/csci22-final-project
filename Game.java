/**
This class contains the logic for the game server. This includes game logic, item functionality, and networking.
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

import java.awt.event.ActionListener;
import java.io.*;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Game {
    private Player p1;
    private Player p2;
    private final DataOutputStream p1Out;
    private final DataOutputStream p2Out;
    private int p1Score;
    private int p2Score;
    private int currentTurn;

    private ArrayList<Environment> envs;
    private Environment currentEnvironment;

    private ArrayList<Boolean> shells;
    private int numShells;
    private int dmgTaken;
    private Boolean enhanced;
    private Boolean dmgModified;

    private boolean gameEnded;
    private final ActionListener endgameCallback;

    /**
     * Class constructor.
     * @param p1O Player 1's DataOutputStream
     * @param p2O Player 2's DataOutputStream
     * @param callback callback to be run at the end of the game
     */
    public Game(DataOutputStream p1O, DataOutputStream p2O, ActionListener callback) {
        p1Out = p1O;
        p2Out = p2O;
        gameEnded = false;
        endgameCallback = callback;
    }

    /**
     * Starts a thread that reads the commands of each of the Players.
     * @param p1In Player 1's DataInputStream
     * @param p2In Player 2's DataInputStream
     */
    public void startThreads(DataInputStream p1In, DataInputStream p2In) {
        new Thread(new ReadFromClient(p1In, 1)).start();
        new Thread(new ReadFromClient(p2In, 2)).start();
    }

    /**
     * Sets up a new game and starts a round.
     */
    public void startGame() {
        p1 = new Player(1);
        p2 = new Player(2);
        p1Score = 0;
        p2Score = 0;
        envs = new ArrayList<>(Arrays.asList(Environment.values()));
        currentTurn = Math.random() < 0.5 ? 1 : 2;
        startRound();
    }

    /**
     * Sets up a new round. If either Player's HP is 0, a point is added to the other Player's score.
     */
    public void startRound() {
        if (p1.getHP() == 0) {
            p2Score++;
        } else if (p2.getHP() == 0) {
            p1Score++;
        }
        announce(String.format("NEW_ROUND;%d;%d", p1Score, p2Score), String.format("NEW_ROUND;%d;%d", p2Score, p1Score));

        if (p1Score >= 3) {
            announce("WIN", "LOSE");
            return;
        } else if (p2Score >= 3) {
            announce("LOSE", "WIN");
            return;
        }

        int envIdx = ThreadLocalRandom.current().nextInt(0, envs.size());
        currentEnvironment = envs.get(envIdx);
        envs.remove(envIdx);
        announce("ENV;" + currentEnvironment.getEnvNum());

        p1.setMaxRestorableHP(Player.MAX_HP);
        p2.setMaxRestorableHP(Player.MAX_HP);

        p1.heal(Player.MAX_HP);
        p2.heal(Player.MAX_HP);
        startSet();
    }

    /**
     * Sets up a new set. Triggers the currentEnvironment's shellSetup and itemSetup.
     */
    public void startSet() {
        currentEnvironment.shellSetup(this);
        currentEnvironment.itemSetup(this);

        dmgTaken = 1;
        enhanced = false;
        dmgModified = false;
        announce("PLUS_DMG;RESET");
        sendGameState();
    }

    /**
     * Handles a shoot command. Triggers the currentEnvironment's onShoot after onBulletChange (from decrementBullet).
     * @param playerNum playerNum of the shooter
     * @param data the data of the command
     */
    public void handleShoot(int playerNum, String[] data) {
        int currentDmg = dmgTaken;
        boolean currentShell = shells.get(numShells - 1);

        if (enhanced && currentShell) {
            currentDmg *= 2;
        }

        Player target;
        if (data[1].equals("self")) {
            target = getSelfPlayer(playerNum);
            if (currentShell) {
                dealDamage(currentDmg, target);
                if (currentEnvironment != Environment.OMEN_SEVEN) {
                    changeTurn();
                }
            } else if (enhanced) {
                target.setIsImmune(true);
            }
        } else {
            target = getOpposingPlayer(playerNum);
            if (currentShell) {
                dealDamage(currentDmg, target);
            } else if (enhanced) {
                target.setIsImmune(true);
            }
            changeTurn();
        }
        
        if (dmgModified) {
            dmgTaken = 1;
            dmgModified = false;
            announce("PLUS_DMG;RESET");
        }

        decrementShells();
        currentEnvironment.onShoot(this, currentShell, getSelfPlayer(playerNum), target);
        sendGameState();
        if (numShells <= 0) {
            startSet();
        }
    }

    /**
     * Handles the use item command. Triggers the currentEnvironment's onItemUse.
     * 
     * Each item's functionality is defined here.
     * @param playerNum playerNum of the item user
     * @param data the use item command
     */
    public void handleItem(int playerNum, String[] data) {
        Player player = getSelfPlayer(playerNum);
        int itemSlot = Integer.parseInt(data[1]);
        Item item = player.getItem(itemSlot);
        switch (item) {
            case CIGARETTE:
                if (player.getHP() < player.getMaxRestorableHP()) {
                    player.heal(1);
                    break;
                } else {
                    return;
                }

            case BEER:
                if (numShells <= 1) {
                    return;
                }
                decrementShells();
                if (numShells <= 0) {
                    startSet();
                }
                break;
            
            case HANDCUFFS:
                if (getOpposingPlayer(playerNum).isSkippingNextTurn()) {
                    return;
                }

                getOpposingPlayer(playerNum).setIsSkippingNextTurn(true);
                break;

            case GLASS:
                revealShell();
                break;
            
            case REVERSE:
                shells.set(numShells - 1, !shells.get(numShells - 1));
                break;

            case MEDICINE:
                int roll = ThreadLocalRandom.current().nextInt(1, 11);
                if (roll < 5) {
                    dealDamage(1, player);
                } else if (roll < 9) {
                    player.heal(2);
                } else {
                    player.heal(2);
                    player.setIsImmune(true);
                }
                break;

            case CASING:
            case DISD_CLAWS:
                dmgTaken++;
                dmgModified = true;
                announce("PLUS_DMG;ADD");
                break;

            case DEST_WHITE:
                if (player.getNumItems() > 2) {
                    player.heal(1);
                    player.clearItems();
                    player.addItem(Item.DEST_BLACK);
                    sendGameState();
                }
                return;

            case DEST_BLACK:
                if (player.getNumItems() > 2) {
                    dealDamage(1, getOpposingPlayer(playerNum));
                    player.clearItems();
                    player.addItem(Item.DEST_WHITE);
                    sendGameState();
                }
                return;

            default:
                return;
        }
        player.removeItem(itemSlot);
        currentEnvironment.onItemUse(this, item, player);
        sendGameState();
        if (numShells <= 0) {
            startSet();
        }
    }

    /**
     * Enhances the current shell.
     */
    public void enhanceShell() {
        enhanced = true;
        announce("ENHANCE;");
    }

    /**
     * Reveals the current shell.
     */
    public void revealShell() {
        if (numShells > 0) {
            announce("REVEAL;" + (shells.get(numShells - 1) ? 1 : 0));
        }
    }

    /**
     * Causes the target Player to take damage. Triggers the currentEnvironment's onDamageTaken.
     * @param damage amount of damage taken
     * @param target Player to take damage
     */
    public void dealDamage(int damage, Player target) {
        if (currentEnvironment == Environment.OMEN_SIX) {
            target.setMaxRestorableHP(target.getMaxRestorableHP() - 1);
            sendGameState();
            if (p1.getHP() == 0 || p2.getHP() == 0) {
                startRound();
            }
            return;
        }
        if (target.isImmune()) {
            target.setIsImmune(false);
            sendGameState();
            return;
        }
        target.takeDamage(damage);
        currentEnvironment.onDamageTaken(this, target);
        sendGameState();
        if (p1.getHP() == 0 || p2.getHP() == 0) {
            startRound();
        }
    }

    /**
     * Gets the Player associated to the playerNum
     * @param playerNum the player number
     * @return the Player
     */
    public Player getSelfPlayer(int playerNum) { 
        if (playerNum == 1) {
            return p1;
        } else {
            return p2;
        }
    }

    /**
     * Gets the Opponent of the Player associated to the playerNum
     * @param playerNum the player number
     * @return the Opponent
     */
    public Player getOpposingPlayer(int playerNum) {
        if (playerNum == 1) {
            return p2;
        } else {
            return p1;
        }
    }

    /**
     * Gets the Opponent of the Player
     * @param self the Player
     * @return their Opponent
     */
    public Player getOpposingPlayer(Player self) {
        if (p1 == self) {
            return p2;
        } else {
            return p1;
        }
    }

    /**
     * Returns the number of shells are currently in the magazine.
     * @return number of shells
     */
    public int getNumShells() { return numShells; }

    /**
     * Returns the number of live shells are currently in the magazine.
     * @return number of lives
     */
    public int getLives() {
        int lives = 0;
        for (boolean shell : shells) {
            if (shell) {
                lives += 1;
            }
        }

        return lives;
    }

    /**
     * Returns the number of blanks are currently in the magazine.
     * @return number of blanks
     */
    public int getBlanks() {
        int blanks = 0;
        for (boolean shell : shells) {
            if (!shell) {
                blanks += 1;
            }
        }

        return blanks;
    }

    /**
     * Sets the magazine
     * @param shots ArrayList of booleans representing the shells
     */
    public void setShells(ArrayList<Boolean> shots) {
        shells = shots;
        numShells = shells.size();
    }

    /**
     * Chambers the next shell. Triggers the currentEnvironment's onShellChange
     */
    public void decrementShells() {
        shells.remove(numShells - 1);
        numShells--;
        if (enhanced) {
            enhanced = false;
        }
        announce("SHOOT");
        currentEnvironment.onShellChange(this);
    }

    /**
     * Changes the turn. If the Player is skipping their turn, changes it again.
     */
    public void changeTurn() {
        if (currentTurn == 1) {
            currentTurn = 2;
        } else {
            currentTurn = 1;
        }

        int isP1Turn = currentTurn == 1 ? 1 : 0;
        int isP2Turn = currentTurn == 2 ? 1 : 0;


        announce("TURN;" + isP1Turn, "TURN;" + isP2Turn);
        sendGameState();

        if (getSelfPlayer(currentTurn).isSkippingNextTurn()) {
            getSelfPlayer(currentTurn).setIsSkippingNextTurn(false);
            changeTurn();
        }
    }

    /**
     * Sends a message to both game clients
     * @param msg the message
     */
    public void announce(String msg) {
        try {
            p1Out.writeUTF(msg);
            p2Out.writeUTF(msg);
        } catch (IOException e) {
            System.out.println("Announcement failed for at least 1 player. One of the players may have disconnected.");
        }
    }
    
    /**
     * Sends a message to each of the game clients
     * @param msg1 message for Player 1
     * @param msg2 message for Player 2
     */
    public void announce(String msg1, String msg2) {
        try {
            p1Out.writeUTF(msg1);
            p2Out.writeUTF(msg2);
        } catch (IOException e) {
            System.out.println("Announcement failed for at least 1 player. One of the players may have disconnected.");
        }
    }

    /**
     * Sends a serialized game state to each Player.
     * 
     * The serialized game state is in the form HP;enemyHP;isSkipping;isEnemySkipping;isImmune;isEnemyImmune;Items(8);EnemyItems(8);Lives;Blanks
     */
    public void sendGameState() {
        String serializedStateForP1 = "STATE;";
        String serializedStateForP2 = "STATE;";

        serializedStateForP1 += String.format(
            "%d;%d;%d;%d;%d;%d;",
            p1.getHP(),
            p2.getHP(),
            p1.isSkippingNextTurn() ? 1 : 0,
            p2.isSkippingNextTurn() ? 1 : 0,
            p1.isImmune() ? 1 : 0,
            p2.isImmune() ? 1 : 0
        );
        serializedStateForP2 += String.format(
            "%d;%d;%d;%d;%d;%d;",
            p2.getHP(),
            p1.getHP(),
            p2.isSkippingNextTurn() ? 1 : 0,
            p1.isSkippingNextTurn() ? 1 : 0,
            p2.isImmune() ? 1 : 0,
            p1.isImmune() ? 1 : 0
        );

        String temp1 = "";
        String temp2 = "";

        for (Item item : p1.getItems()) {
            serializedStateForP1 += item.getItemNum() + ";";
            temp1 += item.getItemNum() + ";";
        }
        for (Item item : p2.getItems()) {
            serializedStateForP2 += item.getItemNum() + ";";
            temp2 += item.getItemNum() + ";";
        }
        serializedStateForP1 += temp2;
        serializedStateForP2 += temp1;

        serializedStateForP1 += String.format("%d;%d", getLives(), getBlanks());
        serializedStateForP2 += String.format("%d;%d", getLives(), getBlanks());

        announce(serializedStateForP1, serializedStateForP2);
    }

    /**
    This class constantly reads from the game client for commands and executes them. Handles 3 commands, relaying cursor movement, handling shoot actions, and handling item use.
    */
    private class ReadFromClient implements Runnable {
        private final DataInputStream dataIn;
        private final int playerNum;

        /**
         * Class constructor.
         * @param in DataInputStream from the game client
         * @param pNum assigned player number
         */
        public ReadFromClient(DataInputStream in, int pNum) {
            dataIn = in;
            playerNum = pNum;
        }

        /**
         * Handles the player's commands continuously while gameEnded is false.
         */
        @Override
        public void run() {
            try {
                while (!gameEnded) { 
                    String req = dataIn.readUTF();
                    String[] parts = req.split(";");
                    switch (parts[0]) {
                        case "CURSOR":
                            if (playerNum == 1) {
                                p2Out.writeUTF(req);
                            } else {
                                p1Out.writeUTF(req);
                            }
                            break;
                        case "SHOOT":
                            handleShoot(playerNum, parts);
                            break;
                        case "ITEM":
                            handleItem(playerNum, parts);
                            break;
                        default:
                            System.out.println(parts[0]);;
                    }
                }
            } catch (SocketException e) {
                endgameCallback.actionPerformed(null);
                announce("DISCONNECT;");
                gameEnded = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}