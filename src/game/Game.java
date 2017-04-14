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
import java.text.*;
import javax.imageio.*;
import javax.sound.sampled.*;
import tileeditor.*;

public class Game extends JPanel {
    public boolean running = false;
    public boolean threadlocked = false;
    public JFrame frame = null;

    public boolean slideover = true;
    public boolean eightbit = false;
    public boolean stats = false;
    public int frames = 0;
    public int fps = 0;
    public int frames2 = 0;
    public int fps2 = 0;
    public int particlecount = 0;
    public int rate = 5; // tick is every 5 milis
    public int tick = 0; // current tick
    /*
     * interesting fact: it would take 4 months and 2 weeks for the tick to
     * make the tick large enough to crash the game cause it is over the max int
     */

    public static final int tilesize = 16; // set this to the tilesize
    public int width = tilesize * 10; // 12
    public int height = tilesize * 5; // 9 or 6? idk
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
    public boolean up = false;
    public boolean down = false;
    public boolean right = false;
    public boolean left = false;

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

    public int screenshot = -1;
    public double jumpheight = tilesize;
    public boolean moving = false;
    public double speed = 0.4;
    public int filter = 0;
    public double acceleration = 0.0125;
    public double xacceleration = 0;
    public double yacceleration = 0;
    public Color background = Color.BLACK;
    public Random random = new Random();

    public Queue<Particle> particles = new ConcurrentLinkedQueue<>();
    public Queue<Entity> entities = new ConcurrentLinkedQueue<>();
    public Queue<Event> events = new ConcurrentLinkedQueue<>();

    public BufferedImage particles_back = null; // optimize by rendering particles in a separate thread
    public BufferedImage particles_front = null;
    public String commands = "";
    public SoundPlayer mainplayer = null;

    public static void main(String[] args) {
        if(args.length != 0 && (args[0].equalsIgnoreCase("--tileeditor") || args[0].equalsIgnoreCase("-e")))
            tileeditor.Main.main(args); // they want the tileeditor
        else
            new Game(args); // start the game
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
                    case "-d":
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
        // init tileset and map
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

        // setup jframe
        this.frame = new JFrame("Game");
        this.frame.setBackground(Color.BLACK);
        this.setBackground(Color.BLACK);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.frame.setExtendedState(this.frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        this.frame.setResizable(true);
        this.frame.getContentPane().add(this);
        this.frame.pack();

        // event listeners
        KeyListener listener = new KeyListener() {
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        w = true;
                    break;
                    case KeyEvent.VK_UP:
                        up = true;
                    break;
                    case KeyEvent.VK_S:
                        s = true;
                    break;
                    case KeyEvent.VK_DOWN:
                        down = true;
                    break;
                    case KeyEvent.VK_D:
                        d = true;
                    break;
                    case KeyEvent.VK_RIGHT:
                        right = true;
                    break;
                    case KeyEvent.VK_A:
                        a = true;
                    break;
                    case KeyEvent.VK_LEFT:
                        left = true;
                    break;
                    case KeyEvent.VK_ESCAPE:
                        System.exit(0);
                    break;
                    case KeyEvent.VK_SPACE:
                        space = true;
                    break;
                    case KeyEvent.VK_SHIFT:
                        shift = true;
                    break;
                }
            }

            public void keyReleased(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        w = false;
                    break;
                    case KeyEvent.VK_UP:
                        up = false;
                    break;
                    case KeyEvent.VK_S:
                        s = false;
                    break;
                    case KeyEvent.VK_DOWN:
                        down = false;
                    break;
                    case KeyEvent.VK_D:
                        d = false;
                    break;
                    case KeyEvent.VK_RIGHT:
                        right = false;
                    break;
                    case KeyEvent.VK_A:
                        a = false;
                    break;
                    case KeyEvent.VK_LEFT:
                        left = false;
                    break;
                    case KeyEvent.VK_SPACE:
                        space = false;
                    break;
                    case KeyEvent.VK_SHIFT:
                        shift = false;
                    break;
                    case KeyEvent.VK_R:
                        respawn();
                    break;
                    case KeyEvent.VK_B:
                        eightbit = !eightbit;
                    break;
                    case KeyEvent.VK_F2:
                        try {
                            File file = new File("../res/screenshots/" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".png");
                            int i = 0;
                            while(file.exists()) {
                                i++;
                                file = new File("../res/screenshots/" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + "_" + i + ".png");
                            }

                            ImageIO.write(image, "png", file);

                            screenshot = 0;
                        } catch(Exception e2) {
                            System.err.println("Screenshot failed!");
                            e2.printStackTrace();
                        }
                    break;
                    /*case KeyEvent.VK_F5:
                        mainplayer.setVolume(mainplayer.getVolume() - 7f);
                    break;
                    case KeyEvent.VK_F6:
                        mainplayer.setVolume(mainplayer.getVolume() + 7f);
                    break;*/
                    case KeyEvent.VK_F3:
                        stats = !stats;
                }
                if(a == false && left == false && d == false && right == false && !(tile.slippery && !jumping))
                    xacceleration = 0;
                if(w == false && up == false && s == false && down == false && !(tile.slippery && !jumping))
                    yacceleration = 0;
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

        // setup tick timer
        new java.util.Timer().scheduleAtFixedRate(new java.util.TimerTask() { // yup, I had to specify the class
            public void run() {
                if(running) {
                    // tick the game dev off ;)
                    tick();

                    dispatchEvent("ontouch");

                    // do particle stuff, and calculate speed
                    new Thread(new Runnable() {@Override public void run() {
                        BufferedImage particles_front = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        BufferedImage particles_back = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        Graphics g1 = particles_front.getGraphics();
                        Graphics g2 = particles_back.getGraphics();

                        int mx = (int)(mousedx / 3) / 3; // I don't know why it need two /3's. TODO: debug.
                        int my = (int)(mousedy / 3) / 3;

                        int tilex = (int)(getTileX(real_width) / 2);
                        int tiley = (int)(getTileY(real_height) / 2);

                        double x = Game.this.x;
                        double y = Game.this.y;

                        // draw particles that are in front
                        for(Particle particle : particles) {
                            particle.tick();

                            if(particle.front) {
                                g1.setColor(particle.color);
                                int px = (int)(particle.x + x + mx + tilex), py = (int)(particle.y + y + my + tiley);
                                g1.drawLine(px, py, px, py);
                            } else {
                                g2.setColor(particle.color);
                                int px = (int)(particle.x + x + mx + tilex), py = (int)(particle.y + y + my + tiley);
                                g2.drawLine(px, py, px, py);
                            }
                        }

                        Game.this.particles_front = particles_front;
                        Game.this.particles_back = particles_back;
                    }}).start();

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
                                // TODO fix this bug!
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

                            if(w || up) {
                                yacceleration += acceleration;
                            }
                            if(s || down) {
                                yacceleration -= acceleration;
                            }
                            if(a || left) {
                                xacceleration += acceleration;
                            }
                            if(d || right) {
                                xacceleration -= acceleration;
                            }

                            if(jumping && !(w || a || s || d || up || down || left || right)) {
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

                            dispatchEvent("onmove");
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

                            for(Tile tile : tiles)
                                if(tick % tile.animation_time == 0) {
                                    if(tile.animation_frame > tile.animations.size() - 2)
                                        tile.animation_frame = 0;
                                    else
                                        tile.animation_frame++;
                                }

                            if(tile.teleport) {
                                x = telecoord(x, tile.teleportx);
                                y = telecoord(y, tile.teleporty);
                            }
                        } else if(tick > 10) {
                            System.out.println("null!\nnull!\nnull!\nnull!\nnull!\nnull!\nnull!\nA severe error occured.");
                            System.exit(1);
                        }

                        updateImage();
                        threadlocked = false;
                    } else
                        System.out.println("Warning: Skipped a tick!");
                }
            }
        }, rate, rate);

        // event dispather thread
        new Thread(new Runnable() {@Override public void run() {
            final String comma = Pattern.quote(",");
            final String colon = Pattern.quote(":");

            while(true) {
                if(!events.isEmpty()) {
                    Event event = events.remove();

                    if(event.tile != null && event.tile.eventstring.length() != 0) {
                        String[] split = tile.eventstring.split(comma);
                        for(String s : split) {
                            String[] split2 = s.split(colon);
                            if(split2[0].equalsIgnoreCase(event.name))
                                executeFunction(commands, split2[1]);
                        }
                    }
                }
            }
        }}).start();

        // audio playing thread
        new Thread(new Runnable() {@Override public void run() {
            if(mainplayer != null)
                mainplayer.loop();
        }}).start();

        // F3 statistics-updating thread
        new java.util.Timer().scheduleAtFixedRate(new TimerTask() {@Override public void run() {
            fps = frames;
            frames = 0;
            fps2 = frames2;
            frames2 = 0;

            particlecount = particles.size();
        }}, 1000, 1000);

        respawn();
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
        reset();

        dispatchEvent("ondeath");

        java.util.List<Particle> particles = Particle.randomlySpread(this, x + half, y + half, Color.GREEN, 600, 100);

        for(Particle particle : particles) {
            float r = random.nextFloat() / 3f;
            float g = random.nextFloat();
            float b = random.nextFloat() / 3f;
            particle.color = new Color(r, g, b);
            particle.xacceleration =  (Math.random() - 0.5) / 3;
            particle.yacceleration = (Math.random() - 0.5) / 3;
            particle.lifetime = (int)(Math.random() * (Math.random() * (Math.random() * 600)));
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
        Tile oldtile = this.tile;
        this.tile = getTile(getTile((int)((x + half) / tilesize),(int)(y / tilesize)));
        if(this.tile != oldtile) {
            events.add(new Event("onexit", oldtile)); // not using dispatch event cause current tile has changed
            dispatchEvent("onentry");
        }

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
                            this.setSpawn(-tilesize * Double.parseDouble(val), this.spawny, true);
                            break;
                        case "spawn.y":
                            this.setSpawn(this.spawnx, -tilesize * Double.parseDouble(val), true);
                            this.y = this.spawny;
                            break;
                        case "music.file":
                            if(mainplayer != null)
                                mainplayer.dispose();
                            this.mainplayer = new SoundPlayer(new File("../res/mus/" + val + ".wav"));
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
        int type = 0; // 0=tile, 1=entity, 2=command
        StringBuilder builder = new StringBuilder();

        for(String line : lines) {
            if(!line.startsWith("#") && line.length() != 0) {
                if(line.toLowerCase().startsWith("[tiles]"))
                    type = 0;
                else if(line.toLowerCase().startsWith("[entities]"))
                    type = 1;
                else if(line.toLowerCase().startsWith("[commands]"))
                    type = 2;
                else if(type == 0) {
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
                                        case "event":
                                        case "events":
                                            tile.eventstring = val.replace("{", "").replace("}", "");
                                            break;
                                        case "teleport":
                                            String[] splitx = val.split(Pattern.quote(","));
                                            tile.teleportx = splitx[0];
                                            tile.teleporty = splitx[1];
                                            tile.teleport = true;
                                            break;
                                        case "copy": // if something changes in this case, press Ctrl+F and search for "// updateme-1"
                                            Tile t = getTile(val.charAt(0));

                                            tile.image = t.image;
                                            tile.solid = t.solid;
                                            tile.dangerous = t.dangerous;
                                            tile.slippery = t.slippery;
                                            tile.spawn = t.spawn;
                                            tile.replace = t.replace;
                                            tile.speed = t.speed;
                                            tile.filter = t.filter;
                                            tile.filterset = t.filterset;
                                            tile.jump = t.jump;
                                            tile.defaultchar = t.defaultchar;
                                            tile.checkpoint = t.checkpoint;
                                            tile.acceleration = t.acceleration;
                                            tile.dither = t.dither;
                                            tile.slideover = t.slideover;
                                            tile.teleport = t.teleport;
                                            tile.teleportx = t.teleportx;
                                            tile.teleporty = t.teleporty;
                                            tile.eventstring = t.eventstring;

                                            tile.animated = t.animated;
                                            tile.animation_time = t.animation_time;
                                            tile.animation_frame = t.animation_frame;

                                            for(BufferedImage buffimg : t.animations)
                                                tile.animations.add(buffimg);

                                            tile.particle = t.particle;
                                            tile.particle_color = t.particle_color;
                                            tile.particle_count = t.particle_count;
                                            tile.particle_iteration = t.particle_iteration;
                                            tile.particle_lifetime = t.particle_lifetime;
                                            tile.particle_xacceleration = t.particle_xacceleration;
                                            tile.particle_front = t.particle_front;

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
                                                                if(pval.endsWith("s"))
                                                                    tile.particle_iteration = secondsToTicks(Double.parseDouble(pval.substring(0, pval.length() - 2)));
                                                                else
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
                                                    //e.printStackTrace();
                                                }
                                            }
                                            break;
                                        case "animation": {
                                                try {
                                                    String[] pairs = val.substring(val.indexOf('{') + 1, val.indexOf('}')).split(Pattern.quote(","));
                                                    int id = 0;
                                                    tile.animated = true;
                                                    for(String pval : pairs) {
                                                        tile.animated = true;
                                                        if(id == 0) {
                                                            if(pval.endsWith("s"))
                                                                tile.animation_time = secondsToTicks(Double.parseDouble(pval.substring(0, pval.length() - 2)));
                                                            else
                                                                tile.animation_time = Integer.parseInt(pval);
                                                            tile.animated = true;
                                                        } else {
                                                            BufferedImage img = null;
                                                            if(pval.equalsIgnoreCase("null"))
                                                                img = new BufferedImage(tilesize, tilesize, BufferedImage.TYPE_INT_ARGB);
                                                            else
                                                                img = getImage(pval);

                                                            tile.animated = true;
                                                            tile.animations.add(img);
                                                        }
                                                        id++;
                                                    }
                                                } catch(Exception e) {
                                                    System.err.println("Error: Failed to parse particle string for tile \"" + tile.character + "\".");
                                                    System.err.println(e.toString());
                                                    //e.printStackTrace();
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
                } else if(type == 1) {
                    // TODO
                } else if(type == 2) {
                    builder.append(line + "\n");
                }
            }
        }

        this.tiles = tiles;
        commands = builder.toString().replace(" ", "").replace("\n", "");
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
        // 6 months later-- why not change these? o.O
        // 1 day later-- change what? oh, those.
        final double x = this.x;
        final double y = this.y;

        HashMap<Character, BufferedImage> cache = new HashMap<>(); // cache animation and tile-getting
        for(int x2 = 0; x2 < map.length; x2++) {
            for(int y2 = 0; y2 < map[x2].length; y2++) { // TODO: Optimize
                char val = getTile(x2, y2);
                BufferedImage img = null;

                if(cache.containsKey(val)) { // check cache for tile
                    img = cache.get(val);
                } else { // get image
                    Tile t = getTile(val);

                    if(t == null)
                        t = getTile(defaultchar); // tile_null doesn't seem to be working
                    if(t.animated && t.animations.size() != 0)
                        img = t.animations.get(t.animation_frame);
                    else
                        img = t.image;

                    cache.put(val, img); // store image in cache
                }

                g.drawImage(img, (x2 * tilesize) + tilex + (int)(x) + mx, (y2 * tilesize) + tiley + (int)(y) + my, null); // render tile
            }
        }

        // draw particles that are in back
        g.drawImage(particles_back, 0, 0, null);

        // draw the player
        if(this.visible) {
            BufferedImage img = this.getCharacter(character_id, tick, !jumping && moving);
            g.drawImage(img, tilex - tilesize + mx,
                tiley - tilesize + my + (int)jumpboost,
                img.getWidth(), img.getHeight(), null);
        }

        // draw particles that are in front
        g.drawImage(particles_front, 0, 0, null);

        // draw the filter on top of everything
        if(filter != 0) {
            g.setColor(new Color(0, 0, 0, filter));
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
        }

        // set the new image
        g.dispose();
        this.setImage(image);
        frames++;
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

    public Font monospaced = new Font("Monospaced", Font.PLAIN, 12);

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
            if(screenshot == -1)
                g.drawImage(this.image, add, addy, wid, hei, null);
            else if(screenshot >= 20)
                screenshot = -1;
            else
                screenshot++;

            if(stats) { // print statistics
                g.setColor(new Color(0f, 0f, 0f, 0.5f));
                g.fillRect(0, 0, 175, 95);
                g.setColor(Color.GREEN);
                g.setFont(monospaced);
                g.drawString("FPS: " + fps + " & " + fps2, 15, 20);
                g.drawString("Particles: " + particlecount, 15, 35);
                g.drawString("Tick: " + tick, 15, 50);
                g.drawString("Position: [" + -(int)x + ", " + -(int)y + "]", 15, 65);
                g.drawString("Tile: [" + -(int)(x / tilesize) + ", " + -(int)(y / tilesize) + "]", 15, 80);
            }
        }

        frames2++;
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

    // calculcates coordinates to teleport to
    public double telecoord(double x, String order) { // can be x or y
        if(order.equals("NaN"))
            return x;
        else {
            double parsed = -tilesize * Double.parseDouble(order.substring(1));
            double ret = 0;

            if(order.startsWith("+"))
                ret = x + parsed;
            else if(order.startsWith("-"))
                ret = x - parsed;
            else
                ret = parsed;

            if(ret > 0)
                ret = 0;

            return ret;
        }
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

    // parse function config and execute the given function
    public void executeFunction(String config, String funcname) { // TODO: Make an evet dispatcher so the game doesn't lag
        final String semicolon = Pattern.quote(";");
        final String equals = Pattern.quote("=");

        try {
            String[] split = config.split(Pattern.quote("("));
            for(int i = 0; i < split.length; i++)
                if(i != 0) {
                    String name = split[i - 1];
                    if(name.contains(")"))
                        name = name.substring(name.lastIndexOf(")") + 1);

                    if(name.equals(funcname)) {
                        String[] commandz = (split[i].contains(")") ? split[i].substring(0, split[i].lastIndexOf(")")) : split[i]).split(semicolon); // TODO try removing if
                        for(String command : commandz) { // the funny spelling is cause the array `commands` already exists and it's easier to program when there aren't two arrays with the same name :P
                            final String[] split2 = command.toLowerCase().split(equals);
                            final String key = split2[0];
                            final String val = (split2.length != 1 ? split2[1].replace("{", "").replace("}", "") : ""); // Wow, I just learned replace(Str,Str) is the same as replaceall but without regex!
                            switch(key) {
                                case "rm": {
                                        switch(val.toLowerCase()) {
                                            case "entity":
                                                entities.clear();
                                                break;
                                            case "particle":
                                                particles.clear();
                                        }
                                    }
                                    break;
                                case "particle": {
                                        Color particle_color = Color.BLUE;
                                        int particle_count = 1;
                                        int particle_iteration = 1;
                                        int particle_lifetime = 1000;
                                        double particle_xacceleration = 0.01;
                                        double particle_yacceleration = 0.01;
                                        boolean particle_front = true;

                                        String[] pairs = val.split(Pattern.quote(","));

                                        for(String pair : pairs) {
                                            String[] parameter = pair.split(Pattern.quote(":"));
                                            String pkey = parameter[0].toLowerCase();
                                            String pval = parameter[1].toLowerCase();

                                            pval = replaceBrackets(pval);

                                            switch(pkey) {
                                                case "color":
                                                    particle_color = Color.decode(pval);
                                                    break;
                                                case "count":
                                                    particle_count = Integer.parseInt(pval);
                                                    break;
                                                case "iteration":
                                                    if(pval.endsWith("s"))
                                                        particle_iteration = secondsToTicks(Double.parseDouble(pval.substring(0, pval.length() - 2)));
                                                    else
                                                        particle_iteration = Integer.parseInt(pval);
                                                    break;
                                                case "lifetime":
                                                    if(pval.endsWith("s"))
                                                        particle_lifetime = secondsToTicks(Double.parseDouble(pval.substring(0, pval.length() - 2)));
                                                    else
                                                        particle_lifetime = Integer.parseInt(pval);
                                                    break;
                                                case "front":
                                                    particle_front = Boolean.parseBoolean(pval);
                                                    break;
                                                case "xacceleration":
                                                    particle_xacceleration = Double.parseDouble(pval);
                                                    break;
                                                case "yacceleration":
                                                    particle_yacceleration = Double.parseDouble(pval);
                                                    break;
                                                default:
                                                    System.out.println("Unknown parameter \"" + pkey + "\" for particle string for function \"" + funcname + "\"");
                                                    break;
                                            }
                                        }

                                        if(tick % particle_iteration == 0) {
                                            java.util.List<Particle> particles2 = Particle.randomlySpread(Game.this, x + half, y + half, particle_color, particle_lifetime, particle_count);

                                            for(Particle particle : particles2) {
                                                particle.xacceleration =  particle_xacceleration;
                                                particle.yacceleration = particle_yacceleration;
                                                particle.front = particle_front;

                                                particles.add(particle);
                                            }
                                        }
                                    }
                                    break;
                                case "kill":
                                    kill();
                                    break;
                                case "dither":
                                    eightbit = Boolean.parseBoolean(val);
                                    break;
                                case "sound":
                                    new SoundPlayer(new File("../res/mus/" + val + ".wav")).play();
                                    break;
                                case "filter":
                                    tile.filter = (int)((Double.parseDouble(val)) * 2.54);
                                    tile.filterset = true;
                                    break;
                                case "teleport":
                                    String[] sep = val.split(Pattern.quote(","));
                                    x = telecoord(x, sep[0]);
                                    y = telecoord(y, sep[1]);
                                    break;
                                default:
                                    if(val.length() != 0)
                                        System.err.println("Unknown parameter \"" + key + "\" with value \"" + val + "\" for function \"" + funcname + "\".");
                                    else if(key.equalsIgnoreCase(funcname))
                                        System.err.println("Indefinite loop detected in function \"" + funcname + "\"! Not going to follow loop.");
                                    else
                                        executeFunction(config, key);
                            }
                        }

                        return;
                    }
                }

            System.err.println("Failed to find function \"" + funcname + "\". Does not exist.");
        } catch(Exception e) {e.printStackTrace();
            throw new RuntimeException("Failed to parse command config.");
        }
    }

    public void dispatchEvent(String name) {
        events.add(new Event(name, tile));
    }
}

class Tile { // if something changes in this class, press Ctrl+F and search for "// updateme-1"
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
    public boolean teleport = false;
    public String teleportx = "NaN";
    public String teleporty = "NaN";
    public String eventstring = "";

    public boolean animated = false;
    public int animation_time = 20;
    public int animation_frame = 0;
    public java.util.List<BufferedImage> animations = new ArrayList<>();

    public boolean particle = false;
    public Color particle_color = Color.BLUE;
    public int particle_count = 1;
    public int particle_iteration = 1;
    public int particle_lifetime = 1000;
    public double particle_xacceleration = 0;
    public double particle_yacceleration = 0;
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

class Entity {
    public double x = 0;
    public double y = 0;
    public double xacceleration = 0;
    public double yacceleration = 0;
    public Game game = null;
    public Random random = new Random();
    public EntityState state = EntityState.STILL;
    public int speed = 0;
    public int walkCounter = 0;
    
    // init Entity
    public Entity(Game game) {
        this.game = game;
    }
    
    // Every tick, this function decides how the entity will react
    public void tick(int tick) {
        switch(state) {
            case FOLLOW:
                break;
            case MOVE:
                if(walkCounter == 0) {
                    walkCounter = 20;
                    double dir = random.nextDouble() * 3.1415d;
                    xacceleration = Math.cos(dir) * speed;
                    yacceleration = Math.sin(dir) * speed;
                } else {
                    x += xacceleration;
                    y += yacceleration;
                }
                break;
            case STILL:
                xacceleration = 0;
                yacceleration = 0;
                break;
            default:
                xacceleration = 0;
                yacceleration = 0;
                break;
        }
    }
    
    // delete and add a state
    public void setState(EntityState state) {
        this.state = state;
    }
}

enum EntityState {
    FOLLOW,
    MOVE,
    STILL,
    ATTACK
}

class Event {
    public String name = "";
    public Tile tile = null;

    public Event(String name, Tile tile) {
        this.name = name;
        this.tile = tile;
    }
}

class SoundPlayer {
    private File file = null;
    private Clip clip = null;
    private FloatControl controls = null;
    private long position = 0;

    public SoundPlayer(File file) {
        this.file = file;

        try {
            this.clip = AudioSystem.getClip();
            this.clip.open(AudioSystem.getAudioInputStream(file.toURI().toURL().openStream()));
            this.controls = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (Exception e) {
            System.err.println("Failed to initialize sound player for file " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public void play() {
        this.clip.setMicrosecondPosition(position);
        this.clip.start();
    }

    public void stop() {
        this.position = 0;
        this.clip.stop();
        this.clip.flush();
    }

    public void restart() {
        stop();
        play();
    }

    public void loop() {
        this.stop();
        this.clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void pause() {
        this.position = clip.getMicrosecondPosition();

        this.clip.stop();
        this.clip.flush();
    }

    // call this when done with audio
    public void dispose() {
        try {
            this.clip.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.clip = null;
        }
    }

    // 0f-1f I think?
    public void setVolume(float volume) {
        this.controls.setValue((float) Math.min(this.controls.getMaximum(), Math.max(this.controls.getMinimum(), volume)));
    }

    public float getVolume() {
        return this.controls.getValue();
    }

    public boolean isPlaying() {
        return this.clip != null && this.clip.isRunning();
    }

    public File getFile() {
        return this.file;
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
