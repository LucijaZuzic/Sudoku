import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;

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
	    		field[num].setText(String.valueOf(userInput[num]));
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
        		border[num] = -1;
	    	    field[num] = new JButton(String.valueOf(userInput[num]));  
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
				        		field[num].setText(String.valueOf("0"));
					    		field[num].setBackground(Color.GRAY);
				        		border[num] = 0;
				        		checkBoxes();
			        		} else {
				        		field[num].setText(String.valueOf("1"));
					    		field[num].setBackground(Color.BLACK);
				        		border[num] = 1;
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
		
        JButton setb = new JButton("Crno-Bijelo");  
        setb.setMargin(new Insets(1,1,1,1));
        setb.setBounds(cols * w + 15 * 2, 15 + 15 + h, cols * w / 4, h);
        setb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        setb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (mode == 0) {
	        			mode = 1;
	        		} else {
	        			mode = 0;
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });

        JButton addb = new JButton("Flood Fill");  
        addb.setMargin(new Insets(1,1,1,1));
        addb.setBounds(cols * w + 15 * 2, 15 * 2 + 15 + h * 2, cols * w / 4, h);
        addb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        addb.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (checkBoxes()) {
	        			CreateSudoku s = new CreateSudoku(9, 9, 3, 3, border, boxNumber);
	        			System.out.println("yes");
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });


        
        difficulty.setBounds(cols * w + 15 * 2, 15 * 4 + 15 + h * 4, 200, h);
        frame.add(difficulty);

        penaltyLabel.setBounds(cols * w + 15 * 2, 15 * 4 + 15 + h * 4, cols * w / 4, h);
        frame.add(penaltyLabel);
        
        frame.add(setb);
        frame.add(addb);
	    frame.setSize(cols * w + 15 * 4 + cols * w / 4 + 100, (rows + 1) * h + 15 * 4 + 20);  
	    frame.setLayout(null); 
	}

	public static void main(String args[]) {
		ChangeBoxBorder s = new ChangeBoxBorder(9, 9, 3, 3);
	}
}
