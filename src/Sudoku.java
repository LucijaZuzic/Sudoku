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
	// Tekstualno podruèje za prikaz greški
	JTextArea errorArea;
	// Tekstualno podruèje za prikaz uputa
	JTextArea instructionArea;
	// Tekstualno podruèje za prikaz odbranog polja 
	JButton zoomArea;
	// Oznaka za prikaz težine
	JLabel difficulty = new JLabel("");
	// Tekst uputa za rješavanje
	String solvingInstructions;
	// Odabrana znamenka
	int selectedDigit = 0;
	// Težina zagonetke
	int difficultyScore = 0;
	// Skup pozicija uklonjenih vrijednosti
	Stack<Integer> lastRemovedPosOriginal = new Stack<Integer>();
	// Skup uklonjenih vrijednosti
	Stack<Integer> lastRemovedValOriginal = new Stack<Integer>();
	// Skup rotacijski simetriènih pozicija uklonjenih vrijednosti
	Stack<Integer> lastRemovedPosSymetric = new Stack<Integer>();
	// Skup rotacijski simetriènih uklonjenih vrijednosti
	Stack<Integer> lastRemovedValSymetric = new Stack<Integer>();
	// Broj praznih polja
	int unset = 0;
	// Je li ukljuèen prikaz uputa korak po korak u zasebnom prozoru
	boolean showSteps = false;
	// Ažurira moguæe vrijednosti za æelije nakon upisa nove konaène vrijednosti
	public int fixPencilmarks() {
		int numChanged = 0;
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (temporary[row * cols + col] != 0) { 
	    			// Ako je konaèna vrijednost upisana uklanjamo moguænosti
			    	for (int val = 0; val < cols; val++) {
			    		possibilities[row * cols + col][val] = 0;
				    }
	    		} else {
	    			// Ukloni moguænosti za vrijednosti koje veæ postoje u istom stupcu
	    			for (int fullCol = 0; fullCol < cols; fullCol++) { 
			    		if (temporary[row * cols + fullCol] != 0 && possibilities[row * cols + col][temporary[row * cols + fullCol] - 1] == 1) { 
				    		possibilities[row * cols + col][temporary[row * cols + fullCol] - 1] = 0;
		    				numChanged = 1;
			    		}
				    }
	    			// Ukloni moguænosti za vrijednosti koje veæ postoje u istom redu
			    	for (int fullRow = 0; fullRow < rows; fullRow++) { 
			    		if (temporary[fullRow * cols + col] != 0 && possibilities[row * cols + col][temporary[fullRow * cols + col] - 1] == 1) { 
			    				possibilities[row * cols + col][temporary[fullRow * cols + col] - 1] = 0;
			    				numChanged = 1;
			    		}
				    }
			    	// Ukloni moguænosti za vrijednosti koje veæ postoje u istoj kutiji
			    	int box = boxNumber[row * cols + col];
				    for (int fullCol = 0; fullCol < rows; fullCol++){ 
					    for (int fullRow = 0; fullRow < cols; fullRow++){
				    		if (temporary[fullCol * cols + fullRow] != 0 && boxNumber[fullCol * cols + fullRow] == box && possibilities[row * cols + col][temporary[fullCol * cols + fullRow] - 1] == 1) {
			    				possibilities[row * cols + col][temporary[fullCol * cols + fullRow] - 1] = 0;
			    				numChanged = 1;
				    		}
				    	}
				    }
			    	// Ukloni moguænosti za vrijednosti koje veæ postoje u rastuæoj dijagonali (ako ukljuèujemo diajgonale i ako je broj na toj dijagonali)
	    			if (row == col && diagonalOn) {
					    for (int diagonally = 0; diagonally < cols; diagonally++){ 
					    	if (temporary[diagonally * cols + diagonally] != 0) {
			    				possibilities[row * cols + col][temporary[diagonally * cols + diagonally] - 1] = 0;
			    				numChanged = 1;
				    		}
					    }
	    			} 
			    	// Ukloni moguænosti za vrijednosti koje veæ postoje u padajuæoj dijagonali (ako ukljuèujemo diajgonale i ako je broj na toj dijagonali)
	    			if (row == cols - 1 - col && diagonalOn) {
					    for (int diagonally = 0; diagonally < cols; diagonally++){ 
					    	if (temporary[diagonally * cols + (cols - 1 - diagonally)] != 0) {
			    				possibilities[row * cols + col][temporary[diagonally * cols + (cols - 1 - diagonally)] - 1] = 0;
			    				numChanged = 1;
				    		}
					    }
	    			}
	    		}
		    	// Ukloni moguænosti prema odnosima veæe-manje
	    		Set<Integer> visitedMax = new HashSet<Integer>();
	    		if (setMaxPossibility(row * cols + col, visitedMax) == 1) {
	    			numChanged = 1;
	    		}
		    	// Ukloni moguænosti prema odnosima manje-veæe
	    		Set<Integer> visitedMin = new HashSet<Integer>();
	    		if (setMinPossibility(row * cols + col, visitedMin) == 1) {
	    			numChanged = 1;
	    		}
	    	}
		}
	    // Vraæamo 0 ako se nije promjenila niti jedna moguænost niti za jednu æeliju, a 1 ako se barem jedna moguænost barem jedne æelije promijenila
	    return numChanged;
	}
	// Tražimo æelije koje imaju samo jednu moguæu vrijednost (kandidata) 
	public int singleCandidate() {
		// Upisujemo konaène vrijednosti u æelije koje imaju samo jednu moguænost
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		// Ako je veæ upisana konaèna vrijednost, preskaèemo æeliju
	    		if (temporary[row * cols + col] == 0) {
	    			int possibility = 0;
			    	for (int val = 0; val < cols; val++) {
			    		possibility += possibilities[row * cols + col][val];
				    }
			    	// Ako je preostala samo jedna moguænost za æeliju, možemo ju upisati
			    	if (possibility == 1) {
			    		// Koristenje jedinog kandidata u æeliji ima trošak 100
			    		difficultyScore += 100;
			    		unset--;
				    	for (int val = 0; val < cols; val++) {
				    		if (possibilities[row * cols + col][val] == 1) {		
				    			// Dodajemo novi red u tekst uputa
				    			solvingInstructions += "Broj " + String.valueOf(val + 1) + " je jedina moguæa vrijednost æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
				    			// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
				    			if (showSteps == true) {
		    		    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Broj " + String.valueOf(val + 1) + " je jedina moguæa vrijednost æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ")", "Rješavaè")) {
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
		    		    		// Ažuriramo moguæe vrijednosti ostalih æelija
		    		    		fixPencilmarks();
		    		    		break;
				    		}
					    }
				    	// Ako smo postavili sve æelije, prekidamo rješavanje
			    		if (unset == 0) {
			    			return 1;
			    		}
			    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
						if (sequence() == 1) {
			    			return 1;
						}
			    	}
	    		}
	    	}
	    }
	    return 0;
	}
	// Broj korištenja za metodu ogoljenih skupova po velièini skupa
	int dj2 = 0;
	int dj3 = 0;
	int dj4 = 0;
	// Maksimalna velièina skrivenog ili ogoljenog skupa
	int depthLimit;
	// Dodaje se trošak za metode ogoljenog skupa i linija na ispis uputa za rješavanje
	// setCells - skup brojeva æelija u ogoljenom skupu, matchString - popis vrijednosti koje pokriva ogoljeni skup, znakovni niz 0 i 1 koje oznaèavaju je li znamenka ukljuèena
	// containerNum - broj reda, stupca ili kutije u kojem se nalazi ogoljeni skup, containerType - string koji oznaèava je li ogoljeni skup u redu, stupcu ili kutiji
	// firstCell - ishodišna æelija ogoljenog skupa kojoj smo tražili æelije koje sadrže neku od istih moguæih vrijednosti kao i ona
	public void difficultySettingNaked(Set<Integer> setCells, String matchString, int containerNum, String containerType, int firstCell) {
		String setType = "Ogoljen";
		String lineSolvInstr = "";
		// Dio koda za ogoljeni par
		if (setCells.size() == 1) {
			// Dodavanje vrijednosti skupa u upute za rješavanje
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
			// Ako smo veæ registrirali ovaj isti ogoljeni par, ne bodujemo ga dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (dj2 == 0) {
    				// Kada prvi put naðemo ogoljeni par, trošak je 750
    				difficultyScore += 750;
    				dj2 = 1;
    			} else {
    				// Kada iduæi put naðemo ogoljeni par, trošak je 500
    				difficultyScore += 500;
    			}
    		} else {
    			return;
    		}
		}
		// Dio koda za ogoljenu trojku
		if (setCells.size() == 2) {
			// Dodavanje vrijednosti skupa u upute za rješavanje
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
			// Ako smo veæ registrirali ovu istu ogoljenu trojku, ne bodujemo ju dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (dj3 == 0) {
    				// Kada prvi put naðemo ogoljenu trojku, trošak je 2000
    				difficultyScore += 2000;
    				dj3 = 1;
    			} else {
    				// Kada iduæi put naðemo ogoljenu trojku, trošak je 1400
    				difficultyScore += 1400;
    			}
    		} else {
    			return;
    		}
		}
		// Dio koda za ogoljenu èetvorku
		if (setCells.size() == 3) {
			// Dodavanje vrijednosti skupa u upute za rješavanje
			lineSolvInstr += setType + "a èetvorka u " + containerType + " " + String.valueOf(containerNum + 1) + ", vrijednosti";
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
			// Ako smo veæ registrirali ovu istu ogoljenu èetvorku, ne bodujemo ju dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (dj4 == 0) {
    				// Kada prvi put naðemo ogoljenu èetvorku, trošak je 5000
    				difficultyScore += 5000;
    				dj4 = 1;
    			} else {
    				// Kada iduæi put naðemo ogoljenu èetvorku, trošak je 4000
    				difficultyScore += 4000;
    			}
    		} else {
    			return;
    		}
		}
		// Dio koda za ogoljeni skup veæi od èetiri
		if (setCells.size() > 3) {
			// Dodavanje vrijednosti skupa u upute za rješavanje
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
			// Ako smo veæ registrirali ovaj isti ogoljeni skup veæi od èetiri, ne bodujemo ga dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (dj4 == 0) {
    				// Kada prvi put naðemo ogoljeni skup veæi od 4, trošak je 5000 (broji se kao i ogoljena èetvorka)
    				difficultyScore += 5000;
    				dj4 = 1;
    			} else {
    				// Kada iduæi put naðemo ogoljeni skup veæi od 4, trošak je 4000 (broji se kao i ogoljena èetvorka)
    				difficultyScore += 4000;
    			}
    		} else {
    			return;
    		}
		}
		// Dodavanje æelija skupa u upute za rješavanje (sluèaj skupa u redu)
		lineSolvInstr += " u æelijama";
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
		// Dodavanje æelija skupa u upute za rješavanje (sluèaj skupa u stupcu)
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
		// Dodavanje æelija skupa u upute za rješavanje (sluèaj skupa u kutiji)
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
    	// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
    	if (showSteps == true) {
		    instructionArea.setText(solvingInstructions);
		    print();
			if (!InformationBox.stepBox(lineSolvInstr, "Rješavaè")) {
				showSteps = false;
			}
		}
	}
	// Tražimo ogoljeni skup u retcima
	// firstRow - redak u kojem se nalazi ishodišna æelija kojoj smo tražili æelije koje sadrže neku od istih moguæih vrijednosti kao i ona
	// firstCol - stupac u kojem se nalazi ishodišna æelija kojoj smo tražili æelije koje sadrže neku od istih moguæih vrijednosti kao i ona
	// sameRowCells - skup brojeva æelija koje bi se mogle nalaziti u ogoljenom skupu
	public int nakedSetForRow (int firstRow, int firstCol, Set<Integer> sameRowCells) {
		// Ako je skup veæi od ogranièene duljine, nastavljamo dalje
		if (sameRowCells.size() >= depthLimit) {
			return 0;
		}
    	int firstCell = firstRow * cols + firstCol;
    	// Pretražujemo sve nove æelije u istom redu kao ishodište, ali u veæim stupcima, da izbjegnemo ponavljanje
    	for (int nextColInRow = firstCol + 1; nextColInRow < cols; nextColInRow++) {
	    	int newToSet = firstRow * cols + nextColInRow;
	    	if (numPossibilities[newToSet] <= 1) {
	    		continue;
	    	}
    		int match = 0;
    		// Pretražujemo sve æelije koje su veæ u ogoljenom skupu, a one se nalaze prije æelije koju dodajemo
    		for (int previousColInRow = 0; previousColInRow < nextColInRow; previousColInRow++) {
		    	int alreadyInSet = firstRow * cols + previousColInRow;
		    	if (numPossibilities[alreadyInSet] <= 1) {
		    		continue;
		    	}
    			if (alreadyInSet == firstCell || sameRowCells.contains(alreadyInSet)) {
		    		for (int val = 0; val < cols; val++) {
		    			// Provjeravamo podudara li se nova æelija sa svakom od æelija koje bi se mogle nalaziti u ogoljenom skupu i s ishodišnom æelijom u barem jednoj od moguæih vrijednosti
		    			if (possibilities[newToSet][val] == 1 && possibilities[alreadyInSet][val] == 1 && !sameRowCells.contains(newToSet) && newToSet != firstCell) {
		    				match++;
		    				break;
		    			}
		    		}
    			}
    		}
    		// Ako se nova æelija podudara sa svim æelijama u ogoljenom skupu i s ishodišnom æelijom, možemo ju dodati u skriveni skup
    		if (match == sameRowCells.size() + 1) {
    			Set<Integer> sameRowCellsNextIteration = new HashSet<Integer>();
        		for (int numCell = 0; numCell < rows * cols; numCell++) {
        			if (sameRowCells.contains(numCell)) {
        				sameRowCellsNextIteration.add(numCell);
        			}
        		}
        		sameRowCellsNextIteration.add(newToSet);
        		// Za svaku æeliju koju dodajemo u ogoljeni skup pokreæemo rekurziju, a u postojeæemo pozivu funkcije nastavljamo bez da ju dodamo
				if (nakedSetForRow(firstRow, firstCol, sameRowCellsNextIteration) == 1) {
	    			return 1;
				}
    		}
    		// Preborajavamo vrijednosti koje pokriva ogoljeni skup æelija u obliku niza znakova 0 i 1
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int val = 0; val < cols; val++) {
	    		String containsVal = "0";
	    		for (int col = 0; col < cols; col++) {
	    			int alreadyInSet = firstRow * cols + col;
	    			if (sameRowCells.contains(alreadyInSet) || alreadyInSet == firstCell) {
	    				// Ako neka od æelija u ogoljenom skupu pokriva vrijednost, dodajemo znak 1
				    	if (possibilities[alreadyInSet][val] == 1) {
				    		containsVal = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsVal;
    		}
	    	// Ako ogoljeni skup pokriva jednako vrijednosti koliko sadrži æelija te nije prazan skup, to je ispravan ogoljeni skup
	    	if (sameRowCells.size() == matchStringLen - 1 && sameRowCells.size() > 0) {
			    int numRemoved = 0;
	    		for (int col = 0; col < cols; col++) {
			    	int notInSet = firstRow * cols + col;
			    	// Ako æelija nije u ogoljenom skupu možda joj možemo ukloniti neku od moguænosti
			    	if (!sameRowCells.contains(notInSet) && notInSet != firstCell) {
			    		for (int val = 0; val < cols; val++) {
			    			// Ako æelija sadrži moguæu vrijednost koju pokriva ogoljeni skup, možemo ukloniti tu moguænost
			    			if (matchString.charAt(val) == '1' && possibilities[notInSet][val] == 1 && temporary[notInSet] == 0) {
			    				// Isti uoèeni ogoljeni skup boduje se samo jednom, iako rezultira uklanjanjem više moguænosti
			    				if (numRemoved == 0) {
			    					difficultySettingNaked(sameRowCells, matchString, firstRow, "redu", firstCell);
			    				}
			    				// Dodajemo novi red za uklanjanje moguænosti u tekst uputa
			    				solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").\n";
			    				// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
			    				if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").", "Rješavaè")) {
			    	    				showSteps = false;
			    	    			}
					    		}
								possibilities[notInSet][val] = 0;
						    	// Ukloni moguænosti prema odnosima veæe-manje
					    		Set<Integer> visitedMax = new HashSet<Integer>();
								setMaxPossibility(notInSet, visitedMax);
						    	// Ukloni moguænosti prema odnosima manje-veæe
					    		Set<Integer> visitedMin = new HashSet<Integer>();
								setMinPossibility(notInSet, visitedMin);
					    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
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
	// Tražimo ogoljeni skup u stupcima
	// firstRow - redak u kojem se nalazi ishodišna æelija kojoj smo tražili æelije koje sadrže neku od istih moguæih vrijednosti kao i ona
	// firstCol - stupac u kojem se nalazi ishodišna æelija kojoj smo tražili æelije koje sadrže neku od istih moguæih vrijednosti kao i ona
	// sameColumnCells - skup brojeva æelija koje bi se mogle nalaziti u ogoljenom skupu
	public int nakedSetForCol (int firstRow, int firstCol, Set<Integer> sameColumnCells) {
		// Ako je skup veæi od ogranièene duljine, nastavljamo dalje
		if (sameColumnCells.size() >= depthLimit) {
			return 0;
		}
		int firstCell = firstRow * cols + firstCol;
    	// Pretražujemo sve nove æelije u istom stupcu kao ishodište, ali u veæim retcima, da izbjegnemo ponavljanje
    	for (int nextRowInCol = firstRow + 1; nextRowInCol < rows; nextRowInCol++) {
	    	int newToSet = nextRowInCol * cols + firstCol;
	    	if (numPossibilities[newToSet] <= 1) {
	    		continue;
	    	}
    		int match = 0;
    		// Pretražujemo sve æelije koje su veæ u ogoljenom skupu, a one se nalaze prije æelije koju dodajemo
    		for (int previousRowInCol = 0; previousRowInCol < nextRowInCol; previousRowInCol++) {
		    	int alreadyInSet = previousRowInCol * cols + firstCol;
		    	if (numPossibilities[alreadyInSet] <= 1) {
		    		continue;
		    	}
    			if (alreadyInSet == firstCell || sameColumnCells.contains(alreadyInSet)) {
		    		for (int val = 0; val < cols; val++) {
		    			// Provjeravamo podudara li se nova æelija sa svakom od æelija koje bi se mogle nalaziti u ogoljenom skupu i s ishodišnom æelijom u barem jednoj od moguæih vrijednosti
		    			if (possibilities[newToSet][val] == 1 && possibilities[alreadyInSet][val] == 1 && !sameColumnCells.contains(newToSet) && newToSet != firstCell) {
		    				match++;
		    				break;
		    			}
		    		}
    			}
    		}
    		// Ako se nova æelija podudara sa svim æelijama u ogoljenom skupu i s ishodišnom æelijom, možemo ju dodati u ogoljeni skup
    		if (match == sameColumnCells.size() + 1) {
    			Set<Integer> sameColumnNextIteration = new HashSet<Integer>();
	    		for (int numCell = 0; numCell < rows * cols; numCell++) {
	    			if (sameColumnCells.contains(numCell)) {
	    				sameColumnNextIteration.add(numCell);
	    			}
	    		}
	    		sameColumnNextIteration.add(newToSet);
        		// Za svaku æeliju koju dodajemo u ogoljeni skup pokreæemo rekurziju, a u postojeæemo pozivu funkcije nastavljamo bez da ju dodamo
				if (nakedSetForCol(firstRow, firstCol, sameColumnNextIteration) == 1) {
	    			return 1;
				}
			}
    		// Preborajavamo vrijednosti koje pokriva ogoljeni skup æelija u obliku niza znakova 0 i 1
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int val = 0; val < cols; val++) {
	    		String containsVal = "0";
	    		for (int row = 0; row < rows; row++) {
	    			int alreadyInSet = row * cols + firstCol;
    				// Ako neka od æelija u ogoljenom skupu pokriva vrijednost, dodajemo znak 1
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
	    	// Ako ogoljeni skup pokriva jednako vrijednosti koliko sadrži æelija te nije prazan skup, to je ispravan ogoljeni skup
	    	if (sameColumnCells.size() == matchStringLen - 1 && sameColumnCells.size() > 0) {
			    int numRemoved = 0;
	    		for (int row = 0; row < rows; row++) {
			    	int notInSet = row * cols + firstCol;
			    	// Ako æelija nije u ogoljenom skupu možda joj možemo ukloniti neku od moguænosti
			    	if (!sameColumnCells.contains(notInSet) && notInSet != firstCell) {
			    		for (int val = 0; val < cols; val++) {
			    			// Ako æelija sadrži moguæu vrijednost koju pokriva ogoljeni skup, možemo ukloniti tu moguænost
			    			if (matchString.charAt(val) == '1' && possibilities[notInSet][val] == 1 && temporary[notInSet] == 0) {
			    				// Isti uoèeni ogoljeni skup boduje se samo jednom, iako rezultira uklanjanjem više moguænosti
			    				if (numRemoved == 0) {
			    					difficultySettingNaked(sameColumnCells, matchString, firstCol, "stupcu", firstCell);
			    				}
			    				// Dodajemo novi red za uklanjanje moguænosti u tekst uputa
			    				solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").\n";
			    				// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
			    				if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").", "Rješavaè")) {
			    	    				showSteps = false;
			    					}
			    				}
			    				possibilities[notInSet][val] = 0;
						    	// Ukloni moguænosti prema odnosima veæe-manje
					    		Set<Integer> visitedMax = new HashSet<Integer>();
								setMaxPossibility(notInSet, visitedMax);
						    	// Ukloni moguænosti prema odnosima manje-veæe
					    		Set<Integer> visitedMin = new HashSet<Integer>();
								setMinPossibility(notInSet, visitedMin);
					    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
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
	// Tražimo ogoljeni skup u kutijama
	// firstRow - redak u kojem se nalazi ishodišna æelija kojoj smo tražili æelije koje sadrže neku od istih moguæih vrijednosti kao i ona
	// firstCol - stupac u kojem se nalazi ishodišna æelija kojoj smo tražili æelije koje sadrže neku od istih moguæih vrijednosti kao i ona
	// sameBoxCells - skup brojeva æelija koje bi se mogle nalaziti u ogoljenom skupu
	public int nakedSetForBox(int firstRow, int firstCol, Set<Integer> sameBoxCells) {
		// Ako je skup veæi od ogranièene duljine, nastavljamo dalje
		if (sameBoxCells.size() >= depthLimit) {
			return 0;
		}
		int firstCell = firstRow * cols + firstCol;
    	// Pretražujemo sve nove æelije u istoj kutiji kao ishodište, ali u veæim æelijama, da izbjegnemo ponavljanje
    	for (int newToSet = firstCell + 1; newToSet < rows * cols; newToSet++) {
	    	if (boxNumber[newToSet] != boxNumber[firstCell]) {
	    		continue;
	    	}
	    	if (numPossibilities[newToSet] <= 1) {
	    		continue;
	    	}
	    	int match = 0;
    		// Pretražujemo sve æelije koje su veæ u ogoljenom skupu, a one se nalaze prije æelije koju dodajemo
	    	for (int alreadyInSet = 0; alreadyInSet < newToSet; alreadyInSet++) {
		    	if (boxNumber[alreadyInSet] != boxNumber[firstCell]) {
		    		continue;
		    	}
		    	if (numPossibilities[alreadyInSet] <= 1) {
		    		continue;
		    	}
    			if (alreadyInSet == firstCell || sameBoxCells.contains(alreadyInSet)) {
		    		for (int val = 0; val < cols; val++) {
		    			// Provjeravamo podudara li se nova æelija sa svakom od æelija koje bi se mogle nalaziti u ogoljenom skupu i s ishodišnom æelijom u barem jednoj od moguæih vrijednosti
		    			if (possibilities[newToSet][val] == 1 && possibilities[alreadyInSet][val] == 1 && !sameBoxCells.contains(newToSet) && newToSet != firstCell) {
		    				match++;
		    				break;
		    			}
		    		}
    			}
	    	}
    		// Ako se nova æelija podudara sa svim æelijama u ogoljenom skupu i s ishodišnom æelijom, možemo ju dodati u ogoljeni skup
    		if (match == sameBoxCells.size() + 1) {
    			Set<Integer> sameBoxNextIteration = new HashSet<Integer>();
	    		for (int numCell = 0; numCell < rows * cols; numCell++) {
	    			if (sameBoxCells.contains(numCell)) {
	    				sameBoxNextIteration.add(numCell);
	    			}
	    		}
	    		sameBoxNextIteration.add(newToSet);
        		// Za svaku æeliju koju dodajemo u ogoljeni skup pokreæemo rekurziju, a u postojeæemo pozivu funkcije nastavljamo bez da ju dodamo
				if (nakedSetForBox(firstRow, firstCol, sameBoxNextIteration) == 1) {
	    			return 1;
				}
    		}
    		// Preborajavamo vrijednosti koje pokriva ogoljeni skup æelija u obliku niza znakova 0 i 1
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int val = 0; val < cols; val++) {
	    		String containsVal = "0";
	    		for (int numCell = 0; numCell < rows * cols; numCell++) {
			    	if (boxNumber[firstCell] != boxNumber[numCell]) {
			    		continue;
			    	}
    				// Ako neka od æelija u ogoljenom skupu pokriva vrijednost, dodajemo znak 1
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
	    	// Ako ogoljeni skup pokriva jednako vrijednosti koliko sadrži æelija te nije prazan skup, to je ispravan ogoljeni skup
	    	if (sameBoxCells.size() == matchStringLen - 1 && sameBoxCells.size() > 0) {
		    	int numRemoved = 0;
		    	for (int numCell = 0; numCell < rows * cols; numCell++) {
			    	int notInSet = numCell;
			    	if (boxNumber[firstCell] != boxNumber[notInSet]) {
			    		continue;
			    	}
			    	// Ako æelija nije u ogoljenom skupu možda joj možemo ukloniti neku od moguænosti
			    	if (!sameBoxCells.contains(notInSet) && notInSet != firstCell) {
			    		for (int val = 0; val < cols; val++) {
			    			// Ako æelija sadrži moguæu vrijednost koju pokriva ogoljeni skup, možemo ukloniti tu moguænost
			    			if (matchString.charAt(val) == '1' && possibilities[notInSet][val] == 1 && temporary[notInSet] == 0) {
			    				// Isti uoèeni ogoljeni skup boduje se samo jednom, iako rezultira uklanjanjem više moguænosti
			    				if (numRemoved == 0) {
			    					difficultySettingNaked(sameBoxCells, matchString, boxNumber[firstCell], "stupcu", firstCell);
			    				}
			    				// Dodajemo novi red za uklanjanje moguænosti u tekst uputa
			    				solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").\n";
			    				// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
			    				if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(notInSet / cols + 1) + ", " + String.valueOf(notInSet % cols + 1) + ").", "Rješavaè")) {
			    	    				showSteps = false;
			    	    			}
					    		}
			    				possibilities[notInSet][val] = 0;
						    	// Ukloni moguænosti prema odnosima veæe-manje
					    		Set<Integer> visitedMax = new HashSet<Integer>();
								setMaxPossibility(notInSet, visitedMax);
						    	// Ukloni moguænosti prema odnosima manje-veæe
					    		Set<Integer> visitedMin = new HashSet<Integer>();
								setMinPossibility(notInSet, visitedMin);
					    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
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
	// Tražimo ogoljeni skup u svim retcima, stupcima i kutijama
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
		    	// Tražimo ogoljeni skup u redu, s ishodištem u odreðenoj æeliji
		    	if (nakedSetForRow(row, col, sameRow) == 1) {
					return 1;
				}
		    	// Tražimo ogoljeni skup u stupcu, s ishodištem u odreðenoj æeliji
		    	Set<Integer> sameColumn = new HashSet<Integer>();
		    	if (nakedSetForCol(row, col, sameColumn) == 1) {
					return 1;
				}
		    	// Tražimo ogoljeni skup u kutiji, s ishodištem u odreðenoj æeliji
		    	Set<Integer> sameBox = new HashSet<Integer>();
		    	if (nakedSetForBox(row, col, sameBox) == 1) {
					return 1;
		    	}
	    	}
	    }
		return 0;
	}
	// Polje znakovnih nizova koji predstavljaju moguæe pozicije definirane vrijednosti unutar odreðenog reda, stupca ili kutije i sastoje se od 0 (moguæe) ili 1 (nije moguæe)
	String valuePos[] = new String[cols];
	// Broj korištenja za metodu skrivenih skupova po velièini skupa
	int us2 = 0;
	int us3 = 0;
	int us4 = 0;
	// Dodaje se trošak za metode skrivenog skupa i linija na ispis uputa za rješavanje
	// setValues - skup vrijednosti koje su pokrivene skrivenim skupom, matchString - popis æelija koje pokriva skriveni skup, znakovni niz 0 i 1 koje oznaèavaju je li znamenka ukljuèena
	// matchStringLen - broj æelija koje pokriva skriveni skup
	// containerNum - broj reda, stupca ili kutije u kojem se nalazi skriveni skup, containerType - string koji oznaèava je li skriveni skup u redu, stupcu ili kutiji
	// firstVal - ishodišna vrijednost skrivenog skupa kojoj smo tražili vrijednosti koje se nalaze kao moguænosti u nekoj od istih æelija kao i ona
	public void difficultySettingHidden(Set<Integer> setValues, String matchString, int matchStringLen, int containerNum, String containerType, int firstVal) {
		String setType = "Skriven";
		String lineSolvInstr = "";
		// Dio koda za skriveni par
		if (setValues.size() == 1) {
			// Dodavanje vrijednosti skupa u upute za rješavanje
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
			// Ako smo veæ registrirali ovaj isti skriveni par, ne bodujemo ga dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (us2 == 0) {
    				// Kada prvi put naðemo skriveni par, trošak je 1500
    				difficultyScore += 1500;
    				us2 = 1;
    			} else {
    				// Kada iduæi put naðemo skriveni par, trošak je 1200
    				difficultyScore += 1200;
    			}
    		} else {
    			return;
    		}
		}
		// Dio koda za skrivenu trojku
		if (setValues.size() == 2) {
			// Dodavanje vrijednosti skupa u upute za rješavanje
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
			// Ako smo veæ registrirali ovu istu skrivenu trojku, ne bodujemo ju dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (us3 == 0) {
    				// Kada prvi put naðemo skrivenu trojku, trošak je 2400
    				difficultyScore += 2400;
    				us3 = 1;
    			} else {
    				// Kada iduæi put naðemo skrivenu trojku, trošak je 1600
    				difficultyScore += 1600;
    			}
    		} else {
    			return;
    		}
		}
		// Dio koda za skrivenu èetvorku
		if (setValues.size() == 3) {
			// Dodavanje vrijednosti skupa u upute za rješavanje
			lineSolvInstr += setType + "a èetvorka u " + containerType + " " + String.valueOf(containerNum + 1) + ", vrijednosti";
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
			// Ako smo veæ registrirali ovu istu skrivenu èetvorku, ne bodujemo ju dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (us4 == 0) {
    				// Kada prvi put naðemo skrivenu èetvorku, trošak je 7000
    				difficultyScore += 7000;
    				us4 = 1;
    			} else {
    				// Kada iduæi put naðemo skrivenu èetvorku, trošak je 5000
    				difficultyScore += 5000;
    			}
    		} else {
    			return;
    		}
		}
		// Dio koda za skriveni skup veæi od èetiri
		if (setValues.size() > 3) {
			// Dodavanje vrijednosti skupa u upute za rješavanje
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
			// Ako smo veæ registrirali ovaj istu skriveni skup veæi od èetiri, ne bodujemo ga dvaput
    		if (!solvingInstructions.contains(lineSolvInstr)) {
    			if (us4 == 0) {
    				// Kada prvi put naðemo skriveni skup veæi od 4, trošak je 7000 (broji se kao i skrivena èetvorka)
    				difficultyScore += 7000;
    				us4 = 1;
    			} else {
    				// Kada iduæi put naðemo skriveni skup veæi od 4, trošak je 5000 (broji se kao i skrivena èetvorka)
    				difficultyScore += 5000;
    			}
    		} else {
    			return;
    		}
		}
		lineSolvInstr += " u æelijama";
		int sizeOfSet = 0;
		// Dodavanje æelija skupa u upute za rješavanje (sluèaj skupa u redu)
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
		// Dodavanje æelija skupa u upute za rješavanje (sluèaj skupa u stupcu)
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
		// Dodavanje æelija skupa u upute za rješavanje (sluèaj skupa u kutiji)
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
    	// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
    	if (showSteps == true) {
		    instructionArea.setText(solvingInstructions);
		    print();
			if (!InformationBox.stepBox(lineSolvInstr, "Rješavaè")) {
				showSteps = false;
			}
		}
	}
	// Tražimo skriveni skup u retcima
	// firstVal - ishodišna vrijednost skrivenog skupa kojoj smo tražili vrijednosti koje se nalaze kao moguænosti u nekoj od istih æelija kao i ona
	// firstRow - redak u kojem tražimo skriveni skup
	// setRowValues - skup vrijednosti koji bi mogli biti pokrivene skrivenim skupom u redu
	public int hiddenSetForRow(int firstVal, int firstRow, Set<Integer> sameRowValues) {
		// Ako je skup veæi od ogranièene duljine, nastavljamo dalje
		if (sameRowValues.size() >= depthLimit) {
			return 0;
		}
    	// Pretražujemo sve nove vrijednosti veæe od ishodišne, da izbjegnemo ponavljanje
		for (int newToSet = firstVal + 1; newToSet < cols; newToSet++) {
			if (newToSet == firstVal || sameRowValues.contains(newToSet)) {
    			continue;
    		}
    		int match = 0;
    		// Pretražujemo sve vrijednosti koje su veæ u skrivenom skupu
    		for (int alreadyInSet = 0; alreadyInSet < cols; alreadyInSet++) {
    			if (alreadyInSet == firstVal || sameRowValues.contains(alreadyInSet)) {
		    		for (int positionInRow = 0; positionInRow < cols; positionInRow++) {
		    			// Provjeravamo podudara li se nova vrijednost sa svakom od vrijednosti koje bi mogle biti pokrivene skrivenim skupom i s ishodišnom vrjednošæu u barem jednoj od moguæih æelija
		    			if (valuePos[newToSet].charAt(positionInRow) == '1' && valuePos[alreadyInSet].charAt(positionInRow) == '1') {
		    				match++;
		    				break;
		    			}
		    		}
    			}
    		}
    		// Ako se nova vrijednost nalazi kao moguænosti u zajednièkoj æeliji sa svakom od vrijednosti u skrivenom skupu i s ishodišnom vrijednošæu, možemo ju dodati u skriveni skup
    		if (match == sameRowValues.size() + 1) {
    			Set<Integer> sameRowValuesNextIteration = new HashSet<Integer>();
        		for (int val = 0; val < cols; val++) {
        			if (sameRowValues.contains(val)) {
        				sameRowValuesNextIteration.add(val);
        			}
        		}
        		sameRowValuesNextIteration.add(newToSet);
        		// Za svaku æeliju koju dodajemo u skriveni skup pokreæemo rekurziju, a u postojeæemo pozivu funkcije nastavljamo bez da ju dodamo
				if (hiddenSetForRow(firstVal, firstRow, sameRowValuesNextIteration) == 1) {
	    			return 1;
				}
    		}
    		// Preborajavamo æelije koje pokrivaju vrijednosti skrivenog skupa u obliku niza znakova 0 i 1
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int col = 0; col < cols; col++) {
	    		String containsCell = "0";
	    		for (int val = 0; val < cols; val++) {
	    			if (val == firstVal || sameRowValues.contains(val)) {
	    				// Ako neka od vrijednosti u skrivenom skupu može biti u odreðenom stupcu unutar reda, dodajemo znak 1
				    	if (valuePos[val].charAt(col) == '1') {
				    		containsCell = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsCell;
    		}
	    	// Ako skriveni skup pokriva jednako vrijednosti koliko sadrži æelija te nije prazan skup, to je ispravan skriveni skup
	    	if (sameRowValues.size() == matchStringLen - 1 && sameRowValues.size() > 0) {
	    		int numRemoved = 0;
				for (int col = 0; col < cols; col++) {
			    	// Ako je æelija u skrivenom skupu možda joj možemo ukloniti neku od moguænosti
					if (matchString.charAt(col) == '1') {
						 for (int val = 0; val < cols; val++) {
				    		// Ako æelija sadrži moguæu vrijednost koju ne pokriva skriveni skup, možemo ukloniti tu moguænost
							if (!sameRowValues.contains(val) && val != firstVal && possibilities[firstRow * cols + col][val] == 1) {
			    				// Isti uoèeni skriveni skup boduje se samo jednom, iako rezultira uklanjanjem više moguænosti
						    	if (numRemoved == 0) {
						    		difficultySettingHidden(sameRowValues, matchString, matchStringLen, firstRow, "redu", firstVal);
						    	}		    			    		
						    	// Dodajemo novi red za uklanjanje moguænosti u tekst uputa
								solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(firstRow + 1) + ", " + String.valueOf(col + 1) + ").\n" ;
								// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(firstRow + 1) + ", " + String.valueOf(col + 1) + ").", "Rješavaè")) {
			    	    				showSteps = false;
			    	    			}
					    		}
			    				possibilities[firstRow * cols + col][val] = 0;
						    	// Ukloni moguænosti prema odnosima veæe-manje
					    		Set<Integer> visitedMax = new HashSet<Integer>();
								setMaxPossibility(firstRow * cols + col, visitedMax);
						    	// Ukloni moguænosti prema odnosima manje-veæe
					    		Set<Integer> visitedMin = new HashSet<Integer>();
								setMinPossibility(firstRow * cols + col, visitedMin);
					    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
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
	// Tražimo skriveni skup u stupcima
	// firstVal - ishodišna vrijednost skrivenog skupa kojoj smo tražili vrijednosti koje se nalaze kao moguænosti u nekoj od istih æelija kao i ona
	// firstCol - stupac u kojem tražimo skriveni skup
	// sameColValues - skup vrijednosti koji bi mogli biti pokrivene skrivenim skupom u stupcu
	public int hiddenSetForCol(int firstVal, int firstCol, Set<Integer> sameColValues) {	
		// Ako je skup veæi od ogranièene duljine, nastavljamo dalje
		if (sameColValues.size() >= depthLimit) {
			return 0;
		}
    	// Pretražujemo sve nove vrijednosti veæe od ishodišne, da izbjegnemo ponavljanje
		for (int newToSet = firstVal + 1; newToSet < cols; newToSet++) {
			if (newToSet == firstVal || sameColValues.contains(newToSet)) {
				continue;
			}
			int match = 0;
    		// Pretražujemo sve vrijednosti koje su veæ u skrivenom skupu
			for (int alreadyInSet = 0; alreadyInSet < cols; alreadyInSet++) {
				if (alreadyInSet == firstVal || sameColValues.contains(alreadyInSet)) {
		    		for (int positionInCol = 0; positionInCol < cols; positionInCol++) {
		    			// Provjeravamo podudara li se nova vrijednost sa svakom od vrijednosti koje bi mogle biti pokrivene skrivenim skupom i s ishodišnom vrjednošæu u barem jednoj od moguæih æelija
		    			if (valuePos[newToSet].charAt(positionInCol) == '1' && valuePos[alreadyInSet].charAt(positionInCol) == '1') {
		    				match++;
		    				break;
		    			}
		    		}
				}
			}
    		// Ako se nova vrijednost nalazi kao moguænosti u zajednièkoj æeliji sa svakom od vrijednosti u skrivenom skupu i s ishodišnom vrijednošæu, možemo ju dodati u skriveni skup
    		if (match == sameColValues.size() + 1) {
    			Set<Integer> sameColValuesNextIteration = new HashSet<Integer>();
        		for (int val = 0; val < cols; val++) {
        			if (sameColValues.contains(val)) {
        				sameColValuesNextIteration.add(val);
        			}
        		}
        		sameColValuesNextIteration.add(newToSet);
        		// Za svaku æeliju koju dodajemo u skriveni skup pokreæemo rekurziju, a u postojeæemo pozivu funkcije nastavljamo bez da ju dodamo
				if (hiddenSetForCol(firstVal, firstCol, sameColValuesNextIteration) == 1) {
	    			return 1;
				}
    		}
    		// Preborajavamo æelije koje pokrivaju vrijednosti skrivenog skupa u obliku niza znakova 0 i 1
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int row = 0; row < rows; row++) {
	    		String containsCell = "0";
	    		for (int val = 0; val < cols; val++) {
	    			if (val == firstVal || sameColValues.contains(val)) {
	    				// Ako neka od vrijednosti u skrivenom skupu može biti u odreðenom redu unutar stupca, dodajemo znak 1
				    	if (valuePos[val].charAt(row) == '1') {
				    		containsCell = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsCell;
    		}
	    	// Ako skriveni skup pokriva jednako vrijednosti koliko sadrži æelija te nije prazan skup, to je ispravan skriveni skup
	    	if (sameColValues.size() == matchStringLen - 1 && sameColValues.size() > 0) {
				int numRemoved = 0;
				for (int row = 0; row < rows; row++) {
			    	// Ako je æelija u skrivenom skupu možda joj možemo ukloniti neku od moguænosti
					if (matchString.charAt(row) == '1') {
						for (int val = 0; val < cols; val++) {
				    		// Ako æelija sadrži moguæu vrijednost koju ne pokriva skriveni skup, možemo ukloniti tu moguænost
							if (!sameColValues.contains(val) && val != firstVal && possibilities[row * cols + firstCol][val] == 1) {
			    				// Isti uoèeni skriveni skup boduje se samo jednom, iako rezultira uklanjanjem više moguænosti
						    	if (numRemoved == 0) {
						    		difficultySettingHidden(sameColValues, matchString, matchStringLen, firstCol, "stupcu", firstVal);
						    	}		    			    	
						    	// Dodajemo novi red za uklanjanje moguænosti u tekst uputa
								solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(firstCol + 1) + ").\n";
								// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(firstCol + 1) + ").", "Rješavaè")) {
			    	    				showSteps = false;
			    	    			}
					    		}
								possibilities[row * cols + firstCol][val] = 0;
						    	// Ukloni moguænosti prema odnosima veæe-manje
					    		Set<Integer> visitedMax = new HashSet<Integer>();
								setMaxPossibility(row * cols + firstCol, visitedMax);
						    	// Ukloni moguænosti prema odnosima manje-veæe
					    		Set<Integer> visitedMin = new HashSet<Integer>();
								setMinPossibility(row * cols + firstCol, visitedMin);
					    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
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
	// Tražimo skriveni skup u kutijama
	// firstVal - ishodišna vrijednost skrivenog skupa kojoj smo tražili vrijednosti koje se nalaze kao moguænosti u nekoj od istih æelija kao i ona
	// firstBox - kutija u kojoj tražimo skriveni skup
	// sameBoxValues - skup vrijednosti koji bi mogli biti pokrivene skrivenim skupom u kutiji
	public int hiddenSetForBox(int firstVal, int firstBox, Set<Integer> sameBoxValues) {	
		// Ako je skup veæi od ogranièene duljine, nastavljamo dalje
		if (sameBoxValues.size() >= depthLimit) {
			return 0;
		}
    	// Pretražujemo sve nove vrijednosti veæe od ishodišne, da izbjegnemo ponavljanje
		for (int newToSet = firstVal + 1; newToSet < cols; newToSet++) {
			if (newToSet == firstVal || sameBoxValues.contains(newToSet)) {
				continue;
			}
			int match = 0;
    		// Pretražujemo sve vrijednosti koje su veæ u skrivenom skupu
			for (int alreadyInSet = 0; alreadyInSet < cols; alreadyInSet++) {
				if (alreadyInSet == firstVal || sameBoxValues.contains(alreadyInSet)) {
		    		for (int positionInBox = 0; positionInBox < cols; positionInBox++) {
		    			// Provjeravamo podudara li se nova vrijednost sa svakom od vrijednosti koje bi mogle biti pokrivene skrivenim skupom i s ishodišnom vrjednošæu u barem jednoj od moguæih æelija
		    			if (valuePos[newToSet].charAt(positionInBox) == '1' && valuePos[alreadyInSet].charAt(positionInBox) == '1') {
		    				match++;
		    				break;
		    			}
		    		}
				}
			}
    		// Ako se nova vrijednost nalazi kao moguænosti u zajednièkoj æeliji sa svakom od vrijednosti u skrivenom skupu i s ishodišnom vrijednošæu, možemo ju dodati u skriveni skup
    		if (match == sameBoxValues.size() + 1) {
    			Set<Integer> sameBoxValuesNextIteration = new HashSet<Integer>();
        		for (int val = 0; val < cols; val++) {
        			if (sameBoxValues.contains(val)) {
        				sameBoxValuesNextIteration.add(val);
        			}
        		}
        		sameBoxValuesNextIteration.add(newToSet);
        		// Za svaku æeliju koju dodajemo u skriveni skup pokreæemo rekurziju, a u postojeæemo pozivu funkcije nastavljamo bez da ju dodamo
				if (hiddenSetForBox(firstVal, firstBox, sameBoxValuesNextIteration) == 1) {
	    			return 1;
				}
			}
    		// Preborajavamo æelije koje pokrivaju vrijednosti skrivenog skupa u obliku niza znakova 0 i 1
	    	int matchStringLen = 0;
	    	String matchString = "";
	    	for (int box = 0; box < cols; box++) {
	    		String containsCell = "0";
	    		for (int val = 0; val < cols; val++) {
	    			if (val == firstVal || sameBoxValues.contains(val)) {
	    				// Ako neka od vrijednosti u skrivenom skupu može biti u odreðenoj æeliji unutar kutije, dodajemo znak 1
				    	if (valuePos[val].charAt(box) == '1') {
				    		containsCell = "1";
				    		matchStringLen++;
				    		break;
				    	}
	    			}
	    		}
	    		matchString += containsCell;
    		}
	    	// Ako skriveni skup pokriva jednako vrijednosti koliko sadrži æelija te nije prazan skup, to je ispravan skriveni skup
	    	if (sameBoxValues.size() == matchStringLen - 1 && sameBoxValues.size() > 0) {
				int cellPositionInBox = -1;
				int numRemoved = 0;
				for (int numCell = 0; numCell < rows * cols; numCell++) {
		    		if (firstBox != boxNumber[numCell]) {
		    			continue;
		    		} else {
		    			cellPositionInBox++;
		    		}
			    	// Ako je æelija u skrivenom skupu možda joj možemo ukloniti neku od moguænosti
					if (matchString.charAt(cellPositionInBox) == '1') {
						for (int notInSet = 0; notInSet < cols; notInSet++) {
				    		// Ako æelija sadrži moguæu vrijednost koju ne pokriva skriveni skup, možemo ukloniti tu moguænost
							if (!sameBoxValues.contains(notInSet) && notInSet != firstVal && possibilities[numCell][notInSet] == 1) {
			    				// Isti uoèeni skriveni skup boduje se samo jednom, iako rezultira uklanjanjem više moguænosti
						    	if (numRemoved == 0) {
						    		difficultySettingHidden(sameBoxValues, matchString, matchStringLen, firstBox, "kutiji", firstVal);
						    	}		 
						    	// Dodajemo novi red za uklanjanje moguænosti u tekst uputa
								solvingInstructions += "Uklanjam moguænost " + String.valueOf(notInSet + 1) + " iz æelije (" + String.valueOf(numCell / cols + 1) + ", " + String.valueOf(numCell % cols + 1) + ").\n";
								// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
								if (showSteps == true) {
					    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Uklanjam moguænost " + String.valueOf(notInSet + 1) + " iz æelije (" + String.valueOf(numCell / cols + 1) + ", " + String.valueOf(numCell % cols + 1) + ").", "Rješavaè")) {
			    	    				showSteps = false;
			    	    			}
					    		}
								possibilities[numCell][notInSet] = 0;
						    	// Ukloni moguænosti prema odnosima veæe-manje
					    		Set<Integer> visitedMax = new HashSet<Integer>();
								setMaxPossibility(numCell, visitedMax);
						    	// Ukloni moguænosti prema odnosima manje-veæe
					    		Set<Integer> visitedMin = new HashSet<Integer>();
								setMinPossibility(numCell, visitedMin);
					    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
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
	// Tražimo skriveni skup u svim retcima, stupcima i kutijama
	public int hiddenSet() {
	    for (int row = 0; row < rows; row++){
	    	// Pamtimo znakovni niz 0 i 1 ovisno o tome može li se znamenka nalaziti u odreðenom stupcu unutar definiranog reda
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
	    	// Tražimo skriveni skup koji sadrži ishodišnu vrijednost unutar definiranog reda
			for (int val = 0; val < cols; val++) {
				Set<Integer> sameRowValues = new HashSet<Integer>();
				if (hiddenSetForRow(val, row, sameRowValues) == 1) {
		    		return 1;
				}
			}
		}
	    for (int col = 0; col < cols; col++){
	    	// Pamtimo znakovni niz 0 i 1 ovisno o tome može li se znamenka nalaziti u odreðenom redu unutar definiranog stupca
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
	    	// Tražimo skriveni skup koji sadrži ishodišnu vrijednost unutar definiranog stupca
			for (int val = 0; val < cols; val++) {
				Set<Integer> sameColValues = new HashSet<Integer>();
				if (hiddenSetForCol(val, col, sameColValues) == 1) {
    	    		return 1;
				}
			}
		}
	    for (int box = 0; box < cols; box++){
	    	// Pamtimo znakovni niz 0 i 1 ovisno o tome može li se znamenka nalaziti u odreðenoj æeliji unutar definirane kutije
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
	    	// Tražimo skriveni skup koji sadrži ishodišnu vrijednost unutar definirane kutije
			for (int val = 0; val < cols; val++) {
				Set<Integer> sameBoxValues = new HashSet<Integer>();
				if (hiddenSetForBox(val, box, sameBoxValues) == 1) {
    	    		return 1;
				}
			}
	    }
		return 0;
	}
	// Broj korištenja za metodu linija kandidata
    int clt = 0;
    //Tražimo linije kandidata u kutiji
	public int candidateLines() {
		// Pretražujemo sve kutije
	    for (int box = 0; box < cols; box++){
	    	// Za svaku vrijednost zapisujemo u kojem se redu može nalaziti unutar kutije
	    	int valueRow[] = new int[cols];
	    	// Za svaku vrijednost zapisujemo u kojem se stupcu nalazi nalaziti unutar kutije
	    	int valueCol[] = new int[cols];
	    	// Na poèetku se vriejdnost ne nalazi nigdje unutar kutije (oznaka -1)
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
    	    			// Ako nova ponaðena pojava vrijednosti unutar kutije nije u istom redu, nemamo liniju kandidata u tom redu u toj kutiji za tu vrijednost (oznaka -2)
    	    			if (valueRow[val] != numCell / cols) {
	    	    			valueRow[val] = -2;
    	    			}
    	    		}
    	    		// Zapisujemo u kojem stupcu se nalazi prva pojava neke vrijednosti unutar kutije
    	    		if (valueCol[val] == -1) {
    	    			valueCol[val] = numCell % cols;
    	    		} else {
	    				// Ako nova ponaðena pojava vrijednosti unutar kutije nije u istom stupcu, nemamo liniju kandidata u tom stupcu u toj kutiji za tu vrijednost (oznaka -2)
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
	    				// Ako se vrijednost za koju imamo liniju kandidata u redu u nekoj kutiji pojavi u drugoj kutiji u tom istom redu, može možemo ukloniti tu moguænost
	    				if (possibilities[valueRow[val] * cols + col][val] == 1 && boxNumber[valueRow[val] * cols + col] != box) {
		    				// Ista uoèena linija kandidata boduje se samo jednom, iako rezultira uklanjanjem više moguænosti
	    					if (numRemoved == 0) {
	    			    		String lineSolvInstr = "Kandidati za " + String.valueOf(val + 1) + " u kutiji " + String.valueOf(box + 1) + " svi u redu " + String.valueOf(valueRow[val] + 1) + ".\n";
	    			    		// Ako smo veæ registrirali ovu istu liniju (red) kandidata u kutiji, ne bodujemo ju dvaput
	    			    		if (!solvingInstructions.contains(lineSolvInstr)) {
    				    			if (clt == 0) {
    				    				// Kada prvi put naðemo liniju kandidata, trošak je 350
    	    		    				difficultyScore += 350;
    	    		    				clt = 1;
    	    		    			} else {
    				    				// Kada iduæi put naðemo liniju kandidata, trošak je 200
    	    		    				difficultyScore += 200;
    	    		    			}		
    				    			// Dodajemo novi red u tekst uputa
    				    			solvingInstructions += lineSolvInstr;
    				    			// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
							    	if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
						    		    print();
				    	    			if (!InformationBox.stepBox(lineSolvInstr, "Rješavaè")) {
				    	    				showSteps = false;
				    	    			}
						    		}
    				    		}
	    					}		
	    					// Dodajemo novi red za uklanjanje moguænosti u tekst uputa
	    					solvingInstructions +=  "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(valueRow[val] + 1) + ", " + String.valueOf(col + 1) + ").\n";
	    					// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
	    					if (showSteps == true) {
				    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			if (!InformationBox.stepBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(valueRow[val] + 1) + ", " + String.valueOf(col + 1) + ").", "Rješavaè")) {
		    	    				showSteps = false;
		    	    			}
				    		}
		    				possibilities[valueRow[val] * cols + col][val] = 0;
					    	// Ukloni moguænosti prema odnosima veæe-manje
				    		Set<Integer> visitedMax = new HashSet<Integer>();
							setMaxPossibility(valueRow[val] * cols + col, visitedMax);
					    	// Ukloni moguænosti prema odnosima manje-veæe
				    		Set<Integer> visitedMin = new HashSet<Integer>();
							setMinPossibility(valueRow[val] * cols + col, visitedMin);
				    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
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
	    				// Ako se vrijednost za koju imamo liniju kandidata u stupcu u nekoj kutiji pojavi u drugoj kutiji u tom istom stupcu, može možemo ukloniti tu moguænost
	    				if (possibilities[row * cols + valueCol[val]][val] == 1 && boxNumber[row * cols + valueCol[val]] != box) {
		    				// Ista uoèena linija kandidata boduje se samo jednom, iako rezultira uklanjanjem više moguænosti
    	    				if (numRemoved == 0) {
    				    		String lineSolvInstr = "Kandidati za " + String.valueOf(val + 1) + " u kutiji " + String.valueOf(box + 1) + " svi su u stupcu " + String.valueOf(valueCol[val] + 1) + ".\n";
    				    		// Ako smo veæ registrirali ovu istu liniju (stupac) kandidata u kutiji, ne bodujemo ju dvaput
    				    		if (!solvingInstructions.contains(lineSolvInstr)) {
    				    			if (clt == 0) {
    				    				// Kada prvi put naðemo liniju kandidata, trošak je 350
    	    		    				difficultyScore += 350;
    	    		    				clt = 1;
    	    		    			} else {
    				    				// Kada iduæi put naðemo liniju kandidata, trošak je 200
    	    		    				difficultyScore += 200;
    	    		    			}		
    				    			// Dodajemo novi red u tekst uputa
    				    			solvingInstructions += lineSolvInstr;
    				    			// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
							    	if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
						    		    print();
				    	    			if (!InformationBox.stepBox(lineSolvInstr, "Rješavaè")) {
				    	    				showSteps = false;
				    	    			}
						    		}
    				    		}
    	    				}		
    	    				// Dodajemo novi red za uklanjanje moguænosti u tekst uputa
	    					solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(valueCol[val] + 1) + ").\n";
	    					// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
	    					if (showSteps == true) {
				    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			if (!InformationBox.stepBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(valueCol[val] + 1) + ").", "Rješavaè")) {
		    	    				showSteps = false;
		    	    			}
				    		}
		    				possibilities[row * cols + valueCol[val]][val] = 0;
					    	// Ukloni moguænosti prema odnosima veæe-manje
				    		Set<Integer> visitedMax = new HashSet<Integer>();
							setMaxPossibility(row * cols + valueCol[val], visitedMax);
					    	// Ukloni moguænosti prema odnosima manje-veæe
				    		Set<Integer> visitedMin = new HashSet<Integer>();
							setMinPossibility(row * cols + valueCol[val], visitedMin);
				    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
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
	// Oznaka jesmo li ogranièeni na dvosturke parove ili tražimo skup više linija kandidata bilo koje velièine
	boolean widthLimit;
	// Broj korištenja za metodu više linija kandidata
	int mlt = 0;
	// Broj korištenja za metodu dvostrukih parova
	int dpt = 0;
	// Tražimo više linija kandidata kroz više kutija
	public int multipleLines() {
		// Pamtimo može li u odreðenoj kutiji odreðena vrijednost biti u odreðenom redu
    	Integer valueRow[][][] = new Integer[cols][cols][rows];
		// Pamtimo može li u odreðenoj kutiji odreðena vrijednost biti u odreðenom stupcu
    	Integer valueCol[][][] = new Integer[cols][cols][cols];
		// Znakovni niz 0 i 1 koji predstavlja može li u odreðenoj kutiji odreðena vrijednost biti u odreðenom redu
    	String valueRowString[][] = new String[cols][cols];
		// Znakovni niz 0 i 1 koji predstavlja može li u odreðenoj kutiji odreðena vrijednost biti u odreðenom stupcu
    	String valueColString[][] = new String[cols][cols];
		// Pamtimo za svaku kutiju u koliko razlièitih redova se može nalaziti odreðena vrijednost
    	Integer valueRowNum[][] = new Integer[cols][cols];
		// Pamtimo za svaku kutiju u koliko razlièitih stupaca se može nalaziti odreðena vrijednost
    	Integer valueColNum[][] = new Integer[cols][cols];
		// Pamtimo koliko æelija kandidata ima u odreðenoj kutiji za odreðenu vrijednost
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
	    	// Ažuriramo saznanja može li u odreðenoj kutiji odreðena vrijednost biti u odreðenom redu ili stupcu
	    	// Istodobno prebrojavamo koliko æelija kandidata ima u odreðenoj kutiji za odreðenu vrijednost
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
	    // Pretvaramo informacije može li u odreðenoj kutiji odreðena vrijednost biti u odreðenom redu ili u odreðenom stupcu iz polja u znakovni niz 0 i 1
	    // Istodobno za svaku kutiju prebrojavamo u koliko razlièitih redova ili stupaca se može nalaziti odreðena vrijednost
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
    	// Nalazimo poèetnu kutiju
    	for (int firstBox = 0; firstBox < cols; firstBox++) {
    		// Prolazimo sve vrijednosti u poèetnoj kutiji
    		for (int val = 0; val < cols; val++){
    			// Pamtimo sve kutije koje se podudaraju s poèetnom u redovima kandidatima za odreðenu vrijednost
				Set<Integer> sameRowBoxes = new HashSet<Integer>();
    			// Pamtimo sve kutije koje se podudaraju s poèetnom u stupcima kandidatima za odreðenu vrijednost
				Set<Integer> sameColBoxes = new HashSet<Integer>();
				// Prolazimo sve kutije veæeg broja od poèetne da izbjegnemo ponavljanje
		    	for (int matchingBox = firstBox + 1; matchingBox < cols; matchingBox ++) {
		    		// Kutije se moraju podudarati u redovima kandidatima za odreðenu vrijednost i broj kandidata u svakoj od kutija je dva za dvostruki par i veæi od dva za više linija kandidata
			    	if (valueRowString[firstBox][val].compareTo(valueRowString[matchingBox][val]) == 0 && matchingBox != firstBox && ((widthLimit && valueCandidates[firstBox][val] == 2 && valueCandidates[matchingBox][val] == 2) || (!widthLimit && (valueCandidates[firstBox][val] > 2 || valueCandidates[matchingBox][val] > 2)))) {
						sameRowBoxes.add(matchingBox);
			    	}
		    		// Kutije se moraju podudarati u stupcima kandidatima za odreðenu vrijednost i broj kandidata u svakoj od kutija je dva za dvostruki par i veæi od dva za više linija kandidata
			    	if (valueColString[firstBox][val].compareTo(valueColString[matchingBox][val]) == 0 && matchingBox != firstBox && ((widthLimit && valueCandidates[firstBox][val] == 2 && valueCandidates[matchingBox][val] == 2) || (!widthLimit && (valueCandidates[firstBox][val] > 2 || valueCandidates[matchingBox][val] > 2)))) {
						sameColBoxes.add(matchingBox);
			    	}
		    	}
		    	// Ako je broj kutija koje se podudaraju u redovima kandidatima za odreðenu vrijednost jednak broju redova kandidata, našli smo dvostruki par ili više linija kandidata
		    	if (sameRowBoxes.size() == valueRowNum[firstBox][val] - 1 && sameRowBoxes.size() > 0) {
		    		int numRemoved = 0;
					for (int row = 0; row < rows; row++) {
						if (valueRow[firstBox][val][row] == 1) {
							for (int col = 0; col < cols; col++) {
								// Ako kutija izvan onih koje se podudaraju u redovima kandidatima za odreðenu vrijednost sadrži tu vrijednost u nekom od tih redova, možemo ukloniti tu moguænost
								if (boxNumber[row * cols + col] != firstBox && !sameRowBoxes.contains(boxNumber[row * cols + col]) && possibilities[row * cols + col][val] == 1) {
									// Isti uoèeni dvostruki par ili više linija kandidata boduje se samo jednom, iako rezultira uklanjanjem više moguænosti
									if (numRemoved == 0) {
										String lineSolvInstr = "";
										if (widthLimit) {
											// Ako smo ogranièili broj kandidata u liniji, tražimo dvostruki par
											lineSolvInstr = "Dvostruki par u redovima";
										} else {
											// Ako je broj kandidata u liniji neogranièen, tražimo više linija kandidata
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
							    	   // Dodajemo vrijednost koju sadrže linije kandidati u liniju ispisa
							    	   lineSolvInstr += " sadrži " + String.valueOf(val + 1) + " u kutijama";
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
										// Ako smo veæ registrirali ovaj isti dvostruki par ili više linija kandidata, ne bodujemo ih dvaput
							    		if (!solvingInstructions.contains(lineSolvInstr)) {
											if (widthLimit) {
								    			if (dpt == 0) {
			    				    				// Kada prvi put naðemo dvostruki par, trošak je 500
								    				difficultyScore += 500;
								    				dpt = 1;
								    			} else {
			    				    				// Kada iduæi put naðemo dvostruki par, trošak je 250
								    				difficultyScore += 250;
								    			}

											} else {
								    			if (mlt == 0) {
			    				    				// Kada prvi put naðemo više linija kandidata, trošak je 700
								    				difficultyScore += 700;
								    				mlt = 1;
								    			} else {
			    				    				// Kada iduæi put naðemo više linija kandidata, trošak je 400
								    				difficultyScore += 400;
								    			}
											}
							    			// Dodajemo novi red u tekst uputa
		    				    			solvingInstructions += lineSolvInstr;
		    				    			// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
									    	if (showSteps == true) {
								    		    instructionArea.setText(solvingInstructions);
								    		    print();
								    		    if (!InformationBox.stepBox(lineSolvInstr, "Rješavaè")) {
								    				showSteps = false;
								    			}
								    		}
		    				    		}
									}
									// Dodajemo novi red za uklanjanje moguænosti u tekst uputa
			    					solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
			    					// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
			    					if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    		    		print();
				    	    			if (!InformationBox.stepBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").", "Rješavaè")) {
				    	    				showSteps = false;
				    	    			}
						    		}
									possibilities[row * cols + col][val] = 0;
							    	// Ukloni moguænosti prema odnosima veæe-manje
						    		Set<Integer> visitedMax = new HashSet<Integer>();
									setMaxPossibility(row * cols + col, visitedMax);
							    	// Ukloni moguænosti prema odnosima manje-veæe
						    		Set<Integer> visitedMin = new HashSet<Integer>();
									setMinPossibility(row * cols + col, visitedMin);
						    		numRemoved++;
						    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
				    				if (sequence() == 1) {
				    	    			return 1;
				    				}
								}
							}
						}
					}
		    	}		    	
		    	// Ako je broj kutija koje se podudaraju u stupcima kandidatima za odreðenu vrijednost jednak broju stupaca kandidata, našli smo dvostruki par ili više linija kandidata
		    	if (sameColBoxes.size() == valueColNum[firstBox][val] - 1 && sameColBoxes.size() > 0) {
		    		int numRemoved = 0;
					for (int col = 0; col < cols; col++) {
						if (valueCol[firstBox][val][col] == 1) {
							for (int row = 0; row < rows; row++) {
								// Ako kutija izvan onih koje se podudaraju u stupcima kandidatima za odreðenu vrijednost sadrži tu vrijednost u nekom od tih stupaca, možemo ukloniti tu moguænost
								if (boxNumber[row * cols + col] != firstBox && !sameColBoxes.contains(boxNumber[row * cols + col]) && possibilities[row * cols + col][val] == 1) {
									// Isti uoèeni dvostruki par ili više linija kandidata boduje se samo jednom, iako rezultira uklanjanjem više moguænosti
									if (numRemoved == 0) {
							    		String lineSolvInstr;
										if (widthLimit) {
											// Ako smo ogranièili broj kandidata u liniji, tražimo dvostruki par
											lineSolvInstr = "Dvostruki par u stupcima";
										} else {
											// Ako je broj kandidata u liniji neogranièen, tražimo više linija kandidata
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
								    	// Dodajemo vrijednost koju sadrže linije kandidati u liniju ispisa
							    		lineSolvInstr += " sadrži " + String.valueOf(val + 1) + " u kutijama";
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
										// Ako smo veæ registrirali ovaj isti dvostruki par ili više linija kandidata, ne bodujemo ih dvaput
							    		if (!solvingInstructions.contains(lineSolvInstr)) {
											if (widthLimit) {
								    			if (dpt == 0) {
			    				    				// Kada prvi put naðemo dvostruki par, trošak je 500
								    				difficultyScore += 500;
								    				dpt = 1;
								    			} else {
			    				    				// Kada iduæi put naðemo dvostruki par, trošak je 250
								    				difficultyScore += 250;
								    			}

											} else {
								    			if (mlt == 0) {
			    				    				// Kada prvi put naðemo više linija kandidata, trošak je 700
								    				difficultyScore += 700;
								    				mlt = 1;
								    			} else {
			    				    				// Kada iduæi put naðemo više linija kandidata, trošak je 400
								    				difficultyScore += 400;
								    			}
											}
							    			// Dodajemo novi red u tekst uputa
							    			solvingInstructions += lineSolvInstr;
							    			// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
									    	if (showSteps == true) {
								    		    instructionArea.setText(solvingInstructions);
								    		    print();
								    		    if (!InformationBox.stepBox(lineSolvInstr, "Rješavaè")) {
								    				showSteps = false;
								    			}
								    		}
		    				    		}
						    		}
									// Dodajemo novi red za uklanjanje moguænosti u tekst uputa
			    					solvingInstructions += "Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
			    					// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
			    					if (showSteps == true) {
						    		    instructionArea.setText(solvingInstructions);
				    		    		print();
				    	    			if (!InformationBox.stepBox("Uklanjam moguænost " + String.valueOf(val + 1) + " iz æelije (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").", "Rješavaè")) {
				    	    				showSteps = false;
				    	    			}
			    					}
									possibilities[row * cols + col][val] = 0;
							    	// Ukloni moguænosti prema odnosima veæe-manje
						    		Set<Integer> visitedMax = new HashSet<Integer>();
									setMaxPossibility(row * cols + col, visitedMax);
							    	// Ukloni moguænosti prema odnosima manje-veæe
						    		Set<Integer> visitedMin = new HashSet<Integer>();
									setMinPossibility(row * cols + col, visitedMin);
						    		numRemoved++;
						    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
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
	// Provjeravamo je li neka æelija jedina moguæa pozicija za odreðenu vrijednost u redu, stupcu ili dijagonali
	public int singlePosition() {
		for (int val = 1; val <= rows; val++) {
			// Pratimo rede u kojima je iskorištena odreðena vrijednost
			int[] usedRows = new int[rows];
			// Pratimo stupce u kojima je iskorištena odreðena vrijednost
			int[] usedCols = new int[cols];
			// Pratimo kutije u kojima je iskorištena odreðena vrijednost
			int[] usedBoxes = new int[rows * cols];
			// Pratimo je li iskorištena odreðena vrijednost u rastuæoj dijagonali
			boolean usedFirstDiagonal = false;
			// Pratimo je li iskorištena odreðena vrijednost u padajuæoj dijagonali
			boolean usedSecondDiagonal = false;
		    for (int row = 0; row < rows; row++){
		    	for (int col = 0; col < cols; col++) {
		    		usedRows[row] = 0;
		    		usedCols[col] = 0;
		    		usedBoxes[row] = 0;
			    }
		    }
			// Ažuriramo redove, stupce, kutije i dijagonale u kojima je iskorištena neka vrijednost
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
			// Postavljamo vrijednost u odreðeni redak
		    for (int row = 0; row < rows; row++){ 
				// Ako je vrijednost veæ postavljena u redak, nastavljamo dalje
		    	if (usedRows[row] == 1) {
		    		continue;
		    	}
		    	int possible = 0;
		    	int colToClear = 0;
				// Pratimo stupce u koje je moguæe postaviti vrijednost unutar reda
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
				// Ako je vrijednost unutar reda moguæe postaviti samo na jednu poziciju, možemo postaviti vrijednost
		    	if (possible == 1) {
		    		// Korištenje jedine moguæe vrijednosti u redu ima trošak 100
		    		difficultyScore += 100;
		    		unset--;
					// Èistimo moguænosti postavljene æelije
			    	for (int valPossible = 0; valPossible < cols; valPossible++) {
			    		possibilities[row * cols + colToClear][valPossible] = 0;
				    }
			    	possibilities[row * cols + colToClear][val - 1] = 1;
					// Ažuriramo redove, stupce, kutije i dijagonale u kojima je iskorištena neka vrijednost
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
	    			solvingInstructions += "Za red " + String.valueOf(row + 1) + ", broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(row + 1) + ", " + String.valueOf(colToClear + 1) + ").\n";
	    			// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
	    			if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			if (!InformationBox.stepBox("Za red " + String.valueOf(row + 1) + ", broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(row + 1) + ", " + String.valueOf(colToClear + 1) + ").", "Rješavaè")) {
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
		    		// Ažuriramo moguæe vrijednosti ostalih æelija
		    		fixPencilmarks();
			    	// Ako smo postavili sve æelije, prekidamo rješavanje
		    		if (unset == 0) {
		    			return 1;
		    		}
		    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
					if (sequence() == 1) {
		    			return 1;
					}
		    	} 
		    }
			// Postavljamo vrijednost u odreðeni stupac
		    for (int col = 0; col < cols; col++){ 
				// Ako je vrijednost veæ postavljena u stupac, nastavljamo dalje
		    	if (usedCols[col] == 1) {
		    		continue;
		    	}
		    	int possible = 0;
		    	int rowToClear = 0;
				// Pratimo redove u koje je moguæe postaviti vrijednost unutar stupca
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
				// Ako je vrijednost unutar stupca moguæe postaviti samo na jednu poziciju, možemo postaviti vrijednost
		    	if (possible == 1) {
		    		// Korištenje jedine moguæe vrijednosti u stupcu ima trošak 100
		    		difficultyScore += 100;
		    		unset--;
					// Èistimo moguænosti postavljene æelije
			    	for (int valPossible = 0; valPossible < cols; valPossible++) {
			    		possibilities[rowToClear * cols + col][valPossible] = 0;
				    }
			    	possibilities[rowToClear * cols + col][val - 1] = 1;
					// Ažuriramo redove, stupce, kutije i dijagonale u kojima je iskorištena neka vrijednost
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
	    			solvingInstructions += "Za stupac " + String.valueOf(col + 1) + ", broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(rowToClear + 1) + ", " + String.valueOf(col + 1) + ").\n";
	    			// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
	    			if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			if (!InformationBox.stepBox("Za stupac " + String.valueOf(col + 1) + ", broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(rowToClear + 1) + ", " + String.valueOf(col + 1) + ").", "Rješavaè")) {
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
		    		// Ažuriramo moguæe vrijednosti ostalih æelija
			    	fixPencilmarks();
			    	// Ako smo postavili sve æelije, prekidamo rješavanje
		    		if (unset == 0) {
		    			return 1;
		    		}
		    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
					if (sequence() == 1) {
		    			return 1;
					}
		    	} 
		    }
			// Postavljamo vrijednost u odreðenu kutiju
		    for (int box = 0; box < cols; box++){ 
				// Ako je vrijednost veæ postavljena u kutiju, nastavljamo dalje
		    	if (usedBoxes[box] == 1) {
		    		continue;
		    	}
		    	int possible = 0;
		    	int cellToClear = 0;
				// Pratimo æelije u koje je moguæe postaviti vrijednost unutar kutije
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
				// Ako je vrijednost unutar kutije moguæe postaviti samo na jednu poziciju, možemo postaviti vrijednost
		    	if (possible == 1) {
		    		// Korištenje jedine moguæe vrijednosti u kutiji ima trošak 100
		    		difficultyScore += 100;
		    		unset--;
					// Èistimo moguænosti postavljene æelije
			    	for (int valPossible = 0; valPossible < cols; valPossible++) {
			    		possibilities[cellToClear][valPossible] = 0;
				    }
			    	possibilities[cellToClear][val - 1] = 1;
					// Ažuriramo redove, stupce, kutije i dijagonale u kojima je iskorištena neka vrijednost
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
	    			solvingInstructions += "Za kutiju " + String.valueOf(boxNumber[cellToClear] + 1) + ", broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").\n";
	    			// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
	    			if (showSteps == true) {
		    		    instructionArea.setText(solvingInstructions);
    		    		print();
    	    			if (!InformationBox.stepBox("Za kutiju " + String.valueOf(boxNumber[cellToClear] + 1) + ", broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").", "Rješavaè")) {
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
		    		// Ažuriramo moguæe vrijednosti ostalih æelija
			    	fixPencilmarks();
			    	// Ako smo postavili sve æelije, prekidamo rješavanje
		    		if (unset == 0) {
		    			return 1;
		    		}
		    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
					if (sequence() == 1) {
		    			return 1;
					}
		    	} 
		    }
		    // Provjeravamo koristimo li pravilo dijagonala
		    if (diagonalOn) {
		    	// Postavljamo vrijednost u rastuæu dijagonalu
				// Ako je vrijednost veæ postavljena u rastuæu dijagonalu, nastavljamo dalje
		    	if (!usedFirstDiagonal) {
		    		int possible = 0;
		    		int cellToClear = 0;
					// Pratimo æelije u koje je moguæe postaviti vrijednost unutar rastuæe dijagonale
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
					// Ako je vrijednost unutar rastuæe dijagonale moguæe postaviti samo na jednu poziciju, možemo postaviti vrijednost
			    	if (possible == 1) {
			    		// Korištenje jedine moguæe vrijednosti u rastuæoj dijagonali ima trošak 100
			    		difficultyScore += 100;
			    		unset--;
						// Èistimo moguænosti postavljene æelije
				    	for (int valPossible = 0; valPossible < cols; valPossible++) {
				    		possibilities[cellToClear][valPossible] = 0;
					    }
				    	possibilities[cellToClear][val - 1] = 1;
						// Ažuriramo redove, stupce, kutije i dijagonale u kojima je iskorištena neka vrijednost
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
		    			solvingInstructions += "Za rastuæu dijagonalu broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").\n";
		    			// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
		    			if (showSteps == true) {
			    		    instructionArea.setText(solvingInstructions);
	    		    		print();
	    	    			if (!InformationBox.stepBox("Za rastuæu dijagonalu broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").", "Rješavaè")) {
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
			    		// Ažuriramo moguæe vrijednosti ostalih æelija
				    	fixPencilmarks();
				    	// Ako smo postavili sve æelije, prekidamo rješavanje
			    		if (unset == 0) {
			    			return 1;
			    		}
			    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
						if (sequence() == 1) {
			    			return 1;
						}
			    	}
		    	}
		    	// Postavljamo vrijednost u padajuæu dijagonalu
				// Ako je vrijednost veæ postavljena u padajuæu dijagonalu, nastavljamo dalje
		    	if (!usedSecondDiagonal) {
		    		int possible = 0;
		    		int cellToClear = 0;
					// Pratimo æelije u koje je moguæe postaviti vrijednost unutar padajuæe dijagonale
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
					// Ako je vrijednost unutar padajuæe dijagonale moguæe postaviti samo na jednu poziciju, možemo postaviti vrijednost
			    	if (possible == 1) {
			    		// Korištenje jedine moguæe vrijednosti u padajuæoj dijagonali ima trošak 100
			    		difficultyScore += 100;
			    		unset--;
						// Èistimo moguænosti postavljene æelije
				    	for (int valPossible = 0; valPossible < cols; valPossible++) {
				    		possibilities[cellToClear][valPossible] = 0;
					    }
				    	possibilities[cellToClear][val - 1] = 1;
						// Ažuriramo redove, stupce, kutije i dijagonale u kojima je iskorištena neka vrijednost
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
		    			solvingInstructions += "Za padajuæu dijagonalu broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").\n";
		    			// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
		    			if (showSteps == true) {
			    		    instructionArea.setText(solvingInstructions);
	    		    		print();
	    	    			if (!InformationBox.stepBox("Za padajuæu dijagonalu broj " + String.valueOf(val) + " je jedino moguæ u æeliji (" + String.valueOf(cellToClear / cols + 1) + ", " + String.valueOf(cellToClear % cols + 1) + ").", "Rješavaè")) {
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
			    		// Ažuriramo moguæe vrijednosti ostalih æelija
				    	fixPencilmarks();
				    	// Ako smo postavili sve æelije, prekidamo rješavanje
			    		if (unset == 0) {
			    			return 1;
			    		}
			    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
						if (sequence() == 1) {
			    			return 1;
						}
			    	}
		    	}
		    }
		}
		return 0;
	}
	// Sekvenca kojom se primjenjuju tehnike, prema složenosti (trošku)
	public int sequence() {
		int impossible = impossibleCheck();
		if (impossible > 0) {
			return 0;
	    }
    	// Pokreæemo traženje jedine poziciju unutar reda, stupca, kutije ili rastuæe ili padajuæe dijagonale
		if (singlePosition() == 1 || unset == 0) {
			return 1;
		}
		// Pokreæemo traženje jedino kandidata u æeliji
		if (singleCandidate() == 1 || unset == 0) {
			return 1;
		}
		// Pokreæemo traženje linija kandidata (redova ili stupaca) u kutiji
		if (candidateLines() == 1 || unset == 0) {
			return 1;
		}
		// Pokreæemo traženje dvostrukih parova u kutijama
		widthLimit = true;
		if (multipleLines() == 1 || unset == 0) {
			return 1;
		}
		// Pokreæemo traženje više linija kandidata (redova ili stupaca) u kutijama
		widthLimit = false;
		if (multipleLines() == 1 || unset == 0) {
			return 1;
		}
		depthLimit = 2;
		// Pokreæemo traženje ogoljenog para
		if (nakedSet() == 1 || unset == 0) {
			return 1;
		}
		// Pokreæemo traženje skrivenog prara
		if (hiddenSet() == 1 || unset == 0) {
			return 1;
		}
		depthLimit = 3;
		// Pokreæemo traženje ogoljene trojke
		if (nakedSet() == 1 || unset == 0) {
			return 1;
		}
		// Pokreæemo traženje skrivene trojke
		if (hiddenSet() == 1 || unset == 0) {
			return 1;
		}
		Stack<Integer> cell = new Stack<Integer>();
		chainLength = 2;
		// Pokreæemo traženje X-krila
		for (int val = 0; val < cols; val++) {
			if (closedChain(cell, -1, 1, 1, val) == 1 || unset == 0) {
				return 1;
			}
			if (closedChain(cell, -1, 1, 0, val) == 1 || unset == 0) {
				return 1;
			}
		}
		// Pokreæemo forsiranje ulanèavanjem
		if (forcingChains() == 1 || unset == 0) {
			return 1;
		} 
		for (int depth = 4; depth < cols; depth++) {
			depthLimit = depth;
			// Pokreæemo traženje ogoljenog skupa velièine 4 ili veæeg
			if (nakedSet() == 1 || unset == 0) {
				return 1;
			}
			// Pokreæemo traženje skrivenog skupa velièine 4 ili veæeg
			if (hiddenSet() == 1 || unset == 0) {
				return 1;
			}
		}
		chainLength = 3;
		// Pokreæemo traženje sabljarke
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
			// Pokreæemo traženje meduze
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
	// Provjeravamo rješivost zagonetke i ažuriramo upute za rješavanje i težinu
	public int isOnlyOneSolution() {
		// Zapisjuemo vrijeme kada smo poèeli rješavati
		startSolving = System.currentTimeMillis();
		// Postavljamo broj korištenja svih tehnika na 0
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
		// Èistimo upute za rješavanje i težinu
		solvingInstructions = "";
		difficultyScore = 0;
	    boolean correct = checkIfCorrect();
	    // Ako zagonetka nije ispravno zadana, ne možemo ju rješavati
    	if (!correct) {
			difficulty.setText("U zagonetki ima grešaka");
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
    	// Za algoritam forsiranja ulanèavanjem pratimo broj posjeta
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	int numCell = row * cols + col;
		    	forceVisited[numCell] = 0;
	    	}
	    }
	    // Inicijaliziramo moguænosti za sve vrijednosti u svim æelijama na 1
	    initPencilmarks();
	    // Ažuriramo moguænosti svih æelija prema pravilima zagonetke
		fixPencilmarks();
		// Sudoku zagonetka 9 * 9 nema jedinstveno rješenje ako je zadano manje od 17 polja i ako nema dijagonale niti odnosa veæe-manje
	    if (cols == 9 && rows * cols - unset < 17 && 0 != unset && diagonalOn == false && sizeRelationships.size() == 0) {
			print();
			difficulty.setText(String.valueOf(unset) + " Zadano je premalo polja");
			return 0;
	    }
		if (sequence() == 1 || unset == 0) {
			solvingInstructions += "Sva polja rješena.\n";
	    	if (showSteps == true) {
			    instructionArea.setText(solvingInstructions);
			    print();
				if (!InformationBox.stepBox( "Sva polja rješena.", "Rješavaè")) {
					showSteps = false;
				}
			}
			// Ako smo postavili sve æelije bez pogaðanja, postoji jedinstveno rješenje
			difficulty.setText(String.valueOf(difficultyScore) + " Postoji jedinstveno rješenje");
			print();
			return 1;
		}
		// Ako nismo postavili konaènu vrijednost u sve æelije, prebrojavamo æelije koje nemaju preostalih moguæih vrijednosti
	    int impossible = impossibleCheck();
		if (impossible > 0) {
	    	// Ako neke æelije nemaju preostalih moguæih vrijednosti, ne postoji naèin rješavanja zagonetke i ona nije ispravna
			difficulty.setText(String.valueOf(impossible) + " Ne postoji rješenje");
			print();
			return 0;
	    }
    	// Ako æelije imaju jednu ili više moguæih vrijednosti, ne postoji jedinstveno rješenje, veæ više njih
		difficulty.setText(String.valueOf(unset) + " Ne postoji jedinstveno rješenje");
		print();
		return 0;
	}

	// Broj korištenja forsiranja ulanèavanjem
	int fct	= 0;
	// Pokušavamo forsirati vrijednosti ulanèavanjem (praæenjem posljedica svakog izbora kroz mrežu)
	public int forcingChains() {
		// Prije nego krenemo prouèavati ishod ako odaberemo bilo koju od moguænosti, moramo zapisati trenutno stanje zbog povratka untrag
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
		    		// U polju pamtimo ishode za sve æelije ako za neku od æelija odaberemo odreðenu moguænost
		    		int [] forcedValues = new int[rows * cols];
		    		// Pratimo koliko puta smo uspjeli odrediti vrijednost neke æelije
		    		int possibilityNum = 0;
		    		for (int val = 0; val < cols; val++) {
		    			// Za svaku od moguænosti u æeliji prouèavamo ishod ako ju odaberemo
			    		if (possibilities[row * cols + col][val] == 1 && temporary[row * cols + col] == 0) {
		    				String lineSolvInstr = "Isprobavam vrijednost " + String.valueOf(val + 1) + " u æeliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").";
	    					solvingInstructions += lineSolvInstr + "\n";
	    					if (showSteps == true) {
	    		    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			if (!InformationBox.stepBox("Isprobavam vrijednost " + String.valueOf(val + 1) + " u æeliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").", "Rješavaè")) {
		    	    				showSteps = false;
		    	    			}
	    		    		}
							// Èistimo moguænosti prepostavljene æelije
				    		for (int clearVal = 0; clearVal < cols; clearVal++) {
				    			possibilities[row * cols + col][clearVal] = 0;
				    		}
			    			possibilities[row * cols + col][val] = 1;
					    	unset--;
					    	// Pokušavamo rješiti zagonetku uz novu pretpostavku
					    	sequence();
		    				lineSolvInstr = "Vraæam unatrag vrijednost " + String.valueOf(val + 1) + " u æeliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").";
	    					solvingInstructions += lineSolvInstr + "\n";
	    					if (showSteps == true) {
	    		    		    instructionArea.setText(solvingInstructions);
		    		    		print();
		    	    			if (!InformationBox.stepBox("Vraæam unatrag vrijednost " + String.valueOf(val + 1) + " u æeliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").", "Rješavaè")) {
		    	    				showSteps = false;
		    	    			}
	    		    		}
							// Nakon rješavanja s pretpostavkom vraæamo prethodno stanje
						    for (int rowRestore = 0; rowRestore < rows; rowRestore++){
						    	for (int colRestore = 0; colRestore < cols; colRestore++) {
						    		if (possibilityNum == 0) {
						    			// Ako smo pomoæu pretpostavke po prvi put dobili vrijednost u nekoj æeliji, zapisujemo ju
						    			forcedValues[rowRestore * cols + colRestore] = temporary[rowRestore * cols + colRestore];
						    		} else { 
						    			// Ako smo pomoæu pretpostavki iduæi put dobili vrijednost u nekoj æeliji, zapisujemo ju ako je ista kao prehodna, a zamjenjujemo s 0 ako se razlikuju
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
		    		// Prelazimo polje gdje pamtimo zajednièke ishode pretpostavki za odreðenu æeliju
				    for (int rowForce = 0; rowForce < rows; rowForce++){
				    	for (int colForce = 0; colForce < cols; colForce++) {
				    		// Ako za bilo koju vrijednost poèetne æelije neka druga æelija ima uvijek istu vrijednost, kažemo da poèetna æelija forsira njezinu vrijednost
				    		if (forcedValues[rowForce * rows + colForce] != 0 && temporary[rowForce * rows + colForce] == 0) {
			    				String lineSolvInstr = "Æelija (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ") forsira vrijednost ";
								// Ako smo veæ forsirali neku vrijednost pomoæu ove iste æelije, ne bodujemo to dvaput
			    				if (!solvingInstructions.contains(lineSolvInstr)) {
			    					if (fct == 0) {
					    				// Kada prvi put koristimo forsiranje ulanèavanjem, trošak je 4200
			    						difficultyScore += 4200;
			    						fct = 1;
			    					} else {
					    				// Kada iduæi put koristimo forsiranje ulanèavanjem, trošak je 2100
			    						difficultyScore += 2100;
			    					}
			    				}
		    					lineSolvInstr += String.valueOf(forcedValues[rowForce * rows + colForce]) + " u æeliji (" + String.valueOf(rowForce + 1) + ", " + String.valueOf(colForce + 1) + ").\n";
		    					// Dodajemo novi red u tekst uputa
		    					solvingInstructions += lineSolvInstr;
		    					// Ako se prikazuje rješavanje korak po korak otvaramo novi prozor s uputama
			    				if (showSteps == true) {
		    		    		    instructionArea.setText(solvingInstructions);
			    		    		print();
			    	    			if (!InformationBox.stepBox("Æelija (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ") forsira vrijednost " + String.valueOf(forcedValues[rowForce * rows + colForce]) + " u æeliji (" + String.valueOf(rowForce + 1) + ", " + String.valueOf(colForce + 1) + ").", "Rješavaè")) {
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
						    	// Ako smo postavili sve æelije, prekidamo rješavanje
			    				if (unset == 0) {
			    					return 1;
			    				} 
							    // Ako nismo postavili sve æelije, pokreæemo sekvencu
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
	// Pokušavamo naæi varijantu rješenja pogaðanjem
	public int guessing() {
		// Prije nego krenemo pogaðati vrijednost u nekoj æeliji, moramo zapisati trenutno stanje zbog povratka untrag
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
		    			// Dodajemo liniju u tekst uputa za rješavanje
		    			solvingInstructions += "Poèinjem pogaðati.\n";
						solvingInstructions += "Pokušavam " + String.valueOf(val + 1) + " u æeliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
						backupSolvingInstructions += "Poèinjem pogaðati.\n";
						backupSolvingInstructions += "Pokušavam " + String.valueOf(val + 1) + " u æeliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
				    	temporary[row * cols + col] = val + 1;
						// Èistimo moguænosti pogoðene æelije
			    		for (int clearVal = 0; clearVal < cols; clearVal++) {
			    			possibilities[row * cols + col][clearVal] = 0;
			    		}
		    			possibilities[row * cols + col][val] = 1;
				    	unset--;
						if (sequence() == 1) {
							// Ako je zagonetka u potpunosti rješena nakon dodavanja pogoðene vrijednosti, našli smo jedno moguæe rješenje i prekidamo rješavanje
							return 1;
						} else {
							// Ako nije moguæe riješiti zagonetku nakon dodavanja pogoðene vrijednosti, vraæamo prethodno stanje
						    for (int rowRestore = 0; rowRestore < rows; rowRestore++){
						    	for (int colRestore = 0; colRestore < cols; colRestore++) {
						    		temporary[rowRestore * cols + colRestore] = backupTemporary[rowRestore * cols + colRestore];
						    		for (int valRestore = 0; valRestore < cols; valRestore++) {
						    			possibilities[rowRestore * cols + colRestore][valRestore] = backupPossibilities[rowRestore * cols + colRestore][valRestore];
						    		}
			    				}
			    			}
						    difficultyScore = backupDifficultyScore;
						    backupSolvingInstructions += "Povlaèim " + String.valueOf(val + 1) + " u æeliji (" + String.valueOf(row + 1) + ", " + String.valueOf(col + 1) + ").\n";
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
	    	// Ako niti nakon pogaðanja nije moguæe rješiti zagonetku, vraæamo prethodno stanje i javljamo da zagonetku nije moguæe rješiti 
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
	    	// Ako je pogaðanjem naðena jedna varijanta rješenja, javljamo da ona postoji 
	    	return 1;
	    }
	}
	// Provjeravamo sadrže li æelije koje smo našli u istom redu ili stupcu kao i prethodna æelija u zatvorenom lancu dozvoljene vrijednosti za nastavak lanca
	// cells - skup æelija koje su veæ u zatvorenom skupu, beginCell - poèetna æelija u zatvorenom lancu, direction - tražimo li nove æelije unaprijed (0) ili unatrag (1)
	// rowOrCols - jesmo li moguæu æeliju koju želimo dodati u zatvoreni lanac našli u istom redu (0) ili u istom stupcu (1) u kojem je zadnja veæ dodana æelija
	// beginVal - zadana vrijednost koja se nalazi kao moguænost u svim æelijama zatvorenog lanca i povezuje ih, numCell - broj æelije koju želimo dodati u zatvoreni lanac
	public void startNextIterationOfClosedChains(Stack<Integer> cells, int beginCell, int direction, int rowOrcols, int beginVal, int numCell) {
		// Ako je æelija veæ u zatvorenom lancu joj je veæ postavljena vrijednost, ili ne sadrži moguæu vrijednost koja povezuje lanac, preskaèemo ju
		if (cells.contains(numCell) || temporary[numCell] != 0 || possibilities[numCell][beginVal] == 0) {
			return;
		}
		int numPossibilities = 0;
		for (int val = 0; val < cols; val++) {
			if (possibilities[numCell][val] == 1) {
				numPossibilities++;
			}
		}
		// Ako u æeliji mogu biti više od dvije moguæe vrijednosti, forsiranje vrijednosti u lancu nije moguæe pa prekidamo izvoðenje
		if (numPossibilities != 2) {
			return;
		}
		Stack<Integer> StackNextIteration = new Stack<Integer>();
		for (int cell = 0; cell < rows * cols; cell++) {
			if (cells.contains(cell)) {
				StackNextIteration.add(cell);
			}
		}
		// Dodajemo æeliju u novi skup æelija za novi poziv rekurzije za potragu za zatvorenim lancem
		StackNextIteration.add(numCell);
		// Ako je lanac prazan, postavljamo njenu poziciju za poziciju poèetne æelije
		if (cells.size() == 0) {
			beginCell = numCell;
		}
		if (rowOrcols == 1) {
			// Ako smo æeliju koju dodajemo našli u istom redu kao i prethodnu, iduæu æemo tražiti u istom stupcu u kojem je i æelija koju dodajemo
			closedChain(StackNextIteration, beginCell, direction, 0, beginVal);
		} else {
			// Ako smo æeliju koju dodajemo našli u istom stupcu kao i prethodnu, iduæu æemo tražiti u istom redu u kojem je i æelija koju dodajemo
			closedChain(StackNextIteration, beginCell, direction, 1, beginVal);
		}
	}
	// Maksimalna velièina prve polovice zatvorenog lanca
	int chainLength;
	// Broj korištenja za metodu X-krila
	int xwg = 0;
	// Broj korištenja za metodu sabljarke
	int sf4 = 0;
	// Tražimo zatvorene lance æelija
	// cells - skup æelija koje su veæ u zatvorenom skupu, beginCell - poèetna æelija u zatvorenom lancu, direction - tražimo li nove æelije unaprijed (0) ili unatrag (1)
	// rowOrCols - jesmo li moguæu æeliju koju želimo dodati u zatvoreni lanac našli u istom redu (0) ili u istom stupcu (1) u kojem je zadnja veæ dodana æelija
	// beginVal - zadana vrijednost koja se nalazi kao moguænost u svim æelijama zatvorenog lanca i povezuje ih
	public int closedChain(Stack<Integer> cells, int beginCell, int direction, int rowOrcols, int beginVal) {
		// Ako smo prošli polovicu zatvorenog lanca, mijenjamo smjer i vraæamo se unatrag
		if (cells.size() == chainLength) {
			direction = 0;
		}
		// Ako smo prošli cjelovitu duljinu lanca, provjeravamo je li lanac zatvoren
		if (cells.size() == chainLength * 2) {
			// Lanac je zatvoren ako smo završili u istom redu ili stupcu gdje smo poèeli
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
				// Ako æelija koja nije dio zatvorenog lanca, a nalazi se u retku ili stupcu koji je dio zatvorenog lanca, sadrži vrijednost koja povezuje zatvoreni lanac, uklanjamo tu moguænost
				if (cells.contains(cell) || (!usedRows.contains(cell / cols) && !usedCols.contains(cell % cols)) || possibilities[cell][beginVal] == 0) {
					continue;
				}						
				// Isti uoèeni zatvoreni skup boduje se samo jednom, iako rezultira uklanjanjem više moguænosti
				if (numRemoved == 0) {
					String lineSolvInstr = "";
					// Zatvoreni lanac koji sadrži dva reda i dva stupca naziva se X-krilo
					if (chainLength == 2) {
						lineSolvInstr += "X-krilo vrijednosti " + String.valueOf(beginVal + 1) + " u æelijama";
					}
					// Zatvoreni lanac koji sadrži tri reda i tri stupca naziva se sabljarka
					if (chainLength == 3) {
						lineSolvInstr += "Sabljarka vrijednosti " + String.valueOf(beginVal + 1) + " u æelijama";
					}
					// Zatvoreni lanac koji sadrži èetiri reda (ili više) i èetiri stupca (ili više) naziva se meduza
					if (chainLength == 4) {
						lineSolvInstr += "Meduza vrijednosti " + String.valueOf(beginVal + 1) + " u æelijama";
					}
					// Dodavanje æelija zatvorenog lanca u upute za rješavanje
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
					// Ako smo veæ registrirali ovaj isti zatvoreni skup, ne bodujemo ga dvaput
		    		if (!solvingInstructions.contains(lineSolvInstr)) {
						if (chainLength == 2) {
			    			if (xwg == 0) {
			    				// Kada prvi put naðemo X-krilo, trošak je 2800
			    				difficultyScore += 2800;
			    				xwg = 1;
			    			} else {
			    				// Kada iduæi put naðemo X-krilo, trošak je 1600
			    				difficultyScore += 1600;
			    			} 
						} else {
			    			if (sf4 == 0) {
			    				// Kada prvi put naðemo sabljarku, trošak je 8000
			    				difficultyScore += 8000;
			    				sf4 = 1;
			    			} else {
			    				// Kada iduæi put naðemo sabljarku, trošak je 6000
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
		    	// Ukloni moguænosti prema odnosima veæe-manje
	    		Set<Integer> visitedMax = new HashSet<Integer>();
				setMaxPossibility(cell, visitedMax);
		    	// Ukloni moguænosti prema odnosima manje-veæe
	    		Set<Integer> visitedMin = new HashSet<Integer>();
				setMinPossibility(cell, visitedMin);
				// Dodajemo novi red za uklanjanje moguænosti u tekst uputa
				solvingInstructions += "Uklanjam moguænost " + String.valueOf(beginVal + 1) + " iz æelije (" + String.valueOf(cell / cols + 1) + ", " + String.valueOf(cell % cols + 1) + ").\n" ;
	    		// Ako nismo postavili sve æelije, pozivamo sekvencu metoda za rješavanje, ako ona uspije sve rješiti, prekidamo rješavanje
				if (sequence() == 1) {
					return 1;
				}
			}
			return 0;
		}
		// Ako skup æelija u zatvorenom lancu nije prazan
	    if (cells.size() > 0) {
	    	// Ako tražimo æeliju u istom redu kao poèetna
			if (rowOrcols == 0) {
				if (direction == 0) {
					// Pretražujemo prethodne æelije u istom redu, imaju manju oznaku stupca
					for (int col = 0; col < cells.peek() % cols; col++){ 
			    		int numCell = cells.peek() / cols * cols + col;
			    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
					}
				} else {
					// Pretražujemo naredne æelije u istom redu, imaju veæu oznaku stupca
					for (int col = cells.peek() % cols + 1; col < cols; col++){ 
			    		int numCell = cells.peek() / cols * cols + col;
			    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
					}
				}
			}
	    	// Ako tražimo æeliju u istom stupcu kao poèetna
			if (rowOrcols == 1) {
				if (direction == 0) {
					// Pretražujemo prethodne æelije u istom stupcu, imaju manju oznaku reda
					for (int row = 0; row < cells.peek() / cols; row++){ 
			    		int numCell = row * cols + cells.peek() % cols;
			    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
					}
				} else {
					// Pretražujemo naredne æelije u istom stupcu, imaju veæu oznaku reda
					for (int row = cells.peek() / cols + 1; row < rows; row++){ 
			    		int numCell = row * cols + cells.peek() % cols;
			    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
					}
				}
			}
	    } else {
	    	// Ako je skup æelija u zatvoren lancu prazan, provjeravamo može li bilo koja æelija u mreži biti poèetna æelija zatvorenog lanca povezanog zadanom vrijednošæu 
	    	for (int row = 0; row < rows; row++){ 
	    		for (int col = 0; col < cols; col++) {
		    		int numCell = row * cols + col;
		    		startNextIterationOfClosedChains(cells, beginCell, direction, rowOrcols, beginVal, numCell);
	    		} 
	    	}
	    }
		return 0;
	}
	// Ispisujemo sve konaène vrijednosti i moguænosti æelija na korisnikom suèelju
		public void print() {
			for (int row = 0; row < rows; row++){ 
		    	for (int col = 0; col < cols; col++) {
			    	int numCell = row * cols + col;
		    		String text = "<html><p style='text-align: center'>";
		    		int numberOptions = 0;
		    		// Zapisujemo sve vrijednosti za æeliju
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
				    	// Ako imam više moguænosti za æeliju zapisujemo ih u HTML oznakama
			    		text = text.substring(0, text.length() - 1) + "</p></html>";
			    	} else {
				    	// Ako nema moguænosti za æeliju zapisujemo prazan string
			    		text = "";
			    	}
		    		field[numCell].setText(text);
		    		if (temporary[numCell] == 0) {
			    		// Ako æelija nema definiranu konaènu vrijednost, moramo je obojati u crveno i smanjiti font
		    			field[numCell].setForeground(Color.RED);
		    			field[numCell].setFont(new Font("Arial", Font.PLAIN, guessFontsize));
		    		} else {
			    		// Ako je definirana konaèna vrijednost æelije, moramo je obojati u zeleno i poveæati font
		    			field[numCell].setForeground(Color.GREEN);
		    			field[numCell].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
		    		}
		    	}
		    }
		}
	
	// Uklanjamo rotacijski simetrièni par æelija da bismo poveæali složenost
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
	    // Ako su sve æelije veæ uklonjene, prekidamo postupak
	    if (allEmpty) {
	    	return;
	    }
	    // Nasumièno odabiremo redak i stupac iz koji uklanjamo 
    	int randomCol = ThreadLocalRandom.current().nextInt(0, cols);
    	int randomRow = ThreadLocalRandom.current().nextInt(0, cols);
    	int original = randomRow * cols + randomCol;
    	// Pronalazimo rotacijski simetrièan par odabrane æelije
    	int symetric = (rows - 1 - randomRow) * cols + (cols - 1 - randomCol);
    	// Ako su obje æelije u par veæ uklonjene, ponavljamo odabir
    	while (temporary[original] == 0 && temporary[symetric] == 0) {
        	randomCol = ThreadLocalRandom.current().nextInt(0, cols);
        	randomRow = ThreadLocalRandom.current().nextInt(0, cols);
        	original = randomRow * cols + randomCol;
        	symetric = (rows - 1 - randomRow) * cols + (cols - 1 - randomCol);
    	}
    	// Na vrh stoga zapisujemo vrijednosti koje su bile zapisane u uklonjenim æelijama
    	lastRemovedPosOriginal.push(original);
    	lastRemovedValOriginal.push(userInput[original]);
    	// Ažuriramo broj korištenja znamenke koju smo pobrisali
    	numUseDigit[userInput[original]]--;
    	checkIfDigitMaxUsed(userInput[original]);
    	temporary[original] = 0;
		userInput[original] = temporary[original];
	    field[original].setText("0");
    	field[original].setForeground(Color.RED);
    	// Na vrh stoga zapisujemo æelije koje su uklonjene
    	lastRemovedPosSymetric.push(symetric);
    	lastRemovedValSymetric.push(userInput[symetric]);
    	// Ažuriramo broj korištenja znamenke koju smo pobrisali ako nismo dvaput pobrisali istu æeliju (sluèaj središnje æelije u mreži neparnih dimenzija)
    	if (original != symetric) {
        	numUseDigit[userInput[symetric]]--;
        	checkIfDigitMaxUsed(userInput[symetric]);
    	}
    	temporary[symetric] = 0;
		userInput[symetric] = temporary[symetric];
	    field[symetric].setText("0");
    	field[symetric].setForeground(Color.RED);
	}

	// Vraæamo rotacijski simetrièni par æelija da bismo poveæali rješivost
	public boolean restoreLastRemoved() {
		// Ako nismo uklonili niti jedan rotacijski simetrièan par, prekidamo postupak
		if (lastRemovedPosOriginal.isEmpty()) {
			return false;
		}
		// S vrha stoga vraæamo položaj uklonjenih æelija i vrijednosti koje su bile zapisane u njima
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
    	// Ažuriramo broj korištenja znamenke koju smo vratili
    	numUseDigit[lastRemovedValOriginal.peek()]++;
    	checkIfDigitMaxUsed(lastRemovedValOriginal.peek());
		userInput[lastRemovedPosSymetric.peek()] = lastRemovedValSymetric.peek();
    	// Ažuriramo broj korištenja znamenke koju smo vratili ako nismo dvaput vratili istu æeliju (sluèaj središnje æelije u mreži neparnih dimenzija)
		if (lastRemovedPosSymetric.peek() != lastRemovedPosOriginal.peek()) {
	    	numUseDigit[lastRemovedValSymetric.peek()]++;
	    	checkIfDigitMaxUsed(lastRemovedValSymetric.peek());
		}
		solution[lastRemovedPosOriginal.peek()] = lastRemovedValOriginal.peek();
		solution[lastRemovedPosSymetric.peek()] = lastRemovedValSymetric.peek();
		temporary[lastRemovedPosOriginal.peek()] = lastRemovedValOriginal.peek();
		temporary[lastRemovedPosSymetric.peek()] = lastRemovedValSymetric.peek();
		// Brišemo vrhove stogova
		lastRemovedPosOriginal.pop();
		lastRemovedValOriginal.pop();
		lastRemovedPosSymetric.pop();
		lastRemovedValSymetric.pop();	
		return true;
	}
	// Generiramo nasumiènu zagonetku
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
		// Postavljamo vrijednost u odreðeni redak
	    for (int row = 0; row < rows; row++) {
			for (int val = 1; val <= cols; val++) {
				// Ako je vrijednost veæ postavljena u redak, nastavljamo dalje
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
		    	// Ako vrijednost nije moguæe postaviti u red, zagonetka koju smo generirali nije ispravna i moramo krenuti isponova
		    	if (possible == 0) {
			    	return 1;
		    	}
		    	// Ako je vrijednost unutar reda moguæe postaviti samo na jednu poziciju, možemo postaviti vrijednost
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
	// Uklanjamo oznaèavanje pozadine reda i stupca æelije koja je u fokusu i oznaèavanje odabrane znamenke
	abstract public void resetHighlight();
	// Dodajemo oznaèavanje pozadine reda i stupca æelije koja je u fokusu
	abstract public void highlightCell(int numCell);
	// Dodajemo oznaèavanje odabrane znamenke
	abstract public void highlightDigit();
	abstract public void assume();
	
	int[] numUseDigit = new int[cols + 1];
	
	// Prebrojavamo koliko je puta iskorištena koja znamenka
	public void checkIfDigitMaxUsed(int digit) {
		// Ne prebrojavamo polja koja su prazna (0)
		if (digit == 0) {
			return;
		}
		if (numUseDigit[digit] >= cols) {
			// Ako je znamenka iskorištena više ili jednako puta koliko je stupaca u mreži, pobojamo tekst gumba koji joj pripada u sivo kao upozorenje
			digitButtons[digit].setForeground(Color.LIGHT_GRAY);
		} else {
			// Ako je znamenka iskorištena manje puta nego što je stupaca u mreži, pobojamo tekst gumba koji joj pripada u crno
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
				errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " veæ postoji u retku " + (row + 1) + ".\n");
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
				errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " veæ postoji u stupcu " + (col + 1) + ".\n");
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
    				errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " veæ postoji u rastuæoj dijagonali.\n");
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
    				errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " veæ postoji u padajuæoj dijagonali.\n");
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
				errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " veæ postoji u kutiji " + (boxNumber[row * cols + col] + 1) + ".\n");
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
	    				errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije veæi od broja " + temporary[newCell] + " u æeliji (" + (row + 1) + ", " + (col + 2) + ").\n");
		    		} 
	    			relationshipCell = String.valueOf(newCell) + " " + String.valueOf(numCell);
	    			if (sizeRelationships.contains(relationshipCell) && temporary[newCell] != 0 && temporary[row * cols + col] >= temporary[newCell]) {
	    				incorrect[numCell] = true;
		    			incorrect[newCell] = true;
		    			errorArea.setText(errorArea.getText() + val + ": " + "(" + (row + 1) + ", " + (col + 1) + ") Broj " + val + " nije manji od broja " + temporary[newCell] + " u æeliji (" + (row + 1) + ", " + (col + 2) + ").\n");
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
			    		errorArea.setText(errorArea.getText() + "Æelija (" + (row + 1) + ", " + (col + 1) + ") nema moguæih vrijednosti.\n");
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
