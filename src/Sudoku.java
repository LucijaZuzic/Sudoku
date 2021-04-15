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
	JTextArea errorArea;
	JTextArea instructionArea;
	JLabel difficulty = new JLabel("");
	JLabel penaltyLabel = new JLabel("");
	String solvingInstructions;

    int mintargetDifficulty = 0;
    int maxtargetDifficulty = 160000;
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

	int numPossibilities[] = new int[rows * cols];
	int[][] possibilities = new int[rows * cols][rows];
	int changed = 1;
	int unset = 0;
	boolean showSteps = false;
	Set<String> solvinstr = new HashSet<String>();
	
	abstract boolean checkIfCorrect();
	boolean showBoxMsg = true;
	public int floodFill(int x, int y, int val) {
		int retVal = 1;
	    int numOld = x * cols + y;
	    int lb = 1;
	    int rb = 1;
	    int tb = 1;
	    int bb = 1;
		boxNumber[numOld] = val;
        if (border[numOld] == 3) {
    		field[numOld].setBackground(Color.LIGHT_GRAY);
    	}
        if (border[numOld] == 2) {
    		field[numOld].setBackground(Color.DARK_GRAY);
    	}
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
	    	//System.out.println("Neke ćelije nisu u kutiji");
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
	    	    	if (showBoxMsg) {
	    	    		InformationBox.infoBox(String.valueOf(boxNumber[num]) + ". kutija je prevelika.", "Stvaranje kutije");	
	    	    	}
	    	    	retVal = false;
	    	    }
	    	    if (x < rows) {
	    	    	if (showBoxMsg) {
	    	    		InformationBox.infoBox(String.valueOf(boxNumber[num]) + ". kutija je premalena", "Stvaranje kutije");	
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
	    for (int i = 0; i < rows; i++){
	    	for (int j = 0; j < cols; j++) {
	    		if (temporary[i * cols + j] != 0) {
			    	for (int k = 0; k < cols; k++) {
			    		possibilities[i * cols + j][k] = 0;
				    }
		    		possibilities[i * cols + j][temporary[i * cols + j] - 1] = 1;
	    		} else {
	    			for (int k = 0; k < cols; k++) {
			    		if (temporary[i * cols + k] != 0 && possibilities[i * cols + j][temporary[i * cols + k] - 1] == 1) {
				    		possibilities[i * cols + j][temporary[i * cols + k] - 1] = 0;
		    				numChanged = 1;
			    		}
				    }
			    	for (int k = 0; k < rows; k++) {
			    		if (temporary[k * cols + j] != 0 && possibilities[i * cols + j][temporary[k * cols + j] - 1] == 1) {
			    				possibilities[i * cols + j][temporary[k * cols + j] - 1] = 0;
			    				numChanged = 1;
			    		}
				    }
			    	int b = boxNumber[i * cols + j];
				    for (int x = 0; x < rows; x++){
					    for (int y = 0; y < cols; y++){
				    		if (temporary[x * cols + y] != 0 && boxNumber[x * cols + y] == b && possibilities[i * cols + j][temporary[x * cols + y] - 1] == 1) {
			    				possibilities[i * cols + j][temporary[x * cols + y] - 1] = 0;
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
	    for (int i = 0; i < rows; i++){
	    	for (int j = 0; j < cols; j++) {
	    		if (temporary[i * cols + j] == 0) {
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
				    			solvingInstructions += "Broj " + String.valueOf(k + 1) + " je jedina moguća vrijednost ćelije (" + String.valueOf(i + 1) + ", " + String.valueOf(j + 1) + ").\n";
				    			if (showSteps == true) {
		    		    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Broj " + String.valueOf(k + 1) + " je jedina moguća vrijednost ćelije (" + String.valueOf(i + 1) + ", " + String.valueOf(j + 1) + ")", "Rješavač");
		    		    		}
			    		    	temporary[i * cols + j] = k + 1;
		    		    		field[i * cols + j].setForeground(Color.BLACK);
		    		    		field[i * cols + j].setText(String.valueOf(k + 1));
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

	
	public int nakedSetForRow (int i, int j, Set<Integer> sameRow) {
    	int num = i * cols + j;
    	for (int k = j + 1; k < cols; k++) {
	    	int num2 = i * cols + k;
	    	if (numPossibilities[num2] <= 1) {
	    		continue;
	    	}
    		int match = 0;
    		for (int kh = 0; kh < k; kh++) {
		    	int num3 = i * cols + kh;
		    	if (numPossibilities[num3] <= 1) {
		    		continue;
		    	}
    			if (num3 == num || sameRow.contains(num3)) {
		    		for (int hg = 0; hg < cols; hg++) {
		    			if (possibilities[num2][hg] == 1 && possibilities[num3][hg] == 1 && !sameRow.contains(num2) && num2 != num) {
		    				//solvingInstructions += "Naked Match old cell " + String.valueOf(num3 / cols + 1) + " " + String.valueOf(num3 % cols + 1) + " " + String.valueOf(hg + 1) + "\n";
		    				//solvingInstructions += "Naked Match new cell " + String.valueOf(num2 / cols + 1) + " " + String.valueOf(num2 % cols + 1) + " " + String.valueOf(hg + 1) + "\n";
		    				match++;
		    				break;
		    			}
		    		}
    			}
    		}
    		if (match == sameRow.size() + 1) {
    			Set<Integer> sameRow2 = new HashSet<Integer>();
        		for (int kh = 0; kh < rows * cols; kh++) {
        			if (sameRow.contains(kh)) {
        				sameRow2.add(kh);
        			}
        		}
        		sameRow2.add(num2);
				if (nakedSetForRow(i, j, sameRow2) == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
    		}
	    	int matchStringLen = 0;
	    	String ms = "";
	    	for (int hg = 0; hg < cols; hg++) {
	    		String c = "0";
	    		for (int kh = 0; kh < cols; kh++) {
	    			int num3 = i * cols + kh;
	    			if (sameRow.contains(num3) || num3 == num) {
				    	if (possibilities[num3][hg] == 1) {
				    		c = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		ms += c;
    		}
	    	//System.out.println(String.valueOf(sameRow.size()) + " actual " + String.valueOf(numPossibilities[num]));
	    	if (sameRow.size() == matchStringLen - 1 && sameRow.size() > 0) {
			    int numRemoved = 0;
	    		for (int fg = 0; fg < cols; fg++) {
			    	int num3 = i * cols + fg;
			    	if (!sameRow.contains(num3) && num3 != num) {
			    		for (int hg = 0; hg < cols; hg++) {
			    			if (ms.charAt(hg) == '1' && possibilities[num3][hg] == 1 && temporary[num3] == 0) {
			    				if (numRemoved == 0) {
			    		    		String x = "";
		    			    		if (sameRow.size() == 1) {
		    			    			x += "Ogoljeni par u redu " + String.valueOf(i + 1) + ", vrijednosti";
		    			    			int y = 0;
		    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
		    				    			if (ms.charAt(hg2) == '1') {
							    				if (y > 0 && y != 1) {
							    					x += ",";
							    				}
							    				if (y == 1) {
							    					x += " i";
							    				}
		    				    				x += " " + String.valueOf(hg2 + 1);
		    				    				y++;
		    				    			}
		    				    		}
		    				    		if (!solvinstr.contains(x)) {
			    			    			if (dj2 == 0) {
			    			    				difficultyScore += 750;
			    			    				dj2 = 1;
			    			    			} else {
			    			    				difficultyScore += 500;
			    			    			}
		    				    		}
		    				    		solvinstr.add(x);
		    			    		}
		    			    		if (sameRow.size() == 2) {
		    			    			x += "Ogoljena trojka u redu " + String.valueOf(i + 1) + ", vrijednosti";
		    			    			int y = 0;
		    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
		    				    			if (ms.charAt(hg2) == '1') {
							    				if (y > 0 && y != 2) {
							    					x += ",";
							    				}
							    				if (y == 2) {
							    					x += " i";
							    				}
		    				    				x += " " + String.valueOf(hg2 + 1);
		    				    				y++;
		    				    			}
		    				    		}
		    				    		if (!solvinstr.contains(x)) {
			    			    			if (dj3 == 0) {
			    			    				difficultyScore += 2000;
			    			    				dj3 = 1;
			    			    			} else {
			    			    				difficultyScore += 1400;
			    			    			}
		    				    		}
		    				    		solvinstr.add(x);
		    			    		}
		    			    		if (sameRow.size() == 3) {
		    			    			x += "Ogoljena četvorka u redu " + String.valueOf(i + 1) + ", vrijednosti";
		    			    			int y = 0;
		    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
		    				    			if (ms.charAt(hg2) == '1') {
							    				if (y > 0 && y != 3) {
							    					x += ",";
							    				}
							    				if (y == 3) {
							    					x += " i";
							    				}
		    				    				x += " " + String.valueOf(hg2 + 1);
		    				    				y++;
		    				    			}
		    				    		}
		    				    		if (!solvinstr.contains(x)) {
			    			    			if (dj4 == 0) {
			    			    				difficultyScore += 5000;
			    			    				dj4 = 1;
			    			    			} else {
			    			    				difficultyScore += 4000;
			    			    			}
		    				    		}
		    				    		solvinstr.add(x);
		    			    		}
		    			    		if (sameRow.size() > 3) {
		    			    			x += "Naked " + String.valueOf(sameRow.size() + 1) + " u redu " + String.valueOf(i + 1) + ", vrijednosti";
		    			    			int y = 0;
		    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
		    				    			if (ms.charAt(hg2) == '1') {
							    				if (y > 0 && y != sameRow.size()) {
							    					x += ",";
							    				}
							    				if (y == sameRow.size()) {
							    					x += " i";
							    				}
		    				    				x += " " + String.valueOf(hg2 + 1);
		    				    				y++;
		    				    			}
		    				    		}
		    				    		if (!solvinstr.contains(x)) {
			    			    			if (dj4 == 0) {
			    			    				difficultyScore += 5000;
			    			    				dj4 = 1;
			    			    			} else {
			    			    				difficultyScore += 4000;
			    			    			}
		    				    		}
		    				    		solvinstr.add(x);
		    			    		}
		    			    		/*print();
		    			    		checkIfCorrect();
		    			    		
		    		    			try {
		    		    				TimeUnit.SECONDS.sleep(1);
		    		    			} catch (InterruptedException e) {
		    		    				// TODO Auto-generated catch block
		    		    				e.printStackTrace();
		    		    			}
		    		    			InformationBox.infoBox(x, "Rješavač");*/
			    		    		x += " u ćelijama";
			    		    		int y2 = 0;
	    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
	    				    	    	int n = i * cols + hg2;
	    				    			if (sameRow.contains(n) || n == num) {
						    				if (y2 > 0 && y2 != sameRow.size()) {
						    					x += ",";
						    				}
						    				if (y2 == sameRow.size()) {
						    					x += " i";
						    				}
	    				    				x += " (" + String.valueOf(n / cols + 1) + ", " + String.valueOf(n % cols + 1) + ")";
	    				    				y2++;
	    				    			}
	    				    		}
	    				    		x += ".\n";
		    				    	solvingInstructions += x;
							    	if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    	    			InformationBox.infoBox(x, "Rješavač");
						    		}
			    				}
			    				solvingInstructions += "Uklanjam mogućnost " + String.valueOf(hg + 1) + " iz ćelije (" + String.valueOf(num3 / cols + 1) + ", " + String.valueOf(num3 % cols + 1) + ").\n";
					    		if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam mogućnost " + String.valueOf(hg + 1) + " iz ćelije (" + String.valueOf(num3 / cols + 1) + ", " + String.valueOf(num3 % cols + 1) + ").", "Rješavač");
					    		}
					    		possibilities[num3][hg] = 0;
				    			numRemoved++;
			    			}
			    		}
			    	}
			    }
	    		if (numRemoved == 0) {
	    			continue;
	    		}
				if (sequence() == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
	    	}
    	}
		return 0;
	}
	
	public int nakedSetForCol (int i, int j, Set<Integer> sameColumn) {
		int num = i * cols + j;
    	for (int k = i + 1; k < rows; k++) {
	    	int num2 = k * cols + j;
	    	if (numPossibilities[num2] <= 1) {
	    		continue;
	    	}
    		int match = 0;
    		for (int kh = 0; kh < k; kh++) {
		    	int num3 = kh * cols + j;
		    	if (numPossibilities[num3] <= 1) {
		    		continue;
		    	}
    			if (num3 == num || sameColumn.contains(num3)) {
		    		for (int hg = 0; hg < cols; hg++) {
		    			if (possibilities[num2][hg] == 1 && possibilities[num3][hg] == 1 && !sameColumn.contains(num2) && num2 != num) {
		    				//System.out.println("Naked Match old cell " + String.valueOf(num3 / cols + 1) + " " + String.valueOf(num3 % cols + 1) + " " + String.valueOf(hg + 1));
		    				//System.out.println("Naked Match new cell " + String.valueOf(num2 / cols + 1) + " " + String.valueOf(num2 % cols + 1) + " " + String.valueOf(hg + 1));
		    				match++;
		    				break;
		    			}
		    		}
    			}
    		}
    		if (match == sameColumn.size() + 1) {
    			Set<Integer> sameColumn2 = new HashSet<Integer>();
	    		for (int kh = 0; kh < rows * cols; kh++) {
	    			if (sameColumn.contains(kh)) {
	    				sameColumn2.add(kh);
	    			}
	    		}
	    		sameColumn2.add(num2);
				if (nakedSetForCol(i, j, sameColumn2) == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
			}
	    	int matchStringLen = 0;
	    	String ms = "";
	    	for (int hg = 0; hg < cols; hg++) {
	    		String c = "0";
	    		for (int kh = 0; kh < rows; kh++) {
	    			int num3 = kh * cols + j;
	    			if (sameColumn.contains(num3) || num3 == num) {
				    	if (possibilities[num3][hg] == 1) {
				    		c = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		ms += c;
    		}
	    	//System.out.println(String.valueOf(sameColumn.size()) + " actual " + String.valueOf(numPossibilities[num]));
	    	if (sameColumn.size() == matchStringLen - 1 && sameColumn.size() > 0) {
			    int numRemoved = 0;
	    		for (int fg = 0; fg < rows; fg++) {
			    	int num3 = fg * cols + j;
			    	if (!sameColumn.contains(num3) && num3 != num) {
			    		for (int hg = 0; hg < cols; hg++) {
			    			if (ms.charAt(hg) == '1' && possibilities[num3][hg] == 1 && temporary[num3] == 0) {
			    				if (numRemoved == 0) {
			    		    		String x = "";
		    			    		if (sameColumn.size() == 1) {
		    			    			x += "Ogoljeni par u stupcu " + String.valueOf(j + 1) + ", vrijednosti";
		    			    			int y = 0;
		    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
		    				    			if (ms.charAt(hg2) == '1') {
							    				if (y > 0 && y != 1) {
							    					x += ",";
							    				}
							    				if (y == 1) {
							    					x += " i";
							    				}
		    				    				x += " " + String.valueOf(hg2 + 1);
		    				    				y++;
		    				    			}
		    				    		}
		    				    		if (!solvinstr.contains(x)) {
			    			    			if (dj2 == 0) {
			    			    				difficultyScore += 750;
			    			    				dj2 = 1;
			    			    			} else {
			    			    				difficultyScore += 500;
			    			    			}
		    				    		}
		    				    		solvinstr.add(x);
		    			    		}
		    			    		if (sameColumn.size() == 2) {
		    			    			x += "Ogoljena trojka u stupcu " + String.valueOf(j + 1) + ", vrijednosti";
		    			    			int y = 0;
		    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
		    				    			if (ms.charAt(hg2) == '1') {
							    				if (y > 0 && y != 2) {
							    					x += ",";
							    				}
							    				if (y == 2) {
							    					x += " i";
							    				}
		    				    				x += " " + String.valueOf(hg2 + 1);
		    				    				y++;
		    				    			}
		    				    		}
		    				    		if (!solvinstr.contains(x)) {
			    			    			if (dj3 == 0) {
			    			    				difficultyScore += 2000;
			    			    				dj3 = 1;
			    			    			} else {
			    			    				difficultyScore += 1400;
			    			    			}
		    				    		}
		    				    		solvinstr.add(x);
		    			    		}
		    			    		if (sameColumn.size() == 3) {
		    			    			x += "Ogoljena četvorka u stupcu " + String.valueOf(j + 1) + ", vrijednosti";
		    			    			int y = 0;
		    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
		    				    			if (ms.charAt(hg2) == '1') {
							    				if (y > 0 && y != 3) {
							    					x += ",";
							    				}
							    				if (y == 3) {
							    					x += " i";
							    				}
		    				    				x += " " + String.valueOf(hg2 + 1);
		    				    				y++;
		    				    			}
		    				    		}
		    				    		if (!solvinstr.contains(x)) {
			    			    			if (dj4 == 0) {
			    			    				difficultyScore += 5000;
			    			    				dj4 = 1;
			    			    			} else {
			    			    				difficultyScore += 4000;
			    			    			}
		    				    		}
		    				    		solvinstr.add(x);
		    			    		}
		    			    		if (sameColumn.size() > 3) {
		    			    			x += "Naked " + String.valueOf(sameColumn.size() + 1) + " u stupcu " + String.valueOf(j + 1) + ", vrijednosti";
		    			    			int y = 0;
		    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
		    				    			if (ms.charAt(hg2) == '1') {
							    				if (y > 0 && y != sameColumn.size()) {
							    					x += ",";
							    				}
							    				if (y == sameColumn.size()) {
							    					x += " i";
							    				}
		    				    				x += " " + String.valueOf(hg2 + 1);
		    				    				y++;
		    				    			}
		    				    		}
		    				    		if (!solvinstr.contains(x)) {
			    			    			if (dj4 == 0) {
			    			    				difficultyScore += 5000;
			    			    				dj4 = 1;
			    			    			} else {
			    			    				difficultyScore += 4000;
			    			    			}
		    				    		}
		    				    		solvinstr.add(x);
		    			    		}
		    			    		/*print();
		    			    		checkIfCorrect();
		    			    		
		    		    			try {
		    		    				TimeUnit.SECONDS.sleep(1);
		    		    			} catch (InterruptedException e) {
		    		    				// TODO Auto-generated catch block
		    		    				e.printStackTrace();
		    		    			}
		    		    			InformationBox.infoBox(x, "Rješavač");*/
			    		    		x += " u ćelijama";
			    		    		int y2 = 0;
	    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
	    				    	    	int n = hg2 * cols + j;
	    				    			if (sameColumn.contains(n) || n == num) {
						    				if (y2 > 0 && y2 != sameColumn.size()) {
						    					x += ",";
						    				}
						    				if (y2 == sameColumn.size()) {
						    					x += " i";
						    				}
	    				    				x += " (" + String.valueOf(n / cols + 1) + ", " + String.valueOf(n % cols + 1) + ")";
	    				    				y2++;
	    				    			}
	    				    		}
		    				    	x += ".\n";
		    				    	solvingInstructions += x;
							    	if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    	    			InformationBox.infoBox(x, "Rješavač");
						    		}
			    				}
			    				solvingInstructions += "Uklanjam mogućnost " + String.valueOf(hg + 1) + " iz ćelije (" + String.valueOf(num3 / cols + 1) + ", " + String.valueOf(num3 % cols + 1) + ").\n";
			    				if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam mogućnost " + String.valueOf(hg + 1) + " iz ćelije (" + String.valueOf(num3 / cols + 1) + ", " + String.valueOf(num3 % cols + 1) + ").", "Rješavač");
					    		}
			    				possibilities[num3][hg] = 0;
			    				numRemoved++;
			    			}
			    		}
			    	}
			    }
	    		if (numRemoved == 0) {
	    			continue;
	    		}
				if (sequence() == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
	    	}
    	}
		return 0;
	}
	
	public int nakedSetForBox(int i, int j, Set<Integer> sameBox) {
		int num = i * cols + j;
    	for (int k = num + 1; k < rows * cols; k++) {
	    	if (boxNumber[k] != boxNumber[num]) {
	    		continue;
	    	}
	    	if (numPossibilities[k] <= 1) {
	    		continue;
	    	}
	    	int match = 0;
	    	for (int k2 = 0; k2 < k; k2++) {
		    	if (boxNumber[k2] != boxNumber[num]) {
		    		continue;
		    	}
		    	if (numPossibilities[k2] <= 1) {
		    		continue;
		    	}
    			if (k2 == num || sameBox.contains(k2)) {
		    		for (int hg = 0; hg < cols; hg++) {
		    			if (possibilities[k][hg] == 1 && possibilities[k2][hg] == 1 && !sameBox.contains(k) && k != num) {
		    				//System.out.println("Naked Match old cell " + String.valueOf(k2 / cols + 1) + " " + String.valueOf(k2 % cols + 1) + " " + String.valueOf(hg + 1));
		    				//System.out.println("Naked Match new cell " + String.valueOf(k / cols + 1) + " " + String.valueOf(k % cols + 1) + " " + String.valueOf(hg + 1));
		    				match++;
		    				break;
		    			}
		    		}
    			}
	    	}
    		if (match == sameBox.size() + 1) {
    			Set<Integer> sameBox2 = new HashSet<Integer>();
	    		for (int kh = 0; kh < rows * cols; kh++) {
	    			if (sameBox.contains(kh)) {
	    				sameBox2.add(kh);
	    			}
	    		}
	    		sameBox2.add(k);
				if (nakedSetForBox(i, j, sameBox2) == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
    		}
	    	int matchStringLen = 0;
	    	String ms = "";
	    	for (int hg = 0; hg < cols; hg++) {
	    		String c = "0";
	    		for (int kh = 0; kh < rows * cols; kh++) {
			    	if (boxNumber[num] != boxNumber[kh]) {
			    		continue;
			    	}
	    			if (sameBox.contains(kh) || kh == num) {
				    	if (possibilities[kh][hg] == 1) {
				    		c = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		ms += c;
    		}
	    	//System.out.println(String.valueOf(sameColumn.size()) + " actual " + String.valueOf(numPossibilities[num]));
	    	if (sameBox.size() == matchStringLen - 1 && sameBox.size() > 0) {
		    	int numRemoved = 0;
		    	for (int fg = 0; fg < rows * cols; fg++) {
			    	int num3 = fg;
			    	if (boxNumber[num] != boxNumber[num3]) {
			    		continue;
			    	}
			    	if (!sameBox.contains(num3) && num3 != num) {
			    		for (int hg = 0; hg < cols; hg++) {
			    			if (ms.charAt(hg) == '1' && possibilities[num3][hg] == 1 && temporary[num3] == 0) {
			    				if (numRemoved == 0) {
			    		    		String x = "";
		    			    		if (sameBox.size() == 1) {
		    			    			x += "Ogoljeni par u kutiji " + String.valueOf(boxNumber[num] + 1) + ", vrijednosti";
		    			    			int y = 0;
		    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
		    				    			if (ms.charAt(hg2) == '1') {
							    				if (y > 0 && y != 1) {
							    					x += ",";
							    				}
							    				if (y == 1) {
							    					x += " i";
							    				}
		    				    				x += " " + String.valueOf(hg2 + 1);
		    				    				y++;
		    				    			}
		    				    		}
		    				    		if (!solvinstr.contains(x)) {
			    			    			if (dj2 == 0) {
			    			    				difficultyScore += 750;
			    			    				dj2 = 1;
			    			    			} else {
			    			    				difficultyScore += 500;
			    			    			}
		    				    		}
		    				    		solvinstr.add(x);
		    			    		}
		    			    		if (sameBox.size() == 2) {
		    			    			x += "Ogoljena trojka u kutiji " + String.valueOf(boxNumber[num] + 1) + ", vrijednosti";
		    			    			int y = 0;
		    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
		    				    			if (ms.charAt(hg2) == '1') {
							    				if (y > 0 && y != 2) {
							    					x += ",";
							    				}
							    				if (y == 2) {
							    					x += " i";
							    				}
		    				    				x += " " + String.valueOf(hg2 + 1);
		    				    				y++;
		    				    			}
		    				    		}
		    				    		if (!solvinstr.contains(x)) {
			    			    			if (dj3 == 0) {
			    			    				difficultyScore += 2000;
			    			    				dj3 = 1;
			    			    			} else {
			    			    				difficultyScore += 1400;
			    			    			}
		    				    		}
		    				    		solvinstr.add(x);
		    			    		}
		    			    		if (sameBox.size() == 3) {
		    			    			x += "Ogoljena četvorka u kutiji " + String.valueOf(boxNumber[num] + 1) + ", vrijednosti";
		    			    			int y = 0;
		    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
		    				    			if (ms.charAt(hg2) == '1') {
							    				if (y > 0 && y != 3) {
							    					x += ",";
							    				}
							    				if (y == 3) {
							    					x += " i";
							    				}
		    				    				x += " " + String.valueOf(hg2 + 1);
		    				    				y++;
		    				    			}
		    				    		}
		    				    		if (!solvinstr.contains(x)) {
			    			    			if (dj4 == 0) {
			    			    				difficultyScore += 5000;
			    			    				dj4 = 1;
			    			    			} else {
			    			    				difficultyScore += 4000;
			    			    			}
		    				    		}
		    				    		solvinstr.add(x);
		    			    		}
		    			    		if (sameBox.size() > 3) {
		    			    			x += "Naked " + String.valueOf(sameBox.size() + 1) + " u kutiji " + String.valueOf(boxNumber[num] + 1) + ", vrijednosti";
		    			    			int y = 0;
		    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
		    				    			if (ms.charAt(hg2) == '1') {
							    				if (y > 0 && y != sameBox.size()) {
							    					x += ",";
							    				}
							    				if (y == sameBox.size()) {
							    					x += " i";
							    				}
		    				    				x += " " + String.valueOf(hg2 + 1);
		    				    				y++;
		    				    			}
		    				    		}
		    				    		if (!solvinstr.contains(x)) {
			    			    			if (dj4 == 0) {
			    			    				difficultyScore += 5000;
			    			    				dj4 = 1;
			    			    			} else {
			    			    				difficultyScore += 4000;
			    			    			}
		    				    		}
		    				    		solvinstr.add(x);
		    			    		}
		    			    		/*print();
		    			    		checkIfCorrect();
		    			    		
		    		    			try {
		    		    				TimeUnit.SECONDS.sleep(1);
		    		    			} catch (InterruptedException e) {
		    		    				// TODO Auto-generated catch block
		    		    				e.printStackTrace();
		    		    			}
		    		    			InformationBox.infoBox(x, "Rješavač");*/
			    		    		x += " u ćelijama";
			    		    		int y2 = 0;
	    				    		for (int hg2 = 0; hg2 < rows * cols; hg2++) {
	    						    	if (boxNumber[hg2] != boxNumber[num]) {
	    						    		continue;
	    						    	}
	    				    			if (sameBox.contains(hg2) || hg2 == num) {
						    				if (y2 > 0 && y2 != sameBox.size()) {
						    					x += ",";
						    				}
						    				if (y2 == sameBox.size()) {
						    					x += " i";
						    				}
	    				    				x += " (" + String.valueOf(hg2 / cols + 1) + ", " + String.valueOf(hg2 % cols + 1) + ")";
	    				    				y2++;
	    				    			}
	    				    		}
		    				    	x += ".\n";
		    				    	solvingInstructions += x;
							    	if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    	    			InformationBox.infoBox(x, "Rješavač");
						    		}
			    				}
			    				solvingInstructions += "Uklanjam mogućnost " + String.valueOf(hg + 1) + " iz ćelije (" + String.valueOf(num3 / cols + 1) + ", " + String.valueOf(num3 % cols + 1) + ").\n";
			    				if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam mogućnost " + String.valueOf(hg + 1) + " iz ćelije (" + String.valueOf(num3 / cols + 1) + ", " + String.valueOf(num3 % cols + 1) + ").", "Rješavač");
					    		}
			    				possibilities[num3][hg] = 0;
					    		numRemoved++;
			    			}
			    		}
			    	}
		    	}
	    		if (numRemoved == 0) {
	    			continue;
	    		}
				if (sequence() == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
	    	}
    	}
    	return 0;
	}
	
	public int nakedSet() {
	    numPossibilities = new int[rows * cols];
	    
	    for (int i = 0; i < rows; i++){
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
		    	numPossibilities[num] = 0;
		    	for (int k = 0; k < cols; k++) {
		    		numPossibilities[num] += possibilities[i * cols + j][k];
			    }
	    	}
	    }
	    for (int i = 0; i < rows; i++){
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
		    	if (numPossibilities[num] <= 1) {
		    		continue;
		    	}
		    	if (temporary[num] != 0) {
		    		continue;
		    	}
		    	Set<Integer> sameRow = new HashSet<Integer>();
		    	if (nakedSetForRow(i, j, sameRow) == 1) {
					difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					return 1;
				}

		    	Set<Integer> sameColumn = new HashSet<Integer>();
		    	if (nakedSetForCol(i, j, sameColumn) == 1) {
					difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					return 1;
				}

		    	Set<Integer> sameBox = new HashSet<Integer>();
		    	if (nakedSetForBox(i, j, sameBox) == 1) {
					difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					return 1;
		    	}
	    	}
	    }
		return 0;
	}
	
	int us2 = 0;
	int us3 = 0;
	int us4 = 0;
	
	String valuePos[] = new String[cols];
	Integer numPos[] = new Integer[cols];

	public int hiddenSetForRow(int k1, int i, Set<Integer> sameRowValues) {
		for (int k = k1 + 1; k < cols; k++) {
			if (k == k1 || sameRowValues.contains(k)) {
    			continue;
    		}
    		int match = 0;
    		for (int kh = 0; kh < cols; kh++) {
    			if (kh == k1 || sameRowValues.contains(kh)) {
		    		for (int hg = 0; hg < cols; hg++) {
		    			if (valuePos[k].charAt(hg) == '1' && valuePos[kh].charAt(hg) == '1') {
		    				//System.out.println("Hidden Match old value " + String.valueOf(kh + 1) + " u redu " + String.valueOf(i + 1) + " at col " + String.valueOf(hg + 1));
		    				//System.out.println("Hidden Match new value " + String.valueOf(k + 1) + " u redu " + String.valueOf(i + 1) + " at col " + String.valueOf(hg + 1));
		    				match++;
		    				break;
		    			}
		    		}
    			}
    		}
    		if (match == sameRowValues.size() + 1) {
    			Set<Integer> sameRowValues2 = new HashSet<Integer>();
        		for (int kh = 0; kh < cols; kh++) {
        			if (sameRowValues.contains(kh)) {
        				sameRowValues2.add(kh);
        			}
        		}
				sameRowValues2.add(k);
				if (hiddenSetForRow(k1, i, sameRowValues2) == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
    		}
	    	int matchStringLen = 0;
	    	String ms = "";
	    	for (int hg = 0; hg < cols; hg++) {
	    		String c = "0";
	    		for (int kh = 0; kh < cols; kh++) {
	    			if (kh == k1 || sameRowValues.contains(kh)) {
				    	if (valuePos[kh].charAt(hg) == '1') {
				    		c = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		ms += c;
    		}
	    	//System.out.println("matchStringLen " + String.valueOf(matchStringLen));
	    	if (sameRowValues.size() == matchStringLen - 1 && sameRowValues.size() > 0) {
	    		int numRemoved = 0;
				for (int q = 0; q < cols; q++) {
					if (ms.charAt(q) == '1') {
						 for (int k2 = 0; k2 < cols; k2++) {
							if (!sameRowValues.contains(k2) && k2 != k1 && possibilities[i * cols + q][k2] == 1) {
						    	if (numRemoved == 0) {
						    		String x = "";
						    		if (sameRowValues.size() == 1) {
						    			x += "Skriveni par u redu " + String.valueOf(i + 1) + ", vrijednosti";
						    			int y = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameRowValues.contains(hg) || hg == k1) {
							    				if (y != 0) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y++;
							    			}
							    		}
		    				    		if (!solvinstr.contains(x)) {
							    			if (us2 == 0) {
							    				difficultyScore += 1500;
							    				us2 = 1;
							    			} else {
							    				difficultyScore += 1200;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
						    		}
						    		if (sameRowValues.size() == 2) {
						    			x += "Skrivena trojka u redu " + String.valueOf(i + 1) + ", vrijednosti";
						    			int y = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameRowValues.contains(hg) || hg == k1) {
							    				if (y > 0 && y != 2) {
							    					x += ",";
							    				}
							    				if (y == 2) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y++;
							    			}
							    		}
		    				    		if (!solvinstr.contains(x)) {
							    			if (us3 == 0) {
							    				difficultyScore += 2400;
							    				us3 = 1;
							    			} else {
							    				difficultyScore += 1600;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
						    		}
						    		if (sameRowValues.size() == 3) {
						    			x += "Skrivena četvorka u redu " + String.valueOf(i + 1) + ", vrijednosti";
						    			int y = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameRowValues.contains(hg) || hg == k1) {
							    				if (y > 0 && y != 3) {
							    					x += ",";
							    				}
							    				if (y == 3) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y++;
							    			}
							    		}
		    				    		if (!solvinstr.contains(x)) {
							    			if (us4 == 0) {
							    				difficultyScore += 7000;
							    				us4 = 1;
							    			} else {
							    				difficultyScore += 5000;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
						    		}
						    		if (sameRowValues.size() > 3) {
						    			x += "Skrivenih " + String.valueOf(sameRowValues.size() + 1) + " u redu " + String.valueOf(i + 1) + ", vrijednosti";
						    			int y = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameRowValues.contains(hg) || hg == k1) {
							    				if (y > 0 && y != sameRowValues.size()) {
							    					x += ",";
							    				}
							    				if (y == sameRowValues.size()) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y++;
							    			}
							    		}
		    				    		if (!solvinstr.contains(x)) {
							    			if (us4 == 0) {
							    				difficultyScore += 7000;
							    				us4 = 1;
							    			} else {
							    				difficultyScore += 5000;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
						    		}
						    		/*print();
						    		checkIfCorrect();
						    		
					    			try {
					    				TimeUnit.SECONDS.sleep(1);
					    			} catch (InterruptedException e) {
					    				// TODO Auto-generated catch block
					    				e.printStackTrace();
					    			}
					    			InformationBox.infoBox(x, "Rješavač");*/
			    		    		x += " u ćelijama";
			    		    		int y2 = 0;
	    				    		for (int hg2 = 0; hg2 < cols; hg2++) {
	    				    			if (ms.charAt(hg2) == '1') {
						    				if (y2 > 0 && y2 != matchStringLen - 1) {
						    					x += ",";
						    				}
						    				if (y2 == matchStringLen - 1) {
						    					x += " i";
						    				}
	    				    				x += " (" + String.valueOf(i + 1) + ", " + String.valueOf(hg2 + 1) + ")";
	    				    				y2++;
	    				    			}
	    				    		}
							    	x += ".\n";
							    	solvingInstructions += x;
							    	if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    	    			InformationBox.infoBox(x, "Rješavač");
						    		}
						    	}		    			    		
								solvingInstructions += "Uklanjam mogućnost " + String.valueOf(k2 + 1) + " iz ćelije (" + String.valueOf(i + 1) + ", " + String.valueOf(q + 1) + ").\n" ;
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam mogućnost " + String.valueOf(k2 + 1) + " iz ćelije (" + String.valueOf(i + 1) + ", " + String.valueOf(q + 1) + ").", "Rješavač");
					    		}
			    				possibilities[i * cols + q][k2] = 0;
					    		numRemoved++;
				    			/*int possibility = 0;
						    	for (int kh = 0; kh < cols; kh++) {
						    		possibility += possibilities[i * cols + q][k2];
							    }
						    	if (possibility == 1) {
						    		difficultyScore += 200 * 1;
						    		changed++;
						    		unset--;
							    	for (int kh = 0; kh < cols; kh++) {
							    		if (possibilities[i * cols + q][kh] == 1) {				    		
							    			solvingInstructions += "Hidden " + numPos[k1] + " " + String.valueOf(kh + 1) + " (" + String.valueOf(i + 1) + ", " + String.valueOf(q + 1) + ")\n";
							    			temporary[i * cols + q] = kh + 1;
						    		    	field[i * cols + q].setForeground(Color.BLACK);
					    		    		field[i * cols + q].setText(String.valueOf(kh + 1));
					    		    		break;
							    		}
								    }
						    		if (unset == 0) {
						    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
						    			return 1;
						    		}
						    	}*/
							} 
						}
					}
				}
	    		if (numRemoved == 0) {
	    			continue;
	    		}
				if (sequence() == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
	    	}
		}
		return 0;
	}
	
	public int hiddenSetForCol(int k1, int i, Set<Integer> sameColValues) {	
		for (int k = k1 + 1; k < cols; k++) {
			if (k == k1 || sameColValues.contains(k)) {
				continue;
			}
			int match = 0;
			for (int kh = 0; kh < cols; kh++) {
				if (kh == k1 || sameColValues.contains(kh)) {
		    		for (int hg = 0; hg < cols; hg++) {
		    			if (valuePos[k].charAt(hg) == '1' && valuePos[kh].charAt(hg) == '1') {
		    				//System.out.println("Hidden Match old value " + String.valueOf(kh + 1) + " in col " + String.valueOf(i + 1) + " at row " + String.valueOf(hg + 1));
		    				//System.out.println("Hidden Match new value " + String.valueOf(k + 1) + " in col " + String.valueOf(i + 1) + " at row " + String.valueOf(hg + 1));
		    				match++;
		    				break;
		    			}
		    		}
				}
			}
    		if (match == sameColValues.size() + 1) {
    			Set<Integer> sameColValues2 = new HashSet<Integer>();
        		for (int kh = 0; kh < cols; kh++) {
        			if (sameColValues.contains(kh)) {
        				sameColValues2.add(kh);
        			}
        		}
        		sameColValues2.add(k);
				if (hiddenSetForCol(k1, i, sameColValues2) == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
    		}
	    	int matchStringLen = 0;
	    	String ms = "";
	    	for (int hg = 0; hg < cols; hg++) {
	    		String c = "0";
	    		for (int kh = 0; kh < cols; kh++) {
	    			if (kh == k1 || sameColValues.contains(kh)) {
				    	if (valuePos[kh].charAt(hg) == '1') {
				    		c = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		ms += c;
    		}
	    	if (sameColValues.size() == matchStringLen - 1 && sameColValues.size() > 0) {
				int numRemoved = 0;
				for (int q = 0; q < rows; q++) {
					if (ms.charAt(q) == '1') {
						for (int k2 = 0; k2 < cols; k2++) {
							if (!sameColValues.contains(k2) && k2 != k1 && possibilities[q * cols + i][k2] == 1) {
						    	if (numRemoved == 0) {
						    		String x = "";
						    		if (sameColValues.size() == 1) {
						    			x += "Skriveni par u stupcu " + String.valueOf(i + 1) + ", vrijednosti";
						    			int y = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameColValues.contains(hg) || hg == k1) {
							    				if (y != 0) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y++;
							    			}
							    		}
		    				    		if (!solvinstr.contains(x)) {
							    			if (us2 == 0) {
							    				difficultyScore += 1500;
							    				us2 = 1;
							    			} else {
							    				difficultyScore += 1200;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
						    		}
						    		if (sameColValues.size() == 2) {
						    			x += "Skrivena trojka u stupcu " + String.valueOf(i + 1) + ", vrijednosti";
						    			int y = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameColValues.contains(hg) || hg == k1) {
							    				if (y > 0 && y != 2) {
							    					x += ",";
							    				}
							    				if (y == 2) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y++;
							    			}
							    		}
		    				    		if (!solvinstr.contains(x)) {
							    			if (us3 == 0) {
							    				difficultyScore += 2400;
							    				us3 = 1;
							    			} else {
							    				difficultyScore += 1600;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
						    		}
						    		if (sameColValues.size() == 3) {
						    			x += "Skrivena četvorka u stupcu " + String.valueOf(i + 1) + ", vrijednosti";
						    			int y = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameColValues.contains(hg) || hg == k1) {
							    				if (y > 0 && y != 3) {
							    					x += ",";
							    				}
							    				if (y == 3) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y++;
							    			}
							    		}
		    				    		if (!solvinstr.contains(x)) {
							    			if (us4 == 0) {
							    				difficultyScore += 7000;
							    				us4 = 1;
							    			} else {
							    				difficultyScore += 5000;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
						    		}
						    		if (sameColValues.size() > 3) {
						    			x += "Skrivenih " + String.valueOf(sameColValues.size() + 1) + " u stupcu " + String.valueOf(i + 1) + ", vrijednosti";
						    			int y = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameColValues.contains(hg) || hg == k1) {
							    				if (y > 0 && y != sameColValues.size()) {
							    					x += ",";
							    				}
							    				if (y == sameColValues.size()) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y++;
							    			}
							    		}
		    				    		if (!solvinstr.contains(x)) {
							    			if (us4 == 0) {
							    				difficultyScore += 7000;
							    				us4 = 1;
							    			} else {
							    				difficultyScore += 5000;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
						    		}
						    		/*print();
						    		checkIfCorrect();
						    		
					    			try {
					    				TimeUnit.SECONDS.sleep(1);
					    			} catch (InterruptedException e) {
					    				// TODO Auto-generated catch block
					    				e.printStackTrace();
					    			}
					    			InformationBox.infoBox(x, "Rješavač");*/
			    		    		x += " u ćelijama";
			    		    		int y2 = 0;
	    				    		for (int hg2 = 0; hg2 < rows; hg2++) {
	    				    			if (ms.charAt(hg2) == '1') {
						    				if (y2 > 0 && y2 != matchStringLen - 1) {
						    					x += ",";
						    				}
						    				if (y2 == matchStringLen - 1) {
						    					x += " i";
						    				}
	    				    				x += " (" + String.valueOf(hg2 + 1) + ", " + String.valueOf(i + 1) + ")";
	    				    				y2++;
	    				    			}
	    				    		}
							    	x += ".\n";
							    	solvingInstructions += x;
							    	if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    	    			InformationBox.infoBox(x, "Rješavač");
						    		}
						    	}		    			    	    		
								solvingInstructions += "Uklanjam mogućnost " + String.valueOf(k2 + 1) + " iz ćelije (" + String.valueOf(q + 1) + ", " + String.valueOf(i + 1) + ").\n";
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam mogućnost " + String.valueOf(k2 + 1) + " iz ćelije (" + String.valueOf(q + 1) + ", " + String.valueOf(i + 1) + ").", "Rješavač");
					    		}
								possibilities[q * cols + i][k2] = 0;
			    				numRemoved++;
				    			/*int possibility = 0;
						    	for (int kh = 0; kh < cols; kh++) {
						    		possibility += possibilities[q * cols + i][k2];
							    }
						    	if (possibility == 1) {
						    		difficultyScore += 200 * 1;
						    		changed++;
						    		unset--;
							    	for (int kh = 0; kh < cols; kh++) {
							    		if (possibilities[i * cols + q][kh] == 1) {				    		
							    			solvingInstructions += "Hidden " + numPos[k1] + " " + String.valueOf(kh + 1) + " (" + String.valueOf(i + 1) + ", " + String.valueOf(q + 1) + ")\n";
							    			temporary[i * cols + q] = kh + 1;
						    		    	field[i * cols + q].setForeground(Color.BLACK);
					    		    		field[i * cols + q].setText(String.valueOf(kh + 1));
					    		    		break;
							    		}
								    }
						    		if (unset == 0) {
						    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
						    			return 1;
						    		}
						    	}*/
							}
						}
					}
				}
	    		if (numRemoved == 0) {
	    			continue;
	    		}
				if (sequence() == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
			}
		}
		return 0;
	}
	
	public int hiddenSetForBox(int k1, int i, Set<Integer> sameBoxValues) {	
		for (int k = k1 + 1; k < cols; k++) {
			if (k == k1 || sameBoxValues.contains(k)) {
				continue;
			}
			int match = 0;
			for (int kh = 0; kh < cols; kh++) {
				if (kh == k1 || sameBoxValues.contains(kh)) {
		    		for (int hg = 0; hg < cols; hg++) {
		    			if (valuePos[k].charAt(hg) == '1' && valuePos[kh].charAt(hg) == '1') {
		    				//System.out.println("Hidden Match old value " + String.valueOf(kh + 1) + " u kutiji " + String.valueOf(i + 1) + " at pos " + String.valueOf(hg + 1));
		    				//System.out.println("Hidden Match new value " + String.valueOf(k + 1) + " u kutiji " + String.valueOf(i + 1) + " at pos " + String.valueOf(hg + 1));
		    				match++;
		    				break;
		    			}
		    		}
				}
			}
    		if (match == sameBoxValues.size() + 1) {
    			Set<Integer> sameBoxValues2 = new HashSet<Integer>();
        		for (int kh = 0; kh < cols; kh++) {
        			if (sameBoxValues.contains(kh)) {
        				sameBoxValues2.add(kh);
        			}
        		}
        		sameBoxValues2.add(k);
				if (hiddenSetForBox(k1, i, sameBoxValues2) == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
			}
	    	int matchStringLen = 0;
	    	String ms = "";
	    	for (int hg = 0; hg < cols; hg++) {
	    		String c = "0";
	    		for (int kh = 0; kh < cols; kh++) {
	    			if (kh == k1 || sameBoxValues.contains(kh)) {
				    	if (valuePos[kh].charAt(hg) == '1') {
				    		c = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		ms += c;
    		}
	    	if (sameBoxValues.size() == matchStringLen - 1 && sameBoxValues.size() > 0) {
				int q = -1;
				int numRemoved = 0;
				for (int y = 0; y < rows * cols; y++) {
		    		if (i != boxNumber[y]) {
		    			continue;
		    		} else {
		    			q++;
		    		}
					if (ms.charAt(q) == '1') {
						for (int k2 = 0; k2 < cols; k2++) {
							if (!sameBoxValues.contains(k2) && k2 != k1 && possibilities[y][k2] == 1) {
						    	if (numRemoved == 0) {
						    		String x = "";
						    		if (sameBoxValues.size() == 1) {
						    			x += "Skriveni par u kutiji " + String.valueOf(i + 1) + ", vrijednosti";
						    			int y2 = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameBoxValues.contains(hg) || hg == k1) {
							    				if (y2 != 0) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y2++;
							    			}
							    		}
		    				    		if (!solvinstr.contains(x)) {
							    			if (us2 == 0) {
							    				difficultyScore += 1500;
							    				us2 = 1;
							    			} else {
							    				difficultyScore += 1200;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
						    		}
						    		if (sameBoxValues.size() == 2) {
						    			x += "Skrivena trojka u kutiji " + String.valueOf(i + 1) + ", vrijednosti";
						    			int y2 = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameBoxValues.contains(hg) || hg == k1) {
							    				if (y2 > 0 && y2 != 2) {
							    					x += ",";
							    				}
							    				if (y2 == 2) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y2++;
							    			}
							    		}
		    				    		if (!solvinstr.contains(x)) {
							    			if (us3 == 0) {
							    				difficultyScore += 2400;
							    				us3 = 1;
							    			} else {
							    				difficultyScore += 1600;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
						    		}
						    		if (sameBoxValues.size() == 3) {
						    			x += "Skrivena četvorka u kutiji " + String.valueOf(i + 1) + ", vrijednosti";
						    			int y2 = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameBoxValues.contains(hg) || hg == k1) {
							    				if (y2 > 0 && y2 != 3) {
							    					x += ",";
							    				}
							    				if (y2 == 3) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y2++;
							    			}
							    		}
		    				    		if (!solvinstr.contains(x)) {
							    			if (us4 == 0) {
							    				difficultyScore += 7000;
							    				us4 = 1;
							    			} else {
							    				difficultyScore += 5000;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
						    		}
						    		if (sameBoxValues.size() > 3) {
						    			x += "Skrivenih " + String.valueOf(sameBoxValues.size() + 1) + " u kutiji " + String.valueOf(i + 1) + ", vrijednosti";
						    			int y2 = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameBoxValues.contains(hg) || hg == k1) {
							    				if (y2 > 0 && y2 != sameBoxValues.size()) {
							    					x += ",";
							    				}
							    				if (y2 == sameBoxValues.size()) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y2++;
							    			}
							    		}
		    				    		if (!solvinstr.contains(x)) {
							    			if (us4 == 0) {
							    				difficultyScore += 7000;
							    				us4 = 1;
							    			} else {
							    				difficultyScore += 5000;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
						    		}
						    		/*print();
						    		checkIfCorrect();
						    		
					    			try {
					    				TimeUnit.SECONDS.sleep(1);
					    			} catch (InterruptedException e) {
					    				// TODO Auto-generated catch block
					    				e.printStackTrace();
					    			}
					    			InformationBox.infoBox(x, "Rješavač");*/
			    		    		x += " u ćelijama";
			    		    		int y2 = 0;
			    					int q2 = -1;
			    					for (int y3 = 0; y3 < rows * cols; y++) {
			    			    		if (i != boxNumber[y3]) {
			    			    			continue;
			    			    		} else {
			    			    			q2++;
			    			    		}
	    				    			if (ms.charAt(q2) == '1') {
						    				if (y2 > 0 && y2 != matchStringLen - 1) {
						    					x += ",";
						    				}
						    				if (y2 == matchStringLen - 1) {
						    					x += " i";
						    				}
	    				    				x += " (" + String.valueOf(y3 / cols + 1) + ", " + String.valueOf(y3 % cols + 1) + ")";
	    				    				y2++;
	    				    			}
			    					}
			    					x += ".\n";
							    	solvingInstructions += x;
							    	if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    	    			InformationBox.infoBox(x, "Rješavač");
						    		}
						    	}		 
								solvingInstructions += "Uklanjam mogućnost " + String.valueOf(k2 + 1) + " iz ćelije (" + String.valueOf(y / cols + 1) + ", " + String.valueOf(y % cols + 1) + ").\n";
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam mogućnost " + String.valueOf(k2 + 1) + " iz ćelije (" + String.valueOf(y / cols + 1) + ", " + String.valueOf(y % cols + 1) + ").", "Rješavač");
					    		}
								possibilities[y][k2] = 0;
			    				numRemoved++;
				    			/*int possibility = 0;
						    	for (int kh = 0; kh < cols; kh++) {
						    		possibility += possibilities[y][k2];
							    }
						    	if (possibility == 1) {
						    		difficultyScore += 200 * 1;
						    		changed++;
						    		unset--;
							    	for (int kh = 0; kh < cols; kh++) {
							    		if (possibilities[y][kh] == 1) {				    		
							    			solvingInstructions += "Hidden " + numPos[k1] + " " + String.valueOf(kh + 1) + " (" + String.valueOf(y / cols + 1) + ", " + String.valueOf(y % cols + 1) + ")\n";
							    			temporary[y] = kh + 1;
						    		    	field[y].setForeground(Color.BLACK);
					    		    		field[y].setText(String.valueOf(kh + 1));
					    		    		break;
							    		}
								    }
						    		if (unset == 0) {
						    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
						    			return 1;
						    		}
						    	}*/
							}
						}
					}
				}
	    		if (numRemoved == 0) {
	    			continue;
	    		}
				if (sequence() == 1) {
	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
	    			return 1;
				}
			}
		}
		return 0;
	}
	
	
	public int hiddenSet() {
	    for (int i = 0; i < rows; i++){
    		for (int k = 0; k < cols; k++) {
    			valuePos[k] = "";
    			numPos[k] = 0;
    		}
	    	for (int j = 0; j < cols; j++) {
	    		for (int k = 0; k < cols; k++) {
	    			if (possibilities[i * cols + j][k] == 1 && temporary[i * cols + j] == 0) {
	    				valuePos[k] += "1";
	    				numPos[k]++;
	    			} else {
	    				valuePos[k] += "0";
	    			}
	    		}
		    }
			for (int k1 = 0; k1 < cols; k1++) {
				Set<Integer> sameRowValues = new HashSet<Integer>();
				if (hiddenSetForRow(k1, i, sameRowValues) == 1) {
					if (sequence() == 1) {
		    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
		    			return 1;
					}
				}
			}
		}

	    for (int i = 0; i < cols; i++){
    		for (int k = 0; k < rows; k++) {
    			valuePos[k] = "";
    			numPos[k] = 0;
    		}
	    	for (int j = 0; j < rows; j++) {
	    		for (int k = 0; k < cols; k++) {
	    			if (possibilities[j * cols + i][k] == 1 && temporary[j * cols + i] == 0) {
	    				valuePos[k] += "1";
	    				numPos[k]++;
	    			} else {
	    				valuePos[k] += "0";
	    			}
	    		}
		    }
			for (int k1 = 0; k1 < cols; k1++) {
				Set<Integer> sameColValues = new HashSet<Integer>();
				if (hiddenSetForCol(k1, i, sameColValues) == 1) {
    				if (sequence() == 1) {
    	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
    	    			return 1;
    				}
				}
			}
		}

	    for (int i = 0; i < cols; i++){
			for (int k = 0; k < cols; k++) {
				valuePos[k] = "";
				numPos[k] = 0;
			}
	    	for (int j = 0; j < rows * cols; j++) {
	    		if (i != boxNumber[j]) {
	    			continue;
	    		}
	    		for (int k = 0; k < cols; k++) {
	    			if (possibilities[j][k] == 1 && temporary[j] == 0) {
	    				valuePos[k] += "1";
	    				numPos[k]++;
	    			} else {
	    				valuePos[k] += "0";
	    			}
	    		}
		    }
	    	
			for (int k1 = 0; k1 < cols; k1++) {
				Set<Integer> sameBoxValues = new HashSet<Integer>();
				if (hiddenSetForBox(k1, i, sameBoxValues) == 1) {
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
	    for (int i = 0; i < cols; i++){
	    	int valueRow[] = new int[cols];
	    	int valueCol[] = new int[cols];
	    	for (int val = 0; val < cols; val ++) {
	    		valueRow[val] = -1;
	    		valueCol[val] = -1;
	    	}
	    	for (int j = 0; j < rows * cols; j++) {
	    		int b = boxNumber[j];
	    		if (b != i) {
	    			continue;
	    		} 
	    		if (temporary[j] != 0) {
	    			continue;
	    		}
    	    	for (int val = 0; val < cols; val ++) {
    	    		if (possibilities[j][val] == 0) {
    	    			continue;
    	    		}
    	    		if (valueRow[val] == -1) {
    	    			valueRow[val] = j / cols;
    	    		} else {
    	    			if (valueRow[val] != j / cols) {
	    	    			valueRow[val] = -2;
    	    			}
    	    		}
    	    		if (valueCol[val] == -1) {
    	    			valueCol[val] = j % cols;
    	    		} else {
    	    			if (valueCol[val] != j % cols) {
    	    				valueCol[val] = -2;
    	    			}
    	    		}
    	    	}
	    	}
	    	for (int val = 0; val < cols; val ++) {
	    		if (valueRow[val] != -1 && valueRow[val] != -2) {
	    			int numRemoved = 0;
	    			//System.out.println("Value " + String.valueOf(val) + " u kutiji " + String.valueOf(i) + " u redu " + String.valueOf(valueRow[val]));
	    			for (int j = 0; j < cols; j++) {
	    				if (possibilities[valueRow[val] * cols + j][val] == 1 && boxNumber[valueRow[val] * cols + j] != i) {
	    					if (numRemoved == 0) {
	    			    		String x = "Kandidati za " + String.valueOf(val + 1) + " u kutiji " + String.valueOf(i + 1) + " svi u redu " + String.valueOf(valueRow[val] + 1) + ".\n";
    				    		if (!solvinstr.contains(x)) {
    				    			if (clt == 0) {
    	    		    				difficultyScore += 350;
    	    		    				clt = 1;
    	    		    			} else {
    	    		    				difficultyScore += 200;
    	    		    			}
    				    		}
    				    		solvinstr.add(x);
    				    		solvingInstructions += x;
						    	if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    	    			InformationBox.infoBox(x, "Rješavač");
					    		}
	    			    		/*print();
	    			    		checkIfCorrect();
	    			    		
	    		    			try {
	    		    				TimeUnit.SECONDS.sleep(1);
	    		    			} catch (InterruptedException e) {
	    		    				// TODO Auto-generated catch block
	    		    				e.printStackTrace();
	    		    			}
	    		    			InformationBox.infoBox("Kandidati za " + String.valueOf(val + 1) + " u kutiji " + String.valueOf(i + 1) + " all u redu " + String.valueOf(valueRow[val] + 1), "Rješavač");
	    	    				*/
	    					}
	    					solvingInstructions +=  "Uklanjam mogućnost " + String.valueOf(val + 1) + " iz ćelije (" + String.valueOf(valueRow[val] + 1) + ", " + String.valueOf(j + 1) + ").\n";
	    					if (showSteps == true) {
				    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			InformationBox.infoBox("Uklanjam mogućnost " + String.valueOf(val + 1) + " iz ćelije (" + String.valueOf(valueRow[val] + 1) + ", " + String.valueOf(j + 1) + ").", "Rješavač");
				    		}
		    				possibilities[valueRow[val] * cols + j][val] = 0;
				    		if (sequence() == 1) {
		    	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
		    	    			return 1;
		    				}
		    				numRemoved++;
					    	/*int possibility = 0;
					    	for (int kh = 0; kh < cols; kh++) {
					    		possibility += possibilities[valueRow[val] * cols + j][kh];
						    }
					    	if (possibility == 1) {
					    		difficultyScore += 200 * 1;
					    		changed++;
					    		unset--;
						    	for (int kh = 0; kh < cols; kh++) {
						    		if (possibilities[valueRow[val] * cols + j][kh]== 1) {				    		
						    			solvingInstructions += "Candidate Lines" + String.valueOf(kh + 1) + " (" + String.valueOf(valueRow[val] + 1) + ", " + String.valueOf(j + 1) + ")\n";
						    			temporary[valueRow[val] * cols + j] = kh + 1;
					    		    	field[valueRow[val] * cols + j].setForeground(Color.BLACK);
				    		    		field[valueRow[val] * cols + j].setText(String.valueOf(kh + 1));
				    		    		break;
						    		}
							    }
					    		if (unset == 0) {
					    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					    			return 1;
					    		}
					    	}*/
						}
	    			}
	    		}
	    		if (valueCol[val] != -1 && valueCol[val] != -2) {
	    			int numRemoved = 0;
	    			for (int j = 0; j < rows; j++) {
	    				if (possibilities[j * cols + valueCol[val]][val] == 1 && boxNumber[j * cols + valueCol[val]] != i) {
	    	    			//System.out.println("Value " + String.valueOf(val) + " u kutiji " + String.valueOf(i) + " in col " + String.valueOf(valueCol[val]));
    	    				if (numRemoved == 0) {
    				    		String x = "Kandidati za " + String.valueOf(val + 1) + " u kutiji " + String.valueOf(i + 1) + " svi su u stupcu " + String.valueOf(valueCol[val] + 1) + ".\n";
    			 				if (!solvinstr.contains(x)) {
    				    			if (clt == 0) {
    	    		    				difficultyScore += 350;
    	    		    				clt = 1;
    	    		    			} else {
    	    		    				difficultyScore += 200;
    	    		    			}
    				    		}
    				    		solvinstr.add(x);
    				    		solvingInstructions += x;
						    	if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    	    			InformationBox.infoBox(x, "Rješavač");
					    		}
    				    		/*print();
    				    		checkIfCorrect();
    				    		
    				    		try {
    			    				TimeUnit.SECONDS.sleep(1);
    			    			} catch (InterruptedException e) {
    			    				// TODO Auto-generated catch block
    			    				e.printStackTrace();
    			    			}
    			    			InformationBox.infoBox("Kandidati za " + String.valueOf(val + 1) + " u kutiji " + String.valueOf(i + 1) + " svi su u stupcu " + String.valueOf(valueCol[val] + 1), "Rješavač");
    		    				*/
    	    				}
	    					solvingInstructions += "Uklanjam mogućnost " + String.valueOf(val + 1) + " iz ćelije (" + String.valueOf(j + 1) + ", " + String.valueOf(valueCol[val] + 1) + ").\n";
	    					if (showSteps == true) {
				    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			InformationBox.infoBox("Uklanjam mogućnost " + String.valueOf(val + 1) + " iz ćelije (" + String.valueOf(j + 1) + ", " + String.valueOf(valueCol[val] + 1) + ").", "Rješavač");
				    		}
		    				possibilities[j * cols + valueCol[val]][val] = 0;
				    		if (sequence() == 1) {
		    	    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
		    	    			return 1;
		    				}
		    				numRemoved++;
					    	/*int possibility = 0;
					    	for (int kh = 0; kh < cols; kh++) {
					    		possibility += possibilities[j * cols + valueCol[val]][kh];
						    }
					    	if (possibility == 1) {
					    		difficultyScore += 200 * 1;
					    		changed++;
					    		unset--;
						    	for (int kh = 0; kh < cols; kh++) {
						    		if (possibilities[j * cols + valueCol[val]][kh]== 1) {				    		
						    			solvingInstructions += "Candidate Lines" + String.valueOf(kh + 1) + " (" + String.valueOf(j + 1) + ", " + String.valueOf(valueCol[val] + 1) + ")\n";
						    			temporary[j * cols + valueCol[val]] = kh + 1;
					    		    	field[j * cols + valueCol[val]].setForeground(Color.BLACK);
				    		    		field[j * cols + valueCol[val]].setText(String.valueOf(kh + 1));
				    		    		break;
						    		}
							    }
					    		if (unset == 0) {
					    			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
					    			return 1;
					    		}
					    	}*/
		    			}
					}
	    		}
	    	}
	    }

		return 0;
	}
	int mlt = 0;
	public int multipleLines() {
    	Integer valueRow[][][] = new Integer[cols][cols][rows];
    	Integer valueCol[][][] = new Integer[cols][cols][cols];
    	String valueRowString[][] = new String[cols][cols];
    	String valueColString[][] = new String[cols][cols];
    	Integer valueRowVals[][] = new Integer[cols][cols];
    	Integer valueColVals[][] = new Integer[cols][cols];
	    for (int i = 0; i < cols; i++){
	    	for (int val = 0; val < cols; val ++) {
	    		valueRowString[i][val] = "";
	    		valueColString[i][val] = "";
	    		valueRowVals[i][val] = 0;
	    		valueColVals[i][val] = 0;
		    	for (int j = 0; j < cols; j ++) {
		    		valueRow[i][val][j] = 0;
		    		valueCol[i][val][j] = 0;
		    	}
		    }
	    	for (int j = 0; j < rows * cols; j++) {
	    		int b = boxNumber[j];
	    		if (b != i) {
	    			continue;
	    		} 
    	    	for (int val = 0; val < cols; val ++) {
    	    		if (temporary[j] != 0) {
    	    			continue;
    	    		}
    	    		if (possibilities[j][val] == 1) {
    		    		valueRow[b][val][j / cols] = 1;
    		    		valueCol[b][val][j % cols] = 1;
    	    		}
    	    	}
	    	}
	    }

    	for (int i = 0; i < cols; i++) {
	    	for (int val = 0; val < cols; val ++) {
	    	    for (int j = 0; j < cols; j++){
	    	    	if (valueRow[i][val][j] == 1) {
	    	    		valueRowString[i][val] += "1";
    		    		valueRowVals[i][val]++;
	    	    	} else {
	    	    		valueRowString[i][val] += "0";
	    	    	}
	    	    	if (valueCol[i][val][j] == 1) {
	    	    		valueColString[i][val] += "1";
    		    		valueColVals[i][val]++;
	    	    	} else {
	    	    		valueColString[i][val] += "0";
	    	    	}
	    	    }
	    	}
    	}

    	for (int b1 = 0; b1 < cols; b1++) {
    		for (int val = 0; val < cols; val++){
				Set<Integer> sameRowValues = new HashSet<Integer>();
				Set<Integer> sameColValues = new HashSet<Integer>();
		    	for (int b2 = b1 + 1; b2 < cols; b2 ++) {
			    	if (valueRowString[b1][val].compareTo(valueRowString[b2][val]) == 0 && b2 != b1) {
						sameRowValues.add(b2);
			    	}
			    	if (valueColString[b1][val].compareTo(valueColString[b2][val]) == 0 && b2 != b1) {
						sameColValues.add(b2);
			    	}
		    	}
		    	if (sameRowValues.size() == valueRowVals[b1][val] - 1 && sameRowValues.size() > 0) {
		    		int numChanges = 0;
					for (int i = 0; i < rows; i++) {
						if (valueRow[b1][val][i] == 1) {
							for (int j = 0; j < cols; j++) {
								if (boxNumber[i * cols + j] != b1 && !sameRowValues.contains(boxNumber[i * cols + j]) && possibilities[i * cols + j][val] == 1) {
									if (numChanges == 0) {
							    		String x = "Više reda sadrži " + String.valueOf(val + 1) + " u kutijama";
							    		int y = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameRowValues.contains(hg) || hg == b1) {
							    				if (y > 0 && y != sameRowValues.size()) {
							    					x += ",";
							    				}
							    				if (y == sameRowValues.size()) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y++;
							    			}
							    		}
							    		x += ".\n";
							    		if (!solvinstr.contains(x)) {
							    			if (mlt == 0) {
							    				difficultyScore += 700;
							    				mlt = 1;
							    			} else {
							    				difficultyScore += 400;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
		    				    		solvingInstructions += x;
								    	if (showSteps == true) {
							    		    instructionArea.setText(solvingInstructions);
					    	    			InformationBox.infoBox(x, "Rješavač");
							    		}
									}
			    					solvingInstructions += "Uklanjam mogućnost " + String.valueOf(val + 1) + " iz ćelije (" + String.valueOf(i + 1) + ", " + String.valueOf(j + 1) + ").\n";
									if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    		    		print();
				    	    			InformationBox.infoBox("Uklanjam mogućnost " + String.valueOf(val + 1) + " iz ćelije (" + String.valueOf(i + 1) + ", " + String.valueOf(j + 1) + ").", "Rješavač");
						    		}
									possibilities[i * cols + j][val] = 0;
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
		    	if (sameColValues.size() == valueColVals[b1][val] - 1 && sameColValues.size() > 0) {
		    		int numChanges = 0;
					for (int j = 0; j < cols; j++) {
						if (valueCol[b1][val][j] == 1) {
							for (int i = 0; i < rows; i++) {
								if (boxNumber[i * cols + j] != b1 && !sameColValues.contains(boxNumber[i * cols + j]) && possibilities[i * cols + j][val] == 1) {
									if (numChanges == 0) {
							    		String x = "Više stupaca sadrži " + String.valueOf(val + 1) + " u kutijama";
							    		int y = 0;
							    		for (int hg = 0; hg < cols; hg++) {
							    			if (sameColValues.contains(hg) || hg == b1) {
							    				if (y > 0 && y != sameRowValues.size()) {
							    					x += ",";
							    				}
							    				if (y == sameRowValues.size()) {
							    					x += " i";
							    				}
							    				x += " " + String.valueOf(hg + 1);
							    				y++;
							    			}
							    		}
							    		x += ".\n";
							    		if (!solvinstr.contains(x)) {
							    			if (mlt == 0) {
							    				difficultyScore += 700;
							    				mlt = 1;
							    			} else {
							    				difficultyScore += 400;
							    			}
		    				    		}
		    				    		solvinstr.add(x);
		    				    		solvingInstructions += x;
								    	if (showSteps == true) {
							    		    instructionArea.setText(solvingInstructions);
					    	    			InformationBox.infoBox(x, "Rješavač");
							    		}
						    		}
			    					solvingInstructions += "Uklanjam mogućnost " + String.valueOf(val + 1) + " iz ćelije (" + String.valueOf(i + 1) + ", " + String.valueOf(j + 1) + ").\n";
									if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    		    		print();
				    	    			InformationBox.infoBox("Uklanjam mogućnost " + String.valueOf(val + 1) + " iz ćelije (" + String.valueOf(i + 1) + ", " + String.valueOf(j + 1) + ").", "Rješavač");
						    		}
									possibilities[i * cols + j][val] = 0;
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
		    		if (usedRows[i] == 0 && usedCols[j] == 0 && usedBoxes[b] == 0 && possibilities[i * cols + j][val - 1] != 0 && temporary[i * cols + j] == 0) {
		    			possible++;
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
			    	usedRows[i] = 1;
			    	usedCols[x] = 1;
			    	int b = boxNumber[i * cols + x];
			    	usedBoxes[b] = 1;
	    			solvingInstructions += "Za red " + String.valueOf(i + 1) + ", broj " + String.valueOf(val) + " je jedino moguć u ćeliji (" + String.valueOf(i + 1) + ", " + String.valueOf(x + 1) + ").\n";
		    		if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			InformationBox.infoBox("Za red" + String.valueOf(i + 1) + ", broj " + String.valueOf(val) + " je jedino moguć u ćeliji (" + String.valueOf(i + 1) + ", " + String.valueOf(x + 1) + ").", "Rješavač");
		    		}
			    	temporary[i * cols + x] = val;
    		    	field[i * cols + x].setForeground(Color.BLACK);
		    		field[i * cols + x].setText(String.valueOf(val));
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
		    for (int i = 0; i < cols; i++){ 
		    	if (usedCols[i] == 1) {
		    		continue;
		    	}
		    	int possible = 0;
		    	int x = 0;
		    	for (int j = 0; j < rows; j++) {
		    		int b = boxNumber[j * cols + i];
		    		if (usedRows[j] == 0 && usedCols[i] == 0 && usedBoxes[b] == 0 && possibilities[j * cols + i][val - 1] != 0 && temporary[j * cols + i] == 0) {
		    			possible++;
		    			x = j;
		    		}
			    }
		    	if (possible == 1) {
		    		difficultyScore += 100 * 1;
		    		changed++;
		    		unset--;
			    	for (int k = 0; k < cols; k++) {
			    		possibilities[x * cols + i][k] = 0;
				    }
			    	possibilities[x * cols + i][val - 1] = 1;
			    	usedRows[x] = 1;
			    	usedCols[i] = 1;
			    	int b = boxNumber[x * cols + i];
			    	usedBoxes[b] = 1;
	    			solvingInstructions += "Za stupac " + String.valueOf(i + 1) + ", broj " + String.valueOf(val) + " je jedino moguć u ćeliji (" + String.valueOf(x + 1) + ", " + String.valueOf(i + 1) + ").\n";
	    			if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			InformationBox.infoBox("Za stupac " + String.valueOf(i + 1) + ", broj " + String.valueOf(val) + " je jedino moguć u ćeliji (" + String.valueOf(x + 1) + ", " + String.valueOf(i + 1) + ").", "Rješavač");
		    		}
	    			temporary[x * cols + i] = val;
    		    	field[x * cols + i].setForeground(Color.BLACK);
		    		field[x * cols + i].setText(String.valueOf(val));
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
		    for (int i = 0; i < cols; i++){ 
		    	if (usedBoxes[i] == 1) {
		    		continue;
		    	}
		    	int possible = 0;
		    	int x = 0;
		    	for (int j = 0; j < rows * cols; j++) {
		    		int b = boxNumber[j];
		    		if (b != i) {
		    			continue;
		    		}
		    		if (usedRows[j / cols] == 0 && usedCols[j % cols] == 0 && usedBoxes[b] == 0 && possibilities[j][val - 1] != 0 && temporary[j] == 0) {
		    			possible++;
		    			x = j;
		    		}
			    }
		    	if (possible == 1) {
		    		difficultyScore += 100 * 1;
		    		changed++;
		    		unset--;
			    	for (int k = 0; k < cols; k++) {
			    		possibilities[x][k] = 0;
				    }
			    	possibilities[x][val - 1] = 1;
			    	usedRows[x / cols] = 1;
			    	usedCols[x % cols] = 1;
			    	usedBoxes[boxNumber[x]] = 1;
	    			solvingInstructions += "Za kutiju " + String.valueOf(boxNumber[x] + 1) + ", broj " + String.valueOf(val) + " je jedino moguć u ćeliji (" + String.valueOf(x / cols + 1) + ", " + String.valueOf(x % cols + 1) + ").\n";
	    			if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			InformationBox.infoBox("Za kutiju " + String.valueOf(boxNumber[x] + 1) + ", broj " + String.valueOf(val) + " je jedino moguć u ćeliji (" + String.valueOf(x / cols + 1) + ", " + String.valueOf(x % cols + 1) + ").", "Rješavač");
		    		}
	    			temporary[x] = val;
    		    	field[x].setForeground(Color.BLACK);
		    		field[x].setText(String.valueOf(val));
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
		if (multipleLines() == 1) {
			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
			return 1;
		}
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
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
		    	temporary[num] = userInput[num];
	    		if (temporary[num] == 0) {
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
			//System.out.println(String.valueOf(unset) + " Ne postoji jedinstveno rješenje");
			return 0;
		} 
		
		difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");			
		//System.out.println(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");	
		return 1;*/
		solvingInstructions += "Počinjem pogađati.\n";
			//forcingChains();
		int g = guessing();
		numIter = 0;
		while (g == 1) {	
			g = guessing();
			numIter++;
			if (numIter == rows * cols) {
				break;
			}
		}
		print();
		if (0 != unset) {
			difficulty.setText(String.valueOf(unset) + " Ne postoji jedinstveno rješenje");
			//System.out.println(String.valueOf(unset) + " Ne postoji jedinstveno rješenje");
			return 0;
		} 
		difficulty.setText(String.valueOf(difficultyScore) + " Postoji verzija rješenja");			
		//System.out.println(String.valueOf(difficultyScore) + " Postoji verzija rješenje");	
		return 1;
	}
	
	public int guessing() {
		int[] t2 = new int[rows * cols];
		int[][] v2 = new int[rows * cols][cols];
		int u1 = unset;
		int d1 = difficultyScore;
		String s = solvingInstructions;
	    for (int ix = 0; ix < rows; ix++){
	    	for (int jx = 0; jx < cols; jx++) {
	    		t2[ix * cols + jx] =  temporary[ix * cols + jx];
	    		for (int v = 0; v < cols; v++) {
	    			v2[ix * cols + jx][v] = possibilities[ix * cols + jx][v];
	    		}
			}
		}
		for (int val = 0; val < rows; val++) {
		    for (int i = 0; i < rows; i++){
		    	int possible = 0;
		    	for (int j = 0; j < cols; j++) {
		    		if (possibilities[i * cols + j][val] == 1 && temporary[i * cols + j] == 0) {
		    			possible++;
		    		}
		    		if (temporary[i * cols + j] == val + 1) {
		    			possible = -1;
		    			break;
		    		}
			    }
		    	if (possible == -1) {
		    		continue;
		    	}
		    	if (possible == 0) {
				    for (int ix = 0; ix < rows; ix++){
				    	for (int jx = 0; jx < cols; jx++) {
				    		temporary[ix * cols + jx] = t2[ix * cols + jx];
				    		for (int v = 0; v < cols; v++) {
				    			possibilities[ix * cols + jx][v] = v2[ix * cols + jx][v];
				    		}
	    				}
	    			}
				    difficultyScore = d1;
				    solvingInstructions = s;
				    unset = u1;
		    		return 1;
		    	}
		    	int randomCol = ThreadLocalRandom.current().nextInt(0, cols);
		    	while (possibilities[i * cols + randomCol][val] == 0 || temporary[i * cols + randomCol] != 0) {
		    		randomCol = ThreadLocalRandom.current().nextInt(0, cols);
		    	}
				solvingInstructions += "Pokušavam " + String.valueOf(val + 1) + " u ćeliji (" + String.valueOf(i + 1) + ", " + String.valueOf(randomCol + 1) + ").\n";
		    	temporary[i * cols + randomCol] = val + 1;
	    		for (int v = 0; v < cols; v++) {
	    			possibilities[i * cols + randomCol][v] = 0;
	    		}
    			possibilities[i * cols + randomCol][val] = 1;
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
	    for (int i = 0; i < rows; i++){
	    	for (int j = 0; j < cols; j++) {
    			for (int v = 0; v < cols; v++) {
    				pv[i * cols + j][v] = 0;
    			}
	    	}
	    }
	    for (int i = 0; i < rows; i++){
	    	for (int j = 0; j < cols; j++) {
	    		if (temporary[i * cols + j] != 0) {
	    			for (int v = 0; v < cols; v++) {
	    				if (possibilities[i * cols + j][v] == 1) {
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
	    					temporary[i * cols + j] = v;
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
			    				solvingInstructions += "Cell (" + String.valueOf(i + 1) + ", " + String.valueOf(j + 1) + ") forces value in cell " + "(" + String.valueOf(i2 + 1) + ", " + String.valueOf(j2 + 1) + ") to be " + String.valueOf(z) + ".\n";
			    			}
				    	}
				    }
	    		}
		    }
		}
		return 0;
	}
	
	/*public int isOnlyOneSolution2() {
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
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
		    	temporary[num] = userInput[num];
	    		if (temporary[num] == 0) {
	    			unset++;
	    		}
	    	}
	    }
	    
		possibilities = new int[rows * cols][rows];
		changed = 1;
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
	    		}
	    	}
	    }
		while (numIter < rows * cols && changed != 0) {
			numIter++;
			changed = 0;
			if (singlePosition() == 1) {
				difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");	
				//System.out.println(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");	
				return 1;
			}
			if (singleCandidate() == 1) {
				difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");	
				//System.out.println(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");	
				return 1;
			}
		}
		int g = guessing();
		while (g == 1) {	
			g = guessing();
		}
		print();
		if (0 != unset) {
			difficulty.setText(String.valueOf(unset) + " Ne postoji jedinstveno rješenje");
			//System.out.println(String.valueOf(unset) + " Ne postoji jedinstveno rješenje");
			return 0;
		} 
		difficulty.setText(String.valueOf(difficultyScore) + " Postoji verzija rješenja");			
		//System.out.println(String.valueOf(difficultyScore) + " Postoji verzija rješenje");	
		return 1;
	}*/
	
	public void print() {
		for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
	    		String t = "<html>";
	    		int z = 0;
		    	for (int k = 0; k < cols; k++) {
		    		if (possibilities[i * cols + j][k] == 1 || temporary[i * cols + j] == k + 1) {
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
	    			field[num].setForeground(Color.WHITE);
	    		}
	    		//userInput[num] = temporary[num];
	    		//solution[num] = temporary[num];
	    	}
	    }
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
    	        	field[num].setForeground(Color.WHITE);
		    	}
		    }
    		return;
    	}
	    int retval = randomPuzzle();
	    while (retval == 1) {
		    for (int i = 0; i < rows; i++){ 
		    	for (int j = 0; j < cols; j++) {
			    	int num = i * cols + j;
			    	temporary[num] = userInput[num];
		    	}
		    }
		    retval = randomPuzzle();
	    } 
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
	    		userInput[num] = temporary[num];
	    		solution[num] = temporary[num];
	    		field[num].setText(String.valueOf(userInput[num]));
	        	field[num].setForeground(Color.WHITE);
	    	}
	    }
	}

	abstract public void draw();
	
}
