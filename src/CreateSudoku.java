import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

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
		checkBoxes();
	    frame.setVisible(true);
	    checkIfCorrect();
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



public void keyPressed(KeyEvent e) {

    int key = e.getKeyCode();

    //System.out.println(key);
}


	@Override
	public void draw () 
    {
		frame = new JFrame("Stvori sudoku");  
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
			    field[num].addActionListener(new ActionListener(){  
			        public void actionPerformed(ActionEvent e) {  
			        	try {
			        		userInput[num] = selectedDigit;
			    	        /*if (selectedDigit == 0) {
			    	        	field[num].setForeground(Color.RED);
			    	        } else {
			    	        	field[num].setForeground(Color.WHITE);
			    	        }*/
			        		field[num].setText(String.valueOf(selectedDigit));
			        		checkIfCorrect();
						} catch (Exception e1) {
		
						}
			        }  
			    });
			    field[num].addKeyListener(
				    	new KeyListener(){
				    		public void keyPressed(KeyEvent e) {
				    			int key = e.getKeyCode();
				    			if (key - 48 >= 0 && key - 48 <= 9) {
				    				selectedDigit = key - 48;
				    			}
				    		}
							public void keyReleased(KeyEvent e) {}
							public void keyTyped(KeyEvent e) {}
						}
			    	);
			    frame.add(field[num]);
		    	x += w;
		    }
		    y += h;
	    }

        JButton fillb = new JButton("Nasumièmo nadopuni");  
		fillb.setMargin(new Insets(1,1,1,1));
		fillb.setBounds((cols + 1) * w + 15 * 2, 15, 9 * w / 4, h);
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

        JButton uniqueb = new JButton("Jedinstvenost rješenja");  
		uniqueb.setMargin(new Insets(1,1,1,1));
		uniqueb.setBounds((cols + 1) * w + 15 * 2, 15 + 15 + h, 9 * w / 4, h);
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

        JButton removeb = new JButton("Simetrièno ukloni");  
        removeb.setMargin(new Insets(1,1,1,1));
        removeb.setBounds((cols + 1) * w + 15 * 2, 15 + 15 * 3 + h * 3, 9 * w / 4, h);
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

        JButton restoreb = new JButton("Vrati uklonjeno");  
        restoreb.setMargin(new Insets(1,1,1,1));
        restoreb.setBounds((cols + 1) * w + 15 * 2, 15 + 15 * 4 + h * 4, 9 * w / 4, h);
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
		FileManipulator f = new FileManipulator();
		f.setSudoku(this);
        JButton filesaveb = new JButton("Spremi zagonetku");  
        filesaveb.setMargin(new Insets(1,1,1,1));
        filesaveb.setBounds((cols + 1) * w + 15 * 2, 15 + 15 * 5 + h * 5, 9 * w / 4, h);
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
        

        JButton filereadb = new JButton("Uèitaj zagonetku");  
        filereadb.setMargin(new Insets(1,1,1,1));
        filereadb.setBounds((cols + 1) * w + 15 * 2, 15 + 15 * 6 + h * 6, 9 * w / 4, h);
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
		        		checkIfCorrect();
					} catch (Exception e1) {
		
		
					}
		        }  
		    });
	        frame.add(digib);
	        x += w;
		}

        difficulty.setBounds((cols + 1) * w + 15 * 2, 15 * 7 + 15 + h * 7, 200, h / 2);
        frame.add(difficulty);
        
        errorArea = new JTextArea(0, 0);
        errorArea.setEditable (false);
	    JPanel errorpanel = new JPanel();
        errorpanel.add(errorArea, BorderLayout.CENTER);
	    JScrollPane errorscroll = new JScrollPane(errorpanel, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    errorscroll.setBounds((cols + 1)  * w + 15 * 2 + 200 + 15, 15, 250, Math.max((rows + 1) * h + 15, 15 * 7 + h * 7 + h / 2));
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
	    instructionscroll.setBounds((cols + 1)  * w + 15 * 2 + 200 + 15 + 15 + 250, 15, 350, Math.max((rows + 1) * h + 15, 15 * 7 + h * 7 + h / 2));
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
	    frame.setSize((cols + 1)  * w + 15 * 2 + 200 + 15 + 15 + 250 + 350 + 30, Math.max((rows + 1) * h + 15 * 3 + 40, 15 * 9 + h * 7 + h / 2 + 40));  
	    frame.setLayout(null);  
    }
	
}
