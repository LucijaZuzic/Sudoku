import java.io.File;  // Import the File class
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;  // Import the IOException class to handle errors
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class FileManipulator {

	SudokuGrid sudoku;

	
	public void setSudoku(SudokuGrid s1) {
		sudoku = s1;
	}
	
	public void CreateFile(String filename) {
	    try {
	      File myObj = new File(filename);
	      if (myObj.createNewFile()) {
	        //System.out.println("File created: " + myObj.getName());
	      } else {
	        //System.out.println("File already exists.");
	      }
	    } catch (IOException e) {
	      //System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
	  }
	
	 public void WriteToFile() {
		  try {
	        FileWriter myWriter = new FileWriter(testFile());
	        String line1 = "";
	        for (int row = 0; row < sudoku.rows; row++) {
	        	for (int col = 0; col < sudoku.cols; col++) {
	        		line1 += String.valueOf(sudoku.userInput[row * sudoku.cols + col]);
	        	}
	        	line1 += "\n";
	        }
	        String line2 = "";
	        for (int i = 0; i < sudoku.rows; i++) {
	        	for (int j = 0; j < sudoku.cols; j++) {
	        		line2 += String.valueOf(sudoku.border[i * sudoku.cols + j]);
	        	}
	        	line2 += "\n";
	        }
	        myWriter.write(line1 + line2);
	        myWriter.close();
			InformationBox.infoBox("Zagonetka je uspješno spremljena.", "Spremanje datoteke");
	      } catch (IOException e) {
	        //System.out.println("An error occurred.");
	        e.printStackTrace();
	      }
	  }
	 
	 public int ReadFile() {
		    try {
		      File myObj = new File(testFile());
		      java.util.Scanner myReader = new java.util.Scanner(myObj);
	          int lineNum = 0;
	          int cols = 0;
	          ArrayList<String> data = new ArrayList<String>();
		      while (myReader.hasNextLine()) {
		    	    data.add(myReader.nextLine());
		    	    cols = data.get(lineNum).length();
				    lineNum++;
		      }
		      myReader.close(); 
		      if (lineNum != cols * 2 || lineNum == 0) {
				  InformationBox.infoBox("Sadržaj datoteke je neispravan.", "Uèitavanje datoteke");
		    	  return 1;
		      }
		      sudoku.rows = cols;
		      sudoku.cols = cols;
		      for (int row = 0; row < lineNum; row++) {
		        	if (row < sudoku.rows) {
			        	for (int col = 0; col < sudoku.cols; col++) {
			        		sudoku.userInput[row * sudoku.cols + col] = Integer.parseInt(data.get(row).substring(col, col + 1));
			        	}
		        	} else {
		        		for (int col = 0; col < sudoku.cols; col++) {
			        		sudoku.border[(row - sudoku.rows) * sudoku.cols + col] = Integer.parseInt(data.get(row).substring(col, col + 1));
		        		}
		        	}
		      }
		      return 0;
		    } catch (FileNotFoundException e) {
		      //System.out.println("An error occurred.");
		      e.printStackTrace();
		      return 1;
		    }
	}
	 
	 public String testFile () {
		JFrame frame = new JFrame();
		int x = 15;
		int y = 15;
		int w = 100;
		int h = 30;
		JButton createb = new JButton("Uèitaj");
		LookAndFeel previousLF = UIManager.getLookAndFeel();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JFileChooser fileChooser = new JFileChooser();
		try {
			UIManager.setLookAndFeel(previousLF);
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int returnVal = fileChooser.showOpenDialog(createb);
	    //System.out.println(returnVal);
	    File file = fileChooser.getSelectedFile();
		String newFile = file.getAbsolutePath();
	    if (returnVal == 1) {
	    	newFile = "sudoku.txt";
	    }
	    if(!file.exists() || file.isDirectory()) { 
	        CreateFile(newFile);
	    }
	    //System.out.println(newFile);
		createb.setBounds(x, y, w, h);
		frame.add(createb);
		frame.setSize(300, 300);  
		frame.setLayout(null);  
		return newFile;
	 }
	
}
