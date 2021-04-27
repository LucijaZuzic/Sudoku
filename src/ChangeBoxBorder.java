import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
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
	public ChangeBoxBorder(int contructRows, int constructCols, int rowLimit, int colLimit) {
		super(constructCols, contructRows, rowLimit, colLimit);
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	boxNumber[row * cols + col] = -1;
		    	border[row * cols + col] = -1;
	    		temporary[row * cols + col] = 0;
		    	int numCol = row * cols + col;
	    		userInput[numCol] = temporary[numCol];
	    		solution[numCol] = temporary[numCol];
		    }
	    }
	    draw();
	    frame.setVisible(true);
	}

	@Override
	public void draw() {
		frame = new JFrame("Promjeni kutiju za sudoku");  
	    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
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

	    for (int row = 0; row < rows; row++){ 
	    	x = space;
	    	for (int col = 0; col < cols; col++) {
	    		int numCell = row * cols + col;
        		border[numCell] = -1;
	    	    field[numCell] = new JButton("");  
			    field[numCell].setMargin(new Insets(1,1,1,1));
			    field[numCell].setFont(new Font("Arial", Font.PLAIN, fontsize));
			    field[numCell].setBounds(x, y, w, h);
			    int leftopBorderorder = 1;
			    int righBorder = 1;
			    int topBorder = 1;
			    int bottomBorder = 1;
			    if (col % xLim== 0) {
			    	if (col != cols - 1 && col != 0) {
				    	leftopBorderorder = 3;
			    	} else {
			    		leftopBorderorder = 4;
			    	}
			    }
			    if (col % xLim == (xLim - 1)) {
			    	if (col != cols - 1 && col != 0) {
				    	righBorder = 3;
			    	} else {
			    		righBorder = 4;
			    	}
			    }
			    if (row % yLim == 0) {
			    	if (row != rows - 1 && row != 0) {
				    	topBorder = 3;
			    	} else {
			    		topBorder = 4;
			    	}
			    }
			    if (row % yLim == (yLim - 1)) {
			    	if (row != rows - 1 && row != 0) {
				    	bottomBorder = 3;
			    	} else {
			    		bottomBorder = 4;
			    	}
			    }
			    field[numCell].setBorder(BorderFactory.createMatteBorder(topBorder, leftopBorderorder, bottomBorder, righBorder, Color.WHITE));
		    	int box = (row / yLim) * (cols / xLim) + (col / xLim);
		    	if (((box % (cols / xLim) % 2 == 0) && (box / (cols / xLim) % 2  == 0)) || 
		    		((box % (cols / xLim) % 2 != 0) && (box / (cols / xLim) % 2  == 1))) {
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
	    w = (int) (135 * widthScaling);
	    y = space;
	    x += space;
	    
	    JLabel rowLabel = new JLabel("Broj redaka mreže: ");
	    rowLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    rowLabel.setBounds(x, y, w, h);
	    frame.add(rowLabel);
	    
	    JTextField row = new JTextField(String.valueOf(rows));
	    row.setFont(new Font("Arial", Font.PLAIN, fontsize));
        row.setBounds(x + w, y, h, h);
	    frame.add(row);

	    y += h;
	    
	    JLabel xLimLabel = new JLabel("Broj redaka kutije: ");
	    xLimLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    xLimLabel.setBounds(x, y, w, h);
	    frame.add(xLimLabel);
	    
	    JTextField xLimVal = new JTextField(String.valueOf(yLim));
	    xLimVal.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    xLimVal.setBounds(x + w, y, h, h);
	    frame.add(xLimVal);

	    y += h;
	    
	    JLabel colLabel = new JLabel("Broj stupaca mreže: ");
	    colLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    colLabel.setBounds(x, y, w, h);
	    frame.add(colLabel);
	    
	    JTextField col = new JTextField(String.valueOf(cols));
	    col.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    col.setBounds(x + w, y, h, h);
	    frame.add(col);

	    y += h;
	    
	    JLabel yLimLabel = new JLabel("Broj stupaca kutije: ");
	    yLimLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    yLimLabel.setBounds(x, y, w, h);
	    frame.add(yLimLabel);
	    
	    JTextField yLimVal = new JTextField(String.valueOf(xLim));
	    yLimVal.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    yLimVal.setBounds(x + w, y, h, h);
	    frame.add(yLimVal);

	    y += h + space;
	   
	    JButton createButton = new JButton("Izmjeni dimenzije");
	    createButton.setBounds(x, y, w + h, h);
	    createButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    createButton.addActionListener(new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		int pr = Integer.parseInt(row.getText());
	        		int pc = Integer.parseInt(col.getText());
	        		int yl = Integer.parseInt(yLimVal.getText());
	        		int xl = Integer.parseInt(xLimVal.getText());
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
        			xLim = yl;
        			yLim = xl;
        		    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        		    frame.removeAll();
        		    frame.dispose();
        		    frame.setVisible(false);
	        		@SuppressWarnings("unused")
					ChangeBoxBorder b = new ChangeBoxBorder(pr, pc, yl, xl);

				} catch (Exception e1) {

				}
	        }  
	    });
	    frame.add(createButton);

	    y += h + space;

	    JLabel miniLabel = new JLabel("Minimalna težina: ");
	    miniLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    miniLabel.setBounds(x, y, w, h);
	    frame.add(miniLabel);
	    
	    JTextField mini = new JTextField(String.valueOf(mintargetDifficulty));
	    mini.setFont(new Font("Arial", Font.PLAIN, fontsize));
        mini.setBounds(x + w, y, 2 * h, h);
	    frame.add(mini);

	    y += h + space;

	    JLabel maksiLabel = new JLabel("Maksimalna težina: ");
	    maksiLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    maksiLabel.setBounds(x, y, w, h);
	    frame.add(maksiLabel);
	    
	    JTextField maksi = new JTextField(String.valueOf(maxtargetDifficulty));
	    maksi.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    maksi.setBounds(x + w, y, 2 * h, h);
	    frame.add(maksi);

	    y += h + space;

        JButton solveRandomButton = new JButton("Riješi nasumièno");  
        solveRandomButton.setMargin(new Insets(1,1,1,1));
        solveRandomButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        solveRandomButton.setBounds(x, y, w, h);
        solveRandomButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (checkBoxes()) {
	        			@SuppressWarnings("unused")
						SolveSudoku s = new SolveSudoku(rows, cols, xLim, yLim, border, boxNumber, Integer.parseInt(mini.getText()), Integer.parseInt(maksi.getText()));
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });

	    y += h + space;
	    JLabel boxColorLabel = new JLabel("Boja kutije: ");
	    boxColorLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    boxColorLabel.setBounds(x, y, w, h);
	    frame.add(boxColorLabel);
	    		
	    y += h + space;
	    JButton modeButton1 = new JButton("");  
        modeButton1.setMargin(new Insets(1,1,1,1));
        modeButton1.setBackground(Color.GRAY);
        modeButton1.setBounds(x, y, w / 4, h);
        modeButton1.setFont(new Font("Arial", Font.PLAIN, fontsize));
        modeButton1.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		mode = 0;
				} catch (Exception e1) {
					
				}
	        }  
	    });

	    JButton modeButton2 = new JButton("");  
        modeButton2.setMargin(new Insets(1,1,1,1));
        modeButton2.setBackground(Color.BLACK);
        modeButton2.setBounds(x + w / 4, y, w / 4, h);
        modeButton2.setFont(new Font("Arial", Font.PLAIN, fontsize));
        modeButton2.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		mode = 1;
				} catch (Exception e1) {
					
				}
	        }  
	    });

	    JButton modeButton3 = new JButton("");  
        modeButton3.setMargin(new Insets(1,1,1,1));
        modeButton3.setBackground(Color.DARK_GRAY);
        modeButton3.setBounds(x + 2 * w / 4, y, w / 4, h);
        modeButton3.setFont(new Font("Arial", Font.PLAIN, fontsize));
        modeButton3.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		mode = 2;
				} catch (Exception e1) {
					
				}
	        }  
	    });
        
	    JButton modeButton4 = new JButton("");  
        modeButton4.setMargin(new Insets(1,1,1,1));
        modeButton4.setBackground(Color.LIGHT_GRAY);
        modeButton4.setBounds(x + 3 * w / 4, y, w / 4, h);
        modeButton4.setFont(new Font("Arial", Font.PLAIN, fontsize));
        modeButton4.addActionListener(new ActionListener(){  
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
        
        JButton designContinueButton = new JButton("Nastavi dizajn");  
        designContinueButton.setMargin(new Insets(1,1,1,1));
        designContinueButton.setBounds(x, y, w, h);
        designContinueButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        designContinueButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (f.ReadFile() == 0) {
	        			@SuppressWarnings("unused")
						CreateSudoku s = new CreateSudoku(rows, cols, xLim, yLim, border, boxNumber, userInput);
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
	    y += h + space;
        JButton designButton = new JButton("Dizajniraj zagonetku");  
        designButton.setMargin(new Insets(1,1,1,1));
        designButton.setBounds(x, y, w, h);
        designButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        designButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (checkBoxes()) {
	        			@SuppressWarnings("unused")
						CreateSudoku s = new CreateSudoku(rows, cols, xLim, yLim, border, boxNumber);
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });



	    y += h + space;

        JButton solveButton = new JButton("Riješi spremljeno");  
        solveButton.setMargin(new Insets(1,1,1,1));
        solveButton.setBounds(x, y, w, h);
        solveButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        solveButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (f.ReadFile() == 0) {
	        			@SuppressWarnings("unused")
						SolveSudoku s = new SolveSudoku(rows, cols, xLim, yLim, border, boxNumber, userInput);
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		int buttonEnd = y + h + space;
	    frame.setSize(x + w + 2 * h + 2 * space, Math.max(digitEnd, buttonEnd) + (int) (40 * widthScaling));  

       
        frame.add(modeButton1);
        frame.add(modeButton2);
        frame.add(modeButton3);
        frame.add(modeButton4);
        frame.add(designButton);
        frame.add(designContinueButton);
        frame.add(solveRandomButton);
        frame.add(solveButton);
        Container contentPane = frame.getContentPane ();
        contentPane.setLayout (new BorderLayout ());
	    frame.setVisible(true);
	}

	public static void main(String args[]) {
		@SuppressWarnings("unused")
		ChangeBoxBorder b = new ChangeBoxBorder(9, 9, 3, 3);
	}

	@Override
	boolean checkIfCorrect() {
		// TODO Auto-generated method stub
		return false;
	}

}
