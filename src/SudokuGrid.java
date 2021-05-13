import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;

public abstract class SudokuGrid {
	JFrame frame;
	int rows = 9;
	int cols = 9;
	int xLim = 3;
	int yLim = 3;
	int[] border;
	int[] boxNumber;
	JButton[] field;
	int[] solution;
	int[] userInput;
	int[] temporary;
	Set<String> sizeRelationships;

    int mintargetDifficulty = 200;
    int maxtargetDifficulty = 160000;

	int numPossibilities[] = new int[rows * cols];
	int[][] possibilities = new int[rows * cols][rows];


	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    double baselineWidth = 1920;
    double baselineHeight = 1080;
    double width = screenSize.getWidth();
    double height = screenSize.getHeight();
    double widthScaling = width / baselineWidth;
    double heightScaling = height / baselineHeight;
    int space = (int) (15 * widthScaling);
    int x = space;
	int y = space;
	int w = (int) (60 * widthScaling);
	int h = (int) (60 * heightScaling);
	int wNumber = (int) (90 * widthScaling);
	int hNumber = (int) (90 * heightScaling);
	int wDigit = (int) (40 * widthScaling);
	int hDigit = (int) (40 * heightScaling);
	int numberFontsize = (int) (24 * heightScaling);
	int guessFontsize = (int) (12 * heightScaling);
	int digitFontsize = (int) (24 * heightScaling);
	int fontsize = (int) (16 * heightScaling);
	boolean diagonalOn = false;
	
	boolean showBoxMsg = true;
	

	String lastUsedPath = "";
	
	
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
	
	public String lineForUserInput() {
        String lineUserInput = "";
        for (int row = 0; row < rows; row++) {
        	for (int col = 0; col < cols; col++) {
        		lineUserInput += String.valueOf(userInput[row * cols + col]);
        	}
        	lineUserInput += "\n";
        }
        return lineUserInput;
	}
	
	public String lineForBorder() {
		String lineBorder = "";
        for (int row = 0; row < rows; row++) {
        	for (int col = 0; col < cols; col++) {
        		lineBorder += String.valueOf(border[row * cols + col]);
        	}
        	lineBorder += "\n";
        }
        return lineBorder;
	}
	
	public String lineForDiagonal() {
        String lineDiagonal = "";
        if (diagonalOn) {
        	lineDiagonal = "Yes\n";
        } else {
        	lineDiagonal = "No\n";
        }
        return lineDiagonal;
	}

	public String lineForRelationship() {
		String lineRelationship = "";
		for(String relationshipCell : sizeRelationships){
        	lineRelationship += relationshipCell + "\n";
		}
        for (int row = 0; row < rows; row++) {
        	for (int col = 0; col < cols; col++) {
        		int numCell = row * cols + cols;
        		for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
        			for (int colOffset = -1; colOffset <= 1; colOffset++) {
        		        int newCell = (numCell / cols + rowOffset) * cols + numCell % cols + colOffset;
        				if (!neighbourCheck(numCell, newCell)) {
        					continue;
        				}
        				String relationshipCell = String.valueOf(numCell) + " " + String.valueOf(newCell);
        				if (sizeRelationships.contains(relationshipCell)) {
        				}
        			}
        		}
        	}
        }
        return lineRelationship;
	}
	
	 public void writeToFile(String filename) {
		  try {
	        FileWriter myWriter;
	        if (filename == "") {
	    	  myWriter = new FileWriter(testFile());
	        } else {
	    	  myWriter = new FileWriter(filename);
	        }
	        myWriter.write(lineForUserInput() + lineForBorder() + lineForDiagonal() + lineForRelationship());
	        myWriter.close();
	        if (filename == "") {
	        	InformationBox.infoBox("Zagonetka je uspješno spremljena.", "Spremanje datoteke");
	        }
	      } catch (IOException e) {
	        //System.out.println("An error occurred.");
	        e.printStackTrace();
	      }
	  }
	 
	 public int readFile(String filename) {
		    try {
		      File myObj;
		      if (filename == "") {
			      myObj = new File(testFile());
		      } else {
		    	  myObj = new File(filename);
		      }
		      java.util.Scanner myReader = new java.util.Scanner(myObj);
	          int lineNum = 0;
	          int newCols = 0;
	          ArrayList<String> data = new ArrayList<String>();
		      while (myReader.hasNextLine()) {
		    	    data.add(myReader.nextLine());
				    lineNum++;
		      }
		      myReader.close(); 
		      newCols = data.get(0).length();
		      if (newCols != cols) {
				  InformationBox.infoBox("Dimenzije zagonetke u datoteci ne odgovaraju vašem dizajnu.", "Uèitavanje datoteke");
				  return 1;
		      }
		      if (lineNum < cols * 2 || lineNum == 0) {
				  InformationBox.infoBox("Sadržaj datoteke je neispravan.", "Uèitavanje datoteke");
		    	  return 1;
		      }
		      rows = newCols;
		      cols = newCols;
	          diagonalOn = false;
	          sizeRelationships.clear();
		      for (int row = 0; row < lineNum; row++) {
		        	if (row < rows) {
			        	for (int col = 0; col < cols; col++) {
			        		userInput[row * cols + col] = Integer.parseInt(data.get(row).substring(col, col + 1));
			        	}
		        	} 
		        	if (row >= rows && row < rows * 2) {
		        		for (int col = 0; col < cols; col++) {
			        		border[(row - rows) * cols + col] = Integer.parseInt(data.get(row).substring(col, col + 1));
		        		}
		        	}
		        	if (row == rows * 2) {
		        		String reply = data.get(row).replace("\n", "");
		        		if (reply.compareTo("Yes") == 0) {
		        			diagonalOn = true;
		        		}
		        	}
		        	if (row > rows * 2) {
		        		String relationship = data.get(row).replace("\n", "");
		        		sizeRelationships.add(relationship);
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
		if (lastUsedPath != "") {
			if (InformationBox.yesNoBox("Želite li pristupiti zadnjoj korištenoj datoteci?", "Uèitavanje")) {
				return lastUsedPath;
			}
		} else {
			
		}
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
		lastUsedPath = newFile;
		return newFile;
	 }
	
	
	
	
	public String returnColour(int centerCell) {
	    String[] buttonColours = {"gray", "black", "dark_gray", "light_gray"};
		return buttonColours[border[centerCell]];
	}
	
	public int relationshipSmallerCheck(int neighbourCell, int centerCell) {
		if (!neighbourCheck(centerCell, neighbourCell)) {
			return 0;
		}
		String relationshipSmaller = String.valueOf(neighbourCell) + " " + String.valueOf(centerCell);
		if (sizeRelationships.contains(relationshipSmaller)) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public int relationshipLargerCheck(int neighbourCell, int centerCell) {
		if (!neighbourCheck(centerCell, neighbourCell)) {
			return 0;
		}
		String relationshipLarger = String.valueOf(centerCell) + " " + String.valueOf(neighbourCell);
		if (sizeRelationships.contains(relationshipLarger)) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public void setBackground(int row, int col, String colorButton) {
		int centerCell = row * cols + col;
		int leftBorderRelationship = relationshipSmallerCheck(centerCell - 1, centerCell);
	    int rightBorderRelationship = relationshipSmallerCheck(centerCell + 1, centerCell);
	    int topBorderRelationship = relationshipSmallerCheck(centerCell - cols, centerCell);
	    int bottomBorderRelationship = relationshipSmallerCheck(centerCell + cols, centerCell);
	    String path = "src/images/" + colorButton + "/arrow" + String.valueOf(leftBorderRelationship) + String.valueOf(rightBorderRelationship) + String.valueOf(topBorderRelationship) + String.valueOf(bottomBorderRelationship) + ".png";
	    ImageIcon imageIcon = new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(field[centerCell].getWidth(), field[centerCell].getHeight(), Image.SCALE_DEFAULT));
	    field[centerCell].setIcon(imageIcon);
	    field[centerCell].setDisabledIcon(imageIcon);
	    field[centerCell].setHorizontalTextPosition(JButton.CENTER);
	    field[centerCell].setVerticalTextPosition(JButton.CENTER);
	}
	
	public int getBorderThickness(int centerCell, int neighbourCell) {
		if (!neighbourCheck(centerCell, neighbourCell)) {
			return 4;
		}
		String relationshipSmaller = String.valueOf(neighbourCell) + " " + String.valueOf(centerCell);
		String relationshipLarger = String.valueOf(centerCell) + " " + String.valueOf(neighbourCell);
		if (border[neighbourCell] != border[centerCell] && !sizeRelationships.contains(relationshipSmaller) && !sizeRelationships.contains(relationshipLarger)) {
			return 3;
		}
		return 1;
	}
	
	public void setBorder(int row, int col) {
	    int centerCell = row * cols + col;
	    int leftBorder = getBorderThickness(centerCell, centerCell - 1);
	    int rightBorder = getBorderThickness(centerCell, centerCell + 1);
	    int topBorder = getBorderThickness(centerCell, centerCell - cols);
	    int bottomBorder = getBorderThickness(centerCell, centerCell + cols);
	    MatteBorder boxLimits = BorderFactory.createMatteBorder(topBorder, leftBorder, bottomBorder, rightBorder, Color.WHITE);	
	    if (diagonalOn && (row == col || row + col + 1 == cols)) {
	    	boxLimits = BorderFactory.createMatteBorder(3, 3, 3, 3, Color.MAGENTA);	
	    }
	    Border emptyBorder = BorderFactory.createEmptyBorder(7 - topBorder, 7 - leftBorder, 7 - bottomBorder, 7 - rightBorder);
	    CompoundBorder basicBorder = new CompoundBorder(boxLimits, emptyBorder);
		field[centerCell].setBorder(basicBorder);
	}
	
	public int floodFill(int row, int col, int val) {
		int retVal = 1;
	    int centerCell = row * cols + col;
	    setBackground(row, col, returnColour(centerCell));
	    setBorder(row, col);
	    boxNumber[centerCell] = val;
	    for (int rowOffset = -1; rowOffset <= 1; rowOffset++){ 
	    	for (int colOffset = -1; colOffset <= 1; colOffset++) {
	    		int neighbourCell = (row + rowOffset) * cols + col + colOffset;
	    		if (!neighbourCheck(centerCell, neighbourCell) || border[neighbourCell] != border[centerCell] || boxNumber[neighbourCell] != -1) {
	    			continue;
	    		}
	    		retVal += floodFill(row + rowOffset, col + colOffset, val);
	    	}
	    }
	    return retVal;
	}
	
	public boolean checkBoxes() {
		boxNumber = new int[rows * cols];
	    boolean borderNotSet = false;
	    boolean retVal = true;
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
		    	boxNumber[numCell] = -1;
		    	if (border[numCell] == -1) {
		    		borderNotSet = true;
		    	}
	    	}
	    }
	    if (borderNotSet) {
	    	retVal = false;
	    }
	    int boxNum = 0;
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
	    		if (boxNumber[numCell] != -1) {
	    			continue;
	    		}
	    		int sizeOfBox = floodFill(row, col, boxNum);
	    	    if (sizeOfBox > rows) {
	    	    	if (showBoxMsg) {
	    	    		InformationBox.infoBox(String.valueOf(boxNumber[numCell] + 1) + ". kutija je prevelika.", "Stvaranje kutije");	
	    	    	}
	    	    	retVal = false;
	    	    }
	    	    if (sizeOfBox < rows) {
	    	    	if (showBoxMsg) {
	    	    		InformationBox.infoBox(String.valueOf(boxNumber[numCell] + 1) + ". kutija je premalena", "Stvaranje kutije");	
	    	    	}
	    	    	retVal = false;
	    	    }
	    		boxNum++;
	    	}
	    }
	    if (boxNum > rows) {
	    	if (showBoxMsg) {
				InformationBox.infoBox("Previše kutija.", "Stvaranje kutije");
	    	}
	    	retVal = false;
	    }
	    if (boxNum < rows) {
	    	if (showBoxMsg) {
				InformationBox.infoBox("Premalo kutija", "Stvaranje kutije");
	    	}
	    	retVal = false;
	    }
	    return retVal;
	}
	
	
	public boolean isTreeSource(int numCell) {
		boolean isSource = false;
		for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
			for (int colOffset = -1; colOffset <= 1; colOffset++) {
		        int newCell = (numCell / cols + rowOffset) * cols + numCell % cols + colOffset;
		        if (relationshipLargerCheck(newCell, numCell) == 1) {
		        	isSource = true;
		        }
		        if (relationshipSmallerCheck(newCell, numCell) == 1) {
		        	return false;
		        }
			}
		}
		return isSource;
	}
	
	public boolean isTreeEnd(int numCell) {
		boolean isEnd = false;
		for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
			for (int colOffset = -1; colOffset <= 1; colOffset++) {
		        int newCell = (numCell / cols + rowOffset) * cols + numCell % cols + colOffset;
		        if (relationshipLargerCheck(newCell, numCell) == 1) {
					return false;
		        }
		        if (relationshipSmallerCheck(newCell, numCell) == 1) {
					isEnd = true;
		        }
			}
		}
		return isEnd;
	}

	public int isInTree(int numCell) {
		for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
			for (int colOffset = -1; colOffset <= 1; colOffset++) {
		        int newCell = (numCell / cols + rowOffset) * cols + numCell % cols + colOffset;
		        if (relationshipLargerCheck(newCell, numCell) == 1) {
					return 1;
		        }
		        if (relationshipSmallerCheck(newCell, numCell) == 1) {
					return 1;
		        }
			}
		}
		return 0;
	}
	
	public int getTreeSize(int numCell, Set<Integer> visited) {
		visited.add(numCell);
		int treeSize = 1;
		for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
			for (int colOffset = -1; colOffset <= 1; colOffset++) {
		        int newCell = (numCell / cols + rowOffset) * cols + numCell % cols + colOffset;
		        if (relationshipLargerCheck(newCell, numCell) == 1) {
		        	if (!visited.contains(newCell)) {
						treeSize += getTreeSize(newCell, visited);
					}
		        }
			}
		}
		return treeSize;
	}


	public int setMaxPossibility(int numCell, Set<Integer> visited) {
		visited.add(numCell);
		int numChanged = 0;
		Set<Integer> visitedNew = new HashSet<Integer>();
		int tree = getTreeSize(numCell, visitedNew);
		if (tree == 1) {
			return 0;
		}
		for (int val = 0; val < tree - 1; val++) {
			if (possibilities[numCell][val] == 1) {
				possibilities[numCell][val] = 0;
				numChanged++;
			}
		}
		int maxPossibility = 0;
		for (int val = 0; val < cols; val++) {
			if (possibilities[numCell][val] == 1) {
				maxPossibility = val;
			}
		}
		if (temporary[numCell] != 0) {
			maxPossibility = temporary[numCell] - 1;
		}
		for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
			for (int colOffset = -1; colOffset <= 1; colOffset++) {
		        int newCell = (numCell / cols + rowOffset) * cols + numCell % cols + colOffset;
				if (neighbourCheck(numCell, newCell)) {
					String relationshipCell = String.valueOf(numCell) + " " + String.valueOf(newCell);
					if (sizeRelationships.contains(relationshipCell)) {
						for (int val = maxPossibility; val < cols; val++) {
							if (possibilities[newCell][val] == 1) {
								possibilities[newCell][val] = 0;
								numChanged++;
							}
						}
						if (visited.contains(newCell)) {
							return 0;
						} else {
							numChanged += setMaxPossibility(newCell, visited);
						}
					} 
				}
			}
		}
		if (numChanged > 0) {
			//InformationBox.infoBox(String.valueOf(numCell) + ": "+ String.valueOf(maxPossibility), "max possibility");
			return 1;
		} else {
			return 0;
		}
	}

	public boolean neighbourCheck(int numCell, int newCell) {
		int rightCell = numCell + 1;
		int leftCell = numCell - 1;
		int bottomCell = numCell + cols;
		int topCell = numCell - cols;
		if ((newCell >= 0 && newCell < rows * cols) && (newCell == rightCell || newCell == leftCell || newCell == bottomCell || newCell == topCell) && (newCell / cols == numCell / cols || newCell % cols == numCell % cols)) {
			return true;
		}
		return false;
	}
	
	
	public int setMinPossibility(int numCell, Set<Integer> visited) {
		visited.add(numCell);
		int numChanged = 0;
		int minPossibility = 0;
		for (int val = 0; val < cols; val++) {
			if (possibilities[numCell][val] == 1) {
				minPossibility = val;
				break;
			}
		}
		if (temporary[numCell] != 0) {
			minPossibility = temporary[numCell] - 1;
		}
		for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
			for (int colOffset = -1; colOffset <= 1; colOffset++) {
		        int newCell = (numCell / cols + rowOffset) * cols + numCell % cols + colOffset;
				if (neighbourCheck(numCell, newCell)) {
					String relationshipCell = String.valueOf(newCell) + " " + String.valueOf(numCell);
					if (sizeRelationships.contains(relationshipCell)) {
						for (int val = 0; val <= minPossibility; val++) {
							if (possibilities[newCell][val] == 1) {
								possibilities[newCell][val] = 0;
								numChanged++;
							}
						}
						if (visited.contains(newCell)) {
							return 0;
						} else {
							numChanged += setMinPossibility(newCell, visited);
						}
					} 
				}
			}
		}
		if (numChanged > 0) {
			//InformationBox.infoBox(String.valueOf(numCell) + ": "+ String.valueOf(minPossibility), "min possibility");
			return 1;
		} else {
			return 0;
		}
	}
	
	
	/*public int getLargerTrees() {
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
	    		int numCell = row * cols + col;
	    		if (isTreeSource(numCell)) {
	    			int tree = getTreeSize(numCell);
	    			if (tree > cols) {
	    				return 1;
	    			}
	    		}
	    	}
	    }
	    return 0;
	}*/


	abstract public void draw();
	
	public SudokuGrid(int constructRows, int constructCols, int rowLimit, int colLimit) {
		rows = constructRows;
		cols = constructCols;
		xLim = rowLimit;
		yLim = colLimit;
		wNumber = (int) (800 / cols * widthScaling);
		hNumber = wNumber;
		wDigit = (int) (wNumber - wNumber / cols);
		int size = 0;
		numberFontsize = (int) (wNumber / 2);
		guessFontsize = 1;
		while (size / wNumber <= hNumber / guessFontsize) {
			size = guessFontsize;
			for (int col = 0; col < cols; col++) {
				size += (String.valueOf(col + 1).length() + 1) * guessFontsize;
			}
			guessFontsize++;
			if (size / wNumber > hNumber / guessFontsize) {
				guessFontsize--;
				break;
			}
		}
		field = new JButton[constructRows * constructCols];
		solution = new int[constructRows * constructCols];
		temporary = new int[constructRows * constructCols];
	    userInput = new int[constructRows * constructCols];
		border = new int[constructRows * constructCols];
		boxNumber = new int[constructRows * constructCols];
		sizeRelationships = new HashSet<String>();
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
	    		int numCell = row * cols + col;
	    		int box = (row / yLim) * (cols / xLim) + (col / xLim);
		    	if (((box % (cols / xLim) % 2 == 0) && (box / (cols / xLim) % 2  == 0)) || 
		    		((box % (cols / xLim) % 2 != 0) && (box / (cols / xLim) % 2  == 1))) {
	        		border[numCell] = 1;
		    	} else {
	        		border[numCell] = 0;
		    	}
	    	}
	    }
	}
	

	public abstract ActionListener makeActionListener(int numCell);
    public abstract int makeButtons();
    public abstract JButton makeAButton(String title, int xPosition, int yPosition, int widthButton, int heightButton, ActionListener actionListerToAdd);
	
}
