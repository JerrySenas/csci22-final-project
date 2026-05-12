/**
This class contains the JFrame where the GameCanvas is hosted on. It also passes the socket from the GameStarter to the GameCanvas. 
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

import java.awt.Dimension;
import java.net.Socket;
import javax.swing.*;

public class GameFrame {
    private final JFrame frame;
    private final int width;
    private final int height;

    private JPanel cp;

    private final GameCanvas gameCanvas;

    /**
     * Class constructor. Passes the server connection to the GameCanvas.
     * @param serverConnection the socket to be passed to GameCanvas
     */
    public GameFrame(Socket serverConnection) {
        frame = new JFrame();
        width = 1024;
        height = 768;
        gameCanvas = new GameCanvas(serverConnection);
    }

    /**
     * Sets up the game UI.
     */
    public void setupGUI() {
        cp = (JPanel) frame.getContentPane();
        cp.setFocusable(true);
        frame.setTitle("Final Project - Senas - Soriano");

        cp.add(gameCanvas);
        gameCanvas.setPreferredSize(new Dimension(width, height));
        
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.addKeyListener(gameCanvas);
        frame.setFocusable(true);
        frame.requestFocusInWindow();

        gameCanvas.startGameClient();
    }
}
