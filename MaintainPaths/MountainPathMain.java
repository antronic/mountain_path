import java.awt.*;

public class MountainPathMain{
  
  public static void main(String[] args){
    
    //construct DrawingPanel, and get its Graphics context
    DrawingPanel board = new DrawingPanel(844, 480);
    Graphics g = board.getGraphics();
    
    //Step 1 - construct mountain map data
    Map map = new Map("Colorado_844x480.dat", 480, 844);
    
    //Step 2 - min, max, minRow in col
    int min = map.findMinValue();
    System.out.println("Min value in map: " + min);
    
    int max = map.findMaxValue();
    System.out.println("Max value in map: " + max);
    
    for (int i = 0; i < map.getRows(); i++){
      
      
      System.out.println(map.grid[i][0]);
    }
    
  }
  
  
}
