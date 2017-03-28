package game;

import javax.swing.*;
import java.util.*;
import java.util.zip.*;
import java.util.regex.*;
import java.util.concurrent.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import javax.imageio.*;
import tileeditor.*;

public class Game extends JPanel {
    public boolean running = false;
    public boolean threadlocked = false;
    public JFrame frame = null;

    public boolean slideover = true;
    public boolean eightbit = false;
    public int rate = 5; // tick is every 5 milis
    public int tick = 0; // current tick
    /*
     * interesting fact: it would take 4 months and 2 weeks for the tick to
     * make the tick large enough to crash the game cause it is over the max int
     */

    public static final int tilesize = 16; // set this to the tilesize
    public int width = tilesize * 12; // 16
    public int height = tilesize * 6; // 9
    final int characterwidth = 16, characterheight = 16; // size of character

    public final int half = (int)(tilesize / 2);
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
    public boolean middleclicked = false;
    public boolean space = false;
    public boolean shift = false;
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
    public boolean jumpingenabled = false; //enable or disable jumping
    public int jumptick = 0;
    public double jumpboost = 0;
    public boolean jumping = false;
    public boolean jumpingup = false;
    public double spawnx = 0;
    public double spawny = 0;
    public Tile tile = null; // current tile
    public char defaultchar = '?';
    public boolean visible = true;

    public double jumpheight = tilesize;
    public boolean moving = false;
    public double speed = 0.4;
    public int filter = 0;
    public double acceleration = 0.0125;
    public double xacceleration = 0;
    public double yacceleration = 0;
    public Color background = Color.BLACK;
    public Random random = new Random();

    public Queue<Particle> particles = new ConcurrentLinkedQueue<Particle>();

    public static void main(String[] args) {
        if(args.length != 0 && (args[0].equalsIgnoreCase("--tileeditor") || args[0].equalsIgnoreCase("-e")))
            tileeditor.Main.main(args);
        else
            new Game(args);
    }

    // initialization with arguments
    public Game(String[] args) {
        this();
        for(String arg : args) {
            String[] split = arg.split(Pattern.quote("=")); // TODO: just split on the first =
            try {
                switch(split[0].toLowerCase()) { // TODO: Error handling
                    case "-j":
                    case "--jump":
                        this.jumpingenabled = true;
                        break;
                    case "-d";
                    case "--dither":
                        eightbit = true;
                        break;
                    case "-p":
                    case "--nopan":
                        slideover = false;
                        break;
                    default:
                        System.out.println("Unknown parameter \"" + split[0] + "\" for game arguments.");
                        break;
                }
            } catch(Exception e) {
                System.err.println("Failed to parse parameters for game arguments.");
                System.exit(1);
            }
        }
    }

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
        this.frame.setBackground(Color.BLACK);
        this.setBackground(Color.BLACK);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
                    respawn();
                } else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    space = true;
                } else if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shift = true;
                }
            }

            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_W) { // todo make switch/case
                    w = false;
                    if(tile != null && !tile.slippery && !jumping)
                        yacceleration = 0;
                } else if(e.getKeyCode() == KeyEvent.VK_S) {
                    s = false;
                    if(tile != null && !tile.slippery && !jumping)
                        yacceleration = 0;
                } else if(e.getKeyCode() == KeyEvent.VK_D) {
                    d = false;
                    if(tile != null && !tile.slippery && !jumping)
                        xacceleration = 0;
                } else if(e.getKeyCode() == KeyEvent.VK_A) {
                    a = false;
                    if(tile != null && !(tile.slippery && !jumping))
                        xacceleration = 0;
                } else if(e.getKeyCode() == KeyEvent.VK_SPACE)
                    space = false;
                else if(e.getKeyCode() == KeyEvent.VK_SHIFT)
                    shift = false;
                else if(e.getKeyCode() == KeyEvent.VK_B)
                    eightbit = !eightbit;
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
                } else if(SwingUtilities.isMiddleMouseButton(e)) {
                    if(slideover) {
                        middleclicked = true;
                        mousex = e.getX();
                        mousey = e.getY();
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e))
                    clicked = false;
                else if(SwingUtilities.isRightMouseButton(e) || !slideover) {
                    rightclicked = false;
                    mousedx = 0;
                    mousedy = 0;
                } else if(SwingUtilities.isMiddleMouseButton(e)) {
                    middleclicked = false;
                    xacceleration = 0;
                    yacceleration = 0;
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
                if(clicked || ((rightclicked || middleclicked) && slideover)) {
                    mousex = e.getX();
                    mousey = e.getY();

                    if(slideover && rightclicked && !middleclicked) {
                        mousedx = -mousex + firstmousex;
                        mousedy = -mousey + firstmousey;
                    }
                }
            }
        });

        this.frame.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                recalculate();
            }

            public void componentHidden(ComponentEvent e) {}
            public void componentShown(ComponentEvent e) {}
            public void componentMoved(ComponentEvent e) {}
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

                    for(Particle particle : particles)
                        particle.tick();

                    if(!threadlocked) {
                            boolean tilenull = tile == null;

                            threadlocked = true;
                            if(jumpingenabled) {
                            if(space && !jumping && (tilenull || tile.jump)) {
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
                        }

                        if(w || s || a || d || (middleclicked && slideover) || jumping) {
                            moving = true;
                            if(middleclicked && slideover && (tilenull || tile.slideover)) {
                                //System.out.println((middleclicked && slideover && (tilenull || tile.slideover)));
                                //System.out.println("..." + (tilenull || tile.slideover));
                                if(mousex != 0 && mousey != 0) {
                                    double mousex2 = (real_width / 2) - mousex;
                                    double mousey2 = (real_height / 2) - mousey;

                                    xacceleration = mousex2 / (real_width / 2);
                                    yacceleration = mousey2 / (real_height / 2);

                                    boolean down = false, right = false, up = false, left = false; // todo improve to only two booleans

                                    if(Math.abs(mousex2) > Math.abs(mousey2)) {
                                        if(mousex2 > 0)
                                            left = true;
                                        else
                                            right = true;
                                    } else {
                                        if(mousey2 > 0)
                                            up = true;
                                        else
                                            down = true;
                                    }

                                    Game.this.character_id = charId(down, right, up, left);
                                }
                            } else
                                if(!jumping)
                                    Game.this.character_id = charId(s, d, w, a);

                            if(!(tilenull || tile.slideover))
                                middleclicked = false;

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

                            if(jumping && !(w || a || s || d)) {
                                xacceleration = xacceleration * 0.92;
                                yacceleration = yacceleration * 0.92;
                            }

                            if(xacceleration > speed)
                                xacceleration = speed;
                            else if(xacceleration < -speed)
                                xacceleration = -speed;
                            if(yacceleration > speed)
                                yacceleration = speed;
                            else if(yacceleration < -speed)
                                yacceleration = -speed;
                        } else
                            moving = false;

                        calculateSpeed();

                        if(!tilenull) {
                            acceleration = tile.acceleration;

                            if(tile.dangerous && !jumping) {
                                kill();
                            } else  {
                                if(tile.filterset)
                                    filter = tile.filter;
                                if(tile.checkpoint)
                                    setSpawn(Game.this.x, Game.this.y, false);

                                // collision detection
                                if((tile.slippery && !jumping) || moving) { // TODO: Optimise by checking if it would collide without actually trying it
                                    double oldx = x, oldy = y;
                                    double spacing = (double)(tilesize - (double)(characterwidth / 5));

                                    // System.out.println(spacing + " " + tilesize + " " + characterwidth);spacing = 8;

                                    x += xacceleration;
                                    Tile current1 = getTile(getTile((int)((x + spacing) / tilesize),(int)(y / tilesize)));
                                    Tile current2 = getTile(getTile((int)((x + spacing) / tilesize),(int)(y / tilesize)));

                                    if(current1.solid || current2.solid)
                                        x = oldx;

                                    y += yacceleration;
                                    current1 = getTile(getTile((int)((x + spacing) / tilesize),(int)(y / tilesize)));
                                    current2 = getTile(getTile((int)((x + spacing) / tilesize),(int)(y / tilesize)));

                                    if(current1.solid || current2.solid)
                                        y = oldy;
                                }

                                Game.this.slideover = tile.slideover;
                            }

                            if(tile.particle && tick % tile.particle_iteration == 0) {
                                java.util.List<Particle> particles2 = Particle.randomlySpread(Game.this, x + half, y + half, tile.particle_color, tile.particle_lifetime, tile.particle_count);

                                for(Particle particle : particles2) {
                                    particle.xacceleration =  tile.particle_xacceleration;
                                    particle.yacceleration = tile.particle_yacceleration;
                                    particle.front = tile.particle_front;

                                    particles.add(particle);
                                }
                            }
                        } else if(tick > 10) {
                            System.out.println("null!\nnull!\nnull!\nnull!\nnull!\nnull!\nnull!\nA severe error occured.");
                            System.exit(1);
                        }
                        updateImage();
                        threadlocked = false;
                    }
                }
            }
        }, rate, rate);
    }

    // sets a spawn point and logs it
    public void setSpawn(double x, double y, boolean abs) {
        boolean print = (int)x != (int)this.spawnx && (int)y != (int)this.spawny;

        this.spawnx = x;
        this.spawny = y;
        if(abs) {
            this.x = x;
            this.y = y;
        }

        if(print)
            System.out.println("Spawn set to [" + (int)this.spawnx + "," + (int)this.spawny + "].");
    }

    // kills the player
    public void kill() {
        respawn();
    }

    // reset and particles around player
    public void respawn() {
        reset(); // Game game, double x, double y, Color color, int lifetime, int number

        java.util.List<Particle> particles = Particle.randomlySpread(this, x + half, y + half, Color.GREEN, 800, 75);

        for(Particle particle : particles) {
            particle.xacceleration =  (Math.random() - 0.5) / 50;
            particle.yacceleration = (Math.random() - 0.5) / 50;
            particle.front = random.nextBoolean();

            this.particles.add(particle);
        }
    }

    // resets player state
    public void reset() {
        this.x = this.spawnx;
        this.y = this.spawny;
        this.xacceleration = 0;
        this.yacceleration = 0;
        this.character_current = this.character_down;
        this.filter = 0;
    }

    // calculate what speed to travel at
    public void calculateSpeed() {
        boolean slippery = (tile != null ? tile.slippery : false); // todo optimize
        Tile tile = getCurrentTile();
        if(tile != null) {
            speed = Math.abs(tile.speed);

            if(!tile.slippery && slippery) {
                xacceleration = 0;
                yacceleration = 0;
            }
        } else
            speed = 0.3;
        if(slideover && rightclicked || middleclicked)
            speed += 0.1;
        else if(shift)
            speed = speed / 2;
    }

    // returns the tile object the player is on
    public Tile getCurrentTile() {
        this.tile = getTile(getTile((int)((x + half) / tilesize),(int)(y / tilesize)));
        return this.tile;
    }

    // gets the X position of a tile from a raw X position
    public int getTileX(double x) {
        return (int)(x / (real_width / width));
    }

    // gets the Y position of a tile from a raw Y position
    public int getTileY(double y) {
        return (int)(y / (real_height / height));
    }

    // gets the X position from a tile x
    public int getRealX(double x) {
        return (int)(x * (real_width / width));
    }

    // gets the Y position from a tile y
    public int getRealY(double y) {
        return (int)(y * (real_height / height));
    }

    // generates the variables for frame size
    public void recalculate() {
        Dimension size = frame.getContentPane().getSize();
        real_width = (int)size.getWidth();
        real_height = (int)size.getHeight();
    }

    // loads a map into memory
    public void loadMap(String name) throws Exception {
        String file = getString(name);

        boolean config = file.startsWith("CONFIG:");
        if(config) {
            String[] cfg = file.substring(0, file.indexOf("\n\n")).split(Pattern.quote("\n"));
            for(String line : cfg) {
                if(!line.startsWith("CONFIG:") && !line.startsWith("#")) {
                    String current = line;
                    String key = current;
                    String val = current;
                    if(current.contains("=")) {
                        int first = current.indexOf("=");
                        key = current.substring(0, first);
                        val = current.substring(first + 1);
                    }

                    switch(key.toLowerCase()) {
                        case "background.color":
                            this.background = Color.decode(val);
                            break;
                        case "spawn.x":
                            this.setSpawn(-Integer.parseInt(val), this.spawny, true);
                            break;
                        case "spawn.y":
                            this.setSpawn(this.spawnx, -Integer.parseInt(val), true);
                            this.y = this.spawny;
                            break;
                        default:
                            System.out.println("Unknown config option \"" + key + "\" for map file \"" + name + "\".");
                            break;
                    }
                }
            }
        }

        String[] lines = (config ? file.split(Pattern.quote("\n\n"))[1] : file).split(Pattern.quote("\n"));
        int width = 0;
        int height = lines.length;
        for(String line : lines)
            if(line.length() > width)
                width = line.length();

        Character[][] map = new Character[width][height];
        for(int i = 0; i < lines.length; i++) {
            if(lines[i].length() != 0) {
                char[] array = lines[i].toCharArray();

                for(int x = 0; x < array.length; x++) {
                    char c = array[x];
                    if(c == ' ')
                        c = defaultchar;
                    Tile t = getTile(c);
                    if(t != null) {
                        if(t.spawn)
                            this.setSpawn(-(int)x - width, -(int)i - height, true);
                        if(t.replace != ' ')
                            c = t.replace;
                    }

                    map[x][i] = c;
                }
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
                    if(split[i].length() != 0) // make sure the parameter isn't empty
                        switch(i) {
                            case 0: // if it is the first parameter on the line
                                tile.character = split[i].charAt(0);
                                break;
                            case 1: // if it is the second
                                if(split[i].equalsIgnoreCase("null"))
                                    tile.image = new BufferedImage(tilesize, tilesize, BufferedImage.TYPE_INT_ARGB);
                                else
                                    tile.image = getImage(split[i]);
                                break;
                            default: // otherwise, it is one of the optional parameters
                                String current = split[i];
                                String key = current;
                                String val = current;
                                if(current.contains("=")) {
                                    int first = current.indexOf("=");
                                    key = current.substring(0, first);
                                    val = current.substring(first + 1);
                                }

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
                                    case "slippery":
                                        tile.slippery = true;
                                        break;
                                    case "sticky":
                                        tile.slippery = false;
                                        break;
                                    case "filter":
                                        tile.filter = (int)((Double.parseDouble(val)) * 2.54);
                                        tile.filterset = true;
                                        break;
                                    case "safe":
                                        tile.dangerous = false;
                                        break;
                                    case "replace":
                                        tile.replace = val.charAt(0);
                                        break;
                                    case "speed":
                                        tile.speed = Double.parseDouble(val);
                                        break;
                                    case "acceleration":
                                        tile.acceleration = Double.parseDouble(val);
                                        break;
                                    case "spawn":
                                        tile.spawn = true;
                                        break;
                                    case "checkpoint":
                                        tile.checkpoint = true;
                                        break;
                                    case "nojump":
                                        tile.jump = false;
                                        break;
                                    case "jump":
                                        tile.jump = true;
                                        break;
                                    case "dither":
                                        tile.dither = true;
                                        break;
                                    case "lock":
                                        tile.slideover = false;
                                        break;
                                    case "unlock":
                                        tile.slideover = true;
                                        break;
                                    case "particle": {
                                            try {
                                                tile.particle = true;

                                                if(val.contains("=")) {
                                                    System.out.println("Warning: Particle string for tile \"" + tile.character + "\" appears to be using character \"=\" rather than \":\". Attempting to parse...");
                                                    val = val.replaceAll(Pattern.quote("="), ":");
                                                }

                                                String[] pairs = val.substring(val.indexOf('{') + 1, val.indexOf('}')).split(Pattern.quote(","));
                                                for(String pair : pairs) {
                                                    String[] parameter = pair.split(Pattern.quote(":"));
                                                    String pkey = parameter[0].toLowerCase();
                                                    String pval = parameter[1].toLowerCase();

                                                    pval = replaceBrackets(pval);

                                                    switch(pkey) {
                                                        case "color":
                                                            tile.particle_color = Color.decode(pval);
                                                            break;
                                                        case "count":
                                                            tile.particle_count = Integer.parseInt(pval);
                                                            break;
                                                        case "iteration":
                                                            tile.particle_iteration = Integer.parseInt(pval);
                                                            break;
                                                        case "lifetime":
                                                            if(pval.endsWith("s"))
                                                                tile.particle_lifetime = secondsToTicks(Double.parseDouble(pval.substring(0, pval.length() - 2)));
                                                            else
                                                                tile.particle_lifetime = Integer.parseInt(pval);
                                                            break;
                                                        case "front":
                                                            tile.particle_front = Boolean.parseBoolean(pval);
                                                            break;
                                                        case "xacceleration":
                                                            tile.particle_xacceleration = Double.parseDouble(pval);
                                                            break;
                                                        case "yacceleration":
                                                            tile.particle_yacceleration = Double.parseDouble(pval);
                                                            break;
                                                        default:
                                                            System.out.println("Unknown parameter \"" + pkey + "\" for particle string for tile \"" + tile.character + "\"");
                                                            break;
                                                    }
                                                }
                                            } catch(Exception e) {
                                                System.err.println("Error: Failed to parse particle string for tile \"" + tile.character + "\".");
                                                System.err.println(e.toString());
                                                e.printStackTrace();
                                            }
                                        }
                                        break;
                                    case "default":
                                        tile.defaultchar = true;
                                        defaultchar = tile.character;
                                        tile_null = tile.image;
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

    // convert a string like "[0.01-0.4]" to a random double between the range
    public String replaceBrackets(String original) {
        if(original.contains("[")) {
            StringBuilder b = new StringBuilder();

            int open = original.indexOf("[");
            int close = original.indexOf("]");

            b.append(original.substring(0, open));
            String[] parse = original.substring(open + 1, close).split(Pattern.quote("|"));
            b.append(random(Double.parseDouble(parse[0]), Double.parseDouble(parse[1])));
            b.append(original.substring(close + 1));

            return b.toString();
        }

        return original;
    }

    // generate a random double between a range
    public double random(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    // generate a random boolean
    public boolean random() {
        return random.nextBoolean();
    }

    // next tick
    public void tick() {
        this.tick++;
    }

    // update the image variable so it knows what to draw
    public void updateImage() {
        BufferedImage image = null;
        if(eightbit || (this.tile != null && this.tile.dither))
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED);
        else
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();

        if(this.background != Color.BLACK && this.background != null) {
            g.setColor(this.background);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
        }

        int mx = (int)(this.mousedx / 3) / 3; // I don't know why it need two /3's. TODO: debug.
        int my = (int)(this.mousedy / 3) / 3;

        int tilex = (int)(getTileX(real_width) / 2);
        int tiley = (int)(getTileY(real_height) / 2);

        // do NOT change these
        final double x = this.x;
        final double y = this.y;

        for(int x2 = 0; x2 < map.length; x2++) {
            for(int y2 = 0; y2 < map[x2].length; y2++) { // TODO: Optimize
                char val = getTile(x2, y2);
                Tile t = getTile(val);
                BufferedImage img = null;
                if(t == null)
                    img = getTile(defaultchar).image; // tile_null doesn't seem to be working
                else
                    img = t.image;
                g.drawImage(img, (x2 * tilesize) + tilex + (int)(x) + mx, (y2 * tilesize) + tiley + (int)(y) + my, null);
            }
        }

        // draw particles that are in back
        for(Particle particle : particles)
            if(!particle.front) {
                g.setColor(particle.color);
                int px = (int)(particle.x + x + mx + tilex), py = (int)(particle.y + y + my + tiley);
                g.drawLine(px, py, px, py);
            }

        // draw the player
        if(this.visible) {
            BufferedImage img = this.getCharacter(character_id, tick, !jumping && moving);
            g.drawImage(img, tilex - tilesize + mx,
                tiley - tilesize + my + (int)jumpboost,
                img.getWidth(), img.getHeight(), null);
        }

        // draw particles that are in front
        for(Particle particle : particles)
            if(particle.front) {
                g.setColor(particle.color);
                int px = (int)(particle.x + x + mx + tilex), py = (int)(particle.y + y + my + tiley);
                g.drawLine(px, py, px, py);
            }

        // draw the filter on top of everything
        if(filter != 0) {
            g.setColor(new Color(0, 0, 0, filter));
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
        }

        // set the new image
        g.dispose();
        this.setImage(image);
    }

    // Generate image and generate position vars
    public void setImage(BufferedImage image) {
        // Sorry if this part isn't readable. <excuse>It's a little too complicated to document or come up with good var names.</excuse>
        if(this.image != image) {
            if((double)((double)real_height / (double)image.getHeight()) < (double)((double)real_width / (double)image.getWidth())) {
                // if screen width > height
        		togo = (double)((double)real_height / (double)image.getHeight());
        		wid = (int)(image.getWidth() * togo);
        		hei = real_height;
        		add = (real_width / 2) - (wid / 2);
        		addy = 0;
    		} else {
                // if screen width < height
                togo = (double)((double)real_width / (double)image.getWidth());
                wid = real_width;
                hei = (int)(image.getHeight() * togo);
                addy = (real_height / 2) - (hei / 2);
                add = 0;
            }

            this.image = image;
            this.repaint();
        }
    }

    // draw whatever is in the image variable
    public void paintComponent(Graphics g) {
        /**
         * Sorry, I know this is confusing.
         * I really didn't document this well.
         * If you have any questions, feel
         * free to contact me at me@arinerron.com
         */
        if(this.image != null) {
            super.paintComponent(g);
            g.drawImage(this.image, add, addy, wid, hei, null);
        }
    }

    // returns the value of a tile at x and y
    public char getTile(int x, int y) {
        x = Math.abs(x);
        y = Math.abs(y);
        if(!(x < 0 || y < 0 || x > this.map.length - 1 || y > this.map[0].length - 1)) {
            Character val = this.map[x][y];
            return (val != null ? val : defaultchar);
        }

        return defaultchar;
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
        int i = (int)(tick / ((rate / (speed)) * 1.6)) % 4;
        return character_spritesheet.getSubimage(walking ? (i == 0 || i == 2 ? 0 : (i == 1 ? 1 : 2)) * characterwidth : 0, direction * characterheight, characterwidth, characterheight);
    }

    // gets a BufferedImage of the character by booleans
    public int charId(boolean down, boolean right, boolean up, boolean left) {
        if(right != left)
            return right ? 1 : 3;
        else if(up != down)
            return up ? 2 : 0;
        else
            return 0;
    }

    // loads a bufferedimage in by filename
    public BufferedImage getImage(String name) throws Exception {
        byte[] imagedata = decompress(Files.readAllBytes(new File("../res/" + name + ".mci").toPath()));
        int xmax = imagedata[0];
        int ymax = (imagedata.length - 1) / xmax;
        BufferedImage b = new BufferedImage(xmax, ymax, BufferedImage.TYPE_INT_ARGB);
        for(int i = 0; i < xmax; i++)
            for(int j = 0; j < ymax; j++)
                b.setRGB(i, j, Colors.colors[imagedata[i * ymax + j + 1]]);
        return b;
    }

    // decompresses the data into a byte array
    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();

        return output;
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

    // convert seconds to ticks
    public int secondsToTicks(double seconds) { // TODO: move to Game class
        return (int)((double)((double)(seconds * 1000) / rate));
    }
}

class Tile {
    public char character = '?';
    public BufferedImage image = null;
    public boolean solid = false;
    public boolean dangerous = false;
    public boolean slippery = false;
    public boolean spawn = false;
    public char replace = ' ';
    public double speed = 0.4;
    public int filter = 0;
    public boolean filterset = false;
    public boolean jump = true;
    public boolean defaultchar = false;
    public boolean checkpoint = false;
    public double acceleration = 0.0125;
    public boolean dither = false;
    public boolean slideover = true;

    public boolean particle = false;
    public Color particle_color = Color.BLUE;
    public int particle_count = 1;
    public int particle_iteration = 1;
    public int particle_lifetime = 1000;
    public double particle_xacceleration = 0.01;
    public double particle_yacceleration = 0.01;
    public boolean particle_front = true;
}

class Particle {
    public Game game = null;
    public double originx = 0;
    public double originy = 0;
    public double x = 0;
    public double y = 0;
    public double xacceleration = 0;
    public double yacceleration = 0;
    public Color color = Color.BLUE;
    public int lifetime = 1000;
    public int life = 0;
    public boolean front = true;

    // initialize particle
    public Particle(Game game, double x, double y) {
        this.game = game;
        this.x = Math.abs(x);
        this.y = Math.abs(y);
        this.originx = this.x;
        this.originy = this.y;
    }

    // next tick
    public void tick() {
        if(life >= lifetime)
            this.destroy();
        x += xacceleration;
        y += yacceleration;
        life++;
    }

    // destroy particle
    public void destroy() {
        game.particles.remove(this);
    }

    // spread particles around point
    public static java.util.List<Particle> randomlySpread(Game game, double x, double y, Color color, int lifetime, int number) {
        // range is the distance around the given point to scatter
        final double range = 6;

        java.util.List<Particle> particles = new java.util.ArrayList<>();
        Random r = new Random();

        double half = (double)(range / 2);

        double xmin = x - game.half + half, xmax = x + game.half - half,
                ymin = y - game.half + half, ymax = y + game.half - half;

        for(int i = 0; i < number; i++) {
            Particle particle = new Particle(game, xmin + (xmax - xmin) * r.nextDouble(),
                                ymin + (ymax - ymin) * r.nextDouble());

            particle.color = color;
            particle.lifetime = lifetime;
            particles.add(particle);
        }

        return particles;
    }
}

class Colors {
    // all of the colors in the images
    public static final int[] colors = new int[] {
        0xff747474,
        0xff24188c,
        0xff0000a8,
        0xff44009c,
        0xff8c0074,
        0xffa80010,
        0xffa40000,
        0xff7c0800,
        0xff402c00,
        0xff004400,
        0xff005000,
        0xff003c14,
        0xff183c5c,
        0xff000000,
        0xff000000,
        0xff000000,

        0xffbcbcbc,
        0xff0070ec,
        0xff2038ec,
        0xff8000f0,
        0xffbc00bc,
        0xffe40058,
        0xffd82800,
        0xffc84c0c,
        0xffac7c00,
        0xff009400,
        0xff00a800,
        0xff009038,
        0xff008088,
        0xff000000,
        0xff000000,
        0xff000000,

        0xfffcfcfc,
        0xff3cbcfc,
        0xff5c94fc,
        0xffcc88fc,
        0xfff478fc,
        0xfffc74b4,
        0xfffc7460,
        0xfffc9838,
        0xfff0bc3c,
        0xff80d010,
        0xff4cdc48,
        0xff58f898,
        0xff00e8d8,
        0xff787878,
        0xff000000,
        0xff000000,

        0x00ffffff,
        0xffa8e4fc,
        0xffc4d4fc,
        0xffd4c8fc,
        0xfffcc4fc,
        0xfffcc4d8,
        0xfffcbcb0,
        0xfffcd8a8,
        0xfffce4a0,
        0xffe0fca0,
        0xffa8f0bc,
        0xffb0fccc,
        0xff9cfcf0,
        0xffc4c4c4,
        0xff000000,
        0xff000000
    };
}
