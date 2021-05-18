import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

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
		    		if (possibilities[row * cols + col][selectedDigit - 1] == 1) {
	    				setAllOptions(possibilities, row, col, true);
		    		}
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
	    		    initialize();
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
	public int countIncorrect(boolean[] incorrect, boolean correct) {
		int localnumErrors = 0;
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
    			int numCell = row * cols + col;
	    		if (incorrect[numCell]) {
	    			localnumErrors++;
	    			field[numCell].setForeground(Color.ORANGE);
	    		} else {
    				field[numCell].setForeground(Color.WHITE);
	    		}
		    }
	    }
		return localnumErrors;
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
	        		zoomArea.setText(field[numCell].getText());
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
		        	    InformationBox.infoBox("Rješavanje je dovršeno, nastavak dizajna.", "Korak po korak");
		        	    assume();
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
		        		highlightDigit();
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
		        		highlightDigit();
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
						SolveSudoku SolveSudoku = new SolveSudoku(rows, cols, xLim, yLim, border, boxNumber, diagonalOn, sizeRelationships, userInput, true);
					} catch (Exception e1) {
		
		
					}
		        }  
		    });
		y += h + space;
	    JLabel row1Label = new JLabel("Red 1: ");
	    row1Label.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    row1Label.setBounds(x, y, w, h);
	    frame.add(row1Label);
	    
	    JTextField row1Value = new JTextField(String.valueOf(rows));
	    row1Value.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    row1Value.setBounds(x + w / 2 - h - space, y, h, h);
	    frame.add(row1Value);

	    JLabel row2Label = new JLabel("Red 2: ");
	    row2Label.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    row2Label.setBounds(x + w / 2 + space, y, w, h);
	    frame.add(row2Label);
	    
	    JTextField row2Value = new JTextField(String.valueOf(rows));
	    row2Value.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    row2Value.setBounds(x + w - h, y, h, h);
	    frame.add(row2Value);

	    y += h + space;
	    makeAButton("Zamjeni redove", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
						if (Integer.parseInt(row1Value.getText()) == Integer.parseInt(row2Value.getText())) {
							InformationBox.infoBox("Ne možete zamjeniti red " + row1Value.getText() + " sa samim sobom.", "Zamjena redova");
							return;
						}
						if (Integer.parseInt(row1Value.getText()) > rows) {
							InformationBox.infoBox("Prvi red je veæi od broja stupaca.", "Zamjena redova");
							return;
						}
						if (Integer.parseInt(row2Value.getText()) > rows) {
							InformationBox.infoBox("Drugi red je veæi od broja stupaca.", "Zamjena redova");
							return;
						}
						if (Integer.parseInt(row1Value.getText()) < 1) {
							InformationBox.infoBox("Prvi red je manji ili jednak 0.", "Zamjena redova");
							return;
						}
						if (Integer.parseInt(row2Value.getText()) < 1) {
							InformationBox.infoBox("Drugi red je manji ili jednak 0.", "Zamjena redova");
							return;
						}
						swapRow(Integer.parseInt(row1Value.getText()) - 1, Integer.parseInt(row2Value.getText()) - 1);
		        	    assume();
		        	    checkIfCorrect();
		        	    resetHighlight();
		        	    highlightDigit();
					} catch (Exception e1) {
						
					}
		        }  
		    });
		y += h + space;
	    JLabel col1Label = new JLabel("Stu. 1: ");
	    col1Label.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    col1Label.setBounds(x, y, w, h);
	    frame.add(col1Label);
	    
	    JTextField col1Value = new JTextField(String.valueOf(rows));
	    col1Value.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    col1Value.setBounds(x + w / 2 - h - space, y, h, h);
	    frame.add(col1Value);

	    JLabel col2Label = new JLabel("Stu. 2:");
	    col2Label.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    col2Label.setBounds(x + w / 2 + space, y, w, h);
	    frame.add(col2Label);
	    
	    JTextField col2Value = new JTextField(String.valueOf(rows));
	    col2Value.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    col2Value.setBounds(x + w - h, y, h, h);
	    frame.add(col2Value);

	    y += h + space;

	    makeAButton("Zamjeni stupce", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
						if (Integer.parseInt(col1Value.getText()) == Integer.parseInt(col2Value.getText())) {
							InformationBox.infoBox("Ne možete zamjeniti stupac " + row1Value.getText() + " sa samim sobom.", "Zamjena stupaca");
							return;
						}
						if (Integer.parseInt(col1Value.getText()) > cols) {
							InformationBox.infoBox("Prvi stupac je veæi od broja stupaca.", "Zamjena stupaca");
							return;
						}
						if (Integer.parseInt(col2Value.getText()) > cols) {
							InformationBox.infoBox("Drugi stupac je veæi od broja stupaca.", "Zamjena stupaca");
							return;
						}
						if (Integer.parseInt(col1Value.getText()) < 1) {
							InformationBox.infoBox("Prvi stupac je manji ili jednak 0.", "Zamjena stupaca");
							return;
						}
						if (Integer.parseInt(col2Value.getText()) < 1) {
							InformationBox.infoBox("Drugi stupac je manji ili jednak 0.", "Zamjena stupaca");
							return;
						}
						swapCol(Integer.parseInt(col1Value.getText()) - 1, Integer.parseInt(col2Value.getText()) - 1);
		        	    assume();
		        	    checkIfCorrect();
		        	    resetHighlight();
		        	    highlightDigit();
					} catch (Exception e1) {
		
		
					}
		        }  
		    });
		y += h + space;
		JLabel val1Label = new JLabel("Broj 1: ");
		val1Label.setFont(new Font("Arial", Font.PLAIN, fontsize));
		val1Label.setBounds(x, y, w, h);
	    frame.add(val1Label);
	    
	    JTextField val1Value = new JTextField(String.valueOf(rows));
	    val1Value.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    val1Value.setBounds(x + w / 2 - h - space, y, h, h);
	    frame.add(val1Value);

	    JLabel val2Label = new JLabel("Broj 2: ");
	    val2Label.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    val2Label.setBounds(x + w / 2 + space, y, w, h);
	    frame.add(val2Label);
	    
	    JTextField val2Value = new JTextField(String.valueOf(rows));
	    val2Value.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    val2Value.setBounds(x + w - h, y, h, h);
	    frame.add(val2Value);

	    y += h + space;

	    makeAButton("Zamjeni vrijednosti", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
						if (Integer.parseInt(val1Value.getText()) == Integer.parseInt(val2Value.getText())) {
							InformationBox.infoBox("Ne možete zamjeniti vrijednost " + val1Value.getText() + " sa samom sobom.", "Zamjena vrijednosti");
							return;
						}
						if (Integer.parseInt(val1Value.getText()) > cols) {
							InformationBox.infoBox("Prva vrijednost je veæa od broja vrijednosti.", "Zamjena vrijednosti");
							return;
						}
						if (Integer.parseInt(val2Value.getText()) > cols) {
							InformationBox.infoBox("Druga vrijednost je veæa od broja vrijednosti.", "Zamjena vrijednosti");
							return;
						}
						if (Integer.parseInt(val1Value.getText()) < 1) {
							InformationBox.infoBox("Prva vrijednost je manja ili jednaka 0.", "Zamjena vrijednosti");
							return;
						}
						if (Integer.parseInt(val2Value.getText()) < 1) {
							InformationBox.infoBox("Druga vrijednost je manja ili jednaka 0.", "Zamjena vrijednosti");
							return;
						}
						swapNumbers(Integer.parseInt(val1Value.getText()), Integer.parseInt(val2Value.getText()));
		        	    assume();
		        	    checkIfCorrect();
		        	    resetHighlight();
		        	    highlightDigit();
					} catch (Exception e1) {
		
		
					}
		        }  
		    });
		y += h + space;
		addZoomBox(x, y, w, w);
		y += w + space;
		w = (int) (250 * widthScaling);
        difficulty.setBounds(x, y, w, h);
        difficulty.setFont(new Font("Arial", Font.PLAIN, fontsize));
        frame.add(difficulty);
		y += h + space;
	    
		int buttonEnd = y;
        
		addErrorScroll(digitEnd, buttonEnd);
		addInstructionScroll(digitEnd, buttonEnd);

	    frame.setSize(x, Math.max(digitEnd, buttonEnd) + (int) (40 * heightScaling));  
	    frame.setLayout(null);  
    }
}
