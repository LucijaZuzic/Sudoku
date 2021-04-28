import java.awt.Color;
import java.awt.Font;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;


public abstract class Sudoku extends SudokuGrid {
	JButton digitButtons[];
	JTextArea errorArea;
	JTextArea instructionArea;
	JLabel difficulty = new JLabel("");
	JLabel penaltyLabel = new JLabel("");
	String solvingInstructions;

	int selectedDigit = 0;
	int difficultyScore = 0;
	int numIter = 0;
	
	Stack<Integer> lastRemovedPosOriginal = new Stack<Integer>();
	Stack<Integer> lastRemovedValOriginal = new Stack<Integer>();
	Stack<Integer> lastRemovedPosSymetric = new Stack<Integer>();
	Stack<Integer> lastRemovedValSymetric = new Stack<Integer>();

	int numPossibilities[] = new int[rows * cols];
	int[][] possibilities = new int[rows * cols][rows];
	int unset = 0;
	boolean showSteps = false;
	
	abstract boolean checkIfCorrect();

	
	public int fixPencilmarks() {
		// A�urira mogu�e vrijednosti za �elije nakon upisa nove kona�ne vrijednosti
		int numChanged = 0;
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (temporary[row * cols + col] != 0) { 
	    			// Ako je kona�na vrijednost upisana uklanjamo mogu�nosti
			    	for (int val = 0; val < cols; val++) {
			    		possibilities[row * cols + col][val] = 0;
				    }
			    	// Jedina preostala mogu�nost je kona�na vrijednost
		    		possibilities[row * cols + col][temporary[row * cols + col] - 1] = 1; 
	    		} else {
	    			// Ukloni mogu�nosti za vrijednosti koje ve� postoje u istom stupcu
	    			for (int fullCol = 0; fullCol < cols; fullCol++) { 
			    		if (temporary[row * cols + fullCol] != 0 && possibilities[row * cols + col][temporary[row * cols + fullCol] - 1] == 1) { 
				    		possibilities[row * cols + col][temporary[row * cols + fullCol] - 1] = 0;
		    				numChanged = 1;
			    		}
				    }
	    			// Ukloni mogu�nosti za vrijednosti koje ve� postoje u istom retku
			    	for (int fullRow = 0; fullRow < rows; fullRow++) { 
			    		if (temporary[fullRow * cols + col] != 0 && possibilities[row * cols + col][temporary[fullRow * cols + col] - 1] == 1) { 
			    				possibilities[row * cols + col][temporary[fullRow * cols + col] - 1] = 0;
			    				numChanged = 1;
			    		}
				    }
			    	// Ukloni mogu�nosti za vrijednosti koje ve� postoje u istoj kutiji
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
		// Upisujemo kona�ne vrijednosti u �elije koje imaju samo jednu mogu�nost
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		// Ako je ve� upisana kona�na vrijednost, preska�emo �eliju
	    		if (temporary[row * cols + col] == 0) {
	    			int possibility = 0;
			    	for (int val = 0; val < cols; val++) {
			    		possibility += possibilities[row * cols + col][val];
				    }
			    	// Ako je preostala samo jedna mogu�nost za �eliju, mo�emo ju upisati
			    	if (possibility == 1) {
			    		// Ova metoda rje�avanja ima tro�ak 100
			    		difficultyScore += 100;
			    		unset--;
				    	for (int val = 0; val < cols; val++) {
				    		if (possibilities[row * cols + col][val] == 1) {		
				    			// Dodajemo novi red u tekst uputa
				    			solvingInstructions += "Broj " + String.valueOf(val + 1) + " je jedina mogu�a vrijednost �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
				    			// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
				    			if (showSteps == true) {
		    		    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Broj " + String.valueOf(val + 1) + " je jedina mogu�a vrijednost �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ")", "Rje�ava�");
		    		    		}
			    		    	temporary[row * cols + col] = val + 1;
		    		    		//field[row * cols + col].setForeground(Color.BLACK);
		    		    		field[row * cols + col].setText(String.valueOf(val + 1));
		    		    		// A�uriramo mogu�e vrijednosti ostalih �elija
		    		    		fixPencilmarks();
		    		    		break;
				    		}
					    }
				    	// Ako smo postavili sve �elije, prekidamo rje�avanje
			    		if (unset == 0) {
			    			return 1;
			    		}
			    		// Ako nismo postavili sve �elije, pozivamo sekvencu metoda za rje�avanje, ako ona uspije sve rje�iti, prekidamo rje�avanje
						if (sequence() == 1) {
			    			return 1;
						}

			    	}
	    		}
	    	}
	    }
	    return 0;
	}
	
	// Broj kori�tenja za metodu ogoljenih skupova po veli�ini skupa
	int dj2 = 0;
	int dj3 = 0;
	int dj4 = 0;
	// Maksimalna veli�ina skrivenog ili ogoljenog skupa
	int depthLimit;
	
	// Dodaje se tro�ak za metode skrivenog skupa i linija na ispis uputa za rje�avanje
	// setCells - skup brojeva �elija u skrivenom skupu, matchString - popis vrijednosti koje pokriva skriveni skup, string 0 i 1 koje ozna�avaju je li znamenka uklju�ena
	// containerNum - broj retka, stupca ili kutije u kojem se nalazi skriveni skup, conatinerType - string koji ozna�ava je li skriveni skup u retku, stupcu ili kutiji
	// firstCell - ishodi�na �elija skrivenog skupa kojoj smo tra�ili �elije koje sadr�e neku od istih mogu�ih vrijednosti kao i ona
	public void difficultySettingNaked(Set<Integer> setCells, String matchString, int containerNum, String containerType, int firstCell) {
		String setType = "Ogoljen";
		String lineSolvInstr = "";
		// Dio koda za ogoljeni par
		if (setCells.size() == 1) {
			// Dodavanje vrijednosti skupa u upute za rje�avanje
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
			// Ako smo ve� registrirali ovaj isti skup, ne bodujemo ga dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (dj2 == 0) {
    				// Kada prvi put na�emo ogoljeni par, tro�ak je 750
    				difficultyScore += 750;
    				dj2 = 1;
    			} else {
    				// Kada idu�i put na�emo ogoljeni par, tro�ak je 500
    				difficultyScore += 500;
    			}
    		} else {
    			return;
    		}
		}
		// Dio koda za ogoljenu trojku
		if (setCells.size() == 2) {
			// Dodavanje vrijednosti skupa u upute za rje�avanje
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
			// Ako smo ve� registrirali ovaj isti skup, ne bodujemo ga dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (dj3 == 0) {
    				// Kada prvi put na�emo ogoljenu trojku, tro�ak je 2000
    				difficultyScore += 2000;
    				dj3 = 1;
    			} else {
    				// Kada idu�i put na�emo ogoljenu trojku, tro�ak je 1400
    				difficultyScore += 1400;
    			}
    		} else {
    			return;
    		}
		}
		// Dio koda za ogoljenu �etvorku
		if (setCells.size() == 3) {
			// Dodavanje vrijednosti skupa u upute za rje�avanje
			lineSolvInstr += setType + "a �etvorka u " + containerType + " " + String.valueOf(containerNum + 1) + ", vrijednosti";
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
			// Ako smo ve� registrirali ovaj isti skup, ne bodujemo ga dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (dj4 == 0) {
    				// Kada prvi put na�emo ogoljenu �etvorku, tro�ak je 5000
    				difficultyScore += 5000;
    				dj4 = 1;
    			} else {
    				// Kada idu�i put na�emo ogoljenu �etvorku, tro�ak je 4000
    				difficultyScore += 4000;
    			}
    		} else {
    			return;
    		}
		}
		// Dio koda za ogoljeni skup ve�i od �etiri
		if (setCells.size() > 3) {
			// Dodavanje vrijednosti skupa u upute za rje�avanje
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
			// Ako smo ve� registrirali ovaj isti skup, ne bodujemo ga dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (dj4 == 0) {
    				// Kada prvi put na�emo ogoljeni skup ve�i od 4, tro�ak je 5000 (broji se kao i ogoljena �etvorka)
    				difficultyScore += 5000;
    				dj4 = 1;
    			} else {
    				// Kada idu�i put na�emo ogoljeni skup ve�i od 4, tro�ak je 4000 (broji se kao i ogoljena �etvorka)
    				difficultyScore += 4000;
    			}
    		} else {
    			return;
    		}
		}
		// Dodavanje �elija skupa u upute za rje�avanje (slu�aj skupa u retku)
		lineSolvInstr += " u �elijama";
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
		// Dodavanje �elija skupa u upute za rje�avanje (slu�aj skupa u stupcu)
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
		// Dodavanje �elija skupa u upute za rje�avanje (slu�aj skupa u kutiji)
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
    	// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
    	if (showSteps == true) {
		    instructionArea.setText(solvingInstructions);
		    print();
			InformationBox.infoBox(lineSolvInstr, "Rje�ava�");
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
			    				solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").\n";
					    		if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").", "Rje�ava�");
					    		}
								possibilities[notInSet][val] = 0;
								if (sequence() == 1) {
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
			    				solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").\n";
			    				if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").", "Rje�ava�");
					    		}
			    				possibilities[notInSet][val] = 0;
								if (sequence() == 1) {
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
			    				solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").\n";
			    				if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").", "Rje�ava�");
					    		}
			    				possibilities[notInSet][val] = 0;
								if (sequence() == 1) {
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
					return 1;
				}

		    	Set<Integer> sameColumn = new HashSet<Integer>();
		    	if (nakedSetForCol(row, col, sameColumn) == 1) {
					return 1;
				}

		    	Set<Integer> sameBox = new HashSet<Integer>();
		    	if (nakedSetForBox(row, col, sameBox) == 1) {
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
			lineSolvInstr += setType + "a �etvorka u " + containerType + " " + String.valueOf(containerNum + 1) + ", vrijednosti";
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
		lineSolvInstr += " u �elijama";
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
		    print();
			InformationBox.infoBox(lineSolvInstr, "Rje�ava�");
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
								solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(firstRow + 1) + ", " + String.valueOf(col + 1) + ").\n" ;
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(firstRow + 1) + ", " + String.valueOf(col + 1) + ").", "Rje�ava�");
					    		}
			    				possibilities[firstRow * cols + col][val] = 0;
								if (sequence() == 1) {
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
								solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(firstCol + 1) + ").\n";
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(firstCol + 1) + ").", "Rje�ava�");
					    		}
								possibilities[row * cols + firstCol][val] = 0;
								if (sequence() == 1) {
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
								solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(notInSet + 1) + " iz �elije (" + String.valueOf(numCell / cols + 1) + ", " + String.valueOf(numCell % cols + 1) + ").\n";
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			InformationBox.infoBox("Uklanjam mogu�nost " + String.valueOf(notInSet + 1) + " iz �elije (" + String.valueOf(numCell / cols + 1) + ", " + String.valueOf(numCell % cols + 1) + ").", "Rje�ava�");
					    		}
								possibilities[numCell][notInSet] = 0;
								if (sequence() == 1) {
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
		    		return 1;
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
    	    		return 1;
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
    	    		return 1;
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
						    		    print();
				    	    			InformationBox.infoBox(lineSolvInstr, "Rje�ava�");
						    		}
    				    		}
	    					}
	    					solvingInstructions +=  "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(valueRow[val] + 1) + ", " + String.valueOf(col + 1) + ").\n";
	    					if (showSteps == true) {
				    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			InformationBox.infoBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(valueRow[val] + 1) + ", " + String.valueOf(col + 1) + ").", "Rje�ava�");
				    		}
		    				possibilities[valueRow[val] * cols + col][val] = 0;
				    		if (sequence() == 1) {
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
						    		    print();
				    	    			InformationBox.infoBox(lineSolvInstr, "Rje�ava�");
						    		}
    				    		}
    	    				}
	    					solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(valueCol[val] + 1) + ").\n";
	    					if (showSteps == true) {
				    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			InformationBox.infoBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(valueCol[val] + 1) + ").", "Rje�ava�");
				    		}
		    				possibilities[row * cols + valueCol[val]][val] = 0;
				    		if (sequence() == 1) {
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
			    	if (valueRowString[firstBox][val].compareTo(valueRowString[matchingBox][val]) == 0 && matchingBox != firstBox && ((widthLimit && valueRowCandidates[firstBox][val] == 2 && valueRowCandidates[matchingBox][val] == 2) || (!widthLimit && (valueRowCandidates[firstBox][val] > 2 || valueRowCandidates[matchingBox][val] > 2)))) {
						sameRowBoxes.add(matchingBox);
			    	}
			    	if (valueColString[firstBox][val].compareTo(valueColString[matchingBox][val]) == 0 && matchingBox != firstBox && ((widthLimit && valueColCandidates[firstBox][val] == 2 && valueColCandidates[matchingBox][val] == 2) || (!widthLimit && (valueColCandidates[firstBox][val] > 2 || valueColCandidates[matchingBox][val] > 2)))) {
						sameColBoxes.add(matchingBox);
			    	}
		    	}
		    	if (sameRowBoxes.size() == valueRowVals[firstBox][val] - 1 && sameRowBoxes.size() > 0) {
		    		int numChanges = 0;
					for (int row = 0; row < rows; row++) {
						if (valueRow[firstBox][val][row] == 1) {
							for (int col = 0; col < cols; col++) {
								if (boxNumber[row * cols + col] != firstBox && !sameRowBoxes.contains(boxNumber[row * cols + col]) && possibilities[row * cols + col][val] == 1) {
									if (numChanges == 0) {
										String lineSolvInstr = "";
										if (widthLimit) {
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
							    	   lineSolvInstr += " sadr�i " + String.valueOf(val + 1) + " u kutijama";
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
											if (widthLimit) {
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
								    		    print();
						    	    			InformationBox.infoBox(lineSolvInstr, "Rje�ava�");
								    		}
		    				    		}
									}
			    					solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
									if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    		    		print();
				    	    			InformationBox.infoBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").", "Rje�ava�");
						    		}
									possibilities[row * cols + col][val] = 0;
						    		numChanges++;
				    				if (sequence() == 1) {
				    	    			return 1;
				    				}
								}
							}
						}
					}
		    	}		    	
		    	if (sameColBoxes.size() == valueColVals[firstBox][val] - 1 && sameColBoxes.size() > 0) {
		    		int numChanges = 0;
					for (int col = 0; col < cols; col++) {
						if (valueCol[firstBox][val][col] == 1) {
							for (int row = 0; row < rows; row++) {
								if (boxNumber[row * cols + col] != firstBox && !sameColBoxes.contains(boxNumber[row * cols + col]) && possibilities[row * cols + col][val] == 1) {
									if (numChanges == 0) {
							    		String lineSolvInstr;
										if (widthLimit) {
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
							    		lineSolvInstr += " sadr�i " + String.valueOf(val + 1) + " u kutijama";
							    		sizeOfSet = 0;
							    		for (int box = 0; box < cols; box++) {
							    			if (sameColBoxes.contains(box) || box == firstBox) {
							    				if (sizeOfSet > 0 && sizeOfSet != sameColBoxes.size()) {
							    					lineSolvInstr += ",";
							    				}
							    				if (sizeOfSet == sameColBoxes.size()) {
							    					lineSolvInstr += " i";
							    				}
							    				lineSolvInstr += " " + String.valueOf(box + 1);
							    				sizeOfSet++;
							    			}
							    		}
							    		lineSolvInstr += ".\n";
							    		if (!solvingInstructions.contains(lineSolvInstr)) {
											if (widthLimit) {
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
								    		    print();
						    	    			InformationBox.infoBox(lineSolvInstr, "Rje�ava�");
								    		}
		    				    		}
						    		}
			    					solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
									if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    		    		print();
				    	    			InformationBox.infoBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").", "Rje�ava�");
						    		}
									possibilities[row * cols + col][val] = 0;
						    		numChanges++;
				    				if (sequence() == 1) {
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
		    		difficultyScore += 100;
		    		unset--;
			    	for (int valPossible = 0; valPossible < cols; valPossible++) {
			    		possibilities[row * cols + colToClear][valPossible] = 0;
				    }
			    	possibilities[row * cols + colToClear][val - 1] = 1;
			    	usedRows[row] = 1;
			    	usedCols[colToClear] = 1;
			    	int b = boxNumber[row * cols + colToClear];
			    	usedBoxes[b] = 1;
	    			solvingInstructions += "Za red " + String.valueOf(row + 1) + ", broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(row + 1) + ", " + String.valueOf(colToClear + 1) + ").\n";
		    		if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			InformationBox.infoBox("Za red " + String.valueOf(row + 1) + ", broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(row + 1) + ", " + String.valueOf(colToClear + 1) + ").", "Rje�ava�");
		    		}
			    	temporary[row * cols + colToClear] = val;
    		    	field[row * cols + colToClear].setForeground(Color.BLACK);
		    		field[row * cols + colToClear].setText(String.valueOf(val));
		    		fixPencilmarks();
		    		if (unset == 0) {
		    			return 1;
		    		}
					if (sequence() == 1) {
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
		    		difficultyScore += 100;
		    		unset--;
			    	for (int valPossible = 0; valPossible < cols; valPossible++) {
			    		possibilities[rowToClear * cols + col][valPossible] = 0;
				    }
			    	possibilities[rowToClear * cols + col][val - 1] = 1;
			    	usedRows[rowToClear] = 1;
			    	usedCols[col] = 1;
			    	int boxToClear = boxNumber[rowToClear * cols + col];
			    	usedBoxes[boxToClear] = 1;
	    			solvingInstructions += "Za stupac " + String.valueOf(col + 1) + ", broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(rowToClear + 1) + ", " + String.valueOf(col + 1) + ").\n";
	    			if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			InformationBox.infoBox("Za stupac " + String.valueOf(col + 1) + ", broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(rowToClear + 1) + ", " + String.valueOf(col + 1) + ").", "Rje�ava�");
		    		}
	    			temporary[rowToClear * cols + col] = val;
    		    	field[rowToClear * cols + col].setForeground(Color.BLACK);
		    		field[rowToClear * cols + col].setText(String.valueOf(val));
			    	fixPencilmarks();
		    		if (unset == 0) {
		    			return 1;
		    		}
					if (sequence() == 1) {
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
		    		difficultyScore += 100;
		    		unset--;
			    	for (int valPossible = 0; valPossible < cols; valPossible++) {
			    		possibilities[cellToClear][valPossible] = 0;
				    }
			    	possibilities[cellToClear][val - 1] = 1;
			    	usedRows[cellToClear / cols] = 1;
			    	usedCols[cellToClear % cols] = 1;
			    	usedBoxes[boxNumber[cellToClear]] = 1;
	    			solvingInstructions += "Za kutiju " + String.valueOf(boxNumber[cellToClear] + 1) + ", broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").\n";
	    			if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			InformationBox.infoBox("Za kutiju " + String.valueOf(boxNumber[cellToClear] + 1) + ", broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").", "Rje�ava�");
		    		}
	    			temporary[cellToClear] = val;
    		    	field[cellToClear].setForeground(Color.BLACK);
		    		field[cellToClear].setText(String.valueOf(val));
			    	fixPencilmarks();
		    		if (unset == 0) {
		    			return 1;
		    		}
					if (sequence() == 1) {
		    			return 1;
					}
		    	} 
		    }
		}
		return 0;
	}
	
	int maxDepth = 4;
	int involvesGuesses;

	public int sequence() {
		involvesGuesses = 0;
		if (singlePosition() == 1 || unset == 0) {
			return 1;
		}
		if (singleCandidate() == 1 || unset == 0) {
			return 1;
		}
		if (candidateLines() == 1 || unset == 0) {
			return 1;
		}
		widthLimit = true;
		if (multipleLines() == 1 || unset == 0) {
			return 1;
		}
		widthLimit = false;
		if (multipleLines() == 1 || unset == 0) {
			return 1;
		}
		for (int depth = 2; depth < maxDepth; depth++) {
			depthLimit = depth;
			if (nakedSet() == 1 || unset == 0) {
				return 1;
			}
			if (hiddenSet() == 1 || unset == 0) {
				return 1;
			}
		}
		Stack<Integer> cell = new Stack<Integer>();
		chainLength = 2;
		for (int val = 0; val < cols; val++) {
			if (closedChain(cell, -1, 1, 1, val) == 1 || unset == 0) {
				return 1;
			}
			if (closedChain(cell, -1, 1, 0, val) == 1 || unset == 0) {
				return 1;
			}
		}
		if (forcingChains() == 1 || unset == 0) {
			return 1;
		}
		depthLimit = maxDepth;
		if (nakedSet() == 1 || unset == 0) {
			return 1;
		}
		depthLimit = cols;
		if (nakedSet() == 1 || unset == 0) {
			return 1;
		}
		depthLimit = maxDepth;
		if (hiddenSet() == 1 || unset == 0) {
			return 1;
		}
		depthLimit = cols;
		if (hiddenSet() == 1 || unset == 0) {
			return 1;
		}
		chainLength = 3;
		for (int val = 0; val < cols; val++) {
			if (closedChain(cell, -1, 1, 1, val) == 1 || unset == 0) {
				return 1;
			}
			if (closedChain(cell, -1, 1, 0, val) == 1 || unset == 0) {
				return 1;
			}
		}
		chainLength = 4;
		for (int val = 0; val < cols; val++) {
			if (closedChain(cell, -1, 1, 1, val) == 1 || unset == 0) {
				return 1;
			}
			if (closedChain(cell, -1, 1, 0, val) == 1 || unset == 0) {
				return 1;
			}
		}
		involvesGuesses = 1;
		if (guessing() == 1 || unset == 0) {
			return 1;
		}
		return 0;
	}
	
	public int isOnlyOneSolution() {
		clt = 0;
		dpt = 0;
		mlt = 0;
		dj2 = 0;
		us2 = 0;
		dj3 = 0;
		us3 = 0;
		dj4 = 0;
		us4 = 0;
		xwg = 0;
		fct = 0;
		sf4 = 0;
		solvingInstructions = "";
		numIter = 0;
		difficultyScore = 0;
	    boolean correct = checkIfCorrect();
    	if (!correct) {
			difficulty.setText("U zagonetki ima gre�aka");
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
		if (sequence() == 1 || unset == 0) {
			solvingInstructions += "Sva polja rje�ena.\n";
			if (involvesGuesses == 1) {
				difficulty.setText(String.valueOf(difficultyScore) + " Postoji verzija rje�enja");	
			} else {
				difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rje�enje");
			}
			print();
			return 1;
		}
		difficulty.setText(String.valueOf(unset) + " Ne postoji jedinstveno rje�enje");
		print();
		return 0;
	}
	
	public void fullPrint () {
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		System.out.print(temporary[row * cols + col]);
	    	}
	    	System.out.println("");
	    }
	}
	
	int fct	= 0;
	
	public int forcingChains() {
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
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		int [] forcedValues = new int[rows * cols];
	    		int possibilityNum = 0;
	    		for (int val = 0; val < rows; val++) {
		    		if (possibilities[row * cols + col][val] == 1 && temporary[row * cols + col] == 0) {
				    	temporary[row * cols + col] = val + 1;
			    		for (int clearVal = 0; clearVal < cols; clearVal++) {
			    			possibilities[row * cols + col][clearVal] = 0;
			    		}
		    			possibilities[row * cols + col][val] = 1;
				    	unset--;
						sequence();
					    for (int rowRestore = 0; rowRestore < rows; rowRestore++){
					    	for (int colRestore = 0; colRestore < cols; colRestore++) {
					    		if (possibilityNum == 0) {
					    			forcedValues[rowRestore * cols + colRestore] = temporary[rowRestore * cols + colRestore];
					    		} else {
					    			if (forcedValues[rowRestore * cols + colRestore] != temporary[rowRestore * cols + colRestore]) {
					    				forcedValues[rowRestore * cols + colRestore] = 0;
					    			}
					    		}
					    		temporary[rowRestore * cols + colRestore] = backupTemporary[rowRestore * cols + colRestore];
					    		for (int valRestore = 0; valRestore < cols; valRestore++) {
					    			possibilities[rowRestore * cols + colRestore][valRestore] = backupPossibilities[rowRestore * cols + colRestore][valRestore];
					    		}
		    				}
		    			}
					    difficultyScore = backupDifficultyScore;
					    solvingInstructions = backupSolvingInstructions;
					    unset = backupUnset;
					    possibilityNum++;
		    		}
			    }
			    for (int rowForce = 0; rowForce < rows; rowForce++){
			    	for (int colForce = 0; colForce < cols; colForce++) {
			    		if (forcedValues[rowForce * rows + colForce] != 0 && temporary[rowForce * rows + colForce] == 0) {
		    				String lineSolvInstr = "�elija (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ") forsira vrijednost ";
		    				if (!solvingInstructions.contains(lineSolvInstr)) {
		    					if (fct == 0) {
		    						difficultyScore += 4200;
		    						fct = 1;
		    					} else {
		    						difficultyScore += 2100;
		    					}
		    				}
	    					lineSolvInstr += String.valueOf(forcedValues[rowForce * rows + colForce]) + " u �eliji (" + String.valueOf(rowForce + 1) + ", " + String.valueOf(colForce + 1) + ").\n";
	    					solvingInstructions += lineSolvInstr;
		    				if (showSteps == true) {
	    		    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			InformationBox.infoBox("�elija (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ") forsira vrijednost " + String.valueOf(forcedValues[rowForce * rows + colForce]) + " u �eliji (" + String.valueOf(rowForce + 1) + ", " + String.valueOf(colForce + 1) + ").", "Rje�ava�");
	    		    		}
			    			for (int val = 0; val < cols; val++) {
			    				possibilities[rowForce * rows + colForce][val] = 0;
			    			}
		    				possibilities[rowForce * rows + colForce][forcedValues[rowForce * rows + colForce] - 1] = 1;
		    				temporary[rowForce * cols + colForce] = forcedValues[rowForce * rows + colForce];
		    				unset--;
		    				fixPencilmarks();
		    				if (unset == 0) {
		    					return 1;
		    				} 
	    					if (sequence() == 1) {
	    						return 1;
	    					}
	    		    	}
			    	}
			    }
		    }
		}
	    return 0;
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
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		for (int val = 0; val < rows; val++) {
		    		if (possibilities[row * cols + col][val] == 1 && temporary[row * cols + col] == 0) {
		    			solvingInstructions += "Po�injem poga�ati.\n";
						solvingInstructions += "Poku�avam " + String.valueOf(val + 1) + " u �eliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
						backupSolvingInstructions += "Po�injem poga�ati.\n";
						backupSolvingInstructions += "Poku�avam " + String.valueOf(val + 1) + " u �eliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
				    	temporary[row * cols + col] = val + 1;
			    		for (int clearVal = 0; clearVal < cols; clearVal++) {
			    			possibilities[row * cols + col][clearVal] = 0;
			    		}
		    			possibilities[row * cols + col][val] = 1;
				    	unset--;
						if (sequence() == 1) {
							fullPrint();
							return 1;
						} else {
							fullPrint();
						    for (int rowRestore = 0; rowRestore < rows; rowRestore++){
						    	for (int colRestore = 0; colRestore < cols; colRestore++) {
						    		temporary[rowRestore * cols + colRestore] = backupTemporary[rowRestore * cols + colRestore];
						    		for (int valRestore = 0; valRestore < cols; valRestore++) {
						    			possibilities[rowRestore * cols + colRestore][valRestore] = backupPossibilities[rowRestore * cols + colRestore][valRestore];
						    		}
			    				}
			    			}
						    difficultyScore = backupDifficultyScore;
						    backupSolvingInstructions += "Povla�im " + String.valueOf(val + 1) + " u �eliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
						    solvingInstructions = backupSolvingInstructions;
						    unset = backupUnset;
						    possibilities[row * cols + col][val] = 0;
							return 0;
						} 
		    		}
			    }
		    }
		}
	    if (unset > 0) {
	    	for (int rowRestore = 0; rowRestore < rows; rowRestore++){
		    	for (int colRestore = 0; colRestore < cols; colRestore++) {
		    		temporary[rowRestore * cols + colRestore] = backupTemporary[rowRestore * cols + colRestore];
		    		for (int valRestore = 0; valRestore < cols; valRestore++) {
		    			possibilities[rowRestore * cols + colRestore][valRestore] = backupPossibilities[rowRestore * cols + colRestore][valRestore];
		    		}
				}
			}
		    difficultyScore = backupDifficultyScore;
		    solvingInstructions = backupSolvingInstructions;
	        unset = backupUnset;
	    	return 0;
	    } else {
	    	return 1;
	    }
	}
	
	public void startNextIterationOfClosedChains(Stack<Integer> cells, int beginCell, int direction, int rowOrcols, int beginVal, int numCell) {
		if (cells.contains(numCell) || temporary[numCell] != 0 || possibilities[numCell][beginVal] == 0) {
			return;
		}
		int numPossibilities = 0;
		for (int val = 0; val < cols; val++) {
			if (possibilities[numCell][val] == 1) {
				numPossibilities++;
			}
		}
		if (numPossibilities != 2) {
			return;
		}
		Stack<Integer> StackNextIteration = new Stack<Integer>();
		for (int cell = 0; cell < rows * cols; cell++) {
			if (cells.contains(cell)) {
				StackNextIteration.add(cell);
			}
		}
		StackNextIteration.add(numCell);
		if (cells.size() == 0) {
			beginCell = numCell;
		}
		if (rowOrcols == 1) {
			closedChain(StackNextIteration, beginCell, direction, 0, beginVal);
		} else {
			closedChain(StackNextIteration, beginCell, direction, 1, beginVal);
		}
	}
	int chainLength;
	int xwg = 0;
	int sf4 = 0;
	public int closedChain(Stack<Integer> cells, int beginCell, int direction, int rowOrcols, int beginVal) {
		if (cells.size() == chainLength) {
			direction = 0;
		}
		if (cells.size() == chainLength * 2) {
			if (cells.peek() / cols != beginCell / cols && cells.peek() % cols != beginCell % cols) {
				return 0;
			}
			Set<Integer> usedRows = new HashSet<Integer>();
			Set<Integer> usedCols = new HashSet<Integer>();
			int numRemoved = 0;
			for (int cell = 0; cell < rows * cols; cell++) {
				if (cells.contains(cell)) {
					usedRows.add(cell / cols);
					usedCols.add(cell % cols);
				}
			}
			for (int cell = 0; cell < rows * cols; cell++) {
				if (cells.contains(cell) || (!usedRows.contains(cell / cols) && !usedCols.contains(cell % cols)) || possibilities[cell][beginVal] == 0) {
					continue;
				}
				if (numRemoved == 0) {
					String lineSolvInstr = "";
					if (chainLength == 2) {
						lineSolvInstr += "X-krilo vrijednosti " + String.valueOf(beginVal + 1) + " u �elijama";
					}
					if (chainLength == 3) {
						lineSolvInstr += "Sabljarka vrijednosti " + String.valueOf(beginVal + 1) + " u �elijama";
					}
					if (chainLength == 4) {
						lineSolvInstr += "Meduza vrijednosti " + String.valueOf(beginVal + 1) + " u �elijama";
					}
					int sizeOfSet = 0;
					for (int cellInSet = 0; cellInSet < rows * cols; cellInSet++) {
						if (cells.contains(cellInSet)) {
		    				if (sizeOfSet > 0 && sizeOfSet != cells.size() - 1) {
		    					lineSolvInstr += ",";
		    				}
		    				if (sizeOfSet == cells.size() - 1) {
		    					lineSolvInstr += " i";
		    				}
							lineSolvInstr += " (" + String.valueOf(cellInSet / cols + 1) + ", " + String.valueOf(cellInSet % cols + 1) + ")";
							sizeOfSet++;
						}
					}
		    		if (!solvingInstructions.contains(lineSolvInstr)) {
						if (chainLength == 2) {
			    			if (xwg == 0) {
			    				difficultyScore += 2800;
			    				xwg = 1;
			    			} else {
			    				difficultyScore += 1600;
			    			} 
						} else {
			    			if (sf4 == 0) {
			    				difficultyScore += 8000;
			    				sf4 = 1;
			    			} else {
			    				difficultyScore += 6000;
			    			} 
			    		}
						solvingInstructions += lineSolvInstr + ".\n";
		    		} else {
		    			return 0;
		    		}
				}	
				numRemoved++;
				possibilities[cell][beginVal] = 0;
				solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(beginVal + 1) + " iz �elije (" + String.valueOf(cell / cols + 1) + ", " + String.valueOf(cell % cols + 1) + ").\n" ;
				if (sequence() == 1) {
					return 1;
				}
			}
			return 0;
		}
	    if (cells.size() > 0) {
			if (rowOrcols == 0) {
				if (direction == 0) {
					for (int col = 0; col < cells.peek() % cols; col++){ 
			    		int numCell = cells.peek() / cols * cols + col;
			    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
					}
				} else {
					for (int col = cells.peek() % cols + 1; col < cols; col++){ 
			    		int numCell = cells.peek() / cols * cols + col;
			    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
					}
				}
			}
			if (rowOrcols == 1) {
				if (direction == 0) {
					for (int row = 0; row < cells.peek() / cols; row++){ 
			    		int numCell = row * cols + cells.peek() % cols;
			    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
					}
				} else {
					for (int row = cells.peek() / cols + 1; row < rows; row++){ 
			    		int numCell = row * cols + cells.peek() % cols;
			    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
					}
				}
			}
	    } else {
	    	for (int row = 0; row < rows; row++){ 
	    		for (int col = 0; col < cols; col++) {
		    		int numCell = row * cols + col;
		    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
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
		    	if (numberOptions > 1) {
	    			field[numCell].setFont(new Font("Arial", Font.PLAIN, fontsize));
		    	} else {
	    			field[numCell].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
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
		super(constructRows, constructCols, rowLimit, colLimit);
	}
	

	abstract public void resetHighlight();
	abstract public void highlightCell(int numCell);
	abstract public void highlightDigit();
	
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
