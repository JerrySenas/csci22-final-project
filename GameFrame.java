import javax.swing.*;

public class GameFrame {
    private JFrame frame;
    private int width;
    private int height;

    private JPanel cp;

    private GameCanvas gameCanvas;

    public GameFrame() {
        frame = new JFrame();
        width = 1024;
        height = 768;
        gameCanvas = new GameCanvas();
    }

    public void setupGUI() {
        cp = (JPanel) frame.getContentPane();
        cp.setFocusable(true);
        frame.setSize(width, height);
        frame.setTitle("Final Project - Senas - Soriano");

        cp.add(gameCanvas);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.addKeyListener(gameCanvas);
        frame.setFocusable(true);
        frame.requestFocusInWindow();
    }
}
