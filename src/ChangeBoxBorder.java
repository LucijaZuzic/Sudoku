import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class ChangeBoxBorder extends Sudoku {
	int mode = 0;
	public ChangeBoxBorder(int contructRows, int constructCols, int xl, int yl) {
		super(constructCols, contructRows, xl, yl);
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	boxNumber[row * cols + col] = -1;
		    	border[row * cols + col] = -1;
	    		temporary[row * cols + col] = 0;
		    	int num = row * cols + col;
	    		userInput[num] = temporary[num];
	    		solution[num] = temporary[num];
		    }
	    }
	    draw();
	    frame.setVisible(true);
	}

	@Override
	boolean checkIfCorrect() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void draw() {
		frame = new JFrame("Promjeni kutiju za sudoku");  
	    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
	    int space = 15;
	    int x = space;
		int y = space;
		int w = 60;
		int h = 60;
		int fontsize = 12;

	    for (int row = 0; row < rows; row++){ 
	    	x = 15;
	    	for (int col = 0; col < cols; col++) {
	    		int numCell = row * cols + col;
        		border[numCell] = -1;
	    	    field[numCell] = new JButton("");  
			    field[numCell].setMargin(new Insets(1,1,1,1));
			    field[numCell].setFont(new Font("Arial", Font.PLAIN, fontsize));
			    field[numCell].setBounds(x, y, w, h);
			    int lb = 1;
			    int rb = 1;
			    int tb = 1;
			    int bb = 1;
			    if (col % xlim== 0) {
			    	if (col != cols - 1 && col != 0) {
				    	lb = 3;
			    	} else {
			    		lb = 4;
			    	}
			    }
			    if (col % xlim == (xlim - 1)) {
			    	if (col != cols - 1 && col != 0) {
				    	rb = 3;
			    	} else {
			    		rb = 4;
			    	}
			    }
			    if (row % ylim == 0) {
			    	if (row != rows - 1 && row != 0) {
				    	tb = 3;
			    	} else {
			    		tb = 4;
			    	}
			    }
			    if (row % ylim == (ylim - 1)) {
			    	if (row != rows - 1 && row != 0) {
				    	bb = 3;
			    	} else {
			    		bb = 4;
			    	}
			    }
			    field[numCell].setBorder(BorderFactory.createMatteBorder(tb, lb, bb, rb, Color.WHITE));
		    	int box = (row / ylim) * (cols / xlim) + (col / xlim);
		    	if (((box % (cols / xlim) % 2 == 0) && (box / (cols / xlim) % 2  == 0)) || 
		    		((box % (cols / xlim) % 2 != 0) && (box / (cols / xlim) % 2  == 1))) {
	        		border[numCell] = 1;
		    		field[numCell].setBackground(Color.BLACK);
		    	} else {
	        		border[numCell] = 0;
		    		field[numCell].setBackground(Color.GRAY);
		    	}
			    field[numCell].addActionListener(new ActionListener(){  
			        public void actionPerformed(ActionEvent e) {  
			        	try {
			        		showBoxMsg = false;
			        		if (mode == 0)  {
					    		field[numCell].setBackground(Color.GRAY);
				        		border[numCell] = 0;
				        		checkBoxes();
			        		}
			        		if (mode == 1)  {
					    		field[numCell].setBackground(Color.BLACK);
				        		border[numCell] = 1;
				        		checkBoxes();
			        		} 
			        		if (mode == 2)  {
					    		field[numCell].setBackground(Color.DARK_GRAY);
				        		border[numCell] = 2;
				        		checkBoxes();
			        		} 
			        		if (mode == 3)  {
					    		field[numCell].setBackground(Color.LIGHT_GRAY);
				        		border[numCell] = 3;
				        		checkBoxes();
			        		} 
			        		showBoxMsg = true;
						} catch (Exception e1) {
		
						}
			        }  
			    });
			    frame.add(field[numCell]);
		    	x += w;
		    }
		    y += h;
	    }
		int digitEnd = y + space;
	    h = h / 2;
	    w = 135;
	    y = space;
	    x += space;
	    
	    JLabel rowLabel = new JLabel("Broj redaka mreže: ");
	    rowLabel.setBounds(x, y, w, h);
	    frame.add(rowLabel);
	    
	    JTextField row = new JTextField(String.valueOf(rows));
        row.setBounds(x + w, y, h, h);
	    frame.add(row);

	    y += h;
	    
	    JLabel xlimLabel = new JLabel("Broj redaka kutije: ");
	    xlimLabel.setBounds(x, y, w, h);
	    frame.add(xlimLabel);
	    
	    JTextField xlimval = new JTextField(String.valueOf(ylim));
	    xlimval.setBounds(x + w, y, h, h);
	    frame.add(xlimval);

	    y += h;
	    
	    JLabel colLabel = new JLabel("Broj stupaca mreže: ");
	    colLabel.setBounds(x, y, w, h);
	    frame.add(colLabel);
	    
	    JTextField col = new JTextField(String.valueOf(cols));
	    col.setBounds(x + w, y, h, h);
	    frame.add(col);

	    y += h;
	    
	    JLabel ylimLabel = new JLabel("Broj stupaca kutije: ");
	    ylimLabel.setBounds(x, y, w, h);
	    frame.add(ylimLabel);
	    
	    JTextField ylimval = new JTextField(String.valueOf(xlim));
	    ylimval.setBounds(x + w, y, h, h);
	    frame.add(ylimval);

	    y += h + space;
	   
	    JButton createb = new JButton("Izmjeni dimenzije");
	    createb.setBounds(x, y, w, h);
	    createb.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    createb.addActionListener(new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		int pr = Integer.parseInt(row.getText());
	        		int pc = Integer.parseInt(col.getText());
	        		int yl = Integer.parseInt(ylimval.getText());
	        		int xl = Integer.parseInt(xlimval.getText());
        			if (pr != pc) {
        				InformationBox.infoBox("Zagonetka nije kvadratna.", "Stvaranje zagonetke");
        				return;
        			}
        			if (yl * xl > pr) {
        				InformationBox.infoBox("Kutije imaju previše znamenki.", "Stvaranje zagonetke");
        				return;
        			}
        			if (yl * xl < pr) {
        				InformationBox.infoBox("Kutije imaju premalo znamenki.", "Stvaranje zagonetke");
        				return;
        			}

        			rows = pr;
        			cols = pc;
        			xlim = yl;
        			ylim = xl;
        		    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        		    frame.removeAll();
        		    frame.dispose();
        		    frame.setVisible(false);
	        		ChangeBoxBorder b = new ChangeBoxBorder(pr, pc, yl, xl);

				} catch (Exception e1) {

				}
	        }  
	    });
	    frame.add(createb);

	    y += h + space;

	    JLabel miniLabel = new JLabel("Minimalna težina: ");
	    miniLabel.setBounds(x, y, w, h);
	    frame.add(miniLabel);
	    
	    JTextField mini = new JTextField(String.valueOf(mintargetDifficulty));
        mini.setBounds(x + w, y, 2 * h, h);
	    frame.add(mini);

	    y += h + space;

	    JLabel maksiLabel = new JLabel("Maksimalna težina: ");
	    maksiLabel.setBounds(x, y, w, h);
	    frame.add(maksiLabel);
	    
	    JTextField maksi = new JTextField(String.valueOf(maxtargetDifficulty));
	    maksi.setBounds(x + w, y, 2 * h, h);
	    frame.add(maksi);

	    y += h + space;

        JButton solverandomb = new JButton("Riješi nasumièno");  
        solverandomb.setMargin(new Insets(1,1,1,1));
        solverandomb.setFont(new Font("Arial", Font.PLAIN, fontsize));
        solverandomb.setBounds(x, y, w, h);
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

	    y += h + space;
	    JLabel bcol = new JLabel("Boja kutije: ");
	    bcol.setBounds(x, y, w, h);
	    frame.add(bcol);
	    		
	    y += h + space;
	    JButton modeb1 = new JButton("");  
        modeb1.setMargin(new Insets(1,1,1,1));
        modeb1.setBackground(Color.GRAY);
        modeb1.setBounds(x, y, w / 4, h);
        modeb1.setFont(new Font("Arial", Font.PLAIN, fontsize));
        modeb1.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		mode = 0;
				} catch (Exception e1) {
					
				}
	        }  
	    });

	    JButton modeb2 = new JButton("");  
        modeb2.setMargin(new Insets(1,1,1,1));
        modeb2.setBackground(Color.BLACK);
        modeb2.setBounds(x + w / 4, y, w / 4, h);
        modeb2.setFont(new Font("Arial", Font.PLAIN, fontsize));
        modeb2.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		mode = 1;
				} catch (Exception e1) {
					
				}
	        }  
	    });

	    JButton modeb3 = new JButton("");  
        modeb3.setMargin(new Insets(1,1,1,1));
        modeb3.setBackground(Color.DARK_GRAY);
        modeb3.setBounds(x + 2 * w / 4, y, w / 4, h);
        modeb3.setFont(new Font("Arial", Font.PLAIN, fontsize));
        modeb3.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		mode = 2;
				} catch (Exception e1) {
					
				}
	        }  
	    });
        
	    JButton modeb4 = new JButton("");  
        modeb4.setMargin(new Insets(1,1,1,1));
        modeb4.setBackground(Color.LIGHT_GRAY);
        modeb4.setBounds(x + 3 * w / 4, y, w / 4, h);
        modeb4.setFont(new Font("Arial", Font.PLAIN, fontsize));
        modeb4.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		mode = 3;
				} catch (Exception e1) {
					
				}
	        }  
	    });

	    y += h + space;
		FileManipulator f = new FileManipulator();
		f.setSudoku(this);
        
        JButton designcontb = new JButton("Nastavi dizajn");  
        designcontb.setMargin(new Insets(1,1,1,1));
        designcontb.setBounds(x, y, w, h);
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
	    y += h + space;
        JButton designb = new JButton("Dizajniraj zagonetku");  
        designb.setMargin(new Insets(1,1,1,1));
        designb.setBounds(x, y, w, h);
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



	    y += h + space;

        JButton solveb = new JButton("Riješi spremljeno");  
        solveb.setMargin(new Insets(1,1,1,1));
        solveb.setBounds(x, y, w, h);
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
		int buttonEnd = y + h + space;
	    frame.setSize(x + w + 2 * h + 2 * space, Math.max(digitEnd, buttonEnd) + 40);  

       
        frame.add(modeb1);
        frame.add(modeb2);
        frame.add(modeb3);
        frame.add(modeb4);
        frame.add(designb);
        frame.add(designcontb);
        frame.add(solverandomb);
        frame.add(solveb);
        Container contentPane = frame.getContentPane ();
        contentPane.setLayout (new BorderLayout ());
	    frame.setVisible(true);
	}

	public static void main(String args[]) {
		ChangeBoxBorder b = new ChangeBoxBorder(9, 9, 3, 3);
	}

}
