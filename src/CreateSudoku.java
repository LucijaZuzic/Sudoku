import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
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
	JButton digibs[];

	public void highlightcell(int num) {
	    for (int i2 = 0; i2 < rows; i2++){
	    	for (int j2 = 0; j2 < cols; j2++) {
	    		if (j2 == num % cols || i2 == num / cols) {
	    			field[i2 * cols + j2].setBackground(new Color(119, 136, 153));
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

	public void highlightdigit() {
		for (int j = 0; j < cols + 1; j++) {
			digibs[j].setBackground(Color.WHITE);
		}
		digibs[selectedDigit].setBackground(Color.CYAN);
	    for (int i2 = 0; i2 < rows; i2++){
	    	for (int j2 = 0; j2 < cols; j2++) {
	    		if (userInput[i2 * cols + j2] == selectedDigit && selectedDigit != 0) {
	    			field[i2 * cols + j2].setFont(field[i2 * cols + j2].getFont().deriveFont(Font.BOLD | Font.ITALIC));
	    		} else {    				    			
	    			field[i2 * cols + j2].setFont(field[i2 * cols + j2].getFont().deriveFont(~Font.BOLD | ~Font.ITALIC));
	    		}
	    	}
	    }
	}
	
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
		checkBoxes();
	    frame.setVisible(true);
	    checkIfCorrect();
	    frame.requestFocus();
	}
	

	public CreateSudoku(int x, int y, int xl, int yl, int[] br, int[] bn, int ui[]) {
		super(x, y, xl, yl);
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
		checkBoxes();
	    checkIfCorrect();
	    frame.setVisible(true);
	    frame.requestFocus();
	}

	@Override
	public boolean checkIfCorrect() {
		String errortext = "";
		boolean incorrect[] = new boolean[rows * cols];
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



/*public void keyPressed(KeyEvent e) {

    int key = e.getKeyCode();

    //System.out.println(key);
}*/


	KeyListener k  =
	new KeyListener(){
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			if (key - 48 >= 0 && key - 48 <= 9) {
				selectedDigit = key - 48;
				resethighlight();
				highlightdigit();
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
	    	    field[num] = new JButton(String.valueOf("0"));  
			    field[num].setMargin(new Insets(1,1,1,1));
			    field[num].setFont(new Font("Arial", Font.PLAIN, fontsize));
			    field[num].setBounds(x, y, w, h);
			    
			    field[num].addActionListener(new ActionListener(){  
			        public void actionPerformed(ActionEvent e) {  
			        	try {
			        		userInput[num] = selectedDigit;
			        		highlightcell(num);
			        		highlightdigit();
			        		field[num].setText(String.valueOf(selectedDigit));
			        		checkIfCorrect();
						} catch (Exception e1) {
		
						}
			        }  
			    });
			    field[num].addKeyListener(k);
			    frame.add(field[num]);
		    	x += w;
		    }
		    y += h;
	    }

        JButton fillb = new JButton("Nasumièmo nadopuni");  
		fillb.setMargin(new Insets(1,1,1,1));
		fillb.setBounds((cols + 1) * w + 15 * 2, 15 + 15 * 3 + h * 3, 9 * w / 4, h);
		fillb.setFont(new Font("Arial", Font.PLAIN, fontsize));
		fillb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		fill();
	        		checkIfCorrect();
	        		difficulty.setText("");
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		fillb.addKeyListener(k);
        JButton uniqueb = new JButton("Jedinstvenost rješenja");  
		uniqueb.setMargin(new Insets(1,1,1,1));
		uniqueb.setBounds((cols + 1) * w + 15 * 2, 15, 9 * w / 4, h);
		uniqueb.setFont(new Font("Arial", Font.PLAIN, fontsize));
		uniqueb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		isOnlyOneSolution();
	        	    instructionArea.setText(solvingInstructions);
	        		checkIfCorrect();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		uniqueb.addKeyListener(k);
		JButton cleargrid = new JButton("Isprazni mrežu");  
        cleargrid.setMargin(new Insets(1,1,1,1));
        cleargrid.setBounds((cols + 1) * w + 15 * 2, 15 + 15 * 2 + h * 2, 9 * w / 4, h);
        cleargrid.setFont(new Font("Arial", Font.PLAIN, fontsize));
        cleargrid.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        	    for (int i = 0; i < rows; i++){
	        	    	for (int j = 0; j < cols; j++) {
	        	        	int num = i * cols + j;
	        	    		temporary[num] = 0;
	        	    		solution[num] = 0;
	        	    		userInput[num] = 0;
	        	    		field[num].setText("0");
	        	    		field[num].setForeground(Color.RED);
	        		    }
	        	    }
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		x = 15;
		cleargrid.addKeyListener(k);
        JButton removeb = new JButton("Simetrièno ukloni");  
        removeb.setMargin(new Insets(1,1,1,1));
        removeb.setBounds((cols + 1) * w + 15 * 2, 15 + 15 * 4 + h * 4, 9 * w / 4, h);
        removeb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        removeb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		removeSymetricPair();
	        		checkIfCorrect();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		x = 15;
		removeb.addKeyListener(k);
        JButton restoreb = new JButton("Vrati uklonjeno");  
        restoreb.setMargin(new Insets(1,1,1,1));
        restoreb.setBounds((cols + 1) * w + 15 * 2, 15 + 15 * 5 + h * 5, 9 * w / 4, h);
        restoreb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        restoreb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		restoreLastRemoved();
	        		checkIfCorrect();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		x = 15;
		restoreb.addKeyListener(k);
		FileManipulator f = new FileManipulator();
		f.setSudoku(this);
        JButton filesaveb = new JButton("Spremi zagonetku");  
        filesaveb.setMargin(new Insets(1,1,1,1));
        filesaveb.setBounds((cols + 1) * w + 15 * 2, 15 + 15 * 6 + h * 6, 9 * w / 4, h);
        filesaveb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        filesaveb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		f.WriteToFile();
				} catch (Exception e1) {
					
				}
	        }  
	    });
		x = 15;
		filesaveb.addKeyListener(k);
        JButton filereadb = new JButton("Uèitaj zagonetku");  
        filereadb.setMargin(new Insets(1,1,1,1));
        filereadb.setBounds((cols + 1) * w + 15 * 2, 15 + 15 * 7 + h * 7, 9 * w / 4, h);
        filereadb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        filereadb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (f.ReadFile() == 0) {
		        	    for (int i = 0; i < rows; i++){ 
		        	    	for (int j = 0; j < cols; j++) {
		        		    	int num = i * cols + j;
		        	    		solution[num] = 0;
		        	    		field[num].setText(String.valueOf(userInput[num]));
		        	    	}
		        	    }
		        		checkBoxes();
		        	    checkIfCorrect();
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		x = 15;
		filereadb.addKeyListener(k);
		digibs = new JButton[cols + 1];
		for (int i = 0; i < cols + 1; i++) {
			digibs[i] = new JButton(String.valueOf(i));  
			digibs[i].setMargin(new Insets(1,1,1,1));
			if (i != selectedDigit) {
				digibs[i].setBackground(Color.WHITE);
			} else {
				digibs[i].setBackground(Color.CYAN);
			}
			digibs[i].setBounds(x, rows * h + 15 * 2, w, h);
			digibs[i].setFont(new Font("Arial", Font.PLAIN, fontsize));
	        if (i == 0) {
	        	digibs[i].setForeground(Color.RED);
	        }
	        int digit = i;
	        digibs[i].addActionListener(new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
		        	try {
		        		selectedDigit = digit;
						resethighlight();
						highlightdigit();
		        		checkIfCorrect();
					} catch (Exception e1) {
		
		
					}
		        }  
		    });
	        digibs[i].addKeyListener(k);
	        frame.add(digibs[i]);
	        x += w;
		}
		filesaveb.addKeyListener(k);

        JButton showstepb = new JButton("Prikaži korake");  
        showstepb.setMargin(new Insets(1,1,1,1));
        showstepb.setBounds((cols + 1) * w + 15 * 2, 15 + 15 * 1 + h * 1, 9 * w / 4, h);
        showstepb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        showstepb.addActionListener(new ActionListener(){  
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
        showstepb.addKeyListener(k);
        difficulty.setBounds((cols + 1) * w + 15 * 2, 15 * 8 + 15 + h * 8, 200, h / 2);
        frame.add(difficulty);
        
        errorArea = new JTextArea(0, 0);
        errorArea.setEditable (false);
	    JPanel errorpanel = new JPanel();
        errorpanel.add(errorArea, BorderLayout.CENTER);
	    JScrollPane errorscroll = new JScrollPane(errorpanel, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    errorscroll.setBounds((cols + 1)  * w + 15 * 2 + 200 + 15, 15, 250, Math.max((rows + 1) * h + 15, 15 * 8 + h * 8 + h / 2));
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
	    instructionscroll.setBounds((cols + 1)  * w + 15 * 2 + 200 + 15 + 15 + 250, 15, 500, Math.max((rows + 1) * h + 15, 15 * 8 + h * 8 + h / 2));
	    frame.add(instructionscroll);
	    instructionpanel.setVisible(true);  
	    instructionpanel.setBackground(Color.WHITE);
	    instructionscroll.setVisible(true);  

        frame.add(fillb);
        frame.add(uniqueb);
        frame.add(cleargrid);
        frame.add(removeb);
        frame.add(restoreb);
        frame.add(filesaveb);
        frame.add(filereadb);
        frame.add(showstepb);
	    frame.setSize((cols + 1)  * w + 15 * 2 + 200 + 15 + 15 + 750 + 30, Math.max((rows + 1) * h + 15 * 3 + 40, 15 * 10 + h * 8 + h / 2 + 40));  
	    frame.setLayout(null);  
    }
}
