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
  
  

  public int findMinValue(){
    
    int min = grid[0][0];
    for (int i = 0; i<grid.length; i++) {
      for (int j = 0; j<grid[0].length; j++) {
        if (grid[i][j]<min){
          min = grid[i][j];
        }
      }
    }
    return min;
  }
  
  

  public int findMaxValue(){
    
    int max = grid[0][0];
    for (int i = 0; i<grid.length; i++) {
      
      for (int j = 0; j<grid[0].length; j++) {
        
        if (grid[i][j]>max){
          max = grid[i][j];
        }
      }
    }
    return max;
  }
  
  

  public int indexOfMinInCol(int col){
    
    int min = grid[0][col];
    int minRow = 0;
    for (int i = 1; i < grid.length; i++){
      
      if (grid[i][col] < min){
        
        min = grid[i][col];
        minRow = i;
      }
    }
    return minRow;
  }
  

  public void drawMap(Graphics g){
    
  }
  

  public int drawLowestElevPath(Graphics g, int row){
    
  }
  

  public int indexOfLowestElevPath(Graphics g){
    
    
  }
  
  
}