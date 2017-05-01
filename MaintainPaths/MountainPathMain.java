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
    
    
    int i = 0;
    while(i < map.getRows()){
      
      
      System.out.println(map.grid[i][0]);
      i++;
    }
    
    int minRow = map.indexOfMinInCol(0);
    System.out.println("Row with lowest value in col 0: " + minRow);
    
    //Step 3 - draw the map
    map.drawMap(g);
    
    //Step 4 - draw a greedy path
    g.setColor(Color.RED); //can set the color of the 'brush' before drawing, then method doesn't need to worry about it
    int totalChange = map.drawLowestElevPath(g, minRow); //use minRow from Step 2 as starting point
    System.out.println("Lowest-Elevation-Change Path starting at row " + minRow + " gives total change of: " + totalChange);
    
    //Step 5 - draw the best path
    g.setColor(Color.RED);
    int bestRow = map.indexOfLowestElevPath(g);
    
    //map.drawMap(g); //use this to get rid of all red lines
    g.setColor(Color.GREEN); //set brush to green for drawing best path
    totalChange = map.drawLowestElevPath(g, bestRow);
    System.out.println("The Lowest-Elevation-Change Path starts at row: " + bestRow + " and gives a total change of: " + totalChange);
    
  }
  
  
}
