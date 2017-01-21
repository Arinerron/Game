import javax.swing.*;
import java.util.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;

public class Game extends JPanel {
    public boolean running = false;
    public JFrame frame = null;

    public int rate = 5; // tick is every 100 milis
    public int tick = 0; // current tick

    public int width = 20 * 16;
    public int height = 20 * 9;
    public int real_width = 0;
    public int real_height = 0;

    // these vars can safely be ignored.
    public double togo = 1;
	public int wid = 1;
	public int hei = 1;
	public int add = 1;
	public int addy = 1;

    public BufferedImage image = null;
    public BufferedImage tile_dirt = null;
    public BufferedImage tile_water = null;
    public BufferedImage character_current = null;
    public BufferedImage character_left = null;
    public BufferedImage character_right = null;
    public BufferedImage character_up = null;
    public BufferedImage character_down = null;

    public boolean w = false;
    public boolean s = false;
    public boolean d = false;
    public boolean a = false;

    public Character[][] map = new Character[100][50];
    public double x = 0;
    public double y = 0;
    public double speed = 0.5;
    public double acceleration = 0.0125;
    public double xacceleration = 0;
    public double yacceleration = 0;
    public boolean visible = true;

    // initialization
    public Game() {
        try {
            this.tile_dirt = getImage("tile_dirt");
            this.tile_water = getImage("tile_water");
            this.character_left = getImage("character_left");
            this.character_right = getImage("character_right");
            this.character_up = getImage("character_up");
            this.character_down = getImage("character_down");
            this.character_current = this.character_right; // the default stance

            loadMap("map.txt");
        } catch(Exception e) {
            System.err.println("Failed to load resources. Exiting...");
            e.printStackTrace();
            System.exit(1);
        }

        this.frame = new JFrame("Game");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setBackground(Color.BLACK);
        this.setBackground(Color.BLACK);
        this.frame.setExtendedState(this.frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        this.frame.setResizable(true);
        this.frame.getContentPane().add(this);
        this.frame.pack();

        KeyListener listener = new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_W) {
                    w = true;
                    character_current = character_up;
                } else if(e.getKeyCode() == KeyEvent.VK_S) {
                    s = true;
                    character_current = character_down;
                } else if(e.getKeyCode() == KeyEvent.VK_D) {
                    d = true;
                    character_current = character_right;
                } else if(e.getKeyCode() == KeyEvent.VK_A) {
                    a = true;
                    character_current = character_left;
                } else if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    System.exit(0);
            }

            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_W) {
                    w = false;
                    yacceleration = 0;
                } else if(e.getKeyCode() == KeyEvent.VK_S) {
                    s = false;
                    yacceleration = 0;
                } else if(e.getKeyCode() == KeyEvent.VK_D) {
                    d = false;
                    xacceleration = 0;
                } else if(e.getKeyCode() == KeyEvent.VK_A) {
                    a = false;
                    xacceleration = 0;
                }
            }

            public void keyTyped(KeyEvent e) {}
        };

        FocusListener focus = new FocusListener() {
            public void focusGained(FocusEvent fe) {
                if(!running) { // run once-- when game starts
                    Dimension size = frame.getContentPane().getSize();
                    real_width = (int)size.getWidth();
                    real_height = (int)size.getHeight();
                    running = true;
                }
            }

            public void focusLost(FocusEvent fe) {}
        };

        this.frame.addFocusListener(focus);
        this.addFocusListener(focus);
        this.addKeyListener(listener);
        frame.addKeyListener(listener);

        this.frame.setVisible(true);

        this.setFocusable(true);
        this.requestFocus();

        new java.util.Timer().scheduleAtFixedRate(new java.util.TimerTask() { // yup, I had to specify the class
            public void run() {
                if(running) {
                    tick();
                    updateImage();

                    if(w || s || a || d) {
                        if(w) {
                            yacceleration += acceleration;
                        }
                        if(s) {
                            yacceleration -= acceleration;
                        }
                        if(a) {
                            xacceleration += acceleration;
                        }
                        if(d) {
                            xacceleration -= acceleration;
                        }

                        if(xacceleration > speed)
                            xacceleration = speed;
                        else if(xacceleration < -speed)
                            xacceleration = -speed;
                        if(yacceleration > speed)
                            yacceleration = speed;
                        else if(yacceleration < -speed)
                            yacceleration = -speed;

                        x += xacceleration;
                        y += yacceleration;
                    }

                    /*if(getTile((int)(x / 20), (int)(y / 20)) == '.')
                        speed = 0.05;
                    else
                        speed = 0.1;*/
                }
            }
        }, rate, rate);
    }

    // loads a map into memory
    public void loadMap(String name) throws Exception {
        String[] lines = getString(name).split(Pattern.quote("\n"));
        int width = 0;
        int height = lines.length;
        for(String line : lines)
            if(line.length() > width)
                width = line.length();

        Character[][] map = new Character[width][height];
        for(int i = 0; i < lines.length; i++) {
            char[] array = lines[i].toCharArray();

            for(int x = 0; x < array.length; x++) {
                char c = array[x];
                if(c == 'S') {
                    c = '#';
                    this.x = i;
                    this.y = x;
                }

                map[x][i] = c;
            }
        }

        this.map = map;
    }

    // next tick
    public void tick() {
        this.tick++;
    }

    // update the image variable so it knows what to draw
    public void updateImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();

        setTile(2, 2, '#');setTile(2, 3, '#');setTile(2, 4, '#');setTile(2, 4, '#');

        for(int x = 0; x < map.length; x++) {
            for(int y = 0; y < map[x].length; y++) {
                if(willRender(x, y)) {
                    char val = getTile(x, y);
                    BufferedImage img = null;
                    switch(val) {
                        case '#':
                            img = tile_dirt;
                            break;
                        case '.':
                            img = tile_water;
                            break;
                    }
                    if(img != null)
                        g.drawImage(img, (x * 20) + (int)this.x, (y * 20) + (int)this.y, null);
                }
            }
        }

        g.drawImage(this.character_current, (width / 2) - 10, (height / 2) - 10, null);

        g.dispose();
        this.setImage(image);
    }

    public boolean willRender(int x, int y) {
        //if(this.x + this.width < x || this.y + this.height < y || this.x > x + 20 || this.y > y + 20)
        //    return false;
        return true;
    }

    // Generate image and generate position vars
    public void setImage(BufferedImage image) {
        // Sorry if this part isn't readable. <excuse>It's a little too complicated to document or come up with good var names.</excuse>

		if((double)((double)real_height / (double)image.getHeight()) < (double)((double)real_width / (double)image.getWidth())) {
    		togo = (double)((double)real_height / (double)image.getHeight());
    		wid = (int)(image.getWidth() * togo);
    		hei = real_height;
    		add = (real_width / 2) - (wid / 2);
    		addy = 0;
		} else {
            togo = (double)((double)real_width / (double)image.getWidth());
            wid = real_width;
            hei = (int)(image.getHeight() * togo);
            addy = (real_height / 2) - (hei / 2);
            add = 0;
        }

		this.image = image;
    }

    // draw whatever is in the image variable
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        /**
         * Sorry, I know this is confusing.
         * I really didn't document this well.
         * If you have any questions, feel
         * free to contact me at me@arinerron.com
         */
        if(this.image != null)
            g.drawImage(this.image, add, addy, wid, hei, null);

        try { Thread.sleep(5); /* give the gpu a short break */ } catch(Exception e) { e.printStackTrace(); }
        this.repaint();
    }

    // returns the value of a tile at x and y
    public char getTile(int x, int y) {
        Character val = this.map[x][y];
        return (val != null ? val : '?');
    }

    // sets a tile at an x and y to a value
    public void setTile(int x, int y, char value) {
        this.map[x][y] = value;
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

    // loads a string in by filename
    private String getString(String name) throws Exception {
        File file = new File("../res/" + name);
        StringBuilder fileContents = new StringBuilder((int)file.length());
        Scanner scanner = new Scanner(file);

        try {
            while(scanner.hasNextLine())
                fileContents.append(scanner.nextLine() + "\n");
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }
}
