/**
This class contains the code of game client. This includes GUI rendering and networking.
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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 * This class contains the code of game client. This includes GUI rendering and networking.
 */
public class GameCanvas extends JComponent implements KeyListener {
    private static final int MAX_ITEMS = 8;
    private static final int MAX_ROWS = MAX_ITEMS / 2;
    private static final int MAX_COLS = 2 * MAX_ITEMS / MAX_ROWS;
    private static final BufferedImage BG_IMAGE = Sprite.loadImage("assets/bg/bg_image.png");

    private ArrayList<Sprite> sprites;
    private Cursor itemCursor;
    private Cursor enemyCursor;
    private HPBar p1HPBar;
    private HPBar p2HPBar;
    private ScoreBoard scoreboard;
    private Magazine magazine;
    private TextBox flavorBox;
    private TextBox shootSelf, shootThem;
    private ItemSprite[] items;

    private boolean isTurn;
    private boolean isGameOver;
    private Environment currentEnvironment;
    
    private Timer animTimer;
    private DataInputStream readFromServer;
    private DataOutputStream writeToServer;

    private Font scoreFont;

    /**
     * Class constructor.
     * @param serverConnection socket hosting the connection to the GameServer
     */
    public GameCanvas(Socket serverConnection) {
        animTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
            
        });

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
                ItemSprite item = new ItemSprite(j, i, Item.EMPTY);
                items[itemCount] = item;
                itemCount++;
            }
        }

        shootThem = new TextBox(364, 140, 300, 100, "Shoot Them");
        shootSelf = new TextBox(364, 485, 300, 100, "Shoot Self");
        sprites.add(shootThem);
        sprites.add(shootSelf);
        
        itemCursor = new Cursor(10, 100, Color.BLUE);
        enemyCursor = new Cursor(10, 100, Color.RED);
        enemyCursor.row = 0;
        enemyCursor.col = 3;
        sprites.add(enemyCursor);
        sprites.add(itemCursor);

        p1HPBar = new HPBar(10, 15, Color.BLUE);
        p2HPBar = new HPBar(700, 15, Color.RED);
        sprites.add(p1HPBar);
        sprites.add(p2HPBar);
        scoreboard = new ScoreBoard(364, 15);
        sprites.add(scoreboard);

        magazine = new Magazine(364, 313, 0, 0);
        sprites.add(magazine);

        flavorBox = new TextBox(10, 600, 989, 110, "You find yourself in a unique situation...");
        sprites.add(flavorBox);

        try {
            writeToServer = new DataOutputStream(serverConnection.getOutputStream());
            readFromServer = new DataInputStream(serverConnection.getInputStream());
        } catch (IOException e) {
            System.out.println("Error in establishing connection with server: " + e);
        }

        try {
            scoreFont = Font.createFont(Font.TRUETYPE_FONT, new File("assets/bg/vcr_osd_mono.ttf")).deriveFont(Font.BOLD, 48);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();   
            ge.registerFont(scoreFont);
        } catch (IOException | FontFormatException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Starts the ReadFromServer thread and the animation timer.
     */
    public void startGameClient() {
        new Thread(new ReadFromServer(readFromServer)).start();
        animTimer.start();
        Sound.loopMusic("assets/bg/music.wav");
    }

    /**
     * Parses the game state response from the server and updates sprites accordingly.
     * @param gameState the response from the server
     */
    public void parseGameState(String[] gameState) {
        p1HPBar.hp = Integer.parseInt(gameState[1]);
        p2HPBar.hp = Integer.parseInt(gameState[2]);

        p1HPBar.isSkipping = Integer.parseInt(gameState[3]) == 1;
        p2HPBar.isSkipping = Integer.parseInt(gameState[4]) == 1;

        p1HPBar.isImmune = Integer.parseInt(gameState[5]) == 1;
        p2HPBar.isImmune = Integer.parseInt(gameState[6]) == 1;

        for (int i = 0; i < 16; i++) {
            int serverItemNum = Integer.parseInt(gameState[7 + i]);
            items[i].changeItem(Item.getItem(serverItemNum));
        }

        magazine.setLives(Integer.parseInt(gameState[23]));
        magazine.setBlanks(Integer.parseInt(gameState[24]));
    }

    /**
     * Updates the bottom TextBox based on where itemCursor is hovering.
     */
    public void updateFlavorText() {
        if (itemCursor.col == 2) {
            if (itemCursor.row == 0) {
                flavorBox.setText("Shoot your opponent and end your turn.");
            } else {
                flavorBox.setText("Shoot yourself. Skips the opponent's turn if the shell was a blank.");
            }
            return;
        }

        int itemIdx = 4*itemCursor.col + itemCursor.row;
        if (itemCursor.col > 2) {
            itemIdx -= 4;
        }

        Item item = items[itemIdx].getItem();
        if (item == Item.EMPTY) {
            flavorBox.setEnvText(currentEnvironment);
        } else {
            flavorBox.setItemText(item);
        }
    }

    /**
     * Updates the UI to reflect the change in turn.
     * @param data response from the server
     */
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

    /**
     * Clears each itemSprite.
     */
    public void cleanup() {
        for (ItemSprite itemSprite : items) {
            itemSprite.item = Item.EMPTY;
        }
    }

    /**
     * Updates each sprite first then draws them with anti-aliasing.
     */
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(BG_IMAGE, 0, 0, null);
        for (ItemSprite item : items) {
            item.draw(g2d);
        }
        for (Sprite sprite : sprites) {
            sprite.update();
            sprite.draw(g2d);
        }
    }

    /**
     * Handles key presses as long as isGameOver is false.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        try {
            if (isGameOver) {
                return;
            }
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    itemCursor.row -= 1;
                    if (itemCursor.col == 2) {
                        itemCursor.row -= 2;
                        if (itemCursor.row < 0) {
                            itemCursor.row = 3;
                        }
                    } else if (itemCursor.row < 0) {
                        itemCursor.row = MAX_ROWS - 1;
                    }
                    writeToServer.writeUTF(String.format("CURSOR;%d;%d;", itemCursor.row, itemCursor.col));
                    break;
                case KeyEvent.VK_DOWN:
                    itemCursor.row += 1;
                    if (itemCursor.col == 2) {
                        itemCursor.row += 2;
                        if (itemCursor.row > 3) {
                            itemCursor.row = 0;
                        }
                    } else if (itemCursor.row >= MAX_ROWS) {
                        itemCursor.row = 0;
                    }
                    writeToServer.writeUTF(String.format("CURSOR;%d;%d;", itemCursor.row, itemCursor.col));
                    break;
                case KeyEvent.VK_LEFT:
                    if (itemCursor.col > 0) {
                        itemCursor.col -= 1;
                        if (itemCursor.col == 2) {
                            if (itemCursor.row <= 1) {
                                itemCursor.row = 0;
                            } else {
                                itemCursor.row = 3;
                            }
                        }
                    } else {
                        itemCursor.col = 4;
                    }
                    writeToServer.writeUTF(String.format("CURSOR;%d;%d;", itemCursor.row, itemCursor.col));
                    break;
                case KeyEvent.VK_RIGHT:
                    if (itemCursor.col < MAX_COLS) {
                        itemCursor.col += 1;
                        if (itemCursor.col == 2) {
                            if (itemCursor.row <= 1) {
                                itemCursor.row = 0;
                            } else {
                                itemCursor.row = 3;
                            }
                        }
                    } else {
                        itemCursor.col = 0;
                    }
                    writeToServer.writeUTF(String.format("CURSOR;%d;%d;", itemCursor.row, itemCursor.col));
                    break;

                case KeyEvent.VK_Z:
                    if (!isTurn) {
                        break;
                    }
                    String cmd;
                    if (itemCursor.col == 2) {
                        cmd = "SHOOT;";
                        if (itemCursor.row == 0) {
                            cmd += "enemy";
                        } else {
                            cmd += "self";
                        }
                    } else if (itemCursor.col < 2) {
                        cmd = "ITEM;";
                        int itemIdx = 4*itemCursor.col + itemCursor.row;
                        if (items[itemIdx].getItem() == Item.REVERSE) {
                            magazine.isRevealedLive = !magazine.isRevealedLive;
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

    /**
     * No function. Required by the KeyListener interface.
     */
    @Override
    public void keyReleased(KeyEvent e) {}

    /**
     * No function. Required by the KeyListener interface.
     */
    @Override
    public void keyTyped(KeyEvent e) {}

    /**
    This class acts as an item slot and render an item's icon onto the screen. ItemSprite's items can be changed on command and its image will changed with it.
    */
    private class ItemSprite extends Sprite {
        Item item;
        final Rectangle2D.Double outline;

        /**
         * Class constructor.
         * @param r the row of this ItemSprite
         * @param c the column of this ItemSprite
         * @param item the item contained by this ItemSprite
         */
        public ItemSprite(int r, int c, Item item) {
            super(getXFromCol(c), getYFromRow(r), item.getSpritePath());
            this.item = item;
            outline = new Rectangle2D.Double(getX(), getY(), 100, 100);
        }

        /**
         * Returns the x value of a column
         * @param col the column
         * @return x-coordinate
         */
        public static double getXFromCol(int col) {
            if (col < 2) {
                return 10 + 115*col;
            } else {
                return 784 + 115*(col - 3);
            }
        }

        /**
         * Returns the y value of a row
         * @param row the row
         * @return y-coordinate
         */
        public static double getYFromRow(int row) {
            return 140 + 115*row;
        }

        /**
         * Draws the item then a black frame.
         */
        @Override
        public void draw(Graphics2D g2d) {
            super.draw(g2d);
            g2d.setColor(Color.BLACK);
            g2d.draw(outline);
        }

        /**
         * Changes the item of this ItemSprite
         * @param item
         */
        public void changeItem(Item item) {
            this.item = item;
            setImage(item.getSpritePath());
        }

        /**
         * Returns this ItemSprite's item.
         * @return the item
         */
        public Item getItem() { return item; }
    }

    /**
    This class represents the players' cursors. Setting its targetX and targetY will make the cursor move towards it each frame.
    */
    private class Cursor extends Sprite {
        int w;
        final int h;
        int row;
        int col;
        final Color color;

        /**
         * Class constructor
         * @param x x-coordinate
         * @param y y-coordinate
         * @param c color
         */
        public Cursor(double x, double y, Color c) {
            super(x, y, "");
            w = 100;
            h = 100;
            row = 0;
            col = 0;
            color = c;
        }

        /**
         * Sets this Cursor's targetX and targetY based on its row and column then calls super.update
         */
        @Override
        public void update() {
            if (col < 2) {
                w = 100;
                setTargetX(10 + 115*(col));
                setTargetY(140 + 115*(row));
            } else if (col == 2) {
                w = 300;
                setTargetX(364);
                setTargetY(140 + row * 115);
            } else {
                w = 100;
                setTargetX(784 + 115*(col - 3));
                setTargetY(140 + 115*(row));
            }
            super.update();
        }

        /**
         * Draws the cursor
         */
        @Override
        public void draw(Graphics2D g2d) {
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(color);
            g2d.draw(new Rectangle2D.Double(getX(), getY(), w, h));
            g2d.setStroke(new BasicStroke(1));
        }
    }

    /**
    This class represents the players' HP bars. Has status effects for being immune to damage and being handcuffed.
    */
    private class HPBar extends Sprite {
        int hp;
        final Color color;
        boolean isTurn;
        boolean isSkipping;
        boolean isImmune;

        final BufferedImage lockedStatusImg;
        final BufferedImage immuneStatusImg;

        /**
         * Class constructor.
         * @param x x-coordinate of the HP bar
         * @param y y-coordinate of the HP bar
         * @param c color of the HP bar
         */
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

        /**
         * Draws the corresponding status icons if they are active.
         */
        public void drawStatus(Graphics2D g2d) {
            int xOffset = 10;
            if (isSkipping) {
                g2d.drawImage(lockedStatusImg, null, (int) getX() + xOffset, (int) getY() + 5);
                xOffset += 30;
            }
            if (isImmune) {
                g2d.drawImage(immuneStatusImg, null, (int) getX() + xOffset, (int) getY() + 5);
            }
        }

        /**
         * Draws the sprite. HP is represented by circles.
         */
        @Override
        public void draw(Graphics2D g2d) {
            if (isTurn) {
                g2d.setStroke(new BasicStroke(3));
            }
            g2d.setColor(color);
            g2d.draw(new Rectangle2D.Double(getX(), getY(), 300, 110));

            for (int i = 0; i < hp; i++) {
                g2d.draw(new Ellipse2D.Double(getX() + 25 + i*65, getY() + 30, 50, 50));
            }
            g2d.setStroke(new BasicStroke(1));

            drawStatus(g2d);
        }
    }

    /**
    This class represents the players' scores. Scores are stored in myScore and theirScore.
    */
    private class ScoreBoard extends Sprite {
        int myScore, theirScore;

        /**
         * Class constructor.
         * @param x x-coordinate of this ScoreBoard
         * @param y y-coordinate of this ScoreBoard
         */
        public ScoreBoard(double x, double y) {
            super(x, y, "");
            myScore = 0;
            theirScore = 0;
        }

        /**
         * Draws the scoreboard in VCR OSD Mono 48 Bold.
         */
        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(Color.WHITE);
            g2d.draw(new Rectangle2D.Double(getX(), getY(), 300, 110));
            g2d.setFont(scoreFont);
            String scoreText = String.format("%d - %d", myScore, theirScore);
            FontMetrics fm = g2d.getFontMetrics();
            int xOffset = (300 - fm.stringWidth(scoreText)) / 2;
            int yOffset = (110 - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(scoreText, (int) getX() + xOffset, (int) getY() + yOffset);
        }

        /**
         * Sets the Player's score
         * @param score Player's score
         */
        public void setMyScore(int score) { myScore = score; }

        /**
         * Sets the opponent's score
         * @param score opponent's score
         */
        public void setTheirScore(int score) { theirScore = score; }
    }

    /**
    This class represents the gun's magazine. Also shows whether any damage modifiers are active.
    */
    private class Magazine extends Sprite {
        int lives;
        int blanks;
        int dmgModifier;
        boolean revealed;
        boolean enhanced;
        boolean isRevealedLive;
        final Rectangle2D.Double outline;
        final BufferedImage plusMarks;
        
        /**
         * Class constructor.
         * @param x x-coordinate
         * @param y y-coordinate
         * @param lives number of live shells
         * @param blanks number of blanks
         */
        public Magazine(double x, double y, int lives, int blanks) {
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

        /**
         * Draws this Magazine. Shells are represented by colored rectangles and revealed shells have thicker outlines.
         * 
         * When the shell is enhanced, the outline turns purple. Damage modifiers are drawn in the top left.
         */
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

        /**
         * Sets the number of live shells.
         * @param l number of live shells.
         */
        public void setLives(int l) { lives = l; }

        /**
         * Sets the number of blanks
         * @param b number of blanks
         */
        public void setBlanks(int b) { blanks = b; }
    }

    /**
    This class constantly reads from the game server for events and handles them. Commands are in all caps and are delimited by ';'.
    */
    private class ReadFromServer implements Runnable {
        final DataInputStream dataIn;
        
        /**
         * Class constructor.
         * @param in DataInputStream from the server
         */
        public ReadFromServer(DataInputStream in) {
            dataIn = in;
        }

        /**
         * Handles server responses while isGameOver is false.
         */
        @Override
        public void run() {
            try {
                while (!isGameOver) {
                    String[] parts = dataIn.readUTF().split(";");
                    switch (parts[0]) {
                        case "CURSOR":
                            enemyCursor.row = Integer.parseInt(parts[1]);
                            int col = Integer.parseInt(parts[2]);
                            if (col == 2) {
                                enemyCursor.col = col;
                            } else {
                                enemyCursor.col = (col + 3) % 6;
                            }
                            break;
                        case "NEW_ROUND":
                            scoreboard.setMyScore(Integer.parseInt(parts[1]));
                            scoreboard.setTheirScore(Integer.parseInt(parts[2]));
                            break;
                        case "STATE":
                            parseGameState(parts);
                            break;
                        case "SHOOT":
                            if (magazine.revealed) {
                                magazine.revealed = false;
                                magazine.isRevealedLive = false;
                            }
                            if (magazine.enhanced) {
                                magazine.enhanced = false;
                            }
                            break;
                        case "SOUND":
                            Sound.playSFX(parts[1]);
                            break;
                        case "TURN":
                            changeTurn(parts);
                            break;
                        case "REVEAL":
                            magazine.revealed = true;
                            magazine.isRevealedLive = Integer.parseInt(parts[1]) == 1;
                            flavorBox.setText(String.format("A %s round has been revealed", magazine.isRevealedLive ? "live" : "blank"));
                            break;
                        case "ENHANCE":
                            magazine.enhanced = Integer.parseInt(parts[1]) == 1;
                            break;
                        case "PLUS_DMG":
                            if (parts[1].equals("ADD")) {
                                magazine.dmgModifier++;
                            } else {
                                magazine.dmgModifier = 0;
                            }
                            break;
                        case "ENV":
                            currentEnvironment = Environment.getEnvironment(Integer.parseInt(parts[1]));
                            break;
                        case "WIN":
                            flavorBox.setText("You have bested your opponent.");
                            Sound.playSFX("assets/sfx/win.wav");
                            cleanup();
                            isGameOver = true;
                            break;
                        case "LOSE":
                            flavorBox.setText("You have lost your life.");
                            Sound.playSFX("assets/sfx/lose.wav");
                            cleanup();
                            isGameOver = true;
                            break;
                        case "DISCONNECT":
                            flavorBox.setText("The other player has disconnected.");
                            cleanup();
                            isGameOver = true;
                            break;
                        default:
                            break;
                    }
                }
            } catch (SocketException e) {
                flavorBox.setText("Disconnected from server. Please exit the game.");
                isGameOver = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
