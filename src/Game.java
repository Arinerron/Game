import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;

public class Game extends JPanel {
    public boolean running = false;
    public JFrame frame = null;

    public int width = 20 * 8;
    public int height = 20 * 6;
    public int real_width = 0;
    public int real_height = 0;

    public BufferedImage image = null;
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

    // initialization
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
        //this.frame.setPreferredSize(500, 500);
        this.frame.setExtendedState(this.frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        this.frame.setResizable(false);
        this.frame.add(this);
        this.frame.setBackground(Color.BLACK);
        this.frame.pack();

        Dimension size = this.frame.getContentPane().getSize();
        this.real_width = (int)size.getWidth();
        this.real_height = (int)size.getHeight();

        System.out.println("width=" + this.real_width + " && height=" + this.real_height);
        System.out.println("framew=" + frame.getWidth() + " && frameh=" + frame.getHeight() + " && panelw=" + this.getWidth() + " && panelh=" + this.getHeight());

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

            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_W)
                    w = false;
                else if(e.getKeyCode() == KeyEvent.VK_S)
                    s = false;
                else if(e.getKeyCode() == KeyEvent.VK_D)
                    d = false;
                else if(e.getKeyCode() == KeyEvent.VK_A)
                    a = false;
            }

            public void keyTyped(KeyEvent e) {}
        };

        this.addKeyListener(listener);
        frame.addKeyListener(listener);

        this.frame.setVisible(true);

        this.setFocusable(true);
        this.requestFocus();
    }

    // update the image variable so it knows what to draw
    public void updateImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    // draw whatever is in the image variable
    public void paintComponent(Graphics g) {
        if(this.image != null)
            g.drawImage(this.image, 0, 0, null);
    }

    // loads a bufferedimage in by filename
    public BufferedImage getImage(String name) throws Exception {
        BufferedImage in = ImageIO.read(new File("../res/" + name + ".png"));

        BufferedImage newImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = newImage.createGraphics();
        g.drawImage(in, 0, 0, null);
        g.dispose();

        return newImage;
    }
}
