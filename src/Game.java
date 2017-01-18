import javax.swing.*;
import java.awt.*;

public class Game extends JPanel {
    public boolean running = false;
    public JFrame frame = null;

    // new game instance
    public Game() {
        this.running = true;

        this.frame = new JFrame("Game");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(500, 500);
        this.frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.frame.add(this);
        this.frame.setBackground(Color.BLACK);
        this.frame.setVisible(true);
    }

    // draw game
    public void paintComponent(Graphics g) {

    }
}
