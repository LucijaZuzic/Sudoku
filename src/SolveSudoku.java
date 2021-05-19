import java.awt.Color;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class SolveSudoku extends Sudoku {

	int[] backup  = new int[rows * cols];
	int[][] options;
	int mode = 0;
	int[] result;
	int numErrors = 0;
	int penalty = 0;
	boolean errorWarn = true;
	long startTime;
	long elapsedTime;
	JLabel timeLabel;
	JLabel helpLabel;
	boolean setAssumed = true;

	// Oznaka za prikaz kaznene vrijednosti
	JLabel penaltyLabel = new JLabel("");
	
	public void resetHighlight() {
		for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
    			if (hints.contains(row * cols + col)) {
	    			setBackground(row, col, "blue");
	    		} else {
	    			setBackground(row, col, returnColour(row * cols + col));
	    		}	
	    		field[row * cols + col].setFont(field[row * cols + col].getFont().deriveFont(~Font.BOLD | ~Font.ITALIC));
	    	}
	    }
	}
	
	public SolveSudoku(int constructRows, int constructCols, int rowLimit, int colLimit, int[] constructBorder, int[] constructBoxNumber, boolean setDiagonalOn, Set<String> setSizeRelationships, int constructMinDifficulty, int constructMaxDifficulty, boolean askUser) {
		super(constructRows, constructCols, rowLimit, colLimit, setDiagonalOn, setSizeRelationships);
		if (askUser) {
			errorWarn = InformationBox.yesNoBox("Želite li da se prikazuju greške?", "Prikaži greške");
			setAssumed = InformationBox.yesNoBox("Želite li da se automatski postave bilješke?", "Postavi bilješke");
			if (setAssumed == false) {
				if(InformationBox.yesNoBox("Želite li da se ukljuèi pisanje bilješki?","Ukljuèi bilješke")){
					mode = 1;
				} else {
					mode = 0;
				}
			} else {
				mode = 0;
			}
		}
		border = constructBorder;
		boxNumber = constructBoxNumber;
	    int retVal = -1;
	    long startGen = System.currentTimeMillis();
	    while(retVal == -1) {
	    	if (System.currentTimeMillis() - startGen >= 10000) {
    		    InformationBox.infoBox("Nije moguæe ispuniti zagonetku prema zadanim kriterijima.", "Pogrešno dizajnirana zagonetka");
    		    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    		    frame.removeAll();
    		    frame.dispose();
    		    frame.setVisible(false);
    		    return;
	    	}
		    for (int row = 0; row < rows; row++){ 
		    	for (int col = 0; col < cols; col++) {
		    		temporary[row * cols + col] = 0;
			    }
		    }
		    useGuessing = true;
		    retVal = isOnlyOneSolution();
		    useGuessing = false;
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
		options = new int[constructRows * constructCols][constructRows];
		result = new int[constructRows * constructCols];
		checkBoxes();
	    int solvable = 0;
	    int numReturns = 0;
	    mintargetDifficulty = constructMinDifficulty;
	    maxtargetDifficulty = constructMaxDifficulty;
	    startGen = System.currentTimeMillis();
	    while (solvable == 0 || difficultyScore > maxtargetDifficulty || difficultyScore < mintargetDifficulty) {
		    removeSymetricPair();
		    solvable = isOnlyOneSolution();
	    	if (solvable == 0) {
	    		numReturns++;
	    		restoreLastRemoved();
			    solvable = isOnlyOneSolution();
	    	} else {
	    		numReturns = 0;
	    	}
	    	if (numReturns >= rows * cols / 2) {
	    		while (restoreLastRemoved()) {
	    			
	    		}
	    		difficultyScore = 0;
	    		numReturns = 0;
	    	}
	    	if (solvable == 1 && System.currentTimeMillis() - startGen >= 10000) {
	    		if (!InformationBox.yesNoBox("Zagonetka je težine " + difficultyScore + ", a zadani raspon je od " + mintargetDifficulty + " do " + maxtargetDifficulty + ".\n Želite li svejedno riješiti zagonetku?", "Upozorenje o težini")) {
	    			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	    		    frame.removeAll();
	    		    frame.dispose();
	    		    frame.setVisible(false);
	    		    return;
	    		}
	    		break;
	    	}
	    }
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
		    	result[numCell] = solution[numCell];
		    	backup[numCell] = userInput[numCell];
	    		if (userInput[numCell] == 0) {
		    		userInput[numCell] = 0;
		    		solution[numCell] = 0;
		    		temporary[numCell] = 0;
		    		field[numCell].setForeground(Color.WHITE);
    	        	field[numCell].setText("");
	    		} else {
	    			field[numCell].setEnabled(false);
	    		}
	    		for (int val = 0; val < rows; val++) {
	    			options[numCell][val] = 0;
	    		}
	    	}	
	    }
	    if (setAssumed) {
	    	assume();
	    }
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
	
	public SolveSudoku(int constructRows, int constructCols, int rowLimit, int colLimit, int[] constructBorder, int[] constructBoxNumber, boolean setDiagonalOn, Set<String> setSizeRelationships, int[] constructUserInput, boolean askUser) {
		super(constructRows, constructCols, rowLimit, colLimit, setDiagonalOn, setSizeRelationships);
		if (askUser) {
			errorWarn = InformationBox.yesNoBox("Želite li da se prikazuju greške?", "Prikaži greške");
			setAssumed = InformationBox.yesNoBox("Želite li da se automatski postave bilješke?", "Postavi bilješke");
			if (setAssumed == false) {
				if(InformationBox.yesNoBox("Želite li da se ukljuèi pisanje bilješki?","Ukljuèi bilješke")){
					mode = 1;
				} else {
					mode = 0;
				}
			} else {
				mode = 0;
			}
		}
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
		options = new int[constructRows * constructCols][constructRows];
		result = new int[constructRows * constructCols];
		checkBoxes();
		isOnlyOneSolution();
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
	    		solution[numCell] = temporary[numCell];
	    	}
	    }
	    checkIfCorrect();
		backup = new int[constructRows * constructCols];
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
		    	result[numCell] = solution[numCell];
		    	backup[numCell] = userInput[numCell];
	    		if (userInput[numCell] == 0) {
		    		userInput[numCell] = 0;
		    		solution[numCell] = 0;
		    		temporary[numCell] = 0;
    	        	field[numCell].setForeground(Color.WHITE);
    	        	field[numCell].setText("");
	    		} else {
	    			field[numCell].setEnabled(false);
	    		}
	    		for (int val = 0; val < rows; val++) {
	    			options[numCell][val] = 0;
	    		}
	    	}	
	    }
	    if (setAssumed) {
	    	assume();
	    }
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

	boolean incorrect[] = new boolean[rows * cols];

	@Override
	public int countIncorrect(boolean[] incorrect, boolean correct) {
		int localnumErrors = 0;
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
    			int numCell = row * cols + col;
	    		if (incorrect[numCell] && backup[numCell] == 0) {
	    			localnumErrors++;
	    			if (errorWarn) {
	    				field[numCell].setForeground(Color.ORANGE);
	    			} else {
	    				field[numCell].setForeground(Color.WHITE);
	    			}
	    		} else {
    				field[numCell].setForeground(Color.WHITE);
	    		}
		    }
	    }
	    if (localnumErrors > numErrors) {
	    	//penalty += localnumErrors - numErrors;
	    	penalty++;
	    }
		if (!errorWarn) {
			errorArea.setText("Greške se ne prikazuju.");
		}
	    numErrors = localnumErrors;
		if (errorWarn) {
			penaltyLabel.setText("Kazneni bodovi: " + String.valueOf(penalty));
		} else {
			penaltyLabel.setText("Kazneni bodovi: *");
		}
		return localnumErrors;
	}
	
	Set<Integer> hints = new HashSet<Integer>();

	public void hint() {
		checkIfCorrect();
		boolean noneEmpty = true;
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	        	int numCell = row * cols + col;
	    		if (userInput[numCell] == 0 || incorrect[numCell] == true) {
	    			noneEmpty = false;
	    		}
		    }
	    }
	    if (noneEmpty) {
	    	return;
	    }
    	int randomCol = ThreadLocalRandom.current().nextInt(0, cols);
    	int randomRow = ThreadLocalRandom.current().nextInt(0, cols);
    	int numCell = randomRow * cols + randomCol;
    	while (userInput[numCell] != 0 && incorrect[numCell] == false) {
        	randomCol = ThreadLocalRandom.current().nextInt(0, cols);
        	randomRow = ThreadLocalRandom.current().nextInt(0, cols);
        	numCell = randomRow * cols + randomCol;
    	}
		for (int val = 0; val < cols; val++) {
    		options[numCell][val] = 0;
		}
		options[numCell][result[numCell] - 1] = 1;
    	backup[numCell] = result[numCell];
    	userInput[numCell] = result[numCell];
		field[numCell].setEnabled(false);
		field[numCell].setBackground(Color.BLUE);
		if (userInput[numCell] < 10) {
			field[numCell].setText(String.valueOf(userInput[numCell]));
		} else {
			char c = 'A';
			c += userInput[numCell] - 10;
			field[numCell].setText("" + c);
		}
	    field[numCell].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
		zoomArea.setText(field[numCell].getText().replace("yellow", "black"));
    	hints.add(numCell);
    	oldHints.add(numCell);
    	numUseDigit[userInput[numCell]]++;
    	checkIfDigitMaxUsed(userInput[numCell]);
    	helpLabel.setText("Iskorištena pomoæ: " + String.valueOf(hints.size()));
		resetHighlight();
		highlightDigit();
		if (setAssumed) {
			assume();
		}
	}
	JButton modeButton;
	JButton pencilButton;
	JButton errorWarnButton;
	public void hint(int numCell) {
		checkIfCorrect();
    	if (userInput[numCell] != 0 && incorrect[numCell] == false) {
    		return;
    	}
		for (int val = 0; val < cols; val++) {
    		options[numCell][val] = 0;
		}
		options[numCell][result[numCell] - 1] = 1;
    	backup[numCell] = result[numCell];
    	userInput[numCell] = result[numCell];
		field[numCell].setEnabled(false);
		field[numCell].setBackground(Color.BLUE);
		if (userInput[numCell] < 10) {
			field[numCell].setText(String.valueOf(userInput[numCell]));
		} else {
			char c = 'A';
			c += userInput[numCell] - 10;
			field[numCell].setText("" + c);
		}
	    field[numCell].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
    	hints.add(numCell);
    	oldHints.add(numCell);
    	numUseDigit[userInput[numCell]]++;
    	checkIfDigitMaxUsed(userInput[numCell]);
    	helpLabel.setText("Iskorištena pomoæ: " + String.valueOf(hints.size()));
		resetHighlight();
		highlightDigit();
		if (setAssumed) {
			assume();
		}
	}
	int errorNum = 0;
	int emptyNum = 0;
	int correctNum = 0;
	Set<Integer> oldHints = new HashSet<Integer>();
	Set<Integer> oldError = new HashSet<Integer>();
	Set<Integer> oldEmpty = new HashSet<Integer>();
	Set<Integer> oldCorrect = new HashSet<Integer>();
	public boolean countError() {
		boolean hasErrors = false;
		if (!timerStopped) {
			errorNum = 0;
			emptyNum = 0;
			correctNum = 0;
		    for (int row = 0; row < rows; row++){ 
		    	for (int col = 0; col < cols; col++) {
			    	int numCell = row * cols + col;
		    		if (backup[numCell] != 0) {
		    			continue;
		    		}
		    		if (userInput[numCell] != result[numCell]) {
		    			if (userInput[numCell] == 0) {
		    	        	emptyNum++;
		    	        	oldEmpty.add(numCell);
		    			} else {
		    	        	errorNum++;
		    	        	oldError.add(numCell);
		    			}
		    	        hasErrors = true;
		    		} else {
	    	        	correctNum++;
	    	        	oldCorrect.add(numCell);
		    		}
		    	}	
		    }
		} else {
			if (errorNum + emptyNum > 0) {
				hasErrors = true;
			}
		}
	    timerStopped = true;
	    InformationBox.infoBox("Pomoæ: " + String.valueOf(oldHints.size()) + "\nGreške: " + String.valueOf(errorNum) + "\nPrazno: " + String.valueOf(emptyNum) + "\nIspravno: " + String.valueOf(correctNum), "Rezultat");
		return hasErrors;
	}
	public void showError() {
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
	    		if (backup[numCell] != 0) {
	    			continue;
	    		}
	    	    field[numCell].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
	    		if (oldHints.contains(numCell)) {
    	        	field[numCell].setForeground(Color.CYAN);
	    			setBackground(row, col, "blue");
	    			continue;
	    		}
	    		if (userInput[numCell] != result[numCell]) {
    	    		if (userInput[numCell] < 10) {
    	    			field[numCell].setText(String.valueOf(result[numCell]));
    	    		} else {
    	    			char c = 'A';
    	    			c += result[numCell] - 10;
    	    			field[numCell].setText("" + c);
    	    		}
	    			if (userInput[numCell] == 0) {
	    	        	field[numCell].setForeground(Color.YELLOW);
	    			} else {
	    	        	field[numCell].setForeground(Color.RED);
	    			}
	    		} else {
    	        	field[numCell].setForeground(Color.GREEN);
	    		}
	    		if (oldCorrect.contains(numCell)) {
    	        	field[numCell].setForeground(Color.GREEN);
	    		}
	    		if (oldError.contains(numCell)) {
    	        	field[numCell].setForeground(Color.RED);
	    		}
	    		if (oldEmpty.contains(numCell)) {
    	        	field[numCell].setForeground(Color.YELLOW);
	    		}
	    	}	
	    }
	    instructionArea.setText(solvingInstructions);
	}

	public void changeTime () {
		elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        String padded = String.format("%02d:%02d:%02d", elapsedTime / 3600, (elapsedTime % 3600) / 60, (elapsedTime % 3600) % 60);
		timeLabel.setText("Proteklo vrijeme: " + padded);
	}
	boolean timerStopped = false;

    @Override
	public void assume() {
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
		    	temporary[numCell] = userInput[numCell];
	    	}
	    }
	    initPencilmarks();
		fixPencilmarks();
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
	    		if (userInput[numCell] != 0) {
	    			continue;
	    		}
	    		setAllOptions(possibilities, row, col, false);
	    	}
	    }
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
	    		if (userInput[numCell] != 0) {
	    			continue;
	    		}
		    	for (int val = 1; val <= cols; val++) {
		    		if (possibilities[numCell][val - 1] == 1) {
		    			options[numCell][val - 1] = 1;
		    		} else {
		    			options[numCell][val - 1] = 0;
		    		}
			    }
	    	}
	    }
	}

	public void highlightCell(int numCell) {
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (col == numCell % cols || row == numCell / cols) {
	    			setBackground(row, col, "one_more_gray");
	    		} else {
	    			if (hints.contains(row * cols + col)) {
		    			setBackground(row, col, "blue");
		    		} else {
		    			setBackground(row, col, returnColour(row * cols + col));
		    		}
	    		}
	    	}
	    }
	}

	public void highlightDigit() {
		for (int col = 0; col < cols + 1; col++) {
			digitButtons[col].setBackground(Color.WHITE);
		}
		digitButtons[selectedDigit].setBackground(Color.CYAN);	
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {	    							    		
    			if (userInput[row * cols + col] != 0) {
		    		field[row * cols + col].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
    				if (userInput[row * cols + col] == selectedDigit) {
	    				field[row * cols + col].setFont(field[row * cols + col].getFont().deriveFont(Font.BOLD | Font.ITALIC));
    				} else {
    					field[row * cols + col].setFont(field[row * cols + col].getFont().deriveFont(~Font.BOLD | ~Font.ITALIC));
    				}
    			} else {
    				if (selectedDigit == 0) {
    					continue;
    				}
		    		if (options[row * cols + col][selectedDigit - 1] == 1) {
	    				setAllOptions(options, row, col, true);
		    		}
    			}
	    	}
	    }
	}
	

    @Override
	public ActionListener makeActionListener(int numCell) {
		return new ActionListener(){  
			public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
	        			return;
	        		}
	        		if (zoomMode) {
		        		zoomArea.setText(field[numCell].getText().replace("yellow", "black"));
						resetHighlight();
						highlightDigit();
						highlightCell(numCell);
		        		return;
	        		}
	        		if (mode == 0)  {
        				for (int val = 0; val < cols; val++) {
        					options[numCell][val] = 0;
        				}
	        			if (selectedDigit != 0) {
			        		options[numCell][selectedDigit - 1] = 1;
	        			}
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
		        		} else {
		        			field[numCell].setText("");
		        		}
		        		if (setAssumed) {
		        			assume();
		        		}
		        		highlightCell(numCell);
		        		highlightDigit();
			        	checkIfCorrect();
	        		} 
	        		if (mode == 1) {
	    			    field[numCell].setFont(new Font("Arial", Font.PLAIN, fontsize));
	        			if (selectedDigit == 0) {
	        				for (int val = 0; val < cols; val++) {
	        					options[numCell][val] = 0;
	        				}
	        			} else {
			        		if (options[numCell][selectedDigit - 1] == 1) {
				        		options[numCell][selectedDigit - 1] = 0;
			        		} else {
				        		options[numCell][selectedDigit - 1] = 1;
			        		}
	        			}
		        		if (userInput[numCell] != 0) {
		        			numUseDigit[userInput[numCell]]--;
		        			numUseDigit[0]++;
		        			checkIfDigitMaxUsed(userInput[numCell]);
	        			}
		        		userInput[numCell] = 0;
		        		field[numCell].setFont(new Font("Arial", Font.PLAIN, guessFontsize));
	    				setAllOptions(options, numCell / cols, numCell % cols, false);
		        		if (setAssumed) {
		        			assume();
		        		}
		        		highlightCell(numCell);
		        		highlightDigit();
	        		}
	        		if (mode == 2) {
	        			hint(numCell);
	        		}
	        		zoomArea.setText(field[numCell].getText().replace("yellow", "black"));
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
	        		if (timerStopped) {
	        			return;
	        		}
	        		selectedDigit = digit;
					resetHighlight();
					highlightDigit();
				} catch (Exception e1) {
	
	
				}
	        }  
	    };
    }
	
	@Override
	public void draw () 
    {
		frame = new JFrame("Riješi sudoku");  
	    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
	    frame.addKeyListener(keyListener);
	    int returnX = makeButtons();
	    int digitEnd = makeDigitButtons();
		h = 2 * space;
	    w = (int) (200 * widthScaling);
	    y = space;
	    x = returnX + space;
		
		makeAButton("Prikaži korake", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		showSteps = true;
		        	    errorWarn = true;
						resetHighlight();
		        	    showError();
						countError();
		        	    if (!InformationBox.stepBox("Jeste li spremni za prikaz koraka?", "Korak po korak")) {
		        	    	showSteps = false;
		        	    }
		        	    for (int row = 0; row < rows; row++){ 
		        	    	for (int col = 0; col < cols; col++) {
		        	    		int numCell = row * cols + col;
		        	    		userInput[numCell] = backup[numCell];
		        	    		if (hints.contains(numCell)) {
		        	    			hints.remove(numCell);
		        	    			userInput[numCell] = 0;
		        	    			backup[numCell] = 0;
		        	    			field[numCell].setEnabled(true);
		        	    		}
		        	    	}	
		        	    }
		        	    checkBoxes();
		        		isOnlyOneSolution();
		        	    instructionArea.setText(solvingInstructions);
		        		showError();
		        		showSteps = false;
		        	} catch (Exception e1) {
		
					}
		        }  
		    });
        
		y += h + space;
        
        makeAButton("Nasumièna pomoæ", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		if (timerStopped) {
		        			return;
		        		}
		        		hint();
		        	} catch (Exception e1) {
		
					}
		        }  
		    });
		y += h + space;

        makeAButton("Odabrana pomoæ", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		if (timerStopped) {
		        			return;
		        		}
		        		mode = 2;
	        			modeButton.setText("Izaði iz pomoæi");
		        	} catch (Exception e1) {
		
					}
		        }  
		    });

		y += h + space;
        modeButton = makeAButton("", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
	        			return;
	        		}
	        		if (setAssumed) {
	        			modeButton.setText("Bilješke ISKLJUÈENE");
	        			mode = 0;
	        			return;
	        		}
	        		if (mode == 1) {
	        			modeButton.setText("Bilješke ISKLJUÈENE");
	        			mode = 0;
	        		} else {
			        	if (mode == 0) {
		        			modeButton.setText("Bilješke UKLJUÈENE");
		        			mode = 1;
			        	} else {
			        		if (mode == 2) {
			        			modeButton.setText("Bilješke UKLJUÈENE");
			        			mode = 1;
			        		}
			        	}
	        		}
	        	} catch (Exception e1) {
	
				}
	        }  
	    }); 
		if (mode == 0) {
			modeButton.setText("Bilješke ISKLJUÈENE");
		}
		if (mode == 1) {
			modeButton.setText("Bilješke UKLJUÈENE");
		}


		y += h + space;


        pencilButton = makeAButton("", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
	        			return;
	        		}
	        		if (setAssumed) {
	        			setAssumed = !setAssumed;
	        			pencilButton.setText("Ruène bilješke");
	        		} else {
	        			setAssumed = !setAssumed;
	        			pencilButton.setText("Postavljene bilješke");
	        			modeButton.setText("Bilješke ISKLJUÈENE");
	        			mode = 0;
	        			assume();
	        		}
	        		checkIfCorrect();
	        	} catch (Exception e1) {
	
				}
	        }  
	    }); 
		if (setAssumed) {
			pencilButton.setText("Postavljene bilješke");
		} else {
			pencilButton.setText("Ruène bilješke");
		}

		y += h + space;
        
        errorWarnButton = makeAButton("", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
	        			return;
	        		}
	        		if (errorWarn) {
		        		errorWarnButton.setText("Ne upozori na greške");
	        			errorWarn = false;
	        		} else {
		        		errorWarnButton.setText("Upozori na greške");
	        			errorWarn = true;
	        		}
	        		checkIfCorrect();
	        	} catch (Exception e1) {
	
				}
	        }  
	    }); 
		if (errorWarn) {
    		errorWarnButton.setText("Upozori na greške");
		} else {
    		errorWarnButton.setText("Ne upozori na greške");
		}

		y += h + space;
		addZoomBox(x, y, w, w);

		w = (int) (250 * widthScaling);
        difficulty.setBounds(x, y, w, h);
        difficulty.setFont(new Font("Arial", Font.PLAIN, fontsize));
        frame.add(difficulty);
		y += h + space;
        
        penaltyLabel.setBounds(x, y, w, h);
        penaltyLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
        frame.add(penaltyLabel);
		y += h + space;
        
        startTime = System.currentTimeMillis();
        elapsedTime = 0L;
        ScoreTask scoreTask = new ScoreTask();
        scoreTask.setSudoku(this);
        new Timer().scheduleAtFixedRate(scoreTask, 0, 1000);

	    timeLabel = new JLabel("Proteklo vrijeme: 00:00:00");
	    timeLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    timeLabel.setBounds(x, y, w, h);
	    frame.add(timeLabel);
		y += h + space;
	    
	    helpLabel = new JLabel("Iskorištena pomoæ: 0");
	    helpLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    helpLabel.setBounds(x, y, w, h);
	    frame.add(helpLabel);
		y += h + space;
		
		
	    int buttonEnd = y;
        
		addErrorScroll(digitEnd, buttonEnd);
		addInstructionScroll(digitEnd, buttonEnd);

	    frame.setSize(x, Math.max(digitEnd, buttonEnd) + (int) (40 * heightScaling));  
	    frame.setLayout(null);  
    }
	
}
