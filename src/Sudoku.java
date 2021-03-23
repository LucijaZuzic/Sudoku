import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public abstract class Sudoku {
	JFrame frame;
	JTextArea errorArea;
	JTextArea instructionArea;
	JFrame errorFrame;
	JFrame instructionFrame;
	JLabel difficulty = new JLabel("");
	JLabel penaltyLabel = new JLabel("");
	String solvingInstructions;

    int mintargetDifficulty = 11000;
    int maxtargetDifficulty = 16000;
	int rows = 9;
	int cols = 9;
	int xlim = 3;
	int ylim = 3;
	int selectedDigit = 0;
	JButton[] field;
	int[] solution;
	int[] userInput;
	int[] temporary;
	int difficultyScore = 0;
	int numIter = 0;
	int[] border;
	int[] boxNumber;
	
	Stack<Integer> lastRemovedPos1 = new Stack<Integer>();
	Stack<Integer> lastRemoved1 = new Stack<Integer>();
	Stack<Integer> lastRemovedPos2 = new Stack<Integer>();
	Stack<Integer> lastRemoved2 = new Stack<Integer>();
	
	abstract boolean checkIfCorrect();

	public int floodFill(int x, int y, int val) {
		int retVal = 1;
	    int numOld = x * cols + y;
	    int lb = 1;
	    int rb = 1;
	    int tb = 1;
	    int bb = 1;
		boxNumber[numOld] = val;
    	if (border[numOld] == 1) {
    		field[numOld].setBackground(Color.BLACK);
    	}
        if (border[numOld] == 0) {
    		field[numOld].setBackground(Color.GRAY);
    	}
        if (border[numOld] == -1) {
    		field[numOld].setBackground(Color.RED);
    	}
	    //field[numOld].setText(String.valueOf(val));
	    for (int i = -1; i < 2; i++){ 
	    	for (int j = -1; j < 2; j++) {
	    		if (Math.abs(i) == Math.abs(j)) {
	    			continue;
	    		}
	    		if (x + i < 0) {
	    			tb = 4;
	    			continue;
	    		}
	    		if (x + i >= rows) {
	    			bb = 4;
	    			continue;
	    		}
	    		if (y + j < 0) {
	    			lb = 4;
	    			continue;
	    		}
	    		if (y + j >= cols) {
	    			rb = 4;
	    			continue;
	    		}
	    		int num = (x + i) * cols + y + j;
	    		if (border[num] != border[numOld]) {
		    		if (j == -1) {
		    			lb = 3;
		    			continue;
		    		}
		    		if (j == 1) {
		    			rb = 3;
		    			continue;
		    		}
		    		if (i == -1) {
		    			tb = 3;
		    			continue;
		    		}
		    		if (i == 1) {
		    			bb = 3;
		    			continue;
		    		}
	    		}
	    		if (boxNumber[num] != -1) {
		    		if (j == -1) {
		    			lb = 1;
		    			continue;
		    		}
		    		if (j == 1) {
		    			rb = 1;
		    			continue;
		    		}
		    		if (i == -1) {
		    			tb = 1;
		    			continue;
		    		}
		    		if (i == 1) {
		    			bb = 1;
		    			continue;
		    		}
	    		}
	    		retVal += floodFill(x + i, y + j, val);
	    	}
	    }
        field[numOld].setBorder(BorderFactory.createMatteBorder(tb, lb, bb, rb, Color.WHITE));
	    return retVal;
	}
	
	public boolean checkBoxes() {
		boxNumber = new int[rows * cols];
	    boolean borderNotSet = false;
	    boolean retVal = true;
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
		    	boxNumber[num] = -1;
		    	if (border[num] == -1) {
		    		borderNotSet = true;
		    	}
	    	}
	    }
	    if (borderNotSet) {
	    	System.out.println("Neke æelije nisu u kutiji");
	    	retVal = false;
	    }
	    int boxNum = 0;
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
	    		if (boxNumber[num] != -1) {
	    			continue;
	    		}
	    		int x = floodFill(i, j, boxNum);
	    	    if (x > rows) {
	    	    	System.out.println(String.valueOf(boxNumber[num]) + " Prevelika kutija");
	    	    	retVal = false;
	    	    }
	    	    if (x < rows) {
	    	    	System.out.println(String.valueOf(boxNumber[num]) + "Premalena kutija");
	    	    	retVal = false;
	    	    }
	    		boxNum++;
	    	}
	    }
	    if (boxNum > rows) {
	    	System.out.println("Previše kutija");
	    	retVal = false;
	    }
	    if (boxNum < rows) {
	    	System.out.println("Premalo kutija");
	    	retVal = false;
	    }
	    return retVal;
	}
	
	public int isOnlyOneSolution() {
		solvingInstructions = "";
		numIter = 0;
		difficultyScore = 0;
	    boolean correct = checkIfCorrect();
    	if (!correct) {
			difficulty.setText("U zagonetki ima grešaka");
    		return -1;
    	}
		int unset = 0;
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
		    	temporary[num] = userInput[num];
	    		if (temporary[num] == 0) {
	    			unset++;
	    		}
	    	}
	    }
	    
		int[][] possibilities = new int[rows * cols][rows];
		int changed = 1;
		while (changed != 0) {
			numIter++;
			changed = 0;
		    for (int i = 0; i < rows; i++){
		    	for (int j = 0; j < cols; j++) {
		    		if (temporary[i * cols + j] != 0) {
				    	for (int k = 0; k < cols; k++) {
				    		possibilities[i * cols + j][k] = 0;
					    }
			    		possibilities[i * cols + j][temporary[i * cols + j] - 1] = 1;
		    		} else {
				    	for (int k = 0; k < cols; k++) {
				    		possibilities[i * cols + j][k] = 1;
					    }
				    	for (int k = 0; k < cols; k++) {
				    		if (temporary[i * cols + k] != 0) {
					    		possibilities[i * cols + j][temporary[i * cols + k] - 1] = 0;
				    		}
					    }
				    	for (int k = 0; k < rows; k++) {
				    		if (temporary[k * cols + j] != 0) {
					    		possibilities[i * cols + j][temporary[k * cols + j] - 1] = 0;
				    		}
					    }
				    	int b = boxNumber[i * cols + j];
					    for (int x = 0; x < rows; x++){
						    for (int y = 0; y < cols; y++){
					    		if (temporary[x * cols + y] != 0 && boxNumber[x * cols + y] == b) {
						    		possibilities[i * cols + j][temporary[x * cols + y] - 1] = 0;
					    		}
					    	}
					    }
		    			int possibility = 0;
				    	for (int k = 0; k < cols; k++) {
				    		possibility += possibilities[i * cols + j][k];
					    }
				    	if (possibility == 1) {
				    		difficultyScore += 100 * 1;
				    		changed++;
				    		unset--;
					    	for (int k = 0; k < cols; k++) {
					    		if (possibilities[i * cols + j][k] == 1) {				    		
					    			solvingInstructions += "Single Candidate " + String.valueOf(k + 1) + " (" + String.valueOf(i + 1) + ", " + String.valueOf(j + 1) + ")\n";
					    			temporary[i * cols + j] = k + 1;
				    		    	field[i * cols + j].setForeground(Color.BLACK);
			    		    		field[i * cols + j].setText(String.valueOf(k + 1));
			    		    		break;
					    		}
						    }
				    		if (unset == 0) {
				    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
				    			return 1;
				    		}
				    	}
		    		}
		    	}
		    }
			for (int val = 1; val <= rows; val++) {
				int[] usedRows = new int[rows];
				int[] usedCols = new int[cols];
				int[] usedBoxes = new int[rows * cols];
			    for (int i = 0; i < rows; i++){
			    	for (int j = 0; j < cols; j++) {
			    		usedRows[i] = 0;
			    		usedCols[j] = 0;
			    		usedBoxes[i] = 0;
				    }
			    }
			    for (int i = 0; i < rows; i++){
			    	for (int j = 0; j < cols; j++) {
			    		if (temporary[i * cols + j] == val) {
				    		usedRows[i]++;
				    		usedCols[j]++;
				    		usedBoxes[boxNumber[i * cols + j]]++;
			    		}
			    		/*if (usedRows[i] > 1) {
			    			System.out.println("Zagonetka nije ispravno zadana");
			    			System.out.println("Broj " + val + " veæ postoji u retku " + i);
			    			return -1;
			    		}
			    		if (usedCols[j] > 1) {
			    			System.out.println("Zagonetka nije ispravno zadana");
			    			System.out.println("Broj " + val + " veæ postoji u stupcu " + j);
			    			return -1;
			    		}
			    		if (usedBoxes[(i / ylim) * (cols / xlim) + (j / xlim)] > 1) {
			    			System.out.println("Zagonetka nije ispravno zadana");
			    			System.out.println("Broj " + val + " veæ postoji u kutiji " + (i / ylim) * (cols / xlim) + (j / xlim));
			    			return -1;
			    		}*/
				    }
			    }
			    for (int i = 0; i < rows; i++){ 
			    	if (usedRows[i] == 1) {
			    		continue;
			    	}
			    	int possible = 0;
			    	int x = 0;
			    	for (int j = 0; j < cols; j++) {
			    		int b = boxNumber[i * cols + j];
			    		if (usedBoxes[b] == 0 && usedCols[j] == 0 && temporary[i * cols + j] == 0) {
			    			possible++;
			    			possibilities[i * cols + j][val - 1] = 1;
			    			x = j;
			    		}
				    }
			    	if (possible == 1) {
			    		difficultyScore += 100 * 1;
			    		changed++;
			    		unset--;
				    	for (int k = 0; k < cols; k++) {
				    		possibilities[i * cols + x][k] = 0;
					    }
				    	possibilities[i * cols + x][val - 1] = 1;
		    			solvingInstructions += "Single Position " + String.valueOf(val) + " (" + String.valueOf(i + 1) + ", " + String.valueOf(x + 1) + ")\n";
			    		temporary[i * cols + x] = val;
	    		    	field[i * cols + x].setForeground(Color.BLACK);
    		    		field[i * cols + x].setText(String.valueOf(val));
			    		if (unset == 0) {
			    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
			    			return 1;
			    		}
			    	} 
			    }
			}
		}
		changed = 1;
		while (changed != 0) {
			numIter++;
			changed = 0;
		    String possibilityString[] = new String[rows * cols];
		    int numPossibilities[] = new int[rows * cols];
		    for (int i = 0; i < rows; i++){
		    	for (int j = 0; j < cols; j++) {
			    	int num = i * cols + j;
			    	possibilityString[num] = "";
			    	numPossibilities[num] = 0;
			    	for (int k = 0; k < cols; k++) {
			    		possibilityString[num] += String.valueOf(possibilities[i * cols + j][k]);
			    		numPossibilities[num] += possibilities[i * cols + j][k];
				    }
		    	}
		    }
		    for (int i = 0; i < rows; i++){
		    	for (int j = 0; j < cols; j++) {
			    	int num = i * cols + j;
			    	
			    	Set<Integer> sameRow = new HashSet<Integer>();
			    	for (int k = i + 1; k < cols; k++) {
				    	int num2 = i * cols + k;
				    	if (num2 != num && possibilityString[num].compareTo(possibilityString[num2]) == 0 && numPossibilities[num] == numPossibilities[num2]) {
				    		//System.out.println(String.valueOf(num) + " row " + String.valueOf(num2));
					    	sameRow.add(num2);
				    	}
				    }
			    	//System.out.println(String.valueOf(sameRow.size()) + " actual " + String.valueOf(numPossibilities[num]));
			    	if (sameRow.size() == numPossibilities[num] - 1 && sameRow.size() != 0) {
			    		for (int fg = 0; fg < cols; fg++) {
					    	int num3 = i * cols + fg;
					    	if (!sameRow.contains(num3) && num3 != num) {
					    		for (int hg = 0; hg < cols; hg++) {
					    			if (possibilityString[num].charAt(hg) == '1' && possibilityString[num3].charAt(hg) == '1' && temporary[num3] == 0) {
					    				possibilities[num3][hg] = 0;
						    			int possibility = 0;
								    	for (int kh = 0; kh < cols; kh++) {
								    		possibility += possibilities[num3][kh];
									    }
								    	if (possibility == 1) {
								    		difficultyScore += 200 * 1;
								    		changed++;
								    		unset--;
									    	for (int kh = 0; kh < cols; kh++) {
									    		if (possibilities[num3][kh] == 1) {				    		
									    			solvingInstructions += "Hidden " + numPossibilities[num] + " " + String.valueOf(kh + 1) + " (" + String.valueOf(i + 1) + ", " + String.valueOf(fg + 1) + ")\n";
									    			temporary[num3] = kh + 1;
								    		    	field[num3].setForeground(Color.BLACK);
							    		    		field[num3].setText(String.valueOf(kh + 1));
							    		    		break;
									    		}
										    }
								    		if (unset == 0) {
								    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
								    			return 1;
								    		}
								    	}
					    			}
					    		}
					    	}
					    }
			    	}
			    	

			    	Set<Integer> sameColumn = new HashSet<Integer>();
			    	for (int k = j + 1; k < rows; k++) {
				    	int num2 = k * cols + j;
				    	if (num2 != num && possibilityString[num].compareTo(possibilityString[num2]) == 0 && numPossibilities[num] == numPossibilities[num2]) {
				    		//System.out.println(String.valueOf(num) + " column " + String.valueOf(num2));
				    		sameColumn.add(num2);
				    	}
				    }
			    	//System.out.println(String.valueOf(sameColumn.size()) + " actual " + String.valueOf(numPossibilities[num]));
			    	if (sameColumn.size() == numPossibilities[num] - 1 && sameColumn.size() != 0) {
			    		for (int fg = 0; fg < rows; fg++) {
					    	int num3 = fg * cols + j;
					    	if (!sameColumn.contains(num3) && num3 != num) {
					    		for (int hg = 0; hg < cols; hg++) {
					    			if (possibilityString[num].charAt(hg) == '1' && possibilityString[num3].charAt(hg) == '1' && temporary[num3] == 0) {
					    				possibilities[num3][hg] = 0;
						    			int possibility = 0;
								    	for (int kh = 0; kh < cols; kh++) {
								    		possibility += possibilities[num3][kh];
									    }
								    	if (possibility == 1) {
								    		difficultyScore += 200 * 1;
								    		changed++;
								    		unset--;
									    	for (int kh = 0; kh < cols; kh++) {
									    		if (possibilities[num3][kh] == 1) {				    		
									    			solvingInstructions += "Hidden " + numPossibilities[num] + " " + String.valueOf(kh + 1) + " (" + String.valueOf(fg + 1) + ", " + String.valueOf(j + 1) + ")\n";
									    			temporary[num3] = kh + 1;
								    		    	field[num3].setForeground(Color.BLACK);
							    		    		field[num3].setText(String.valueOf(kh + 1));
							    		    		break;
									    		}
										    }
								    		if (unset == 0) {
								    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
								    			return 1;
								    		}
								    	}
					    			}
					    		}
					    	}
					    }
			    	}

			    	Set<Integer> sameBox = new HashSet<Integer>();
			    	for (int k = num + 1; k < rows * cols; k++) {
				    	int num2 = k;
				    	if (boxNumber[num2] != boxNumber[num]) {
				    		continue;
				    	}
				    	if (num2 != num && possibilityString[num].compareTo(possibilityString[num2]) == 0 && numPossibilities[num] == 2 && numPossibilities[num2] == 2) {
				    		//System.out.println(String.valueOf(num) + " box " + String.valueOf(num2));
				    		sameBox.add(num2);
				    	}
				    }
			    	//System.out.println(String.valueOf(sameColumn.size()) + " actual " + String.valueOf(numPossibilities[num]));
			    	if (sameBox.size() == numPossibilities[num] - 1 && sameBox.size() != 0) {
				    	for (int fg = 0; fg < rows * cols; fg++) {
					    	int num3 = fg;
					    	if (boxNumber[num] != boxNumber[num3]) {
					    		continue;
					    	}
					    	if (!sameBox.contains(num3) && num3 != num) {
					    		for (int hg = 0; hg < cols; hg++) {
					    			if (possibilityString[num].charAt(hg) == '1' && possibilityString[num3].charAt(hg) == '1' && temporary[num3] == 0) {
					    				possibilities[num3][hg] = 0;
						    			int possibility = 0;
								    	for (int kh = 0; kh < cols; kh++) {
								    		possibility += possibilities[num3][kh];
									    }
								    	if (possibility == 1) {
								    		difficultyScore += 200 * 1;
								    		changed++;
								    		unset--;
									    	for (int kh = 0; kh < cols; kh++) {
									    		if (possibilities[num3][kh] == 1) {				    		
									    			solvingInstructions += "Hidden " + numPossibilities[num] + " " + String.valueOf(kh + 1) + " (" + String.valueOf(num3 / cols + 1) + ", " + String.valueOf(num3 % cols + 1) + ")\n";
									    			temporary[num3] = kh + 1;
								    		    	field[num3].setForeground(Color.BLACK);
							    		    		field[num3].setText(String.valueOf(kh + 1));
							    		    		break;
									    		}
										    }
								    		if (unset == 0) {
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
		    }
		}
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
	    		String t = "<html>";
	    		int z = 0;
		    	for (int k = 0; k < cols; k++) {
		    		if (possibilities[i * cols + j][k] == 1) {
		    			z++;
		    			t += String.valueOf(k + 1);
		    			if (z % 3 == 0) {
		    				t += "<br />";
		    			} else {
		    				t += " ";
		    			}
		    		}
			    }
		    	if (z != 0) {
		    		t = t.substring(0, t.length() - 1) + "</html>";
		    	} else {
		    		t = "0";
		    	}
	    		field[num].setText(t);
	    		if (userInput[num] != temporary[num] || temporary[num] == 0) {
	    			field[num].setForeground(Color.RED);
	    		} else {
	    			field[num].setForeground(Color.BLACK);
	    		}
	    		//userInput[num] = temporary[num];
	    		//solution[num] = temporary[num];
	    	}
	    }
	    instructionArea.setText(solvingInstructions);
		if (0 != unset) {
			difficulty.setText(String.valueOf(unset) + "Ne postoji jedinstveno rješenje");
			System.out.println(String.valueOf(unset) + "Ne postoji jedinstveno rješenje");
			return 0;
		} 
		difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");			
		System.out.println(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");	
		return 1;
	}
	
	public void removeSymetricPair() {
		boolean allEmpty = true;
	    for (int i = 0; i < rows; i++){
	    	for (int j = 0; j < cols; j++) {
	        	int num = i * cols + j;
	    		if (temporary[num] > 0) {
	    			allEmpty = false;
	    		}
		    }
	    }
	    if (allEmpty) {
	    	return;
	    }
    	int randomCol = ThreadLocalRandom.current().nextInt(0, cols);
    	int randomRow = ThreadLocalRandom.current().nextInt(0, cols);
    	int num = randomRow * cols + randomCol;
    	int num2 = (rows - 1 - randomRow) * cols + (cols - 1 - randomCol);
    	while (temporary[num] == 0 && temporary[num2] == 0) {
        	randomCol = ThreadLocalRandom.current().nextInt(0, cols);
        	randomRow = ThreadLocalRandom.current().nextInt(0, cols);
        	num = randomRow * cols + randomCol;
        	num2 = (rows - 1 - randomRow) * cols + (cols - 1 - randomCol);
    	}
    	lastRemovedPos1.push(num);
    	lastRemoved1.push(userInput[num]);
    	temporary[num] = 0;
		userInput[num] = temporary[num];
	    field[num].setText("0");
    	field[num].setForeground(Color.RED);
    	lastRemovedPos2.push(num2);
    	lastRemoved2.push(userInput[num2]);
    	temporary[num2] = 0;
		userInput[num2] = temporary[num2];
	    field[num2].setText("0");
    	field[num2].setForeground(Color.RED);
	}

	public boolean restoreLastRemoved() {
		if (lastRemovedPos1.isEmpty()) {
			return false;
		}
	    field[lastRemovedPos1.peek()].setText(String.valueOf(lastRemoved1.peek()));
    	field[lastRemovedPos1.peek()].setForeground(Color.BLACK);
	    field[lastRemovedPos2.peek()].setText(String.valueOf(lastRemoved2.peek()));
    	field[lastRemovedPos2.peek()].setForeground(Color.BLACK);
		userInput[lastRemovedPos1.peek()] = lastRemoved1.peek();
		userInput[lastRemovedPos2.peek()] = lastRemoved2.peek();
		solution[lastRemovedPos1.peek()] = lastRemoved1.peek();
		solution[lastRemovedPos2.peek()] = lastRemoved2.peek();
		temporary[lastRemovedPos1.peek()] = lastRemoved1.peek();
		temporary[lastRemovedPos2.peek()] = lastRemoved2.peek();
		lastRemovedPos1.pop();
		lastRemoved1.pop();
		lastRemovedPos2.pop();
		lastRemoved2.pop();	
		return true;
	}
	
	public int randomPuzzle() {
		for (int val = 1; val <= rows; val++) {
			int[] usedRows = new int[rows];
			int[] usedCols = new int[cols];
			int[] usedBoxes = new int[rows * cols];
		    for (int i = 0; i < rows; i++){
		    	for (int j = 0; j < cols; j++) {
		    		usedRows[i] = 0;
		    		usedCols[j] = 0;
		    		usedBoxes[i] = 0;
			    }
		    }
		    for (int i = 0; i < rows; i++){
		    	for (int j = 0; j < cols; j++) {
		    		if (temporary[i * cols + j] == val) {
			    		usedRows[i]++;
			    		usedCols[j]++;
			    		usedBoxes[boxNumber[i * cols + j]]++;
		    		}
		    		/*if (usedRows[i] > 1) {
		    			System.out.println("Zagonetka nije ispravno zadana");
		    			System.out.println("Broj " + val + " veæ postoji u retku " + i);
		    			return -1;
		    		}
		    		if (usedCols[j] > 1) {
		    			System.out.println("Zagonetka nije ispravno zadana");
		    			System.out.println("Broj " + val + " veæ postoji u stupcu " + j);
		    			return -1;
		    		}
		    		if (usedBoxes[(i / ylim) * (cols / xlim) + (j / xlim)] > 1) {
		    			System.out.println("Zagonetka nije ispravno zadana");
		    			System.out.println("Broj " + val + " veæ postoji u kutiji " + (i / ylim) * (cols / xlim) + (j / xlim));
		    			return -1;
		    		}*/
			    }
		    }
		    for (int i = 0; i < rows; i++){ 
		    	if (usedRows[i] == 1) {
		    		continue;
		    	}
		    	int possible = 0;
		    	for (int j = 0; j < cols; j++) {
		    		int b = boxNumber[i * cols + j];
		    		if (usedBoxes[b] == 0 && usedCols[j] == 0 && temporary[i * cols + j] == 0) {
		    			possible++;
		    		}
			    }
		    	if (possible == 0) {
		    		return 1;
		    	}
		    	int randomCol = ThreadLocalRandom.current().nextInt(0, cols);
		    	int box = boxNumber[i * cols + randomCol];
		    	while (usedBoxes[box] == 1 || usedCols[randomCol] == 1 || temporary[i * cols + randomCol] != 0) {
			    	randomCol = ThreadLocalRandom.current().nextInt(0, cols);
			    	box = boxNumber[i * cols + randomCol];
		    	}
		    	usedCols[randomCol] = 1;
		    	usedBoxes[box] = 1;
		    	temporary[i * cols + randomCol] = val;
		    }
		}
		return 0;
	}
	
	public Sudoku(int x, int y, int xl, int yl) {
		rows = x;
		cols = y;
		xlim = xl;
		ylim = yl;
		field = new JButton[x * y];
		solution = new int[x * y];
		temporary = new int[x * y];
		userInput = new int[x * y];
		border = new int[x * y];
		boxNumber = new int[x * y];
	}
	
	public void fill() {
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
		    	temporary[num] = userInput[num];
	    	}
	    }
	    int retval = 1;
	    boolean correct = checkIfCorrect();
    	if (!correct) {
    		return;
    	}
    	int numsols = isOnlyOneSolution();
    	if (numsols == 1) {
		    for (int i = 0; i < rows; i++){ 
		    	for (int j = 0; j < cols; j++) {
			    	int num = i * cols + j;
		    		userInput[num] = temporary[num];
		    		solution[num] = temporary[num];
		    		field[num].setText(String.valueOf(userInput[num]));
    	        	field[num].setForeground(Color.BLACK);
		    	}
		    }
    		return;
    	}
	    retval = randomPuzzle();
	    if (retval == 0) {
		    for (int i = 0; i < rows; i++){ 
		    	for (int j = 0; j < cols; j++) {
			    	int num = i * cols + j;
		    		userInput[num] = temporary[num];
		    		solution[num] = temporary[num];
		    		field[num].setText(String.valueOf(userInput[num]));
    	        	field[num].setForeground(Color.BLACK);
		    	}
		    }
	    } else {
	    	if (retval == 1) {
	    		fill();
	    	}
	    }
	}

	public void errorOutput () 
    {
        errorArea = new JTextArea (9, 20);

        errorArea.setEditable (false);

        errorFrame = new JFrame ("Greške");
        errorFrame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
        Container contentPane = errorFrame.getContentPane ();
        contentPane.setLayout (new BorderLayout ());
        contentPane.add (
            new JScrollPane (
            		errorArea, 
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
            BorderLayout.CENTER);
        errorFrame.pack ();
        errorFrame.setVisible(false);
    }

	public void instructionOutput () 
    {
        instructionArea = new JTextArea (30, 20);

        instructionArea.setEditable (false);

        instructionFrame = new JFrame ("Upute");
        instructionFrame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
        Container contentPane = instructionFrame.getContentPane ();
        contentPane.setLayout (new BorderLayout ());
        contentPane.add (
            new JScrollPane (
            		instructionArea, 
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
            BorderLayout.CENTER);
        instructionFrame.pack ();
        instructionFrame.setVisible(false);
    }
	
	abstract void draw();


}
