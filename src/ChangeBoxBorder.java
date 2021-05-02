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

public class ChangeBoxBorder extends SudokuGrid {
	int mode = -1;
	int relationshipStatus = 0;
	int largerCell;
	int smallerCell;
    JButton relationshipAddButton = new JButton("");  
    JButton relationshipRemoveButton = new JButton("");  
	public ChangeBoxBorder(int contructRows, int constructCols, int rowLimit, int colLimit, boolean makeVisible) {
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
	    frame.setVisible(makeVisible);
	}

	@Override
	public void draw() {
		frame = new JFrame("Promjeni kutiju za sudoku");  
	    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);

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
			        		}
			        		if (mode == 1)  {
					    		field[numCell].setBackground(Color.BLACK);
				        		border[numCell] = 1;
			        		} 
			        		if (mode == 2)  {
					    		field[numCell].setBackground(Color.DARK_GRAY);
				        		border[numCell] = 2;
			        		} 
			        		if (mode == 3)  {
					    		field[numCell].setBackground(Color.LIGHT_GRAY);
				        		border[numCell] = 3;
			        		} 
			        		if (mode == 4 || mode == 5)  {
			        			if (relationshipStatus == 1) {
			        				largerCell = numCell;
			        				relationshipStatus = 2;
			        				if (mode == 4) {
			        					relationshipAddButton.setText("Odaberi manju �eliju");
			        				} else {
			        					relationshipRemoveButton.setText("Odaberi manju �eliju");
			        				}
			        			} else {
				        			if (relationshipStatus == 2) {
				        				smallerCell = numCell;
				        				int rightCell = largerCell + 1;
				        				int leftCell = largerCell - 1;
				        				int bottomCell = largerCell + cols;
				        				int topCell = largerCell - cols;
				        				if (mode == 4) {
					        				if (smallerCell == rightCell || smallerCell == leftCell || smallerCell == bottomCell || smallerCell == topCell) {
					        					String relationship = String.valueOf(largerCell) + " " + String.valueOf(smallerCell);
					        					String relationshipReverse = String.valueOf(smallerCell) + " " + String.valueOf(largerCell);
					        					if (sizeRelationships.contains(relationship)) {
							    	    			InformationBox.infoBox("�elija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") je ve� manja od susjedne �elije (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ").", "Ve�e-manje");
					        					} else {
						        					if (sizeRelationships.contains(relationshipReverse)) {
								    	    			InformationBox.infoBox("�elija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") ne mo�e istodobno biti i ve�a i manja od susjedne �elije (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ").", "Ve�e-manje");
						        					} else {
						        						sizeRelationships.add(relationship);
						        						//if (getLargerTrees() > 0) {
								        					//sizeRelationships.remove(relationship);
									    	    			//InformationBox.infoBox("�elija (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ") je dio stabla u kojem ima previ�e djece.", "Ve�e-manje");
						        						//}
						        					}
					        					}
					        				} else {
						    	    			InformationBox.infoBox("�elija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") nije susjedna �eliji (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ").", "Ve�e-manje");
					        				}
					        				relationshipStatus = 1;
				        					relationshipAddButton.setText("Odaberi ve�u �eliju");
				        				} else {
					        				if (smallerCell == rightCell || smallerCell == leftCell || smallerCell == bottomCell || smallerCell == topCell) {
					        					String relationship = String.valueOf(largerCell) + " " + String.valueOf(smallerCell);
					        					if (!sizeRelationships.contains(relationship)) {
							    	    			InformationBox.infoBox("�elija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") nije jo� manja od susjedne �elije (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + "), pa se odnos ne mo�e ukloniti.", "Ve�e-manje");
					        					} else {
						        					sizeRelationships.remove(relationship);
					        					}
					        				} else {
						    	    			InformationBox.infoBox("�elija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") nije susjedna �eliji (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ").", "Ve�e-manje");
					        				}
					        				relationshipStatus = 1;
					        				relationshipRemoveButton.setText("Odaberi ve�u �eliju");
				        				}
				        			}
			        			}
			        		} 
				        	checkBoxes();
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
	    w = (int) (150 * widthScaling);
	    y = space;
	    x += space;
	    
	    JLabel rowLabel = new JLabel("Broj redaka mre�e: ");
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
	    
	    JLabel colLabel = new JLabel("Broj stupaca mre�e: ");
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
	   
	    JButton createButton = new JButton("Nove dimenzije");
	    createButton.setBounds(x, y, w, h);
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
        				InformationBox.infoBox("Kutije imaju previ�e znamenki.", "Stvaranje zagonetke");
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
					ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(pr, pc, yl, xl, true);

				} catch (Exception e1) {

				}
	        }  
	    });
	    frame.add(createButton);

	    y += h + space;

	    JLabel miniLabel = new JLabel("Minimalna te�ina: ");
	    miniLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    miniLabel.setBounds(x, y, w, h);
	    frame.add(miniLabel);
	    
	    JTextField mini = new JTextField(String.valueOf(mintargetDifficulty));
	    mini.setFont(new Font("Arial", Font.PLAIN, fontsize));
        mini.setBounds(x + w, y, 2 * h, h);
	    frame.add(mini);

	    y += h + space;

	    JLabel maksiLabel = new JLabel("Maksimalna te�ina: ");
	    maksiLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    maksiLabel.setBounds(x, y, w, h);
	    frame.add(maksiLabel);
	    
	    JTextField maksi = new JTextField(String.valueOf(maxtargetDifficulty));
	    maksi.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    maksi.setBounds(x + w, y, 2 * h, h);
	    frame.add(maksi);

	    y += h + space;

        JButton solveRandomButton = new JButton("Rije�i nasumi�no");  
        solveRandomButton.setMargin(new Insets(1,1,1,1));
        solveRandomButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        solveRandomButton.setBounds(x, y, w, h);
        solveRandomButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (checkBoxes()) {
	        			@SuppressWarnings("unused")
						SolveSudoku solveSudoku = new SolveSudoku(rows, cols, xLim, yLim, border, boxNumber, diagonalOn, sizeRelationships, Integer.parseInt(mini.getText()), Integer.parseInt(maksi.getText()));
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });

	    y += h + space;

        JButton solveButton = new JButton("Rije�i spremljeno");  
        solveButton.setMargin(new Insets(1,1,1,1));
        solveButton.setBounds(x, y, w, h);
        solveButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        solveButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
					ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(rows, cols, xLim, yLim, false);
					FileManipulator fileManipulator = new FileManipulator();
					fileManipulator.setSudoku(changeBoxBorder);
	        		if (fileManipulator.ReadFile() == 0) {
	        			@SuppressWarnings("unused")
	        			SolveSudoku SolveSudoku = new SolveSudoku(changeBoxBorder.rows, changeBoxBorder.cols, changeBoxBorder.xLim, changeBoxBorder.yLim, changeBoxBorder.border, changeBoxBorder.boxNumber, changeBoxBorder.diagonalOn, changeBoxBorder.sizeRelationships, changeBoxBorder.userInput);
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
        	    	relationshipStatus = 0;
        	    	relationshipRemoveButton.setText("Pokreni uklanjanje para ve�e-manje");
        	    	relationshipAddButton.setText("Pokreni odabir para ve�e-manje");
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
        	    	relationshipStatus = 0;
        	    	relationshipRemoveButton.setText("Pokreni uklanjanje para ve�e-manje");
        	    	relationshipAddButton.setText("Pokreni odabir para ve�e-manje");
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
        	    	relationshipStatus = 0;
        	    	relationshipRemoveButton.setText("Pokreni uklanjanje para ve�e-manje");
        	    	relationshipAddButton.setText("Pokreni odabir para ve�e-manje");
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
        	    	relationshipStatus = 0;
        	    	relationshipRemoveButton.setText("Pokreni uklanjanje para ve�e-manje");
        	    	relationshipAddButton.setText("Pokreni odabir para ve�e-manje");
				} catch (Exception e1) {
					
				}
	        }  
	    });

	    y += h + space;
	    ChangeBoxBorder currentChangeBoxBorder = this;
        JButton designContinueButton = new JButton("Nastavi dizajn");  
        designContinueButton.setMargin(new Insets(1,1,1,1));
        designContinueButton.setBounds(x, y, w, h);
        designContinueButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        designContinueButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
					FileManipulator fileManipulator = new FileManipulator();
					fileManipulator.setSudoku(currentChangeBoxBorder);
					fileManipulator.ReadFile();
	        		currentChangeBoxBorder.checkBoxes();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });

	    y += h + space;
        JButton designSaveButton = new JButton("Spremi dizajn");  
        designSaveButton.setMargin(new Insets(1,1,1,1));
        designSaveButton.setBounds(x, y, w, h);
        designSaveButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        designSaveButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
					FileManipulator fileManipulator = new FileManipulator();
					fileManipulator.setSudoku(currentChangeBoxBorder);
					fileManipulator.WriteToFile();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
	    y -= h + space;
	    x += w + space;
        JButton numberContinueButton = new JButton("Ispuni brojeve za spremljenu zagonetku");  
        numberContinueButton.setMargin(new Insets(1,1,1,1));
        numberContinueButton.setBounds(x, y, w + h * 5, h);
        numberContinueButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        numberContinueButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (checkBoxes()) {
						FileManipulator fileManipulator = new FileManipulator();
						fileManipulator.setSudoku(currentChangeBoxBorder);
		        		if (fileManipulator.ReadFile() == 0) {
		        			@SuppressWarnings("unused")
							CreateSudoku createSudoku = new CreateSudoku(currentChangeBoxBorder.rows, currentChangeBoxBorder.cols, currentChangeBoxBorder.xLim, currentChangeBoxBorder.yLim, currentChangeBoxBorder.border, currentChangeBoxBorder.boxNumber, currentChangeBoxBorder.diagonalOn, currentChangeBoxBorder.sizeRelationships, currentChangeBoxBorder.userInput);
		        		}
		        		currentChangeBoxBorder.checkBoxes();
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });

	    y += h + space;
        JButton numberAddButton = new JButton("Ispuni brojeve za nasumi�nu zagonetku");  
        numberAddButton.setMargin(new Insets(1,1,1,1));
        numberAddButton.setBounds(x, y, w + h * 5, h);
        numberAddButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        numberAddButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (checkBoxes()) {
	        			@SuppressWarnings("unused")
						CreateSudoku createSudoku = new CreateSudoku(rows, cols, xLim, yLim, border, boxNumber, diagonalOn, sizeRelationships);
	        		}
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
		int buttonEnd = y + h + space;

	    int widthOne = x + w + h * 5 + space;
	    x -= w + space;
        x += w + h + space;
        y = space;

        JButton diagonalButton = new JButton("");  
	    if (diagonalOn) {
	    	diagonalButton.setText("Jedinstvena dijagonala");
	    } else {
	    	diagonalButton.setText("Dijagonala nije jedinstvena");
	    }
        diagonalButton.setMargin(new Insets(1,1,1,1));
        diagonalButton.setBounds(x, y, w + h * 2, h);
        diagonalButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        diagonalButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        	    if (diagonalOn) {
	        	    	diagonalOn = false;
	        	    	diagonalButton.setText("Dijagonala nije jedinstvena");
	        	    } else {
	        	    	diagonalOn = true;
	        	    	diagonalButton.setText("Jedinstvena dijagonala");
	        	    }
	        	    checkBoxes();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
        
	    y += h + space;
	    
	    if (relationshipStatus == 0) {
	    	relationshipAddButton.setText("Pokreni odabir para ve�e-manje");
	    }
	    if (relationshipStatus == 1) {
	    	relationshipAddButton.setText("Odaberi ve�u �eliju");
	    }
	    if (relationshipStatus == 2) {
	    	relationshipAddButton.setText("Odaberi manju �eliju");
	    }
	    relationshipAddButton.setMargin(new Insets(1,1,1,1));
	    relationshipAddButton.setBounds(x, y, w + h * 4, h);
        relationshipAddButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
        relationshipAddButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
        	    	relationshipStatus = 1;
        	    	relationshipAddButton.setText("Odaberi ve�u �eliju");
        	    	relationshipRemoveButton.setText("Pokreni uklanjanje para ve�e-manje");
        	    	mode = 4;
	        	    checkBoxes();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });
        
	    y += h + space;
	    
        if (relationshipStatus == 0) {
	    	relationshipRemoveButton.setText("Pokreni uklanjanje para ve�e-manje");
	    }
	    if (relationshipStatus == 1) {
	    	relationshipRemoveButton.setText("Odaberi ve�u �eliju");
	    }
	    if (relationshipStatus == 2) {
	    	relationshipRemoveButton.setText("Odaberi manju �eliju");
	    }
	    relationshipRemoveButton.setMargin(new Insets(1,1,1,1));
	    relationshipRemoveButton.setBounds(x, y, w + h * 4, h);
	    relationshipRemoveButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    relationshipRemoveButton.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
        	    	relationshipStatus = 1;
        	    	relationshipRemoveButton.setText("Odaberi ve�u �eliju");
        	    	relationshipAddButton.setText("Pokreni odabir para ve�e-manje");
        	    	mode = 5;
	        	    checkBoxes();
				} catch (Exception e1) {
	
	
				}
	        }  
	    });


        x += w + h * 4 + space * 2;

	    frame.setSize(Math.max(widthOne, x), Math.max(digitEnd, buttonEnd) + (int) (40 * heightScaling));  
	    
        frame.add(modeButton1);
        frame.add(modeButton2);
        frame.add(modeButton3);
        frame.add(modeButton4);
        frame.add(numberAddButton);
        frame.add(numberContinueButton);
        frame.add(designContinueButton);
        frame.add(designSaveButton);
        frame.add(solveRandomButton);
        frame.add(solveButton);
        frame.add(diagonalButton);
        frame.add(relationshipAddButton);
        frame.add(relationshipRemoveButton);
        Container contentPane = frame.getContentPane ();
        contentPane.setLayout (new BorderLayout ());
	}

	public static void main(String args[]) {
		@SuppressWarnings("unused")
		ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(9, 9, 3, 3, true);
	}
}
