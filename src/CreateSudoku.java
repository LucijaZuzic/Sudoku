import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;

public class CreateSudoku extends Sudoku {

	public CreateSudoku(int x, int y, int xl, int yl, int[] br, int[] bn) {
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
		checkBoxes();
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
				    			if (temporary[num] == val) {
				    				incorrect[num] = true;
				    			}
				    		}
				    		for (int k = 0; k < cols; k++) {
				    			int num = i * cols + k;
				    			if (temporary[num] == val) {
				    				incorrect[num] = true;
				    			}
				    		}
						    for (int x = (i / ylim) * (cols / xlim); x < (i / ylim + 1) * (cols / xlim); x++){
						    	for (int y = (j / xlim) * (cols / xlim); y < (j / xlim + 1) * (cols / xlim); y++) {
					    			int num = x * cols + y;
						    		if (temporary[num] == val) {
					    				incorrect[num] = true;
						    		}
						    	}
						    }
			    		}
		    		}
			    }
		    }
		}
	    for (int i = 0; i < rows; i++){
	    	for (int j = 0; j < cols; j++) {
    			int num = i * cols + j;
	    		if (incorrect[num] && temporary[num] != 0) {
    				field[num].setForeground(Color.ORANGE);
	    		} else {
	    			if (temporary[num] == 0) {
	    				field[num].setForeground(Color.RED);
		    		} else {
	    				field[num].setForeground(Color.GREEN);	
		    		}
	    		}
		    }
	    }
	    errorArea.setText(errortext);
		return correct;
	}

	@Override
	public void draw () 
    {
		frame = new JFrame("Stvori sudoku");  
	    frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
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
			    field[num].setBorder(BorderFactory.createMatteBorder(tb, lb, bb, rb, Color.BLACK));
		    	int box = (i / ylim) * (cols / xlim) + (j / xlim);
		    	if (((box % (cols / xlim) % 2 == 0) && (box / (cols / xlim) % 2  == 0)) || 
		    		((box % (cols / xlim) % 2 != 0) && (box / (cols / xlim) % 2  == 1))) {
		    		field[num].setBackground(Color.WHITE);
		    	} else {
		    		field[num].setBackground(Color.GRAY);
		    	}
			    field[num].addActionListener(new ActionListener(){  
			        public void actionPerformed(ActionEvent e) {  
			        	try {
			        		userInput[num] = selectedDigit;
			    	        if (selectedDigit == 0) {
			    	        	field[num].setForeground(Color.RED);
			    	        } else {
			    	        	field[num].setForeground(Color.BLACK);
			    	        }
			        		field[num].setText(String.valueOf(selectedDigit));
						} catch (Exception e1) {
		
						}
			        }  
			    });
			    frame.add(field[num]);
		    	x += w;
		    }
		    y += h;
	    }

        JButton fillb = new JButton("Nasumièmo nadopuni");  
		fillb.setMargin(new Insets(1,1,1,1));
		fillb.setBounds(cols * w + 15 * 2, 15, cols * w / 4, h);
		fillb.setFont(new Font("Arial", Font.PLAIN, fontsize));
		fillb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		fill();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });

        JButton uniqueb = new JButton("Jedinstvenost rješenja");  
		uniqueb.setMargin(new Insets(1,1,1,1));
		uniqueb.setBounds(cols * w + 15 * 2, 15 + 15 + h, cols * w / 4, h);
		uniqueb.setFont(new Font("Arial", Font.PLAIN, fontsize));
		uniqueb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		isOnlyOneSolution();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });

        JButton correctb = new JButton("Ispravnost rješenja");  
        correctb.setMargin(new Insets(1,1,1,1));
        correctb.setBounds(cols * w + 15 * 2, 15 + 15 * 2 + h * 2, cols * w / 4, h);
        correctb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        correctb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		checkIfCorrect();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		x = 15;

        JButton removeb = new JButton("Simetrièno ukloni");  
        removeb.setMargin(new Insets(1,1,1,1));
        removeb.setBounds(cols * w + 15 * 2, 15 + 15 * 3 + h * 3, cols * w / 4, h);
        removeb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        removeb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		removeSymetricPair();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		x = 15;

        JButton restoreb = new JButton("Vrati uklonjeno");  
        restoreb.setMargin(new Insets(1,1,1,1));
        restoreb.setBounds(cols * w + 15 * 2, 15 + 15 * 4 + h * 4, cols * w / 4, h);
        restoreb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        restoreb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		restoreLastRemoved();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		x = 15;

		for (int i = 0; i < cols + 1; i++) {
	        JButton digib = new JButton(String.valueOf(i));  
	        digib.setMargin(new Insets(1,1,1,1));
	        digib.setBounds(x, rows * h + 15 * 2, w, h);
	        digib.setFont(new Font("Arial", Font.PLAIN, fontsize));
	        if (i == 0) {
	        	digib.setForeground(Color.RED);
	        }
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

        difficulty.setBounds(cols * w + 15 * 2, 15 * 6 + 15 + h * 6, 200, h);
        frame.add(difficulty);
        
        frame.add(fillb);
        frame.add(uniqueb);
        frame.add(correctb);
        frame.add(removeb);
        frame.add(restoreb);
	    frame.setSize(cols * w + 15 * 4 + cols * w / 4 + 100, (rows + 1) * h + 15 * 4 + 20);  
	    frame.setLayout(null);  
    }
	
	public static void main(String args[]) {
		//CreateSudoku s = new CreateSudoku(9, 9, 3, 3);
	}

}
