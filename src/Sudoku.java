import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.util.LinkedList;
import java.util.Queue;
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
	Queue<Integer> lastRemovedPos1 = new LinkedList<Integer>();
	Queue<Integer> lastRemoved1 = new LinkedList<Integer>();
	Queue<Integer> lastRemovedPos2 = new LinkedList<Integer>();
	Queue<Integer> lastRemoved2 = new LinkedList<Integer>();
	
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
	    field[numOld].setText(String.valueOf(val));
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
	    		System.out.println(boxNum + " " + x);
	    	    if (x > rows) {
	    	    	System.out.println("Prevelika kutija");
	    	    	retVal = false;
	    	    }
	    	    if (x < rows) {
	    	    	System.out.println("Premalena kutija");
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
				    		difficultyScore += 100 * numIter;
				    		changed++;
				    		unset--;
					    	for (int k = 0; k < cols; k++) {
					    		if (possibilities[i * cols + j][k] == 1) {				    		
					    			solvingInstructions += "Single Candidate " + String.valueOf(k + 1) + " (" + String.valueOf(i + 1) + ", " + String.valueOf(j + 1) + ")\n";
					    			temporary[i * cols + j] = k + 1;
					    		}
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
			    		difficultyScore += 100 * numIter;
			    		changed++;
			    		unset--;
				    	for (int k = 0; k < cols; k++) {
				    		possibilities[i * cols + x][k] = 0;
					    }
				    	possibilities[i * cols + x][val - 1] = 1;
		    			solvingInstructions += "Single Position " + String.valueOf(val) + " (" + String.valueOf(i + 1) + ", " + String.valueOf(x + 1) + ")\n";
			    		temporary[i * cols + x] = val;
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
			difficulty.setText("Ne postoji jedinstveno rješenje");
			return 0;
		} 
		difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
		return 1;
	}
	
	public void removeSymetricPair() {
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
    	lastRemovedPos1.add(num);
    	lastRemoved1.add(userInput[num]);
    	temporary[num] = 0;
		userInput[num] = temporary[num];
	    field[num].setText("0");
    	field[num].setForeground(Color.RED);
    	lastRemovedPos2.add(num2);
    	lastRemoved2.add(userInput[num2]);
    	temporary[num2] = 0;
		userInput[num2] = temporary[num2];
	    field[num2].setText("0");
    	field[num2].setForeground(Color.RED);
	}

	public void restoreLastRemoved() {
		if (lastRemovedPos1.isEmpty()) {
			return;
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
		lastRemovedPos1.remove();
		lastRemoved1.remove();
		lastRemovedPos2.remove();
		lastRemoved2.remove();	
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
