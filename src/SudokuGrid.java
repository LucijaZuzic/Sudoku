import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;

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

    int mintargetDifficulty = 0;
    int maxtargetDifficulty = 160000;

	int numPossibilities[] = new int[rows * cols];
	int[][] possibilities = new int[rows * cols][rows];
	int unset = 0;
	boolean showSteps = false;


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
	int numberFontsize = (int) (24 * heightScaling);
	int fontsize = (int) (16 * heightScaling);
	
	boolean showBoxMsg = true;
	public int floodFill(int row, int col, int val) {
		int retVal = 1;
	    int centerCell = row * cols + col;
	    int leftBorder = 1;
	    int rightBorder = 1;
	    int topBorder = 1;
	    int bottomBorder = 1;
		boxNumber[centerCell] = val;
        if (border[centerCell] == 3) {
    		field[centerCell].setBackground(Color.LIGHT_GRAY);
    	}
        if (border[centerCell] == 2) {
    		field[centerCell].setBackground(Color.DARK_GRAY);
    	}
    	if (border[centerCell] == 1) {
    		field[centerCell].setBackground(Color.BLACK);
    	}
        if (border[centerCell] == 0) {
    		field[centerCell].setBackground(Color.GRAY);
    	}
        if (border[centerCell] == -1) {
    		field[centerCell].setBackground(Color.RED);
    	}
	    for (int rowOffset = -1; rowOffset < 2; rowOffset++){ 
	    	for (int colOffset = -1; colOffset < 2; colOffset++) {
	    		if (Math.abs(rowOffset) == Math.abs(colOffset)) {
	    			continue;
	    		}
	    		if (row + rowOffset < 0) {
	    			topBorder = 4;
	    			continue;
	    		}
	    		if (row + rowOffset >= rows) {
	    			bottomBorder = 4;
	    			continue;
	    		}
	    		if (col + colOffset < 0) {
	    			leftBorder = 4;
	    			continue;
	    		}
	    		if (col + colOffset >= cols) {
	    			rightBorder = 4;
	    			continue;
	    		}
	    		int neighbourCell = (row + rowOffset) * cols + col + colOffset;
	    		if (border[neighbourCell] != border[centerCell]) {
		    		if (colOffset == -1) {
		    			leftBorder = 3;
		    			continue;
		    		}
		    		if (colOffset == 1) {
		    			rightBorder = 3;
		    			continue;
		    		}
		    		if (rowOffset == -1) {
		    			topBorder = 3;
		    			continue;
		    		}
		    		if (rowOffset == 1) {
		    			bottomBorder = 3;
		    			continue;
		    		}
	    		}
	    		if (boxNumber[neighbourCell] != -1) {
		    		if (colOffset == -1) {
		    			leftBorder = 1;
		    			continue;
		    		}
		    		if (colOffset == 1) {
		    			rightBorder = 1;
		    			continue;
		    		}
		    		if (rowOffset == -1) {
		    			topBorder = 1;
		    			continue;
		    		}
		    		if (rowOffset == 1) {
		    			bottomBorder = 1;
		    			continue;
		    		}
	    		}
	    		retVal += floodFill(row + rowOffset, col + colOffset, val);
	    	}
	    }
        field[centerCell].setBorder(BorderFactory.createMatteBorder(topBorder, leftBorder, bottomBorder, rightBorder, Color.WHITE));
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
	    	    		InformationBox.infoBox(String.valueOf(boxNumber[numCell]) + ". kutija je prevelika.", "Stvaranje kutije");	
	    	    	}
	    	    	retVal = false;
	    	    }
	    	    if (sizeOfBox < rows) {
	    	    	if (showBoxMsg) {
	    	    		InformationBox.infoBox(String.valueOf(boxNumber[numCell]) + ". kutija je premalena", "Stvaranje kutije");	
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
	
	

	abstract public void draw();
	
	public SudokuGrid(int constructRows, int constructCols, int rowLimit, int colLimit) {
		rows = constructRows;
		cols = constructCols;
		xLim = rowLimit;
		yLim = colLimit;
		field = new JButton[constructRows * constructCols];
		solution = new int[constructRows * constructCols];
		temporary = new int[constructRows * constructCols];
	    userInput = new int[constructRows * constructCols];
		border = new int[constructRows * constructCols];
		boxNumber = new int[constructRows * constructCols];
	}
	
}
