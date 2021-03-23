import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class SolveSudoku extends Sudoku {

	int[] backup;
	int[][] options;
	int mode = 0;
	int[] result;
	int numerrors = 0;
	int penalty = 0;
	long startTime;
	long elapsedTime;
	JLabel timeLabel;
	public SolveSudoku(int x, int y, int xl, int yl, int[] br, int[] bn, int mint, int maxt) {
		super(x, y, xl, yl);
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
		errorOutput();
		instructionOutput();
		options = new int[x * y][x];
		result = new int[x * y];
		checkBoxes();
		selectedDigit = 1;
	    int solvable = 0;
	    int numReturns = 0;
	    mintargetDifficulty = mint;
	    maxtargetDifficulty = maxt;
	    while (solvable == 0 || difficultyScore > maxtargetDifficulty || difficultyScore < mintargetDifficulty) {
		    removeSymetricPair();
		    solvable = isOnlyOneSolution();
		    System.out.println(String.valueOf(difficultyScore));
	    	if (solvable == 0) {
	    		numReturns++;
	    		restoreLastRemoved();
			    solvable = isOnlyOneSolution();
			    System.out.println("backtrack " + String.valueOf(difficultyScore));
	    	} else {
	    		numReturns = 0;
	    	}
	    	if (numReturns >= rows * cols / 2) {
	    		while (restoreLastRemoved()) {
	    			
	    		}
	    		difficultyScore = 0;
	    		numReturns = 0;
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
    	        	field[num].setText("<html><font color='red'>" + String.valueOf(userInput[num]) + "</html></font>");
	    		}
	    		for (int k = 0; k < rows; k++) {
	    			options[num][k] = 0;
	    		}
	    	}	
	    }
	    frame.setVisible(true);
	    instructionFrame.setVisible(true);
	    errorFrame.setVisible(true);
	}

	@Override
	public boolean checkIfCorrect() {
		String errortext = "";
		boolean incorrect[] = new boolean[rows * cols];
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
		    	field[i * cols + j].setForeground(Color.WHITE);
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
			    			errortext += val + ": " + "(" + (i + 1) + ", " + (j + 1) + ") Broj " + val + " ve� postoji u retku " + (i + 1) + "\n";
			    			correct = false;
			    			status = true;
			    			incorrect[i * cols + j] = true;
			    		}
			    		if (usedCols[j] > 1) {
			    			errortext += val + ": " + "(" + (i + 1) + ", " + (j + 1) + ") Broj " + val + " ve� postoji u stupcu " + (j + 1) + "\n";
			    			correct = false;
			    			status = true;
			    			incorrect[i * cols + j] = true;
			    		}
			    		if (usedBoxes[boxNumber[i * cols + j]] > 1) {
			    			errortext += val + ": " + "(" + (i + 1) + ", " + (j + 1) + ") Broj " + val + " ve� postoji u kutiji " + (boxNumber[i * cols + j] + 1) + "\n";
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
    				field[num].setForeground(Color.ORANGE);
	    		} else {
    				field[num].setForeground(Color.WHITE);
	    		}
		    }
	    }
	    if (localnumerrors > numerrors) {
	    	//penalty += localnumerrors - numerrors;
	    	penalty++;
	    }
	    errorArea.setText(errortext);
	    numerrors = localnumerrors;
	    penaltyLabel.setText("Kazneni bodovi: " + String.valueOf(penalty));
		return correct;
	}
	
	public boolean showerror() {
		boolean haserrors = false;
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
	    		if (backup[num] != 0) {
	    			continue;
	    		}
	    		if (userInput[num] != result[num]) {
    	        	field[num].setForeground(Color.RED);
    	        	haserrors = true;
	    		} else {
    	        	field[num].setForeground(Color.GREEN);
	    		}
	    	}	
	    }
		return haserrors;
	}

	@Override
	public void draw () 
    {
		frame = new JFrame("Rije�i sudoku");  
	    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);

	    int x = 15;
		int y = 15;
		int w = 60;
		int h = 60;
		int fontsize = 12;

	    for (int i = 0; i < rows; i++){ 
	    	x = 15;
	    	for (int j = 0; j < cols; j++) {
	    		int num = i * cols + j;
	    	    field[num] = new JButton(String.valueOf("0"));  
			    field[num].setMargin(new Insets(1,1,1,1));
			    field[num].setFont(new Font("Arial", Font.PLAIN, fontsize));
			    field[num].setBounds(x, y, w, h);
			    int lb = 1;
			    int rb = 1;
			    int tb = 1;
			    int bb = 1;
			    if (j % xlim== 0) {
			    	if (j != cols - 1 && j != 0) {
				    	lb = 3;
			    	} else {
			    		lb = 4;
			    	}
			    }
			    if (j % xlim == (xlim - 1)) {
			    	if (j != cols - 1 && j != 0) {
				    	rb = 3;
			    	} else {
			    		rb = 4;
			    	}
			    }
			    if (i % ylim == 0) {
			    	if (i != rows - 1 && i != 0) {
				    	tb = 3;
			    	} else {
			    		tb = 4;
			    	}
			    }
			    if (i % ylim == (ylim - 1)) {
			    	if (i != rows - 1 && i != 0) {
				    	bb = 3;
			    	} else {
			    		bb = 4;
			    	}
			    }
			    field[num].setBorder(BorderFactory.createMatteBorder(tb, lb, bb, rb, Color.WHITE));
		    	int box = (i / ylim) * (cols / xlim) + (j / xlim);
		    	if (((box % (cols / xlim) % 2 == 0) && (box / (cols / xlim) % 2  == 0)) || 
		    		((box % (cols / xlim) % 2 != 0) && (box / (cols / xlim) % 2  == 1))) {
		    		field[num].setBackground(Color.BLACK);
		    	} else {
		    		field[num].setBackground(Color.GRAY);
		    	}
			    field[num].addActionListener(new ActionListener(){  
			        public void actionPerformed(ActionEvent e) {  
			        	try {
			        		if (mode == 0)  {
				        		for (int k = 0; k < cols; k++) {
					        		options[num][k] = 0;
				        		}
				        		options[num][selectedDigit - 1] = 1;
				        		userInput[num] = selectedDigit;
				        		field[num].setText(String.valueOf(selectedDigit));
			        		} else {
				        		if (options[num][selectedDigit - 1] == 1) {
					        		options[num][selectedDigit - 1] = 0;
				        		} else {
					        		options[num][selectedDigit - 1] = 1;
				        		}
				        		userInput[num] = 0;
					    		String t = "<html><font color='yellow'>";
					    		int z = 0;
						    	for (int k = 0; k < cols; k++) {
						    		if (options[num][k] == 1) {
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
						    		t = t.substring(0, t.length() - 1) + "</font></html>";
						    	} else {
						    		t = "";
						    	}
				        		field[num].setText(t);
			        		}
			        		checkIfCorrect();
						} catch (Exception e1) {
		
						}
			        }  
			    });
			    frame.add(field[num]);
		    	x += w;
		    }
		    y += h;
	    }
	    x = 15;
		for (int i = 1; i < cols + 1; i++) {
	        JButton digib = new JButton(String.valueOf(i));  
	        digib.setMargin(new Insets(1,1,1,1));
	        digib.setBounds(x, rows * h + 15 * 2, w, h);
	        digib.setFont(new Font("Arial", Font.PLAIN, fontsize));
	        int digit = i;
	        digib.addActionListener(new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		selectedDigit = digit;
					} catch (Exception e1) {
		
		
					}
		        }  
		    });
	        frame.add(digib);
	        x += w;
		}
		
        JButton solvedb = new JButton("Ispravnost rje�enja");  
        solvedb.setMargin(new Insets(1,1,1,1));
        solvedb.setBounds(cols * w + 15 * 2, 15, 9 * w / 4, h);
        solvedb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        solvedb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		showerror();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });

        JButton modeb = new JButton("Bilje�ke ISKLJU�ENE");  
        modeb.setMargin(new Insets(1,1,1,1));
        modeb.setBounds(cols * w + 15 * 2, 15 + 15 + h, 9 * w / 4, h);
        modeb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        modeb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (mode == 1) {
	        			modeb.setText("Bilje�ke ISKLJU�ENE");
	        			mode = 0;
	        		} else {
	        			modeb.setText("Bilje�ke UKLJU�ENE");
	        			mode = 1;
	        		}
				} catch (Exception e1) {
	
				}
	        }  
	    });

        difficulty.setBounds(cols * w + 15 * 2, 15 * 2 + 15 + h * 2, 200, h / 2);
        frame.add(difficulty);

        penaltyLabel.setBounds(cols * w + 15 * 2, 15 * 3 + 15 + h * 3, 9 * w / 4, h / 2);
        frame.add(penaltyLabel);
        
        startTime = System.currentTimeMillis();
        elapsedTime = 0L;


	    timeLabel = new JLabel("Proteklo vrijeme: : ");
	    timeLabel.setBounds(cols * w + 15 * 2, 15 * 4 + 15 + h * 4, 9 * w / 4, h / 2);
	    frame.add(timeLabel);
        
        frame.add(solvedb);
        frame.add(modeb);

	    frame.setSize(cols * w + 15 * 4 + cols * w / 4 + 100, (rows + 1) * h + 15 * 4 + 20);  
	    frame.setLayout(null);  
    }
	

}
