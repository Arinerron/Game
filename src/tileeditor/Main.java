package tileeditor;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class Main {
    static int xmax = 16;
    static int ymax = 16;
    static int pixelsize = 16;
    static byte[] colormap = new byte[xmax * ymax + 1];
    static byte currentColor = 0;
    static final JFileChooser fc = new JFileChooser(new File("../res/"));
    static JFrame frame;
    static Color[] colors = new Color[] {
        new Color(0x747474),
        new Color(0x24188c),
        new Color(0x0000a8),
        new Color(0x44009c),
        new Color(0x8c0074),
        new Color(0xa80010),
        new Color(0xa40000),
        new Color(0x7c0800),
        new Color(0x402c00),
        new Color(0x004400),
        new Color(0x005000),
        new Color(0x003c14),
        new Color(0x183c5c),
        new Color(0x000000),
        new Color(0x000000),
        new Color(0x000000),

        new Color(0xbcbcbc),
        new Color(0x0070ec),
        new Color(0x2038ec),
        new Color(0x8000f0),
        new Color(0xbc00bc),
        new Color(0xe40058),
        new Color(0xd82800),
        new Color(0xc84c0c),
        new Color(0xac7c00),
        new Color(0x009400),
        new Color(0x00a800),
        new Color(0x009038),
        new Color(0x008088),
        new Color(0x000000),
        new Color(0x000000),
        new Color(0x000000),

        new Color(0xfcfcfc),
        new Color(0x3cbcfc),
        new Color(0x5c94fc),
        new Color(0xcc88fc),
        new Color(0xf478fc),
        new Color(0xfc74b4),
        new Color(0xfc7460),
        new Color(0xfc9838),
        new Color(0xf0bc3c),
        new Color(0x80d010),
        new Color(0x4cdc48),
        new Color(0x58f898),
        new Color(0x00e8d8),
        new Color(0x787878),
        new Color(0x000000),
        new Color(0x000000),

        new Color(0xcccccc),
        new Color(0xa8e4fc),
        new Color(0xc4d4fc),
        new Color(0xd4c8fc),
        new Color(0xfcc4fc),
        new Color(0xfcc4d8),
        new Color(0xfcbcb0),
        new Color(0xfcd8a8),
        new Color(0xfce4a0),
        new Color(0xe0fca0),
        new Color(0xa8f0bc),
        new Color(0xb0fccc),
        new Color(0x9cfcf0),
        new Color(0xc4c4c4),
        new Color(0x000000),
        new Color(0x000000)
    };
    static JPanel panel1;
    static JPanel panel2;

    public static void main(String[] args) {
        fc.setAcceptAllFileFilterUsed(true);
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(new FileNameExtensionFilter("Mooncat Compressed Image (.mci)", "mci"));
        refresh();
        frame = new JFrame("Tile Editor");
        panel1 = new JPanel() {
            public void paintComponent(Graphics g) {
                for(int x = 0; x < xmax; x++) {
                    for(int y = 0; y < ymax; y++) {
                        if(colormap[x * ymax + y + 1] == 48) {
                            g.setColor(Color.white);
                            g.fillRect(x * pixelsize, y * pixelsize, pixelsize / 2, pixelsize / 2);
                            g.fillRect(x * pixelsize + pixelsize / 2, y * pixelsize + pixelsize / 2, pixelsize / 2, pixelsize / 2);
                            g.setColor(colors[48]);
                            g.fillRect(x * pixelsize, y * pixelsize + pixelsize / 2, pixelsize / 2, pixelsize / 2);
                            g.fillRect(x * pixelsize + pixelsize / 2, y * pixelsize, pixelsize / 2, pixelsize / 2);
                        } else {
                            g.setColor(colors[colormap[x * ymax + y + 1]]);
                            g.fillRect(x * pixelsize, y * pixelsize, pixelsize, pixelsize);
                        }
                    }
                }
                g.setColor(Color.black);
                for(int x = 1; x < xmax / 8; x++) {
                    g.drawLine(x * pixelsize * 8, 0, x * pixelsize * 8, ymax * pixelsize * 8);
                }
                for(int y = 1; y < ymax / 8; y++) {
                    g.drawLine(0, y * pixelsize * 8, xmax * pixelsize * 8, y * pixelsize * 8);
                }
            }
        };
        panel1.addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent e) {
                try {
                    colormap[(e.getX() / pixelsize) * ymax + (e.getY() / pixelsize) + 1] = currentColor;
                } catch(Exception e1) {}
                panel1.repaint();
            }

            public void mouseReleased(MouseEvent e) {}
            public void mouseClicked(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });
        panel1.addMouseMotionListener(new MouseMotionListener() {
            int lastX = -1;
            int lastY = -1;
            public void mouseDragged(MouseEvent e) {
                if(e.getX() / pixelsize != lastX || e.getY() / pixelsize != lastY) {
                    lastX = e.getX() / pixelsize;
                    lastY = e.getY() / pixelsize;
                    try {
                        colormap[(e.getX() / pixelsize) * ymax + (e.getY() / pixelsize) + 1] = currentColor;
                    } catch(Exception e1) {}
                    panel1.repaint();
                }
            }

            public void mouseMoved(MouseEvent e) {}
        });
        panel2 = new JPanel();
        {
            final int len = colors.length;
            panel2.setLayout(new GridLayout(4, 16));
            for(int i = 0; i < 16; i++) {
                for(int j = 0; j < 4; j++) {
                    JButton b;
                    final byte color = (byte)(i * 4 + j);
                    if(i == 12 && j == 0) {
                        b = new JButton() {
                            public void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                g.setColor(Color.gray);
                                g.drawLine(0, getHeight(), getWidth(), 0);
                            }
                        };
                        b.setBackground(Color.black);
                    } else {
                        b = new JButton();
                        b.setBackground(colors[color]);
                    }
                    b.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            currentColor = color;
                        }
                    });
                    panel2.add(b);
                }
            }
        }
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        menuBar.add(file);
        // TODO: Add undo and redo features
        //JMenu edit = new JMenu("Edit");
        //menuBar.add(edit);

        JMenuItem newImage = new JMenuItem("New");
        newImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveRequest();
                refresh();
                panel1.repaint();
            }
        });
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
            save();
        }});
        JMenuItem open = new JMenuItem("Open");
        open.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
            open();
        }});
        JMenuItem export = new JMenuItem("Export");
        export.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
            export();
        }});
        JMenuItem import_file = new JMenuItem("Import");
        import_file.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
            import_file();
        }});
        JMenuItem quit = new JMenuItem("Exit");
        quit.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {
            saveRequest();
            System.exit(0);
        }});

        file.add(newImage);
        file.addSeparator();
        file.add(open);
        file.add(save);
        file.addSeparator();
        file.add(export);
        file.add(import_file);
        file.addSeparator();
        file.add(quit);

        panel2.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        //frame.setResizable(false);
        frame.getContentPane().add("North", menuBar);
        frame.getContentPane().add("Center", panel1);
        frame.getContentPane().add("South", panel2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        refreshPanels();
        frame.setVisible(true);
    }

    public static void refresh() {
        for(int x = 0; x < xmax; x++) {
            for(int y = 0; y < ymax; y++) {
                colormap[x * ymax + y + 1] = 48;
            }
        }
    }

    public static void saveRequest() {
        int dialogResult = JOptionPane.showConfirmDialog(null, "Save image?", "Are you sure..?", JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
            save();
        }
    }

    public static void save() {
        if(fc.showSaveDialog(panel1) == JFileChooser.APPROVE_OPTION) {
            try {
                colormap[0] = (byte)xmax;
                File f = fc.getSelectedFile();
                String filePath = f.getAbsolutePath();
                if(!filePath.endsWith(".mci")) {
                    f = new File(filePath + ".mci");
                }
                Files.write(f.toPath(), compress(colormap));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void open() {
        if(fc.showOpenDialog(panel1) == JFileChooser.APPROVE_OPTION) {
            try {
                colormap = decompress(Files.readAllBytes(fc.getSelectedFile().toPath()));
                xmax = colormap[0];
                ymax = (colormap.length - 1) / xmax;
                refreshPanels();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void export() {
        if(fc.showSaveDialog(panel1) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage b = new BufferedImage(xmax, ymax, BufferedImage.TYPE_INT_ARGB);
                for(int i = 0; i < b.getWidth(); i++) {
                    for(int j = 0; j < b.getHeight(); j++) {
                        b.setRGB(i, j, colors[colormap[i * b.getHeight() + j + 1]].getRGB());
                    }
                }
                ImageIO.write(b, "png", fc.getSelectedFile());
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void import_file() {
        if(fc.showOpenDialog(panel1) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage b = ImageIO.read(fc.getSelectedFile());
                colormap = new byte[b.getWidth()* b.getHeight() + 1];
                for(int i = 0; i < b.getWidth(); i++) {
                    for(int j = 0; j < b.getHeight(); j++) {
                        colormap[i * b.getHeight() + j + 1] = (byte)indexof(b.getRGB(i, j));
                    }
                }
                xmax = b.getWidth();
                ymax = b.getHeight();
                refreshPanels();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int indexof(int rgb) {
        int closest = 99999999;
        int id = -1;
        final Color color = new Color(rgb);
        final int len = colors.length;
        int j;
        for(int i = 0; i < len; i++) {
            j = (int)Math.sqrt((colors[i].getRed() - color.getRed()) * (colors[i].getRed() - color.getRed())
            + (colors[i].getGreen() - color.getGreen()) * (colors[i].getGreen() - color.getGreen())
            + (colors[i].getBlue() - color.getBlue()) * (colors[i].getBlue() - color.getBlue()));
            if(j < closest) {
                closest = j;
                id = i;
            }
        }
        return id;
    }

    public static byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();

        return output;
    }

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

    public static void refreshPanels() {
        pixelsize = 500 / Math.max(xmax, ymax);
        panel1.setPreferredSize(new Dimension(pixelsize * xmax, pixelsize * ymax));
        panel2.setPreferredSize(new Dimension(pixelsize * xmax, pixelsize * xmax / 4 + 10));
        frame.pack();
        panel1.repaint();
    }
}
