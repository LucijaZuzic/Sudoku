import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;


public abstract class Sudoku {
	JFrame frame;
	JButton digitButtons[];
	JTextArea errorArea;
	JTextArea instructionArea;
	JLabel difficulty = new JLabel("");
	JLabel penaltyLabel = new JLabel("");
	String solvingInstructions;

    int mintargetDifficulty = 0;
    int maxtargetDifficulty = 160000;
	int rows = 9;
	int cols = 9;
	int xLim = 3;
	int yLim = 3;
	int selectedDigit = 0;
	JButton[] field;
	int[] solution;
	int[] userInput;
	int[] temporary;
	int difficultyScore = 0;
	int numIter = 0;
	int[] border;
	int[] boxNumber;
	
	Stack<Integer> lastRemovedPosOriginal = new Stack<Integer>();
	Stack<Integer> lastRemovedValOriginal = new Stack<Integer>();
	Stack<Integer> lastRemovedPosSymetric = new Stack<Integer>();
	Stack<Integer> lastRemovedValSymetric = new Stack<Integer>();

	int numPossibilities[] = new int[rows * cols];
	int[][] possibilities = new int[rows * cols][rows];
	int changed = 1;
	int unset = 0;
	boolean showSteps = false;
	
	abstract boolean checkIfCorrect();
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
	
	public int fixPencilmarks() {
		int numChanged = 0;
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (temporary[row * cols + col] != 0) {
			    	for (int val = 0; val < cols; val++) {
			    		possibilities[row * cols + col][val] = 0;
				    }
		    		possibilities[row * cols + col][temporary[row * cols + col] - 1] = 1;
	    		} else {
	    			for (int fullCol = 0; fullCol < cols; fullCol++) {
			    		if (temporary[row * cols + fullCol] != 0 && possibilities[row * cols + col][temporary[row * cols + fullCol] - 1] == 1) {
				    		possibilities[row * cols + col][temporary[row * cols + fullCol] - 1] = 0;
		    				numChanged = 1;
			    		}
				    }
			    	for (int fullRow = 0; fullRow < rows; fullRow++) {
			    		if (temporary[fullRow * cols + col] != 0 && possibilities[row * cols + col][temporary[fullRow * cols + col] - 1] == 1) {
			    				possibilities[row * cols + col][temporary[fullRow * cols + col] - 1] = 0;
			    				numChanged = 1;
			    		}
				    }
			    	int box = boxNumber[row * cols + col];
				    for (int fullCol = 0; fullCol < rows; fullCol++){
					    for (int fullRow = 0; fullRow < cols; fullRow++){
				    		if (temporary[fullCol * cols + fullRow] != 0 && boxNumber[fullCol * cols + fullRow] == box && possibilities[row * cols + col][temporary[fullCol * cols + fullRow] - 1] == 1) {
			    				possibilities[row * cols + col][temporary[fullCol * cols + fullRow] - 1] = 0;
			    				numChanged = 1;
				    		}
				    	}
				    }
	    		}
	    	}
		}
	    return numChanged;
	}
	
	public int singleCandidate() {
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (temporary[row * cols + col] == 0) {
	    			int possibility = 0;
			    	for (int val = 0; val < cols; val++) {
			    		possibility += possibilities[row * cols + col][val];
				    }
			    	if (possibility == 1) {
			    		difficultyScore += 100 * 1;
			    		changed++;
			    		unset--;
				    	for (int val = 0; val < cols; val++) {
				    		if (possibilities[row * cols + col][val] == 1) {		
				    			solvingInstructions += "Broj " + String.valueOf(val + 1) + " je jedina moguæa vrijednost æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
				    			if (showSteps == true) {
		    		    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Broj " + String.valueOf(val + 1) + " je jedina moguæa vrijednost æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ")", "Rješavaè");
		    		    		}
			    		    	temporary[row * cols + col] = val + 1;
		    		    		field[row * cols + col].setForeground(Color.BLACK);
		    		    		field[row * cols + col].setText(String.valueOf(val + 1));
		    		    		fixPencilmarks();
		    		    		break;
				    		}
					    }
			    		if (unset == 0) {
			    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
			    			return 1;
			    		}
						if (sequence() == 1) {
			    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
			    			return 1;
						}

			    	}
	    		}
	    	}
	    }
	    return 0;
	}
	
	int dj2 = 0;
	int dj3 = 0;
	int dj4 = 0;
	int depthLimit;
	
	public void difficultySettingNaked(Set<Integer> setCells, String matchString, int containerNum, String containerType, int firstCell) {
		String setType = "Ogoljen";
		String lineSolvInstr = "";
		if (setCells.size() == 1) {
			lineSolvInstr += setType + "i par u " + containerType + " " + String.valueOf(containerNum + 1) + ", vrijednosti";
			int sizeOfSet = 0;
    		for (int valInSet = 0; valInSet < cols; valInSet++) {
    			if (matchString.charAt(valInSet) == '1') {
    				if (sizeOfSet > 0 && sizeOfSet != 1) {
    					lineSolvInstr += ",";
    				}
    				if (sizeOfSet == 1) {
    					lineSolvInstr += " i";
    				}
    				lineSolvInstr += " " + String.valueOf(valInSet + 1);
    				sizeOfSet++;
    			}
    		}
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (dj2 == 0) {
    				difficultyScore += 750;
    				dj2 = 1;
    			} else {
    				difficultyScore += 500;
    			}
    		} else {
    			return;
    		}
		}
		if (setCells.size() == 2) {
			lineSolvInstr += setType + "a trojka u " + containerType + " " + String.valueOf(containerNum + 1) + ", vrijednosti";
			int sizeOfSet = 0;
    		for (int valInSet = 0; valInSet < cols; valInSet++) {
    			if (matchString.charAt(valInSet) == '1') {
    				if (sizeOfSet > 0 && sizeOfSet != 2) {
    					lineSolvInstr += ",";
    				}
    				if (sizeOfSet == 2) {
    					lineSolvInstr += " i";
    				}
    				lineSolvInstr += " " + String.valueOf(valInSet + 1);
    				sizeOfSet++;
    			}
    		}
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (dj3 == 0) {
    				difficultyScore += 2000;
    				dj3 = 1;
    			} else {
    				difficultyScore += 1400;
    			}
    		} else {
    			return;
    		}
		}
		if (setCells.size() == 3) {
			lineSolvInstr += setType + "a èetvorka u " + containerType + " " + String.valueOf(containerNum + 1) + ", vrijednosti";
			int sizeOfSet = 0;
    		for (int valInSet = 0; valInSet < cols; valInSet++) {
    			if (matchString.charAt(valInSet) == '1') {
    				if (sizeOfSet > 0 && sizeOfSet != 3) {
    					lineSolvInstr += ",";
    				}
    				if (sizeOfSet == 3) {
    					lineSolvInstr += " i";
    				}
    				lineSolvInstr += " " + String.valueOf(valInSet + 1);
    				sizeOfSet++;
    			}
    		}
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (dj4 == 0) {
    				difficultyScore += 5000;
    				dj4 = 1;
    			} else {
    				difficultyScore += 4000;
    			}
    		} else {
    			return;
    		}
		}
		if (setCells.size() > 3) {
			lineSolvInstr += setType + "ih " + String.valueOf(setCells.size() + 1) + " u " + containerType + " " + String.valueOf(containerNum + 1) + ", vrijednosti";
			int sizeOfSet = 0;
    		for (int valInSet = 0; valInSet < cols; valInSet++) {
    			if (matchString.charAt(valInSet) == '1') {
    				if (sizeOfSet > 0 && sizeOfSet != setCells.size()) {
    					lineSolvInstr += ",";
    				}
    				if (sizeOfSet == setCells.size()) {
    					lineSolvInstr += " i";
    				}
    				lineSolvInstr += " " + String.valueOf(valInSet + 1);
    				sizeOfSet++;
    			}
    		}
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (dj4 == 0) {
    				difficultyScore += 5000;
    				dj4 = 1;
    			} else {
    				difficultyScore += 4000;
    			}
    		} else {
    			return;
    		}
		}
		lineSolvInstr += " u æelijama";
		int sizeOfCellSet = 0;
		if (containerType == "redu") {
			for (int possibleCol = 0; possibleCol < cols; possibleCol++) {
		    	int numCell = containerNum * cols + possibleCol;
				if (setCells.contains(numCell) || numCell == firstCell) {
					if (sizeOfCellSet > 0 && sizeOfCellSet != setCells.size()) {
						lineSolvInstr += ",";
					}
					if (sizeOfCellSet == setCells.size()) {
						lineSolvInstr += " i";
					}
					lineSolvInstr += " (" + String.valueOf(numCell / cols + 1) + ", " + String.valueOf(numCell % cols + 1) + ")";
					sizeOfCellSet++;
				}
			}
		}
		if (containerType == "stupcu") {
    		for (int possibleRow = 0; possibleRow < cols; possibleRow++) {
    	    	int numCell = possibleRow * cols + containerNum;
    			if (setCells.contains(numCell) || numCell == firstCell) {
    				if (sizeOfCellSet > 0 && sizeOfCellSet != setCells.size()) {
    					lineSolvInstr += ",";
    				}
    				if (sizeOfCellSet == setCells.size()) {
    					lineSolvInstr += " i";
    				}
    				lineSolvInstr += " (" + String.valueOf(numCell / cols + 1) + ", " + String.valueOf(numCell % cols + 1) + ")";
    				sizeOfCellSet++;
    			}
    		}
		}
		if (containerType == "kutiji") {
    		for (int possibleCell = 0; possibleCell < rows * cols; possibleCell++) {
		    	if (boxNumber[possibleCell] != boxNumber[firstCell]) {
		    		continue;
		    	}
    			if (setCells.contains(possibleCell) || possibleCell == firstCell) {
    				if (sizeOfCellSet > 0 && sizeOfCellSet != setCells.size()) {
    					lineSolvInstr += ",";
    				}
    				if (sizeOfCellSet == setCells.size()) {
    					lineSolvInstr += " i";
    				}
    				lineSolvInstr += " (" + String.valueOf(possibleCell / cols + 1) + ", " + String.valueOf(possibleCell % cols + 1) + ")";
    				sizeOfCellSet++;
    			}
    		}
		}
		lineSolvInstr += ".\n";
    	solvingInstructions += lineSolvInstr;
    	if (showSteps == true) {
		    instructionArea.setText(solvingInstructions);
			InformationBox.infoBox(lineSolvInstr, "Rješavaè");
		}
	}
	
	public int nakedSetForRow (int firstRow, int firstCol, Set<Integer> sameRowCells) {
		if (sameRowCells.size() >= depthLimit) {
			return 0;
		}
    	int firstCell = firstRow * cols + firstCol;
    	for (int nextColInRow = firstCol + 1; nextColInRow < cols; nextColInRow++) {
	    	int newToSet = firstRow * cols + nextColInRow;
	    	if (numPossibilities[newToSet] <= 1) {
	    		continue;
	    	}
    		int match = 0;
    		for (int previousColInRow = 0; previousColInRow < nextColInRow; previousColInRow++) {
		    	int alreadyInSet = firstRow * cols + previousColInRow;
		    	if (numPossibilities[alreadyInSet] <= 1) {
		    		continue;
		    	}
    			if (alreadyInSet == firstCell || sameRowCells.contains(alreadyInSet)) {
		    		for (int val = 0; val < cols; val++) {
		    			if (possibilities[newToSet][val] == 1 && possibilities[alreadyInSet][val] == 1 && !sameRowCells.contains(newToSet) && newToSet != firstCell) {
		    				match++;
		    				break;
		    			}
		    		}
    			}
    		}
    		if (match == sameRowCells.size() + 1) {
    			Set<Integer> sameRowCellsNextIteration = new HashSet<Integer>();
        		for (int numCell = 0; numCell < rows * cols; numCell++) {
        			if (sameRowCells.contains(numCell)) {
        				sameRowCellsNextIteration.add(numCell);
        			}
        		}
        		sameRowCellsNextIteration.add(newToSet);
				if (nakedSetForRow(firstRow, firstCol, sameRowCellsNextIteration) == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
    		}
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int val = 0; val < cols; val++) {
	    		String containsVal = "0";
	    		for (int col = 0; col < cols; col++) {
	    			int alreadyInSet = firstRow * cols + col;
	    			if (sameRowCells.contains(alreadyInSet) || alreadyInSet == firstCell) {
				    	if (possibilities[alreadyInSet][val] == 1) {
				    		containsVal = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsVal;
    		}
	    	if (sameRowCells.size() == matchStringLen - 1 && sameRowCells.size() > 0) {
			    int numRemoved = 0;
	    		for (int col = 0; col < cols; col++) {
			    	int notInSet = firstRow * cols + col;
			    	if (!sameRowCells.contains(notInSet) && notInSet != firstCell) {
			    		for (int val = 0; val < cols; val++) {
			    			if (matchString.charAt(val) == '1' && possibilities[notInSet][val] == 1 && temporary[notInSet] == 0) {
			    				if (numRemoved == 0) {
			    					difficultySettingNaked(sameRowCells, matchString, firstRow, "redu", firstCell);
			    				}
			    				solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").\n";
					    		if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").", "Rješavaè");
					    		}
								possibilities[notInSet][val] = 0;
								if (sequence() == 1) {
					    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					    			return 1;
								}
				    			numRemoved++;
			    			}
			    		}
			    	}
			    }
	    		if (numRemoved == 0) {
	    			continue;
	    		}
	    	}
    	}
		return 0;
	}
	
	public int nakedSetForCol (int firstRow, int firstCol, Set<Integer> sameColumnCells) {
		if (sameColumnCells.size() >= depthLimit) {
			return 0;
		}
		int firstCell = firstRow * cols + firstCol;
    	for (int nextRowInCol = firstRow + 1; nextRowInCol < rows; nextRowInCol++) {
	    	int newToSet = nextRowInCol * cols + firstCol;
	    	if (numPossibilities[newToSet] <= 1) {
	    		continue;
	    	}
    		int match = 0;
    		for (int previousRowInCol = 0; previousRowInCol < nextRowInCol; previousRowInCol++) {
		    	int alreadyInSet = previousRowInCol * cols + firstCol;
		    	if (numPossibilities[alreadyInSet] <= 1) {
		    		continue;
		    	}
    			if (alreadyInSet == firstCell || sameColumnCells.contains(alreadyInSet)) {
		    		for (int val = 0; val < cols; val++) {
		    			if (possibilities[newToSet][val] == 1 && possibilities[alreadyInSet][val] == 1 && !sameColumnCells.contains(newToSet) && newToSet != firstCell) {
		    				match++;
		    				break;
		    			}
		    		}
    			}
    		}
    		if (match == sameColumnCells.size() + 1) {
    			Set<Integer> sameColumnNextIteration = new HashSet<Integer>();
	    		for (int numCell = 0; numCell < rows * cols; numCell++) {
	    			if (sameColumnCells.contains(numCell)) {
	    				sameColumnNextIteration.add(numCell);
	    			}
	    		}
	    		sameColumnNextIteration.add(newToSet);
				if (nakedSetForCol(firstRow, firstCol, sameColumnNextIteration) == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
			}
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int val = 0; val < cols; val++) {
	    		String containsVal = "0";
	    		for (int row = 0; row < rows; row++) {
	    			int alreadyInSet = row * cols + firstCol;
	    			if (sameColumnCells.contains(alreadyInSet) || alreadyInSet == firstCell) {
				    	if (possibilities[alreadyInSet][val] == 1) {
				    		containsVal = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsVal;
    		}
	    	if (sameColumnCells.size() == matchStringLen - 1 && sameColumnCells.size() > 0) {
			    int numRemoved = 0;
	    		for (int row = 0; row < rows; row++) {
			    	int notInSet = row * cols + firstCol;
			    	if (!sameColumnCells.contains(notInSet) && notInSet != firstCell) {
			    		for (int val = 0; val < cols; val++) {
			    			if (matchString.charAt(val) == '1' && possibilities[notInSet][val] == 1 && temporary[notInSet] == 0) {
			    				if (numRemoved == 0) {
			    					difficultySettingNaked(sameColumnCells, matchString, firstCol, "stupcu", firstCell);
			    				}
			    				solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").\n";
			    				if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").", "Rješavaè");
					    		}
			    				possibilities[notInSet][val] = 0;
								if (sequence() == 1) {
					    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					    			return 1;
								}
			    				numRemoved++;
			    			}
			    		}
			    	}
			    }
	    		if (numRemoved == 0) {
	    			continue;
	    		}
	    	}
    	}
		return 0;
	}
	
	public int nakedSetForBox(int firstRow, int firstCol, Set<Integer> sameBoxCells) {
		if (sameBoxCells.size() >= depthLimit) {
			return 0;
		}
		int firstCell = firstRow * cols + firstCol;
    	for (int newToSet = firstCell + 1; newToSet < rows * cols; newToSet++) {
	    	if (boxNumber[newToSet] != boxNumber[firstCell]) {
	    		continue;
	    	}
	    	if (numPossibilities[newToSet] <= 1) {
	    		continue;
	    	}
	    	int match = 0;
	    	for (int alreadyInSet = 0; alreadyInSet < newToSet; alreadyInSet++) {
		    	if (boxNumber[alreadyInSet] != boxNumber[firstCell]) {
		    		continue;
		    	}
		    	if (numPossibilities[alreadyInSet] <= 1) {
		    		continue;
		    	}
    			if (alreadyInSet == firstCell || sameBoxCells.contains(alreadyInSet)) {
		    		for (int val = 0; val < cols; val++) {
		    			if (possibilities[newToSet][val] == 1 && possibilities[alreadyInSet][val] == 1 && !sameBoxCells.contains(newToSet) && newToSet != firstCell) {
		    				match++;
		    				break;
		    			}
		    		}
    			}
	    	}
    		if (match == sameBoxCells.size() + 1) {
    			Set<Integer> sameBoxNextIteration = new HashSet<Integer>();
	    		for (int numCell = 0; numCell < rows * cols; numCell++) {
	    			if (sameBoxCells.contains(numCell)) {
	    				sameBoxNextIteration.add(numCell);
	    			}
	    		}
	    		sameBoxNextIteration.add(newToSet);
				if (nakedSetForBox(firstRow, firstCol, sameBoxNextIteration) == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
    		}
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int val = 0; val < cols; val++) {
	    		String containsVal = "0";
	    		for (int numCell = 0; numCell < rows * cols; numCell++) {
			    	if (boxNumber[firstCell] != boxNumber[numCell]) {
			    		continue;
			    	}
	    			if (sameBoxCells.contains(numCell) || numCell == firstCell) {
				    	if (possibilities[numCell][val] == 1) {
				    		containsVal = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsVal;
    		}
	    	if (sameBoxCells.size() == matchStringLen - 1 && sameBoxCells.size() > 0) {
		    	int numRemoved = 0;
		    	for (int numCell = 0; numCell < rows * cols; numCell++) {
			    	int notInSet = numCell;
			    	if (boxNumber[firstCell] != boxNumber[notInSet]) {
			    		continue;
			    	}
			    	if (!sameBoxCells.contains(notInSet) && notInSet != firstCell) {
			    		for (int val = 0; val < cols; val++) {
			    			if (matchString.charAt(val) == '1' && possibilities[notInSet][val] == 1 && temporary[notInSet] == 0) {
			    				if (numRemoved == 0) {
			    					difficultySettingNaked(sameBoxCells, matchString, boxNumber[firstCell], "stupcu", firstCell);
			    				}
			    				solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").\n";
			    				if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").", "Rješavaè");
					    		}
			    				possibilities[notInSet][val] = 0;
								if (sequence() == 1) {
					    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					    			return 1;
								}
					    		numRemoved++;
			    			}
			    		}
			    	}
		    	}
	    		if (numRemoved == 0) {
	    			continue;
	    		}
	    	}
    	}
    	return 0;
	}
	
	public int nakedSet() {
	    numPossibilities = new int[rows * cols];
	    
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
		    	numPossibilities[numCell] = 0;
		    	for (int val = 0; val < cols; val++) {
		    		numPossibilities[numCell] += possibilities[row * cols + col][val];
			    }
	    	}
	    }
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
		    	if (numPossibilities[numCell] <= 1) {
		    		continue;
		    	}
		    	if (temporary[numCell] != 0) {
		    		continue;
		    	}
		    	Set<Integer> sameRow = new HashSet<Integer>();
		    	if (nakedSetForRow(row, col, sameRow) == 1) {
					difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					return 1;
				}

		    	Set<Integer> sameColumn = new HashSet<Integer>();
		    	if (nakedSetForCol(row, col, sameColumn) == 1) {
					difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					return 1;
				}

		    	Set<Integer> sameBox = new HashSet<Integer>();
		    	if (nakedSetForBox(row, col, sameBox) == 1) {
					difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					return 1;
		    	}
	    	}
	    }
		return 0;
	}
	
	String valuePos[] = new String[cols];


	int us2 = 0;
	int us3 = 0;
	int us4 = 0;
	
	public void difficultySettingHidden(Set<Integer> setValues, String matchString, int matchStringLen, int containerNum, String containerType, int firstVal) {
		String setType = "Skriven";
		String lineSolvInstr = "";
		if (setValues.size() == 1) {
			lineSolvInstr += setType + "i par u " + containerType + " " + String.valueOf(containerNum + 1) + ", vrijednosti";
			int sizeOfSet = 0;
    		for (int valInSet = 0; valInSet < cols; valInSet++) {
    			if (setValues.contains(valInSet) || valInSet == firstVal) {
    				if (sizeOfSet != 0) {
    					lineSolvInstr += " i";
    				}
    				lineSolvInstr += " " + String.valueOf(valInSet + 1);
    				sizeOfSet++;
    			}
    		}
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (us2 == 0) {
    				difficultyScore += 1500;
    				us2 = 1;
    			} else {
    				difficultyScore += 1200;
    			}
    		} else {
    			return;
    		}
		}
		if (setValues.size() == 2) {
			lineSolvInstr += setType + "a trojka u " + containerType + " " + String.valueOf(containerNum + 1) + ", vrijednosti";
			int sizeOfSet = 0;
    		for (int valInSet = 0; valInSet < cols; valInSet++) {
    			if (setValues.contains(valInSet) || valInSet == firstVal) {
    				if (sizeOfSet > 0 && sizeOfSet != 2) {
    					lineSolvInstr += ",";
    				}
    				if (sizeOfSet == 2) {
    					lineSolvInstr += " i";
    				}
    				lineSolvInstr += " " + String.valueOf(valInSet + 1);
    				sizeOfSet++;
    			}
    		}
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (us3 == 0) {
    				difficultyScore += 2400;
    				us3 = 1;
    			} else {
    				difficultyScore += 1600;
    			}
    		} else {
    			return;
    		}
		}
		if (setValues.size() == 3) {
			lineSolvInstr += setType + "a èetvorka u " + containerType + " " + String.valueOf(containerNum + 1) + ", vrijednosti";
			int sizeOfSet = 0;
    		for (int valInSet = 0; valInSet < cols; valInSet++) {
    			if (setValues.contains(valInSet) || valInSet == firstVal) {
    				if (sizeOfSet > 0 && sizeOfSet != 3) {
    					lineSolvInstr += ",";
    				}
    				if (sizeOfSet == 3) {
    					lineSolvInstr += " i";
    				}
    				lineSolvInstr += " " + String.valueOf(valInSet + 1);
    				sizeOfSet++;
    			}
    		}
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (us4 == 0) {
    				difficultyScore += 7000;
    				us4 = 1;
    			} else {
    				difficultyScore += 5000;
    			}
    		} else {
    			return;
    		}
		}
		if (setValues.size() > 3) {
			lineSolvInstr += setType + "ih " + String.valueOf(setValues.size() + 1) + " u " + containerType + " " + String.valueOf(containerNum + 1) + ", vrijednosti";
			int sizeOfSet = 0;
    		for (int valInSet = 0; valInSet < cols; valInSet++) {
    			if (setValues.contains(valInSet) || valInSet == firstVal) {
    				if (sizeOfSet > 0 && sizeOfSet != setValues.size()) {
    					lineSolvInstr += ",";
    				}
    				if (sizeOfSet == setValues.size()) {
    					lineSolvInstr += " i";
    				}
    				lineSolvInstr += " " + String.valueOf(valInSet + 1);
    				sizeOfSet++;
    			}
    		}
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (us4 == 0) {
    				difficultyScore += 7000;
    				us4 = 1;
    			} else {
    				difficultyScore += 5000;
    			}
    		} else {
    			return;
    		}
		}
		lineSolvInstr += " u æelijama";
		int sizeOfSet = 0;
		if (containerType == "redu") {
			for (int colInSet = 0; colInSet < cols; colInSet++) {
				if (matchString.charAt(colInSet) == '1') {
					if (sizeOfSet > 0 && sizeOfSet != matchStringLen - 1) {
						lineSolvInstr += ",";
					}
					if (sizeOfSet == matchStringLen - 1) {
						lineSolvInstr += " i";
					}
					lineSolvInstr += " (" + String.valueOf(containerNum + 1) + ", " + String.valueOf(colInSet + 1) + ")";
					sizeOfSet++;
				}
			}
		}
		if (containerType == "stupcu") {
			for (int rowInSet = 0; rowInSet < rows; rowInSet++) {
				if (matchString.charAt(rowInSet) == '1') {
					if (sizeOfSet > 0 && sizeOfSet != matchStringLen - 1) {
						lineSolvInstr += ",";
					}
					if (sizeOfSet == matchStringLen - 1) {
						lineSolvInstr += " i";
					}
					lineSolvInstr += " (" + String.valueOf(rowInSet + 1) + ", " + String.valueOf(containerNum + 1) + ")";
					sizeOfSet++;
				}
			}
		}
		if (containerType == "kutiji") {
		int matchPositionInBox = -1;
			for (int cell = 0; cell < rows * cols; cell++) {
	    		if (containerNum != boxNumber[cell]) {
	    			continue;
	    		} else {
	    			matchPositionInBox++;
	    		}
				if (matchString.charAt(matchPositionInBox) == '1') {
					if (sizeOfSet > 0 && sizeOfSet != matchStringLen - 1) {
						lineSolvInstr += ",";
					}
					if (sizeOfSet == matchStringLen - 1) {
						lineSolvInstr += " i";
					}
					lineSolvInstr += " (" + String.valueOf(cell / cols + 1) + ", " + String.valueOf(cell % cols + 1) + ")";
					sizeOfSet++;
				}
			}
		}

    	lineSolvInstr += ".\n";
    	solvingInstructions += lineSolvInstr;
    	if (showSteps == true) {
		    instructionArea.setText(solvingInstructions);
			InformationBox.infoBox(lineSolvInstr, "Rješavaè");
		}
	}
	
	public int hiddenSetForRow(int firstVal, int firstRow, Set<Integer> sameRowValues) {
		if (sameRowValues.size() >= depthLimit) {
			return 0;
		}
		for (int newToSet = firstVal + 1; newToSet < cols; newToSet++) {
			if (newToSet == firstVal || sameRowValues.contains(newToSet)) {
    			continue;
    		}
    		int match = 0;
    		for (int alreadyInSet = 0; alreadyInSet < cols; alreadyInSet++) {
    			if (alreadyInSet == firstVal || sameRowValues.contains(alreadyInSet)) {
		    		for (int positionInRow = 0; positionInRow < cols; positionInRow++) {
		    			if (valuePos[newToSet].charAt(positionInRow) == '1' && valuePos[alreadyInSet].charAt(positionInRow) == '1') {
		    				match++;
		    				break;
		    			}
		    		}
    			}
    		}
    		if (match == sameRowValues.size() + 1) {
    			Set<Integer> sameRowValuesNextIteration = new HashSet<Integer>();
        		for (int val = 0; val < cols; val++) {
        			if (sameRowValues.contains(val)) {
        				sameRowValuesNextIteration.add(val);
        			}
        		}
        		sameRowValuesNextIteration.add(newToSet);
				if (hiddenSetForRow(firstVal, firstRow, sameRowValuesNextIteration) == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
    		}
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int col = 0; col < cols; col++) {
	    		String containsCell = "0";
	    		for (int val = 0; val < cols; val++) {
	    			if (val == firstVal || sameRowValues.contains(val)) {
				    	if (valuePos[val].charAt(col) == '1') {
				    		containsCell = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsCell;
    		}
	    	if (sameRowValues.size() == matchStringLen - 1 && sameRowValues.size() > 0) {
	    		int numRemoved = 0;
				for (int col = 0; col < cols; col++) {
					if (matchString.charAt(col) == '1') {
						 for (int val = 0; val < cols; val++) {
							if (!sameRowValues.contains(val) && val != firstVal && possibilities[firstRow * cols + col][val] == 1) {
						    	if (numRemoved == 0) {
						    		difficultySettingHidden(sameRowValues, matchString, matchStringLen, firstRow, "redu", firstVal);
						    	}		    			    		
								solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(firstRow + 1) + ", " + String.valueOf(col + 1) + ").\n" ;
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(firstRow + 1) + ", " + String.valueOf(col + 1) + ").", "Rješavaè");
					    		}
			    				possibilities[firstRow * cols + col][val] = 0;
								if (sequence() == 1) {
					    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					    			return 1;
								}
					    		numRemoved++;
							} 
						}
					}
				}
	    		if (numRemoved == 0) {
	    			continue;
	    		}
	    	}
		}
		return 0;
	}
	
	public int hiddenSetForCol(int firstVal, int firstCol, Set<Integer> sameColValues) {	
		if (sameColValues.size() >= depthLimit) {
			return 0;
		}
		for (int newToSet = firstVal + 1; newToSet < cols; newToSet++) {
			if (newToSet == firstVal || sameColValues.contains(newToSet)) {
				continue;
			}
			int match = 0;
			for (int alreadyInSet = 0; alreadyInSet < cols; alreadyInSet++) {
				if (alreadyInSet == firstVal || sameColValues.contains(alreadyInSet)) {
		    		for (int positionInCol = 0; positionInCol < cols; positionInCol++) {
		    			if (valuePos[newToSet].charAt(positionInCol) == '1' && valuePos[alreadyInSet].charAt(positionInCol) == '1') {
		    				match++;
		    				break;
		    			}
		    		}
				}
			}
    		if (match == sameColValues.size() + 1) {
    			Set<Integer> sameColValuesNextIteration = new HashSet<Integer>();
        		for (int val = 0; val < cols; val++) {
        			if (sameColValues.contains(val)) {
        				sameColValuesNextIteration.add(val);
        			}
        		}
        		sameColValuesNextIteration.add(newToSet);
				if (hiddenSetForCol(firstVal, firstCol, sameColValuesNextIteration) == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
    		}
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int row = 0; row < rows; row++) {
	    		String containsCell = "0";
	    		for (int val = 0; val < cols; val++) {
	    			if (val == firstVal || sameColValues.contains(val)) {
				    	if (valuePos[val].charAt(row) == '1') {
				    		containsCell = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsCell;
    		}
	    	if (sameColValues.size() == matchStringLen - 1 && sameColValues.size() > 0) {
				int numRemoved = 0;
				for (int row = 0; row < rows; row++) {
					if (matchString.charAt(row) == '1') {
						for (int val = 0; val < cols; val++) {
							if (!sameColValues.contains(val) && val != firstVal && possibilities[row * cols + firstCol][val] == 1) {
						    	if (numRemoved == 0) {
						    		difficultySettingHidden(sameColValues, matchString, matchStringLen, firstCol, "stupcu", firstVal);
						    	}		    			    	    		
								solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(firstCol + 1) + ").\n";
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(firstCol + 1) + ").", "Rješavaè");
					    		}
								possibilities[row * cols + firstCol][val] = 0;
								if (sequence() == 1) {
					    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					    			return 1;
								}
			    				numRemoved++;
							}
						}
					}
				}
	    		if (numRemoved == 0) {
	    			continue;
	    		}
			}
		}
		return 0;
	}
	
	public int hiddenSetForBox(int firstVal, int firstBox, Set<Integer> sameBoxValues) {	
		if (sameBoxValues.size() >= depthLimit) {
			return 0;
		}
		for (int newToSet = firstVal + 1; newToSet < cols; newToSet++) {
			if (newToSet == firstVal || sameBoxValues.contains(newToSet)) {
				continue;
			}
			int match = 0;
			for (int alreadyInSet = 0; alreadyInSet < cols; alreadyInSet++) {
				if (alreadyInSet == firstVal || sameBoxValues.contains(alreadyInSet)) {
		    		for (int positionInBox = 0; positionInBox < cols; positionInBox++) {
		    			if (valuePos[newToSet].charAt(positionInBox) == '1' && valuePos[alreadyInSet].charAt(positionInBox) == '1') {
		    				match++;
		    				break;
		    			}
		    		}
				}
			}
    		if (match == sameBoxValues.size() + 1) {
    			Set<Integer> sameBoxValuesNextIteration = new HashSet<Integer>();
        		for (int val = 0; val < cols; val++) {
        			if (sameBoxValues.contains(val)) {
        				sameBoxValuesNextIteration.add(val);
        			}
        		}
        		sameBoxValuesNextIteration.add(newToSet);
				if (hiddenSetForBox(firstVal, firstBox, sameBoxValuesNextIteration) == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
			}
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int box = 0; box < cols; box++) {
	    		String containsCell = "0";
	    		for (int val = 0; val < cols; val++) {
	    			if (val == firstVal || sameBoxValues.contains(val)) {
				    	if (valuePos[val].charAt(box) == '1') {
				    		containsCell = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsCell;
    		}
	    	if (sameBoxValues.size() == matchStringLen - 1 && sameBoxValues.size() > 0) {
				int cellPositionInBox = -1;
				int numRemoved = 0;
				for (int numCell = 0; numCell < rows * cols; numCell++) {
		    		if (firstBox != boxNumber[numCell]) {
		    			continue;
		    		} else {
		    			cellPositionInBox++;
		    		}
					if (matchString.charAt(cellPositionInBox) == '1') {
						for (int notInSet = 0; notInSet < cols; notInSet++) {
							if (!sameBoxValues.contains(notInSet) && notInSet != firstVal && possibilities[numCell][notInSet] == 1) {
						    	if (numRemoved == 0) {
						    		difficultySettingHidden(sameBoxValues, matchString, matchStringLen, firstBox, "kutiji", firstVal);
						    	}		 
								solvingInstructions += "Uklanjam moguænost " + String.valueOf(notInSet + 1) + " iz æelije (" + String.valueOf(numCell / cols + 1) + ", " + String.valueOf(numCell % cols + 1) + ").\n";
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam moguænost " + String.valueOf(notInSet + 1) + " iz æelije (" + String.valueOf(numCell / cols + 1) + ", " + String.valueOf(numCell % cols + 1) + ").", "Rješavaè");
					    		}
								possibilities[numCell][notInSet] = 0;
								if (sequence() == 1) {
					    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					    			return 1;
								}
							    numRemoved++;
							}
						}
					}
				}
	    		if (numRemoved == 0) {
	    			continue;
	    		}
			}
		}
		return 0;
	}
	
	
	public int hiddenSet() {
	    for (int row = 0; row < rows; row++){
    		for (int val = 0; val < cols; val++) {
    			valuePos[val] = "";
    		}
	    	for (int col = 0; col < cols; col++) {
	    		for (int val = 0; val < cols; val++) {
	    			if (possibilities[row * cols + col][val] == 1 && temporary[row * cols + col] == 0) {
	    				valuePos[val] += "1";
	    			} else {
	    				valuePos[val] += "0";
	    			}
	    		}
		    }
			for (int val = 0; val < cols; val++) {
				Set<Integer> sameRowValues = new HashSet<Integer>();
				if (hiddenSetForRow(val, row, sameRowValues) == 1) {
					if (sequence() == 1) {
		    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
		    			return 1;
					}
				}
			}
		}

	    for (int col = 0; col < cols; col++){
    		for (int val = 0; val < rows; val++) {
    			valuePos[val] = "";
    		}
	    	for (int row = 0; row < rows; row++) {
	    		for (int val = 0; val < cols; val++) {
	    			if (possibilities[row * cols + col][val] == 1 && temporary[row * cols + col] == 0) {
	    				valuePos[val] += "1";
	    			} else {
	    				valuePos[val] += "0";
	    			}
	    		}
		    }
			for (int val = 0; val < cols; val++) {
				Set<Integer> sameColValues = new HashSet<Integer>();
				if (hiddenSetForCol(val, col, sameColValues) == 1) {
    				if (sequence() == 1) {
    	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
    	    			return 1;
    				}
				}
			}
		}

	    for (int box = 0; box < cols; box++){
			for (int val = 0; val < cols; val++) {
				valuePos[val] = "";
			}
	    	for (int numCell = 0; numCell < rows * cols; numCell++) {
	    		if (box != boxNumber[numCell]) {
	    			continue;
	    		}
	    		for (int val = 0; val < cols; val++) {
	    			if (possibilities[numCell][val] == 1 && temporary[numCell] == 0) {
	    				valuePos[val] += "1";
	    			} else {
	    				valuePos[val] += "0";
	    			}
	    		}
		    }
	    	
			for (int val = 0; val < cols; val++) {
				Set<Integer> sameBoxValues = new HashSet<Integer>();
				if (hiddenSetForBox(val, box, sameBoxValues) == 1) {
    				if (sequence() == 1) {
    	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
    	    			return 1;
    				}
				}
			}
	    }

		return 0;
	}
	
    int clt = 0;
	public int candidateLines() {
	    for (int box = 0; box < cols; box++){
	    	int valueRow[] = new int[cols];
	    	int valueCol[] = new int[cols];
	    	for (int val = 0; val < cols; val ++) {
	    		valueRow[val] = -1;
	    		valueCol[val] = -1;
	    	}
	    	for (int numCell = 0; numCell < rows * cols; numCell++) {
	    		int newBox = boxNumber[numCell];
	    		if (newBox != box) {
	    			continue;
	    		} 
	    		if (temporary[numCell] != 0) {
	    			continue;
	    		}
    	    	for (int val = 0; val < cols; val ++) {
    	    		if (possibilities[numCell][val] == 0) {
    	    			continue;
    	    		}
    	    		if (valueRow[val] == -1) {
    	    			valueRow[val] = numCell / cols;
    	    		} else {
    	    			if (valueRow[val] != numCell / cols) {
	    	    			valueRow[val] = -2;
    	    			}
    	    		}
    	    		if (valueCol[val] == -1) {
    	    			valueCol[val] = numCell % cols;
    	    		} else {
    	    			if (valueCol[val] != numCell % cols) {
    	    				valueCol[val] = -2;
    	    			}
    	    		}
    	    	}
	    	}
	    	for (int val = 0; val < cols; val ++) {
	    		if (valueRow[val] != -1 && valueRow[val] != -2) {
	    			int numRemoved = 0;
	    			for (int col = 0; col < cols; col++) {
	    				if (possibilities[valueRow[val] * cols + col][val] == 1 && boxNumber[valueRow[val] * cols + col] != box) {
	    					if (numRemoved == 0) {
	    			    		String lineSolvInstr = "Kandidati za " + String.valueOf(val + 1) + " u kutiji " + String.valueOf(box + 1) + " svi u redu " + String.valueOf(valueRow[val] + 1) + ".\n";
    				    		if (!solvingInstructions.contains(lineSolvInstr)) {
    				    			if (clt == 0) {
    	    		    				difficultyScore += 350;
    	    		    				clt = 1;
    	    		    			} else {
    	    		    				difficultyScore += 200;
    	    		    			}
    				    			solvingInstructions += lineSolvInstr;
							    	if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    	    			InformationBox.infoBox(lineSolvInstr, "Rješavaè");
						    		}
    				    		}
	    					}
	    					solvingInstructions +=  "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(valueRow[val] + 1) + ", " + String.valueOf(col + 1) + ").\n";
	    					if (showSteps == true) {
				    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			InformationBox.infoBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(valueRow[val] + 1) + ", " + String.valueOf(col + 1) + ").", "Rješavaè");
				    		}
		    				possibilities[valueRow[val] * cols + col][val] = 0;
				    		if (sequence() == 1) {
		    	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
		    	    			return 1;
		    				}
		    				numRemoved++;
						}
	    			}
	    		}
	    		if (valueCol[val] != -1 && valueCol[val] != -2) {
	    			int numRemoved = 0;
	    			for (int row = 0; row < rows; row++) {
	    				if (possibilities[row * cols + valueCol[val]][val] == 1 && boxNumber[row * cols + valueCol[val]] != box) {
	    	    			//System.out.println("Value " + String.valueOf(val) + " u kutiji " + String.valueOf(i) + " in col " + String.valueOf(valueCol[val]));
    	    				if (numRemoved == 0) {
    				    		String lineSolvInstr = "Kandidati za " + String.valueOf(val + 1) + " u kutiji " + String.valueOf(box + 1) + " svi su u stupcu " + String.valueOf(valueCol[val] + 1) + ".\n";
    			 				if (!solvingInstructions.contains(lineSolvInstr)) {
    				    			if (clt == 0) {
    	    		    				difficultyScore += 350;
    	    		    				clt = 1;
    	    		    			} else {
    	    		    				difficultyScore += 200;
    	    		    			}
    				    			solvingInstructions += lineSolvInstr;
							    	if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    	    			InformationBox.infoBox(lineSolvInstr, "Rješavaè");
						    		}
    				    		}
    	    				}
	    					solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(valueCol[val] + 1) + ").\n";
	    					if (showSteps == true) {
				    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			InformationBox.infoBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(valueCol[val] + 1) + ").", "Rješavaè");
				    		}
		    				possibilities[row * cols + valueCol[val]][val] = 0;
				    		if (sequence() == 1) {
		    	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
		    	    			return 1;
		    				}
		    				numRemoved++;
		    			}
					}
	    		}
	    	}
	    }

		return 0;
	}
	boolean widthLimit;
	int mlt = 0;
	int dpt = 0;
	public int multipleLines() {
    	Integer valueRow[][][] = new Integer[cols][cols][rows];
    	Integer valueCol[][][] = new Integer[cols][cols][cols];
    	String valueRowString[][] = new String[cols][cols];
    	String valueColString[][] = new String[cols][cols];
    	Integer valueRowVals[][] = new Integer[cols][cols];
    	Integer valueColVals[][] = new Integer[cols][cols];
    	Integer valueColCandidates[][] = new Integer[cols][cols];
    	Integer valueRowCandidates[][] = new Integer[cols][cols];
	    for (int numBox = 0; numBox < cols; numBox++){
	    	for (int val = 0; val < cols; val ++) {
	    		valueRowString[numBox][val] = "";
	    		valueColString[numBox][val] = "";
	    		valueRowVals[numBox][val] = 0;
	    		valueColVals[numBox][val] = 0;
	    		valueColCandidates[numBox][val] = 0;
	    		valueRowCandidates[numBox][val] = 0;
		    	for (int col = 0; col < cols; col ++) {
		    		valueRow[numBox][val][col] = 0;
		    		valueCol[numBox][val][col] = 0;
		    	}
		    }
	    	for (int numCell = 0; numCell < rows * cols; numCell++) {
	    		int box = boxNumber[numCell];
	    		if (box != numBox) {
	    			continue;
	    		} 
    	    	for (int val = 0; val < cols; val ++) {
    	    		if (temporary[numCell] != 0) {
    	    			continue;
    	    		}
    	    		if (possibilities[numCell][val] == 1) {
    		    		valueRow[box][val][numCell / cols] = 1;
    		    		valueRowCandidates[box][val]++;
    		    		valueCol[box][val][numCell % cols] = 1;
    		    		valueColCandidates[box][val]++;
    	    		}
    	    	}
	    	}
	    }

    	for (int numBox = 0; numBox < cols; numBox++) {
	    	for (int val = 0; val < cols; val ++) {
	    	    for (int col = 0; col < cols; col++){
	    	    	if (valueRow[numBox][val][col] == 1) {
	    	    		valueRowString[numBox][val] += "1";
    		    		valueRowVals[numBox][val]++;
	    	    	} else {
	    	    		valueRowString[numBox][val] += "0";
	    	    	}
	    	    	if (valueCol[numBox][val][col] == 1) {
	    	    		valueColString[numBox][val] += "1";
    		    		valueColVals[numBox][val]++;
	    	    	} else {
	    	    		valueColString[numBox][val] += "0";
	    	    	}
	    	    }
	    	}
    	}

    	for (int firstBox = 0; firstBox < cols; firstBox++) {
    		for (int val = 0; val < cols; val++){
				Set<Integer> sameRowBoxes = new HashSet<Integer>();
				Set<Integer> sameColBoxes = new HashSet<Integer>();
		    	for (int matchingBox = firstBox + 1; matchingBox < cols; matchingBox ++) {
			    	if (valueRowString[firstBox][val].compareTo(valueRowString[matchingBox][val]) == 0 && matchingBox != firstBox) {
						sameRowBoxes.add(matchingBox);
			    	}
			    	if (valueColString[firstBox][val].compareTo(valueColString[matchingBox][val]) == 0 && matchingBox != firstBox) {
						sameColBoxes.add(matchingBox);
			    	}
		    	}
		    	if (sameRowBoxes.size() == valueRowVals[firstBox][val] - 1 && sameRowBoxes.size() > 0 && ((widthLimit && valueRowCandidates[firstBox][val] == 2) || (!widthLimit && valueRowCandidates[firstBox][val] > 2))) {
		    		int numChanges = 0;
					for (int row = 0; row < rows; row++) {
						if (valueRow[firstBox][val][row] == 1) {
							for (int col = 0; col < cols; col++) {
								if (boxNumber[row * cols + col] != firstBox && !sameRowBoxes.contains(boxNumber[row * cols + col]) && possibilities[row * cols + col][val] == 1) {
									if (numChanges == 0) {
										String lineSolvInstr = "";
										if (valueRowCandidates[firstBox][val] == 2) {
											lineSolvInstr = "Dvostruki par u redovima";
										} else {
											lineSolvInstr = "Skup redova";
										}
							    		int sizeOfSet = 0;
							    		for (int rowWithValue = 0; rowWithValue < rows; rowWithValue++) {
							    			if (valueRowString[firstBox][val].charAt(rowWithValue) == '1') {
							    				if (sizeOfSet > 0 && sizeOfSet != sameRowBoxes.size()) {
							    					lineSolvInstr += ",";
							    				}
							    				if (sizeOfSet == sameRowBoxes.size()) {
							    					lineSolvInstr += " i";
							    				}
							    				lineSolvInstr += " " + String.valueOf(rowWithValue + 1);
							    				sizeOfSet++;
							    			}
							    	   }
							    	   lineSolvInstr += " sadrži " + String.valueOf(val + 1) + " u kutijama";
							    	   sizeOfSet = 0;
							    		for (int box = 0; box < cols; box++) {
							    			if (sameRowBoxes.contains(box) || box == firstBox) {
							    				if (sizeOfSet > 0 && sizeOfSet != sameRowBoxes.size()) {
							    					lineSolvInstr += ",";
							    				}
							    				if (sizeOfSet == sameRowBoxes.size()) {
							    					lineSolvInstr += " i";
							    				}
							    				lineSolvInstr += " " + String.valueOf(box + 1);
							    				sizeOfSet++;
							    			}
							    		}
							    		lineSolvInstr += ".\n";
							    		if (!solvingInstructions.contains(lineSolvInstr)) {
											if (valueRowCandidates[firstBox][val] == 2) {
								    			if (dpt == 0) {
								    				difficultyScore += 500;
								    				dpt = 1;
								    			} else {
								    				difficultyScore += 250;
								    			}

											} else {
								    			if (mlt == 0) {
								    				difficultyScore += 700;
								    				mlt = 1;
								    			} else {
								    				difficultyScore += 400;
								    			}
											}
		    				    			solvingInstructions += lineSolvInstr;
									    	if (showSteps == true) {
								    		    instructionArea.setText(solvingInstructions);
						    	    			InformationBox.infoBox(lineSolvInstr, "Rješavaè");
								    		}
		    				    		}
									}
			    					solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
									if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    		    		print();
				    	    			InformationBox.infoBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").", "Rješavaè");
						    		}
									possibilities[row * cols + col][val] = 0;
						    		numChanges++;
				    				if (sequence() == 1) {
				    	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
				    	    			return 1;
				    				}
								}
							}
						}
					}
		    	}		    	
		    	if (sameColBoxes.size() == valueColVals[firstBox][val] - 1 && sameColBoxes.size() > 0 && ((widthLimit && valueColCandidates[firstBox][val] == 2) || (!widthLimit && valueColCandidates[firstBox][val] > 2))) {
		    		int numChanges = 0;
					for (int col = 0; col < cols; col++) {
						if (valueCol[firstBox][val][col] == 1) {
							for (int row = 0; row < rows; row++) {
								if (boxNumber[row * cols + col] != firstBox && !sameColBoxes.contains(boxNumber[row * cols + col]) && possibilities[row * cols + col][val] == 1) {
									if (numChanges == 0) {
							    		String lineSolvInstr;
										if (valueColCandidates[firstBox][val] == 2) {
											lineSolvInstr = "Dvostruki par u stupcima";
										} else {
											lineSolvInstr = "Skup stupaca";
										}
							    		int sizeOfSet = 0;
							    		for (int colWithValue = 0; colWithValue < rows; colWithValue++) {
							    			if (valueColString[firstBox][val].charAt(colWithValue) == '1') {
							    				if (sizeOfSet > 0 && sizeOfSet != sameColBoxes.size()) {
							    					lineSolvInstr += ",";
							    				}
							    				if (sizeOfSet == sameColBoxes.size()) {
							    					lineSolvInstr += " i";
							    				}
							    				lineSolvInstr += " " + String.valueOf(colWithValue + 1);
							    				sizeOfSet++;
							    			}
								    	}
							    		lineSolvInstr += " sadrži " + String.valueOf(val + 1) + " u kutijama";
							    		sizeOfSet = 0;
							    		for (int box = 0; box < cols; box++) {
							    			if (sameColBoxes.contains(box) || box == firstBox) {
							    				if (sizeOfSet > 0 && sizeOfSet != sameRowBoxes.size()) {
							    					lineSolvInstr += ",";
							    				}
							    				if (sizeOfSet == sameRowBoxes.size()) {
							    					lineSolvInstr += " i";
							    				}
							    				lineSolvInstr += " " + String.valueOf(box + 1);
							    				sizeOfSet++;
							    			}
							    		}
							    		lineSolvInstr += ".\n";
							    		if (!solvingInstructions.contains(lineSolvInstr)) {
											if (valueColCandidates[firstBox][val] == 2) {
								    			if (dpt == 0) {
								    				difficultyScore += 500;
								    				dpt = 1;
								    			} else {
								    				difficultyScore += 250;
								    			}

											} else {
								    			if (mlt == 0) {
								    				difficultyScore += 700;
								    				mlt = 1;
								    			} else {
								    				difficultyScore += 400;
								    			}
											}
							    			solvingInstructions += lineSolvInstr;
									    	if (showSteps == true) {
								    		    instructionArea.setText(solvingInstructions);
						    	    			InformationBox.infoBox(lineSolvInstr, "Rješavaè");
								    		}
		    				    		}
						    		}
			    					solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
									if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    		    		print();
				    	    			InformationBox.infoBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").", "Rješavaè");
						    		}
									possibilities[row * cols + col][val] = 0;
						    		numChanges++;
				    				if (sequence() == 1) {
				    	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
				    	    			return 1;
				    				}
								}
							}
						}
					}
		    	}
    		}
    	}

		return 0;
	}
	
	public int singlePosition() {

		for (int val = 1; val <= rows; val++) {
			int[] usedRows = new int[rows];
			int[] usedCols = new int[cols];
			int[] usedBoxes = new int[rows * cols];
		    for (int row = 0; row < rows; row++){
		    	for (int col = 0; col < cols; col++) {
		    		usedRows[row] = 0;
		    		usedCols[col] = 0;
		    		usedBoxes[row] = 0;
			    }
		    }
		    for (int row = 0; row < rows; row++){
		    	for (int col = 0; col < cols; col++) {
		    		if (temporary[row * cols + col] == val) {
			    		usedRows[row]++;
			    		usedCols[col]++;
			    		usedBoxes[boxNumber[row * cols + col]]++;
		    		}
			    }
		    }
		    for (int row = 0; row < rows; row++){ 
		    	if (usedRows[row] == 1) {
		    		continue;
		    	}
		    	int possible = 0;
		    	int colToClear = 0;
		    	for (int col = 0; col < cols; col++) {
		    		int box = boxNumber[row * cols + col];
		    		if (usedRows[row] == 0 && usedCols[col] == 0 && usedBoxes[box] == 0 && possibilities[row * cols + col][val - 1] != 0 && temporary[row * cols + col] == 0) {
		    			possible++;
		    			colToClear = col;
		    		}
			    }
		    	if (possible == 1) {
		    		difficultyScore += 100 * 1;
		    		changed++;
		    		unset--;
			    	for (int k = 0; k < cols; k++) {
			    		possibilities[row * cols + colToClear][k] = 0;
				    }
			    	possibilities[row * cols + colToClear][val - 1] = 1;
			    	usedRows[row] = 1;
			    	usedCols[colToClear] = 1;
			    	int b = boxNumber[row * cols + colToClear];
			    	usedBoxes[b] = 1;
	    			solvingInstructions += "Za red " + String.valueOf(row + 1) + ", broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(row + 1) + ", " + String.valueOf(colToClear + 1) + ").\n";
		    		if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			InformationBox.infoBox("Za red " + String.valueOf(row + 1) + ", broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(row + 1) + ", " + String.valueOf(colToClear + 1) + ").", "Rješavaè");
		    		}
			    	temporary[row * cols + colToClear] = val;
    		    	field[row * cols + colToClear].setForeground(Color.BLACK);
		    		field[row * cols + colToClear].setText(String.valueOf(val));
		    		fixPencilmarks();
		    		if (unset == 0) {
		    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
		    			return 1;
		    		}
					if (sequence() == 1) {
		    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
		    			return 1;
					}
		    	} 
		    }
		    for (int col = 0; col < cols; col++){ 
		    	if (usedCols[col] == 1) {
		    		continue;
		    	}
		    	int possible = 0;
		    	int rowToClear = 0;
		    	for (int row = 0; row < rows; row++) {
		    		int box = boxNumber[row * cols + col];
		    		if (usedRows[row] == 0 && usedCols[col] == 0 && usedBoxes[box] == 0 && possibilities[row * cols + col][val - 1] != 0 && temporary[row * cols + col] == 0) {
		    			possible++;
		    			rowToClear = row;
		    		}
			    }
		    	if (possible == 1) {
		    		difficultyScore += 100 * 1;
		    		changed++;
		    		unset--;
			    	for (int k = 0; k < cols; k++) {
			    		possibilities[rowToClear * cols + col][k] = 0;
				    }
			    	possibilities[rowToClear * cols + col][val - 1] = 1;
			    	usedRows[rowToClear] = 1;
			    	usedCols[col] = 1;
			    	int boxToClear = boxNumber[rowToClear * cols + col];
			    	usedBoxes[boxToClear] = 1;
	    			solvingInstructions += "Za stupac " + String.valueOf(col + 1) + ", broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(rowToClear + 1) + ", " + String.valueOf(col + 1) + ").\n";
	    			if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			InformationBox.infoBox("Za stupac " + String.valueOf(col + 1) + ", broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(rowToClear + 1) + ", " + String.valueOf(col + 1) + ").", "Rješavaè");
		    		}
	    			temporary[rowToClear * cols + col] = val;
    		    	field[rowToClear * cols + col].setForeground(Color.BLACK);
		    		field[rowToClear * cols + col].setText(String.valueOf(val));
			    	fixPencilmarks();
		    		if (unset == 0) {
		    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
		    			return 1;
		    		}
					if (sequence() == 1) {
		    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
		    			return 1;
					}
		    	} 
		    }
		    for (int box = 0; box < cols; box++){ 
		    	if (usedBoxes[box] == 1) {
		    		continue;
		    	}
		    	int possible = 0;
		    	int cellToClear = 0;
		    	for (int numCell = 0; numCell < rows * cols; numCell++) {
		    		int newBox = boxNumber[numCell];
		    		if (newBox != box) {
		    			continue;
		    		}
		    		if (usedRows[numCell / cols] == 0 && usedCols[numCell % cols] == 0 && usedBoxes[newBox] == 0 && possibilities[numCell][val - 1] != 0 && temporary[numCell] == 0) {
		    			possible++;
		    			cellToClear = numCell;
		    		}
			    }
		    	if (possible == 1) {
		    		difficultyScore += 100 * 1;
		    		changed++;
		    		unset--;
			    	for (int k = 0; k < cols; k++) {
			    		possibilities[cellToClear][k] = 0;
				    }
			    	possibilities[cellToClear][val - 1] = 1;
			    	usedRows[cellToClear / cols] = 1;
			    	usedCols[cellToClear % cols] = 1;
			    	usedBoxes[boxNumber[cellToClear]] = 1;
	    			solvingInstructions += "Za kutiju " + String.valueOf(boxNumber[cellToClear] + 1) + ", broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").\n";
	    			if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			InformationBox.infoBox("Za kutiju " + String.valueOf(boxNumber[cellToClear] + 1) + ", broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").", "Rješavaè");
		    		}
	    			temporary[cellToClear] = val;
    		    	field[cellToClear].setForeground(Color.BLACK);
		    		field[cellToClear].setText(String.valueOf(val));
			    	fixPencilmarks();
		    		if (unset == 0) {
		    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
		    			return 1;
		    		}
		    	} 
		    }
		}
		return 0;
	}
	int maxDepth = 4;
	public int sequence() {
		if (singlePosition() == 1) {
			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
			return 1;
		}
		if (singleCandidate() == 1) {
			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
			return 1;
		}
		if (candidateLines() == 1) {
			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
			return 1;
		}
		widthLimit = true;
		if (multipleLines() == 1) {
			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
			return 1;
		}
		widthLimit = false;
		if (multipleLines() == 1) {
			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
			return 1;
		}
		if (multipleLines() == 1) {
			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
			return 1;
		}
		for (int depth = 2; depth <= maxDepth; depth++) {
			depthLimit = depth;
			if (nakedSet() == 1) {
				difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
				return 1;
			}
			if (hiddenSet() == 1) {
				difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
				return 1;
			}
		}
		depthLimit = cols;
		if (nakedSet() == 1) {
			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
			return 1;
		}
		if (hiddenSet() == 1) {
			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
			return 1;
		}
		return 0;
	}
	
	public int isOnlyOneSolution() {
		clt = 0;
		us2 = 0;
		us3 = 0;
		us4 = 0;
		dj2 = 0;
		mlt = 0;
		solvingInstructions = "";
		numIter = 0;
		difficultyScore = 0;
	    boolean correct = checkIfCorrect();
    	if (!correct) {
			difficulty.setText("U zagonetki ima grešaka");
    		return -1;
    	}
		unset = 0;
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
		    	temporary[numCell] = userInput[numCell];
	    		if (temporary[numCell] == 0) {
	    			unset++;
	    		}
	    	}
	    }
	    if (cols == 9 && rows * cols - unset < 17) {
			print();
			if (0 != unset) {
				sequence();
				print();
				difficulty.setText(String.valueOf(unset) + " Zadano je premalo polja");
				return 0;
			} 
	    }
		possibilities = new int[rows * cols][rows];
		//changed = 1;
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (temporary[row * cols + col] != 0) {
			    	for (int val = 0; val < cols; val++) {
			    		possibilities[row * cols + col][val] = 0;
				    }
		    		possibilities[row * cols + col][temporary[row * cols + col] - 1] = 1;
	    		} else {
			    	for (int val = 0; val < cols; val++) {
			    		possibilities[row * cols + col][val] = 1;
				    }
			    	for (int fullCol = 0; fullCol < cols; fullCol++) {
			    		if (temporary[row * cols + fullCol] != 0) {
				    		possibilities[row * cols + col][temporary[row * cols + fullCol] - 1] = 0;
			    		}
				    }
			    	for (int fullRow = 0; fullRow < rows; fullRow++) {
			    		if (temporary[fullRow * cols + col] != 0) {
				    		possibilities[row * cols + col][temporary[fullRow * cols + col] - 1] = 0;
			    		}
				    }
			    	int box = boxNumber[row * cols + col];
				    for (int fullBoxRow = 0; fullBoxRow < rows; fullBoxRow++){
					    for (int fullBoxCol = 0; fullBoxCol < cols; fullBoxCol++){
				    		if (temporary[fullBoxRow * cols + fullBoxCol] != 0 && boxNumber[fullBoxRow * cols + fullBoxCol] == box) {
					    		possibilities[row * cols + col][temporary[fullBoxRow * cols + fullBoxCol] - 1] = 0;
				    		}
				    	}
				    }
	    		}
	    	}
	    }
		//while (numIter < rows * cols && changed != 0) {
			//numIter++;
			changed = 0;
			//solvingInstructions += "Iteration number " + String.valueOf(numIter) + "\n";
			if (sequence() == 1 || unset == 0) {
				solvingInstructions += "Sva polja rješena.\n";
				difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
				return 1;
			}
			/*if (changed == 0 && unset != 0) {
				solvingInstructions += "Iteration number " + String.valueOf(numIter) + " didn't find anything.\n";
			}*/
		//}
	    /*print();
		if (0 != unset) {
			difficulty.setText(String.valueOf(unset) + " Ne postoji jedinstveno rješenje");
			return 0;
		} 
		
		difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");		
		return 1;*/
		solvingInstructions += "Poèinjem pogaðati.\n";
			//forcingChains();
		/*int g = guessing();
		numIter = 0;
		while (g == 1) {	
			g = guessing();
			numIter++;
			if (numIter == rows * cols) {
				break;
			}
		}*/
		print();
		if (0 != unset) {
			difficulty.setText(String.valueOf(unset) + " Ne postoji jedinstveno rješenje");
			return 0;
		} 
		difficulty.setText(String.valueOf(difficultyScore) + " Postoji verzija rješenja");		
		return 1;
	}
	
	public int guessing() {
		int[] backupTemporary = new int[rows * cols];
		int[][] backupPossibilities = new int[rows * cols][cols];
		int backupUnset = unset;
		int backupDifficultyScore = difficultyScore;
		String backupSolvingInstructions = solvingInstructions;
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		backupTemporary[row * cols + col] =  temporary[row * cols + col];
	    		for (int val = 0; val < cols; val++) {
	    			backupPossibilities[row * cols + col][val] = possibilities[row * cols + col][val];
	    		}
			}
		}
		for (int val = 0; val < rows; val++) {
		    for (int row = 0; row < rows; row++){
		    	int possible = 0;
		    	for (int col = 0; col < cols; col++) {
		    		if (possibilities[row * cols + col][val] == 1 && temporary[row * cols + col] == 0) {
		    			possible++;
		    		}
		    		if (temporary[row * cols + col] == val + 1) {
		    			possible = -1;
		    			break;
		    		}
			    }
		    	if (possible == -1) {
		    		continue;
		    	}
		    	if (possible == 0) {
				    for (int rowRestore = 0; rowRestore < rows; rowRestore++){
				    	for (int colRestore = 0; colRestore < cols; colRestore++) {
				    		temporary[rowRestore * cols + colRestore] = backupTemporary[rowRestore * cols + colRestore];
				    		for (int valRestore = 0; val < cols; val++) {
				    			possibilities[rowRestore * cols + colRestore][valRestore] = backupPossibilities[rowRestore * cols + colRestore][valRestore];
				    		}
	    				}
	    			}
				    difficultyScore = backupDifficultyScore;
				    solvingInstructions = backupSolvingInstructions;
				    unset = backupUnset;
		    		return 1;
		    	}
		    	int randomCol = ThreadLocalRandom.current().nextInt(0, cols);
		    	while (possibilities[row * cols + randomCol][val] == 0 || temporary[row * cols + randomCol] != 0) {
		    		randomCol = ThreadLocalRandom.current().nextInt(0, cols);
		    	}
				solvingInstructions += "Pokušavam " + String.valueOf(val + 1) + " u æeliji (" + String.valueOf(row + 1) + ", " + String.valueOf(randomCol + 1) + ").\n";
		    	temporary[row * cols + randomCol] = val + 1;
	    		for (int clearVal = 0; clearVal < cols; clearVal++) {
	    			possibilities[row * cols + randomCol][clearVal] = 0;
	    		}
    			possibilities[row * cols + randomCol][val] = 1;
		    	unset--;
				if (sequence() == 1) {
					return 0;
				}
		    }
		}
		return 0;
	}
	

	public int forcingChains() {
		int [][] pv = new int[rows * cols][cols];
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
    			for (int v = 0; v < cols; v++) {
    				pv[row * cols + col][v] = 0;
    			}
	    	}
	    }
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (temporary[row * cols + col] != 0) {
	    			for (int v = 0; v < cols; v++) {
	    				if (possibilities[row * cols + col][v] == 1) {
	    					int[] t2 = new int[rows * cols];
	    					int[][] v2 = new int[rows * cols][cols];
	    					int u1 = unset;
	    					int d1 = difficultyScore;
	    					String s = solvingInstructions;
	    				    for (int ix = 0; ix < rows; ix++){
	    				    	for (int jx = 0; jx < cols; jx++) {
	    				    		t2[ix * cols + jx] =  temporary[ix * cols + jx];
	    				    		for (int vx = 0; vx < cols; vx++) {
	    				    			v2[ix * cols + jx][vx] = possibilities[ix * cols + jx][vx];
	    				    		}
	    						}
	    					}
	    					temporary[row * cols + col] = v;
	    					sequence();
	    				    for (int i2 = 0; i2 < rows; i2++){
	    				    	for (int j2 = 0; j2 < cols; j2++) {
	    				    		if (temporary[i2 * cols + j2] != 0) {
	    				    			pv[i2 * cols + j2][temporary[i2 * cols + j2] - 1] = 1;
	    				    		}
	    				    	}
	    				    }
	    				    for (int ix = 0; ix < rows; ix++){
	    				    	for (int jx = 0; jx < cols; jx++) {
	    				    		temporary[ix * cols + jx] = t2[ix * cols + jx];
	    				    		for (int vx = 0; vx < cols; vx++) {
	    				    			possibilities[ix * cols + jx][vx] = v2[ix * cols + jx][vx];
	    				    		}
	    	    				}
	    	    			}
	    				    difficultyScore = d1;
	    				    solvingInstructions = s;
	    				    unset = u1;
	    				}
	    			}
				    for (int i2 = 0; i2 < rows; i2++){
				    	for (int j2 = 0; j2 < cols; j2++) {
				    		int opt = 0;
				    		int z = 0;
			    			for (int v = 0; v < cols; v++) {
			    				if (pv[i2 * cols + j2][v] == 1) {
			    					opt++;
			    					z = v + 1;
			    				}
			    			}
			    			if (opt == 1) {
			    				solvingInstructions += "Cell (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ") forces value in cell " + "(" + String.valueOf(i2 + 1) + ", " + String.valueOf(j2 + 1) + ") to be " + String.valueOf(z) + ".\n";
			    			}
				    	}
				    }
	    		}
		    }
		}
		return 0;
	}
	
	public void print() {
		for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
	    		String text = "<html>";
	    		int numberOptions = 0;
		    	for (int val = 0; val < cols; val++) {
		    		if (possibilities[row * cols + col][val] == 1 || temporary[row * cols + col] == val + 1) {
		    			numberOptions++;
		    			text += String.valueOf(val + 1);
		    			if (numberOptions % 3 == 0) {
		    				text += "<br />";
		    			} else {
		    				text += " ";
		    			}
		    		}
			    }
		    	if (numberOptions != 0) {
		    		text = text.substring(0, text.length() - 1) + "</html>";
		    	} else {
		    		text = "0";
		    	}
	    		field[numCell].setText(text);
	    		if (userInput[numCell] != temporary[numCell] || temporary[numCell] == 0) {
	    			field[numCell].setForeground(Color.RED);
	    		} else {
	    			field[numCell].setForeground(Color.WHITE);
	    		}
	    	}
	    }
	}
	
	public void removeSymetricPair() {
		boolean allEmpty = true;
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	        	int original = row * cols + col;
	    		if (temporary[original] > 0) {
	    			allEmpty = false;
	    		}
		    }
	    }
	    if (allEmpty) {
	    	return;
	    }
    	int randomCol = ThreadLocalRandom.current().nextInt(0, cols);
    	int randomRow = ThreadLocalRandom.current().nextInt(0, cols);
    	int original = randomRow * cols + randomCol;
    	int symetric = (rows - 1 - randomRow) * cols + (cols - 1 - randomCol);
    	while (temporary[original] == 0 && temporary[symetric] == 0) {
        	randomCol = ThreadLocalRandom.current().nextInt(0, cols);
        	randomRow = ThreadLocalRandom.current().nextInt(0, cols);
        	original = randomRow * cols + randomCol;
        	symetric = (rows - 1 - randomRow) * cols + (cols - 1 - randomCol);
    	}
    	lastRemovedPosOriginal.push(original);
    	lastRemovedValOriginal.push(userInput[original]);
    	numUseDigit[userInput[original]]--;
    	checkIfDigitMaxUsed(userInput[original]);
    	temporary[original] = 0;
		userInput[original] = temporary[original];
	    field[original].setText("0");
    	field[original].setForeground(Color.RED);
    	lastRemovedPosSymetric.push(symetric);
    	lastRemovedValSymetric.push(userInput[symetric]);
    	if (original != symetric) {
        	numUseDigit[userInput[symetric]]--;
        	checkIfDigitMaxUsed(userInput[symetric]);
    	}
    	temporary[symetric] = 0;
		userInput[symetric] = temporary[symetric];
	    field[symetric].setText("0");
    	field[symetric].setForeground(Color.RED);
	}

	public boolean restoreLastRemoved() {
		if (lastRemovedPosOriginal.isEmpty()) {
			return false;
		}
	    field[lastRemovedPosOriginal.peek()].setText(String.valueOf(lastRemovedValOriginal.peek()));
    	field[lastRemovedPosOriginal.peek()].setForeground(Color.BLACK);
	    field[lastRemovedPosSymetric.peek()].setText(String.valueOf(lastRemovedValSymetric.peek()));
    	field[lastRemovedPosSymetric.peek()].setForeground(Color.BLACK);
		userInput[lastRemovedPosOriginal.peek()] = lastRemovedValOriginal.peek();
    	numUseDigit[lastRemovedValOriginal.peek()]++;
    	checkIfDigitMaxUsed(lastRemovedValOriginal.peek());
		userInput[lastRemovedPosSymetric.peek()] = lastRemovedValSymetric.peek();
		if (lastRemovedPosSymetric.peek() != lastRemovedPosOriginal.peek()) {
	    	numUseDigit[lastRemovedValSymetric.peek()]++;
	    	checkIfDigitMaxUsed(lastRemovedValSymetric.peek());
		}
		solution[lastRemovedPosOriginal.peek()] = lastRemovedValOriginal.peek();
		solution[lastRemovedPosSymetric.peek()] = lastRemovedValSymetric.peek();
		temporary[lastRemovedPosOriginal.peek()] = lastRemovedValOriginal.peek();
		temporary[lastRemovedPosSymetric.peek()] = lastRemovedValSymetric.peek();
		lastRemovedPosOriginal.pop();
		lastRemovedValOriginal.pop();
		lastRemovedPosSymetric.pop();
		lastRemovedValSymetric.pop();	
		return true;
	}
	
	
	public int randomPuzzle() {
		for (int val = 1; val <= rows; val++) {
			int[] usedRows = new int[rows];
			int[] usedCols = new int[cols];
			int[] usedBoxes = new int[rows * cols];
		    for (int row = 0; row < rows; row++){
		    	for (int col = 0; col < cols; col++) {
		    		usedRows[row] = 0;
		    		usedCols[col] = 0;
		    		usedBoxes[row] = 0;
			    }
		    }
		    for (int row = 0; row < rows; row++){
		    	for (int col = 0; col < cols; col++) {
		    		if (temporary[row * cols + col] == val) {
			    		usedRows[row]++;
			    		usedCols[col]++;
			    		usedBoxes[boxNumber[row * cols + col]]++;
		    		}
			    }
		    }
		    for (int row = 0; row < rows; row++){ 
		    	if (usedRows[row] == 1) {
		    		continue;
		    	}
		    	int possible = 0;
		    	for (int col = 0; col < cols; col++) {
		    		int box = boxNumber[row * cols + col];
		    		if (usedBoxes[box] == 0 && usedCols[col] == 0 && temporary[row * cols + col] == 0) {
		    			possible++;
		    		}
			    }
		    	if (possible == 0) {
		    		return 1;
		    	}
		    	int randomCol = ThreadLocalRandom.current().nextInt(0, cols);
		    	int box = boxNumber[row * cols + randomCol];
		    	while (usedBoxes[box] == 1 || usedCols[randomCol] == 1 || temporary[row * cols + randomCol] != 0) {
			    	randomCol = ThreadLocalRandom.current().nextInt(0, cols);
			    	box = boxNumber[row * cols + randomCol];
		    	}
		    	usedCols[randomCol] = 1;
		    	usedBoxes[box] = 1;
		    	temporary[row * cols + randomCol] = val;
		    }
		}
		return 0;
	}
	
	public Sudoku(int constructRows, int constructCols, int rowLimit, int colLimit) {
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
	
	public void fill() {
	    boolean correct = checkIfCorrect();
    	if (!correct) {
    		return;
    	}
    	int numSols = isOnlyOneSolution();
    	if (numSols == 1) {
		    for (int row = 0; row < rows; row++){ 
		    	for (int col = 0; col < cols; col++) {
			    	int numCell = row * cols + col;
		    		userInput[numCell] = temporary[numCell];
		    		solution[numCell] = temporary[numCell];
		    		field[numCell].setText(String.valueOf(userInput[numCell]));
    	        	field[numCell].setForeground(Color.WHITE);
		    	}
		    }
    		return;
    	}
	    int retVal = randomPuzzle();
	    while (retVal == 1) {
		    for (int row = 0; row < rows; row++){ 
		    	for (int col = 0; col < cols; col++) {
			    	int numCell = row * cols + col;
			    	temporary[numCell] = userInput[numCell];
		    	}
		    }
		    retVal = randomPuzzle();
	    } 
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
	    		userInput[numCell] = temporary[numCell];
	    		solution[numCell] = temporary[numCell];
	    		field[numCell].setText(String.valueOf(userInput[numCell]));
	        	field[numCell].setForeground(Color.WHITE);
	    	}
	    }
	}

	abstract public void draw();
	
	int[] numUseDigit = new int[cols + 1];
	
	public void checkIfDigitMaxUsed(int digit) {
		if (digit == 0) {
			return;
		}
		if (numUseDigit[digit] >= cols) {
			digitButtons[digit].setForeground(Color.LIGHT_GRAY);
		} else {
			digitButtons[digit].setForeground(Color.BLACK);
		}
	}
}
