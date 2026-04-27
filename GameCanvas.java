import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;

public class GameCanvas extends JComponent implements KeyListener {

    private ArrayList<Sprite> sprites;
    private Cursor itemCursor;
    private HPBar p1HPBar;
    private HPBar p2HPBar;
    private Rack rack;
    private TextBox flavorBox;
    private TextBox shootSelf, shootThem;
    private Item[] items;

    private int selectedRow;
    private int selectedCol;
    private static final int MAX_ITEMS = 8;
    private static final int MAX_ROWS = MAX_ITEMS / 2;
    private static final int MAX_COLS = 2 * MAX_ITEMS / MAX_ROWS;
    private boolean isTurn;
    
    private Timer animTimer;
    private DataOutputStream writeToServer;

    public GameCanvas() {
        animTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
                repaint();
            }
            
        });

        selectedRow = 0;
        selectedCol = 0;
        isTurn = false;

        sprites = new ArrayList<>();
        items = new Item[16];
        int itemCount = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                if (j == 2) {
                    continue;
                }
                Item item = new Item(i, j, (j < 2) ? Color.BLACK : Color.RED );
                sprites.add(item);
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

        rack = new Rack(364, 313, 0, 0);
        sprites.add(rack);

        flavorBox = new TextBox(10, 600, 1004, 110, "You find yourself in a unique situation...");
        sprites.add(flavorBox);

        try {
            Socket socket = new Socket("localhost", 7777);
            writeToServer = new DataOutputStream(socket.getOutputStream());
            new Thread(new ReadFromServer(new DataInputStream(socket.getInputStream()))).start();
        } catch (IOException e) {
            System.out.println("Error in establishing connection with server: " + e);
        }

        animTimer.start();
    }

    public void update() {
        for (Sprite sprite : sprites) {
            sprite.update();
        }
    }

    public void parseGameState(String[] gameState) {
        p1HPBar.setHP(Integer.parseInt(gameState[1]));
        p2HPBar.setHP(Integer.parseInt(gameState[2]));
        isTurn = Integer.parseInt(gameState[21]) == 1;
        if (isTurn) {
            flavorBox.setText("It's your turn. Time to make your choice.");
        } else {
            flavorBox.setText("You look over the table nervously...");

        }
        rack.setLives(Integer.parseInt(gameState[22]));
        rack.setBlanks(Integer.parseInt(gameState[23]));
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (Sprite sprite : sprites) {
            sprite.update();
            sprite.draw(g2d);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        try {
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
                    if (selectedCol == 2) {
                        String cmd = "SHOOT;";
                        if (selectedRow == 0) {
                            cmd += "enemy";
                        } else {
                            cmd += "self";
                        }
                        writeToServer.writeUTF(cmd);
                    } else {
                        writeToServer.writeUTF("Yo");
                    }
                    writeToServer.flush();
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    private class Item extends Sprite {
        private Rectangle2D.Double outline;
        private Color color;

        public Item(int r, int c, Color col) {
            super(getXFromCol(c), getYFromRow(r), null);
            outline = new Rectangle2D.Double(getX(), getY(), 100, 100);
            color = col;
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

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.draw(outline);
        }
    }

    private class Cursor extends Sprite {
        private int w;
        private int h;

        public Cursor(double x, double y) {
            super(x, y, null);
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
        private int hp;
        private Color color;
        public HPBar(double x, double y, Color c) {
            super(x, y, null);
            color = c;
            hp = 4;
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(Color.BLACK);
            g2d.draw(new Rectangle2D.Double(getX(), getY(), 300, 110));
            for (int i = 0; i < hp; i++) {
                g2d.setColor(color);
                g2d.draw(new Ellipse2D.Double(getX() + 25 + i*65, getY() + 30, 50, 50));
            }
        }

        public void setHP(int newHP) { hp = newHP; }
    }

    private class Rack extends Sprite {
        private int lives;
        private int blanks;
        private final Rectangle2D.Double outline;
        
        public Rack(double x, double y, int lives, int blanks) {
            super(x, y, null);
            this.lives = lives;
            this.blanks = blanks;
            outline = new Rectangle2D.Double(x, y, 300, 100);
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(Color.BLACK);
            g2d.draw(outline);
            for (int i = 0; i < lives + blanks; i++) {
                if (i < lives) {
                    g2d.setColor(Color.RED);
                } else {
                    g2d.setColor(Color.BLUE);
                }
                g2d.draw(new Rectangle2D.Double(getX() + 30 + i * 30, getY() + 25, 20, 50));
            }
        }

        public void setLives(int l) { lives = l; }
        public void setBlanks(int b) { blanks = b; }
    }

    private class TextBox extends Sprite {
        private String text;
        private final double width, height;
        private final Rectangle2D.Double outline;
        public TextBox(double x, double y, double w, double h, String txt) {
            super(x, y, null);
            text = txt;
            width = w;
            height = h;
            outline = new Rectangle2D.Double(x, y, width, height);
        }

        public void setText(String txt) { text = txt; }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(Color.BLACK);
            g2d.draw(outline);
            g2d.drawString(text, (int) getX() + 100, (int) getY() + 30);
        }
    }

    private class ReadFromServer implements Runnable {
        private DataInputStream dataIn;
        
        public ReadFromServer(DataInputStream in) {
            dataIn = in;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String[] parts = dataIn.readUTF().split(";");
                    switch (parts[0]) {
                        case "STATE":
                            parseGameState(parts);
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
