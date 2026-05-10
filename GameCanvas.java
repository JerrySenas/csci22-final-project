import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;

public class GameCanvas extends JComponent implements KeyListener {

    private static final int MAX_ITEMS = 8;
    private static final int MAX_ROWS = MAX_ITEMS / 2;
    private static final int MAX_COLS = 2 * MAX_ITEMS / MAX_ROWS;

    private ArrayList<Sprite> sprites;
    private Cursor itemCursor;
    private HPBar p1HPBar;
    private HPBar p2HPBar;
    private ScoreBoard scoreboard;
    private Rack rack;
    private TextBox flavorBox;
    private TextBox shootSelf, shootThem;
    private ItemSprite[] items;

    private int selectedRow;
    private int selectedCol;
    private boolean isTurn;
    private boolean isGameOver;
    private Environment currentEnvironment;
    
    private Timer animTimer;
    private DataInputStream readFromServer;
    private DataOutputStream writeToServer;

    public GameCanvas(Socket serverConnection) {
        animTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
            
        });

        selectedRow = 0;
        selectedCol = 0;
        isTurn = false;
        isGameOver = false;

        sprites = new ArrayList<>();
        items = new ItemSprite[16];
        int itemCount = 0;
        for (int i = 0; i < 5; i++) {
            if (i == 2) {
                continue;
            }
            for (int j = 0; j < 4; j++) {
                ItemSprite item = new ItemSprite(j, i, 0 );
                items[itemCount] = item;
                itemCount++;
            }
        }

        shootThem = new TextBox(364, 140, 300, 100, "Shoot Them");
        shootSelf = new TextBox(364, 485, 300, 100, "Shoot Self");
        sprites.add(shootThem);
        sprites.add(shootSelf);
        
        itemCursor = new Cursor(10, 100);
        sprites.add(itemCursor);

        p1HPBar = new HPBar(10, 15, Color.BLACK);
        p2HPBar = new HPBar(700, 15, Color.RED);
        sprites.add(p1HPBar);
        sprites.add(p2HPBar);
        scoreboard = new ScoreBoard(364, 15);
        sprites.add(scoreboard);

        rack = new Rack(364, 313, 0, 0);
        sprites.add(rack);

        flavorBox = new TextBox(10, 600, 989, 110, "You find yourself in a unique situation...");
        sprites.add(flavorBox);

        try {
            writeToServer = new DataOutputStream(serverConnection.getOutputStream());
            readFromServer = new DataInputStream(serverConnection.getInputStream());
        } catch (IOException e) {
            System.out.println("Error in establishing connection with server: " + e);
        }
    }
    
    public void startGameClient() {
        new Thread(new ReadFromServer(readFromServer)).start();
        animTimer.start();
    }

    public void parseGameState(String[] gameState) {
        p1HPBar.hp = Integer.parseInt(gameState[1]);
        p2HPBar.hp = Integer.parseInt(gameState[2]);

        p1HPBar.isSkipping = Integer.parseInt(gameState[3]) == 1;
        p2HPBar.isSkipping = Integer.parseInt(gameState[4]) == 1;

        p1HPBar.isImmune = Integer.parseInt(gameState[5]) == 1;
        p2HPBar.isImmune = Integer.parseInt(gameState[6]) == 1;

        for (int i = 0; i < 16; i++) {
            int serverItemNum = Integer.parseInt(gameState[7 + i]);
            items[i].changeItem(serverItemNum);
        }

        rack.setLives(Integer.parseInt(gameState[23]));
        rack.setBlanks(Integer.parseInt(gameState[24]));
    }

    public void updateFlavorText() {
        if (selectedCol == 2) {
            if (selectedRow == 0) {
                flavorBox.setText("Shoot your opponent and end your turn.");
            } else {
                flavorBox.setText("Shoot yourself. Skips the opponent's turn if the shell was a blank.");
            }
            return;
        }

        int itemIdx = 4*selectedCol + selectedRow;
        if (selectedCol > 2) {
            itemIdx -= 4;
        }

        Item item = items[itemIdx].getItem();
        if (item == Item.EMPTY) {
            flavorBox.setText(String.format("Current environment: %s\n\n - %s", currentEnvironment.getName(), currentEnvironment.getDescription()));
        } else {
            flavorBox.setText(String.format("%s\n\n - %s", item.getName(), item.getDescription()));
        }
    }

    public void changeTurn(String[] data) {
        isTurn = Integer.parseInt(data[1]) == 1;
        p1HPBar.isTurn = false;
        p2HPBar.isTurn = false;
        if (isTurn) {
            flavorBox.setText("It's your turn. Time to make your choice.");
            p1HPBar.isTurn = true;
        } else {
            flavorBox.setText("You look over the table nervously...");
            p2HPBar.isTurn = true;
        }
    }

    public void cleanup() {
        for (ItemSprite itemSprite : items) {
            itemSprite.item = Item.EMPTY;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (ItemSprite item : items) {
            item.draw(g2d);
        }
        for (Sprite sprite : sprites) {
            sprite.update();
            sprite.draw(g2d);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        try {
            if (isGameOver) {
                return;
            }
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    selectedRow -= 1;
                    if (selectedCol == 2) {
                        selectedRow -= 2;
                        if (selectedRow < 0) {
                            selectedRow = 3;
                        }
                    } else if (selectedRow < 0) {
                        selectedRow = MAX_ROWS - 1;
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    selectedRow += 1;
                    if (selectedCol == 2) {
                        selectedRow += 2;
                        if (selectedRow > 3) {
                            selectedRow = 0;
                        }
                    } else if (selectedRow >= MAX_ROWS) {
                        selectedRow = 0;
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    if (selectedCol > 0) {
                        selectedCol -= 1;
                        if (selectedCol == 2) {
                            if (selectedRow <= 1) {
                                selectedRow = 0;
                            } else {
                                selectedRow = 3;
                            }
                        }
                    } else {
                        selectedCol = 4;
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (selectedCol < MAX_COLS) {
                        selectedCol += 1;
                        if (selectedCol == 2) {
                            if (selectedRow <= 1) {
                                selectedRow = 0;
                            } else {
                                selectedRow = 3;
                            }
                        }
                    } else {
                        selectedCol = 0;
                    }
                    break;

                case KeyEvent.VK_Z:
                    if (!isTurn) {
                        break;
                    }
                    String cmd;
                    if (selectedCol == 2) {
                        cmd = "SHOOT;";
                        if (selectedRow == 0) {
                            cmd += "enemy";
                        } else {
                            cmd += "self";
                        }
                    } else if (selectedCol < 2) {
                        cmd = "ITEM;";
                        int itemIdx = 4*selectedCol + selectedRow;
                        if (items[itemIdx].getItem() == Item.REVERSE) {
                            rack.isRevealedLive = !rack.isRevealedLive;
                        }
                        cmd += itemIdx;
                    } else {
                        break;
                    }
                    writeToServer.writeUTF(cmd);
                    writeToServer.flush();
                    break;
                default:
                    break;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        updateFlavorText();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    private class ItemSprite extends Sprite {
        Item item;
        final Rectangle2D.Double outline;

        public ItemSprite(int r, int c, Item item) {
            super(getXFromCol(c), getYFromRow(r), item.getSpritePath());
            this.item = item;
            outline = new Rectangle2D.Double(getX(), getY(), 100, 100);
        }

        public ItemSprite(int r, int c, int itemNum) {
            this(r, c, Item.getItem(itemNum));
        }

        public static double getXFromCol(int col) {
            if (col < 2) {
                return 10 + 115*col;
            } else {
                return 784 + 115*(col - 3);
            }
        }

        public static double getYFromRow(int row) {
            return 140 + 115*row;
        }

        public void changeItem(int itemNum) {
            item = Item.getItem(itemNum);
            setImage(item.getSpritePath());
        }

        @Override
        public void draw(Graphics2D g2d) {
            super.draw(g2d);
            g2d.setColor(Color.BLACK);
            g2d.draw(outline);
        }

        public Item getItem() { return item; }
    }

    private class Cursor extends Sprite {
        int w;
        final int h;

        public Cursor(double x, double y) {
            super(x, y, "");
            w = 100;
            h = 100;
        }

        @Override
        public void update() {
            if (selectedCol < 2) {
                w = 100;
                setTargetX(10 + 115*(selectedCol));
                setTargetY(140 + 115*(selectedRow));
            } else if (selectedCol == 2) {
                w = 300;
                setTargetX(364);
                setTargetY(140 + selectedRow * 115);
            } else {
                w = 100;
                setTargetX(784 + 115*(selectedCol - 3));
                setTargetY(140 + 115*(selectedRow));
            }
            super.update();
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(Color.RED);
            g2d.draw(new Rectangle2D.Double(getX(), getY(), w, h));
            g2d.setStroke(new BasicStroke(1));
        }
    }

    private class HPBar extends Sprite {
        int hp;
        final Color color;
        boolean isTurn;
        boolean isSkipping;
        boolean isImmune;

        final BufferedImage lockedStatusImg;
        final BufferedImage immuneStatusImg;

        public HPBar(double x, double y, Color c) {
            super(x, y, "");
            color = c;
            hp = 4;
            isTurn = false;
            isSkipping = true;
            isImmune = true;

            lockedStatusImg = Sprite.loadImage("assets/status/lock.png");
            immuneStatusImg = Sprite.loadImage("assets/status/immune.png");
        }

        public void drawStatus(Graphics2D g2d) {
            int xOffset = 10;
            if (isSkipping) {
                g2d.drawImage(lockedStatusImg, null, (int) getX() + xOffset, (int) getY() + 5);
                xOffset += 30;
            }
            if (isImmune) {
                g2d.drawImage(immuneStatusImg, null, (int) getX() + xOffset, (int) getY() + 5);
                xOffset += 30;
            }
        }

        @Override
        public void draw(Graphics2D g2d) {
            if (isTurn) {
                g2d.setStroke(new BasicStroke(3));
            }
            g2d.setColor(color);
            g2d.draw(new Rectangle2D.Double(getX(), getY(), 300, 110));
            
            g2d.setColor(Color.BLUE);
            if (isSkipping) {
                g2d.setColor(Color.RED);
            }

            for (int i = 0; i < hp; i++) {
                g2d.draw(new Ellipse2D.Double(getX() + 25 + i*65, getY() + 30, 50, 50));
            }
            g2d.setStroke(new BasicStroke(1));

            drawStatus(g2d);
        }
    }

    private class ScoreBoard extends Sprite {
        int myScore, theirScore;

        public ScoreBoard(double x, double y) {
            super(x, y, "");
            myScore = 0;
            theirScore = 0;
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(Color.BLACK);
            g2d.draw(new Rectangle2D.Double(getX(), getY(), 300, 110));
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.drawString(String.format("%d - %d", myScore, theirScore), (int) getX() + 100, (int) getY() + 75);
        }

        public void setMyScore(int score) { myScore = score; }
        public void setTheirScore(int score) { theirScore = score; }
    }

    private class Rack extends Sprite {
        int lives;
        int blanks;
        int dmgModifier;
        boolean revealed;
        boolean enhanced;
        boolean isRevealedLive;
        final Rectangle2D.Double outline;
        final BufferedImage plusMarks;
        
        public Rack(double x, double y, int lives, int blanks) {
            super(x, y, "");
            this.lives = lives;
            this.blanks = blanks;
            dmgModifier = 0;
            revealed = false;
            enhanced = false;
            isRevealedLive = false;
            outline = new Rectangle2D.Double(x, y, 300, 100);
            plusMarks = Sprite.loadImage("assets/status/plus1.png");
        }

        @Override
        public void draw(Graphics2D g2d) {
            if (enhanced) {
                g2d.setColor(Color.MAGENTA);
                g2d.setStroke(new BasicStroke(3));
            } else {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
            }
            g2d.draw(outline);
            g2d.setStroke(new BasicStroke(1));

            for (int i = 0; i < lives + blanks; i++) {
                if (i < lives) {
                    g2d.setColor(Color.RED);
                } else {
                    g2d.setColor(Color.BLUE);
                }
                g2d.draw(new Rectangle2D.Double(getX() + 30 + i * 30, getY() + 25, 20, 50));
            }

            if (revealed) {
                g2d.setStroke(new BasicStroke(3));
                if (isRevealedLive) {
                    g2d.setColor(Color.RED);
                    g2d.draw(new Rectangle2D.Double(getX() + 30, getY() + 25, 20, 50));
                } else {
                    g2d.setColor(Color.BLUE);
                    g2d.draw(new Rectangle2D.Double(getX() + 30 + lives * 30, getY() + 25, 20, 50));
                }
                g2d.setStroke(new BasicStroke(1));
            }

            for (int i = 0; i < dmgModifier; i++) {
                g2d.drawImage(plusMarks, null, (int) getX() + 10 + 5*i, (int) getY() + 10);
            }
        }

        public void setLives(int l) { lives = l; }
        public void setBlanks(int b) { blanks = b; }
    }

    private class ReadFromServer implements Runnable {
        final DataInputStream dataIn;
        
        public ReadFromServer(DataInputStream in) {
            dataIn = in;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String[] parts = dataIn.readUTF().split(";");
                    switch (parts[0]) {
                        case "NEW_ROUND":
                            scoreboard.setMyScore(Integer.parseInt(parts[1]));
                            scoreboard.setTheirScore(Integer.parseInt(parts[2]));
                            break;
                        case "STATE":
                            parseGameState(parts);
                            break;
                        case "SHOOT":
                            if (rack.revealed) {
                                rack.revealed = false;
                                rack.isRevealedLive = false;
                            }
                            if (rack.enhanced) {
                                rack.enhanced = false;
                            }
                            break;
                        case "TURN":
                            changeTurn(parts);
                            break;
                        case "REVEAL":
                            rack.revealed = true;
                            rack.isRevealedLive = Integer.parseInt(parts[1]) == 1;
                            flavorBox.setText(String.format("A %s round has been revealed", rack.isRevealedLive ? "live" : "blank"));
                            break;
                        case "ENHANCE":
                            rack.enhanced = true;
                            break;
                        case "PLUS_DMG":
                            if (parts[1].equals("ADD")) {
                                rack.dmgModifier++;
                            } else {
                                rack.dmgModifier = 0;
                            }
                            break;
                        case "ENV":
                            currentEnvironment = Environment.getEnvironment(Integer.parseInt(parts[1]));
                            break;
                        case "WIN":
                            flavorBox.setText("You have bested your opponent.");
                            cleanup();
                            isGameOver = true;
                            break;
                        case "LOSE":
                            flavorBox.setText("You have lost your life.");
                            cleanup();
                            isGameOver = true;
                            break;
                        default:
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
