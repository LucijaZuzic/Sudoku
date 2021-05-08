import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class CreateSudoku extends Sudoku {

	FileManipulator fileManipulator = new FileManipulator();
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
	    		field[numCell].setText(String.valueOf(userInput[numCell]));
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
			    		String text = "<html><font color = yellow>";
			    		int numberOptions = 0;
				    	for (int val = 0; val < cols; val++) {
				    		if (possibilities[row * cols + col][val] == 1) {
				    			numberOptions++;
				    			if (val == selectedDigit - 1) {
				    				text += "<row><b><i>";
				    			}
				    			text += String.valueOf(val + 1);
				    			if (val == selectedDigit - 1) {
				    				text += "</row></b></i>";
				    			}
				    			if (numberOptions % 3 == 0) {
				    				text += "<br />";
				    			} else {
				    				text += " ";
				    			}
				    		}
					    }
				    	if (numberOptions != 0) {
				    		text = text.substring(0, text.length() - 1) + "</font></html>";
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
	    		field[numCell].setText(String.valueOf(userInput[numCell]));
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
	

	public CreateSudoku(int constructRows, int constructCols, int rowLimit, int colLimit, int[] constructBorder, int[] constructBoxNumber, boolean setDiagonalOn, Set<String> setSizeRelationships, int constructUserInput[], FileManipulator oldFileManipulator) {
		super(constructRows, constructCols, rowLimit, colLimit, setDiagonalOn, setSizeRelationships);
		fileManipulator = oldFileManipulator;
		border = constructBorder;
		boxNumber = constructBoxNumber;
	    draw();
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
	    		userInput[numCell] = constructUserInput[numCell];
	    		solution[numCell] = 0;
	    		field[numCell].setText(String.valueOf(userInput[numCell]));
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
			    		int rightCell = row * cols + col + 1;
			    		if (rightCell < rows * cols) {
			    			String relationshipRightCell = String.valueOf(row * cols + col) + " " + String.valueOf(rightCell);
			    			if (sizeRelationships.contains(relationshipRightCell) && temporary[row * cols + col] <= temporary[rightCell]) {
				    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije veæi od broja " + temporary[rightCell] + " u æeliji (" + (row + 1) + ", " + (col + 2) + ").\n";
				    			correct = false;
				    			status = true;
				    			incorrect[row * cols + col] = true;
				    			incorrect[rightCell] = true;
			    			} 
			    			relationshipRightCell = String.valueOf(rightCell) + " " + String.valueOf(row * cols + col);
			    			if (sizeRelationships.contains(relationshipRightCell) && temporary[rightCell] != 0 && temporary[row * cols + col] >= temporary[rightCell]) {
				    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije manji od broja " + temporary[rightCell] + " u æeliji (" + (row + 1) + ", " + (col + 2) + ").\n";
				    			correct = false;
				    			status = true;
				    			incorrect[row * cols + col] = true;
				    			incorrect[rightCell] = true;
			    			} 
			    		}
			    		int leftCell = row * cols + col - 1;
			    		if (leftCell >= 0) {
			    			String relationshipLeftCell = String.valueOf(row * cols + col) + " " + String.valueOf(leftCell);
			    			if (sizeRelationships.contains(relationshipLeftCell) && temporary[row * cols + col] <= temporary[leftCell]) {
				    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije veæi od broja " + temporary[leftCell] + " u æeliji (" + (row + 1) + ", " + (col) + ").\n";
				    			correct = false;
				    			status = true;
				    			incorrect[row * cols + col] = true;
				    			incorrect[leftCell] = true;
			    			} 
			    			relationshipLeftCell = String.valueOf(leftCell) + " " + String.valueOf(row * cols + col);
			    			if (sizeRelationships.contains(relationshipLeftCell) && temporary[leftCell] != 0 && temporary[row * cols + col] >= temporary[leftCell]) {
				    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije manji od broja " + temporary[leftCell] + " u æeliji (" + (row + 1) + ", " + (col) + ").\n";
				    			correct = false;
				    			status = true;
				    			incorrect[row * cols + col] = true;
				    			incorrect[leftCell] = true;
			    			} 
			    		}
			    		int bottomCell = row * cols + col + cols;
			    		if (bottomCell < rows * cols) {
			    			String relationshipBottomCell = String.valueOf(row * cols + col) + " " + String.valueOf(bottomCell);
			    			if (sizeRelationships.contains(relationshipBottomCell) && temporary[row * cols + col] <= temporary[bottomCell]) {
				    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije veæi od broja " + temporary[bottomCell] + " u æeliji (" + (row + 2) + ", " + (col + 1) + ").\n";
				    			correct = false;
				    			status = true;
				    			incorrect[row * cols + col] = true;
				    			incorrect[bottomCell] = true;
			    			} 
			    			relationshipBottomCell = String.valueOf(leftCell) + " " + String.valueOf(row * cols + col);
			    			if (sizeRelationships.contains(relationshipBottomCell) && temporary[bottomCell] != 0 && temporary[row * cols + col] >= temporary[bottomCell]) {
				    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije manji od broja " + temporary[bottomCell] + " u æeliji (" + (row + 2) + ", " + (col + 1) + ").\n";
				    			correct = false;
				    			status = true;
				    			incorrect[row * cols + col] = true;
				    			incorrect[bottomCell] = true;
			    			} 
			    		}
			    		int topCell = row * cols + col - cols;
			    		if (topCell >= 0) {
			    			String relationshipTopCell = String.valueOf(row * cols + col) + " " + String.valueOf(topCell);
			    			if (sizeRelationships.contains(relationshipTopCell) && (temporary[row * cols + col] <= temporary[topCell] || temporary[topCell] == 1)) {
				    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije veæi od broja " + temporary[topCell] + " u æeliji (" + (row + 2) + ", " + (col + 1) + ").\n";
				    			correct = false;
				    			status = true;
				    			incorrect[row * cols + col] = true;
				    			incorrect[topCell] = true;
			    			} 
			    			relationshipTopCell = String.valueOf(leftCell) + " " + String.valueOf(row * cols + col);
			    			if (sizeRelationships.contains(relationshipTopCell) && ((temporary[topCell] != 0 && temporary[row * cols + col] >= temporary[topCell]) || temporary[topCell] == 9)) {
				    			errorText += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije manji od broja " + temporary[topCell] + " u æeliji (" + (row + 2) + ", " + (col + 1) + ").\n";
				    			correct = false;
				    			status = true;
				    			incorrect[row * cols + col] = true;
				    			incorrect[topCell] = true;
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
	    		String text = "<html><font color = yellow>";
			    field[numCell].setFont(new Font("Arial", Font.PLAIN, fontsize));
	    		int numberOptions = 0;
		    	for (int val = 1; val <= cols; val++) {
		    		if (possibilities[numCell][val - 1] == 1) {
		    			numberOptions++;
		    			text += String.valueOf(val);
		    			if (numberOptions % 3 == 0) {
		    				text += "<br />";
		    			} else {
		    				text += " ";
		    			}
		    		}
			    }
		    	if (numberOptions != 0) {
		    		text = text.substring(0, text.length() - 1) + "</font></html>";
		    	} else {
		    		text = "";
		    	}
        		field[numCell].setText(text);
	    	}
	    }
	}
    

	KeyListener keyListener  =
	new KeyListener(){
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			if (key - 48 >= 0 && key - 48 <= 9) {
				selectedDigit = key - 48;
				resetHighlight();
				highlightDigit();
			}
    		checkIfCorrect();
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};
	@Override
	public void draw () 
    {
		frame = new JFrame("Stvori sudoku");  
	    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
	    frame.addKeyListener(keyListener);
	    for (int row = 0; row < rows; row++){ 
	    	x = space;
	    	for (int col = 0; col < cols; col++) {
	    		int numCell = row * cols + col;
	    	    field[numCell] = new JButton(String.valueOf("0"));  
			    field[numCell].setMargin(new Insets(1,1,1,1));
			    field[numCell].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
			    field[numCell].setBounds(x, y, w, h);
			    
			    field[numCell].addActionListener(new ActionListener(){  
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
						    	field[numCell].setText(String.valueOf(selectedDigit));
			        		}
			        		assume();
			        		highlightCell(numCell);
							highlightDigit();
			        		checkIfCorrect();
						} catch (Exception e1) {
		
						}
			        }  
			    });
			    field[numCell].addKeyListener(keyListener);
			    frame.add(field[numCell]);
		    	x += w;
		    }
		    y += h;
	    }
	    x = space;
	    y += space;
		digitButtons = new JButton[cols + 1];
		for (int row = 0; row < cols + 1; row++) {
			digitButtons[row] = new JButton(String.valueOf(row));  
			digitButtons[row].setMargin(new Insets(1,1,1,1));
			if (row != selectedDigit) {
				digitButtons[row].setBackground(Color.WHITE);
			} else {
				digitButtons[row].setBackground(Color.CYAN);
			}
			digitButtons[row].setBounds(x, y, w, h);
			digitButtons[row].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
	        if (row == 0) {
	        	digitButtons[row].setForeground(Color.RED);
	        }
	        int digit = row;
	        digitButtons[row].addActionListener(new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		selectedDigit = digit;
						resetHighlight();
						highlightDigit();
		        		checkIfCorrect();
					} catch (Exception e1) {
		
		
					}
		        }  
		    });
	        digitButtons[row].addKeyListener(keyListener);
	        frame.add(digitButtons[row]);
	        x += w;
		}
		int digitEnd = y + h + space;
	    h = h / 2;
	    w = (int) (200 * widthScaling);
	    y = space;
	    x += space;
        JButton showStepButton = new JButton("Prikaži korake");  
        showStepButton.setMargin(new Insets(1,1,1,1));
        showStepButton.setBounds(x, y, w, h);
        showStepButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        showStepButton.addActionListener(new ActionListener(){  
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
        showStepButton.addKeyListener(keyListener);
		y += h + space;
		JButton clearGridButton = new JButton("Isprazni mrežu");  
        clearGridButton.setMargin(new Insets(1,1,1,1));
        clearGridButton.setBounds(x, y, w, h);
        clearGridButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        clearGridButton.addActionListener(new ActionListener(){  
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
		clearGridButton.addKeyListener(keyListener);
		y += h + space;
        JButton fillButton = new JButton("Nasumièmo nadopuni");  
		fillButton.setMargin(new Insets(1,1,1,1));
		fillButton.setBounds(x, y, w, h);
		fillButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
		fillButton.addActionListener(new ActionListener(){  
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
		fillButton.addKeyListener(keyListener);
		y += h + space;
        JButton removeButton = new JButton("Simetrièno ukloni");  
        removeButton.setMargin(new Insets(1,1,1,1));
        removeButton.setBounds(x, y, w, h);
        removeButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        removeButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		removeSymetricPair();
	        		assume();
	        		checkIfCorrect();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		removeButton.addKeyListener(keyListener);
		y += h + space;
        JButton restoreButton = new JButton("Vrati uklonjeno");  
        restoreButton.setMargin(new Insets(1,1,1,1));
        restoreButton.setBounds(x, y, w, h);
        restoreButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        restoreButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		restoreLastRemoved();
	        		assume();
	        		checkIfCorrect();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		restoreButton.addKeyListener(keyListener);
		y += h + space;
		fileManipulator.setSudoku(this);
        JButton fileSaveButton = new JButton("Spremi zagonetku");  
        fileSaveButton.setMargin(new Insets(1,1,1,1));
        fileSaveButton.setBounds(x, y, w, h);
        fileSaveButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        fileSaveButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		fileManipulator.WriteToFile();
				} catch (Exception e1) {
					
				}
	        }  
	    });
		fileSaveButton.addKeyListener(keyListener);
		y += h + space;
        JButton fileReadButton = new JButton("Uèitaj zagonetku");  
        fileReadButton.setBounds(x, y, w, h);
        fileReadButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        fileReadButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (fileManipulator.ReadFile() == 0) {
		        	    for (int row = 0; row < rows; row++){ 
		        	    	for (int col = 0; col < cols; col++) {
		        		    	int numCell = row * cols + col;
		        	    		solution[numCell] = 0;
		        	    		field[numCell].setText(String.valueOf(userInput[numCell]));
		        	    	}
		        	    }
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
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		fileReadButton.addKeyListener(keyListener);
		y += h + space;
	    JButton solveButton = new JButton("Riješi zagonetku");  
	 	solveButton.setMargin(new Insets(1,1,1,1));
	 	solveButton.setBounds(x, y, w, h);
	 	solveButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
	 	solveButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
        			@SuppressWarnings("unused")
					SolveSudoku SolveSudoku = new SolveSudoku(rows, cols, xLim, yLim, border, boxNumber, diagonalOn, sizeRelationships, userInput);
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
	 	solveButton.addKeyListener(keyListener);
		y += h + space;
		w = (int) (250 * widthScaling);
        difficulty.setBounds(x, y, w, h);
        difficulty.setFont(new Font("Arial", Font.PLAIN, fontsize));
        frame.add(difficulty);
		int buttonEnd = y + h + space;
        x += w + space;
        w = (int) (350 * widthScaling);
        y = space;
        errorArea = new JTextArea(0, 0);
        errorArea.setFont(new Font("Arial", Font.PLAIN, fontsize));
        errorArea.setEditable (false);
	    JPanel errorPanel = new JPanel();
        errorPanel.add(errorArea, BorderLayout.CENTER);
	    JScrollPane errorScroll = new JScrollPane(errorPanel, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    errorScroll.setBounds(x, y, w, Math.max(digitEnd, buttonEnd) - 2 * space);
	    frame.add(errorScroll);
        x += w + space;
        w = (int) (550 * widthScaling);
	    errorPanel.setVisible(true);  
	    errorPanel.setBackground(Color.WHITE);
	    errorScroll.setVisible(true);  
        instructionArea = new JTextArea(0, 0);
        instructionArea.setFont(new Font("Arial", Font.PLAIN, fontsize));
        instructionArea.setEditable (false);
	    JPanel instructionPanel = new JPanel();
        instructionPanel.add(instructionArea, BorderLayout.CENTER);
	    JScrollPane instructionScroll = new JScrollPane(instructionPanel, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    instructionScroll.setBounds(x, y, w, Math.max(digitEnd, buttonEnd) - 2 * space);
	    frame.add(instructionScroll);
	    instructionPanel.setVisible(true);  
	    instructionPanel.setBackground(Color.WHITE);
	    instructionScroll.setVisible(true);  
        x += w + 2 * space;

        frame.add(fillButton);
        frame.add(clearGridButton);
        frame.add(removeButton);
        frame.add(restoreButton);
        frame.add(fileSaveButton);
        frame.add(fileReadButton);
        frame.add(showStepButton);
        frame.add(solveButton);
	    frame.setSize(x, Math.max(digitEnd, buttonEnd) + (int) (40 * heightScaling));  
	    frame.setLayout(null);  
    }
}
