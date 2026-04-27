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
            startGame();
            startRound();
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

        dmgTaken = 1;
        sendGameState();
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
        sendGameState();
    }
    
    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.acceptConnections();
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
    }

    public void sendGameState() {
        String serializedStateForP1 = "STATE;";
        String serializedStateForP2 = "STATE;";
        // HP;enemyHP;isImmune;isEnemyImmune;Items(8);EnemyItems(8);Turn;Lives;Blanks
        serializedStateForP1 += String.format(
            "%d;%d;%d;%d;",
            p1.getHP(),
            p2.getHP(),
            p1.isImmune() ? 1 : 0,
            p2.isImmune() ? 1 : 0
        );
        serializedStateForP2 += String.format(
            "%d;%d;%d;%d;",
            p2.getHP(),
            p1.getHP(),
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

        serializedStateForP1 += currentTurn == 1 ? "1;" : "0;";
        serializedStateForP2 += currentTurn == 2 ? "1;" : "0;";

        serializedStateForP1 += String.format("%d;%d", getLives(), getBlanks());
        serializedStateForP2 += String.format("%d;%d", getLives(), getBlanks());
        try {
            p1Out.writeUTF(serializedStateForP1);
            p2Out.writeUTF(serializedStateForP2);
        } catch (Exception e) {
        }
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