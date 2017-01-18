import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;

public class Game extends JPanel {
    public boolean running = false;
    public JFrame frame = null;

    public BufferedImage tile_dirt = null;
    public BufferedImage character_left = null;
    public BufferedImage character_right = null;
    public BufferedImage character_up = null;
    public BufferedImage character_down = null;

    public boolean w = false;
    public boolean s = false;
    public boolean d = false;
    public boolean a = false;

    public int x = 0;
    public int y = 0;
    public boolean visible = true;

    // new game instance
    public Game() {
        this.running = true;

        try {
            this.tile_dirt = getImage("tile_dirt");
        } catch(Exception e) {
            System.err.println("Failed to load resources. Exiting...");
            e.printStackTrace();
            System.exit(1);
        }

        this.frame = new JFrame("Game");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(500, 500);
        this.frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.frame.add(this);
        this.frame.setBackground(Color.BLACK);
        KeyListener listener = new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_W)
                    w = true;
                else if(e.getKeyCode() == KeyEvent.VK_S)
                    s = true;
                else if(e.getKeyCode() == KeyEvent.VK_D)
                    d = true;
                else if(e.getKeyCode() == KeyEvent.VK_A)
                    a = true;
                else if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    System.exit(0);
            }

            public void keyReleased(KeyEvent e) { System.out.println("released"); }

            public void keyTyped(KeyEvent e) { System.out.println("typed"); }
        };
        this.addKeyListener(listener);
        frame.addKeyListener(listener);

        this.frame.setVisible(true);

        this.setFocusable(true);
        this.requestFocus();
    }

    // draw game
    public void paintComponent(Graphics g) {
        g.drawImage(this.tile_dirt, 0, 0, null);
    }

    public BufferedImage getImage(String name) throws Exception {
        BufferedImage in = ImageIO.read(new File("../res/" + name + ".png"));

        BufferedImage newImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = newImage.createGraphics();
        g.drawImage(in, 0, 0, null);
        g.dispose();

        return newImage;
    }
}
