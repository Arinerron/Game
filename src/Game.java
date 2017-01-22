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

    public boolean slideover = true;
    public boolean eightbit = false;
    public int rate = 5; // tick is every 5 milis
    public int tick = 0; // current tick

    public int width = 20 * 12; // 16
    public int height = 20 * 6; // 9
    public int real_width = 0;
    public int real_height = 0;

    public double togo = 1;
	public int wid = 1;
	public int hei = 1;
	public int add = 1;
	public int addy = 1;

    public BufferedImage image = null;
    public java.util.List<Tile> tiles = new java.util.ArrayList<>();
    public BufferedImage tile_null = null;
    public int character_id = 1;
    public BufferedImage character_current = null;
    public BufferedImage character_spritesheet = null;
    public BufferedImage character_left = null;
    public BufferedImage character_right = null;
    public BufferedImage character_up = null;
    public BufferedImage character_down = null;

    public boolean clicked = false;
    public boolean rightclicked = false;
    public boolean space = false;
    public boolean w = false;
    public boolean s = false;
    public boolean d = false;
    public boolean a = false;

    public int mousex = 0;
    public int mousey = 0;
    public int firstmousex = 0;
    public int firstmousey = 0;
    public double mousedx = 0;
    public double mousedy = 0;

    public Character[][] map = new Character[100][50];
    public double x = 0;
    public double y = 0;
    public int jumptick = 0;
    public double jumpboost = 0;
    public boolean jumping = false;
    public boolean jumpingup = false;
    public double spawnx = 0;
    public double spawny = 0;
    public boolean visible = true;

    public double jumpheight = 0; // change to 20 to enable jumping, and 0 to disable.
    public double speed = 0.4;
    public double acceleration = 0.0125;
    public double xacceleration = 0;
    public double yacceleration = 0;

    // initialization
    public Game() {
        try {
            this.tile_null = getImage("tiles/null");

            this.character_spritesheet = getImage("character_spritesheet");

            loadTileset("tileset.txt");
            loadMap("map.txt");
        } catch(Exception e) {
            System.err.println("Failed to load resources. Exiting...");
            e.printStackTrace();
            System.exit(1);
        }

        this.frame = new JFrame("Game");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
                else if(e.getKeyCode() == KeyEvent.VK_R) {
                    x = spawnx;
                    y = spawny;
                    xacceleration = 0;
                    yacceleration = 0;
                    character_current = character_down;
                } else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    space = true;
                } else if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    speed = 0.1;
                }
            }

            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_W) { // todo make switch/case
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
                } else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    space = false;
                } else if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    speed = 0.4;
                }  else if(e.getKeyCode() == KeyEvent.VK_B) {
                    eightbit = !eightbit;
                } else {
                    try {
                        int i = Integer.parseInt("" + e.getKeyChar());
                        width = 2 * 20 * i + 1;
                        height = 20 * i + 1;
                        System.out.println("Aspect ratio set to " + width + "x" + height);
                        recalculate();
                    } catch(Exception ex) {
                        // do nothing
                    }
                }
            }

            public void keyTyped(KeyEvent e) {}
        };

        FocusListener focus = new FocusListener() {
            public void focusGained(FocusEvent fe) {
                if(!running) { // run once-- when game starts
                    recalculate();
                    running = true;
                }
            }

            public void focusLost(FocusEvent fe) {}
        };

        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e))
                    clicked = true;
                else if(SwingUtilities.isRightMouseButton(e)) {
                    rightclicked = true;
                    mousex = e.getX();
                    mousey = e.getY();
                    firstmousex = e.getX();
                    firstmousey = e.getY();
                    mousedx = firstmousex - mousex;
                    mousedy = firstmousey - mousey;
                    speed = 0.5;
                }
            }

            public void mouseReleased(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e))
                    clicked = false;
                else if(SwingUtilities.isRightMouseButton(e)) {
                    rightclicked = false;
                    speed = 0.4;
                    mousedx = 0;
                    mousedy = 0;
                }
            }

            public void mouseClicked(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });

        this.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {
                mousex = e.getX();
                mousey = e.getY();
            }

            public void mouseDragged(MouseEvent e) {
                if(rightclicked) {
                    mousex = e.getX();
                    mousey = e.getY();

                    if(slideover) {
                        mousedx = -mousex + firstmousex;
                        mousedy = -mousey + firstmousey;
                    }
                }
            }
        });

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

                    if(space && !jumping) {
                        jumping = true;
                        jumpingup = true;
                        jumpboost = 0;
                        jumptick = tick;
                    }

                    if(jumping) {
                        double jspeed = 0.3;

                        if(jumpingup) {
                            if(jumpboost > -jumpheight)
                                jumpboost -= jspeed;
                            else
                                jumpingup = false;
                        } else {
                            if(jumpboost < -0.1)
                                jumpboost += jspeed;
                            else {
                                jumping = false;
                                jumpboost = 0;
                            }
                        }
                    }

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
                    System.out.println("x=" + (int)x + " y=" + (int)y + " 20x=" + (int)((x/20) - (width / 40)) + " 20y=" + (int)((y/20) - (height / 40)));
                    /*if(getTile((int)(x / 20), (int)(y / 20)) == '.')
                        speed = 0.05;
                    else
                        speed = 0.1;*/
                }
            }
        }, rate, rate);
    }

    // generates the variables for frame size
    public void recalculate() {
        Dimension size = frame.getContentPane().getSize();
        real_width = (int)size.getWidth();
        real_height = (int)size.getHeight();
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
                Tile t = getTile(c);
                if(t != null) {
                    if(t.spawn) { // still debugging spawn
                        System.out.println(this.x + " = " + (-(int)(x) - width) + "; // x=" + x + " && width=" + width);
                        this.x = -(int)(x) - width;
                        this.y = -(int)(i) - height;
                        this.spawnx = -(int)x - width;
                        this.spawny = -(int)i - height;

                        System.out.println("Spawn set to " + this.x + "," + this.y + ". Notice " + x + "," + this.x + "," + spawnx);
                    }

                    if(t.replace != null)
                        c = t.replace;
                }

                map[x][i] = c;
            }
        }

        this.map = map;
    }

    // loads a tileset into memory
    public void loadTileset(String name) throws Exception {
        String[] lines = getString(name).split(Pattern.quote("\n"));
        java.util.List<Tile> tiles = new java.util.ArrayList<>();

        for(String line : lines) {
            if(!line.startsWith("#") && line.length() != 0) {
                String[] split = line.split(Pattern.quote(" "));
                Tile tile = new Tile();

                for(int i = 0; i < split.length; i++) {
                    switch(i) {
                        case 0:
                            tile.character = split[i].charAt(0);
                            break;
                        case 1:
                            if(split[i].equalsIgnoreCase("null"))
                                tile.image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
                            else
                                tile.image = getImage(split[i]);
                            break;
                        default:
                            String[] pair = split[i].split(Pattern.quote("="));
                            String key = pair[0];

                            switch(key.toLowerCase()) {
                                case "fluid":
                                    tile.solid = false;
                                    break;
                                case "solid":
                                    tile.solid = true;
                                    break;
                                case "dangerous":
                                    tile.dangerous = true;
                                    break;
                                case "safe":
                                    tile.dangerous = false;
                                    break;
                                case "replace":
                                    tile.replace = pair[1].charAt(0);
                                    break;
                                case "spawn":
                                    tile.spawn = true;
                                    break;
                                default:
                                    System.out.println("Unknown parameter \"" + split[i] + "\" for tile \"" + split[0] + "\".");
                                    break;
                            }
                            break;
                    }
                }

                tiles.add(tile);
            }
        }

        this.tiles = tiles;
    }

    // next tick
    public void tick() {
        this.tick++;
    }

    // update the image variable so it knows what to draw
    public void updateImage() {
        BufferedImage image = null;
        if(eightbit)
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED);
        else
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();

        int mx = (int)(this.mousedx / 3) / 3; // I don't know why it need two /3's. TODO: debug.
        int my = (int)(this.mousedy / 3) / 3;

        for(int x = 0; x < map.length; x++) {
            for(int y = 0; y < map[x].length; y++) {
                if(willRender(x, y)) {
                    char val = getTile(x, y);
                    Tile t = getTile(val);
                    BufferedImage img = null;
                    if(t == null)
                        img = tile_null;
                    else
                        img = t.image;
                    g.drawImage(img, (x * 20) + (int)(this.x) + mx, (y * 20) + (int)(this.y) + my, null);
                }
            }
        }

        if(this.visible) {
            boolean yeah = w || a || s || d;
            if(yeah)
                this.character_id = this.charId(s, d, w, a);

            BufferedImage img = this.getCharacter(character_id, tick, !jumping && yeah);
            int sizeadjustment = (int)(jumpboost / jumpheight * 1); // change the 1 to change the size when jumping
            g.drawImage(img, (width / 2) + (sizeadjustment / 2) - 10 + mx,
                (height / 2) + (sizeadjustment / 2) - 10 + my + (int)jumpboost,
                img.getWidth() - sizeadjustment, img.getHeight() - sizeadjustment, null);
        }

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

    // returns the Tile object for a char
    public Tile getTile(char c) {
        for(Tile tile : this.tiles)
            if(tile.character == c)
                return tile;
        return null;
    }

    // sets a tile at an x and y to a value
    public void setTile(int x, int y, char value) {
        this.map[x][y] = value;
    }

    // gets a BufferedImage of the character from a spritesheet
    public BufferedImage getCharacter(int direction, int tick, boolean walking) {
        int i = (int)(tick / ((rate / speed) * 1.6)) % 4;
        return character_spritesheet.getSubimage(walking ? (i == 0 || i == 2 ? 0 : (i == 1 ? 1 : 2)) * 16 : 0, direction * 20, 16, 20);
    }

    // gets a BufferedImage of the character by booleans
    public int charId(boolean down, boolean right, boolean up, boolean left) {
        if(right != left) {
            return right ? 1 : 3;
        } else if(up != down) {
            return up ? 2 : 0;
        } else {
            return 0;
        }
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
