import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class ChangeBoxBorder extends Sudoku {
	int mode = 0;
	public ChangeBoxBorder(int x, int y, int xl, int yl) {
		super(x, y, xl, yl);
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	boxNumber[i * cols + j] = -1;
		    	border[i * cols + j] = -1;
	    		temporary[i * cols + j] = 0;
		    	int num = i * cols + j;
	    		userInput[num] = temporary[num];
	    		solution[num] = temporary[num];
		    }
	    }
	    draw();
		errorOutput();
		instructionOutput();
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	boxNumber[i * cols + j] = -1;
		    	border[i * cols + j] = -1;
	    		temporary[i * cols + j] = 0;
		    }
	    }
	    draw();
	    for (int i = 0; i < rows; i++){ 
	    	for (int j = 0; j < cols; j++) {
		    	int num = i * cols + j;
	    		userInput[num] = temporary[num];
	    		solution[num] = temporary[num];
	    		field[num].setText("");
	    	}
	    }
	    frame.setVisible(true);
	}

	@Override
	boolean checkIfCorrect() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	void draw() {
		frame = new JFrame("Promjeni kutiju za sudoku");  
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
        		border[num] = -1;
	    	    field[num] = new JButton("");  
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
	        		border[num] = 1;
		    		field[num].setBackground(Color.BLACK);
		    	} else {
	        		border[num] = 0;
		    		field[num].setBackground(Color.GRAY);
		    	}
			    field[num].addActionListener(new ActionListener(){  
			        public void actionPerformed(ActionEvent e) {  
			        	try {
			        		if (mode == 0)  {
					    		field[num].setBackground(Color.GRAY);
				        		border[num] = 0;
				        		checkBoxes();
			        		}
			        		if (mode == 1)  {
					    		field[num].setBackground(Color.BLACK);
				        		border[num] = 1;
				        		checkBoxes();
			        		} 
			        		if (mode == 2)  {
					    		field[num].setBackground(Color.YELLOW);
				        		border[num] = 2;
				        		checkBoxes();
			        		} 
			        		if (mode == 3)  {
					    		field[num].setBackground(Color.ORANGE);
				        		border[num] = 3;
				        		checkBoxes();
			        		} 
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
	    
	    JButton modeb = new JButton("Sivo");  
        modeb.setMargin(new Insets(1,1,1,1));
        modeb.setBounds(cols * w + 15 * 2, 15, 9 * w / 4, h);
        modeb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        modeb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (mode == 0) {
	        			mode = 1;
	        			modeb.setText(String.valueOf("Crno"));
	        			return;
	        		} 
	        		if (mode == 1) {
	        			mode = 2;
	        			modeb.setText(String.valueOf("Žuto"));
	        			return;
	        		}
	        		if (mode == 2) {
	        			mode = 3;
	        			modeb.setText(String.valueOf("Naranèasto"));
	        			return;
	        		}
	        		if (mode == 3) {
	        			mode = 0;
	        			modeb.setText(String.valueOf("Sivo"));
	        			return;
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });

        JButton designb = new JButton("Dizajniraj zagonetku");  
        designb.setMargin(new Insets(1,1,1,1));
        designb.setBounds(cols * w + 15 * 2, 15 * 2 + 15 + h * 2, 9 * w / 4, h);
        designb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        designb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (checkBoxes()) {
	        			CreateSudoku s = new CreateSudoku(rows, cols, xlim, ylim, border, boxNumber);
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });


		FileManipulator f = new FileManipulator();
		f.setSudoku(this);
        
        JButton designcontb = new JButton("Nastavi dizajn");  
        designcontb.setMargin(new Insets(1,1,1,1));
        designcontb.setBounds(cols * w + 15 * 2, 15 + 15 + h, 9 * w / 4, h);
        designcontb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        designcontb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (f.ReadFile() == 0) {
	        			CreateSudoku s = new CreateSudoku(rows, cols, xlim, ylim, border, boxNumber, userInput);
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
        
	    JLabel miniLabel = new JLabel("Minimalna težina: ");
	    miniLabel.setBounds(cols * w + 15 * 2, 15 * 5 + 15 + h * 10 / 2, 200, h / 2);
	    frame.add(miniLabel);
	    
	    JTextField mini = new JTextField(String.valueOf(mintargetDifficulty));
        mini.setBounds(cols * w + 15 * 2 + 130, 15 * 5 + 15 + h * 10 / 2, 100, h / 2);
	    frame.add(mini);

	    JLabel maksiLabel = new JLabel("Maksimalna težina: ");
	    maksiLabel.setBounds(cols * w + 15 * 2, 15 * 6 + 15 + h * 11 / 2, 200, h / 2);
	    frame.add(maksiLabel);
	    
	    JTextField maksi = new JTextField(String.valueOf(maxtargetDifficulty));
	    maksi.setBounds(cols * w + 15 * 2 + 130, 15 * 6 + 15 + h * 11 / 2, 100, h / 2);
	    frame.add(maksi);

        JButton solverandomb = new JButton("Riješi nasumièno");  
        solverandomb.setMargin(new Insets(1,1,1,1));
        solverandomb.setBounds(cols * w + 15 * 2, 15 * 3 + 15 + h * 3, 9 * w / 4, h);
        solverandomb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        solverandomb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (checkBoxes()) {
	        			SolveSudoku s = new SolveSudoku(rows, cols, xlim, ylim, border, boxNumber, Integer.parseInt(mini.getText()), Integer.parseInt(maksi.getText()));
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });

        JButton solveb = new JButton("Riješi spremljeno");  
        solveb.setMargin(new Insets(1,1,1,1));
        solveb.setBounds(cols * w + 15 * 2, 15 * 4 + 15 + h * 4, 9 * w / 4, h);
        solveb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        solveb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (f.ReadFile() == 0) {
	        			SolveSudoku s = new SolveSudoku(rows, cols, xlim, ylim, border, boxNumber, userInput);
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
        frame.add(modeb);
        frame.add(designb);
        frame.add(designcontb);
        frame.add(solverandomb);
        frame.add(solveb);
	    frame.setSize(cols * w + 15 * 4 + cols * w / 4 + 145, Math.max((rows + 1) * h + 15 * 4 + 20, 15 * 7 + 15 + h * 7));  
	    frame.setLayout(null); 
	}

	/*public static void main(String args[]) {
		ChangeBoxBorder s = new ChangeBoxBorder(9, 9, 3, 3);
	}*/
}
