import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
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
	int numberFontsize = (int) (24 * heightScaling);
	int fontsize = (int) (16 * heightScaling);
	boolean diagonalOn = false;
	
	boolean showBoxMsg = true;
	

	public String returnColour(int centerCell) {
	    String colorButton = "";
        if (border[centerCell] == 3) {
    	    colorButton = "light_gray";
    	}
        if (border[centerCell] == 2) {
    	    colorButton = "dark_gray";
    	}
    	if (border[centerCell] == 1) {
    	    colorButton = "black";
    	}
        if (border[centerCell] == 0) {
    	    colorButton = "gray";
    	}
        if (border[centerCell] == -1) {
    	    colorButton = "gray";
    	}
		return colorButton;
	}
	
	public void setBackground(int row, int col, String colorButton) {
		int centerCell = row * cols + col;
		int leftBorderRelationship = 0;
	    int rightBorderRelationship = 0;
	    int topBorderRelationship = 0;
	    int bottomBorderRelationship = 0;
	    for (int rowOffset = -1; rowOffset < 2; rowOffset++){ 
	    	for (int colOffset = -1; colOffset < 2; colOffset++) {
	    		int neighbourCell = (row + rowOffset) * cols + col + colOffset;
	    		if (Math.abs(rowOffset) == Math.abs(colOffset)) {
	    			continue;
	    		}
	    		if (row + rowOffset < 0) {
	    			continue;
	    		}
	    		if (row + rowOffset >= rows) {
	    			continue;
	    		}
	    		if (col + colOffset < 0) {
	    			continue;
	    		}
	    		if (col + colOffset >= cols) {
	    			continue;
	    		}
				String relationshipSmaller = String.valueOf(neighbourCell) + " " + String.valueOf(centerCell);

	    		if (sizeRelationships.contains(relationshipSmaller)) {
		    		if (colOffset == -1) {
		    			leftBorderRelationship = 1;
		    		}
		    		if (colOffset == 1) {
		    			rightBorderRelationship = 1;
		    		}
		    		if (rowOffset == -1) {
		    			topBorderRelationship = 1;
		    		}
		    		if (rowOffset == 1) {
		    			bottomBorderRelationship = 1;
		    		}
	    		} 
	    	}
	    }
	    String path = "src/images/" + colorButton + "/arrow" + String.valueOf(leftBorderRelationship) + String.valueOf(rightBorderRelationship) + String.valueOf(topBorderRelationship) + String.valueOf(bottomBorderRelationship) + ".png";
	    ImageIcon imageIcon = new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(field[centerCell].getWidth(), field[centerCell].getHeight(), Image.SCALE_DEFAULT));
	    field[centerCell].setIcon(imageIcon);
	    field[centerCell].setDisabledIcon(imageIcon);
	}
	
	public int floodFill(int row, int col, int val) {
		int retVal = 1;
	    int centerCell = row * cols + col;
	    int leftBorder = 1;
	    int rightBorder = 1;
	    int topBorder = 1;
	    int bottomBorder = 1;
	    int leftBorderRelationship = 0;
	    int rightBorderRelationship = 0;
	    int topBorderRelationship = 0;
	    int bottomBorderRelationship = 0;
	    String colorButton = "";
		boxNumber[centerCell] = val;
        if (border[centerCell] == 3) {
    		field[centerCell].setBackground(Color.LIGHT_GRAY);
    	    colorButton = "light_gray";
    	}
        if (border[centerCell] == 2) {
    		field[centerCell].setBackground(Color.DARK_GRAY);
    	    colorButton = "dark_gray";
    	}
    	if (border[centerCell] == 1) {
    		field[centerCell].setBackground(Color.BLACK);
    	    colorButton = "black";
    	}
        if (border[centerCell] == 0) {
    		field[centerCell].setBackground(Color.GRAY);
    	    colorButton = "gray";
    	}
        if (border[centerCell] == -1) {
    		field[centerCell].setBackground(Color.RED);
    	}
	    for (int rowOffset = -1; rowOffset < 2; rowOffset++){ 
	    	for (int colOffset = -1; colOffset < 2; colOffset++) {
	    		int neighbourCell = (row + rowOffset) * cols + col + colOffset;
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
				String relationshipSmaller = String.valueOf(neighbourCell) + " " + String.valueOf(centerCell);
				String relationshipLarger = String.valueOf(centerCell) + " " + String.valueOf(neighbourCell);
	    		if (sizeRelationships.contains(relationshipSmaller)) {
		    		if (colOffset == -1) {
		    			leftBorderRelationship = 1;
		    		}
		    		if (colOffset == 1) {
		    			rightBorderRelationship = 1;
		    		}
		    		if (rowOffset == -1) {
		    			topBorderRelationship = 1;
		    		}
		    		if (rowOffset == 1) {
		    			bottomBorderRelationship = 1;
		    		}
	    		} 
	    		if (border[neighbourCell] != border[centerCell]) {
		    		if (colOffset == -1) {
			    		if (!sizeRelationships.contains(relationshipSmaller) && !sizeRelationships.contains(relationshipLarger)) {
			    			leftBorder = 3;
			    		}
		    			continue;
		    		}
		    		if (colOffset == 1) {
			    		if (!sizeRelationships.contains(relationshipSmaller) && !sizeRelationships.contains(relationshipLarger)) {
			    			rightBorder = 3;
			    		}
		    			continue;
		    		}
		    		if (rowOffset == -1) {
			    		if (!sizeRelationships.contains(relationshipSmaller) && !sizeRelationships.contains(relationshipLarger)) {
			    			topBorder = 3;
			    		}
		    			continue;
		    		}
		    		if (rowOffset == 1) {
			    		if (!sizeRelationships.contains(relationshipSmaller) && !sizeRelationships.contains(relationshipLarger)) {
			    			bottomBorder = 3;
			    		}
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
	    MatteBorder boxLimits = BorderFactory.createMatteBorder(topBorder, leftBorder, bottomBorder, rightBorder, Color.WHITE);
	    Border emptyBorder = BorderFactory.createEmptyBorder(7 - topBorder, 7 - leftBorder, 7 - bottomBorder, 7 - rightBorder);
	    CompoundBorder basicBorder = new CompoundBorder(boxLimits, emptyBorder);
	    MatteBorder diagonalBorder = BorderFactory.createMatteBorder(3, 3, 3, 3, Color.MAGENTA);
	    if (!diagonalOn) {
	    	field[centerCell].setBorder(basicBorder);
	    } else {
	    	if (row == col || row + col + 1 == cols) {
		    	field[centerCell].setBorder(new CompoundBorder(basicBorder, diagonalBorder));
	    	} else {
		    	field[centerCell].setBorder(basicBorder);
	    	}
	    }

	    String path = "src/images/" + colorButton + "/arrow" + String.valueOf(leftBorderRelationship) + String.valueOf(rightBorderRelationship) + String.valueOf(topBorderRelationship) + String.valueOf(bottomBorderRelationship) + ".png";
	    ImageIcon imageIcon = new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(field[centerCell].getWidth(), field[centerCell].getHeight(), Image.SCALE_DEFAULT));
	    field[centerCell].setIcon(imageIcon);
	    field[centerCell].setDisabledIcon(imageIcon);
	    field[centerCell].setHorizontalTextPosition(JButton.CENTER);
	    field[centerCell].setVerticalTextPosition(JButton.CENTER);
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

	/*public int breakingRelationship(int numCell, int val) {
		int rightCell = numCell + 1;
		if (rightCell < rows * cols) {
			String relationshipRightCell = String.valueOf(numCell) + " " + String.valueOf(rightCell);
			String relationshipReverseRightCell = String.valueOf(rightCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipRightCell) && (temporary[rightCell] > val || val == 1)) {
				return 1;
			} 
			if (sizeRelationships.contains(relationshipReverseRightCell)  && ((temporary[rightCell] < val && temporary[rightCell] != 0) || val == 9)) {
				return 1;
			} 
		}
		int leftCell = numCell - 1;
		if (leftCell >= 0) {
			String relationshipLeftCell = String.valueOf(numCell) + " " + String.valueOf(leftCell);
			String relationshipReverseLeftCell = String.valueOf(leftCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipLeftCell)  && (temporary[leftCell] > val || val == 1)) {
				return 1;
			} 
			if (sizeRelationships.contains(relationshipReverseLeftCell) && ((temporary[leftCell] < val && temporary[leftCell] != 0) || val == 9)) {
				return 1;
			} 
		}
		int bottomCell = numCell + cols;
		if (bottomCell < rows * cols) {
			String relationshipBottomCell = String.valueOf(numCell) + " " + String.valueOf(bottomCell);
			String relationshipReverseBottomCell = String.valueOf(bottomCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipBottomCell) && (temporary[bottomCell] > val || val == 1)) {
				return 1;
			} 
			if (sizeRelationships.contains(relationshipReverseBottomCell) && ((temporary[bottomCell] < val && temporary[bottomCell] != 0) || val == 9)) {
				return 1;
			} 
		}
		int topCell = numCell - cols;
		if (topCell >= 0) {
			String relationshipTopCell = String.valueOf(numCell) + " " + String.valueOf(topCell);
			String relationshipReverseTopCell = String.valueOf(topCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipTopCell) && (temporary[topCell] > val || val == 1)) {
				return 1;
			} 
			if (sizeRelationships.contains(relationshipReverseTopCell) && ((temporary[topCell] < val && temporary[topCell] != 0) || val == 9)) {
				return 1;
			} 
		}
		return 0;
	}*/
	
	
	/*public boolean isTreeSource(int numCell) {
		boolean isSource = false;
		int rightCell = numCell + 1;
		if (rightCell < rows * cols) {
			String relationshipRightCell = String.valueOf(numCell) + " " + String.valueOf(rightCell);
			if (sizeRelationships.contains(relationshipRightCell)) {
				isSource = true;
			} 
			relationshipRightCell = String.valueOf(rightCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipRightCell)) {
				return false;
			} 
		}
		int leftCell = numCell - 1;
		if (leftCell >= 0) {
			String relationshipLeftCell = String.valueOf(numCell) + " " + String.valueOf(leftCell);
			if (sizeRelationships.contains(relationshipLeftCell)) {
				isSource = true;
			} 
			relationshipLeftCell = String.valueOf(leftCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipLeftCell)) {
				return false;
			} 
		}
		int bottomCell = numCell + cols;
		if (bottomCell < rows * cols) {
			String relationshipBottomCell = String.valueOf(numCell) + " " + String.valueOf(bottomCell);
			if (sizeRelationships.contains(relationshipBottomCell)) {
				isSource = true;
			} 
			relationshipBottomCell = String.valueOf(bottomCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipBottomCell)) {
				return false;
			} 
		}
		int topCell = numCell - cols;
		if (topCell >= 0) {
			String relationshipTopCell = String.valueOf(numCell) + " " + String.valueOf(topCell);
			if (sizeRelationships.contains(relationshipTopCell)) {
				isSource = true;
			} 
			relationshipTopCell = String.valueOf(topCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipTopCell)) {
				return false;
			} 
		}
		return isSource;
	}*/

	public int isInTree(int numCell) {
		int rightCell = numCell + 1;
		if (rightCell < rows * cols) {
			String relationshipRightCell = String.valueOf(numCell) + " " + String.valueOf(rightCell);
			if (sizeRelationships.contains(relationshipRightCell)) {
				return 1;
			} 
			relationshipRightCell = String.valueOf(rightCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipRightCell)) {
				return 1;
			} 
		}
		int leftCell = numCell - 1;
		if (leftCell >= 0) {
			String relationshipLeftCell = String.valueOf(numCell) + " " + String.valueOf(leftCell);
			if (sizeRelationships.contains(relationshipLeftCell)) {
				return 1;
			} 
			relationshipLeftCell = String.valueOf(leftCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipLeftCell)) {
				return 1;
			} 
		}
		int bottomCell = numCell + cols;
		if (bottomCell < rows * cols) {
			String relationshipBottomCell = String.valueOf(numCell) + " " + String.valueOf(bottomCell);
			if (sizeRelationships.contains(relationshipBottomCell)) {
				return 1;
			} 
			relationshipBottomCell = String.valueOf(bottomCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipBottomCell)) {
				return 1;
			} 
		}
		int topCell = numCell - cols;
		if (topCell >= 0) {
			String relationshipTopCell = String.valueOf(numCell) + " " + String.valueOf(topCell);
			if (sizeRelationships.contains(relationshipTopCell)) {
				return 1;
			} 
			relationshipTopCell = String.valueOf(topCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipTopCell)) {
				return 1;
			} 
		}
		return 0;
	}
	
	public int getTreeSize(int numCell) {
		int treeSize = 1;
		int rightCell = numCell + 1;
		if (rightCell < rows * cols) {
			String relationshipRightCell = String.valueOf(numCell) + " " + String.valueOf(rightCell);
			if (sizeRelationships.contains(relationshipRightCell)) {
				treeSize += getTreeSize(rightCell);
			} 
		}
		int leftCell = numCell - 1;
		if (leftCell >= 0) {
			String relationshipLeftCell = String.valueOf(numCell) + " " + String.valueOf(leftCell);
			if (sizeRelationships.contains(relationshipLeftCell)) {
				treeSize += getTreeSize(leftCell);
			} 
		}
		int bottomCell = numCell + cols;
		if (bottomCell < rows * cols) {
			String relationshipBottomCell = String.valueOf(numCell) + " " + String.valueOf(bottomCell);
			if (sizeRelationships.contains(relationshipBottomCell)) {
				treeSize += getTreeSize(bottomCell);
			} 
		}
		int topCell = numCell - cols;
		if (topCell >= 0) {
			String relationshipTopCell = String.valueOf(numCell) + " " + String.valueOf(topCell);
			if (sizeRelationships.contains(relationshipTopCell)) {
				treeSize += getTreeSize(topCell);
			} 
		}
		return treeSize;
	}


	public int setMaxPossibility(int numCell) {
		int numChanged = 0;
		int tree = getTreeSize(numCell);
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
		int rightCell = numCell + 1;
		if (rightCell < rows * cols) {
			String relationshipRightCell = String.valueOf(numCell) + " " + String.valueOf(rightCell);
			if (sizeRelationships.contains(relationshipRightCell)) {
				for (int val = maxPossibility; val < cols; val++) {
					if (possibilities[rightCell][val] == 1) {
						possibilities[rightCell][val] = 0;
						numChanged++;
					}
				}
				numChanged += setMaxPossibility(rightCell);
			} 
		}
		int leftCell = numCell - 1;
		if (leftCell >= 0) {
			String relationshipLeftCell = String.valueOf(numCell) + " " + String.valueOf(leftCell);
			if (sizeRelationships.contains(relationshipLeftCell)) {
				for (int val = maxPossibility; val < cols; val++) {
					if (possibilities[leftCell][val] == 1) {
						possibilities[leftCell][val] = 0;
						numChanged++;
					}
				}
				numChanged += setMaxPossibility(leftCell);
			} 
		}
		int bottomCell = numCell + cols;
		if (bottomCell < rows * cols) {
			String relationshipBottomCell = String.valueOf(numCell) + " " + String.valueOf(bottomCell);
			if (sizeRelationships.contains(relationshipBottomCell)) {
				for (int val = maxPossibility; val < cols; val++) {
					if (possibilities[bottomCell][val] == 1) {
						possibilities[bottomCell][val] = 0;
						numChanged++;
					}
				}
				numChanged += setMaxPossibility(bottomCell);
			} 
		}
		int topCell = numCell - cols;
		if (topCell >= 0) {
			String relationshipTopCell = String.valueOf(numCell) + " " + String.valueOf(topCell);
			if (sizeRelationships.contains(relationshipTopCell)) {
				for (int val = maxPossibility; val < cols; val++) {
					if (possibilities[topCell][val] == 1) {
						possibilities[topCell][val] = 0;
						numChanged++;
					}
				}
				numChanged += setMaxPossibility(topCell);
			} 
		}
		if (numChanged > 0) {
			//InformationBox.infoBox(String.valueOf(numCell) + ": "+ String.valueOf(maxPossibility), "max possibility");
			return 1;
		} else {
			return 0;
		}
	}


	public int setMinPossibility(int numCell) {
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
		int rightCell = numCell + 1;
		if (rightCell < rows * cols) {
			String relationshipRightCell = String.valueOf(rightCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipRightCell)) {
				for (int val = 0; val <= minPossibility; val++) {
					if (possibilities[rightCell][val] == 1) {
						possibilities[rightCell][val] = 0;
						numChanged++;
					}
				}
				numChanged += setMinPossibility(rightCell);
			} 
		}
		int leftCell = numCell - 1;
		if (leftCell >= 0) {
			String relationshipLeftCell = String.valueOf(leftCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipLeftCell)) {
				for (int val = 0; val <= minPossibility; val++) {
					if (possibilities[leftCell][val] == 1) {
						possibilities[leftCell][val] = 0;
						numChanged++;
					}
				}
				numChanged += setMinPossibility(leftCell);
			} 
		}
		int bottomCell = numCell + cols;
		if (bottomCell < rows * cols) {
			String relationshipBottomCell = String.valueOf(bottomCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipBottomCell)) {
				for (int val = 0; val <= minPossibility; val++) {
					if (possibilities[bottomCell][val] == 1) {
						possibilities[bottomCell][val] = 0;
						numChanged++;
					}
				}
				numChanged += setMinPossibility(bottomCell);
			} 
		}
		int topCell = numCell - cols;
		if (topCell >= 0) {
			String relationshipTopCell = String.valueOf(topCell) + " " + String.valueOf(numCell);
			if (sizeRelationships.contains(relationshipTopCell)) {
				for (int val = 0; val <= minPossibility; val++) {
					if (possibilities[topCell][val] == 1) {
						possibilities[topCell][val] = 0;
						numChanged++;
					}
				}
				numChanged += setMinPossibility(topCell);
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
		field = new JButton[constructRows * constructCols];
		solution = new int[constructRows * constructCols];
		temporary = new int[constructRows * constructCols];
	    userInput = new int[constructRows * constructCols];
		border = new int[constructRows * constructCols];
		boxNumber = new int[constructRows * constructCols];
		sizeRelationships = new HashSet<String>();
	}
	
}
