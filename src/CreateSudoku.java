import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class CreateSudoku extends Sudoku {
	JButton digitButtons[];

	public void highlightCell(int numCell) {
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (col == numCell % cols || row == numCell / cols) {
	    			field[row * cols + col].setBackground(new Color(119, 136, 153));
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

	public void highlightDigit() {
		for (int val = 0; val < cols + 1; val++) {
			digitButtons[val].setBackground(Color.WHITE);
		}
		digitButtons[selectedDigit].setBackground(Color.CYAN);
	    for (int row = 0; row < rows; row++){
	    	for (int col = 0; col < cols; col++) {
	    		if (userInput[row * cols + col] == selectedDigit && selectedDigit != 0) {
	    			field[row * cols + col].setFont(field[row * cols + col].getFont().deriveFont(Font.BOLD | Font.ITALIC));
	    		} else {    				    			
	    			field[row * cols + col].setFont(field[row * cols + col].getFont().deriveFont(~Font.BOLD | ~Font.ITALIC));
	    		}
	    	}
	    }
	}
	
	public CreateSudoku(int constructRows, int constructCols, int rowLimit, int colLimit, int[] constructBorder, int[] constructBoxNumber) {
		super(constructRows, constructCols, rowLimit, colLimit);
		border = constructBorder;
		boxNumber = constructBoxNumber;
	    int retval = 1;
	    while(retval == 1) {
		    for (int row = 0; row < rows; row++){ 
		    	for (int j = 0; j < cols; j++) {
		    		temporary[row * cols + j] = 0;
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
		checkBoxes();
	    frame.setVisible(true);
	    checkIfCorrect();
	    frame.requestFocus();
	}
	

	public CreateSudoku(int constructRows, int constructCols, int rowLimit, int colLimit, int[] constructBorder, int[] constructBoxNumber, int constructUserInput[]) {
		super(constructRows, constructCols, rowLimit, colLimit);
		border = constructBorder;
		boxNumber = constructBoxNumber;
	    draw();
	    for (int row = 0; row < rows; row++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int numCell = row * cols + j;
	    		userInput[numCell] = constructUserInput[numCell];
	    		solution[numCell] = 0;
	    		field[numCell].setText(String.valueOf(userInput[numCell]));
	    	}
	    }
		checkBoxes();
	    checkIfCorrect();
	    frame.setVisible(true);
	    frame.requestFocus();
	}

	@Override
	public boolean checkIfCorrect() {
		String errortext = "";
		boolean incorrect[] = new boolean[rows * cols];
	    for (int row = 0; row < rows; row++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int numCell = row * cols + j;
		    	field[row * cols + j].setForeground(Color.BLACK);
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
		    	for (int j = 0; j < cols; j++) {
		    		usedRows[row] = 0;
		    		usedCols[j] = 0;
		    		usedBoxes[row] = 0;
			    }
		    }
		    for (int row = 0; row < rows; row++){
		    	for (int j = 0; j < cols; j++) {
		    		boolean status = false;
		    		if (temporary[row * cols + j] == val) {
			    		usedRows[row]++;
			    		usedCols[j]++;
			    		usedBoxes[boxNumber[row * cols + j]]++;
			    		if (usedRows[row] > 1) {
			    			errortext += val + ": " + "(" + (row + 1) + ", " + (j + 1) + ") Broj " + val + " ve� postoji u retku " + (row + 1) + "\n";
			    			correct = false;
			    			status = true;
			    			incorrect[row * cols + j] = true;
			    		}
			    		if (usedCols[j] > 1) {
			    			errortext += val + ": " + "(" + (row + 1) + ", " + (j + 1) + ") Broj " + val + " ve� postoji u stupcu " + (j + 1) + "\n";
			    			correct = false;
			    			status = true;
			    			incorrect[row * cols + j] = true;
			    		}
			    		if (usedBoxes[boxNumber[row * cols + j]] > 1) {
			    			errortext += val + ": " + "(" + (row + 1) + ", " + (j + 1) + ") Broj " + val + " ve� postoji u kutiji " + (boxNumber[row * cols + j] + 1) + "\n";
			    			correct = false;
			    			status = true;
			    			incorrect[row * cols + j] = true;
			    		}
			    		if (status) {
			    			for (int k = 0; k < rows; k++) {
				    			int numCell = k * cols + j;
				    			if (temporary[numCell] == val) {
				    				incorrect[numCell] = true;
				    			}
				    		}
				    		for (int k = 0; k < cols; k++) {
				    			int numCell = row * cols + k;
				    			if (temporary[numCell] == val) {
				    				incorrect[numCell] = true;
				    			}
				    		}
						    for (int x = (row / yLim) * (cols / xLim); x < (row / yLim + 1) * (cols / xLim); x++){
						    	for (int y = (j / xLim) * (cols / xLim); y < (j / xLim + 1) * (cols / xLim); y++) {
					    			int numCell = x * cols + y;
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
	    	for (int j = 0; j < cols; j++) {
    			int numCell = row * cols + j;
	    		if (incorrect[numCell] && temporary[numCell] != 0) {
    				field[numCell].setForeground(Color.ORANGE);
	    		} else {
	    			if (temporary[numCell] == 0) {
	    				field[numCell].setForeground(Color.RED);
		    		} else {
	    				field[numCell].setForeground(Color.GREEN);	
		    		}
	    		}
		    }
	    }
	    errorArea.setText(errortext);
		return correct;
	}



/*public void keyPressed(KeyEvent e) {

    int key = e.getKeyCode();

    //System.out.println(key);
}*/


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
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    double baselineWidth = 1920;
	    double baselineHeight = 1080;
	    double width = screenSize.getWidth();
	    double height = screenSize.getHeight();
	    double widthScaling = width / baselineWidth;
	    double heightScaling = height / baselineHeight;
	    int space = (int) (15 * widthScaling);
	    int x = space;
		int y = space;
		int w = (int) (60 * widthScaling);
		int h = (int) (60 * heightScaling);
		int fontsize = (int) (12 * heightScaling);
	    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
	    frame.addKeyListener(keyListener);
	    for (int row = 0; row < rows; row++){ 
	    	x = space;
	    	for (int col = 0; col < cols; col++) {
	    		int numCell = row * cols + col;
	    	    field[numCell] = new JButton(String.valueOf("0"));  
			    field[numCell].setMargin(new Insets(1,1,1,1));
			    field[numCell].setFont(new Font("Arial", Font.PLAIN, fontsize));
			    field[numCell].setBounds(x, y, w, h);
			    
			    field[numCell].addActionListener(new ActionListener(){  
			        public void actionPerformed(ActionEvent e) {  
			        	try {
			        		userInput[numCell] = selectedDigit;
			        		highlightCell(numCell);
			        		highlightDigit();
			        		field[numCell].setText(String.valueOf(selectedDigit));
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
			digitButtons[row].setFont(new Font("Arial", Font.PLAIN, fontsize));
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
	    w = (int) (135 * widthScaling);
	    y = space;
	    x += space;
        JButton uniqueButton = new JButton("Jedinstvenost rje�enja");  
		uniqueButton.setMargin(new Insets(1,1,1,1));
		uniqueButton.setBounds(x, y, w, h);
		uniqueButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
		uniqueButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		isOnlyOneSolution();
	        	    instructionArea.setText(solvingInstructions);
	        		checkIfCorrect();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		uniqueButton.addKeyListener(keyListener);
		y += h + space;
        JButton showStepButton = new JButton("Prika�i korake");  
        showStepButton.setMargin(new Insets(1,1,1,1));
        showStepButton.setBounds(x, y, w, h);
        showStepButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        showStepButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		showSteps = true;
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
		JButton clearGridButton = new JButton("Isprazni mre�u");  
        clearGridButton.setMargin(new Insets(1,1,1,1));
        clearGridButton.setBounds(x, y, w, h);
        clearGridButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        clearGridButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        	    for (int row = 0; row < rows; row++){
	        	    	for (int j = 0; j < cols; j++) {
	        	        	int numCell = row * cols + j;
	        	    		temporary[numCell] = 0;
	        	    		solution[numCell] = 0;
	        	    		userInput[numCell] = 0;
	        	    		field[numCell].setText("0");
	        	    		field[numCell].setForeground(Color.RED);
	        		    }
	        	    }
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		clearGridButton.addKeyListener(keyListener);
		y += h + space;
        JButton fillButton = new JButton("Nasumi�mo nadopuni");  
		fillButton.setMargin(new Insets(1,1,1,1));
		fillButton.setBounds(x, y, w, h);
		fillButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
		fillButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		fill();
	        		checkIfCorrect();
	        		difficulty.setText("");
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		fillButton.addKeyListener(keyListener);
		y += h + space;
        JButton removeButton = new JButton("Simetri�no ukloni");  
        removeButton.setMargin(new Insets(1,1,1,1));
        removeButton.setBounds(x, y, w, h);
        removeButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        removeButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		removeSymetricPair();
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
	        		checkIfCorrect();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		restoreButton.addKeyListener(keyListener);
		y += h + space;
		FileManipulator f = new FileManipulator();
		f.setSudoku(this);
        JButton fileSaveButton = new JButton("Spremi zagonetku");  
        fileSaveButton.setMargin(new Insets(1,1,1,1));
        fileSaveButton.setBounds(x, y, w, h);
        fileSaveButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        fileSaveButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		f.WriteToFile();
				} catch (Exception e1) {
					
				}
	        }  
	    });
		fileSaveButton.addKeyListener(keyListener);
		y += h + space;
        JButton fileReadButton = new JButton("U�itaj zagonetku");  
        fileReadButton.setMargin(new Insets(1,1,1,1));
        fileReadButton.setBounds(x, y, w, h);
        fileReadButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        fileReadButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (f.ReadFile() == 0) {
		        	    for (int row = 0; row < rows; row++){ 
		        	    	for (int j = 0; j < cols; j++) {
		        		    	int numCell = row * cols + j;
		        	    		solution[numCell] = 0;
		        	    		field[numCell].setText(String.valueOf(userInput[numCell]));
		        	    	}
		        	    }
		        		checkBoxes();
		        	    checkIfCorrect();
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		fileReadButton.addKeyListener(keyListener);
		y += h + space;
		w = (int) (200 * widthScaling);
        difficulty.setBounds(x, y, w, h);
        difficulty.setFont(new Font("Arial", Font.PLAIN, fontsize));
        frame.add(difficulty);
		int buttonEnd = y + h + space;
        x += w + space;
        w = (int) (250 * widthScaling);
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
        w = (int) (500 * widthScaling);
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
        frame.add(uniqueButton);
        frame.add(clearGridButton);
        frame.add(removeButton);
        frame.add(restoreButton);
        frame.add(fileSaveButton);
        frame.add(fileReadButton);
        frame.add(showStepButton);
	    frame.setSize(x, Math.max(digitEnd, buttonEnd) + (int) (40 * widthScaling));  
	    frame.setLayout(null);  
    }
}
