import java.net.Socket;
import javax.swing.*;

public class GameFrame {
    private final JFrame frame;
    private final int width;
    private final int height;

    private JPanel cp;

    private final GameCanvas gameCanvas;

    public GameFrame(Socket serverConnection) {
        frame = new JFrame();
        width = 1024;
        height = 768;
        gameCanvas = new GameCanvas(serverConnection);
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

        gameCanvas.startGameClient();
    }
}
