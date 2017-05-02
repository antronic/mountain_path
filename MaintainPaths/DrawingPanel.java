import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DrawingPanel implements ActionListener, MouseMotionListener, WindowListener {
    
    
    // class constants
    public static final String ANIMATED_PROPERTY   = "drawingpanel.animated";
    public static final String DIFF_PROPERTY       = "drawingpanel.diff";
    public static final String HEADLESS_PROPERTY   = "drawingpanel.headless";
    public static final String MULTIPLE_PROPERTY   = "drawingpanel.multiple";
    public static final String SAVE_PROPERTY       = "drawingpanel.save";
    public static final String ANIMATION_FILE_NAME = "_drawingpanel_animation_save.txt";
    private static final String TITLE              = "DrawingPanel";
    private static final String COURSE_WEB_SITE = "http://www.cs.washington.edu/education/courses/cse142/CurrentQtr/drawingpanel.txt";
    private static final int DELAY                 = 100;     // delay between repaints in millis
    private static final int MAX_FRAMES            = 100;     // max animation frames
    private static final int MAX_SIZE              = 10000;   // max width/height
    private static final boolean DEBUG             = true;
    private static int instances = 0;
    private static Thread shutdownThread = null;
    
    
    
    private static boolean hasProperty(String name) {
        try {
            return System.getProperty(name) != null;
        } catch (SecurityException e) {
            if (DEBUG) System.out.println("Security exception when trying to read " + name);
            return false;
        }
    }
    
    private static boolean propertyIsTrue(String name) {
        try {
            String prop = System.getProperty(name);
            return prop != null && (prop.equalsIgnoreCase("true") || prop.equalsIgnoreCase("yes") || prop.equalsIgnoreCase("1"));
        } catch (SecurityException e) {
            if (DEBUG) System.out.println("Security exception when trying to read " + name);
            return false;
        }
    }
    
    
    // Returns whether the 'main' thread is still running.
    private static boolean mainIsActive() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        int activeCount = group.activeCount();
        
        // look for the main thread in the current thread group
        Thread[] threads = new Thread[activeCount];
        group.enumerate(threads);
        int i = 0;
        while(i < threads.length) {
          
            Thread thread = threads[i];
            String name = ("" + thread.getName()).toLowerCase();
            
            if (name.indexOf("main") >= 0 || 
                name.indexOf("testrunner-assignmentrunner") >= 0) {
              
                // found main thread!
                // (TestRunnerApplet's main runner also counts as "main" thread)
                return thread.isAlive();
            }
            
            i++;
        }
        
        // didn't find a running main thread; guess that main is done running
        return false;
    }
    
    private class ImagePanel extends JPanel {
        private static final long serialVersionUID = 0;
        private Image image;
        
        public ImagePanel(Image image) {
            setImage(image);
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(image.getWidth(this), image.getHeight(this)));
        }
        
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            if (zoom != 1) {
                g2.scale(zoom, zoom);
            }
            g2.drawImage(image, 0, 0, this);
        }
        
        public Image getImage(){
            return image;
        }
        
        public void setImage(Image image) {
            this.image = image;
            repaint();
        }
    }

    // fields
    private int width, height;             // dimensions of window frame
    private JFrame frame;                  // overall window frame
    private JPanel panel;                  // overall drawing surface
    private ImagePanel imagePanel;         // real drawing surface
    private BufferedImage img;           // remembers drawing commands
    private Graphics2D g2;                 // graphics context for painting
    private JLabel statusBar;              // status bar showing mouse position
    private JFileChooser chooser;          // file chooser to save files
    private long createTime;               // time at which DrawingPanel was constructed
    private Timer timer;                   // animation timer
    
    private Color backgroundColor = Color.WHITE;
    private String callingClassName;       // name of class that constructed this panel
    private boolean animated = false;      // changes to true if sleep() is called
    private boolean antiAlias = true;         // true to anti-alias
    private int instanceNumber;
    private int zoom = 1;
    
    // construct a drawing panel of given width and height enclosed in a window
    public DrawingPanel(int width, int height) {
        if (width < 0 || width > MAX_SIZE || height < 0 || height > MAX_SIZE) {
            throw new IllegalArgumentException("Illegal width/height: " + width + " x " + height);
        }
        
        
        synchronized (getClass()) {
            instances++;
            instanceNumber = instances;  // each DrawingPanel stores its own int number
            
            if (shutdownThread == null ) {
                shutdownThread = new Thread(new Runnable() {
                  
                  public void run() {
                    try {
                      while (true) {
                        // maybe shut down the program, if no more DrawingPanels are onscreen
                        // and main has finished executing
                        if ((instances == 0 || shouldSave()) && !mainIsActive()) {
                          try {
                            System.exit(0);
                          } catch (SecurityException sex) {}
                        }
                        
                        Thread.sleep(250);
                      }
                    } catch (Exception e) {}
                  }
                });
                shutdownThread.setPriority(Thread.MIN_PRIORITY);
                shutdownThread.start();
            }
        }
        this.width = width;
        this.height = height;
        
        if (DEBUG) System.out.println("w = " + width + " ,h = " + height);
        
        if (isAnimated() && shouldSave()) {
          
            // image must be no more than 256 colors
            img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED);
            
            // image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            antiAlias = false;   // turn off anti-aliasing to save palette colors
            
            // initially fill the entire frame with the background color,
            // because it won't show through via transparency like with a full ARGB image
            Graphics g = img.getGraphics();
            g.setColor(backgroundColor);
            g.fillRect(0, 0, width + 1, height + 1);
        } else {
            img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        
        g2 = (Graphics2D) img.getGraphics();
        g2.setColor(Color.BLACK);
        if (antiAlias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        
        
        
        if (isGraphical()) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            
            statusBar = new JLabel(" ");
            statusBar.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            panel.setBackground(backgroundColor);
            panel.setPreferredSize(new Dimension(width, height));
            imagePanel = new ImagePanel(img);
            imagePanel.setBackground(backgroundColor);
            panel.add(imagePanel);
            
            // listen to mouse movement
            panel.addMouseMotionListener(this);
            
            // main window frame
            frame = new JFrame(TITLE);
            frame.addWindowListener(this);
            JScrollPane center = new JScrollPane(panel);
            frame.getContentPane().add(center);
            frame.getContentPane().add(statusBar, "South");
            frame.setBackground(Color.DARK_GRAY);

            frame.pack();
            center(frame);
            frame.setVisible(true);
            if (!shouldSave()) {
                frame.toFront();
            }
            
            // repaint timer so that the screen will update
            createTime = System.currentTimeMillis();
            timer = new Timer(DELAY, this);
            timer.start();
            
        } else if (shouldSave()) {
          
            // headless mode; just set a hook on shutdown to save the image
            callingClassName = getCallingClassName();
            try {
                
            } catch (Exception e) {
                if (DEBUG) System.out.println("unable to add shutdown hook: " + e);
            }
        }
    }
    
    // used for an internal timer that keeps repainting
    public void actionPerformed(ActionEvent e) {
      
        if (e.getSource() instanceof Timer) {
            // redraw the screen at regular intervals to catch all paint operations
            panel.repaint();
            if (shouldDiff() &&
                System.currentTimeMillis() > createTime + 4 * DELAY) {
                String expected = System.getProperty(DIFF_PROPERTY);
                try {
                    String actual = saveToTempFile();
                    
                    
                } catch (IOException ioe) {
                    System.err.println("Error diffing image: " + ioe);
                }
                timer.stop();
            } else if (shouldSave() && readyToClose()) {
              
                // auto-save-and-close if desired
                try {
                    
                  save(System.getProperty(SAVE_PROPERTY));
                    
                } catch (IOException ioe) {
                    System.err.println("Error saving image: " + ioe);
                }
                exit();
            }
        } 
    }
    
    // closes the frame and exits the program
    private void exit() {
        if (isGraphical()) {
            frame.setVisible(false);
            frame.dispose();
        }
        try {
            System.exit(0);
        } catch (SecurityException e) {
            // if we're running in an applet or something, can't do System.exit
        }
    }
    
    
    // obtain the Graphics object to draw on the panel
    public Graphics2D getGraphics() {
        return g2;
    }
    
    // listens to mouse dragging
    public void mouseDragged(MouseEvent e) {}
    
    // listens to mouse movement
    public void mouseMoved(MouseEvent e) {
      
        int x = e.getX() / zoom;
        int y = e.getY() / zoom;
        setStatusBarText("(" + x + ", " + y + ")");
    }
    
    

    // take the current contents of the panel and write them to a file
    public void save(String filename) throws IOException {
        BufferedImage img2 = getImage();

        int lastDot = filename.lastIndexOf(".");
        String ext = filename.substring(lastDot + 1);
        
        // write file
        // TODO: doesn't save background color I don't think
        ImageIO.write(img2, ext, new File(filename));
    }
    
    
    
    // set the background color of the drawing panel
    public void setBackground(Color c) {
      
        Color oldBackgroundColor = backgroundColor;
        backgroundColor = c;
        
        if (isGraphical()) {
          
            panel.setBackground(c);
            imagePanel.setBackground(c);
        }
        
        // with animated images, need to palette-swap the old bg color for the new
        // because there's no notion of transparency in a palettized 8-bit image
        if (isAnimated()) {
            replaceColor(img, oldBackgroundColor, c);
        }
    }
    
    // show or hide the drawing panel on the screen
    public void setVisible(boolean visible) {
      
        if (isGraphical()) {
            frame.setVisible(visible);
        }
    }
    
    // returns the drawing panel's width in pixels
    public int getWidth() {
        return width;
    }
    
    // returns the drawing panel's width in pixels
    public int getHeight() {
        return height;
    }
     
    // returns the drawing panel's pixel size (width, height) as a Dimension object
    public Dimension getSize() {
        return new Dimension(width, height);
    }
    
    // sets the drawing panel's width in pixels to the given value
    // After calling this method, the client must call getGraphics() again
    // to get the new graphics context of the newly enlarged image buffer.
    public void setWidth(int width) {
        setSize(width, getHeight());
    }
     
    // sets the drawing panel's height in pixels to the given value
    // After calling this method, the client must call getGraphics() again
    // to get the new graphics context of the newly enlarged image buffer.
    public void setHeight(int height) {
        setSize(getWidth(), height);
    }
     
    // sets the drawing panel's pixel size (width, height) to the given values
    // After calling this method, the client must call getGraphics() again
    // to get the new graphics context of the newly enlarged image buffer.
    public void setSize(int width, int height) {
        // replace the image buffer for drawing
        BufferedImage newImage = new BufferedImage(width, height, img.getType());
        imagePanel.setImage(newImage);
        newImage.getGraphics().drawImage(img, 0, 0, imagePanel);

        this.width = width;
        this.height = height;
        img = newImage;
        g2 = (Graphics2D) newImage.getGraphics();
        g2.setColor(Color.BLACK);
        if (antiAlias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        
        if (isGraphical()) {
            frame.pack();
        }
    }
    
    public void setImage(BufferedImage img){
        imagePanel.setImage(img);
    }
    
    public Image getBufferedImage(){
        return imagePanel.getImage();
    }

    // moves window on top of other windows
    public void toFront() {
        frame.toFront();
    }
    
    // called when DrawingPanel closes, to potentially exit the program
    public void windowClosing(WindowEvent event) {
        frame.setVisible(false);
        synchronized (getClass()) {
            instances--;
        }
        frame.dispose();
    }
    
    // methods required by WindowListener interface
    public void windowActivated(WindowEvent event) {}
    public void windowClosed(WindowEvent event) {}
    public void windowDeactivated(WindowEvent event) {}
    public void windowDeiconified(WindowEvent event) {}
    public void windowIconified(WindowEvent event) {}
    public void windowOpened(WindowEvent event) {}


    // moves given jframe to center of screen
    private void center(Window frame) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screen = tk.getScreenSize();
        
        int x = Math.max(0, (screen.width - frame.getWidth()) / 2);
        int y = Math.max(0, (screen.height - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }
    
    // constructs and initializes JFileChooser object if necessary
    private void checkChooser() {
        if (chooser == null) {
            // TODO: fix security on applet mode
            chooser = new JFileChooser(System.getProperty("user.dir"));
            chooser.setMultiSelectionEnabled(false);
            
        }
    }
    
    // returns a best guess about the name of the class that constructed this panel
    private String getCallingClassName() {
      
        StackTraceElement[] stack = new RuntimeException().getStackTrace();
        String className = this.getClass().getName();
        
        for (StackTraceElement element : stack) {
            String cl = element.getClassName();
            
            if (!className.equals(cl)) {
              
                className = cl; break;
            }
        }
        
        return className;
    }
    
    private BufferedImage getImage() {
        // create second image so we get the background color
        BufferedImage img2;
        if (isAnimated()) {
          
            img2 = new BufferedImage( width, height, BufferedImage.TYPE_BYTE_INDEXED);
            
        } else {
          
            img2 = new BufferedImage( width, height, img.getType());
        }
        Graphics g = img2.getGraphics();
        if (DEBUG) System.out.println("getImage setting background to " + backgroundColor);
        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);
        g.drawImage(img, 0, 0, panel);
        return img2;
    }
    
    
    
    private boolean isAnimated() {
      
        return animated || propertyIsTrue(ANIMATED_PROPERTY);
    }
    
    private boolean isGraphical() {
      
        return !hasProperty(SAVE_PROPERTY) && !hasProperty(HEADLESS_PROPERTY);
    }
    
    
    
    private boolean readyToClose() {

        return (instances == 0 || shouldSave()) && !mainIsActive();
    }
    
    private void replaceColor(BufferedImage image, Color oldColor, Color newColor) {
        int oldRGB = oldColor.getRGB();
        int newRGB = newColor.getRGB();
        
        for (int y = 0; y < image.getHeight(); y++) {
          
            for (int x = 0; x < image.getWidth(); x++) {
              
                if (image.getRGB(x, y) == oldRGB) {
                    image.setRGB(x, y, newRGB);
                }
            }
        }
    }
    
    
    // saves DrawingPanel image to a temporary file and returns file's name
    private String saveToTempFile() throws IOException {
        File currentImageFile = File.createTempFile("current_image", ".png");
        save(currentImageFile.toString());
        return currentImageFile.toString();
    }
    
    // sets the text that will appear in the bottom status bar
    private void setStatusBarText(String text) {
      
        if (zoom != 1) {
            text += " (current zoom: " + zoom + "x" + ")";
        }
        statusBar.setText(text);
    }
    
    
    
    private boolean shouldDiff() {
      
        return hasProperty(DIFF_PROPERTY);
    }
    
    private boolean shouldSave() {
      
        return hasProperty(SAVE_PROPERTY);
    }

    
}
