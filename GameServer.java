import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private ServerSocket ss;
    private static final int REQUIRED_PLAYERS = 2;
    private int numPlayers;
    private DataOutputStream p1Out;
    private DataOutputStream p2Out;

    private Player p1;
    private Player p2;
    private int currentTurn;
    
    private int numBullets;
    private ArrayList<Boolean> bullets;

    private int dmgTaken;

    public GameServer() {
        try {
            numPlayers = 0;
            ss = new ServerSocket(7777);
        } catch (Exception e) {
        }
    }
    
    public void acceptConnections() {
        try {
            System.out.println("Awaiting players...");
            while (numPlayers < REQUIRED_PLAYERS) {
                numPlayers++;
                
                Socket socket = ss.accept();
                DataInputStream in = new DataInputStream(socket.getInputStream());
                if (numPlayers == 1) {
                    p1Out = new DataOutputStream(socket.getOutputStream());
                } else {
                    p2Out = new DataOutputStream(socket.getOutputStream());
                }

                new Thread(new ReadFromClient(in, numPlayers)).start();
            }
        } catch (Exception e) {
        }
    }

    public void startGame() {
        p1 = new Player(1);
        p2 = new Player(2);
    }

    public void startRound() {
        currentTurn = Math.random() < 0.5 ? 1 : 2;
        numBullets = (int) ((Math.random() * 7) + 2);
        bullets = new ArrayList<>();
        bullets.add(true);
        bullets.add(false);
        for (int i = 0; i < numBullets - 2; i++) {
            bullets.add(Math.random() < 0.5);
        }

        Collections.shuffle(bullets);

        p1.clearItems();
        p2.clearItems();
        int numItems = (int) (Math.random() * 9);
        for (int i = 0; i < numItems; i++) {
            Item item = Item.getItem((int) (Math.random() * 5));
            p1.addItem(item);
            p2.addItem(item);
        }

        dmgTaken = 1;
        sendGameState();
        changeTurn();
    }

    public void handleShoot(int playerNum, String[] data) {
        if (data[1].equals("self")) {
            if (bullets.get(numBullets - 1)) {
                getSelfPlayer(playerNum).takeDamage(dmgTaken);
                changeTurn();
            }
        } else {
            if (bullets.get(numBullets - 1)) {
                getOpposingPlayer(playerNum).takeDamage(dmgTaken);
            }
            changeTurn();
        }
        bullets.remove(numBullets - 1);
        numBullets--;
        if (numBullets == 0) {
            startRound();
        }
        announce("SHOOT");
        sendGameState();
    }

    public void handleItem(int playerNum, String[] data) {
        Player player = getSelfPlayer(playerNum);
        int itemSlot = Integer.parseInt(data[1]);
        Item item = player.getItem(itemSlot);
        switch (item) {
            case CIGARETTE:
                if (player.getHP() < 4) {
                    player.heal(1);
                }
                break;

            case BEER:
                bullets.remove(numBullets - 1);
                numBullets--;
                if (numBullets == 0) {
                    startRound();
                }
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

            default:
                return;
        }
        player.removeItem(itemSlot);
        sendGameState();
    }
    
    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.acceptConnections();
        gameServer.startGame();
        gameServer.startRound();
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
        // HP;enemyHP;isSkipping;isEnemySkipping;Items(8);EnemyItems(8);Lives;Blanks
        serializedStateForP1 += String.format(
            "%d;%d;%d;%d;",
            p1.getHP(),
            p2.getHP(),
            p1.isSkippingNextTurn() ? 1 : 0,
            p2.isSkippingNextTurn() ? 1 : 0
        );
        serializedStateForP2 += String.format(
            "%d;%d;%d;%d;",
            p2.getHP(),
            p1.getHP(),
            p2.isSkippingNextTurn() ? 1 : 0,
            p1.isSkippingNextTurn() ? 1 : 0
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}