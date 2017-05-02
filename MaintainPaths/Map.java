import java.util.*;
import java.io.*;
import java.awt.*;

public class Map{
  
  public int[][] grid;
  
  public Map(String filename, int rows, int cols){
    
    grid = new int[rows][cols];
    
    Scanner sc;
    try {
      
      sc = new Scanner(new File(filename));
      for (int i = 0; i < rows; i++){
        
        for (int j = 0; j < cols; j++){
          
          if (sc.hasNextInt()){
            
            grid[i][j] = sc.nextInt(); 
          }
        }
        
      }
      
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  
  public int getRows(){
    
    return grid.length;
  }
  
  public int getColumns(){
    
    return grid[0].length;
  }
  
  
  /**
   * @return the min value in the entire grid
   */
  public int findMinValue(){
    
    int min = grid[0][0];
    for (int i = 0; i < grid.length; i++) {
      
      for (int j = 0; j < grid[0].length; j++) {
        
        if (grid[i][j] < min){
          min = grid[i][j];
        }
      }
    }
    return min;
  }
  
  
  /**
   * @return the max value in the entire grid
   */
  public int findMaxValue(){
    
    int max = grid[0][0];
    for (int i = 0; i < grid.length; i++) {
      
      for (int j = 0; j < grid[0].length; j++) {
        
        if (grid[i][j] > max){
          max = grid[i][j];
        }
      }
    }
    return max;
  }
  
  
  /**
   * @param col the column of the grid to check
   * @return the index of the row with the lowest value in the given col for the grid
   */
  public int indexOfMinCol(int cols){
    
    int min = grid[0][cols];
    int minRow = 0;
    for (int i = 1; i < grid.length; i++){
      
      if (grid[i][cols] < min){
        
        min = grid[i][cols];
        minRow = i;
      }
    }
    return minRow;
  }
  
  /**
   * Draws the grid using the given Graphics object.
   * Colors should be grayscale values 0-255, scaled based on min/max values in grid
   */
  public void drawMap(Graphics g){
    int min = findMinValue();
    int max = findMaxValue();
    
    double scale = 255.0 / (max - min);
    
    int[][] greyscale = new int[grid.length][grid[0].length];
    
    for (int i = 0; i < grid.length; i++){
      
      for (int j = 0; j < grid[0].length; j++){
        
        greyscale[i][j] = (int) ((grid[i][j] - min) * scale);
      }
    }
    
    for (int i = 0; i < greyscale.length; i++){
      
      for (int j = 0; j < greyscale[0].length; j++){
        
        int value = greyscale[i][j];
        g.setColor(new Color( value, value, value));
        g.fillRect( j, i, 1, 1);
      }
    }
  }
  
  /**
   * Find a path from West-to-East starting at given row.
   * Choose a forward step out of 3 possible forward locations, using greedy method described in assignment.
   * @return the total change in elevation traveled from West-to-East
   */
  public int LowestElevPath(Graphics g, int rows){
    
    int max = findMaxValue();
    int totalChange = 0;
    int j = 0;
    
    while(j < grid[0].length - 1){
      
      g.fillRect(j, rows, 1, 1);
      int fwd = grid[rows][j + 1];
      int fwd_up = -1;
      int fwd_down = -1;
      
      if (rows != 0){
        
        fwd_up = grid[rows - 1][j + 1];
      }
      
      if (rows != grid.length - 1){
        
        fwd_down = grid[rows + 1][j + 1];
      }
      
      int current_fwd_diff = Math.abs(grid[rows][j] - fwd);
      int current_fwdup_diff = max + 1;
      int current_fwddown_diff = max + 1;
      if (fwd_up > -1){
        
        current_fwdup_diff = Math.abs(grid[rows][j] - fwd_up);
      }
      
      if (fwd_down > -1){
        
        current_fwddown_diff = Math.abs(grid[rows][j] - fwd_down);
      }
      
      int least = current_fwd_diff;
      
      if (current_fwd_diff > current_fwdup_diff){
        
        if (current_fwdup_diff > current_fwddown_diff){
          
          least = current_fwddown_diff;
          rows++;
        }else{
          
          least = current_fwdup_diff;
          rows--;
        }
      }else{
        
        if (current_fwd_diff > current_fwddown_diff){
          
          least = current_fwddown_diff;
          rows++;
          
        }else{
          least = current_fwd_diff;
        }
      }
      
      totalChange += least;
      j++;
    }
    
    return totalChange;
  }
  
  /**
   * @return the index of the starting row for the lowest-elevation-change path in the entire grid.
   */
  public int indexOfLowestElevPath(Graphics g){
    
    int least = LowestElevPath(g, 0);
    int index = 0;
    int i = 1;
    
    while(i < grid.length){
      
      int change = LowestElevPath(g, i);
      
      if (change < least){
        least = change;
        index = i;
      }
      
      i++;
    }
    
    return index;
    
  }
  
  
}