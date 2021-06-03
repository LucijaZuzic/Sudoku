import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class CreateSudoku extends Sudoku {
	
	public void resetHighlight() {
		for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
    			setBackground(row, col, returnColour(row * cols + col));
	    		field[row * cols + col].setFont(field[row * cols + col].getFont().deriveFont(~Font.BOLD | ~Font.ITALIC));
	    		if (userInput[row * cols + col] == 0) {
	    			setAllOptions(possibilities, row, col, false);
	    		}
	    	}
	    }
		for (int col = 0; col < cols + 1; col++) {
			digitButtons[col].setBackground(Color.WHITE);
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
	
	public CreateSudoku(int constructRows, int constructCols, int rowLimit, int colLimit, int[] constructBorder, int[] constructBoxNumber, boolean setDiagonalOn, boolean setWrapAround, Set<String> setSizeRelationships, int[] constructSumBoxSums, int[] constructSumBoxNumbers) {
		super(constructRows, constructCols, rowLimit, colLimit, setDiagonalOn, setWrapAround, setSizeRelationships);
		border = constructBorder;
		boxNumber = constructBoxNumber;
		sumBoxSums = constructSumBoxSums;
		sumBoxNumber = constructSumBoxNumbers;
		if (cols <= 12) {
			if (InformationBox.yesNoBox("Želite li da se mreža automatski ispuni?", "Ukljuèi bilješke")) {
			    long startGen = System.currentTimeMillis();
			    int retval = -1;
			    while(retval == -2 || retval == -1) {
			    	if (System.currentTimeMillis() - startGen >= 10000) {
		    		    InformationBox.infoBox("Nije moguæe ispuniti zagonetku prema zadanim kriterijima.", "Pogrešno dizajnirana zagonetka");
		    		    initialize();
			    		break;
			    	}
				    for (int row = 0; row < rows; row++){ 
				    	for (int col = 0; col < cols; col++) {
				    		temporary[row * cols + col] = 0;
					    }
				    }
					useGuessing = true;
				    retval = isOnlyOneSolution();
					useGuessing = false;
			    }
			} else {
			    for (int row = 0; row < rows; row++){ 
			    	for (int col = 0; col < cols; col++) {
			    		temporary[row * cols + col] = 0;
				    }
			    }
			}
		} else {
		    InformationBox.infoBox("Nije moguæe stvoriti nasumiènu zagonetku veæu od 12x12.", "Prevelika zagonetka");
		    initialize();
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
		// Èistimo upute za rješavanje i težinu
		difficultyScore = 0;
		instructionArea.setText("");
		solvingInstructions = "";
		difficulty.setText("");
	    frame.setVisible(true);
	    frame.requestFocus();
	}
	

	public CreateSudoku(int constructRows, int constructCols, int rowLimit, int colLimit, int[] constructBorder, int[] constructBoxNumber, boolean setDiagonalOn, boolean setWrapAround, Set<String> setSizeRelationships, int constructUserInput[], String oldLastUsedPath,  int[] constructSumBoxSums, int[] constructSumBoxNumbers) {
		super(constructRows, constructCols, rowLimit, colLimit, setDiagonalOn, setWrapAround, setSizeRelationships);
		lastUsedPath = oldLastUsedPath;
		border = constructBorder;
		boxNumber = constructBoxNumber;
		sumBoxSums = constructSumBoxSums;
		sumBoxNumber = constructSumBoxNumbers;
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
	        		if (zoomMode) {
		        		zoomArea.setText(field[numCell].getText().replace("yellow", "black"));
						resetHighlight();
						highlightDigit();
						highlightCell(numCell);
		        		return;
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
	        		}
	        		assume();
	        		zoomArea.setText(field[numCell].getText().replace("yellow", "black"));
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
    
    public boolean checkRowToSwap(JTextField val1Value, JTextField val2Value) {
    	if (Integer.parseInt(val1Value.getText()) == Integer.parseInt(val2Value.getText())) {
			InformationBox.infoBox("Ne možete zamjeniti red " + val1Value.getText() + " sa samim sobom.", "Zamjena redova");
			return false;
		}
		if (Integer.parseInt(val1Value.getText()) > rows) {
			InformationBox.infoBox("Prvi red je veæi od broja stupaca.", "Zamjena redova");
			return false;
		}
		if (Integer.parseInt(val2Value.getText()) > rows) {
			InformationBox.infoBox("Drugi red je veæi od broja stupaca.", "Zamjena redova");
			return false;
		}
		if (Integer.parseInt(val1Value.getText()) < 1) {
			InformationBox.infoBox("Prvi red je manji ili jednak 0.", "Zamjena redova");
			return false;
		}
		if (Integer.parseInt(val2Value.getText()) < 1) {
			InformationBox.infoBox("Drugi red je manji ili jednak 0.", "Zamjena redova");
			return false;
		}
		return true;
    }
    
    public boolean checkColToSwap(JTextField val1Value, JTextField val2Value) {
		if (Integer.parseInt(val1Value.getText()) == Integer.parseInt(val2Value.getText())) {
			InformationBox.infoBox("Ne možete zamjeniti stupac " + val1Value.getText() + " sa samim sobom.", "Zamjena stupaca");
			return false;
		}
		if (Integer.parseInt(val1Value.getText()) > cols) {
			InformationBox.infoBox("Prvi stupac je veæi od broja stupaca.", "Zamjena stupaca");
			return false;
		}
		if (Integer.parseInt(val2Value.getText()) > cols) {
			InformationBox.infoBox("Drugi stupac je veæi od broja stupaca.", "Zamjena stupaca");
			return false;
		}
		if (Integer.parseInt(val1Value.getText()) < 1) {
			InformationBox.infoBox("Prvi stupac je manji ili jednak 0.", "Zamjena stupaca");
			return false;
		}
		if (Integer.parseInt(val2Value.getText()) < 1) {
			InformationBox.infoBox("Drugi stupac je manji ili jednak 0.", "Zamjena stupaca");
			return false;
		}
		return true;
    }
    
    public boolean checkValToSwap(JTextField val1Value, JTextField val2Value) {
    	if (Integer.parseInt(val1Value.getText()) == Integer.parseInt(val2Value.getText())) {
			InformationBox.infoBox("Ne možete zamjeniti vrijednost " + val1Value.getText() + " sa samom sobom.", "Zamjena vrijednosti");
			return false;
		}
		if (Integer.parseInt(val1Value.getText()) > cols) {
			InformationBox.infoBox("Prva vrijednost je veæa od broja vrijednosti.", "Zamjena vrijednosti");
			return false;
		}
		if (Integer.parseInt(val2Value.getText()) > cols) {
			InformationBox.infoBox("Druga vrijednost je veæa od broja vrijednosti.", "Zamjena vrijednosti");
			return false;
		}
		if (Integer.parseInt(val1Value.getText()) < 1) {
			InformationBox.infoBox("Prva vrijednost je manja ili jednaka 0.", "Zamjena vrijednosti");
			return false;
		}
		if (Integer.parseInt(val2Value.getText()) < 1) {
			InformationBox.infoBox("Druga vrijednost je manja ili jednaka 0.", "Zamjena vrijednosti");
			return false;
		}
		return true;
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
		        		useGuessing = false;
		        	    if (InformationBox.yesNoBox("Želite li da se pri rješavanju koristi pogaðanje?", "Korak po korak")) {
		        	    	useGuessing = true;
		        	    }
		        		showSteps = true;
		        	    if (!InformationBox.stepBox("Jeste li spremni za prikaz koraka?", "Korak po korak")) {
		        	    	showSteps = false;
		        	    }
		        		isOnlyOneSolution();
		        		useGuessing = false;
		        	    instructionArea.setText(solvingInstructions);
		        	    InformationBox.infoBox("Rješavanje je dovršeno, nastavak dizajna.", "Korak po korak");
		        	    if (InformationBox.yesNoBox("Želite li da se rješenje zapiše u polja?", "Korak po korak")) {
		        	    	 for (int row = 0; row < rows; row++){ 
			        	    	for (int col = 0; col < cols; col++) {
			        		    	int numCell = row * cols + col;
			        	    		userInput[numCell] = temporary[numCell];
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
		        		if (cols > 12) {
		        		    InformationBox.infoBox("Nije moguæe stvoriti nasumiènu zagonetku veæu od 12x12.", "Prevelika zagonetka");
		        		    return;
		        		}
		        		showSteps = false;
		        	    useGuessing = true;
		        		isOnlyOneSolution();
		        	    useGuessing = false;
		        	    instructionArea.setText(solvingInstructions);
		        	    for (int row = 0; row < rows; row++){ 
		        	    	for (int col = 0; col < cols; col++) {
		        		    	int numCell = row * cols + col;
		        	    		userInput[numCell] = temporary[numCell];
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
		        	    difficultyScore = 0;
		        		instructionArea.setText("");
		        		solvingInstructions = "";
		        		difficulty.setText("");
		        	    checkIfCorrect();
		        		showSteps = false;
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
		        	    resetHighlight();
		        	    highlightDigit();
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
						SolveSudoku SolveSudoku = new SolveSudoku(rows, cols, xLim, yLim, border, boxNumber, diagonalOn, wrapAround, sizeRelationships, userInput, true, sumBoxSums, sumBoxNumber, lastUsedPath);
					} catch (Exception e1) {
		
		
					}
		        }  
		    });
		y += h + space;
	   
		JLabel val1Label = new JLabel("Broj 1: ");
		val1Label.setFont(new Font("Arial", Font.PLAIN, fontsize));
		val1Label.setBounds(x, y, w, h);
	    frame.add(val1Label);
	    
	    JTextField val1Value = new JTextField(String.valueOf(1));
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
	    
	    JButton rowSwap = makeAButton("", x, y, h, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
						if (checkRowToSwap(val1Value, val2Value)) {
						swapRow(Integer.parseInt(val1Value.getText()) - 1, Integer.parseInt(val2Value.getText()) - 1);
			        	    assume();
			        	    checkIfCorrect();
			        	    resetHighlight();
			        	    highlightDigit();
						}
					} catch (Exception e1) {
						
					}
		        }  
		    });
		
		JButton colSwap = makeAButton("", x + h + space, y , h, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
						if (checkColToSwap(val1Value, val2Value)) {
							swapCol(Integer.parseInt(val1Value.getText()) - 1, Integer.parseInt(val2Value.getText()) - 1);
			        	    assume();
			        	    checkIfCorrect();
			        	    resetHighlight();
			        	    highlightDigit();
						}
					} catch (Exception e1) {
		
		
					}
		        }  
		    });
		
		JButton valSwap = makeAButton("", x + 2 * h + 2 * space, y, h, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
						if (checkValToSwap(val1Value, val2Value)) {
							swapNumbers(Integer.parseInt(val1Value.getText()), Integer.parseInt(val2Value.getText()));
			        	    assume();
			        	    checkIfCorrect();
			        	    resetHighlight();
			        	    highlightDigit();
						}
					} catch (Exception e1) {
		
		
					}
		        }  
		    });
	    y += h + space;

	    rowSwap.setIcon(new ImageIcon(new ImageIcon("src/images/swap_rows.png").getImage().getScaledInstance(rowSwap.getWidth(), rowSwap.getHeight(), Image.SCALE_DEFAULT)));
	    colSwap.setIcon(new ImageIcon(new ImageIcon("src/images/swap_columns.png").getImage().getScaledInstance(colSwap.getWidth(), colSwap.getHeight(), Image.SCALE_DEFAULT)));
	    valSwap.setIcon(new ImageIcon(new ImageIcon("src/images/swap_values.png").getImage().getScaledInstance(valSwap.getWidth(), valSwap.getHeight(), Image.SCALE_DEFAULT)));

		JButton transpose = makeAButton("", x, y, h, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		transpose();
		        		assume();
		        	    checkIfCorrect();
		        	    resetHighlight();
		        	    highlightDigit();
		        	} catch (Exception e1) {
		
					}
		        }  
		    });
		transpose.setIcon(new ImageIcon(new ImageIcon("src/images/transpose.png").getImage().getScaledInstance(transpose.getWidth(), transpose.getHeight(), Image.SCALE_DEFAULT)));

		JButton vertical = makeAButton("", x + h + space, y, h, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		mirrorVertical();
		        		assume();
		        	    checkIfCorrect();
		        	    resetHighlight();
		        	    highlightDigit();
		        	} catch (Exception e1) {
		
					}
		        }  
		    });
	    vertical.setIcon(new ImageIcon(new ImageIcon("src/images/vertical.png").getImage().getScaledInstance(vertical.getWidth(), vertical.getHeight(), Image.SCALE_DEFAULT)));
	    
	    JButton horizontal = makeAButton("", x + 2 * h + 2 * space, y, h, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		mirrorHorizontal();
		        		assume();
		        	    checkIfCorrect();
		        	    resetHighlight();
		        	    highlightDigit();
		        	} catch (Exception e1) {
		
					}
		        }  
		    });
		horizontal.setIcon(new ImageIcon(new ImageIcon("src/images/horizontal.png").getImage().getScaledInstance(horizontal.getWidth(), horizontal.getHeight(), Image.SCALE_DEFAULT)));
	    y += h + space;
		
	    addZoomBox(x, y, w, w);

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
