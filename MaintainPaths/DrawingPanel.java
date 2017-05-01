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
  
  
  // fields
  private int width, height;             // dimensions of window frame
  private Graphics2D g2;                 // graphics context for painting
  private long createTime;               // time at which DrawingPanel was constructed
  
  // construct a drawing panel of given width and height enclosed in a window
  public DrawingPanel(int width, int height) {
    if (width < 0 || width > MAX_SIZE || height < 0 || height > MAX_SIZE) {
      throw new IllegalArgumentException("Illegal width/height: " + width + " x " + height);
    }
    
    this.width = width;
    this.height = height;
        
    if (DEBUG) System.out.println("w = " + width + " ,h = " + height);
  }
  
  // used for an internal timer that keeps repainting
  public void actionPerformed(ActionEvent e) {
      
    
  }
  
  // obtain the Graphics object to draw on the panel
  public Graphics2D getGraphics() {
    return g2;
  }
  
  // listens to mouse dragging
  public void mouseDragged(MouseEvent e) {}
    
  // listens to mouse movement
  public void mouseMoved(MouseEvent e) {
    
  }
}
