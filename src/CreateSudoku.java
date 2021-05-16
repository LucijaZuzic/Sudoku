import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JFrame;

public class CreateSudoku extends Sudoku {

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
    	    		if (userInput[numCell] < 10) {
    	    			field[numCell].setText(String.valueOf(userInput[numCell]));
    	    		} else {
    	    			char c = 'A';
    	    			c += userInput[numCell] - 10;
    	    			field[numCell].setText("" + c);
    	    		}
    	        	field[numCell].setForeground(Color.WHITE);
		    	}
		    }
    		return;
    	}
	    long startGen = System.currentTimeMillis();
	    int retVal = randomPuzzle();
	    while (retVal == 1) {
	    	if (System.currentTimeMillis() - startGen >= 10000) {
    		    InformationBox.infoBox("Nije moguæe ispuniti zagonetku prema zadanim kriterijima.", "Pogrešno dizajnirana zagonetka");
	    		break;
	    	}
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
	    		if (userInput[numCell] < 10) {
	    			field[numCell].setText(String.valueOf(userInput[numCell]));
	    		} else {
	    			char c = 'A';
	    			c += userInput[numCell] - 10;
	    			field[numCell].setText("" + c);
	    		}
	        	field[numCell].setForeground(Color.WHITE);
	    	}
	    }
	}
	
	public void resetHighlight() {
		for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
    			setBackground(row, col, returnColour(row * cols + col));
	    		field[row * cols + col].setFont(field[row * cols + col].getFont().deriveFont(~Font.BOLD | ~Font.ITALIC));
	    	}
	    }
	}
	
	public void highlightCell(int numCell) {
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (col == numCell % cols || row == numCell / cols) {
	    			setBackground(row, col, "one_more_gray");
	    		} else {
	    			setBackground(row, col, returnColour(row * cols + col));
	    		}
	    	}
	    }
	}

	public void highlightDigit() {
		for (int val = 0; val < cols + 1; val++) {
			digitButtons[val].setBackground(Color.WHITE);
		}
		digitButtons[selectedDigit].setBackground(Color.CYAN);
		if (selectedDigit == 0) {
			return;
		}
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (userInput[row * cols + col] == selectedDigit || possibilities[row * cols + col][selectedDigit - 1] == 1) {
	    			if (userInput[row * cols + col] == selectedDigit) {
	    				field[row * cols + col].setFont(field[row * cols + col].getFont().deriveFont(Font.BOLD | Font.ITALIC));
	    			} else {
			    		String text = "<html><p style='text-align: center'><font color = yellow>";
			    		int numberOptions = 0;
				    	for (int val = 0; val < cols; val++) {
				    		if (possibilities[row * cols + col][val] == 1) {
				    			numberOptions++;
				    			if (val == selectedDigit - 1) {
				    				text += "<b><i>";
				    			}
				    			if (val + 1 < 10) {
				    				text += String.valueOf(val + 1) + " ";
				    			} else {
				    				char c = 'A';
				    				c += val - 9;
				    				text += c + " ";
				    			}
				    			if (val == selectedDigit - 1) {
				    				text += "</b></i>";
				    			}
				    			text += " ";
				    		}
					    }
				    	if (numberOptions != 0) {
				    		text = text.substring(0, text.length() - 1) + "</font></p></html>";
				    	} else {
				    		text = "";
				    	}
		        		field[row * cols + col].setText(text);
	    			}
	    		} else {    				    			
	    			field[row * cols + col].setFont(field[row * cols + col].getFont().deriveFont(~Font.BOLD | ~Font.ITALIC));
	    		}
	    	}
	    }
	}
	
	public CreateSudoku(int constructRows, int constructCols, int rowLimit, int colLimit, int[] constructBorder, int[] constructBoxNumber, boolean setDiagonalOn, Set<String> setSizeRelationships) {
		super(constructRows, constructCols, rowLimit, colLimit, setDiagonalOn, setSizeRelationships);
		border = constructBorder;
		boxNumber = constructBoxNumber;
		if (InformationBox.yesNoBox("Želite li da se mreža automatski ispuni?", "Ukljuèi bilješke")) {
		    long startGen = System.currentTimeMillis();
		    int retval = 1;
		    while(retval == 1) {
		    	if (System.currentTimeMillis() - startGen >= 10000) {
	    		    InformationBox.infoBox("Nije moguæe ispuniti zagonetku prema zadanim kriterijima.", "Pogrešno dizajnirana zagonetka");
	    		    /*frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	    		    frame.removeAll();
	    		    frame.dispose();
	    		    frame.setVisible(false);
	    		    return;*/
		    		break;
		    	}
			    for (int row = 0; row < rows; row++){ 
			    	for (int col = 0; col < cols; col++) {
			    		temporary[row * cols + col] = 0;
				    }
			    }
			    retval = randomPuzzle();
		    }
		} else {
		    for (int row = 0; row < rows; row++){ 
		    	for (int col = 0; col < cols; col++) {
		    		temporary[row * cols + col] = 0;
			    }
		    }
		}
	    draw();
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
	    		userInput[numCell] = temporary[numCell];
	    		solution[numCell] = temporary[numCell];
	    		if (userInput[numCell] < 10) {
	    			field[numCell].setText(String.valueOf(userInput[numCell]));
	    		} else {
	    			char c = 'A';
	    			c += userInput[numCell] - 10;
	    			field[numCell].setText("" + c);
	    		}
	    	}
	    }
	    assume();
		checkBoxes();
	    checkIfCorrect();
		for (int digit = 0; digit < cols + 1; digit++) {
			numUseDigit[digit] = 0;
		}
		for (int cell = 0; cell < rows * cols; cell++){
	    	numUseDigit[userInput[cell]]++;
	    }
		for (int digit = 1; digit < cols + 1; digit++) {
			checkIfDigitMaxUsed(digit);
		}
	    frame.setVisible(true);
	    frame.requestFocus();
	}
	

	public CreateSudoku(int constructRows, int constructCols, int rowLimit, int colLimit, int[] constructBorder, int[] constructBoxNumber, boolean setDiagonalOn, Set<String> setSizeRelationships, int constructUserInput[], String oldLastUsedPath) {
		super(constructRows, constructCols, rowLimit, colLimit, setDiagonalOn, setSizeRelationships);
		lastUsedPath = oldLastUsedPath;
		border = constructBorder;
		boxNumber = constructBoxNumber;
	    draw();
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
	    		userInput[numCell] = constructUserInput[numCell];
	    		solution[numCell] = 0;
	    		if (userInput[numCell] < 10) {
	    			field[numCell].setText(String.valueOf(userInput[numCell]));
	    		} else {
	    			char c = 'A';
	    			c += userInput[numCell] - 10;
	    			field[numCell].setText("" + c);
	    		}
	    	}
	    }
	    assume();
		checkBoxes();
	    checkIfCorrect();
		for (int digit = 0; digit < cols + 1; digit++) {
			numUseDigit[digit] = 0;
		}
		for (int cell = 0; cell < rows * cols; cell++){
	    	numUseDigit[userInput[cell]]++;
	    }
		for (int digit = 1; digit < cols + 1; digit++) {
			checkIfDigitMaxUsed(digit);
		}
	    frame.setVisible(true);
	    frame.requestFocus();
	}
	
    @Override
	public boolean checkIfCorrect() {
		String errorText = "";
		boolean incorrect[] = new boolean[rows * cols];
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
		    	temporary[numCell] = userInput[numCell];
		    	incorrect[numCell] = false;
	    	}
	    }
		possibilities = new int[rows * cols][rows];
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
	    		}
	    	}
	    }
		fixPencilmarks();
		boolean correct = true;
		for (int val = 1; val <= rows; val++) {
			int[] usedRows = new int[rows];
			int[] usedCols = new int[cols];
			int[] usedBoxes = new int[rows];
		    int usedFirstDiagonal = 0;
		    int usedSecondDiagonal = 0;
		    for (int row = 0; row < rows; row++){
		    	for (int col = 0; col < cols; col++) {
		    		usedRows[row] = 0;
		    		usedCols[col] = 0;
		    		usedBoxes[row] = 0;
			    }
		    }
		    for (int row = 0; row < rows; row++){
		    	for (int col = 0; col < cols; col++) {
		    		boolean status = false;
		    		if (temporary[row * cols + col] == val) {
			    		usedRows[row]++;
			    		usedCols[col]++;
			    		usedBoxes[boxNumber[row * cols + col]]++;
			    		if (row == col && diagonalOn) {
			    			usedFirstDiagonal++;
			    		}
			    		if (row == cols - 1 - col  && diagonalOn) {
			    			usedSecondDiagonal++;
			    		}
			    		if (usedRows[row] > 1) {
			    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " veæ postoji u retku " + (row + 1) + ".\n";
			    			correct = false;
			    			status = true;
			    			incorrect[row * cols + col] = true;
			    		}
			    		if (usedCols[col] > 1) {
			    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " veæ postoji u stupcu " + (col + 1) + ".\n";
			    			correct = false;
			    			status = true;
			    			incorrect[row * cols + col] = true;
			    		}
			    		if (usedBoxes[boxNumber[row * cols + col]] > 1) {
			    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " veæ postoji u kutiji " + (boxNumber[row * cols + col] + 1) + ".\n";
			    			correct = false;
			    			status = true;
			    			incorrect[row * cols + col] = true;
			    		}
			    		if (row == col  && usedFirstDiagonal > 1 && diagonalOn) {
			    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " veæ postoji u rastuæoj dijagonali.\n";
			    			correct = false;
			    			status = true;
			    			incorrect[row * cols + col] = true;
			    		}
			    		if (row == cols - 1 - col  && usedSecondDiagonal > 1 && diagonalOn) {
			    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " veæ postoji u padajuæoj dijagonali.\n";
			    			correct = false;
			    			status = true;
			    			incorrect[row * cols + col] = true;
			    		}
			    		for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
			    			for (int colOffset = -1; colOffset <= 1; colOffset++) {
			    				int numCell = row * cols + col;
			    		        int newCell = (numCell / cols + rowOffset) * cols + numCell % cols + colOffset;
			    				if (neighbourCheck(numCell, newCell)) {
			    					String relationshipCell = String.valueOf(row * cols + col) + " " + String.valueOf(newCell);
					    			if (sizeRelationships.contains(relationshipCell) && temporary[row * cols + col] <= temporary[newCell]) {
						    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije veæi od broja " + temporary[newCell] + " u æeliji (" + (row + 1) + ", " + (col + 2) + ").\n";
						    			correct = false;
						    			status = true;
						    			incorrect[row * cols + col] = true;
						    			incorrect[newCell] = true;
					    			} 
					    			relationshipCell = String.valueOf(newCell) + " " + String.valueOf(row * cols + col);
					    			if (sizeRelationships.contains(relationshipCell) && temporary[newCell] != 0 && temporary[row * cols + col] >= temporary[newCell]) {
						    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije manji od broja " + temporary[newCell] + " u æeliji (" + (row + 1) + ", " + (col + 2) + ").\n";
						    			correct = false;
						    			status = true;
						    			incorrect[row * cols + col] = true;
						    			incorrect[newCell] = true;
					    			} 
			    				}
			    			}
			    		}
			    		if (status) {
			    			for (int sameCol = 0; sameCol < rows; sameCol++) {
				    			int numCell = sameCol * cols + col;
				    			if (temporary[numCell] == val) {
				    				incorrect[numCell] = true;
				    			}
				    		}
				    		for (int sameRow = 0; sameRow < cols; sameRow++) {
				    			int numCell = row * cols + sameRow;
				    			if (temporary[numCell] == val) {
				    				incorrect[numCell] = true;
				    			}
				    		}
						    for (int x = (row / yLim) * (cols / xLim); x < (row / yLim + 1) * (cols / xLim); x++){
						    	for (int y = (col / xLim) * (cols / xLim); y < (col / xLim + 1) * (cols / xLim); y++) {
					    			int numCell = x * cols + y;
						    		if (temporary[numCell] == val) {
					    				incorrect[numCell] = true;
						    		}
						    	}
						    }
						    if (row == col && diagonalOn) {
						    	for (int diagonally = 0; diagonally < cols; diagonally++) {
						    		int numCell = diagonally * cols + diagonally;
						    		if (temporary[numCell] == val) {
					    				incorrect[numCell] = true;
						    		}
						    	}
						    }
						    if (row == cols - 1 - col && diagonalOn) {
						    	for (int diagonally = 0; diagonally < cols; diagonally++) {
						    		int numCell = diagonally * cols + cols - 1 - diagonally;
						    		if (temporary[numCell] == val) {
					    				incorrect[numCell] = true;
						    		}
						    	}
						    }
			    		}
		    		}
			    }
		    }
		}
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		int numPossibilities = 0;
	    		if (temporary[row * cols + col] == 0) {
			    	for (int val = 0; val < cols; val++) {
			    		if (possibilities[row * cols + col][val] == 1) {
			    			numPossibilities++;
			    		}
				    }
			    	if (numPossibilities == 0) {
		    			errorText += "Æelija (" + (row + 1) + ", " + (col + 1) + ") nema moguæih vrijednosti.\n";
	    				incorrect[row * cols + col] = true;
		    			correct = false;
			    	}
	    		}
	    	}
	    }
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
    			int numCell = row * cols + col;
    			if (temporary[numCell] != 0) {
    			    field[numCell].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
    	    		if (incorrect[numCell]) {
        				field[numCell].setForeground(Color.ORANGE);
    	    		} else {
    	    			field[numCell].setForeground(Color.WHITE);
    	    		}
	    		}
		    }
	    }
	    errorArea.setText(errorText);
		return correct;
	}



/*public void keyPressed(KeyEvent e) {

    int key = e.getKeyCode();

    //System.out.println(key);
}*/
    @Override
	public void assume() {
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
		    	temporary[numCell] = userInput[numCell];
	    	}
	    }
		possibilities = new int[rows * cols][rows];
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
	    		}
	    	}
	    }
		fixPencilmarks();
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
	    		if (userInput[numCell] != 0) {
	    			continue;
	    		}
	    		String text = "<html><p style='text-align: center'><font color = yellow>";
			    field[numCell].setFont(new Font("Arial", Font.PLAIN, guessFontsize));
	    		int numberOptions = 0;
		    	for (int val = 1; val <= cols; val++) {
		    		if (possibilities[numCell][val - 1] == 1) {
		    			numberOptions++;
		    			if (val < 10) {
		    				text += String.valueOf(val) + " ";
		    			} else {
		    				char c = 'A';
		    				c += val - 10;
		    				text += c + " ";
		    			}
		    		}
			    }
		    	if (numberOptions != 0) {
		    		text = text.substring(0, text.length() - 1) + "</font></p></html>";
		    	} else {
		    		text = "";
		    	}
        		field[numCell].setText(text);
	    	}
	    }
	}
    
    @Override
	public ActionListener makeActionListener(int numCell) {
		return new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (userInput[numCell] != selectedDigit) {
	        			numUseDigit[userInput[numCell]]--;
	        			numUseDigit[selectedDigit]++;
	        			if (userInput[numCell] != 0) {
	        				checkIfDigitMaxUsed(userInput[numCell]);
	        			}
	        			if (selectedDigit != 0) {
	        				checkIfDigitMaxUsed(selectedDigit);
	        			}
	        		}
	        		userInput[numCell] = selectedDigit;
				    field[numCell].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
	        		if (selectedDigit != 0) {
        	    		if (selectedDigit < 10) {
	    	    			field[numCell].setText(String.valueOf(selectedDigit));
	    	    		} else {
	    	    			char c = 'A';
	    	    			c += selectedDigit - 10;
	    	    			field[numCell].setText("" + c);
	    	    		}
	        		}
	        		assume();
	        		highlightCell(numCell);
					highlightDigit();
	        		checkIfCorrect();
				} catch (Exception e1) {

				}
	        }  
	    };
	}

    @Override
	public ActionListener makeDigitActionListener(int digit) {
    	return new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		selectedDigit = digit;
					resetHighlight();
					highlightDigit();
	        		checkIfCorrect();
				} catch (Exception e1) {
	
	
				}
	        }  
	    };
    }
    
	@Override
	public void draw () 
    {
		frame = new JFrame("Stvori sudoku");  
	    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
	    frame.addKeyListener(keyListener);
	    int returnX = makeButtons();
	    int digitEnd = makeDigitButtons();
	    h = h / 2;
	    w = (int) (200 * widthScaling);
	    y = space;
	    x = returnX + space;
	 	makeAButton("Prikaži korake", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		showSteps = true;
		        	    if (!InformationBox.stepBox("Jeste li spremni za prikaz koraka?", "Korak po korak")) {
		        	    	showSteps = false;
		        	    }
		        		isOnlyOneSolution();
		        	    instructionArea.setText(solvingInstructions);
		        		checkIfCorrect();
		        		showSteps = false;
					} catch (Exception e1) {
		
		
					}
		        }  
	    });
		y += h + space;
	 	makeAButton("Isprazni mrežu", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        	    for (int row = 0; row < rows; row++){
		        	    	for (int col = 0; col < cols; col++) {
		        	        	int numCell = row * cols + col;
		        	    		temporary[numCell] = 0;
		        	    		solution[numCell] = 0;
		        	    		userInput[numCell] = 0;
		        		    }
		        	    }
		        		for (int digit = 0; digit < cols + 1; digit++) {
		        			numUseDigit[digit] = 0;
		        		}
		        		for (int digit = 1; digit < cols + 1; digit++) {
		        			checkIfDigitMaxUsed(digit);
		        		}
		        		assume();
		        		resetHighlight();
					} catch (Exception e1) {
		
		
					}
		        }  
	    });
		y += h + space;
	 	makeAButton("Nasumièmo nadopuni", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		fill();
		        		for (int digit = 0; digit < cols + 1; digit++) {
		        			numUseDigit[digit] = 0;
		        		}
		        		for (int cell = 0; cell < rows * cols; cell++){
		        	    	numUseDigit[userInput[cell]]++;
		        	    }
		        		for (int digit = 1; digit < cols + 1; digit++) {
		        			checkIfDigitMaxUsed(digit);
		        		}
		        		checkIfCorrect();
		        		difficulty.setText("");
					} catch (Exception e1) {
		
					}
		        }  
		    });
		y += h + space;
	 	makeAButton("Simetrièno ukloni", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		removeSymetricPair();
		        		assume();
		        		checkIfCorrect();
		        	} catch (Exception e1) {
		
					}
		        }  
		    });
		y += h + space;
	 	makeAButton("Vrati uklonjeno", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		restoreLastRemoved();
		        		assume();
		        		checkIfCorrect();
		        	} catch (Exception e1) {
		
					}
		        }  
		    });
		y += h + space;
	 	makeAButton("Spremi zagonetku", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		writeToFile("");
		        	} catch (Exception e1) {
		
					}
		        }  
		    });
		y += h + space;
	 	makeAButton("Uèitaj zagonetku", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (readFile("") == 0) {
		        	    for (int row = 0; row < rows; row++){ 
		        	    	for (int col = 0; col < cols; col++) {
		        		    	int numCell = row * cols + col;
		        	    		solution[numCell] = 0;
		        	    		if (userInput[numCell] < 10) {
			    	    			field[numCell].setText(String.valueOf(userInput[numCell]));
			    	    		} else {
			    	    			char c = 'A';
			    	    			c += userInput[numCell] - 10;
			    	    			field[numCell].setText("" + c);
			    	    		}
		        	    	}
		        	    }
		        		checkBoxes();
		        		assume();
		        	    checkIfCorrect();
		        		for (int digit = 0; digit < cols + 1; digit++) {
		        			numUseDigit[digit] = 0;
		        		}
		        		for (int cell = 0; cell < rows * cols; cell++){
		        	    	numUseDigit[userInput[cell]]++;
		        	    }
		        		for (int digit = 1; digit < cols + 1; digit++) {
		        			checkIfDigitMaxUsed(digit);
		        		}
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		y += h + space;
	 	makeAButton("Riješi zagonetku", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		boolean correct = checkIfCorrect();
		        	    // Ako zagonetka nije ispravno zadana, ne možemo ju rješavati
		            	if (!correct) {
		        			difficulty.setText("U zagonetki ima grešaka");
		            		return;
		            	}
	        			@SuppressWarnings("unused")
						SolveSudoku SolveSudoku = new SolveSudoku(rows, cols, xLim, yLim, border, boxNumber, diagonalOn, sizeRelationships, userInput);
					} catch (Exception e1) {
		
		
					}
		        }  
		    });
		y += h + space;
		w = (int) (250 * widthScaling);
        difficulty.setBounds(x, y, w, h);
        difficulty.setFont(new Font("Arial", Font.PLAIN, fontsize));
        frame.add(difficulty);
		int buttonEnd = y + h + space;
        
		addErrorScroll(digitEnd, buttonEnd);
		addInstructionScroll(digitEnd, buttonEnd);

	    frame.setSize(x, Math.max(digitEnd, buttonEnd) + (int) (40 * heightScaling));  
	    frame.setLayout(null);  
    }
}
