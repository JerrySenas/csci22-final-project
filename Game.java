import java.awt.event.ActionListener;
import java.io.*;
import java.net.SocketException;
import java.util.*;

public class Game {
    private Player p1;
    private Player p2;
    private DataOutputStream p1Out;
    private DataOutputStream p2Out;
    private int p1Score;
    private int p2Score;
    private int currentTurn;

    private ArrayList<Environment> envs;
    private Environment currentEnvironment;

    private ArrayList<Boolean> bullets;
    private int numBullets;
    private int dmgTaken;
    private Boolean enhanced;
    private Boolean dmgModified;

    private boolean gameEnded;
    private ActionListener endgameCallback;

    public Game(DataOutputStream p1O, DataOutputStream p2O, ActionListener callback) {
        p1Out = p1O;
        p2Out = p2O;
        gameEnded = false;
        endgameCallback = callback;
    }

    public void startThreads(DataInputStream p1In, DataInputStream p2In) {
        new Thread(new ReadFromClient(p1In, 1)).start();
        new Thread(new ReadFromClient(p2In, 2)).start();
    }

    public void startGame() {
        p1 = new Player(1);
        p2 = new Player(2);
        p1Score = 0;
        p2Score = 0;
        envs = new ArrayList<>(Arrays.asList(Environment.values()));
        startRound();
    }

    public void startRound() {
        if (p1.getHP() == 0) {
            p2Score++;
        } else if (p2.getHP() == 0) {
            p1Score++;
        }

        if (p1Score >= 3) {
            announce("WIN", "LOSE");
        } else if (p2Score >= 3) {
            announce("LOSE", "WIN");
        }

        int envIdx = (int) (Math.random() * envs.size());
        currentEnvironment = envs.get(envIdx);
        envs.remove(envIdx);
        announce("ENV;" + currentEnvironment.getEnvNum());

        p1.heal(Player.MAX_HP);
        p2.heal(Player.MAX_HP);
        announce(String.format("NEW_ROUND;%d;%d", p1Score, p2Score), String.format("NEW_ROUND;%d;%d", p2Score, p1Score));
        currentTurn = Math.random() < 0.5 ? 1 : 2;
        startSet();
    }

    public void startSet() {
        currentEnvironment.bulletSetup(this);
        currentEnvironment.itemSetup(this);

        dmgTaken = 1;
        enhanced = false;
        dmgModified = false;
        sendGameState();
        changeTurn();
    }

    public void handleShoot(int playerNum, String[] data) {
        int currentDmg = dmgTaken;
        boolean currentBullet = bullets.get(numBullets - 1);

        if (enhanced && currentBullet) {
            currentDmg *= 2;
        }

        if (data[1].equals("self")) {
            if (currentBullet) {
                getSelfPlayer(playerNum).takeDamage(currentDmg);
                changeTurn();
            } else if (enhanced) {
                getSelfPlayer(playerNum).setIsImmune(true);
            }
        } else {
            if (bullets.get(numBullets - 1)) {
                getOpposingPlayer(playerNum).takeDamage(currentDmg);
            } else if (enhanced) {
                getOpposingPlayer(playerNum).setIsImmune(true);
            }
            changeTurn();
        }
        
        if (enhanced) {
            enhanced = false;
        }

        if (dmgModified) {
            dmgTaken = 1;
            dmgModified = false;
            announce("PLUS_DMG;RESET");
        }

        decrementBullets();

        sendGameState();
        if (p1.getHP() == 0 || p2.getHP() == 0) {
            startRound();
        }
    }

    public void handleItem(int playerNum, String[] data) {
        Player player = getSelfPlayer(playerNum);
        int itemSlot = Integer.parseInt(data[1]);
        Item item = player.getItem(itemSlot);
        switch (item) {
            case CIGARETTE:
                if (player.getHP() < 4) {
                    player.heal(1);
                    break;
                } else {
                    return;
                }

            case BEER:
                if (numBullets <= 1) {
                    return;
                }
                decrementBullets();
                break;
            
            case HANDCUFFS:
                if (getOpposingPlayer(playerNum).isSkippingNextTurn()) {
                    return;
                }

                getOpposingPlayer(playerNum).setIsSkippingNextTurn(true);
                break;

            case GLASS:
                announce("REVEAL;" + (bullets.get(numBullets - 1) ? 1 : 0));
                break;
            
            case REVERSE:
                bullets.set(numBullets - 1, !bullets.get(numBullets - 1));
                break;

            case CASING:
                dmgTaken++;
                dmgModified = true;
                announce("PLUS_DMG;ADD");
                break;

            default:
                return;
        }
        player.removeItem(itemSlot);
        currentEnvironment.onItemUse(this, item);
        sendGameState();
    }

    public void enhanceBullet() {
        enhanced = true;
        announce("ENHANCE;");
    }

    public Player getSelfPlayer(int playerNum) { 
        if (playerNum == 1) {
            return p1;
        } else {
            return p2;
        }
    }

    public Player getOpposingPlayer(int playerNum) {
        if (playerNum == 1) {
            return p2;
        } else {
            return p1;
        }
    }

    public int getNumBullets() { return numBullets; }

    public int getLives() {
        int lives = 0;
        for (boolean bullet : bullets) {
            if (bullet) {
                lives += 1;
            }
        }

        return lives;
    }
    public int getBlanks() {
        int blanks = 0;
        for (boolean bullet : bullets) {
            if (!bullet) {
                blanks += 1;
            }
        }

        return blanks;
    }

    public void setBullets(ArrayList<Boolean> shots) {
        bullets = shots;
        numBullets = bullets.size();
    }

    public void decrementBullets() {
        bullets.remove(numBullets - 1);
        numBullets--;
        announce("SHOOT");
        currentEnvironment.onBulletChange(this);
        if (numBullets <= 0) {
            startSet();
        }
    }

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

    public void announce(String msg) {
        try {
            p1Out.writeUTF(msg);
            p2Out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void announce(String msg1, String msg2) {
        try {
            p1Out.writeUTF(msg1);
            p2Out.writeUTF(msg2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendGameState() {
        String serializedStateForP1 = "STATE;";
        String serializedStateForP2 = "STATE;";
        // HP;enemyHP;isSkipping;isEnemySkipping;isImmune;isEnemyImmune;Items(8);EnemyItems(8);Lives;Blanks
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

    private class ReadFromClient implements Runnable {
        private DataInputStream dataIn;
        private int playerNum;

        public ReadFromClient(DataInputStream in, int pNum) {
            dataIn = in;
            playerNum = pNum;
        }

        public void run() {
            try {
                while (true) { 
                    String[] parts = dataIn.readUTF().split(";");
                    switch (parts[0]) {
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
                if (!gameEnded) {
                    endgameCallback.actionPerformed(null);
                    gameEnded = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}