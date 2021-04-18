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
	JButton digibs[];
	int[] backup;
	int[][] options;
	int mode = 1;
	int[] result;
	int numerrors = 0;
	int penalty = 0;
	boolean errorwarn = true;
	long startTime;
	long elapsedTime;
	JLabel timeLabel;
	JLabel helpLabel;

	boolean setassumed = false;
	public SolveSudoku(int x, int y, int xl, int yl, int[] br, int[] bn, int mint, int maxt) {
		super(x, y, xl, yl);
		JFrame newf = new JFrame();
		int dialogResult = JOptionPane.showConfirmDialog (newf, "Želite li da se prikazuju greške?","Prikaži greške",0);
		if(dialogResult == JOptionPane.YES_OPTION){
			errorwarn = true;
		} else {
			errorwarn = false;
		}
		dialogResult = JOptionPane.showConfirmDialog (newf, "Želite li da se automatski postave bilješke?","Postavi bilješke",0);
		if(dialogResult == JOptionPane.YES_OPTION){
			setassumed = true;
		} else {
			setassumed = false;
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
		    for (int i = 0; i < rows; i++){ 
		    	for (int j = 0; j < cols; j++) {
		    		temporary[i * cols + j] = 0;
			    }
		    }
		    retval = randomPuzzle();
	    }
	    draw();
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
	    		userInput[num] = temporary[num];
	    		solution[num] = temporary[num];
	    		field[num].setText(String.valueOf(userInput[num]));
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
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
		    	result[num] = solution[num];
		    	backup[num] = userInput[num];
	    		if (userInput[num] == 0) {
		    		userInput[num] = 0;
		    		solution[num] = 0;
		    		temporary[num] = 0;
		    		field[num].setForeground(Color.WHITE);
    	        	field[num].setText("");
	    		} else {
	    			field[num].setEnabled(false);
	    		}
	    		for (int k = 0; k < rows; k++) {
	    			options[num][k] = 0;
	    		}
	    	}	
	    }
	    if (setassumed) {
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
			errorwarn = true;
		} else {
			errorwarn = false;
		}
		dialogResult = JOptionPane.showConfirmDialog (newf, "Želite li da se automatski postave bilješke?","Postavi bilješke",0);
		if(dialogResult == JOptionPane.YES_OPTION){
			setassumed = true;
		} else {
			setassumed = false;
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
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
	    		userInput[num] = ui[num];
	    		solution[num] = 0;
	    		field[num].setText(String.valueOf(userInput[num]));
	    	}
	    }
		options = new int[x * y][x];
		result = new int[x * y];
		checkBoxes();
		selectedDigit = 1;
		isOnlyOneSolution();
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
	    		solution[num] = temporary[num];
	    	}
	    }
	    checkIfCorrect();
		backup = new int[x * y];
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
		    	result[num] = solution[num];
		    	backup[num] = userInput[num];
	    		if (userInput[num] == 0) {
		    		userInput[num] = 0;
		    		solution[num] = 0;
		    		temporary[num] = 0;
    	        	field[num].setForeground(Color.WHITE);
    	        	field[num].setText("");
	    		} else {
	    			field[num].setEnabled(false);
	    		}
	    		for (int k = 0; k < rows; k++) {
	    			options[num][k] = 0;
	    		}
	    	}	
	    }
	    if (setassumed) {
	    	assume();
	    }
	    frame.setVisible(true);
	    frame.requestFocus();
	}

	boolean incorrect[] = new boolean[rows * cols];
	@Override
	public boolean checkIfCorrect() {
		String errortext = "";
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
		    	field[i * cols + j].setForeground(Color.BLACK);
		    	temporary[num] = userInput[num];
		    	incorrect[num] = false;
	    	}
	    }
		boolean correct = true;
		for (int val = 1; val <= rows; val++) {
			int[] usedRows = new int[rows];
			int[] usedCols = new int[cols];
			int[] usedBoxes = new int[rows];
		    for (int i = 0; i < rows; i++){
		    	for (int j = 0; j < cols; j++) {
		    		usedRows[i] = 0;
		    		usedCols[j] = 0;
		    		usedBoxes[i] = 0;
			    }
		    }
		    for (int i = 0; i < rows; i++){
		    	for (int j = 0; j < cols; j++) {
		    		boolean status = false;
		    		if (temporary[i * cols + j] == val) {
			    		usedRows[i]++;
			    		usedCols[j]++;
			    		usedBoxes[boxNumber[i * cols + j]]++;
			    		if (usedRows[i] > 1) {
			    			errortext += val + ": " + "(" + (i + 1) + ", " + (j + 1) + ") Broj " + val + " veæ postoji u retku " + (i + 1) + "\n";
			    			correct = false;
			    			status = true;
			    			incorrect[i * cols + j] = true;
			    		}
			    		if (usedCols[j] > 1) {
			    			errortext += val + ": " + "(" + (i + 1) + ", " + (j + 1) + ") Broj " + val + " veæ postoji u stupcu " + (j + 1) + "\n";
			    			correct = false;
			    			status = true;
			    			incorrect[i * cols + j] = true;
			    		}
			    		if (usedBoxes[boxNumber[i * cols + j]] > 1) {
			    			errortext += val + ": " + "(" + (i + 1) + ", " + (j + 1) + ") Broj " + val + " veæ postoji u kutiji " + (boxNumber[i * cols + j] + 1) + "\n";
			    			correct = false;
			    			status = true;
			    			incorrect[i * cols + j] = true;
			    		}
			    		if (status) {
			    			for (int k = 0; k < rows; k++) {
				    			int num = k * cols + j;
				    			if (temporary[num] == val && backup[num] == 0) {
				    				incorrect[num] = true;
				    			}
				    		}
				    		for (int k = 0; k < cols; k++) {
				    			int num = i * cols + k;
				    			if (temporary[num] == val && backup[num] == 0) {
				    				incorrect[num] = true;
				    			}
				    		}
						    for (int x = (i / ylim) * (cols / xlim); x < (i / ylim + 1) * (cols / xlim); x++){
						    	for (int y = (j / xlim) * (cols / xlim); y < (j / xlim + 1) * (cols / xlim); y++) {
					    			int num = x * cols + y;
						    		if (temporary[num] == val && backup[num] == 0) {
					    				incorrect[num] = true;
						    		}
						    	}
						    }
			    		}
		    		}
			    }
		    }
		}
		int localnumerrors = 0;
	    for (int i = 0; i < rows; i++){
	    	for (int j = 0; j < cols; j++) {
    			int num = i * cols + j;
	    		if (incorrect[num] && backup[num] == 0) {
	    			localnumerrors++;
	    			if (errorwarn) {
	    				field[num].setForeground(Color.ORANGE);
	    			} else {
	    				field[num].setForeground(Color.WHITE);
	    			}
	    		} else {
    				field[num].setForeground(Color.WHITE);
	    		}
		    }
	    }
	    if (localnumerrors > numerrors) {
	    	//penalty += localnumerrors - numerrors;
	    	penalty++;
	    }
		if (errorwarn) {
			errorArea.setText(errortext);
		} else {
			errorArea.setText("Greške se ne prikazuju.");
		}
	    numerrors = localnumerrors;
		if (errorwarn) {
			penaltyLabel.setText("Kazneni bodovi: " + String.valueOf(penalty));
		} else {
			penaltyLabel.setText("Kazneni bodovi: *");
		}
		return correct;
	}
	
	Set<Integer> hints = new HashSet<Integer>();

	public void hint() {
		boolean noneEmpty = true;
	    for (int i = 0; i < rows; i++){
	    	for (int j = 0; j < cols; j++) {
	        	int num = i * cols + j;
	    		if (userInput[num] == 0 || incorrect[num] == true) {
	    			noneEmpty = false;
	    		}
		    }
	    }
	    if (noneEmpty) {
	    	return;
	    }
    	int randomCol = ThreadLocalRandom.current().nextInt(0, cols);
    	int randomRow = ThreadLocalRandom.current().nextInt(0, cols);
    	int num = randomRow * cols + randomCol;
    	while (userInput[num] != 0 && incorrect[num] == false) {
        	randomCol = ThreadLocalRandom.current().nextInt(0, cols);
        	randomRow = ThreadLocalRandom.current().nextInt(0, cols);
        	num = randomRow * cols + randomCol;
    	}
    	backup[num] = 1;
    	userInput[num] = result[num];
		field[num].setEnabled(false);
		field[num].setBackground(Color.BLUE);
    	field[num].setText(String.valueOf(userInput[num]));
    	hints.add(num);
    	numHints++;
    	//System.out.println(numHints);
    	helpLabel.setText("Iskorištena pomoæ: " + String.valueOf(numHints));
		if (setassumed == false) {
    		checkIfCorrect();
			return;
		}
		assume();
		checkIfCorrect();
	}
	JButton modeb;

	public void hint(int num) {
    	if (userInput[num] != 0 && incorrect[num] == false) {
    		return;
    	}
    	backup[num] = 1;
    	userInput[num] = result[num];
		field[num].setEnabled(false);
		field[num].setBackground(Color.BLUE);
    	field[num].setText(String.valueOf(userInput[num]));
    	hints.add(num);
    	numHints++;
    	helpLabel.setText("Iskorištena pomoæ: " + String.valueOf(numHints));
		modeb.setText("Izaði iz pomoæi");
		if (setassumed == false) {
    		checkIfCorrect();
			return;
		}
		assume();
		checkIfCorrect();
	}
	int errornum = 0;
	int emptynum = 0;
	int correctnum = 0;

	public boolean showerror() {
		boolean haserrors = false;
		errornum = 0;
		emptynum = 0;
		correctnum = 0;
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
	    		if (backup[num] != 0) {
	    			continue;
	    		}
	    		if (userInput[num] != result[num]) {
	    			if (userInput[num] == 0) {
	    	        	field[num].setText(String.valueOf(result[num]));
	    	        	field[num].setForeground(Color.MAGENTA);
	    	        	emptynum++;
	    			} else {
	    	        	field[num].setForeground(Color.RED);
	    	        	errornum++;
	    			}
	    	        haserrors = true;
	    		} else {
    	        	field[num].setForeground(Color.GREEN);
    	        	correctnum++;
	    		}
	    	}	
	    }
	    instructionArea.setText(solvingInstructions);
	    timerStopped = true;

		for (int i2 = 0; i2 < rows; i2++){
	    	for (int j2 = 0; j2 < cols; j2++) {
    			if (hints.contains(i2 * cols + j2)) {
	    			field[i2 * cols + j2].setBackground(Color.BLUE);
	    		} else {
	    	        if (border[i2 * cols + j2] == 3) {
	    	    		field[i2 * cols + j2].setBackground(Color.LIGHT_GRAY);
	    	    	}
	    	        if (border[i2 * cols + j2] == 2) {
	    	    		field[i2 * cols + j2].setBackground(Color.DARK_GRAY);
	    	    	}
	    	    	if (border[i2 * cols + j2] == 1) {
	    	    		field[i2 * cols + j2].setBackground(Color.BLACK);
	    	    	}
	    	        if (border[i2 * cols + j2] == 0) {
	    	    		field[i2 * cols + j2].setBackground(Color.GRAY);
	    	    	}
	    	        if (border[i2 * cols + j2] == -1) {
	    	    		field[i2 * cols + j2].setBackground(Color.RED);
	    	    	}
	    		}  				    			
	    		field[i2 * cols + j2].setFont(field[i2 * cols + j2].getFont().deriveFont(~Font.BOLD | ~Font.ITALIC));
	    		
	    	}
	    }
	    InformationBox.infoBox("Greške: " + String.valueOf(errornum) + "\nPrazno: " + String.valueOf(emptynum) + "\nIspravno: " + String.valueOf(correctnum), "Rezultat");
		return haserrors;
	}

	public void changeTime () {
		elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        String padded = String.format("%02d:%02d:%02d", elapsedTime / 3600, (elapsedTime % 3600) / 60, (elapsedTime % 3600) % 60);
		timeLabel.setText("Proteklo vrijeme: " + padded);
	}
	boolean timerStopped = false;

	KeyListener k  =
	new KeyListener(){
		public void keyPressed(KeyEvent e) {
    		if (timerStopped) {
    			return;
    		}
			int key = e.getKeyCode();
			if (key - 48 >= 1 && key - 48 <= 9) {
				selectedDigit = key - 48;
				resethighlight();
				highlightdigit();
			}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};
	
	public void assume() {
		for (int i2 = 0; i2 < rows; i2++){
	    	for (int j2 = 0; j2 < cols; j2++) {
	    		int f1 = i2 * cols + j2;
	    		if (userInput[f1] != 0) {
	    			continue;
	    		}
	    		Set<Integer> pos = new HashSet<Integer>();
        		for (int i = 0; i < rows; i++){
			    	for (int j = 0; j < cols; j++) {
			    		int f2 = i * cols + j;
			    		if (userInput[f2] == 0) {
			    			continue;
			    		}
			    		if (boxNumber[f2] == boxNumber[f1] || i2 == i || j2 == j) {
			    			pos.add(userInput[f2]);
			    		}
			    	}
			    }
	    		String t = "<html><font color = yellow>";
	    		int z = 0;
		    	for (int k = 1; k <= cols; k++) {
		    		if (!pos.contains(k)) {
		    			z++;
		    			t += String.valueOf(k);
		    			options[f1][k - 1] = 1;
		    			if (z % 3 == 0) {
		    				t += "<br />";
		    			} else {
		    				t += " ";
		    			}
		    		} else {
		    			options[f1][k - 1] = 0;
		    		}
			    }
		    	if (z != 0) {
		    		t = t.substring(0, t.length() - 1) + "</font></html>";
		    	} else {
		    		t = "";
		    	}
        		field[f1].setText(t);
	    	}
	    }
	}

	public void highlightcell(int num) {
	    for (int i2 = 0; i2 < rows; i2++){
	    	for (int j2 = 0; j2 < cols; j2++) {
	    		if (j2 == num % cols || i2 == num / cols) {
	    			field[i2 * cols + j2].setBackground(new Color(119, 136, 153));
	    		} else {
	    			if (hints.contains(i2 * cols + j2)) {
		    			field[i2 * cols + j2].setBackground(Color.BLUE);
		    		} else {
		    	        if (border[i2 * cols + j2] == 3) {
		    	    		field[i2 * cols + j2].setBackground(Color.LIGHT_GRAY);
		    	    	}
		    	        if (border[i2 * cols + j2] == 2) {
		    	    		field[i2 * cols + j2].setBackground(Color.DARK_GRAY);
		    	    	}
		    	    	if (border[i2 * cols + j2] == 1) {
		    	    		field[i2 * cols + j2].setBackground(Color.BLACK);
		    	    	}
		    	        if (border[i2 * cols + j2] == 0) {
		    	    		field[i2 * cols + j2].setBackground(Color.GRAY);
		    	    	}
		    	        if (border[i2 * cols + j2] == -1) {
		    	    		field[i2 * cols + j2].setBackground(Color.RED);
		    	    	}
		    		}
	    		}
	    	}
	    }
	}

	public void highlightdigit() {
		for (int j = 1; j < cols + 1; j++) {
			digibs[j].setBackground(Color.WHITE);
		}
		digibs[selectedDigit].setBackground(Color.CYAN);
	    for (int i2 = 0; i2 < rows; i2++){
	    	for (int j2 = 0; j2 < cols; j2++) {
	    		if ((userInput[i2 * cols + j2] == selectedDigit || options[i2 * cols + j2][selectedDigit - 1] == 1) && selectedDigit != 0) {
	    			if (userInput[i2 * cols + j2] == selectedDigit) {
	    				field[i2 * cols + j2].setFont(field[i2 * cols + j2].getFont().deriveFont(Font.BOLD | Font.ITALIC));
	    			} else {
			    		String t = "<html><font color = yellow>";
			    		int z = 0;
				    	for (int k = 0; k < cols; k++) {
				    		if (options[i2 * cols + j2][k] == 1) {
				    			z++;
				    			if (k == selectedDigit - 1) {
				    				t += "<i><b>";
				    			}
				    			t += String.valueOf(k + 1);
				    			if (k == selectedDigit - 1) {
				    				t += "</i></b>";
				    			}
				    			if (z % 3 == 0) {
				    				t += "<br />";
				    			} else {
				    				t += " ";
				    			}
				    		}
					    }
				    	if (z != 0) {
				    		t = t.substring(0, t.length() - 1) + "</font></html>";
				    	} else {
				    		t = "";
				    	}
		        		field[i2 * cols + j2].setText(t);
	    			}
	    		} else {    				    			
	    			field[i2 * cols + j2].setFont(field[i2 * cols + j2].getFont().deriveFont(~Font.BOLD | ~Font.ITALIC));
	    		}
	    	}
	    }
	}
	
	
	@Override
	public void draw () 
    {
		frame = new JFrame("Riješi sudoku");  
	    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
	    frame.addKeyListener(k);
	    int x = 15;
		int y = 15;
		int w = 60;
		int h = 60;
		int fontsize = 12;

	    for (int i = 0; i < rows; i++){ 
	    	x = 15;
	    	for (int j = 0; j < cols; j++) {
	    		int num = i * cols + j;
	    	    field[num] = new JButton("");  
			    field[num].setUI(new MetalButtonUI() {
    			    protected Color getDisabledTextColor() {
    			        return Color.CYAN;
    			    }
    			});
			    field[num].setMargin(new Insets(1,1,1,1));
			    field[num].setFont(new Font("Arial", Font.PLAIN, fontsize));
			    field[num].setBounds(x, y, w, h);
			    field[num].addActionListener(new ActionListener(){  
			        public void actionPerformed(ActionEvent e) {  
			        	try {
			        		if (timerStopped) {
			        			return;
			        		}
			        		if (mode == 0)  {
				        		for (int k = 0; k < cols; k++) {
					        		options[num][k] = 0;
				        		}
				        		options[num][selectedDigit - 1] = 1;
				        		userInput[num] = selectedDigit;
				        		field[num].setText(String.valueOf(selectedDigit));
				        		if (setassumed == false) {
					        		checkIfCorrect();
				        			return;
				        		}
				        		assume();
				        		checkIfCorrect();
				        		highlightcell(num);
				        		highlightdigit();
			        		} 
			        		if (mode == 1) {
				        		if (options[num][selectedDigit - 1] == 1) {
					        		options[num][selectedDigit - 1] = 0;
				        		} else {
					        		options[num][selectedDigit - 1] = 1;
				        		}
				        		userInput[num] = 0;
					    		String t = "<html><font color = yellow>";
					    		int z = 0;
						    	for (int k = 0; k < cols; k++) {
						    		if (options[num][k] == 1) {
						    			z++;
						    			if (k == selectedDigit - 1) {
						    				t += "<i><b>";
						    			}
						    			t += String.valueOf(k + 1);
						    			if (k == selectedDigit - 1) {
						    				t += "</i></b>";
						    			}
						    			if (z % 3 == 0) {
						    				t += "<br />";
						    			} else {
						    				t += " ";
						    			}
						    		}
							    }
						    	if (z != 0) {
						    		t = t.substring(0, t.length() - 1) + "</font></html>";
						    	} else {
						    		t = "";
						    	}
				        		field[num].setText(t);
				        		highlightcell(num);
				        		highlightdigit();
			        		}
			        		if (mode == 2) {
			        			hint(num);
			        		}
			        		checkIfCorrect();
						} catch (Exception e1) {
		
						}
			        }  
			    });
			    field[num].addKeyListener(
				    	new KeyListener(){
				    		public void keyPressed(KeyEvent e) {
				        		if (timerStopped) {
				        			return;
				        		}
				    			int key = e.getKeyCode();
				    			if (key - 48 >= 1 && key - 48 <= 9) {
				    				selectedDigit = key - 48;
				    				resethighlight();
				    				highlightdigit();
				    			}
				    			if (key == 8) {
				    				for (int i = 0; i < cols; i++) {
						        		options[num][i] = 0;
				    				}
					        		userInput[num] = 0;
				    				field[num].setText("");
				    			}
				    		}
							public void keyReleased(KeyEvent e) {}
							public void keyTyped(KeyEvent e) {}
						}
			    	);
			    frame.add(field[num]);
		    	x += w;
		    }
		    y += h;
	    }
	    x = 15;
		digibs = new JButton[cols + 1];
		for (int i = 1; i < cols + 1; i++) {
	        digibs[i] = new JButton(String.valueOf(i));  
	        digibs[i].setMargin(new Insets(1,1,1,1));
			if (i != selectedDigit) {
				digibs[i].setBackground(Color.WHITE);
			} else {
				digibs[i].setBackground(Color.CYAN);
			}
	        digibs[i].setBounds(x, rows * h + 15 * 2, w, h);
	        digibs[i].setFont(new Font("Arial", Font.PLAIN, fontsize));
	        int digit = i;
	        digibs[i].addActionListener(new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		if (timerStopped) {
		        			return;
		        		}
		        		selectedDigit = digit;
						resethighlight();
		        		highlightdigit();
					} catch (Exception e1) {
		
		
					}
		        }  
		    });
	        digibs[i].addKeyListener(k);
	        frame.add(digibs[i]);
	        x += w;
		}
		h = 30;
        JButton solvedb = new JButton("Ispravnost rješenja");  
        solvedb.setMargin(new Insets(1,1,1,1));
        solvedb.setBounds(cols * w + 15 * 2, 15, 9 * w / 4, h);
        solvedb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        solvedb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
	        			return;
	        		}
					resethighlight();
	        	    errorwarn = true;
	        	    checkIfCorrect();
	        		showerror();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
        solvedb.addKeyListener(k);
        modeb = new JButton();  
		if (mode == 0) {
			modeb.setText("Bilješke ISKLJUÈENE");
		}
		if (mode == 1) {
			modeb.setText("Bilješke UKLJUÈENE");
		}
        modeb.setMargin(new Insets(1,1,1,1));
        modeb.setBounds(cols * w + 15 * 2, 15 + 15 * 4 + h * 4, 9 * w / 4, h);
        modeb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        modeb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
	        			return;
	        		}
	        		if (mode == 1) {
	        			modeb.setText("Bilješke ISKLJUÈENE");
	        			mode = 0;
	        		} else {
			        	if (mode == 0) {
		        			modeb.setText("Bilješke UKLJUÈENE");
		        			mode = 1;
			        	} else {
			        		if (mode == 2) {
			        			modeb.setText("Bilješke UKLJUÈENE");
			        			mode = 1;
			        		}
			        	}
	        		}
				} catch (Exception e1) {
	
				}
	        }  
	    });
        modeb.addKeyListener(k);
        JButton randomhintb = new JButton("Nasumièna pomoæ");  
        randomhintb.setMargin(new Insets(1,1,1,1));
        randomhintb.setBounds(cols * w + 15 * 2, 15 + 15 * 2 + h * 2, 9 * w / 4, h);
        randomhintb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        randomhintb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
	        			return;
	        		}
	        		hint();
	        		/*mode = 1;
        			modeb.setText("Bilješke UKLJUÈENE");*/
				} catch (Exception e1) {
	
				}
	        }  
	    });
        randomhintb.addKeyListener(k);
        JButton hintb = new JButton("Odabrana pomoæ");  
        hintb.setMargin(new Insets(1,1,1,1));
        hintb.setBounds(cols * w + 15 * 2, 15 + 15 * 3 + h * 3, 9 * w / 4, h);
        hintb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        hintb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
	        			return;
	        		}
	        		mode = 2;
        			modeb.setText("Izaði iz pomoæi");
				} catch (Exception e1) {
	
				}
	        }  
	    });
        hintb.addKeyListener(k);

        JButton showstepb = new JButton("Prikaži korake");  
        showstepb.setMargin(new Insets(1,1,1,1));
        showstepb.setBounds(cols * w + 15 * 2, 15 + 15 + h, 9 * w / 4, h);
        showstepb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        showstepb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		showSteps = true;
	        	    timerStopped = true;
	        	    errorwarn = true;
					resethighlight();
	        	    for (int i = 0; i < rows; i++){ 
	        	    	for (int j = 0; j < cols; j++) {
	        	    		userInput[i * cols + j] = backup[i * cols + j];
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
        showstepb.addKeyListener(k);


        JButton pencilb = new JButton("Postavi bilješke");  
		if (setassumed) {
			pencilb.setText("Postavljene bilješke");
		} else {
			pencilb.setText("Ruène bilješke");
		}
        pencilb.setMargin(new Insets(1,1,1,1));
        pencilb.setBounds(cols * w + 15 * 2, 15 + 15 * 5 + h * 5, 9 * w / 4, h);
        pencilb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        pencilb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
	        			return;
	        		}
	        		if (setassumed) {
	        			setassumed = false;
	        			pencilb.setText("Ruène bilješke");
	        		} else {
	        			setassumed = true;
	        			pencilb.setText("Postavljene bilješke");
	        			assume();
	        		}
	        		checkIfCorrect();
				} catch (Exception e1) {
	
				}
	        }  
	    });
        pencilb.addKeyListener(k);
        
        JButton errorwarnb = new JButton(""); 
		if (errorwarn) {
    		errorwarnb.setText("Upozori na greške");
		} else {
    		errorwarnb.setText("Ne upozori na greške");
		}
        errorwarnb.setMargin(new Insets(1,1,1,1));
        errorwarnb.setBounds(cols * w + 15 * 2, 15 + 15 * 6 + h * 6, 9 * w / 4, h);
        errorwarnb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        errorwarnb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (timerStopped) {
	        			return;
	        		}
	        		if (errorwarn) {
		        		errorwarnb.setText("Ne upozori na greške");
	        			errorwarn = false;
	        		} else {
		        		errorwarnb.setText("Upozori na greške");
	        			errorwarn = true;
	        		}
	        		checkIfCorrect();
				} catch (Exception e1) {
	
				}
	        }  
	    });
        errorwarnb.addKeyListener(k);
        difficulty.setBounds(cols * w + 15 * 2, 15 * 7 + 15 + h * 7, 200, h / 2);
        frame.add(difficulty);
        
        difficulty.setBounds(cols * w + 15 * 2, 15 * 7 + 15 + h * 7, 200, h / 2);
        frame.add(difficulty);

        
        penaltyLabel.setBounds(cols * w + 15 * 2, 15 * 15 / 2 + 15 + h * 15 / 2, 9 * w / 4, h / 2);
        frame.add(penaltyLabel);
        
        startTime = System.currentTimeMillis();
        elapsedTime = 0L;
        ScoreTask t = new ScoreTask();
        t.setSudoku(this);
        new Timer().scheduleAtFixedRate(t, 0, 1000);

	    timeLabel = new JLabel("Proteklo vrijeme: 00:00:00");
	    timeLabel.setBounds(cols * w + 15 * 2, 15 * 8 + 15 + h * 8, 200, h / 2);
	    frame.add(timeLabel);
	    
	    helpLabel = new JLabel("Iskorištena pomoæ: 0");
	    helpLabel.setBounds(cols * w + 15 * 2, 15 * 17 / 2 + 15 + h * 17 / 2, 200, h / 2);
	    frame.add(helpLabel);

        h = 60;
        errorArea = new JTextArea(0, 0);
        errorArea.setEditable (false);
	    JPanel errorpanel = new JPanel();
        errorpanel.add(errorArea, BorderLayout.CENTER);
	    JScrollPane errorscroll = new JScrollPane(errorpanel, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    errorscroll.setBounds(cols * w + 15 * 2 + 200 + 15, 15, 250, Math.max((rows + 1) * h + 15, 9 * h + 10 * 15));
	    frame.add(errorscroll);
	    errorpanel.setVisible(true);  
	    errorpanel.setBackground(Color.WHITE);
	    errorscroll.setVisible(true);  
        instructionArea = new JTextArea(0, 0);
        instructionArea.setEditable (false);
	    JPanel instructionpanel = new JPanel();
        instructionpanel.add(instructionArea, BorderLayout.CENTER);
	    JScrollPane instructionscroll = new JScrollPane(instructionpanel, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    instructionscroll.setBounds(cols * w + 15 * 2 + 200 + 15 + 15 + 250, 15, 500, Math.max((rows + 1) * h + 15, 9 * h + 10 * 15));
	    frame.add(instructionscroll);
	    instructionpanel.setVisible(true);  
	    instructionpanel.setBackground(Color.WHITE);
	    instructionscroll.setVisible(true);  

        frame.add(solvedb);
        frame.add(modeb);
        frame.add(randomhintb);
        frame.add(pencilb);
        frame.add(hintb);
        frame.add(showstepb);
        frame.add(errorwarnb);
	    frame.setSize(cols * w + 15 * 2 + 200 + 15 + 15 + 750 + 30, Math.max((rows + 1) * h + 15 * 3 + 40, 9 * h + 12 * 15 + 40));  
	    frame.setLayout(null);  
    }
	

}
