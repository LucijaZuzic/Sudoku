import java.awt.List;
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

	Sudoku s;

	
	public void setSudoku(Sudoku s1) {
		s = s1;
	}
	
	public void CreateFile(String filename) {
	    try {
	      File myObj = new File(filename);
	      if (myObj.createNewFile()) {
	        System.out.println("File created: " + myObj.getName());
	      } else {
	        System.out.println("File already exists.");
	      }
	    } catch (IOException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
	  }
	
	 public void WriteToFile() {
		  try {
	        FileWriter myWriter = new FileWriter(testFile());
	        String line1 = "";
	        for (int i = 0; i < s.rows; i++) {
	        	for (int j = 0; j < s.cols; j++) {
	        		line1 += String.valueOf(s.userInput[i * s.cols + j]);
	        	}
	        	line1 += "\n";
	        }
	        String line2 = "";
	        for (int i = 0; i < s.rows; i++) {
	        	for (int j = 0; j < s.cols; j++) {
	        		line2 += String.valueOf(s.border[i * s.cols + j]);
	        	}
	        	line2 += "\n";
	        }
	        myWriter.write(line1 + line2);
	        myWriter.close();
	        System.out.println("Successfully wrote to the file.");
	      } catch (IOException e) {
	        System.out.println("An error occurred.");
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
		    	  System.out.println("Sadržaj datoteke je neispravan.");
		    	  return 1;
		      }
		      s.rows = cols;
		      s.cols = cols;
		      for (int i = 0; i < lineNum; i++) {
			      	System.out.print("LineNum " +  String.valueOf(i)+ " ");
		        	if (i < s.rows) {
				        System.out.print("userInput: ");
			        	for (int j = 0; j < s.cols; j++) {
			        		System.out.print(data.get(i).substring(j, j + 1) + " ");
			        		s.userInput[i * s.cols + j] = Integer.parseInt(data.get(i).substring(j, j + 1));
			        	}
		        	} else {
					    System.out.print("border: ");
		        		for (int j = 0; j < s.cols; j++) {
			        		System.out.print(data.get(i).substring(j, j + 1) + " ");
			        		s.border[(i - s.rows) * s.cols + j] = Integer.parseInt(data.get(i).substring(j, j + 1));
		        		}
		        	}
				    System.out.println("");
		      }
		      return 0;
		    } catch (FileNotFoundException e) {
		      System.out.println("An error occurred.");
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
		JFileChooser j = new JFileChooser();
		try {
			UIManager.setLookAndFeel(previousLF);
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int returnVal = j.showOpenDialog(createb);
	    System.out.println(returnVal);
	    File f = j.getSelectedFile();
		String newFile = f.getAbsolutePath();
	    if (returnVal == 1) {
	    	newFile = "sudoku.txt";
	    }
	    if(!f.exists() || f.isDirectory()) { 
	        CreateFile(newFile);
	    }
	    System.out.println(newFile);
		createb.setBounds(x, y, w, h);
		frame.add(createb);
		frame.setSize(300, 300);  
		frame.setLayout(null);  
		return newFile;
	 }
	
}
