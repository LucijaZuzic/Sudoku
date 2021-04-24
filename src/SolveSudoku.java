import java.awt.BorderLayout;
import java.awt.Color;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import javax.swing.plaf.metal.MetalButtonUI;

public class SolveSudoku extends Sudoku {

	int numHints = 0;
	JButton digitButtons[];
	int[] backup;
	int[][] options;
	int mode = 1;
	int[] result;
	int numErrors = 0;
	int penalty = 0;
	boolean errorWarn = true;
	long startTime;
	long elapsedTime;
	JLabel timeLabel;
	JLabel helpLabel;

	boolean setAssumed = false;
	public SolveSudoku(int x, int y, int xl, int yl, int[] br, int[] bn, int mint, int maxt) {
		super(x, y, xl, yl);
		JFrame newf = new JFrame();
		int dialogResult = JOptionPane.showConfirmDialog (newf, "Želite li da se prikazuju greške?","Prikaži greške",0);
		if(dialogResult == JOptionPane.YES_OPTION){
			errorWarn = true;
		} else {
			errorWarn = false;
		}
		dialogResult = JOptionPane.showConfirmDialog (newf, "Želite li da se automatski postave bilješke?","Postavi bilješke",0);
		if(dialogResult == JOptionPane.YES_OPTION){
			setAssumed = true;
		} else {
			setAssumed = false;
		}
		dialogResult = JOptionPane.showConfirmDialog (newf, "Želite li da se ukljuèi pisanje bilješki?","Ukljuèi bilješke",0);
		if(dialogResult == JOptionPane.YES_OPTION){
			mode = 1;
		} else {
			mode = 0;
		}
		border = br;
		boxNumber = bn;
	    int retval = 1;
	    while(retval == 1) {
		    for (int row = 0; row < rows; row++){ 
		    	for (int col = 0; col < cols; col++) {
		    		temporary[row * cols + col] = 0;
			    }
		    }
		    retval = randomPuzzle();
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
		options = new int[x * y][x];
		result = new int[x * y];
		checkBoxes();
	    int solvable = 0;
	    int numReturns = 0;
	    mintargetDifficulty = mint;
	    maxtargetDifficulty = maxt;
	    long startGen = System.currentTimeMillis();
	    while (solvable == 0 || difficultyScore > maxtargetDifficulty || difficultyScore < mintargetDifficulty) {
		    removeSymetricPair();
		    solvable = isOnlyOneSolution();
		    //System.out.println(String.valueOf(difficultyScore));
	    	if (solvable == 0) {
	    		numReturns++;
	    		restoreLastRemoved();
			    solvable = isOnlyOneSolution();
			    //System.out.println("backtrack " + String.valueOf(difficultyScore));
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
	    		break;
	    	}
	    }
		backup = new int[x * y];
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
	    		for (int k = 0; k < rows; k++) {
	    			options[numCell][k] = 0;
	    		}
	    	}	
	    }
	    if (setAssumed) {
	    	assume();
	    }
	    frame.setVisible(true);
	    frame.requestFocus();
	}
	
	public SolveSudoku(int x, int y, int xl, int yl, int[] br, int[] bn, int[] ui) {
		super(x, y, xl, yl);
		JFrame newf = new JFrame();
		int dialogResult = JOptionPane.showConfirmDialog (newf, "Želite li da se prikazuju greške?","Prikaži greške",0);
		if(dialogResult == JOptionPane.YES_OPTION){
			errorWarn = true;
		} else {
			errorWarn = false;
		}
		dialogResult = JOptionPane.showConfirmDialog (newf, "Želite li da se automatski postave bilješke?","Postavi bilješke",0);
		if(dialogResult == JOptionPane.YES_OPTION){
			setAssumed = true;
		} else {
			setAssumed = false;
		}
		dialogResult = JOptionPane.showConfirmDialog (newf, "Želite li da se ukljuèi pisanje bilješki?","Ukljuèi bilješke",0);
		if(dialogResult == JOptionPane.YES_OPTION){
			mode = 1;
		} else {
			mode = 0;
		}
		border = br;
		boxNumber = bn;
	    draw();
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
	    		userInput[numCell] = ui[numCell];
	    		solution[numCell] = 0;
	    		field[numCell].setText(String.valueOf(userInput[numCell]));
	    	}
	    }
		options = new int[x * y][x];
		result = new int[x * y];
		checkBoxes();
		selectedDigit = 1;
		isOnlyOneSolution();
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
	    		solution[numCell] = temporary[numCell];
	    	}
	    }
	    checkIfCorrect();
		backup = new int[x * y];
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
	    		for (int k = 0; k < rows; k++) {
	    			options[numCell][k] = 0;
	    		}
	    	}	
	    }
	    if (setAssumed) {
	    	assume();
	    }
	    frame.setVisible(true);
	    frame.requestFocus();
	}

	boolean incorrect[] = new boolean[rows * cols];
	@Override
	public boolean checkIfCorrect() {
		String errortext = "";
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
		    	field[row * cols + col].setForeground(Color.BLACK);
		    	temporary[numCell] = userInput[numCell];
		    	incorrect[numCell] = false;
	    	}
	    }
		boolean correct = true;
		for (int val = 1; val <= rows; val++) {
			int[] usedRows = new int[rows];
			int[] usedCols = new int[cols];
			int[] usedBoxes = new int[rows];
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
			    		if (usedRows[row] > 1) {
			    			errortext += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " veæ postoji u retku " + (row + 1) + "\n";
			    			correct = false;
			    			status = true;
			    			incorrect[row * cols + col] = true;
			    		}
			    		if (usedCols[col] > 1) {
			    			errortext += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " veæ postoji u stupcu " + (col + 1) + "\n";
			    			correct = false;
			    			status = true;
			    			incorrect[row * cols + col] = true;
			    		}
			    		if (usedBoxes[boxNumber[row * cols + col]] > 1) {
			    			errortext += val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " veæ postoji u kutiji " + (boxNumber[row * cols + col] + 1) + "\n";
			    			correct = false;
			    			status = true;
			    			incorrect[row * cols + col] = true;
			    		}
			    		if (status) {
			    			for (int k = 0; k < rows; k++) {
				    			int numCell = k * cols + col;
				    			if (temporary[numCell] == val && backup[numCell] == 0) {
				    				incorrect[numCell] = true;
				    			}
				    		}
				    		for (int k = 0; k < cols; k++) {
				    			int numCell = row * cols + k;
				    			if (temporary[numCell] == val && backup[numCell] == 0) {
				    				incorrect[numCell] = true;
				    			}
				    		}
						    for (int x = (row / ylim) * (cols / xlim); x < (row / ylim + 1) * (cols / xlim); x++){
						    	for (int y = (col / xlim) * (cols / xlim); y < (col / xlim + 1) * (cols / xlim); y++) {
					    			int numCell = x * cols + y;
						    		if (temporary[numCell] == val && backup[numCell] == 0) {
					    				incorrect[numCell] = true;
						    		}
						    	}
						    }
			    		}
		    		}
			    }
		    }
		}
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
		if (errorWarn) {
			errorArea.setText(errortext);
		} else {
			errorArea.setText("Greške se ne prikazuju.");
		}
	    numErrors = localnumErrors;
		if (errorWarn) {
			penaltyLabel.setText("Kazneni bodovi: " + String.valueOf(penalty));
		} else {
			penaltyLabel.setText("Kazneni bodovi: *");
		}
		return correct;
	}
	
	Set<Integer> hints = new HashSet<Integer>();

	public void hint() {
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
    	backup[numCell] = 1;
    	userInput[numCell] = result[numCell];
		field[numCell].setEnabled(false);
		field[numCell].setBackground(Color.BLUE);
    	field[numCell].setText(String.valueOf(userInput[numCell]));
    	hints.add(numCell);
    	numHints++;
    	//System.out.println(numHints);
    	helpLabel.setText("Iskorištena pomoæ: " + String.valueOf(numHints));
		if (setAssumed == false) {
    		checkIfCorrect();
			return;
		}
		assume();
		checkIfCorrect();
	}
	JButton modeButton;

	public void hint(int numCell) {
    	if (userInput[numCell] != 0 && incorrect[numCell] == false) {
    		return;
    	}
    	backup[numCell] = 1;
    	userInput[numCell] = result[numCell];
		field[numCell].setEnabled(false);
		field[numCell].setBackground(Color.BLUE);
    	field[numCell].setText(String.valueOf(userInput[numCell]));
    	hints.add(numCell);
    	numHints++;
    	helpLabel.setText("Iskorištena pomoæ: " + String.valueOf(numHints));
		modeButton.setText("Izaði iz pomoæi");
		if (setAssumed == false) {
    		checkIfCorrect();
			return;
		}
		assume();
		checkIfCorrect();
	}
	int errorNum = 0;
	int emptyNum = 0;
	int correctNum = 0;

	public boolean showError() {
		boolean hasErrors = false;
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
	    	        	field[numCell].setText(String.valueOf(result[numCell]));
	    	        	field[numCell].setForeground(Color.MAGENTA);
	    	        	emptyNum++;
	    			} else {
	    	        	field[numCell].setForeground(Color.RED);
	    	        	errorNum++;
	    			}
	    	        hasErrors = true;
	    		} else {
    	        	field[numCell].setForeground(Color.GREEN);
    	        	correctNum++;
	    		}
	    	}	
	    }
	    instructionArea.setText(solvingInstructions);
	    timerStopped = true;

		for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
    			if (hints.contains(row * cols + col)) {
	    			field[row * cols + col].setBackground(Color.BLUE);
	    		} else {
	    	        if (border[row * cols + col] == 3) {
	    	    		field[row * cols + col].setBackground(Color.LIGHT_GRAY);
	    	    	}
	    	        if (border[row * cols + col] == 2) {
	    	    		field[row * cols + col].setBackground(Color.DARK_GRAY);
	    	    	}
	    	    	if (border[row * cols + col] == 1) {
	    	    		field[row * cols + col].setBackground(Color.BLACK);
	    	    	}
	    	        if (border[row * cols + col] == 0) {
	    	    		field[row * cols + col].setBackground(Color.GRAY);
	    	    	}
	    	        if (border[row * cols + col] == -1) {
	    	    		field[row * cols + col].setBackground(Color.RED);
	    	    	}
	    		}  				    			
	    		field[row * cols + col].setFont(field[row * cols + col].getFont().deriveFont(~Font.BOLD | ~Font.ITALIC));
	    		
	    	}
	    }
	    InformationBox.infoBox("Greške: " + String.valueOf(errorNum) + "\nPrazno: " + String.valueOf(emptyNum) + "\nIspravno: " + String.valueOf(correctNum), "Rezultat");
		return hasErrors;
	}

	public void changeTime () {
		elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        String padded = String.format("%02d:%02d:%02d", elapsedTime / 3600, (elapsedTime % 3600) / 60, (elapsedTime % 3600) % 60);
		timeLabel.setText("Proteklo vrijeme: " + padded);
	}
	boolean timerStopped = false;

	KeyListener keyListener  =
	new KeyListener(){
		public void keyPressed(KeyEvent e) {
    		if (timerStopped) {
    			return;
    		}
			int key = e.getKeyCode();
			if (key - 48 >= 1 && key - 48 <= 9) {
				selectedDigit = key - 48;
				resetHighlight();
				highlightDigit();
			}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};
	
	public void assume() {
		for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		int numCell = row * cols + col;
	    		if (userInput[numCell] != 0) {
	    			continue;
	    		}
	    		Set<Integer> bannedValues = new HashSet<Integer>();
        		for (int sameRow = 0; sameRow < sameRow; row++){
			    	for (int sameCol = 0; sameCol < cols; col++) {
			    		int cellThatClears = row * cols + col;
			    		if (userInput[cellThatClears] == 0) {
			    			continue;
			    		}
			    		if (boxNumber[cellThatClears] == boxNumber[numCell] || row == sameRow || col == sameCol) {
			    			bannedValues.add(userInput[cellThatClears]);
			    		}
			    	}
			    }
	    		String text = "<html><font color = yellow>";
	    		int numberOptions = 0;
		    	for (int k = 1; k <= cols; k++) {
		    		if (!bannedValues.contains(k)) {
		    			numberOptions++;
		    			text += String.valueOf(k);
		    			options[numCell][k - 1] = 1;
		    			if (numberOptions % 3 == 0) {
		    				text += "<br />";
		    			} else {
		    				text += " ";
		    			}
		    		} else {
		    			options[numCell][k - 1] = 0;
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

	public void highlightCell(int numCell) {
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (col == numCell % cols || row == numCell / cols) {
	    			field[row * cols + col].setBackground(new Color(119, 136, 153));
	    		} else {
	    			if (hints.contains(row * cols + col)) {
		    			field[row * cols + col].setBackground(Color.BLUE);
		    		} else {
		    	        if (border[row * cols + col] == 3) {
		    	    		field[row * cols + col].setBackground(Color.LIGHT_GRAY);
		    	    	}
		    	        if (border[row * cols + col] == 2) {
		    	    		field[row * cols + col].setBackground(Color.DARK_GRAY);
		    	    	}
		    	    	if (border[row * cols + col] == 1) {
		    	    		field[row * cols + col].setBackground(Color.BLACK);
		    	    	}
		    	        if (border[row * cols + col] == 0) {
		    	    		field[row * cols + col].setBackground(Color.GRAY);
		    	    	}
		    	        if (border[row * cols + col] == -1) {
		    	    		field[row * cols + col].setBackground(Color.RED);
		    	    	}
		    		}
	    		}
	    	}
	    }
	}

	public void highlightDigit() {
		for (int col = 1; col < cols + 1; col++) {
			digitButtons[col].setBackground(Color.WHITE);
		}
		digitButtons[selectedDigit].setBackground(Color.CYAN);
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if ((userInput[row * cols + col] == selectedDigit || options[row * cols + col][selectedDigit - 1] == 1) && selectedDigit != 0) {
	    			if (userInput[row * cols + col] == selectedDigit) {
	    				field[row * cols + col].setFont(field[row * cols + col].getFont().deriveFont(Font.BOLD | Font.ITALIC));
	    			} else {
			    		String text = "<html><font color = yellow>";
			    		int numberOptions = 0;
				    	for (int val = 0; val < cols; val++) {
				    		if (options[row * cols + col][val] == 1) {
				    			numberOptions++;
				    			if (val == selectedDigit - 1) {
				    				text += "<row><b>";
				    			}
				    			text += String.valueOf(val + 1);
				    			if (val == selectedDigit - 1) {
				    				text += "</row></b>";
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
	
	
	@Override
	public void draw () 
    {
		frame = new JFrame("Riješi sudoku");  
	    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
	    frame.addKeyListener(keyListener);
	    int space = 15;
	    int x = space;
		int y = space;
		int w = 60;
		int h = 60;
		int fontsize = 12;

	    for (int row = 0; row < rows; row++){ 
	    	x = space;
	    	for (int col = 0; col < cols; col++) {
	    		int numCell = row * cols + col;
	    	    field[numCell] = new JButton("");  
			    field[numCell].setUI(new MetalButtonUI() {
    			    protected Color getDisabledTextColor() {
    			        return Color.CYAN;
    			    }
    			});
			    field[numCell].setMargin(new Insets(1,1,1,1));
			    field[numCell].setFont(new Font("Arial", Font.PLAIN, fontsize));
			    field[numCell].setBounds(x, y, w, h);
			    field[numCell].addActionListener(new ActionListener(){  
			        public void actionPerformed(ActionEvent e) {  
			        	try {
			        		if (timerStopped) {
			        			return;
			        		}
			        		if (mode == 0)  {
				        		for (int k = 0; k < cols; k++) {
					        		options[numCell][k] = 0;
				        		}
				        		options[numCell][selectedDigit - 1] = 1;
				        		userInput[numCell] = selectedDigit;
				        		field[numCell].setText(String.valueOf(selectedDigit));
				        		if (setAssumed == false) {
					        		checkIfCorrect();
				        			return;
				        		}
				        		assume();
				        		checkIfCorrect();
				        		highlightCell(numCell);
				        		highlightDigit();
			        		} 
			        		if (mode == 1) {
				        		if (options[numCell][selectedDigit - 1] == 1) {
					        		options[numCell][selectedDigit - 1] = 0;
				        		} else {
					        		options[numCell][selectedDigit - 1] = 1;
				        		}
				        		userInput[numCell] = 0;
					    		String text = "<html><font color = yellow>";
					    		int numberOptions = 0;
						    	for (int val = 0; val < cols; val++) {
						    		if (options[numCell][val] == 1) {
						    			numberOptions++;
						    			if (val == selectedDigit - 1) {
						    				text += "<row><b>";
						    			}
						    			text += String.valueOf(val + 1);
						    			if (val == selectedDigit - 1) {
						    				text += "</row></b>";
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
				        		field[numCell].setText(text);
				        		highlightCell(numCell);
				        		highlightDigit();
			        		}
			        		if (mode == 2) {
			        			hint(numCell);
			        		}
			        		checkIfCorrect();
						} catch (Exception e1) {
		
						}
			        }  
			    });
			    field[numCell].addKeyListener(
				    	new KeyListener(){
				    		public void keyPressed(KeyEvent e) {
				        		if (timerStopped) {
				        			return;
				        		}
				    			int key = e.getKeyCode();
				    			if (key - 48 >= 1 && key - 48 <= 9) {
				    				selectedDigit = key - 48;
				    				resetHighlight();
				    				highlightDigit();
				    			}
				    			if (key == 8) {
				    				for (int row = 0; row < cols; row++) {
						        		options[numCell][row] = 0;
				    				}
					        		userInput[numCell] = 0;
				    				field[numCell].setText("");
				    			}
				    		}
							public void keyReleased(KeyEvent e) {}
							public void keyTyped(KeyEvent e) {}
						}
			    	);
			    frame.add(field[numCell]);
		    	x += w;
		    }
		    y += h;
	    }
	    x = space;
	    y += space;
		digitButtons = new JButton[cols + 1];
		for (int row = 1; row < cols + 1; row++) {
	        digitButtons[row] = new JButton(String.valueOf(row));  
	        digitButtons[row].setMargin(new Insets(1,1,1,1));
			if (row != selectedDigit) {
				digitButtons[row].setBackground(Color.WHITE);
			} else {
				digitButtons[row].setBackground(Color.CYAN);
			}
	        digitButtons[row].setBounds(x, y, w, h);
	        digitButtons[row].setFont(new Font("Arial", Font.PLAIN, fontsize));
	        int digit = row;
	        digitButtons[row].addActionListener(new ActionListener(){  
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
		    });
	        digitButtons[row].addKeyListener(keyListener);
	        frame.add(digitButtons[row]);
	        x += w;
		}
		int digitEnd = y + h + space;
		h = 2 * space;
		w = 135;
		x += space;
		y = space;
        JButton solvedButton = new JButton("Ispravnost rješenja");  
        solvedButton.setMargin(new Insets(1,1,1,1));
        solvedButton.setBounds(x, y, w, h);
        solvedButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        solvedButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
	        			return;
	        		}
					resetHighlight();
	        	    errorWarn = true;
	        	    checkIfCorrect();
	        		showError();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
        solvedButton.addKeyListener(keyListener);
		y += h + space;
        JButton showStepButton = new JButton("Prikaži korake");  
        showStepButton.setMargin(new Insets(1,1,1,1));
        showStepButton.setBounds(x, y, w, h);
        showStepButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        showStepButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		showSteps = true;
	        	    timerStopped = true;
	        	    errorWarn = true;
					resetHighlight();
	        	    for (int row = 0; row < rows; row++){ 
	        	    	for (int col = 0; col < cols; col++) {
	        	    		userInput[row * cols + col] = backup[row * cols + col];
	        	    	}	
	        	    }
	        		isOnlyOneSolution();
	        		checkIfCorrect();
	        	    instructionArea.setText(solvingInstructions);
	        		showSteps = false;
				} catch (Exception e1) {
	
				}
	        }  
	    });
        showStepButton.addKeyListener(keyListener);
		y += h + space;
        JButton randomHintButton = new JButton("Nasumièna pomoæ");  
        randomHintButton.setMargin(new Insets(1,1,1,1));
        randomHintButton.setBounds(x, y, w, h);
        randomHintButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        randomHintButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
	        			return;
	        		}
	        		hint();
	        		/*mode = 1;
        			modeButton.setText("Bilješke UKLJUÈENE");*/
				} catch (Exception e1) {
	
				}
	        }  
	    });
        randomHintButton.addKeyListener(keyListener);
		y += h + space;
        JButton hintButton = new JButton("Odabrana pomoæ");  
        hintButton.setMargin(new Insets(1,1,1,1));
        hintButton.setBounds(x, y, w, h);
        hintButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        hintButton.addActionListener(new ActionListener(){  
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
        hintButton.addKeyListener(keyListener);
		y += h + space;
        modeButton = new JButton();  
		if (mode == 0) {
			modeButton.setText("Bilješke ISKLJUÈENE");
		}
		if (mode == 1) {
			modeButton.setText("Bilješke UKLJUÈENE");
		}
        modeButton.setMargin(new Insets(1,1,1,1));
        modeButton.setBounds(x, y, w, h);
        modeButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        modeButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
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
        modeButton.addKeyListener(keyListener);

		y += h + space;


        JButton pencilButton = new JButton("Postavi bilješke");  
		if (setAssumed) {
			pencilButton.setText("Postavljene bilješke");
		} else {
			pencilButton.setText("Ruène bilješke");
		}
        pencilButton.setMargin(new Insets(1,1,1,1));
        pencilButton.setBounds(x, y, w, h);
        pencilButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        pencilButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
	        			return;
	        		}
	        		if (setAssumed) {
	        			setAssumed = false;
	        			pencilButton.setText("Ruène bilješke");
	        		} else {
	        			setAssumed = true;
	        			pencilButton.setText("Postavljene bilješke");
	        			assume();
	        		}
	        		checkIfCorrect();
				} catch (Exception e1) {
	
				}
	        }  
	    });
        pencilButton.addKeyListener(keyListener);
		y += h + space;
        
        JButton errorWarnButton = new JButton(""); 
		if (errorWarn) {
    		errorWarnButton.setText("Upozori na greške");
		} else {
    		errorWarnButton.setText("Ne upozori na greške");
		}
        errorWarnButton.setMargin(new Insets(1,1,1,1));
        errorWarnButton.setBounds(x, y, w, h);
        errorWarnButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        errorWarnButton.addActionListener(new ActionListener(){  
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
        errorWarnButton.addKeyListener(keyListener);
		y += h + space;
		h = h / 2;
		w = 200;
        difficulty.setBounds(x, y, w, h);
        frame.add(difficulty);
		y += h + space;
        
        penaltyLabel.setBounds(x, y, w, h);
        frame.add(penaltyLabel);
		y += h + space;
        
        startTime = System.currentTimeMillis();
        elapsedTime = 0L;
        ScoreTask scoreTask = new ScoreTask();
        scoreTask.setSudoku(this);
        new Timer().scheduleAtFixedRate(scoreTask, 0, 1000);

	    timeLabel = new JLabel("Proteklo vrijeme: 00:00:00");
	    timeLabel.setBounds(x, y, w, h);
	    frame.add(timeLabel);
		y += h + space;
	    
	    helpLabel = new JLabel("Iskorištena pomoæ: 0");
	    helpLabel.setBounds(x, y, w, h);
	    frame.add(helpLabel);
	    int buttonEnd = y + h + space;
        x += w + space;
        y = space;
        w = 250;
        errorArea = new JTextArea(0, 0);
        errorArea.setEditable (false);
	    JPanel errorPanel = new JPanel();
        errorPanel.add(errorArea, BorderLayout.CENTER);
	    JScrollPane errorScroll = new JScrollPane(errorPanel, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    errorScroll.setBounds(x, y, w, Math.max(digitEnd, buttonEnd) - 2 * space);
	    frame.add(errorScroll);
        x += w + space;
        w = 500;
	    errorPanel.setVisible(true);  
	    errorPanel.setBackground(Color.WHITE);
	    errorScroll.setVisible(true);  
        instructionArea = new JTextArea(0, 0);
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
        frame.add(solvedButton);
        frame.add(modeButton);
        frame.add(randomHintButton);
        frame.add(pencilButton);
        frame.add(hintButton);
        frame.add(showStepButton);
        frame.add(errorWarnButton);
	    frame.setSize(x, Math.max(digitEnd, buttonEnd) + 40);  
	    frame.setLayout(null);  
    }
	

}
