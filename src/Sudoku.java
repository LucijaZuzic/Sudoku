import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.plaf.metal.MetalButtonUI;


public abstract class Sudoku extends SudokuGrid {
	// Gumbi za odabir znamenki
	JButton digitButtons[];
	// Tekstualno podru�je za prikaz gre�ki
	JTextArea errorArea;
	// Tekstualno podru�je za prikaz uputa
	JTextArea instructionArea;
	// Tekstualno podru�je za prikaz odbranog polja 
	JButton zoomArea;
	// Oznaka za prikaz te�ine
	JLabel difficulty = new JLabel("");
	// Tekst uputa za rje�avanje
	String solvingInstructions;
	// Odabrana znamenka
	int selectedDigit = 0;
	// Te�ina zagonetke
	int difficultyScore = 0;
	// Skup pozicija uklonjenih vrijednosti
	Stack<Integer> lastRemovedPosOriginal = new Stack<Integer>();
	// Skup uklonjenih vrijednosti
	Stack<Integer> lastRemovedValOriginal = new Stack<Integer>();
	// Skup rotacijski simetri�nih pozicija uklonjenih vrijednosti
	Stack<Integer> lastRemovedPosSymetric = new Stack<Integer>();
	// Skup rotacijski simetri�nih uklonjenih vrijednosti
	Stack<Integer> lastRemovedValSymetric = new Stack<Integer>();
	// Broj praznih polja
	int unset = 0;
	// Je li uklju�en prikaz uputa korak po korak u zasebnom prozoru
	boolean showSteps = false;
	// A�urira mogu�e vrijednosti za �elije nakon upisa nove kona�ne vrijednosti
	public int fixPencilmarks() {
		int numChanged = 0;
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (temporary[row * cols + col] != 0) { 
	    			// Ako je kona�na vrijednost upisana uklanjamo mogu�nosti
			    	for (int val = 0; val < cols; val++) {
			    		possibilities[row * cols + col][val] = 0;
				    }
	    		} else {
	    			// Ukloni mogu�nosti za vrijednosti koje ve� postoje u istom stupcu
	    			for (int fullCol = 0; fullCol < cols; fullCol++) { 
			    		if (temporary[row * cols + fullCol] != 0 && possibilities[row * cols + col][temporary[row * cols + fullCol] - 1] == 1) { 
				    		possibilities[row * cols + col][temporary[row * cols + fullCol] - 1] = 0;
		    				numChanged = 1;
			    		}
				    }
	    			// Ukloni mogu�nosti za vrijednosti koje ve� postoje u istom redu
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
			    	// Ukloni mogu�nosti za vrijednosti koje ve� postoje u rastu�oj dijagonali (ako uklju�ujemo diajgonale i ako je broj na toj dijagonali)
	    			if (row == col && diagonalOn) {
					    for (int diagonally = 0; diagonally < cols; diagonally++){ 
					    	if (temporary[diagonally * cols + diagonally] != 0) {
			    				possibilities[row * cols + col][temporary[diagonally * cols + diagonally] - 1] = 0;
			    				numChanged = 1;
				    		}
					    }
	    			} 
			    	// Ukloni mogu�nosti za vrijednosti koje ve� postoje u padaju�oj dijagonali (ako uklju�ujemo diajgonale i ako je broj na toj dijagonali)
	    			if (row == cols - 1 - col && diagonalOn) {
					    for (int diagonally = 0; diagonally < cols; diagonally++){ 
					    	if (temporary[diagonally * cols + (cols - 1 - diagonally)] != 0) {
			    				possibilities[row * cols + col][temporary[diagonally * cols + (cols - 1 - diagonally)] - 1] = 0;
			    				numChanged = 1;
				    		}
					    }
	    			}
	    		}
		    	// Ukloni mogu�nosti prema odnosima ve�e-manje
	    		Set<Integer> visitedMax = new HashSet<Integer>();
	    		if (setMaxPossibility(row * cols + col, visitedMax) == 1) {
	    			numChanged = 1;
	    		}
		    	// Ukloni mogu�nosti prema odnosima manje-ve�e
	    		Set<Integer> visitedMin = new HashSet<Integer>();
	    		if (setMinPossibility(row * cols + col, visitedMin) == 1) {
	    			numChanged = 1;
	    		}
	    	}
		}
	    // Vra�amo 0 ako se nije promjenila niti jedna mogu�nost niti za jednu �eliju, a 1 ako se barem jedna mogu�nost barem jedne �elije promijenila
	    return numChanged;
	}
	// Tra�imo �elije koje imaju samo jednu mogu�u vrijednost (kandidata) 
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
			    		// Koristenje jedinog kandidata u �eliji ima tro�ak 100
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
			    	    			if (!InformationBox.stepBox("Broj " + String.valueOf(val + 1) + " je jedina mogu�a vrijednost �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ")", "Rje�ava�")) {
			    	    				showSteps = false;
			    	    			}
		    		    		}
			    		    	temporary[row * cols + col] = val + 1;
		    		    		if (val + 1 < 10) {
		    		    			field[row * cols + col].setText(String.valueOf(val + 1));
		    		    		} else {
		    		    			char c = 'A';
		    		    			c += val - 9;
		    		    			field[row * cols + col].setText("" + c);
		    		    		}
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
	// Dodaje se tro�ak za metode ogoljenog skupa i linija na ispis uputa za rje�avanje
	// setCells - skup brojeva �elija u ogoljenom skupu, matchString - popis vrijednosti koje pokriva ogoljeni skup, znakovni niz 0 i 1 koje ozna�avaju je li znamenka uklju�ena
	// containerNum - broj reda, stupca ili kutije u kojem se nalazi ogoljeni skup, containerType - string koji ozna�ava je li ogoljeni skup u redu, stupcu ili kutiji
	// firstCell - ishodi�na �elija ogoljenog skupa kojoj smo tra�ili �elije koje sadr�e neku od istih mogu�ih vrijednosti kao i ona
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
			// Ako smo ve� registrirali ovaj isti ogoljeni par, ne bodujemo ga dvaput
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
			// Ako smo ve� registrirali ovu istu ogoljenu trojku, ne bodujemo ju dvaput
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
			// Ako smo ve� registrirali ovu istu ogoljenu �etvorku, ne bodujemo ju dvaput
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
			// Ako smo ve� registrirali ovaj isti ogoljeni skup ve�i od �etiri, ne bodujemo ga dvaput
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
		// Dodavanje �elija skupa u upute za rje�avanje (slu�aj skupa u redu)
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
		// Dodajemo novi red u tekst uputa
    	solvingInstructions += lineSolvInstr;
    	// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
    	if (showSteps == true) {
		    instructionArea.setText(solvingInstructions);
		    print();
			if (!InformationBox.stepBox(lineSolvInstr, "Rje�ava�")) {
				showSteps = false;
			}
		}
	}
	// Tra�imo ogoljeni skup u retcima
	// firstRow - redak u kojem se nalazi ishodi�na �elija kojoj smo tra�ili �elije koje sadr�e neku od istih mogu�ih vrijednosti kao i ona
	// firstCol - stupac u kojem se nalazi ishodi�na �elija kojoj smo tra�ili �elije koje sadr�e neku od istih mogu�ih vrijednosti kao i ona
	// sameRowCells - skup brojeva �elija koje bi se mogle nalaziti u ogoljenom skupu
	public int nakedSetForRow (int firstRow, int firstCol, Set<Integer> sameRowCells) {
		// Ako je skup ve�i od ograni�ene duljine, nastavljamo dalje
		if (sameRowCells.size() >= depthLimit) {
			return 0;
		}
    	int firstCell = firstRow * cols + firstCol;
    	// Pretra�ujemo sve nove �elije u istom redu kao ishodi�te, ali u ve�im stupcima, da izbjegnemo ponavljanje
    	for (int nextColInRow = firstCol + 1; nextColInRow < cols; nextColInRow++) {
	    	int newToSet = firstRow * cols + nextColInRow;
	    	if (numPossibilities[newToSet] <= 1) {
	    		continue;
	    	}
    		int match = 0;
    		// Pretra�ujemo sve �elije koje su ve� u ogoljenom skupu, a one se nalaze prije �elije koju dodajemo
    		for (int previousColInRow = 0; previousColInRow < nextColInRow; previousColInRow++) {
		    	int alreadyInSet = firstRow * cols + previousColInRow;
		    	if (numPossibilities[alreadyInSet] <= 1) {
		    		continue;
		    	}
    			if (alreadyInSet == firstCell || sameRowCells.contains(alreadyInSet)) {
		    		for (int val = 0; val < cols; val++) {
		    			// Provjeravamo podudara li se nova �elija sa svakom od �elija koje bi se mogle nalaziti u ogoljenom skupu i s ishodi�nom �elijom u barem jednoj od mogu�ih vrijednosti
		    			if (possibilities[newToSet][val] == 1 && possibilities[alreadyInSet][val] == 1 && !sameRowCells.contains(newToSet) && newToSet != firstCell) {
		    				match++;
		    				break;
		    			}
		    		}
    			}
    		}
    		// Ako se nova �elija podudara sa svim �elijama u ogoljenom skupu i s ishodi�nom �elijom, mo�emo ju dodati u skriveni skup
    		if (match == sameRowCells.size() + 1) {
    			Set<Integer> sameRowCellsNextIteration = new HashSet<Integer>();
        		for (int numCell = 0; numCell < rows * cols; numCell++) {
        			if (sameRowCells.contains(numCell)) {
        				sameRowCellsNextIteration.add(numCell);
        			}
        		}
        		sameRowCellsNextIteration.add(newToSet);
        		// Za svaku �eliju koju dodajemo u ogoljeni skup pokre�emo rekurziju, a u postoje�emo pozivu funkcije nastavljamo bez da ju dodamo
				if (nakedSetForRow(firstRow, firstCol, sameRowCellsNextIteration) == 1) {
	    			return 1;
				}
    		}
    		// Preborajavamo vrijednosti koje pokriva ogoljeni skup �elija u obliku niza znakova 0 i 1
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int val = 0; val < cols; val++) {
	    		String containsVal = "0";
	    		for (int col = 0; col < cols; col++) {
	    			int alreadyInSet = firstRow * cols + col;
	    			if (sameRowCells.contains(alreadyInSet) || alreadyInSet == firstCell) {
	    				// Ako neka od �elija u ogoljenom skupu pokriva vrijednost, dodajemo znak 1
				    	if (possibilities[alreadyInSet][val] == 1) {
				    		containsVal = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsVal;
    		}
	    	// Ako ogoljeni skup pokriva jednako vrijednosti koliko sadr�i �elija te nije prazan skup, to je ispravan ogoljeni skup
	    	if (sameRowCells.size() == matchStringLen - 1 && sameRowCells.size() > 0) {
			    int numRemoved = 0;
	    		for (int col = 0; col < cols; col++) {
			    	int notInSet = firstRow * cols + col;
			    	// Ako �elija nije u ogoljenom skupu mo�da joj mo�emo ukloniti neku od mogu�nosti
			    	if (!sameRowCells.contains(notInSet) && notInSet != firstCell) {
			    		for (int val = 0; val < cols; val++) {
			    			// Ako �elija sadr�i mogu�u vrijednost koju pokriva ogoljeni skup, mo�emo ukloniti tu mogu�nost
			    			if (matchString.charAt(val) == '1' && possibilities[notInSet][val] == 1 && temporary[notInSet] == 0) {
			    				// Isti uo�eni ogoljeni skup boduje se samo jednom, iako rezultira uklanjanjem vi�e mogu�nosti
			    				if (numRemoved == 0) {
			    					difficultySettingNaked(sameRowCells, matchString, firstRow, "redu", firstCell);
			    				}
			    				// Dodajemo novi red za uklanjanje mogu�nosti u tekst uputa
			    				solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").\n";
			    				// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
			    				if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").", "Rje�ava�")) {
			    	    				showSteps = false;
			    	    			}
					    		}
								possibilities[notInSet][val] = 0;
						    	// Ukloni mogu�nosti prema odnosima ve�e-manje
					    		Set<Integer> visitedMax = new HashSet<Integer>();
								setMaxPossibility(notInSet, visitedMax);
						    	// Ukloni mogu�nosti prema odnosima manje-ve�e
					    		Set<Integer> visitedMin = new HashSet<Integer>();
								setMinPossibility(notInSet, visitedMin);
					    		// Ako nismo postavili sve �elije, pozivamo sekvencu metoda za rje�avanje, ako ona uspije sve rje�iti, prekidamo rje�avanje
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
	// Tra�imo ogoljeni skup u stupcima
	// firstRow - redak u kojem se nalazi ishodi�na �elija kojoj smo tra�ili �elije koje sadr�e neku od istih mogu�ih vrijednosti kao i ona
	// firstCol - stupac u kojem se nalazi ishodi�na �elija kojoj smo tra�ili �elije koje sadr�e neku od istih mogu�ih vrijednosti kao i ona
	// sameColumnCells - skup brojeva �elija koje bi se mogle nalaziti u ogoljenom skupu
	public int nakedSetForCol (int firstRow, int firstCol, Set<Integer> sameColumnCells) {
		// Ako je skup ve�i od ograni�ene duljine, nastavljamo dalje
		if (sameColumnCells.size() >= depthLimit) {
			return 0;
		}
		int firstCell = firstRow * cols + firstCol;
    	// Pretra�ujemo sve nove �elije u istom stupcu kao ishodi�te, ali u ve�im retcima, da izbjegnemo ponavljanje
    	for (int nextRowInCol = firstRow + 1; nextRowInCol < rows; nextRowInCol++) {
	    	int newToSet = nextRowInCol * cols + firstCol;
	    	if (numPossibilities[newToSet] <= 1) {
	    		continue;
	    	}
    		int match = 0;
    		// Pretra�ujemo sve �elije koje su ve� u ogoljenom skupu, a one se nalaze prije �elije koju dodajemo
    		for (int previousRowInCol = 0; previousRowInCol < nextRowInCol; previousRowInCol++) {
		    	int alreadyInSet = previousRowInCol * cols + firstCol;
		    	if (numPossibilities[alreadyInSet] <= 1) {
		    		continue;
		    	}
    			if (alreadyInSet == firstCell || sameColumnCells.contains(alreadyInSet)) {
		    		for (int val = 0; val < cols; val++) {
		    			// Provjeravamo podudara li se nova �elija sa svakom od �elija koje bi se mogle nalaziti u ogoljenom skupu i s ishodi�nom �elijom u barem jednoj od mogu�ih vrijednosti
		    			if (possibilities[newToSet][val] == 1 && possibilities[alreadyInSet][val] == 1 && !sameColumnCells.contains(newToSet) && newToSet != firstCell) {
		    				match++;
		    				break;
		    			}
		    		}
    			}
    		}
    		// Ako se nova �elija podudara sa svim �elijama u ogoljenom skupu i s ishodi�nom �elijom, mo�emo ju dodati u ogoljeni skup
    		if (match == sameColumnCells.size() + 1) {
    			Set<Integer> sameColumnNextIteration = new HashSet<Integer>();
	    		for (int numCell = 0; numCell < rows * cols; numCell++) {
	    			if (sameColumnCells.contains(numCell)) {
	    				sameColumnNextIteration.add(numCell);
	    			}
	    		}
	    		sameColumnNextIteration.add(newToSet);
        		// Za svaku �eliju koju dodajemo u ogoljeni skup pokre�emo rekurziju, a u postoje�emo pozivu funkcije nastavljamo bez da ju dodamo
				if (nakedSetForCol(firstRow, firstCol, sameColumnNextIteration) == 1) {
	    			return 1;
				}
			}
    		// Preborajavamo vrijednosti koje pokriva ogoljeni skup �elija u obliku niza znakova 0 i 1
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int val = 0; val < cols; val++) {
	    		String containsVal = "0";
	    		for (int row = 0; row < rows; row++) {
	    			int alreadyInSet = row * cols + firstCol;
    				// Ako neka od �elija u ogoljenom skupu pokriva vrijednost, dodajemo znak 1
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
	    	// Ako ogoljeni skup pokriva jednako vrijednosti koliko sadr�i �elija te nije prazan skup, to je ispravan ogoljeni skup
	    	if (sameColumnCells.size() == matchStringLen - 1 && sameColumnCells.size() > 0) {
			    int numRemoved = 0;
	    		for (int row = 0; row < rows; row++) {
			    	int notInSet = row * cols + firstCol;
			    	// Ako �elija nije u ogoljenom skupu mo�da joj mo�emo ukloniti neku od mogu�nosti
			    	if (!sameColumnCells.contains(notInSet) && notInSet != firstCell) {
			    		for (int val = 0; val < cols; val++) {
			    			// Ako �elija sadr�i mogu�u vrijednost koju pokriva ogoljeni skup, mo�emo ukloniti tu mogu�nost
			    			if (matchString.charAt(val) == '1' && possibilities[notInSet][val] == 1 && temporary[notInSet] == 0) {
			    				// Isti uo�eni ogoljeni skup boduje se samo jednom, iako rezultira uklanjanjem vi�e mogu�nosti
			    				if (numRemoved == 0) {
			    					difficultySettingNaked(sameColumnCells, matchString, firstCol, "stupcu", firstCell);
			    				}
			    				// Dodajemo novi red za uklanjanje mogu�nosti u tekst uputa
			    				solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").\n";
			    				// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
			    				if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").", "Rje�ava�")) {
			    	    				showSteps = false;
			    					}
			    				}
			    				possibilities[notInSet][val] = 0;
						    	// Ukloni mogu�nosti prema odnosima ve�e-manje
					    		Set<Integer> visitedMax = new HashSet<Integer>();
								setMaxPossibility(notInSet, visitedMax);
						    	// Ukloni mogu�nosti prema odnosima manje-ve�e
					    		Set<Integer> visitedMin = new HashSet<Integer>();
								setMinPossibility(notInSet, visitedMin);
					    		// Ako nismo postavili sve �elije, pozivamo sekvencu metoda za rje�avanje, ako ona uspije sve rje�iti, prekidamo rje�avanje
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
	// Tra�imo ogoljeni skup u kutijama
	// firstRow - redak u kojem se nalazi ishodi�na �elija kojoj smo tra�ili �elije koje sadr�e neku od istih mogu�ih vrijednosti kao i ona
	// firstCol - stupac u kojem se nalazi ishodi�na �elija kojoj smo tra�ili �elije koje sadr�e neku od istih mogu�ih vrijednosti kao i ona
	// sameBoxCells - skup brojeva �elija koje bi se mogle nalaziti u ogoljenom skupu
	public int nakedSetForBox(int firstRow, int firstCol, Set<Integer> sameBoxCells) {
		// Ako je skup ve�i od ograni�ene duljine, nastavljamo dalje
		if (sameBoxCells.size() >= depthLimit) {
			return 0;
		}
		int firstCell = firstRow * cols + firstCol;
    	// Pretra�ujemo sve nove �elije u istoj kutiji kao ishodi�te, ali u ve�im �elijama, da izbjegnemo ponavljanje
    	for (int newToSet = firstCell + 1; newToSet < rows * cols; newToSet++) {
	    	if (boxNumber[newToSet] != boxNumber[firstCell]) {
	    		continue;
	    	}
	    	if (numPossibilities[newToSet] <= 1) {
	    		continue;
	    	}
	    	int match = 0;
    		// Pretra�ujemo sve �elije koje su ve� u ogoljenom skupu, a one se nalaze prije �elije koju dodajemo
	    	for (int alreadyInSet = 0; alreadyInSet < newToSet; alreadyInSet++) {
		    	if (boxNumber[alreadyInSet] != boxNumber[firstCell]) {
		    		continue;
		    	}
		    	if (numPossibilities[alreadyInSet] <= 1) {
		    		continue;
		    	}
    			if (alreadyInSet == firstCell || sameBoxCells.contains(alreadyInSet)) {
		    		for (int val = 0; val < cols; val++) {
		    			// Provjeravamo podudara li se nova �elija sa svakom od �elija koje bi se mogle nalaziti u ogoljenom skupu i s ishodi�nom �elijom u barem jednoj od mogu�ih vrijednosti
		    			if (possibilities[newToSet][val] == 1 && possibilities[alreadyInSet][val] == 1 && !sameBoxCells.contains(newToSet) && newToSet != firstCell) {
		    				match++;
		    				break;
		    			}
		    		}
    			}
	    	}
    		// Ako se nova �elija podudara sa svim �elijama u ogoljenom skupu i s ishodi�nom �elijom, mo�emo ju dodati u ogoljeni skup
    		if (match == sameBoxCells.size() + 1) {
    			Set<Integer> sameBoxNextIteration = new HashSet<Integer>();
	    		for (int numCell = 0; numCell < rows * cols; numCell++) {
	    			if (sameBoxCells.contains(numCell)) {
	    				sameBoxNextIteration.add(numCell);
	    			}
	    		}
	    		sameBoxNextIteration.add(newToSet);
        		// Za svaku �eliju koju dodajemo u ogoljeni skup pokre�emo rekurziju, a u postoje�emo pozivu funkcije nastavljamo bez da ju dodamo
				if (nakedSetForBox(firstRow, firstCol, sameBoxNextIteration) == 1) {
	    			return 1;
				}
    		}
    		// Preborajavamo vrijednosti koje pokriva ogoljeni skup �elija u obliku niza znakova 0 i 1
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int val = 0; val < cols; val++) {
	    		String containsVal = "0";
	    		for (int numCell = 0; numCell < rows * cols; numCell++) {
			    	if (boxNumber[firstCell] != boxNumber[numCell]) {
			    		continue;
			    	}
    				// Ako neka od �elija u ogoljenom skupu pokriva vrijednost, dodajemo znak 1
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
	    	// Ako ogoljeni skup pokriva jednako vrijednosti koliko sadr�i �elija te nije prazan skup, to je ispravan ogoljeni skup
	    	if (sameBoxCells.size() == matchStringLen - 1 && sameBoxCells.size() > 0) {
		    	int numRemoved = 0;
		    	for (int numCell = 0; numCell < rows * cols; numCell++) {
			    	int notInSet = numCell;
			    	if (boxNumber[firstCell] != boxNumber[notInSet]) {
			    		continue;
			    	}
			    	// Ako �elija nije u ogoljenom skupu mo�da joj mo�emo ukloniti neku od mogu�nosti
			    	if (!sameBoxCells.contains(notInSet) && notInSet != firstCell) {
			    		for (int val = 0; val < cols; val++) {
			    			// Ako �elija sadr�i mogu�u vrijednost koju pokriva ogoljeni skup, mo�emo ukloniti tu mogu�nost
			    			if (matchString.charAt(val) == '1' && possibilities[notInSet][val] == 1 && temporary[notInSet] == 0) {
			    				// Isti uo�eni ogoljeni skup boduje se samo jednom, iako rezultira uklanjanjem vi�e mogu�nosti
			    				if (numRemoved == 0) {
			    					difficultySettingNaked(sameBoxCells, matchString, boxNumber[firstCell], "stupcu", firstCell);
			    				}
			    				// Dodajemo novi red za uklanjanje mogu�nosti u tekst uputa
			    				solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").\n";
			    				// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
			    				if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").", "Rje�ava�")) {
			    	    				showSteps = false;
			    	    			}
					    		}
			    				possibilities[notInSet][val] = 0;
						    	// Ukloni mogu�nosti prema odnosima ve�e-manje
					    		Set<Integer> visitedMax = new HashSet<Integer>();
								setMaxPossibility(notInSet, visitedMax);
						    	// Ukloni mogu�nosti prema odnosima manje-ve�e
					    		Set<Integer> visitedMin = new HashSet<Integer>();
								setMinPossibility(notInSet, visitedMin);
					    		// Ako nismo postavili sve �elije, pozivamo sekvencu metoda za rje�avanje, ako ona uspije sve rje�iti, prekidamo rje�avanje
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
	// Tra�imo ogoljeni skup u svim retcima, stupcima i kutijama
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
		    	// Tra�imo ogoljeni skup u redu, s ishodi�tem u odre�enoj �eliji
		    	if (nakedSetForRow(row, col, sameRow) == 1) {
					return 1;
				}
		    	// Tra�imo ogoljeni skup u stupcu, s ishodi�tem u odre�enoj �eliji
		    	Set<Integer> sameColumn = new HashSet<Integer>();
		    	if (nakedSetForCol(row, col, sameColumn) == 1) {
					return 1;
				}
		    	// Tra�imo ogoljeni skup u kutiji, s ishodi�tem u odre�enoj �eliji
		    	Set<Integer> sameBox = new HashSet<Integer>();
		    	if (nakedSetForBox(row, col, sameBox) == 1) {
					return 1;
		    	}
	    	}
	    }
		return 0;
	}
	// Polje znakovnih nizova koji predstavljaju mogu�e pozicije definirane vrijednosti unutar odre�enog reda, stupca ili kutije i sastoje se od 0 (mogu�e) ili 1 (nije mogu�e)
	String valuePos[] = new String[cols];
	// Broj kori�tenja za metodu skrivenih skupova po veli�ini skupa
	int us2 = 0;
	int us3 = 0;
	int us4 = 0;
	// Dodaje se tro�ak za metode skrivenog skupa i linija na ispis uputa za rje�avanje
	// setValues - skup vrijednosti koje su pokrivene skrivenim skupom, matchString - popis �elija koje pokriva skriveni skup, znakovni niz 0 i 1 koje ozna�avaju je li znamenka uklju�ena
	// matchStringLen - broj �elija koje pokriva skriveni skup
	// containerNum - broj reda, stupca ili kutije u kojem se nalazi skriveni skup, containerType - string koji ozna�ava je li skriveni skup u redu, stupcu ili kutiji
	// firstVal - ishodi�na vrijednost skrivenog skupa kojoj smo tra�ili vrijednosti koje se nalaze kao mogu�nosti u nekoj od istih �elija kao i ona
	public void difficultySettingHidden(Set<Integer> setValues, String matchString, int matchStringLen, int containerNum, String containerType, int firstVal) {
		String setType = "Skriven";
		String lineSolvInstr = "";
		// Dio koda za skriveni par
		if (setValues.size() == 1) {
			// Dodavanje vrijednosti skupa u upute za rje�avanje
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
			// Ako smo ve� registrirali ovaj isti skriveni par, ne bodujemo ga dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (us2 == 0) {
    				// Kada prvi put na�emo skriveni par, tro�ak je 1500
    				difficultyScore += 1500;
    				us2 = 1;
    			} else {
    				// Kada idu�i put na�emo skriveni par, tro�ak je 1200
    				difficultyScore += 1200;
    			}
    		} else {
    			return;
    		}
		}
		// Dio koda za skrivenu trojku
		if (setValues.size() == 2) {
			// Dodavanje vrijednosti skupa u upute za rje�avanje
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
			// Ako smo ve� registrirali ovu istu skrivenu trojku, ne bodujemo ju dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (us3 == 0) {
    				// Kada prvi put na�emo skrivenu trojku, tro�ak je 2400
    				difficultyScore += 2400;
    				us3 = 1;
    			} else {
    				// Kada idu�i put na�emo skrivenu trojku, tro�ak je 1600
    				difficultyScore += 1600;
    			}
    		} else {
    			return;
    		}
		}
		// Dio koda za skrivenu �etvorku
		if (setValues.size() == 3) {
			// Dodavanje vrijednosti skupa u upute za rje�avanje
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
			// Ako smo ve� registrirali ovu istu skrivenu �etvorku, ne bodujemo ju dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (us4 == 0) {
    				// Kada prvi put na�emo skrivenu �etvorku, tro�ak je 7000
    				difficultyScore += 7000;
    				us4 = 1;
    			} else {
    				// Kada idu�i put na�emo skrivenu �etvorku, tro�ak je 5000
    				difficultyScore += 5000;
    			}
    		} else {
    			return;
    		}
		}
		// Dio koda za skriveni skup ve�i od �etiri
		if (setValues.size() > 3) {
			// Dodavanje vrijednosti skupa u upute za rje�avanje
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
			// Ako smo ve� registrirali ovaj istu skriveni skup ve�i od �etiri, ne bodujemo ga dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (us4 == 0) {
    				// Kada prvi put na�emo skriveni skup ve�i od 4, tro�ak je 7000 (broji se kao i skrivena �etvorka)
    				difficultyScore += 7000;
    				us4 = 1;
    			} else {
    				// Kada idu�i put na�emo skriveni skup ve�i od 4, tro�ak je 5000 (broji se kao i skrivena �etvorka)
    				difficultyScore += 5000;
    			}
    		} else {
    			return;
    		}
		}
		lineSolvInstr += " u �elijama";
		int sizeOfSet = 0;
		// Dodavanje �elija skupa u upute za rje�avanje (slu�aj skupa u redu)
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
		// Dodavanje �elija skupa u upute za rje�avanje (slu�aj skupa u stupcu)
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
		// Dodavanje �elija skupa u upute za rje�avanje (slu�aj skupa u kutiji)
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
    	// Dodajemo novi red u tekst uputa
    	solvingInstructions += lineSolvInstr;
    	// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
    	if (showSteps == true) {
		    instructionArea.setText(solvingInstructions);
		    print();
			if (!InformationBox.stepBox(lineSolvInstr, "Rje�ava�")) {
				showSteps = false;
			}
		}
	}
	// Tra�imo skriveni skup u retcima
	// firstVal - ishodi�na vrijednost skrivenog skupa kojoj smo tra�ili vrijednosti koje se nalaze kao mogu�nosti u nekoj od istih �elija kao i ona
	// firstRow - redak u kojem tra�imo skriveni skup
	// setRowValues - skup vrijednosti koji bi mogli biti pokrivene skrivenim skupom u redu
	public int hiddenSetForRow(int firstVal, int firstRow, Set<Integer> sameRowValues) {
		// Ako je skup ve�i od ograni�ene duljine, nastavljamo dalje
		if (sameRowValues.size() >= depthLimit) {
			return 0;
		}
    	// Pretra�ujemo sve nove vrijednosti ve�e od ishodi�ne, da izbjegnemo ponavljanje
		for (int newToSet = firstVal + 1; newToSet < cols; newToSet++) {
			if (newToSet == firstVal || sameRowValues.contains(newToSet)) {
    			continue;
    		}
    		int match = 0;
    		// Pretra�ujemo sve vrijednosti koje su ve� u skrivenom skupu
    		for (int alreadyInSet = 0; alreadyInSet < cols; alreadyInSet++) {
    			if (alreadyInSet == firstVal || sameRowValues.contains(alreadyInSet)) {
		    		for (int positionInRow = 0; positionInRow < cols; positionInRow++) {
		    			// Provjeravamo podudara li se nova vrijednost sa svakom od vrijednosti koje bi mogle biti pokrivene skrivenim skupom i s ishodi�nom vrjedno��u u barem jednoj od mogu�ih �elija
		    			if (valuePos[newToSet].charAt(positionInRow) == '1' && valuePos[alreadyInSet].charAt(positionInRow) == '1') {
		    				match++;
		    				break;
		    			}
		    		}
    			}
    		}
    		// Ako se nova vrijednost nalazi kao mogu�nosti u zajedni�koj �eliji sa svakom od vrijednosti u skrivenom skupu i s ishodi�nom vrijedno��u, mo�emo ju dodati u skriveni skup
    		if (match == sameRowValues.size() + 1) {
    			Set<Integer> sameRowValuesNextIteration = new HashSet<Integer>();
        		for (int val = 0; val < cols; val++) {
        			if (sameRowValues.contains(val)) {
        				sameRowValuesNextIteration.add(val);
        			}
        		}
        		sameRowValuesNextIteration.add(newToSet);
        		// Za svaku �eliju koju dodajemo u skriveni skup pokre�emo rekurziju, a u postoje�emo pozivu funkcije nastavljamo bez da ju dodamo
				if (hiddenSetForRow(firstVal, firstRow, sameRowValuesNextIteration) == 1) {
	    			return 1;
				}
    		}
    		// Preborajavamo �elije koje pokrivaju vrijednosti skrivenog skupa u obliku niza znakova 0 i 1
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int col = 0; col < cols; col++) {
	    		String containsCell = "0";
	    		for (int val = 0; val < cols; val++) {
	    			if (val == firstVal || sameRowValues.contains(val)) {
	    				// Ako neka od vrijednosti u skrivenom skupu mo�e biti u odre�enom stupcu unutar reda, dodajemo znak 1
				    	if (valuePos[val].charAt(col) == '1') {
				    		containsCell = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsCell;
    		}
	    	// Ako skriveni skup pokriva jednako vrijednosti koliko sadr�i �elija te nije prazan skup, to je ispravan skriveni skup
	    	if (sameRowValues.size() == matchStringLen - 1 && sameRowValues.size() > 0) {
	    		int numRemoved = 0;
				for (int col = 0; col < cols; col++) {
			    	// Ako je �elija u skrivenom skupu mo�da joj mo�emo ukloniti neku od mogu�nosti
					if (matchString.charAt(col) == '1') {
						 for (int val = 0; val < cols; val++) {
				    		// Ako �elija sadr�i mogu�u vrijednost koju ne pokriva skriveni skup, mo�emo ukloniti tu mogu�nost
							if (!sameRowValues.contains(val) && val != firstVal && possibilities[firstRow * cols + col][val] == 1) {
			    				// Isti uo�eni skriveni skup boduje se samo jednom, iako rezultira uklanjanjem vi�e mogu�nosti
						    	if (numRemoved == 0) {
						    		difficultySettingHidden(sameRowValues, matchString, matchStringLen, firstRow, "redu", firstVal);
						    	}		    			    		
						    	// Dodajemo novi red za uklanjanje mogu�nosti u tekst uputa
								solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(firstRow + 1) + ", " + String.valueOf(col + 1) + ").\n" ;
								// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(firstRow + 1) + ", " + String.valueOf(col + 1) + ").", "Rje�ava�")) {
			    	    				showSteps = false;
			    	    			}
					    		}
			    				possibilities[firstRow * cols + col][val] = 0;
						    	// Ukloni mogu�nosti prema odnosima ve�e-manje
					    		Set<Integer> visitedMax = new HashSet<Integer>();
								setMaxPossibility(firstRow * cols + col, visitedMax);
						    	// Ukloni mogu�nosti prema odnosima manje-ve�e
					    		Set<Integer> visitedMin = new HashSet<Integer>();
								setMinPossibility(firstRow * cols + col, visitedMin);
					    		// Ako nismo postavili sve �elije, pozivamo sekvencu metoda za rje�avanje, ako ona uspije sve rje�iti, prekidamo rje�avanje
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
	// Tra�imo skriveni skup u stupcima
	// firstVal - ishodi�na vrijednost skrivenog skupa kojoj smo tra�ili vrijednosti koje se nalaze kao mogu�nosti u nekoj od istih �elija kao i ona
	// firstCol - stupac u kojem tra�imo skriveni skup
	// sameColValues - skup vrijednosti koji bi mogli biti pokrivene skrivenim skupom u stupcu
	public int hiddenSetForCol(int firstVal, int firstCol, Set<Integer> sameColValues) {	
		// Ako je skup ve�i od ograni�ene duljine, nastavljamo dalje
		if (sameColValues.size() >= depthLimit) {
			return 0;
		}
    	// Pretra�ujemo sve nove vrijednosti ve�e od ishodi�ne, da izbjegnemo ponavljanje
		for (int newToSet = firstVal + 1; newToSet < cols; newToSet++) {
			if (newToSet == firstVal || sameColValues.contains(newToSet)) {
				continue;
			}
			int match = 0;
    		// Pretra�ujemo sve vrijednosti koje su ve� u skrivenom skupu
			for (int alreadyInSet = 0; alreadyInSet < cols; alreadyInSet++) {
				if (alreadyInSet == firstVal || sameColValues.contains(alreadyInSet)) {
		    		for (int positionInCol = 0; positionInCol < cols; positionInCol++) {
		    			// Provjeravamo podudara li se nova vrijednost sa svakom od vrijednosti koje bi mogle biti pokrivene skrivenim skupom i s ishodi�nom vrjedno��u u barem jednoj od mogu�ih �elija
		    			if (valuePos[newToSet].charAt(positionInCol) == '1' && valuePos[alreadyInSet].charAt(positionInCol) == '1') {
		    				match++;
		    				break;
		    			}
		    		}
				}
			}
    		// Ako se nova vrijednost nalazi kao mogu�nosti u zajedni�koj �eliji sa svakom od vrijednosti u skrivenom skupu i s ishodi�nom vrijedno��u, mo�emo ju dodati u skriveni skup
    		if (match == sameColValues.size() + 1) {
    			Set<Integer> sameColValuesNextIteration = new HashSet<Integer>();
        		for (int val = 0; val < cols; val++) {
        			if (sameColValues.contains(val)) {
        				sameColValuesNextIteration.add(val);
        			}
        		}
        		sameColValuesNextIteration.add(newToSet);
        		// Za svaku �eliju koju dodajemo u skriveni skup pokre�emo rekurziju, a u postoje�emo pozivu funkcije nastavljamo bez da ju dodamo
				if (hiddenSetForCol(firstVal, firstCol, sameColValuesNextIteration) == 1) {
	    			return 1;
				}
    		}
    		// Preborajavamo �elije koje pokrivaju vrijednosti skrivenog skupa u obliku niza znakova 0 i 1
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int row = 0; row < rows; row++) {
	    		String containsCell = "0";
	    		for (int val = 0; val < cols; val++) {
	    			if (val == firstVal || sameColValues.contains(val)) {
	    				// Ako neka od vrijednosti u skrivenom skupu mo�e biti u odre�enom redu unutar stupca, dodajemo znak 1
				    	if (valuePos[val].charAt(row) == '1') {
				    		containsCell = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsCell;
    		}
	    	// Ako skriveni skup pokriva jednako vrijednosti koliko sadr�i �elija te nije prazan skup, to je ispravan skriveni skup
	    	if (sameColValues.size() == matchStringLen - 1 && sameColValues.size() > 0) {
				int numRemoved = 0;
				for (int row = 0; row < rows; row++) {
			    	// Ako je �elija u skrivenom skupu mo�da joj mo�emo ukloniti neku od mogu�nosti
					if (matchString.charAt(row) == '1') {
						for (int val = 0; val < cols; val++) {
				    		// Ako �elija sadr�i mogu�u vrijednost koju ne pokriva skriveni skup, mo�emo ukloniti tu mogu�nost
							if (!sameColValues.contains(val) && val != firstVal && possibilities[row * cols + firstCol][val] == 1) {
			    				// Isti uo�eni skriveni skup boduje se samo jednom, iako rezultira uklanjanjem vi�e mogu�nosti
						    	if (numRemoved == 0) {
						    		difficultySettingHidden(sameColValues, matchString, matchStringLen, firstCol, "stupcu", firstVal);
						    	}		    			    	
						    	// Dodajemo novi red za uklanjanje mogu�nosti u tekst uputa
								solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(firstCol + 1) + ").\n";
								// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(firstCol + 1) + ").", "Rje�ava�")) {
			    	    				showSteps = false;
			    	    			}
					    		}
								possibilities[row * cols + firstCol][val] = 0;
						    	// Ukloni mogu�nosti prema odnosima ve�e-manje
					    		Set<Integer> visitedMax = new HashSet<Integer>();
								setMaxPossibility(row * cols + firstCol, visitedMax);
						    	// Ukloni mogu�nosti prema odnosima manje-ve�e
					    		Set<Integer> visitedMin = new HashSet<Integer>();
								setMinPossibility(row * cols + firstCol, visitedMin);
					    		// Ako nismo postavili sve �elije, pozivamo sekvencu metoda za rje�avanje, ako ona uspije sve rje�iti, prekidamo rje�avanje
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
	// Tra�imo skriveni skup u kutijama
	// firstVal - ishodi�na vrijednost skrivenog skupa kojoj smo tra�ili vrijednosti koje se nalaze kao mogu�nosti u nekoj od istih �elija kao i ona
	// firstBox - kutija u kojoj tra�imo skriveni skup
	// sameBoxValues - skup vrijednosti koji bi mogli biti pokrivene skrivenim skupom u kutiji
	public int hiddenSetForBox(int firstVal, int firstBox, Set<Integer> sameBoxValues) {	
		// Ako je skup ve�i od ograni�ene duljine, nastavljamo dalje
		if (sameBoxValues.size() >= depthLimit) {
			return 0;
		}
    	// Pretra�ujemo sve nove vrijednosti ve�e od ishodi�ne, da izbjegnemo ponavljanje
		for (int newToSet = firstVal + 1; newToSet < cols; newToSet++) {
			if (newToSet == firstVal || sameBoxValues.contains(newToSet)) {
				continue;
			}
			int match = 0;
    		// Pretra�ujemo sve vrijednosti koje su ve� u skrivenom skupu
			for (int alreadyInSet = 0; alreadyInSet < cols; alreadyInSet++) {
				if (alreadyInSet == firstVal || sameBoxValues.contains(alreadyInSet)) {
		    		for (int positionInBox = 0; positionInBox < cols; positionInBox++) {
		    			// Provjeravamo podudara li se nova vrijednost sa svakom od vrijednosti koje bi mogle biti pokrivene skrivenim skupom i s ishodi�nom vrjedno��u u barem jednoj od mogu�ih �elija
		    			if (valuePos[newToSet].charAt(positionInBox) == '1' && valuePos[alreadyInSet].charAt(positionInBox) == '1') {
		    				match++;
		    				break;
		    			}
		    		}
				}
			}
    		// Ako se nova vrijednost nalazi kao mogu�nosti u zajedni�koj �eliji sa svakom od vrijednosti u skrivenom skupu i s ishodi�nom vrijedno��u, mo�emo ju dodati u skriveni skup
    		if (match == sameBoxValues.size() + 1) {
    			Set<Integer> sameBoxValuesNextIteration = new HashSet<Integer>();
        		for (int val = 0; val < cols; val++) {
        			if (sameBoxValues.contains(val)) {
        				sameBoxValuesNextIteration.add(val);
        			}
        		}
        		sameBoxValuesNextIteration.add(newToSet);
        		// Za svaku �eliju koju dodajemo u skriveni skup pokre�emo rekurziju, a u postoje�emo pozivu funkcije nastavljamo bez da ju dodamo
				if (hiddenSetForBox(firstVal, firstBox, sameBoxValuesNextIteration) == 1) {
	    			return 1;
				}
			}
    		// Preborajavamo �elije koje pokrivaju vrijednosti skrivenog skupa u obliku niza znakova 0 i 1
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int box = 0; box < cols; box++) {
	    		String containsCell = "0";
	    		for (int val = 0; val < cols; val++) {
	    			if (val == firstVal || sameBoxValues.contains(val)) {
	    				// Ako neka od vrijednosti u skrivenom skupu mo�e biti u odre�enoj �eliji unutar kutije, dodajemo znak 1
				    	if (valuePos[val].charAt(box) == '1') {
				    		containsCell = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsCell;
    		}
	    	// Ako skriveni skup pokriva jednako vrijednosti koliko sadr�i �elija te nije prazan skup, to je ispravan skriveni skup
	    	if (sameBoxValues.size() == matchStringLen - 1 && sameBoxValues.size() > 0) {
				int cellPositionInBox = -1;
				int numRemoved = 0;
				for (int numCell = 0; numCell < rows * cols; numCell++) {
		    		if (firstBox != boxNumber[numCell]) {
		    			continue;
		    		} else {
		    			cellPositionInBox++;
		    		}
			    	// Ako je �elija u skrivenom skupu mo�da joj mo�emo ukloniti neku od mogu�nosti
					if (matchString.charAt(cellPositionInBox) == '1') {
						for (int notInSet = 0; notInSet < cols; notInSet++) {
				    		// Ako �elija sadr�i mogu�u vrijednost koju ne pokriva skriveni skup, mo�emo ukloniti tu mogu�nost
							if (!sameBoxValues.contains(notInSet) && notInSet != firstVal && possibilities[numCell][notInSet] == 1) {
			    				// Isti uo�eni skriveni skup boduje se samo jednom, iako rezultira uklanjanjem vi�e mogu�nosti
						    	if (numRemoved == 0) {
						    		difficultySettingHidden(sameBoxValues, matchString, matchStringLen, firstBox, "kutiji", firstVal);
						    	}		 
						    	// Dodajemo novi red za uklanjanje mogu�nosti u tekst uputa
								solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(notInSet + 1) + " iz �elije (" + String.valueOf(numCell / cols + 1) + ", " + String.valueOf(numCell % cols + 1) + ").\n";
								// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Uklanjam mogu�nost " + String.valueOf(notInSet + 1) + " iz �elije (" + String.valueOf(numCell / cols + 1) + ", " + String.valueOf(numCell % cols + 1) + ").", "Rje�ava�")) {
			    	    				showSteps = false;
			    	    			}
					    		}
								possibilities[numCell][notInSet] = 0;
						    	// Ukloni mogu�nosti prema odnosima ve�e-manje
					    		Set<Integer> visitedMax = new HashSet<Integer>();
								setMaxPossibility(numCell, visitedMax);
						    	// Ukloni mogu�nosti prema odnosima manje-ve�e
					    		Set<Integer> visitedMin = new HashSet<Integer>();
								setMinPossibility(numCell, visitedMin);
					    		// Ako nismo postavili sve �elije, pozivamo sekvencu metoda za rje�avanje, ako ona uspije sve rje�iti, prekidamo rje�avanje
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
	// Tra�imo skriveni skup u svim retcima, stupcima i kutijama
	public int hiddenSet() {
	    for (int row = 0; row < rows; row++){
	    	// Pamtimo znakovni niz 0 i 1 ovisno o tome mo�e li se znamenka nalaziti u odre�enom stupcu unutar definiranog reda
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
	    	// Tra�imo skriveni skup koji sadr�i ishodi�nu vrijednost unutar definiranog reda
			for (int val = 0; val < cols; val++) {
				Set<Integer> sameRowValues = new HashSet<Integer>();
				if (hiddenSetForRow(val, row, sameRowValues) == 1) {
		    		return 1;
				}
			}
		}
	    for (int col = 0; col < cols; col++){
	    	// Pamtimo znakovni niz 0 i 1 ovisno o tome mo�e li se znamenka nalaziti u odre�enom redu unutar definiranog stupca
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
	    	// Tra�imo skriveni skup koji sadr�i ishodi�nu vrijednost unutar definiranog stupca
			for (int val = 0; val < cols; val++) {
				Set<Integer> sameColValues = new HashSet<Integer>();
				if (hiddenSetForCol(val, col, sameColValues) == 1) {
    	    		return 1;
				}
			}
		}
	    for (int box = 0; box < cols; box++){
	    	// Pamtimo znakovni niz 0 i 1 ovisno o tome mo�e li se znamenka nalaziti u odre�enoj �eliji unutar definirane kutije
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
	    	// Tra�imo skriveni skup koji sadr�i ishodi�nu vrijednost unutar definirane kutije
			for (int val = 0; val < cols; val++) {
				Set<Integer> sameBoxValues = new HashSet<Integer>();
				if (hiddenSetForBox(val, box, sameBoxValues) == 1) {
    	    		return 1;
				}
			}
	    }
		return 0;
	}
	// Broj kori�tenja za metodu linija kandidata
    int clt = 0;
    //Tra�imo linije kandidata u kutiji
	public int candidateLines() {
		// Pretra�ujemo sve kutije
	    for (int box = 0; box < cols; box++){
	    	// Za svaku vrijednost zapisujemo u kojem se redu mo�e nalaziti unutar kutije
	    	int valueRow[] = new int[cols];
	    	// Za svaku vrijednost zapisujemo u kojem se stupcu nalazi nalaziti unutar kutije
	    	int valueCol[] = new int[cols];
	    	// Na po�etku se vriejdnost ne nalazi nigdje unutar kutije (oznaka -1)
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
    	    		// Zapisujemo u kojem redu se nalazi prva pojava neke vrijednosti unutar kutije
    	    		if (valueRow[val] == -1) {
    	    			valueRow[val] = numCell / cols;
    	    		} else {
    	    			// Ako nova pona�ena pojava vrijednosti unutar kutije nije u istom redu, nemamo liniju kandidata u tom redu u toj kutiji za tu vrijednost (oznaka -2)
    	    			if (valueRow[val] != numCell / cols) {
	    	    			valueRow[val] = -2;
    	    			}
    	    		}
    	    		// Zapisujemo u kojem stupcu se nalazi prva pojava neke vrijednosti unutar kutije
    	    		if (valueCol[val] == -1) {
    	    			valueCol[val] = numCell % cols;
    	    		} else {
	    				// Ako nova pona�ena pojava vrijednosti unutar kutije nije u istom stupcu, nemamo liniju kandidata u tom stupcu u toj kutiji za tu vrijednost (oznaka -2)
    	    			if (valueCol[val] != numCell % cols) {
    	    				valueCol[val] = -2;
    	    			}
    	    		}
    	    	}
	    	}
	    	for (int val = 0; val < cols; val ++) {
	    		// Ako su sve pojave neke vrijednosti unutar kutije u istom redu, imamo liniju kandidata u tom redu u toj kutiji
	    		if (valueRow[val] != -1 && valueRow[val] != -2) {
	    			int numRemoved = 0;
	    			for (int col = 0; col < cols; col++) {
	    				// Ako se vrijednost za koju imamo liniju kandidata u redu u nekoj kutiji pojavi u drugoj kutiji u tom istom redu, mo�e mo�emo ukloniti tu mogu�nost
	    				if (possibilities[valueRow[val] * cols + col][val] == 1 && boxNumber[valueRow[val] * cols + col] != box) {
		    				// Ista uo�ena linija kandidata boduje se samo jednom, iako rezultira uklanjanjem vi�e mogu�nosti
	    					if (numRemoved == 0) {
	    			    		String lineSolvInstr = "Kandidati za " + String.valueOf(val + 1) + " u kutiji " + String.valueOf(box + 1) + " svi u redu " + String.valueOf(valueRow[val] + 1) + ".\n";
	    			    		// Ako smo ve� registrirali ovu istu liniju (red) kandidata u kutiji, ne bodujemo ju dvaput
	    			    		if (!solvingInstructions.contains(lineSolvInstr)) {
    				    			if (clt == 0) {
    				    				// Kada prvi put na�emo liniju kandidata, tro�ak je 350
    	    		    				difficultyScore += 350;
    	    		    				clt = 1;
    	    		    			} else {
    				    				// Kada idu�i put na�emo liniju kandidata, tro�ak je 200
    	    		    				difficultyScore += 200;
    	    		    			}		
    				    			// Dodajemo novi red u tekst uputa
    				    			solvingInstructions += lineSolvInstr;
    				    			// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
							    	if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
						    		    print();
				    	    			if (!InformationBox.stepBox(lineSolvInstr, "Rje�ava�")) {
				    	    				showSteps = false;
				    	    			}
						    		}
    				    		}
	    					}		
	    					// Dodajemo novi red za uklanjanje mogu�nosti u tekst uputa
	    					solvingInstructions +=  "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(valueRow[val] + 1) + ", " + String.valueOf(col + 1) + ").\n";
	    					// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
	    					if (showSteps == true) {
				    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			if (!InformationBox.stepBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(valueRow[val] + 1) + ", " + String.valueOf(col + 1) + ").", "Rje�ava�")) {
		    	    				showSteps = false;
		    	    			}
				    		}
		    				possibilities[valueRow[val] * cols + col][val] = 0;
					    	// Ukloni mogu�nosti prema odnosima ve�e-manje
				    		Set<Integer> visitedMax = new HashSet<Integer>();
							setMaxPossibility(valueRow[val] * cols + col, visitedMax);
					    	// Ukloni mogu�nosti prema odnosima manje-ve�e
				    		Set<Integer> visitedMin = new HashSet<Integer>();
							setMinPossibility(valueRow[val] * cols + col, visitedMin);
				    		// Ako nismo postavili sve �elije, pozivamo sekvencu metoda za rje�avanje, ako ona uspije sve rje�iti, prekidamo rje�avanje
				    		if (sequence() == 1) {
		    	    			return 1;
		    				}
		    				numRemoved++;
						}
	    			}
	    		}
	    		// Ako su sve pojave neke vrijednosti unutar kutije u istom stupcu, imamo liniju kandidata u tom stupcu u toj kutiji
	    		if (valueCol[val] != -1 && valueCol[val] != -2) {
	    			int numRemoved = 0;
	    			for (int row = 0; row < rows; row++) {
	    				// Ako se vrijednost za koju imamo liniju kandidata u stupcu u nekoj kutiji pojavi u drugoj kutiji u tom istom stupcu, mo�e mo�emo ukloniti tu mogu�nost
	    				if (possibilities[row * cols + valueCol[val]][val] == 1 && boxNumber[row * cols + valueCol[val]] != box) {
		    				// Ista uo�ena linija kandidata boduje se samo jednom, iako rezultira uklanjanjem vi�e mogu�nosti
    	    				if (numRemoved == 0) {
    				    		String lineSolvInstr = "Kandidati za " + String.valueOf(val + 1) + " u kutiji " + String.valueOf(box + 1) + " svi su u stupcu " + String.valueOf(valueCol[val] + 1) + ".\n";
    				    		// Ako smo ve� registrirali ovu istu liniju (stupac) kandidata u kutiji, ne bodujemo ju dvaput
    				    		if (!solvingInstructions.contains(lineSolvInstr)) {
    				    			if (clt == 0) {
    				    				// Kada prvi put na�emo liniju kandidata, tro�ak je 350
    	    		    				difficultyScore += 350;
    	    		    				clt = 1;
    	    		    			} else {
    				    				// Kada idu�i put na�emo liniju kandidata, tro�ak je 200
    	    		    				difficultyScore += 200;
    	    		    			}		
    				    			// Dodajemo novi red u tekst uputa
    				    			solvingInstructions += lineSolvInstr;
    				    			// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
							    	if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
						    		    print();
				    	    			if (!InformationBox.stepBox(lineSolvInstr, "Rje�ava�")) {
				    	    				showSteps = false;
				    	    			}
						    		}
    				    		}
    	    				}		
    	    				// Dodajemo novi red za uklanjanje mogu�nosti u tekst uputa
	    					solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(valueCol[val] + 1) + ").\n";
	    					// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
	    					if (showSteps == true) {
				    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			if (!InformationBox.stepBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(valueCol[val] + 1) + ").", "Rje�ava�")) {
		    	    				showSteps = false;
		    	    			}
				    		}
		    				possibilities[row * cols + valueCol[val]][val] = 0;
					    	// Ukloni mogu�nosti prema odnosima ve�e-manje
				    		Set<Integer> visitedMax = new HashSet<Integer>();
							setMaxPossibility(row * cols + valueCol[val], visitedMax);
					    	// Ukloni mogu�nosti prema odnosima manje-ve�e
				    		Set<Integer> visitedMin = new HashSet<Integer>();
							setMinPossibility(row * cols + valueCol[val], visitedMin);
				    		// Ako nismo postavili sve �elije, pozivamo sekvencu metoda za rje�avanje, ako ona uspije sve rje�iti, prekidamo rje�avanje
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
	// Oznaka jesmo li ograni�eni na dvosturke parove ili tra�imo skup vi�e linija kandidata bilo koje veli�ine
	boolean widthLimit;
	// Broj kori�tenja za metodu vi�e linija kandidata
	int mlt = 0;
	// Broj kori�tenja za metodu dvostrukih parova
	int dpt = 0;
	// Tra�imo vi�e linija kandidata kroz vi�e kutija
	public int multipleLines() {
		// Pamtimo mo�e li u odre�enoj kutiji odre�ena vrijednost biti u odre�enom redu
    	Integer valueRow[][][] = new Integer[cols][cols][rows];
		// Pamtimo mo�e li u odre�enoj kutiji odre�ena vrijednost biti u odre�enom stupcu
    	Integer valueCol[][][] = new Integer[cols][cols][cols];
		// Znakovni niz 0 i 1 koji predstavlja mo�e li u odre�enoj kutiji odre�ena vrijednost biti u odre�enom redu
    	String valueRowString[][] = new String[cols][cols];
		// Znakovni niz 0 i 1 koji predstavlja mo�e li u odre�enoj kutiji odre�ena vrijednost biti u odre�enom stupcu
    	String valueColString[][] = new String[cols][cols];
		// Pamtimo za svaku kutiju u koliko razli�itih redova se mo�e nalaziti odre�ena vrijednost
    	Integer valueRowNum[][] = new Integer[cols][cols];
		// Pamtimo za svaku kutiju u koliko razli�itih stupaca se mo�e nalaziti odre�ena vrijednost
    	Integer valueColNum[][] = new Integer[cols][cols];
		// Pamtimo koliko �elija kandidata ima u odre�enoj kutiji za odre�enu vrijednost
    	Integer valueCandidates[][] = new Integer[cols][cols];
    	// Prolazimo sve kutije
	    for (int numBox = 0; numBox < cols; numBox++){
	    	// Inicijaliziramo prije definirana polja
	    	for (int val = 0; val < cols; val ++) {
	    		valueRowString[numBox][val] = "";
	    		valueColString[numBox][val] = "";
	    		valueRowNum[numBox][val] = 0;
	    		valueColNum[numBox][val] = 0;
	    		valueCandidates[numBox][val] = 0;
		    	for (int col = 0; col < cols; col ++) {
		    		valueRow[numBox][val][col] = 0;
		    		valueCol[numBox][val][col] = 0;
		    	}
		    }
	    	// A�uriramo saznanja mo�e li u odre�enoj kutiji odre�ena vrijednost biti u odre�enom redu ili stupcu
	    	// Istodobno prebrojavamo koliko �elija kandidata ima u odre�enoj kutiji za odre�enu vrijednost
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
    		    		valueCol[box][val][numCell % cols] = 1;
    		    		valueCandidates[box][val]++;
    	    		}
    	    	}
	    	}
	    }
	    // Pretvaramo informacije mo�e li u odre�enoj kutiji odre�ena vrijednost biti u odre�enom redu ili u odre�enom stupcu iz polja u znakovni niz 0 i 1
	    // Istodobno za svaku kutiju prebrojavamo u koliko razli�itih redova ili stupaca se mo�e nalaziti odre�ena vrijednost
    	for (int numBox = 0; numBox < cols; numBox++) {
	    	for (int val = 0; val < cols; val ++) {
	    	    for (int col = 0; col < cols; col++){
	    	    	if (valueRow[numBox][val][col] == 1) {
	    	    		valueRowString[numBox][val] += "1";
    		    		valueRowNum[numBox][val]++;
	    	    	} else {
	    	    		valueRowString[numBox][val] += "0";
	    	    	}
	    	    	if (valueCol[numBox][val][col] == 1) {
	    	    		valueColString[numBox][val] += "1";
    		    		valueColNum[numBox][val]++;
	    	    	} else {
	    	    		valueColString[numBox][val] += "0";
	    	    	}
	    	    }
	    	}
    	}
    	// Nalazimo po�etnu kutiju
    	for (int firstBox = 0; firstBox < cols; firstBox++) {
    		// Prolazimo sve vrijednosti u po�etnoj kutiji
    		for (int val = 0; val < cols; val++){
    			// Pamtimo sve kutije koje se podudaraju s po�etnom u redovima kandidatima za odre�enu vrijednost
				Set<Integer> sameRowBoxes = new HashSet<Integer>();
    			// Pamtimo sve kutije koje se podudaraju s po�etnom u stupcima kandidatima za odre�enu vrijednost
				Set<Integer> sameColBoxes = new HashSet<Integer>();
				// Prolazimo sve kutije ve�eg broja od po�etne da izbjegnemo ponavljanje
		    	for (int matchingBox = firstBox + 1; matchingBox < cols; matchingBox ++) {
		    		// Kutije se moraju podudarati u redovima kandidatima za odre�enu vrijednost i broj kandidata u svakoj od kutija je dva za dvostruki par i ve�i od dva za vi�e linija kandidata
			    	if (valueRowString[firstBox][val].compareTo(valueRowString[matchingBox][val]) == 0 && matchingBox != firstBox && ((widthLimit && valueCandidates[firstBox][val] == 2 && valueCandidates[matchingBox][val] == 2) || (!widthLimit && (valueCandidates[firstBox][val] > 2 || valueCandidates[matchingBox][val] > 2)))) {
						sameRowBoxes.add(matchingBox);
			    	}
		    		// Kutije se moraju podudarati u stupcima kandidatima za odre�enu vrijednost i broj kandidata u svakoj od kutija je dva za dvostruki par i ve�i od dva za vi�e linija kandidata
			    	if (valueColString[firstBox][val].compareTo(valueColString[matchingBox][val]) == 0 && matchingBox != firstBox && ((widthLimit && valueCandidates[firstBox][val] == 2 && valueCandidates[matchingBox][val] == 2) || (!widthLimit && (valueCandidates[firstBox][val] > 2 || valueCandidates[matchingBox][val] > 2)))) {
						sameColBoxes.add(matchingBox);
			    	}
		    	}
		    	// Ako je broj kutija koje se podudaraju u redovima kandidatima za odre�enu vrijednost jednak broju redova kandidata, na�li smo dvostruki par ili vi�e linija kandidata
		    	if (sameRowBoxes.size() == valueRowNum[firstBox][val] - 1 && sameRowBoxes.size() > 0) {
		    		int numRemoved = 0;
					for (int row = 0; row < rows; row++) {
						if (valueRow[firstBox][val][row] == 1) {
							for (int col = 0; col < cols; col++) {
								// Ako kutija izvan onih koje se podudaraju u redovima kandidatima za odre�enu vrijednost sadr�i tu vrijednost u nekom od tih redova, mo�emo ukloniti tu mogu�nost
								if (boxNumber[row * cols + col] != firstBox && !sameRowBoxes.contains(boxNumber[row * cols + col]) && possibilities[row * cols + col][val] == 1) {
									// Isti uo�eni dvostruki par ili vi�e linija kandidata boduje se samo jednom, iako rezultira uklanjanjem vi�e mogu�nosti
									if (numRemoved == 0) {
										String lineSolvInstr = "";
										if (widthLimit) {
											// Ako smo ograni�ili broj kandidata u liniji, tra�imo dvostruki par
											lineSolvInstr = "Dvostruki par u redovima";
										} else {
											// Ako je broj kandidata u liniji neograni�en, tra�imo vi�e linija kandidata
											lineSolvInstr = "Skup redova";
										}
										// Dodajemo redove kandidate u liniju ispisa
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
							    	   // Dodajemo vrijednost koju sadr�e linije kandidati u liniju ispisa
							    	   lineSolvInstr += " sadr�i " + String.valueOf(val + 1) + " u kutijama";
							    	   // Dodajemo kutije kroz koje prolaze linije kandidati u liniju ispisa
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
										// Ako smo ve� registrirali ovaj isti dvostruki par ili vi�e linija kandidata, ne bodujemo ih dvaput
							    		if (!solvingInstructions.contains(lineSolvInstr)) {
											if (widthLimit) {
								    			if (dpt == 0) {
			    				    				// Kada prvi put na�emo dvostruki par, tro�ak je 500
								    				difficultyScore += 500;
								    				dpt = 1;
								    			} else {
			    				    				// Kada idu�i put na�emo dvostruki par, tro�ak je 250
								    				difficultyScore += 250;
								    			}

											} else {
								    			if (mlt == 0) {
			    				    				// Kada prvi put na�emo vi�e linija kandidata, tro�ak je 700
								    				difficultyScore += 700;
								    				mlt = 1;
								    			} else {
			    				    				// Kada idu�i put na�emo vi�e linija kandidata, tro�ak je 400
								    				difficultyScore += 400;
								    			}
											}
							    			// Dodajemo novi red u tekst uputa
		    				    			solvingInstructions += lineSolvInstr;
		    				    			// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
									    	if (showSteps == true) {
								    		    instructionArea.setText(solvingInstructions);
								    		    print();
								    		    if (!InformationBox.stepBox(lineSolvInstr, "Rje�ava�")) {
								    				showSteps = false;
								    			}
								    		}
		    				    		}
									}
									// Dodajemo novi red za uklanjanje mogu�nosti u tekst uputa
			    					solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
			    					// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
			    					if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    		    		print();
				    	    			if (!InformationBox.stepBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").", "Rje�ava�")) {
				    	    				showSteps = false;
				    	    			}
						    		}
									possibilities[row * cols + col][val] = 0;
							    	// Ukloni mogu�nosti prema odnosima ve�e-manje
						    		Set<Integer> visitedMax = new HashSet<Integer>();
									setMaxPossibility(row * cols + col, visitedMax);
							    	// Ukloni mogu�nosti prema odnosima manje-ve�e
						    		Set<Integer> visitedMin = new HashSet<Integer>();
									setMinPossibility(row * cols + col, visitedMin);
						    		numRemoved++;
						    		// Ako nismo postavili sve �elije, pozivamo sekvencu metoda za rje�avanje, ako ona uspije sve rje�iti, prekidamo rje�avanje
				    				if (sequence() == 1) {
				    	    			return 1;
				    				}
								}
							}
						}
					}
		    	}		    	
		    	// Ako je broj kutija koje se podudaraju u stupcima kandidatima za odre�enu vrijednost jednak broju stupaca kandidata, na�li smo dvostruki par ili vi�e linija kandidata
		    	if (sameColBoxes.size() == valueColNum[firstBox][val] - 1 && sameColBoxes.size() > 0) {
		    		int numRemoved = 0;
					for (int col = 0; col < cols; col++) {
						if (valueCol[firstBox][val][col] == 1) {
							for (int row = 0; row < rows; row++) {
								// Ako kutija izvan onih koje se podudaraju u stupcima kandidatima za odre�enu vrijednost sadr�i tu vrijednost u nekom od tih stupaca, mo�emo ukloniti tu mogu�nost
								if (boxNumber[row * cols + col] != firstBox && !sameColBoxes.contains(boxNumber[row * cols + col]) && possibilities[row * cols + col][val] == 1) {
									// Isti uo�eni dvostruki par ili vi�e linija kandidata boduje se samo jednom, iako rezultira uklanjanjem vi�e mogu�nosti
									if (numRemoved == 0) {
							    		String lineSolvInstr;
										if (widthLimit) {
											// Ako smo ograni�ili broj kandidata u liniji, tra�imo dvostruki par
											lineSolvInstr = "Dvostruki par u stupcima";
										} else {
											// Ako je broj kandidata u liniji neograni�en, tra�imo vi�e linija kandidata
											lineSolvInstr = "Skup stupaca";
										}
										// Dodajemo stupce kandidate u liniju ispisa
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
								    	// Dodajemo vrijednost koju sadr�e linije kandidati u liniju ispisa
							    		lineSolvInstr += " sadr�i " + String.valueOf(val + 1) + " u kutijama";
								        // Dodajemo kutije kroz koje prolaze linije kandidati u liniju ispisa
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
										// Ako smo ve� registrirali ovaj isti dvostruki par ili vi�e linija kandidata, ne bodujemo ih dvaput
							    		if (!solvingInstructions.contains(lineSolvInstr)) {
											if (widthLimit) {
								    			if (dpt == 0) {
			    				    				// Kada prvi put na�emo dvostruki par, tro�ak je 500
								    				difficultyScore += 500;
								    				dpt = 1;
								    			} else {
			    				    				// Kada idu�i put na�emo dvostruki par, tro�ak je 250
								    				difficultyScore += 250;
								    			}

											} else {
								    			if (mlt == 0) {
			    				    				// Kada prvi put na�emo vi�e linija kandidata, tro�ak je 700
								    				difficultyScore += 700;
								    				mlt = 1;
								    			} else {
			    				    				// Kada idu�i put na�emo vi�e linija kandidata, tro�ak je 400
								    				difficultyScore += 400;
								    			}
											}
							    			// Dodajemo novi red u tekst uputa
							    			solvingInstructions += lineSolvInstr;
							    			// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
									    	if (showSteps == true) {
								    		    instructionArea.setText(solvingInstructions);
								    		    print();
								    		    if (!InformationBox.stepBox(lineSolvInstr, "Rje�ava�")) {
								    				showSteps = false;
								    			}
								    		}
		    				    		}
						    		}
									// Dodajemo novi red za uklanjanje mogu�nosti u tekst uputa
			    					solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
			    					// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
			    					if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    		    		print();
				    	    			if (!InformationBox.stepBox("Uklanjam mogu�nost " + String.valueOf(val + 1) + " iz �elije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").", "Rje�ava�")) {
				    	    				showSteps = false;
				    	    			}
			    					}
									possibilities[row * cols + col][val] = 0;
							    	// Ukloni mogu�nosti prema odnosima ve�e-manje
						    		Set<Integer> visitedMax = new HashSet<Integer>();
									setMaxPossibility(row * cols + col, visitedMax);
							    	// Ukloni mogu�nosti prema odnosima manje-ve�e
						    		Set<Integer> visitedMin = new HashSet<Integer>();
									setMinPossibility(row * cols + col, visitedMin);
						    		numRemoved++;
						    		// Ako nismo postavili sve �elije, pozivamo sekvencu metoda za rje�avanje, ako ona uspije sve rje�iti, prekidamo rje�avanje
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
	// Provjeravamo je li neka �elija jedina mogu�a pozicija za odre�enu vrijednost u redu, stupcu ili dijagonali
	public int singlePosition() {
		for (int val = 1; val <= rows; val++) {
			// Pratimo rede u kojima je iskori�tena odre�ena vrijednost
			int[] usedRows = new int[rows];
			// Pratimo stupce u kojima je iskori�tena odre�ena vrijednost
			int[] usedCols = new int[cols];
			// Pratimo kutije u kojima je iskori�tena odre�ena vrijednost
			int[] usedBoxes = new int[rows * cols];
			// Pratimo je li iskori�tena odre�ena vrijednost u rastu�oj dijagonali
			boolean usedFirstDiagonal = false;
			// Pratimo je li iskori�tena odre�ena vrijednost u padaju�oj dijagonali
			boolean usedSecondDiagonal = false;
		    for (int row = 0; row < rows; row++){
		    	for (int col = 0; col < cols; col++) {
		    		usedRows[row] = 0;
		    		usedCols[col] = 0;
		    		usedBoxes[row] = 0;
			    }
		    }
			// A�uriramo redove, stupce, kutije i dijagonale u kojima je iskori�tena neka vrijednost
		    for (int row = 0; row < rows; row++){
		    	for (int col = 0; col < cols; col++) {
		    		if (temporary[row * cols + col] == val) {
			    		usedRows[row]++;
			    		usedCols[col]++;
			    		usedBoxes[boxNumber[row * cols + col]]++;
			    		if (row == col && diagonalOn) {
			    			usedFirstDiagonal = true;
			    		}
			    		if (row == cols - 1 - col && diagonalOn) {
			    			usedSecondDiagonal = true;
			    		}
		    		}
			    }
		    }
			// Postavljamo vrijednost u odre�eni redak
		    for (int row = 0; row < rows; row++){ 
				// Ako je vrijednost ve� postavljena u redak, nastavljamo dalje
		    	if (usedRows[row] == 1) {
		    		continue;
		    	}
		    	int possible = 0;
		    	int colToClear = 0;
				// Pratimo stupce u koje je mogu�e postaviti vrijednost unutar reda
		    	for (int col = 0; col < cols; col++) {
		    		if (row == col && diagonalOn && usedFirstDiagonal) {
		    			continue;
		    		}
		    		if (row == cols - 1 - col && diagonalOn && usedSecondDiagonal) {
		    			continue;
		    		}
		    		int box = boxNumber[row * cols + col];
		    		if (usedRows[row] == 0 && usedCols[col] == 0 && usedBoxes[box] == 0 && possibilities[row * cols + col][val - 1] != 0 && temporary[row * cols + col] == 0) {
		    			possible++;
		    			colToClear = col;
		    		}
			    }
				// Ako je vrijednost unutar reda mogu�e postaviti samo na jednu poziciju, mo�emo postaviti vrijednost
		    	if (possible == 1) {
		    		// Kori�tenje jedine mogu�e vrijednosti u redu ima tro�ak 100
		    		difficultyScore += 100;
		    		unset--;
					// �istimo mogu�nosti postavljene �elije
			    	for (int valPossible = 0; valPossible < cols; valPossible++) {
			    		possibilities[row * cols + colToClear][valPossible] = 0;
				    }
			    	possibilities[row * cols + colToClear][val - 1] = 1;
					// A�uriramo redove, stupce, kutije i dijagonale u kojima je iskori�tena neka vrijednost
			    	usedRows[row] = 1;
			    	usedCols[colToClear] = 1;
			    	int b = boxNumber[row * cols + colToClear];
			    	usedBoxes[b] = 1;
		    		if (row == colToClear && diagonalOn) {
		    			usedFirstDiagonal = true;
		    		}
		    		if (row == cols - 1 - colToClear && diagonalOn) {
		    			usedSecondDiagonal = true;
		    		}	
	    			// Dodajemo novi red u tekst uputa
	    			solvingInstructions += "Za red " + String.valueOf(row + 1) + ", broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(row + 1) + ", " + String.valueOf(colToClear + 1) + ").\n";
	    			// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
	    			if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			if (!InformationBox.stepBox("Za red " + String.valueOf(row + 1) + ", broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(row + 1) + ", " + String.valueOf(colToClear + 1) + ").", "Rje�ava�")) {
    	    				showSteps = false;
    	    			}
		    		
	    			}
			    	temporary[row * cols + colToClear] = val;
		    		if (val < 10) {
		    			field[row * cols + colToClear].setText(String.valueOf(val));
		    		} else {
		    			char c = 'A';
		    			c += val - 10;
		    			field[row * cols + colToClear].setText("" + c);
		    		}
		    		// A�uriramo mogu�e vrijednosti ostalih �elija
		    		fixPencilmarks();
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
			// Postavljamo vrijednost u odre�eni stupac
		    for (int col = 0; col < cols; col++){ 
				// Ako je vrijednost ve� postavljena u stupac, nastavljamo dalje
		    	if (usedCols[col] == 1) {
		    		continue;
		    	}
		    	int possible = 0;
		    	int rowToClear = 0;
				// Pratimo redove u koje je mogu�e postaviti vrijednost unutar stupca
		    	for (int row = 0; row < rows; row++) {
		    		if (row == col && diagonalOn && usedFirstDiagonal) {
		    			continue;
		    		}
		    		if (row == cols - 1 - col && diagonalOn && usedSecondDiagonal) {
		    			continue;
		    		}
		    		int box = boxNumber[row * cols + col];
		    		if (usedRows[row] == 0 && usedCols[col] == 0 && usedBoxes[box] == 0 && possibilities[row * cols + col][val - 1] != 0 && temporary[row * cols + col] == 0) {
		    			possible++;
		    			rowToClear = row;
		    		}
			    }
				// Ako je vrijednost unutar stupca mogu�e postaviti samo na jednu poziciju, mo�emo postaviti vrijednost
		    	if (possible == 1) {
		    		// Kori�tenje jedine mogu�e vrijednosti u stupcu ima tro�ak 100
		    		difficultyScore += 100;
		    		unset--;
					// �istimo mogu�nosti postavljene �elije
			    	for (int valPossible = 0; valPossible < cols; valPossible++) {
			    		possibilities[rowToClear * cols + col][valPossible] = 0;
				    }
			    	possibilities[rowToClear * cols + col][val - 1] = 1;
					// A�uriramo redove, stupce, kutije i dijagonale u kojima je iskori�tena neka vrijednost
			    	usedRows[rowToClear] = 1;
			    	usedCols[col] = 1;
			    	int boxToClear = boxNumber[rowToClear * cols + col];
			    	usedBoxes[boxToClear] = 1;
		    		if (rowToClear == col && diagonalOn) {
		    			usedFirstDiagonal = true;
		    		}
		    		if (rowToClear == cols - 1 - col && diagonalOn) {
		    			usedSecondDiagonal = true;
		    		}
		    		// Dodajemo novi red u tekst uputa
	    			solvingInstructions += "Za stupac " + String.valueOf(col + 1) + ", broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(rowToClear + 1) + ", " + String.valueOf(col + 1) + ").\n";
	    			// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
	    			if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			if (!InformationBox.stepBox("Za stupac " + String.valueOf(col + 1) + ", broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(rowToClear + 1) + ", " + String.valueOf(col + 1) + ").", "Rje�ava�")) {
    	    				showSteps = false;
    	    			}
		    		}
	    			temporary[rowToClear * cols + col] = val;
		    		if (val < 10) {
		    			field[rowToClear * cols + col].setText(String.valueOf(val));
		    		} else {
		    			char c = 'A';
		    			c += val - 10;
		    			field[rowToClear * cols + col].setText("" + c);
		    		}
		    		// A�uriramo mogu�e vrijednosti ostalih �elija
			    	fixPencilmarks();
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
			// Postavljamo vrijednost u odre�enu kutiju
		    for (int box = 0; box < cols; box++){ 
				// Ako je vrijednost ve� postavljena u kutiju, nastavljamo dalje
		    	if (usedBoxes[box] == 1) {
		    		continue;
		    	}
		    	int possible = 0;
		    	int cellToClear = 0;
				// Pratimo �elije u koje je mogu�e postaviti vrijednost unutar kutije
		    	for (int numCell = 0; numCell < rows * cols; numCell++) {
		    		if (numCell / cols == numCell % cols && diagonalOn && usedFirstDiagonal) {
		    			continue;
		    		}
		    		if (numCell / cols == cols - 1 - numCell % cols && diagonalOn && usedSecondDiagonal) {
		    			continue;
		    		}
		    		int newBox = boxNumber[numCell];
		    		if (newBox != box) {
		    			continue;
		    		}
		    		if (usedRows[numCell / cols] == 0 && usedCols[numCell % cols] == 0 && usedBoxes[newBox] == 0 && possibilities[numCell][val - 1] != 0 && temporary[numCell] == 0) {
		    			possible++;
		    			cellToClear = numCell;
		    		}
			    }
				// Ako je vrijednost unutar kutije mogu�e postaviti samo na jednu poziciju, mo�emo postaviti vrijednost
		    	if (possible == 1) {
		    		// Kori�tenje jedine mogu�e vrijednosti u kutiji ima tro�ak 100
		    		difficultyScore += 100;
		    		unset--;
					// �istimo mogu�nosti postavljene �elije
			    	for (int valPossible = 0; valPossible < cols; valPossible++) {
			    		possibilities[cellToClear][valPossible] = 0;
				    }
			    	possibilities[cellToClear][val - 1] = 1;
					// A�uriramo redove, stupce, kutije i dijagonale u kojima je iskori�tena neka vrijednost
			    	usedRows[cellToClear / cols] = 1;
			    	usedCols[cellToClear % cols] = 1;
		    		if (cellToClear / cols == cellToClear % cols && diagonalOn) {
		    			usedFirstDiagonal = true;
		    		}
		    		if (cellToClear / cols == cols - 1 - cellToClear % cols && diagonalOn) {
		    			usedSecondDiagonal = true;
		    		}
			    	usedBoxes[boxNumber[cellToClear]] = 1;
			    	// Dodajemo novi red u tekst uputa
	    			solvingInstructions += "Za kutiju " + String.valueOf(boxNumber[cellToClear] + 1) + ", broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").\n";
	    			// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
	    			if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			if (!InformationBox.stepBox("Za kutiju " + String.valueOf(boxNumber[cellToClear] + 1) + ", broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").", "Rje�ava�")) {
    	    				showSteps = false;
    	    			}
		    		}
	    			temporary[cellToClear] = val;
		    		if (val < 10) {
		    			field[cellToClear].setText(String.valueOf(val));
		    		} else {
		    			char c = 'A';
		    			c += val - 10;
		    			field[cellToClear].setText("" + c);
		    		}
		    		// A�uriramo mogu�e vrijednosti ostalih �elija
			    	fixPencilmarks();
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
		    // Provjeravamo koristimo li pravilo dijagonala
		    if (diagonalOn) {
		    	// Postavljamo vrijednost u rastu�u dijagonalu
				// Ako je vrijednost ve� postavljena u rastu�u dijagonalu, nastavljamo dalje
		    	if (!usedFirstDiagonal) {
		    		int possible = 0;
		    		int cellToClear = 0;
					// Pratimo �elije u koje je mogu�e postaviti vrijednost unutar rastu�e dijagonale
		    		for (int diagonally = 0; diagonally < cols; diagonally++) {
		    			int numCell = diagonally * cols + diagonally;
			    		if (numCell / cols == numCell % cols && diagonalOn && usedFirstDiagonal) {
			    			continue;
			    		}
			    		if (numCell / cols == cols - 1 - numCell % cols && diagonalOn && usedSecondDiagonal) {
			    			continue;
			    		}
			    		int newBox = boxNumber[numCell];
			    		if (usedRows[numCell / cols] == 0 && usedCols[numCell % cols] == 0 && usedBoxes[newBox] == 0 && possibilities[numCell][val - 1] != 0 && temporary[numCell] == 0) {
			    			possible++;
			    			cellToClear = numCell;
			    		}
		    		}
					// Ako je vrijednost unutar rastu�e dijagonale mogu�e postaviti samo na jednu poziciju, mo�emo postaviti vrijednost
			    	if (possible == 1) {
			    		// Kori�tenje jedine mogu�e vrijednosti u rastu�oj dijagonali ima tro�ak 100
			    		difficultyScore += 100;
			    		unset--;
						// �istimo mogu�nosti postavljene �elije
				    	for (int valPossible = 0; valPossible < cols; valPossible++) {
				    		possibilities[cellToClear][valPossible] = 0;
					    }
				    	possibilities[cellToClear][val - 1] = 1;
						// A�uriramo redove, stupce, kutije i dijagonale u kojima je iskori�tena neka vrijednost
				    	usedRows[cellToClear / cols] = 1;
				    	usedCols[cellToClear % cols] = 1;
			    		if (cellToClear / cols == cellToClear % cols && diagonalOn) {
			    			usedFirstDiagonal = true;
			    		}
			    		if (cellToClear / cols == cols - 1 - cellToClear % cols && diagonalOn) {
			    			usedSecondDiagonal = true;
			    		}
				    	usedBoxes[boxNumber[cellToClear]] = 1;
				    	// Dodajemo novi red u tekst uputa
		    			solvingInstructions += "Za rastu�u dijagonalu broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").\n";
		    			// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
		    			if (showSteps == true) {
			    		    instructionArea.setText(solvingInstructions);
	    		    		print();
	    	    			if (!InformationBox.stepBox("Za rastu�u dijagonalu broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").", "Rje�ava�")) {
	    	    				showSteps = false;
	    	    			}
			    		}
		    			temporary[cellToClear] = val;
			    		if (val < 10) {
			    			field[cellToClear].setText(String.valueOf(val));
			    		} else {
			    			char c = 'A';
			    			c += val - 10;
			    			field[cellToClear].setText("" + c);
			    		}
			    		// A�uriramo mogu�e vrijednosti ostalih �elija
				    	fixPencilmarks();
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
		    	// Postavljamo vrijednost u padaju�u dijagonalu
				// Ako je vrijednost ve� postavljena u padaju�u dijagonalu, nastavljamo dalje
		    	if (!usedSecondDiagonal) {
		    		int possible = 0;
		    		int cellToClear = 0;
					// Pratimo �elije u koje je mogu�e postaviti vrijednost unutar padaju�e dijagonale
		    		for (int diagonally = 0; diagonally < cols; diagonally++) {
		    			int numCell = diagonally * cols + cols - 1 - diagonally;
			    		if (numCell / cols == numCell % cols && diagonalOn && usedFirstDiagonal) {
			    			continue;
			    		}
			    		if (numCell / cols == cols - 1 - numCell % cols && diagonalOn && usedSecondDiagonal) {
			    			continue;
			    		}
			    		int newBox = boxNumber[numCell];
			    		if (usedRows[numCell / cols] == 0 && usedCols[numCell % cols] == 0 && usedBoxes[newBox] == 0 && possibilities[numCell][val - 1] != 0 && temporary[numCell] == 0) {
			    			possible++;
			    			cellToClear = numCell;
			    		}
		    		}
					// Ako je vrijednost unutar padaju�e dijagonale mogu�e postaviti samo na jednu poziciju, mo�emo postaviti vrijednost
			    	if (possible == 1) {
			    		// Kori�tenje jedine mogu�e vrijednosti u padaju�oj dijagonali ima tro�ak 100
			    		difficultyScore += 100;
			    		unset--;
						// �istimo mogu�nosti postavljene �elije
				    	for (int valPossible = 0; valPossible < cols; valPossible++) {
				    		possibilities[cellToClear][valPossible] = 0;
					    }
				    	possibilities[cellToClear][val - 1] = 1;
						// A�uriramo redove, stupce, kutije i dijagonale u kojima je iskori�tena neka vrijednost
				    	usedRows[cellToClear / cols] = 1;
				    	usedCols[cellToClear % cols] = 1;
			    		if (cellToClear / cols == cellToClear % cols && diagonalOn) {
			    			usedFirstDiagonal = true;
			    		}
			    		if (cellToClear / cols == cols - 1 - cellToClear % cols && diagonalOn) {
			    			usedSecondDiagonal = true;
			    		}
				    	usedBoxes[boxNumber[cellToClear]] = 1;
				    	// Dodajemo novi red u tekst uputa
		    			solvingInstructions += "Za padaju�u dijagonalu broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").\n";
		    			// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
		    			if (showSteps == true) {
			    		    instructionArea.setText(solvingInstructions);
	    		    		print();
	    	    			if (!InformationBox.stepBox("Za padaju�u dijagonalu broj " + String.valueOf(val) + " je jedino mogu� u �eliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").", "Rje�ava�")) {
	    	    				showSteps = false;
	    	    			}
			    		}
		    			temporary[cellToClear] = val;
			    		if (val < 10) {
			    			field[cellToClear].setText(String.valueOf(val));
			    		} else {
			    			char c = 'A';
			    			c += val - 10;
			    			field[cellToClear].setText("" + c);
			    		}
			    		// A�uriramo mogu�e vrijednosti ostalih �elija
				    	fixPencilmarks();
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
	// Sekvenca kojom se primjenjuju tehnike, prema slo�enosti (tro�ku)
	public int sequence() {
		int impossible = impossibleCheck();
		if (impossible > 0) {
			return 0;
	    }
    	// Pokre�emo tra�enje jedine poziciju unutar reda, stupca, kutije ili rastu�e ili padaju�e dijagonale
		if (singlePosition() == 1 || unset == 0) {
			return 1;
		}
		// Pokre�emo tra�enje jedino kandidata u �eliji
		if (singleCandidate() == 1 || unset == 0) {
			return 1;
		}
		// Pokre�emo tra�enje linija kandidata (redova ili stupaca) u kutiji
		if (candidateLines() == 1 || unset == 0) {
			return 1;
		}
		// Pokre�emo tra�enje dvostrukih parova u kutijama
		widthLimit = true;
		if (multipleLines() == 1 || unset == 0) {
			return 1;
		}
		// Pokre�emo tra�enje vi�e linija kandidata (redova ili stupaca) u kutijama
		widthLimit = false;
		if (multipleLines() == 1 || unset == 0) {
			return 1;
		}
		depthLimit = 2;
		// Pokre�emo tra�enje ogoljenog para
		if (nakedSet() == 1 || unset == 0) {
			return 1;
		}
		// Pokre�emo tra�enje skrivenog prara
		if (hiddenSet() == 1 || unset == 0) {
			return 1;
		}
		depthLimit = 3;
		// Pokre�emo tra�enje ogoljene trojke
		if (nakedSet() == 1 || unset == 0) {
			return 1;
		}
		// Pokre�emo tra�enje skrivene trojke
		if (hiddenSet() == 1 || unset == 0) {
			return 1;
		}
		Stack<Integer> cell = new Stack<Integer>();
		chainLength = 2;
		// Pokre�emo tra�enje X-krila
		for (int val = 0; val < cols; val++) {
			if (closedChain(cell, -1, 1, 1, val) == 1 || unset == 0) {
				return 1;
			}
			if (closedChain(cell, -1, 1, 0, val) == 1 || unset == 0) {
				return 1;
			}
		}
		// Pokre�emo forsiranje ulan�avanjem
		if (forcingChains() == 1 || unset == 0) {
			return 1;
		} 
		for (int depth = 4; depth < cols; depth++) {
			depthLimit = depth;
			// Pokre�emo tra�enje ogoljenog skupa veli�ine 4 ili ve�eg
			if (nakedSet() == 1 || unset == 0) {
				return 1;
			}
			// Pokre�emo tra�enje skrivenog skupa veli�ine 4 ili ve�eg
			if (hiddenSet() == 1 || unset == 0) {
				return 1;
			}
		}
		chainLength = 3;
		// Pokre�emo tra�enje sabljarke
		for (int val = 0; val < cols; val++) {
			if (closedChain(cell, -1, 1, 1, val) == 1 || unset == 0) {
				return 1;
			}
			if (closedChain(cell, -1, 1, 0, val) == 1 || unset == 0) {
				return 1;
			}
		}
		for (int depth = 4; depth < cols; depth++) {
			chainLength = depth;
			// Pokre�emo tra�enje meduze
			for (int val = 0; val < cols; val++) {
				if (closedChain(cell, -1, 1, 1, val) == 1 || unset == 0) {
					return 1;
				}
				if (closedChain(cell, -1, 1, 0, val) == 1 || unset == 0) {
					return 1;
				}
			}
		}
		return 0;
	}
	public int impossibleCheck() {
		int impossible = 0;
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (temporary[row * cols + col] != 0) {
	    			continue;
	    		}
	    		int possibleVals = 0;
	    		for (int val = 0; val < cols; val++) {
	    			if (possibilities[row * cols + col][val] == 1) {
	    				possibleVals = 1;
	    				break;
	    			}
	    		}
	    		if (possibleVals == 0) {
	    			impossible++;
	    		}
	    	}
	    }
	    return impossible;
	}
	
	long startSolving;
	int[] forceVisited = new int[rows * cols];
	// Provjeravamo rje�ivost zagonetke i a�uriramo upute za rje�avanje i te�inu
	public int isOnlyOneSolution() {
		// Zapisjuemo vrijeme kada smo po�eli rje�avati
		startSolving = System.currentTimeMillis();
		// Postavljamo broj kori�tenja svih tehnika na 0
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
		// �istimo upute za rje�avanje i te�inu
		solvingInstructions = "";
		difficultyScore = 0;
	    boolean correct = checkIfCorrect();
	    // Ako zagonetka nije ispravno zadana, ne mo�emo ju rje�avati
    	if (!correct) {
			difficulty.setText("U zagonetki ima gre�aka");
    		return -1;
    	}
    	// Prebrojavamo prazna polja i kopiramo unos u polja koja nisu prazna
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
    	// Za algoritam forsiranja ulan�avanjem pratimo broj posjeta
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
		    	forceVisited[numCell] = 0;
	    	}
	    }
	    // Inicijaliziramo mogu�nosti za sve vrijednosti u svim �elijama na 1
	    initPencilmarks();
	    // A�uriramo mogu�nosti svih �elija prema pravilima zagonetke
		fixPencilmarks();
		// Sudoku zagonetka 9 * 9 nema jedinstveno rje�enje ako je zadano manje od 17 polja i ako nema dijagonale niti odnosa ve�e-manje
	    if (cols == 9 && rows * cols - unset < 17 && 0 != unset && diagonalOn == false && sizeRelationships.size() == 0) {
			print();
			difficulty.setText(String.valueOf(unset) + " Zadano je premalo polja");
			return 0;
	    }
		if (sequence() == 1 || unset == 0) {
			solvingInstructions += "Sva polja rje�ena.\n";
	    	if (showSteps == true) {
			    instructionArea.setText(solvingInstructions);
			    print();
				if (!InformationBox.stepBox( "Sva polja rje�ena.", "Rje�ava�")) {
					showSteps = false;
				}
			}
			// Ako smo postavili sve �elije bez poga�anja, postoji jedinstveno rje�enje
			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rje�enje");
			print();
			return 1;
		}
		// Ako nismo postavili kona�nu vrijednost u sve �elije, prebrojavamo �elije koje nemaju preostalih mogu�ih vrijednosti
	    int impossible = impossibleCheck();
		if (impossible > 0) {
	    	// Ako neke �elije nemaju preostalih mogu�ih vrijednosti, ne postoji na�in rje�avanja zagonetke i ona nije ispravna
			difficulty.setText(String.valueOf(impossible) + " Ne postoji rje�enje");
			print();
			return 0;
	    }
    	// Ako �elije imaju jednu ili vi�e mogu�ih vrijednosti, ne postoji jedinstveno rje�enje, ve� vi�e njih
		difficulty.setText(String.valueOf(unset) + " Ne postoji jedinstveno rje�enje");
		print();
		return 0;
	}

	// Broj kori�tenja forsiranja ulan�avanjem
	int fct	= 0;
	// Poku�avamo forsirati vrijednosti ulan�avanjem (pra�enjem posljedica svakog izbora kroz mre�u)
	public int forcingChains() {
		// Prije nego krenemo prou�avati ishod ako odaberemo bilo koju od mogu�nosti, moramo zapisati trenutno stanje zbog povratka untrag
		int[] backupTemporary = new int[rows * cols];
		int[][] backupPossibilities = new int[rows * cols][cols];
		int backupUnset = unset;
		int backupDifficultyScore = difficultyScore;
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		backupTemporary[row * cols + col] =  temporary[row * cols + col];
	    		for (int val = 0; val < cols; val++) {
	    			backupPossibilities[row * cols + col][val] = possibilities[row * cols + col][val];
	    		}
			}
		}
	    for (int maxNumPossiblities = 2; maxNumPossiblities <= cols; maxNumPossiblities++) {
		    for (int row = 0; row < rows; row++){
		    	for (int col = 0; col < cols; col++) {
		    		int countPossibilities = 0;
		    		for (int val = 0; val < rows; val++) {
			    		if (possibilities[row * cols + col][val] == 1 && temporary[row * cols + col] == 0) {
			    			countPossibilities++;
			    		}
		    		}
		    		if (forceVisited[row * cols + row] == 1 || countPossibilities != maxNumPossiblities) {
		    			continue;
		    		}
		    		forceVisited[row * cols + row] = 1;
		    		// U polju pamtimo ishode za sve �elije ako za neku od �elija odaberemo odre�enu mogu�nost
		    		int [] forcedValues = new int[rows * cols];
		    		// Pratimo koliko puta smo uspjeli odrediti vrijednost neke �elije
		    		int possibilityNum = 0;
		    		for (int val = 0; val < cols; val++) {
		    			// Za svaku od mogu�nosti u �eliji prou�avamo ishod ako ju odaberemo
			    		if (possibilities[row * cols + col][val] == 1 && temporary[row * cols + col] == 0) {
		    				String lineSolvInstr = "Isprobavam vrijednost " + String.valueOf(val + 1) + " u �eliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").";
	    					solvingInstructions += lineSolvInstr + "\n";
	    					if (showSteps == true) {
	    		    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			if (!InformationBox.stepBox("Isprobavam vrijednost " + String.valueOf(val + 1) + " u �eliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").", "Rje�ava�")) {
		    	    				showSteps = false;
		    	    			}
	    		    		}
							// �istimo mogu�nosti prepostavljene �elije
				    		for (int clearVal = 0; clearVal < cols; clearVal++) {
				    			possibilities[row * cols + col][clearVal] = 0;
				    		}
			    			possibilities[row * cols + col][val] = 1;
					    	unset--;
					    	// Poku�avamo rje�iti zagonetku uz novu pretpostavku
					    	sequence();
		    				lineSolvInstr = "Vra�am unatrag vrijednost " + String.valueOf(val + 1) + " u �eliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").";
	    					solvingInstructions += lineSolvInstr + "\n";
	    					if (showSteps == true) {
	    		    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			if (!InformationBox.stepBox("Vra�am unatrag vrijednost " + String.valueOf(val + 1) + " u �eliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").", "Rje�ava�")) {
		    	    				showSteps = false;
		    	    			}
	    		    		}
							// Nakon rje�avanja s pretpostavkom vra�amo prethodno stanje
						    for (int rowRestore = 0; rowRestore < rows; rowRestore++){
						    	for (int colRestore = 0; colRestore < cols; colRestore++) {
						    		if (possibilityNum == 0) {
						    			// Ako smo pomo�u pretpostavke po prvi put dobili vrijednost u nekoj �eliji, zapisujemo ju
						    			forcedValues[rowRestore * cols + colRestore] = temporary[rowRestore * cols + colRestore];
						    		} else { 
						    			// Ako smo pomo�u pretpostavki idu�i put dobili vrijednost u nekoj �eliji, zapisujemo ju ako je ista kao prehodna, a zamjenjujemo s 0 ako se razlikuju
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
						    unset = backupUnset;
						    possibilityNum++;
			    		}
				    }
		    		// Prelazimo polje gdje pamtimo zajedni�ke ishode pretpostavki za odre�enu �eliju
				    for (int rowForce = 0; rowForce < rows; rowForce++){
				    	for (int colForce = 0; colForce < cols; colForce++) {
				    		// Ako za bilo koju vrijednost po�etne �elije neka druga �elija ima uvijek istu vrijednost, ka�emo da po�etna �elija forsira njezinu vrijednost
				    		if (forcedValues[rowForce * rows + colForce] != 0 && temporary[rowForce * rows + colForce] == 0) {
			    				String lineSolvInstr = "�elija (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ") forsira vrijednost ";
								// Ako smo ve� forsirali neku vrijednost pomo�u ove iste �elije, ne bodujemo to dvaput
			    				if (!solvingInstructions.contains(lineSolvInstr)) {
			    					if (fct == 0) {
					    				// Kada prvi put koristimo forsiranje ulan�avanjem, tro�ak je 4200
			    						difficultyScore += 4200;
			    						fct = 1;
			    					} else {
					    				// Kada idu�i put koristimo forsiranje ulan�avanjem, tro�ak je 2100
			    						difficultyScore += 2100;
			    					}
			    				}
		    					lineSolvInstr += String.valueOf(forcedValues[rowForce * rows + colForce]) + " u �eliji (" + String.valueOf(rowForce + 1) + ", " + String.valueOf(colForce + 1) + ").\n";
		    					// Dodajemo novi red u tekst uputa
		    					solvingInstructions += lineSolvInstr;
		    					// Ako se prikazuje rje�avanje korak po korak otvaramo novi prozor s uputama
			    				if (showSteps == true) {
		    		    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("�elija (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ") forsira vrijednost " + String.valueOf(forcedValues[rowForce * rows + colForce]) + " u �eliji (" + String.valueOf(rowForce + 1) + ", " + String.valueOf(colForce + 1) + ").", "Rje�ava�")) {
			    	    				showSteps = false;
			    	    			}
		    		    		}
				    			for (int val = 0; val < cols; val++) {
				    				possibilities[rowForce * rows + colForce][val] = 0;
				    			}
			    				possibilities[rowForce * rows + colForce][forcedValues[rowForce * rows + colForce] - 1] = 1;
			    				temporary[rowForce * cols + colForce] = forcedValues[rowForce * rows + colForce];
			    				unset--;
			    				fixPencilmarks();
						    	// Ako smo postavili sve �elije, prekidamo rje�avanje
			    				if (unset == 0) {
			    					return 1;
			    				} 
							    // Ako nismo postavili sve �elije, pokre�emo sekvencu
							    if (sequence() == 1) {
									return 1;
								}
		    		    	}
				    	}
				    }
			    }
			}
	    }
	    return 0;
	}
	// Poku�avamo na�i varijantu rje�enja poga�anjem
	public int guessing() {
		// Prije nego krenemo poga�ati vrijednost u nekoj �eliji, moramo zapisati trenutno stanje zbog povratka untrag
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
		    			// Dodajemo liniju u tekst uputa za rje�avanje
		    			solvingInstructions += "Po�injem poga�ati.\n";
						solvingInstructions += "Poku�avam " + String.valueOf(val + 1) + " u �eliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
						backupSolvingInstructions += "Po�injem poga�ati.\n";
						backupSolvingInstructions += "Poku�avam " + String.valueOf(val + 1) + " u �eliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
				    	temporary[row * cols + col] = val + 1;
						// �istimo mogu�nosti pogo�ene �elije
			    		for (int clearVal = 0; clearVal < cols; clearVal++) {
			    			possibilities[row * cols + col][clearVal] = 0;
			    		}
		    			possibilities[row * cols + col][val] = 1;
				    	unset--;
						if (sequence() == 1) {
							// Ako je zagonetka u potpunosti rje�ena nakon dodavanja pogo�ene vrijednosti, na�li smo jedno mogu�e rje�enje i prekidamo rje�avanje
							return 1;
						} else {
							// Ako nije mogu�e rije�iti zagonetku nakon dodavanja pogo�ene vrijednosti, vra�amo prethodno stanje
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
	    	// Ako niti nakon poga�anja nije mogu�e rje�iti zagonetku, vra�amo prethodno stanje i javljamo da zagonetku nije mogu�e rje�iti 
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
	    	// Ako je poga�anjem na�ena jedna varijanta rje�enja, javljamo da ona postoji 
	    	return 1;
	    }
	}
	// Provjeravamo sadr�e li �elije koje smo na�li u istom redu ili stupcu kao i prethodna �elija u zatvorenom lancu dozvoljene vrijednosti za nastavak lanca
	// cells - skup �elija koje su ve� u zatvorenom skupu, beginCell - po�etna �elija u zatvorenom lancu, direction - tra�imo li nove �elije unaprijed (0) ili unatrag (1)
	// rowOrCols - jesmo li mogu�u �eliju koju �elimo dodati u zatvoreni lanac na�li u istom redu (0) ili u istom stupcu (1) u kojem je zadnja ve� dodana �elija
	// beginVal - zadana vrijednost koja se nalazi kao mogu�nost u svim �elijama zatvorenog lanca i povezuje ih, numCell - broj �elije koju �elimo dodati u zatvoreni lanac
	public void startNextIterationOfClosedChains(Stack<Integer> cells, int beginCell, int direction, int rowOrcols, int beginVal, int numCell) {
		// Ako je �elija ve� u zatvorenom lancu joj je ve� postavljena vrijednost, ili ne sadr�i mogu�u vrijednost koja povezuje lanac, preska�emo ju
		if (cells.contains(numCell) || temporary[numCell] != 0 || possibilities[numCell][beginVal] == 0) {
			return;
		}
		int numPossibilities = 0;
		for (int val = 0; val < cols; val++) {
			if (possibilities[numCell][val] == 1) {
				numPossibilities++;
			}
		}
		// Ako u �eliji mogu biti vi�e od dvije mogu�e vrijednosti, forsiranje vrijednosti u lancu nije mogu�e pa prekidamo izvo�enje
		if (numPossibilities != 2) {
			return;
		}
		Stack<Integer> StackNextIteration = new Stack<Integer>();
		for (int cell = 0; cell < rows * cols; cell++) {
			if (cells.contains(cell)) {
				StackNextIteration.add(cell);
			}
		}
		// Dodajemo �eliju u novi skup �elija za novi poziv rekurzije za potragu za zatvorenim lancem
		StackNextIteration.add(numCell);
		// Ako je lanac prazan, postavljamo njenu poziciju za poziciju po�etne �elije
		if (cells.size() == 0) {
			beginCell = numCell;
		}
		if (rowOrcols == 1) {
			// Ako smo �eliju koju dodajemo na�li u istom redu kao i prethodnu, idu�u �emo tra�iti u istom stupcu u kojem je i �elija koju dodajemo
			closedChain(StackNextIteration, beginCell, direction, 0, beginVal);
		} else {
			// Ako smo �eliju koju dodajemo na�li u istom stupcu kao i prethodnu, idu�u �emo tra�iti u istom redu u kojem je i �elija koju dodajemo
			closedChain(StackNextIteration, beginCell, direction, 1, beginVal);
		}
	}
	// Maksimalna veli�ina prve polovice zatvorenog lanca
	int chainLength;
	// Broj kori�tenja za metodu X-krila
	int xwg = 0;
	// Broj kori�tenja za metodu sabljarke
	int sf4 = 0;
	// Tra�imo zatvorene lance �elija
	// cells - skup �elija koje su ve� u zatvorenom skupu, beginCell - po�etna �elija u zatvorenom lancu, direction - tra�imo li nove �elije unaprijed (0) ili unatrag (1)
	// rowOrCols - jesmo li mogu�u �eliju koju �elimo dodati u zatvoreni lanac na�li u istom redu (0) ili u istom stupcu (1) u kojem je zadnja ve� dodana �elija
	// beginVal - zadana vrijednost koja se nalazi kao mogu�nost u svim �elijama zatvorenog lanca i povezuje ih
	public int closedChain(Stack<Integer> cells, int beginCell, int direction, int rowOrcols, int beginVal) {
		// Ako smo pro�li polovicu zatvorenog lanca, mijenjamo smjer i vra�amo se unatrag
		if (cells.size() == chainLength) {
			direction = 0;
		}
		// Ako smo pro�li cjelovitu duljinu lanca, provjeravamo je li lanac zatvoren
		if (cells.size() == chainLength * 2) {
			// Lanac je zatvoren ako smo zavr�ili u istom redu ili stupcu gdje smo po�eli
			if (cells.peek() / cols != beginCell / cols && cells.peek() % cols != beginCell % cols) {
				return 0;
			}
			// Provjeravamo u kojim redovima i stupacima je rasprostranjen zatvoreni lanac
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
				// Ako �elija koja nije dio zatvorenog lanca, a nalazi se u retku ili stupcu koji je dio zatvorenog lanca, sadr�i vrijednost koja povezuje zatvoreni lanac, uklanjamo tu mogu�nost
				if (cells.contains(cell) || (!usedRows.contains(cell / cols) && !usedCols.contains(cell % cols)) || possibilities[cell][beginVal] == 0) {
					continue;
				}						
				// Isti uo�eni zatvoreni skup boduje se samo jednom, iako rezultira uklanjanjem vi�e mogu�nosti
				if (numRemoved == 0) {
					String lineSolvInstr = "";
					// Zatvoreni lanac koji sadr�i dva reda i dva stupca naziva se X-krilo
					if (chainLength == 2) {
						lineSolvInstr += "X-krilo vrijednosti " + String.valueOf(beginVal + 1) + " u �elijama";
					}
					// Zatvoreni lanac koji sadr�i tri reda i tri stupca naziva se sabljarka
					if (chainLength == 3) {
						lineSolvInstr += "Sabljarka vrijednosti " + String.valueOf(beginVal + 1) + " u �elijama";
					}
					// Zatvoreni lanac koji sadr�i �etiri reda (ili vi�e) i �etiri stupca (ili vi�e) naziva se meduza
					if (chainLength == 4) {
						lineSolvInstr += "Meduza vrijednosti " + String.valueOf(beginVal + 1) + " u �elijama";
					}
					// Dodavanje �elija zatvorenog lanca u upute za rje�avanje
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
					// Ako smo ve� registrirali ovaj isti zatvoreni skup, ne bodujemo ga dvaput
		    		if (!solvingInstructions.contains(lineSolvInstr)) {
						if (chainLength == 2) {
			    			if (xwg == 0) {
			    				// Kada prvi put na�emo X-krilo, tro�ak je 2800
			    				difficultyScore += 2800;
			    				xwg = 1;
			    			} else {
			    				// Kada idu�i put na�emo X-krilo, tro�ak je 1600
			    				difficultyScore += 1600;
			    			} 
						} else {
			    			if (sf4 == 0) {
			    				// Kada prvi put na�emo sabljarku, tro�ak je 8000
			    				difficultyScore += 8000;
			    				sf4 = 1;
			    			} else {
			    				// Kada idu�i put na�emo sabljarku, tro�ak je 6000
			    				difficultyScore += 6000;
			    			} 
			    		}	
		    			// Dodajemo novi red u tekst uputa
						solvingInstructions += lineSolvInstr + ".\n";
		    		} else {
		    			return 0;
		    		}
				}	
				numRemoved++;
				possibilities[cell][beginVal] = 0;
		    	// Ukloni mogu�nosti prema odnosima ve�e-manje
	    		Set<Integer> visitedMax = new HashSet<Integer>();
				setMaxPossibility(cell, visitedMax);
		    	// Ukloni mogu�nosti prema odnosima manje-ve�e
	    		Set<Integer> visitedMin = new HashSet<Integer>();
				setMinPossibility(cell, visitedMin);
				// Dodajemo novi red za uklanjanje mogu�nosti u tekst uputa
				solvingInstructions += "Uklanjam mogu�nost " + String.valueOf(beginVal + 1) + " iz �elije (" + String.valueOf(cell / cols + 1) + ", " + String.valueOf(cell % cols + 1) + ").\n" ;
	    		// Ako nismo postavili sve �elije, pozivamo sekvencu metoda za rje�avanje, ako ona uspije sve rje�iti, prekidamo rje�avanje
				if (sequence() == 1) {
					return 1;
				}
			}
			return 0;
		}
		// Ako skup �elija u zatvorenom lancu nije prazan
	    if (cells.size() > 0) {
	    	// Ako tra�imo �eliju u istom redu kao po�etna
			if (rowOrcols == 0) {
				if (direction == 0) {
					// Pretra�ujemo prethodne �elije u istom redu, imaju manju oznaku stupca
					for (int col = 0; col < cells.peek() % cols; col++){ 
			    		int numCell = cells.peek() / cols * cols + col;
			    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
					}
				} else {
					// Pretra�ujemo naredne �elije u istom redu, imaju ve�u oznaku stupca
					for (int col = cells.peek() % cols + 1; col < cols; col++){ 
			    		int numCell = cells.peek() / cols * cols + col;
			    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
					}
				}
			}
	    	// Ako tra�imo �eliju u istom stupcu kao po�etna
			if (rowOrcols == 1) {
				if (direction == 0) {
					// Pretra�ujemo prethodne �elije u istom stupcu, imaju manju oznaku reda
					for (int row = 0; row < cells.peek() / cols; row++){ 
			    		int numCell = row * cols + cells.peek() % cols;
			    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
					}
				} else {
					// Pretra�ujemo naredne �elije u istom stupcu, imaju ve�u oznaku reda
					for (int row = cells.peek() / cols + 1; row < rows; row++){ 
			    		int numCell = row * cols + cells.peek() % cols;
			    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
					}
				}
			}
	    } else {
	    	// Ako je skup �elija u zatvoren lancu prazan, provjeravamo mo�e li bilo koja �elija u mre�i biti po�etna �elija zatvorenog lanca povezanog zadanom vrijedno��u 
	    	for (int row = 0; row < rows; row++){ 
	    		for (int col = 0; col < cols; col++) {
		    		int numCell = row * cols + col;
		    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
	    		} 
	    	}
	    }
		return 0;
	}
	// Ispisujemo sve kona�ne vrijednosti i mogu�nosti �elija na korisnikom su�elju
		public void print() {
			for (int row = 0; row < rows; row++){ 
		    	for (int col = 0; col < cols; col++) {
			    	int numCell = row * cols + col;
		    		String text = "<html><p style='text-align: center'>";
		    		int numberOptions = 0;
		    		// Zapisujemo sve vrijednosti za �eliju
			    	for (int val = 0; val < cols; val++) {
			    		if (possibilities[row * cols + col][val] == 1 || temporary[row * cols + col] == val + 1) {
			    			numberOptions++;
			    			if (val + 1 < 10) {
			    				text += String.valueOf(val + 1) + " ";
			    			} else {
			    				char c = 'A';
			    				c += val - 9;
			    				text += c + " ";
			    			}
			    		}
				    }
			    	if (numberOptions != 0) {
				    	// Ako imam vi�e mogu�nosti za �eliju zapisujemo ih u HTML oznakama
			    		text = text.substring(0, text.length() - 1) + "</p></html>";
			    	} else {
				    	// Ako nema mogu�nosti za �eliju zapisujemo prazan string
			    		text = "";
			    	}
		    		field[numCell].setText(text);
		    		if (temporary[numCell] == 0) {
			    		// Ako �elija nema definiranu kona�nu vrijednost, moramo je obojati u crveno i smanjiti font
		    			field[numCell].setForeground(Color.RED);
		    			field[numCell].setFont(new Font("Arial", Font.PLAIN, guessFontsize));
		    		} else {
			    		// Ako je definirana kona�na vrijednost �elije, moramo je obojati u zeleno i pove�ati font
		    			field[numCell].setForeground(Color.GREEN);
		    			field[numCell].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
		    		}
		    	}
		    }
		}
	
	// Uklanjamo rotacijski simetri�ni par �elija da bismo pove�ali slo�enost
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
	    // Ako su sve �elije ve� uklonjene, prekidamo postupak
	    if (allEmpty) {
	    	return;
	    }
	    // Nasumi�no odabiremo redak i stupac iz koji uklanjamo 
    	int randomCol = ThreadLocalRandom.current().nextInt(0, cols);
    	int randomRow = ThreadLocalRandom.current().nextInt(0, cols);
    	int original = randomRow * cols + randomCol;
    	// Pronalazimo rotacijski simetri�an par odabrane �elije
    	int symetric = (rows - 1 - randomRow) * cols + (cols - 1 - randomCol);
    	// Ako su obje �elije u par ve� uklonjene, ponavljamo odabir
    	while (temporary[original] == 0 && temporary[symetric] == 0) {
        	randomCol = ThreadLocalRandom.current().nextInt(0, cols);
        	randomRow = ThreadLocalRandom.current().nextInt(0, cols);
        	original = randomRow * cols + randomCol;
        	symetric = (rows - 1 - randomRow) * cols + (cols - 1 - randomCol);
    	}
    	// Na vrh stoga zapisujemo vrijednosti koje su bile zapisane u uklonjenim �elijama
    	lastRemovedPosOriginal.push(original);
    	lastRemovedValOriginal.push(userInput[original]);
    	// A�uriramo broj kori�tenja znamenke koju smo pobrisali
    	numUseDigit[userInput[original]]--;
    	checkIfDigitMaxUsed(userInput[original]);
    	temporary[original] = 0;
		userInput[original] = temporary[original];
	    field[original].setText("0");
    	field[original].setForeground(Color.RED);
    	// Na vrh stoga zapisujemo �elije koje su uklonjene
    	lastRemovedPosSymetric.push(symetric);
    	lastRemovedValSymetric.push(userInput[symetric]);
    	// A�uriramo broj kori�tenja znamenke koju smo pobrisali ako nismo dvaput pobrisali istu �eliju (slu�aj sredi�nje �elije u mre�i neparnih dimenzija)
    	if (original != symetric) {
        	numUseDigit[userInput[symetric]]--;
        	checkIfDigitMaxUsed(userInput[symetric]);
    	}
    	temporary[symetric] = 0;
		userInput[symetric] = temporary[symetric];
	    field[symetric].setText("0");
    	field[symetric].setForeground(Color.RED);
	}

	// Vra�amo rotacijski simetri�ni par �elija da bismo pove�ali rje�ivost
	public boolean restoreLastRemoved() {
		// Ako nismo uklonili niti jedan rotacijski simetri�an par, prekidamo postupak
		if (lastRemovedPosOriginal.isEmpty()) {
			return false;
		}
		// S vrha stoga vra�amo polo�aj uklonjenih �elija i vrijednosti koje su bile zapisane u njima
		if (lastRemovedValOriginal.peek() < 10) {
		    field[lastRemovedPosOriginal.peek()].setText(String.valueOf(lastRemovedValOriginal.peek()));
		} else {
			char c = 'A';
			c += lastRemovedValOriginal.peek() - 10;
			field[lastRemovedPosOriginal.peek()].setText("" + c);
		}
		if (lastRemovedValSymetric.peek() < 10) {
		    field[lastRemovedPosSymetric.peek()].setText(String.valueOf(lastRemovedValSymetric.peek()));
		} else {
			char c = 'A';
			c += lastRemovedValSymetric.peek() - 10;
			field[lastRemovedPosSymetric.peek()].setText("" + c);
		}
		userInput[lastRemovedPosOriginal.peek()] = lastRemovedValOriginal.peek();
    	// A�uriramo broj kori�tenja znamenke koju smo vratili
    	numUseDigit[lastRemovedValOriginal.peek()]++;
    	checkIfDigitMaxUsed(lastRemovedValOriginal.peek());
		userInput[lastRemovedPosSymetric.peek()] = lastRemovedValSymetric.peek();
    	// A�uriramo broj kori�tenja znamenke koju smo vratili ako nismo dvaput vratili istu �eliju (slu�aj sredi�nje �elije u mre�i neparnih dimenzija)
		if (lastRemovedPosSymetric.peek() != lastRemovedPosOriginal.peek()) {
	    	numUseDigit[lastRemovedValSymetric.peek()]++;
	    	checkIfDigitMaxUsed(lastRemovedValSymetric.peek());
		}
		solution[lastRemovedPosOriginal.peek()] = lastRemovedValOriginal.peek();
		solution[lastRemovedPosSymetric.peek()] = lastRemovedValSymetric.peek();
		temporary[lastRemovedPosOriginal.peek()] = lastRemovedValOriginal.peek();
		temporary[lastRemovedPosSymetric.peek()] = lastRemovedValSymetric.peek();
		// Bri�emo vrhove stogova
		lastRemovedPosOriginal.pop();
		lastRemovedValOriginal.pop();
		lastRemovedPosSymetric.pop();
		lastRemovedValSymetric.pop();	
		return true;
	}
	// Generiramo nasumi�nu zagonetku
	public int randomPuzzle() {
		unset = 0;
		possibilities = new int[rows * cols][rows];
	    int[][] usedRow = new int[cols][rows];
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		usedRow[row][col] = 0;
	    		if (temporary[row * cols + col] != 0) {
			    	for (int val = 0; val < cols; val++) {
			    		possibilities[row * cols + col][val] = 0;
				    }
		    		possibilities[row * cols + col][temporary[row * cols + col] - 1] = 1;
		    		
	    		} else {
			    	for (int val = 0; val < cols; val++) {
			    		possibilities[row * cols + col][val] = 1;
				    }
			    	unset++;
	    		}
	    	}
	    }
	    fixPencilmarks();
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
	    		int numCell = row * cols + col;
	    		if (temporary[numCell] != 0) {
	    			continue;
	    		}
	    		if (isInTree(numCell) > 0) {
		    	    int possible = 0;
		    	    int firstPossible = 0;
		    		int lastPossible = 0;
		    		Set<Integer> possibleVals = new HashSet<Integer>();
		    		for (int val = 1; val <= cols; val++) {
			    		if (possibilities[numCell][val - 1] == 1 && temporary[numCell] == 0) {
			    			if (possible == 0) {
			    				firstPossible = val;
			    			}
			    			possible++;
			    			possibleVals.add(val);
			    			lastPossible = val;
			    		}
				    }
			    	if (possible == 0) {
				    	return 1;
			    	}
			    	if (possible == 1) {
				    	temporary[row * cols + col] = lastPossible;
				    	usedRow[lastPossible - 1][row] = 1;
				    	fixPencilmarks();
			    		unset--;
			    		if (unset == 0) {
			    			return 0;
			    		}
				    	continue;
			    	}
			    	int randomVal = ThreadLocalRandom.current().nextInt(firstPossible, lastPossible + 1);
			    	while (!possibleVals.contains(randomVal)) {
			    		randomVal = ThreadLocalRandom.current().nextInt(firstPossible, lastPossible + 1);
			    	}
			    	temporary[row * cols + col] = randomVal;
			    	usedRow[randomVal - 1][row] = 1;
			    	fixPencilmarks();
		    		unset--;
		    		if (unset == 0) {
		    			return 0;
		    		}
	    		}
	    	}
	    }
	    if (diagonalOn) {
		    int[] rowOrder = new int[rows];
		    int rowIndex = 0;
		    for (int rowOffset = 0; rowOffset < rows / 2 + rows % 2; rowOffset++){
		    	if (rows % 2 == 0) {
		    		rowOrder[rowIndex] = rows / 2 - 1 - rowOffset;
		    		rowIndex++;
		    		rowOrder[rowIndex] = rows / 2 - rowOffset;
		    		rowIndex++;
		    	} else {
		    		if (rowOffset == 0) {
			    		rowOrder[rowIndex] = rows / 2 - rowOffset;
			    		rowIndex++;
		    		} else {
			    		rowOrder[rowIndex] = rows / 2 - rowOffset;
			    		rowIndex++;
			    		rowOrder[rowIndex] = rows / 2 + rowOffset;
			    		rowIndex++;
		    		}
		    	}
		    }
		    for (int rowOffset = 0; rowOffset < rows; rowOffset++) {
		    	int row = rowOrder[rowOffset];
	    	    for (int col = 0; col < cols; col++) {
	    	    	if (row != col && row != cols - 1 - col) {
	    	    		continue;
	    	    	}
		    	    int possible = 0;
		    	    int firstPossible = 0;
		    		int lastPossible = 0;
		    		Set<Integer> possibleVals = new HashSet<Integer>();
		    		for (int val = 1; val <= cols; val++) {
			    		int numCell = row * cols + col;
			    		if (possibilities[numCell][val - 1] == 1 && temporary[numCell] == 0) {
			    			if (possible == 0) {
			    				firstPossible = val;
			    			}
			    			possible++;
			    			possibleVals.add(val);
			    			lastPossible = val;
			    		}
				    }
			    	if (possible == 0) {
				    	return 1;
			    	}
			    	if (possible == 1) {
				    	temporary[row * cols + col] = lastPossible;
				    	usedRow[lastPossible - 1][row] = 1;
				    	fixPencilmarks();
			    		unset--;
			    		if (unset == 0) {
			    			return 0;
			    		}
				    	continue;
			    	}
			    	int randomVal = ThreadLocalRandom.current().nextInt(firstPossible, lastPossible + 1);
			    	while (!possibleVals.contains(randomVal)) {
			    		randomVal = ThreadLocalRandom.current().nextInt(firstPossible, lastPossible + 1);
			    	}
			    	temporary[row * cols + col] = randomVal;
			    	usedRow[randomVal - 1][row] = 1;
			    	fixPencilmarks();
		    		unset--;
		    		if (unset == 0) {
		    			return 0;
		    		}
			    }
			}
		    for (int rowOffset = 0; rowOffset < rows; rowOffset++) {
		    	int row = rowOrder[rowOffset];
	    	    for (int col = 0; col < cols; col++) {
	    	    	if (row == col || row == cols - 1 - col) {
	    	    		continue;
	    	    	}
	    	    	int boxLeft = boxNumber[row * cols + row];
	    	    	int boxRight = boxNumber[row * cols + cols - 1 - row];
	    	    	int box= boxNumber[row * cols + col];
	    	    	if (box != boxLeft && box != boxRight) {
	    	    		continue;
	    	    	}
		    	    int possible = 0;
		    	    int firstPossible = 0;
		    		int lastPossible = 0;
		    		Set<Integer> possibleVals = new HashSet<Integer>();
		    		for (int val = 1; val <= cols; val++) {
			    		int numCell = row * cols + col;
			    		if (possibilities[numCell][val - 1] == 1 && temporary[numCell] == 0) {
			    			if (possible == 0) {
			    				firstPossible = val;
			    			}
			    			possible++;
			    			possibleVals.add(val);
			    			lastPossible = val;
			    		}
				    }
			    	if (possible == 0) {
				    	return 1;
			    	}
			    	if (possible == 1) {
				    	temporary[row * cols + col] = lastPossible;
				    	usedRow[lastPossible - 1][row] = 1;
				    	fixPencilmarks();
			    		unset--;
			    		if (unset == 0) {
			    			return 0;
			    		}
				    	continue;
			    	}
			    	int randomVal = ThreadLocalRandom.current().nextInt(firstPossible, lastPossible + 1);
			    	while (!possibleVals.contains(randomVal)) {
			    		randomVal = ThreadLocalRandom.current().nextInt(firstPossible, lastPossible + 1);
			    	}
			    	temporary[row * cols + col] = randomVal;
			    	usedRow[randomVal - 1][row] = 1;
			    	fixPencilmarks();
		    		unset--;
		    		if (unset == 0) {
		    			return 0;
		    		}
			    }

			}
	    }
		// Postavljamo vrijednost u odre�eni redak
	    for (int row = 0; row < rows; row++) {
			for (int val = 1; val <= cols; val++) {
				// Ako je vrijednost ve� postavljena u redak, nastavljamo dalje
				if (usedRow[val - 1][row] == 1) {
					continue;
				}
	    	    int possible = 0;
	    	    int firstPossible = 0;
	    		int lastPossible = 0;
	    		Set<Integer> possibleCols = new HashSet<Integer>();
	    	    for (int col = 0; col < cols; col++) {
		    		int numCell = row * cols + col;
		    		if (possibilities[numCell][val - 1] == 1 && temporary[numCell] == 0) {
		    			if (possible == 0) {
		    				firstPossible = col;
		    			}
		    			possible++;
		    			possibleCols.add(col);
		    			lastPossible = col;
		    		}
			    }
		    	// Ako vrijednost nije mogu�e postaviti u red, zagonetka koju smo generirali nije ispravna i moramo krenuti isponova
		    	if (possible == 0) {
			    	return 1;
		    	}
		    	// Ako je vrijednost unutar reda mogu�e postaviti samo na jednu poziciju, mo�emo postaviti vrijednost
		    	if (possible == 1) {
			    	temporary[row * cols + lastPossible] = val;
			    	fixPencilmarks();
		    		unset--;
		    		if (unset == 0) {
		    			return 0;
		    		}
			    	continue;
		    	}
		    	int randomCol = ThreadLocalRandom.current().nextInt(firstPossible, lastPossible + 1);
		    	while (!possibleCols.contains(randomCol)) {
		    		randomCol = ThreadLocalRandom.current().nextInt(firstPossible, lastPossible + 1);
		    	}
		    	temporary[row * cols + randomCol] = val;
		    	fixPencilmarks();
	    		unset--;
	    		if (unset == 0) {
	    			return 0;
	    		}
		    }
		}
		return 0;
	}
	public Sudoku(int constructRows, int constructCols, int rowLimit, int colLimit, boolean setDiagonalOn, Set<String> setSizeRelationships) {
		super(constructRows, constructCols, rowLimit, colLimit);
		diagonalOn = setDiagonalOn;
		setSizeRelationships.forEach( relationship -> sizeRelationships.add(relationship));
	}
	// Uklanjamo ozna�avanje pozadine reda i stupca �elije koja je u fokusu i ozna�avanje odabrane znamenke
	abstract public void resetHighlight();
	// Dodajemo ozna�avanje pozadine reda i stupca �elije koja je u fokusu
	abstract public void highlightCell(int numCell);
	// Dodajemo ozna�avanje odabrane znamenke
	abstract public void highlightDigit();
	abstract public void assume();
	
	int[] numUseDigit = new int[cols + 1];
	
	// Prebrojavamo koliko je puta iskori�tena koja znamenka
	public void checkIfDigitMaxUsed(int digit) {
		// Ne prebrojavamo polja koja su prazna (0)
		if (digit == 0) {
			return;
		}
		if (numUseDigit[digit] >= cols) {
			// Ako je znamenka iskori�tena vi�e ili jednako puta koliko je stupaca u mre�i, pobojamo tekst gumba koji joj pripada u sivo kao upozorenje
			digitButtons[digit].setForeground(Color.LIGHT_GRAY);
		} else {
			// Ako je znamenka iskori�tena manje puta nego �to je stupaca u mre�i, pobojamo tekst gumba koji joj pripada u crno
			digitButtons[digit].setForeground(Color.BLACK);
		}
	}	

	KeyListener keyListener  =
	new KeyListener(){
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			if (key - 48 >= 0 && key <= 57 && key - 48 <= cols) {
				selectedDigit = key - 48;
				resetHighlight();
				highlightDigit();
			}
			if (key - 65 >= 0 && key <= 90 && key - 65 + 10 <= cols) {
				selectedDigit = key - 65 + 10;
				resetHighlight();
				highlightDigit();
			}
    		checkIfCorrect();
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};
	
	public void addErrorScroll(int digitEnd, int buttonEnd) {
		x += w + space;
        y = space;
        w = (int) (280 * widthScaling);
        errorArea = new JTextArea(0, 0);
        errorArea.setFont(new Font("Arial", Font.PLAIN, fontsizeTextArea));
        errorArea.setEditable (false);
	    JPanel errorPanel = new JPanel();
        errorPanel.add(errorArea, BorderLayout.CENTER);
	    JScrollPane errorScroll = new JScrollPane(errorPanel, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    errorScroll.setBounds(x, y, w, Math.max(digitEnd, buttonEnd) - 2 * space);
	    frame.add(errorScroll);
	    errorScroll.setVisible(true);  
	    errorScroll.setBackground(Color.WHITE);
	    errorScroll.setVisible(true);  
	    x += w + space;
	}
	
	public void addInstructionScroll(int digitEnd, int buttonEnd) {
        w = (int) (500 * widthScaling);
		instructionArea = new JTextArea(0, 0);
        instructionArea.setFont(new Font("Arial", Font.PLAIN, fontsizeTextArea));
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
	}
	

	public void addZoomBox(int xZoom, int yZoom, int widthZoom, int heightZoom) {
		zoomArea = new JButton();
		zoomArea.setFont(new Font("Arial", Font.PLAIN, numberFontsize));
		zoomArea.setBackground(Color.BLACK);
		zoomArea.setForeground(Color.WHITE);
		zoomArea.setFocusable(false);
		zoomArea.setBounds(xZoom, yZoom, widthZoom, heightZoom);
	    frame.add(zoomArea);  
	}
	
	@Override
	public int makeButtons() {
	    for (int row = 0; row < rows; row++){ 
	    	x = space;
	    	for (int col = 0; col < cols; col++) {
	    		int numCell = row * cols + col;
	    	    field[numCell] = new JButton();  
			    field[numCell].setMargin(new Insets(1,1,1,1));
			    field[numCell].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
			    field[numCell].setBounds(x, y, wNumber, hNumber);
			    field[numCell].setUI(new MetalButtonUI() {
    			    protected Color getDisabledTextColor() {
    			        return Color.CYAN;
    			    }
    			});
			    field[numCell].addKeyListener(keyListener);
			    field[numCell].addActionListener(makeActionListener(numCell));
			    frame.add(field[numCell]);
		    	x += wNumber;
		    }
		    y += hNumber;
	    }
	    x = space;
	    y += space;
	    return space + wNumber * cols;
	}

	public abstract ActionListener makeDigitActionListener(int numCell);
	
	public int makeDigitButtons() {
		x = space;
	    y += space;
		digitButtons = new JButton[cols + 1];
		for (int row = 0; row < cols + 1; row++) {
			String text = "";
			if (row < 10) {
				text += String.valueOf(row);
			} else {
				char c = 'A';
				c += row - 10;
				text += c;
			}
			digitButtons[row] = new JButton(text);  
			digitButtons[row].setMargin(new Insets(1,1,1,1));
			if (row != selectedDigit) {
				digitButtons[row].setBackground(Color.WHITE);
			} else {
				digitButtons[row].setBackground(Color.CYAN);
			}
			digitButtons[row].setBounds(x, y, wDigit, hDigit);
			digitButtons[row].setFont(new Font("Arial", Font.PLAIN, digitFontsize));
	        if (row == 0) {
	        	digitButtons[row].setForeground(Color.RED);
	        }
	        int digit = row;
	        digitButtons[row].addActionListener(makeDigitActionListener(digit));
	        digitButtons[row].addKeyListener(keyListener);
	        frame.add(digitButtons[row]);
	        x += wDigit;
		}
		return y + hDigit + space;
	}
	
	@Override
	public JButton makeAButton(String title, int xPosition, int yPosition, int widthButton, int heightButton, ActionListener actionListerToAdd) {
		JButton newButton = new JButton(title);  
		newButton.setMargin(new Insets(1,1,1,1));
		newButton.setBounds(xPosition, yPosition, widthButton, heightButton);
		newButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
		newButton.addActionListener(actionListerToAdd);
		newButton.addKeyListener(keyListener);
		frame.add(newButton);
		return newButton;
	} 
	
	public int setAllOptions(int[][] optionList, int row, int col, boolean makeBold) {
		String text = "<html><p style='text-align: center'><font color = yellow>";
		int numberOptions = 0;
    	for (int val = 0; val < cols; val++) {
    		if (optionList[row * cols + col][val] == 1) {
    			numberOptions++;
    			if (val == selectedDigit - 1 && makeBold) {
    				text += "<b><i>";
    			}
    			if (val + 1 < 10) {
    				text += String.valueOf(val + 1) + " ";
    			} else {
    				char c = 'A';
    				c += val - 9;
    				text += c + " ";
    			}
    			if (val == selectedDigit - 1 && makeBold) {
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
		field[row * cols + col].setForeground(Color.YELLOW);
		field[row * cols + col].setFont(new Font("Arial", Font.PLAIN, guessFontsize));
		return numberOptions;
	}

	public abstract int countIncorrect(boolean[] incorrect, boolean correct);

	
	public void incorrectRow(boolean[] incorrect, int row, int col, int val) {
		int optionNumber = 0;
		for (int sameRow = 0; sameRow < cols; sameRow++) {
			int numCell = row * cols + sameRow;
			if (temporary[numCell] == val && numCell != row * cols + col) {
				incorrect[numCell] = true;
				incorrect[row * cols + col] = true;    
				optionNumber++;				
				if (optionNumber > 1) {
					continue;
				}				        			
				errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " ve� postoji u retku " + (row + 1) + ".\n");
			}
		}
	}
	
	public void incorrectCol(boolean[] incorrect, int row, int col, int val) {
		int optionNumber = 0;
		for (int sameCol = 0; sameCol < rows; sameCol++) {
			int numCell = sameCol * cols + col;
			if (temporary[numCell] == val && numCell != row * cols + col) {
				incorrect[numCell] = true;
				incorrect[row * cols + col] = true;  
				optionNumber++;				
				if (optionNumber > 1) {
					continue;
				}
				errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " ve� postoji u stupcu " + (col + 1) + ".\n");
			}
		}
	}
	
	public void incorrectDiagonalUp(boolean[] incorrect, int row, int col, int val) {
	    int optionNumber = 0;
	    if (row == col && diagonalOn) {
	    	for (int diagonally = 0; diagonally < cols; diagonally++) {
	    		int numCell = diagonally * cols + diagonally;
	    		if (temporary[numCell] == val && numCell != row * cols + col) {
    				incorrect[numCell] = true;    
    				incorrect[row * cols + col] = true;   	
    				optionNumber++;				
    				if (optionNumber > 1) {
    					continue;
    				}
    				errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " ve� postoji u rastu�oj dijagonali.\n");
	    		}
	    	}
	    }
	}
	
	public void incorrectDiagonalDown(boolean[] incorrect, int row, int col, int val) {
	    int optionNumber = 0;
	    if (row == cols - 1 - col && diagonalOn) {
	    	for (int diagonally = 0; diagonally < cols; diagonally++) {
	    		int numCell = diagonally * cols + cols - 1 - diagonally;
	    		if (temporary[numCell] == val && numCell != row * cols + col) {
    				incorrect[numCell] = true;   
    				incorrect[row * cols + col] = true;   
    				optionNumber++;				
    				if (optionNumber > 1) {
    					continue;
    				}
    				errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " ve� postoji u padaju�oj dijagonali.\n");
	    		}
	    	}
	    }
	}
	
	public void incorrectBox(boolean[] incorrect, int row, int col, int val) {
		int optionNumber = 0;
		for (int newCell = 0; newCell < rows * cols; newCell++) {
			if (boxNumber[row * cols + col] != boxNumber[newCell]) {
				continue;
			}
			if (temporary[newCell] == val && newCell != row * cols + col) {
				incorrect[newCell] = true;	
				incorrect[row * cols + col] = true;   	
				optionNumber++;				
				if (optionNumber > 1) {
					continue;
				}
				errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " ve� postoji u kutiji " + (boxNumber[row * cols + col] + 1) + ".\n");
			}
		}
	}
	
	public void incorrectRelationship(boolean[] incorrect, int row, int col, int val) {
		for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
			for (int colOffset = -1; colOffset <= 1; colOffset++) {
				int numCell = row * cols + col;
		        int newCell = (numCell / cols + rowOffset) * cols + numCell % cols + colOffset;
				if (neighbourCheck(numCell, newCell)) {
					String relationshipCell = String.valueOf(numCell) + " " + String.valueOf(newCell);
	    			if (sizeRelationships.contains(relationshipCell) && temporary[numCell] <= temporary[newCell]) {
		    			incorrect[numCell] = true;
		    			incorrect[newCell] = true;
	    				errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije ve�i od broja " + temporary[newCell] + " u �eliji (" + (row + 1) + ", " + (col + 2) + ").\n");
		    		} 
	    			relationshipCell = String.valueOf(newCell) + " " + String.valueOf(numCell);
	    			if (sizeRelationships.contains(relationshipCell) && temporary[newCell] != 0 && temporary[row * cols + col] >= temporary[newCell]) {
	    				incorrect[numCell] = true;
		    			incorrect[newCell] = true;
		    			errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije manji od broja " + temporary[newCell] + " u �eliji (" + (row + 1) + ", " + (col + 2) + ").\n");
	    			} 
				}
			}
		}
	}
	
	public boolean[] basicIncorrect() {
		boolean incorrect[] = new boolean[rows * cols];
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
		    	temporary[numCell] = userInput[numCell];
		    	incorrect[numCell] = false;
	    	}
	    }
	    initPencilmarks();
		fixPencilmarks();
    	errorArea.setText("");
		for (int val = 1; val <= rows; val++) {
		    for (int row = 0; row < rows; row++){
		    	for (int col = 0; col < cols; col++) {
		    		if (temporary[row * cols + col] == val) {
		    			incorrectRow(incorrect, row, col, val);
		    			incorrectCol(incorrect, row, col, val);
		    			incorrectBox(incorrect, row, col, val);
		    			incorrectDiagonalUp(incorrect, row, col, val);
		    			incorrectDiagonalDown(incorrect, row, col, val);
		    			incorrectRelationship(incorrect, row, col, val);
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
			    		errorArea.setText(errorArea.getText() + "�elija (" + (row + 1) + ", " + (col + 1) + ") nema mogu�ih vrijednosti.\n");
	    				incorrect[row * cols + col] = true;
			    	}
	    		}
	    	}
	    }
		return incorrect;
	}
	
	public boolean checkIfCorrect() {
		boolean incorrect[] = basicIncorrect();
	    boolean correct = true;
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
	    		if (incorrect[row * cols + col]) {
	    			correct = false;
	    			break;
	    		}
	    	}
	    }
		if (countIncorrect(incorrect, correct) > 0) {
			return false;
		} else {
			return true;
		}
	}
	
	public void swapRow(int row1, int row2) {
		int[] array1 = new int[cols];
		int[] array2 = new int[cols];
    	for (int col = 0; col < cols; col++) {
    		array1[col] = userInput[row1 * cols + col];
    		array2[col] = userInput[row2 * cols + col];
    	}
    	for (int col = 0; col < cols; col++) {
    		userInput[row1 * cols + col] = array2[col];
    		userInput[row2 * cols + col] = array1[col];
    		if (userInput[row1 * cols + col] < 10) {
    			field[row1 * cols + col].setText(String.valueOf(userInput[row1 * cols + col]));
    		} else {
    			char c = 'A';
    			c += userInput[row1 * cols + col] - 10;
    			field[row1 * cols + col].setText("" + c);
    		}
    		if (userInput[row2 * cols + col] < 10) {
    			field[row2 * cols + col].setText(String.valueOf(userInput[row2 * cols + col]));
    		} else {
    			char c = 'A';
    			c += userInput[row2 * cols + col] - 10;
    			field[row2 * cols + col].setText("" + c);
    		}
    	}
	}

	public void swapCol(int col1, int col2) {
		int[] array1 = new int[cols];
		int[] array2 = new int[cols];
    	for (int row = 0; row < rows; row++) {
    		array1[row] = userInput[row * cols + col1];
    		array2[row] = userInput[row * cols + col2];
    	}
    	for (int row = 0; row < rows; row++) {
    		userInput[row * cols + col1] = array2[row];
    		userInput[row * cols + col2] = array1[row];
    		if (userInput[row * cols + col1] < 10) {
    			field[row * cols + col1].setText(String.valueOf(userInput[row * cols + col1]));
    		} else {
    			char c = 'A';
    			c += userInput[row * cols + col1] - 10;
    			field[row * cols + col1].setText("" + c);
    		}
    		if (userInput[row * cols + col2] < 10) {
    			field[row * cols + col2].setText(String.valueOf(userInput[row * cols + col2]));
    		} else {
    			char c = 'A';
    			c += userInput[row * cols + col2] - 10;
    			field[row * cols + col2].setText("" + c);
    		}
    	}
	}

	public void swapNumbers(int a, int b) {
    	for (int row = 0; row < rows; row++) {
    		for (int col = 0; col < cols; col++) {
    			int numCell = row * cols + col;
    			if (userInput[numCell] == a) {
    				userInput[numCell] = b;
    	    		if (userInput[numCell] < 10) {
    	    			field[numCell].setText(String.valueOf(userInput[numCell]));
    	    		} else {
    	    			char c = 'A';
    	    			c += userInput[numCell] - 10;
    	    			field[numCell].setText("" + c);
    	    		}
    			} else {
    				if (userInput[numCell] == b) {
        				userInput[numCell] = a;
        	    		if (userInput[numCell] < 10) {
	    	    			field[numCell].setText(String.valueOf(userInput[numCell]));
	    	    		} else {
	    	    			char c = 'A';
	    	    			c += userInput[numCell] - 10;
	    	    			field[numCell].setText("" + c);
	    	    		}
    				}
    			}
    		}
    	}
	}

	public void initialize() {
		int rowOrder[] = new int[rows];
		int colOrder[] = new int[cols];
	    for (int row = 0; row < rows; row++){ 
	    	rowOrder[row] = -1;
	    }
    	for (int col = 0; col < cols; col++) {
    		colOrder[col] = -1;
    	}
	    for (int val = 0; val < rows; val++){ 
	    	int pos = ThreadLocalRandom.current().nextInt(0, rows);
	    	while (rowOrder[pos] != -1) {
	    		pos = ThreadLocalRandom.current().nextInt(0, rows);
	    	}
	    	rowOrder[pos] = val;
	    }
	    for (int val = 0; val < cols; val++){ 
	    	int pos = ThreadLocalRandom.current().nextInt(0, cols);
	    	while (colOrder[pos] != -1) {
	    		pos = ThreadLocalRandom.current().nextInt(0, cols);
	    	}
	    	colOrder[pos] = val;
	    }
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
	    		int numCell = row * cols + col;
	    		userInput[numCell] = colOrder[col] - rowOrder[row];
	    		while (userInput[numCell] <= 0) {
	    			userInput[numCell] += cols;
	    		}
	    		temporary[numCell] = userInput[numCell];
	    	}
	    }
	}
}
