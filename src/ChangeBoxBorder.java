import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.MetalButtonUI;

public class ChangeBoxBorder extends SudokuGrid {
	int mode = -1;
	int relationshipStatus = 0;
	int largerCell;
	int smallerCell;
    JButton relationshipAddButton = new JButton("");  
    JButton relationshipRemoveButton = new JButton("");  
    JButton diagonalButton = new JButton("");  
	public ChangeBoxBorder(int contructRows, int constructCols, int rowLimit, int colLimit, boolean makeVisible) {
		super(constructCols, contructRows, rowLimit, colLimit);
	    for (int row = 0; row < rows; row++){ 
	    	for (int col = 0; col < cols; col++) {
		    	boxNumber[row * cols + col] = -1;
	    		temporary[row * cols + col] = 0;
		    	int numCol = row * cols + col;
	    		userInput[numCol] = temporary[numCol];
	    		solution[numCol] = temporary[numCol];
		    }
	    }
	    draw();
	    checkBoxes();
	    frame.setVisible(makeVisible);
	}
    Color[] colorsOrder = {Color.GRAY, Color.BLACK, Color.DARK_GRAY, Color.LIGHT_GRAY};

	@Override
	public ActionListener makeActionListener(int numCell) {
		return new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (mode < 0) {
	        			return;
	        		}
	        		showBoxMsg = false;
	        		if (mode < 4)  {
			    		field[numCell].setBackground(colorsOrder[mode]);
		        		border[numCell] = mode;
	        		} 
	        		if (mode == 4 || mode == 5)  {
	        			if (relationshipStatus == 1) {
	        				largerCell = numCell;
	        				relationshipStatus = 2;
	        				if (mode == 4) {
	        					relationshipAddButton.setText("Odaberi manju æeliju");
	        				} else {
	        					relationshipRemoveButton.setText("Odaberi manju æeliju");
	        				}
	        			} else {
		        			if (relationshipStatus == 2) {
		        				smallerCell = numCell;
		        				if (mode == 4) {
		        					if (neighbourCheck(largerCell, smallerCell)) {
										String relationship = String.valueOf(largerCell) + " " + String.valueOf(smallerCell);
			        					String relationshipReverse = String.valueOf(smallerCell) + " " + String.valueOf(largerCell);
			        					if (sizeRelationships.contains(relationship)) {
					    	    			InformationBox.infoBox("Æelija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") je veæ manja od susjedne æelije (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ").", "Veæe-manje");
			        					} else {
				        					if (sizeRelationships.contains(relationshipReverse)) {
						    	    			InformationBox.infoBox("Æelija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") ne može istodobno biti i veæa i manja od susjedne æelije (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ").", "Veæe-manje");
				        					} else {
				        						sizeRelationships.add(relationship);
				        					    // Inicijaliziramo moguænosti za sve vrijednosti u svim æelijama na 1
				        						possibilities = new int[rows * cols][rows];
				        					    for (int row = 0; row < rows; row++){
				        					    	for (int col = 0; col < cols; col++) {
				        					    		if (temporary[row * cols + col] != 0) {
				        							    	for (int val = 0; val < cols; val++) {
				        							    		possibilities[row * cols + col][val] = 0;
				        								    }
				        						    		possibilities[row * cols + col][temporary[row * cols + col] - 1] = 1;
				        					    		} else {
				        							    	for (int val = 0; val < cols; val++) {
				        							    		possibilities[row * cols + col][val] = 1;
				        								    }
				        					    		}
				        					    	}
				        					    }
				        					    for (int row = 0; row < rows; row++){
				        					    	for (int col = 0; col < cols; col++) {
				        					    		Set<Integer> visitedMax = new HashSet<Integer>();
				        					    		Set<Integer> visitedMin = new HashSet<Integer>();
				        					    		setMaxPossibility(row * cols + col, visitedMax);
				        					    		setMinPossibility(row * cols + col, visitedMin);
				        					    	}
				        					    }
				        					    Set<Integer> impossible = new HashSet<Integer>();
				        					    String impossibleString = "";
				        					    for (int row = 0; row < rows; row++){
				        					    	for (int col = 0; col < cols; col++) {
				        					    		int possibleVals = 0;
				        					    		for (int val = 0; val < cols; val++) {
				        					    			if (possibilities[row * cols + col][val] == 1) {
				        					    				possibleVals = 1;
				        					    				break;
				        					    			}
				        					    		}
				        					    		if (possibleVals == 0) {
				        					    			if (impossible.size() == 0) {
				        					    				impossibleString += "\n"; 
				        					    			} else {
				        					    				impossibleString += ", "; 
				        					    			}
				        					    			impossibleString += "(" + (row + 1) + ", " + (col + 1) + ")";
				        					    			impossible.add(row * cols + col);
				        					    			break;
				        					    		}
				        					    	}
				        					    }
				        					    if (impossible.size() > 0) {
							    	    			InformationBox.infoBox("Æelija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") ne može biti manja od æelije (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ") jer bi ove æelije ostale bez moguæih vrijednosti: " + impossibleString, "Veæe-manje");
							    	    			sizeRelationships.remove(relationship);
				        					    }
				        					}
			        					}
			        				} else {
				    	    			InformationBox.infoBox("Æelija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") nije susjedna æeliji (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ").", "Veæe-manje");
			        				}
			        				relationshipStatus = 1;
		        					relationshipAddButton.setText("Odaberi veæu æeliju");
		        				} else {
			        				if (neighbourCheck(largerCell, smallerCell)) {
			        					String relationship = String.valueOf(largerCell) + " " + String.valueOf(smallerCell);
			        					if (!sizeRelationships.contains(relationship)) {
					    	    			InformationBox.infoBox("Æelija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") nije još manja od susjedne æelije (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + "), pa se odnos ne može ukloniti.", "Veæe-manje");
			        					} else {
				        					sizeRelationships.remove(relationship);
			        					}
			        				} else {
				    	    			InformationBox.infoBox("Æelija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") nije susjedna æeliji (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ").", "Veæe-manje");
			        				}
			        				relationshipStatus = 1;
			        				relationshipRemoveButton.setText("Odaberi veæu æeliju");
		        				}
		        			}
	        			}
	        		} 
		        	checkBoxes();
	        		showBoxMsg = true;
				} catch (Exception e1) {

				}
	        }  
	    };
	}
	
	@Override
	public void makeButtons() {
	    for (int row = 0; row < rows; row++){ 
	    	x = space;
	    	for (int col = 0; col < cols; col++) {
	    		int numCell = row * cols + col;
	    	    field[numCell] = new JButton();  
			    field[numCell].setMargin(new Insets(1,1,1,1));
			    field[numCell].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
			    field[numCell].setBounds(x, y, w, h);
			    field[numCell].setUI(new MetalButtonUI() {
    			    protected Color getDisabledTextColor() {
    			        return Color.CYAN;
    			    }
    			});
			    field[numCell].addActionListener(makeActionListener(numCell));
			    frame.add(field[numCell]);
		    	x += w;
		    }
		    y += h;
	    }
	    y += space;
	}
	
	@Override
	public JButton makeAButton(String title, int xPosition, int yPosition, int widthButton, int heightButton, ActionListener actionListerToAdd) {
		JButton newButton = new JButton(title);  
		newButton.setMargin(new Insets(1,1,1,1));
		newButton.setBounds(xPosition, yPosition, widthButton, heightButton);
		newButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
		newButton.addActionListener(actionListerToAdd);
		frame.add(newButton);
		return newButton;
	}
	
	
	public JRadioButton makeRadioButton(String name, String averageForRadio, String minimumForRadio, String maximumForRadio, JTextField mini, JTextField maksi) {
	    int widthDifficulty = (int) (300 * widthScaling);
	    JRadioButton radio = new JRadioButton(name + " (avg. " + averageForRadio + ", " + minimumForRadio + "-" + maximumForRadio + ")");
	    radio.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    radio.setBounds(x + w + 2 * h + space, y, widthDifficulty, h);
	    radio.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (checkBoxes()) {
	        			mini.setText(minimumForRadio);
	        			maksi.setText(maximumForRadio);
	        		}
				} catch (Exception e1) {
	
				}
	        }  
	    });
	    frame.add(radio);
	    y += h / 2 + space;
	    return radio;
	}
	
	@Override
	public void draw() {
	    UIManager.put("TextField.inactiveBackground", new ColorUIResource(Color.WHITE));
		frame = new JFrame("Promjeni kutiju za sudoku");  
	    frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);

	    makeButtons();
		int digitEnd = y + space;
	    h = h / 2;
	    w = (int) (150 * widthScaling);
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
	    col.setEditable(false);
	    col.setBounds(x + w, y, h, h);
	    frame.add(col);

	    y += h;
	    
	    JLabel yLimLabel = new JLabel("Broj stupaca kutije: ");
	    yLimLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    yLimLabel.setBounds(x, y, w, h);
	    frame.add(yLimLabel);
	    
	    JTextField yLimVal = new JTextField(String.valueOf(xLim));
	    yLimVal.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    yLimVal.setEditable(false);
	    yLimVal.setBounds(x + w, y, h, h);
	    frame.add(yLimVal);

	    y += h + space;
	   
	    row.getDocument().addDocumentListener(new DocumentListener() {
	      	  public void changedUpdate(DocumentEvent e) {
	      	    SwingUtilities.invokeLater(matchRowsAndCols);
	      	  }
	      	  public void removeUpdate(DocumentEvent e) {
	      		SwingUtilities.invokeLater(matchRowsAndCols);
	      	  }
	      	  public void insertUpdate(DocumentEvent e) {
	      		SwingUtilities.invokeLater(matchRowsAndCols);
	      	  }
	      	  Runnable matchRowsAndCols = new Runnable() {public void run() {
	      		col.setText(row.getText());
				yLimVal.setText(String.valueOf(Integer.parseInt(row.getText()) / Integer.parseInt(xLimVal.getText())));
	      	  }};
	    });
	    
	    xLimVal.getDocument().addDocumentListener(new DocumentListener() {
	      	  public void changedUpdate(DocumentEvent e) {
	      	    SwingUtilities.invokeLater(matchRowsAndCols);
	      	  }
	      	  public void removeUpdate(DocumentEvent e) {
	      		SwingUtilities.invokeLater(matchRowsAndCols);
	      	  }
	      	  public void insertUpdate(DocumentEvent e) {
	      		SwingUtilities.invokeLater(matchRowsAndCols);
	      	  }
	      	  Runnable matchRowsAndCols = new Runnable() {public void run() {
				yLimVal.setText(String.valueOf(Integer.parseInt(row.getText()) / Integer.parseInt(xLimVal.getText())));
	      	  }};
	    });
	    

	 	makeAButton("Nove dimenzije", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
		      		if (Integer.parseInt(row.getText()) < 4) {
		  				InformationBox.infoBox("Najmanji broj redova u zagonetki je 4.", "Stvaranje zagonetke");
	    				row.setText("4");
	    				col.setText("4");
	    				xLimVal.setText("2");
	    				yLimVal.setText("2");
	    				return;
	    			}
		      		col.setText(row.getText());
		      		if (Integer.parseInt(row.getText()) % Integer.parseInt(xLimVal.getText()) != 0) {
		      			InformationBox.infoBox("Broj redaka mreže mora biti djeljiv brojem redaka kutije.", "Stvaranje zagonetke");
		      			int xLimitNew = 1;
	  					for (int xLimitPossible = 2; xLimitPossible <= Integer.parseInt(row.getText()); xLimitPossible++) {
	  						double differenceCurrent = Math.abs(Math.sqrt(Integer.parseInt(row.getText())) - xLimitNew);
	  						double differenceNew = Math.abs(Math.sqrt(Integer.parseInt(row.getText())) - xLimitPossible);
	  						if (Integer.parseInt(row.getText()) % xLimitPossible == 0 && differenceCurrent > differenceNew) {
	  							xLimitNew = xLimitPossible;
	  						}
	  					}
	  					xLimVal.setText(String.valueOf(xLimitNew));
		  				return;
		  			}
					yLimVal.setText(String.valueOf(Integer.parseInt(row.getText()) / Integer.parseInt(xLimVal.getText())));

	        		int pr = Integer.parseInt(row.getText());
	        		int pc = Integer.parseInt(col.getText());
	        		int yl = Integer.parseInt(yLimVal.getText());
	        		int xl = Integer.parseInt(xLimVal.getText());

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

	    y += h + space;

	    int yreset = y;
	    JLabel miniLabel = new JLabel("Minimalna težina: ");
	    miniLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    miniLabel.setBounds(x, y, w, h);
	    frame.add(miniLabel);
	    
	    JTextField mini = new JTextField(String.valueOf(mintargetDifficulty));
	    mini.setFont(new Font("Arial", Font.PLAIN, fontsize));
        mini.setBounds(x + w, y, 2 * h, h);
	    frame.add(mini);
	    JTextField maksi = new JTextField(String.valueOf(maxtargetDifficulty));


	    int widthDifficulty = (int) (300 * widthScaling);

	    ButtonGroup group = new ButtonGroup();
	    group.add(makeRadioButton("Poèetnièka", "4000", "3600", "4500", mini, maksi));
	    group.add(makeRadioButton("Lagano", "4900", "4300", "5500", mini, maksi));
	    group.add(makeRadioButton("Srednje", "6000", "5300", "6900", mini, maksi));
	    group.add(makeRadioButton("Zahtjevno", "7600", "6500", "9300", mini, maksi));
	    group.add(makeRadioButton("Izazovno", "10000", "8300", "14000", mini, maksi));
	    group.add(makeRadioButton("Pakleno", "18000", "11000", "25000", mini, maksi));
	    int widthTwo = x + w + 2 * h + space * 2 + widthDifficulty + space;
	    y = yreset;
	    y += h / 2 + space;


	    JLabel maksiLabel = new JLabel("Maksimalna težina: ");
	    maksiLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    maksiLabel.setBounds(x, y, w, h);
	    frame.add(maksiLabel);
	    
	    maksi.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    maksi.setBounds(x + w, y, 2 * h, h);
	    frame.add(maksi);

	    y += h + space;
	    
	    makeAButton("Riješi nasumièno", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (checkBoxes()) {
	    	      		if (Integer.parseInt(mini.getText()) < 200) {
	        				InformationBox.infoBox("Težina ne može biti manja od 200 (dva uklonjena polja).", "Neispravan raspon težine");
	        				mini.setText("200");
	        				return;
	        			}
	    	      		if (Integer.parseInt(maksi.getText()) < 200) {
	    	  				InformationBox.infoBox("Težina ne može biti manja od 200 (dva uklonjena polja).", "Neispravan raspon težine");
	    	  				maksi.setText("200");
	    	  				return;
	    	  			}
	    	  			if (Integer.parseInt(mini.getText()) > Integer.parseInt(maksi.getText())) {
	    	  				InformationBox.infoBox("Maksimalna težina ne može biti manja od minimalne težine.", "Neispravan raspon težine");
	    	  				maksi.setText(mini.getText());
	    	  				return;
	    	  			}
	        			@SuppressWarnings("unused")
						SolveSudoku solveSudoku = new SolveSudoku(rows, cols, xLim, yLim, border, boxNumber, diagonalOn, sizeRelationships, Integer.parseInt(mini.getText()), Integer.parseInt(maksi.getText()));
	        		}
		    
				} catch (Exception e1) {

				}
	        }  
	    });

	    y += h + space;

	    makeAButton("Riješi spremljeno", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
					ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(rows, cols, xLim, yLim, false);
					changeBoxBorder.readFile("");
	        		@SuppressWarnings("unused")
	        		SolveSudoku SolveSudoku = new SolveSudoku(changeBoxBorder.rows, changeBoxBorder.cols, changeBoxBorder.xLim, changeBoxBorder.yLim, changeBoxBorder.border, changeBoxBorder.boxNumber, changeBoxBorder.diagonalOn, changeBoxBorder.sizeRelationships, changeBoxBorder.userInput);
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
	    for (int modeButtonNumber = 0; modeButtonNumber < 4; modeButtonNumber++) {
	    	int modeNumber = modeButtonNumber;
	    	JButton modeButton = makeAButton("", x + modeButtonNumber * w / 4, y, w / 4, h, new ActionListener(){public void actionPerformed(ActionEvent e) {  
		        	try {
		        		mode = modeNumber;
	        	    	relationshipStatus = 0;
	        	    	relationshipRemoveButton.setText("Pokreni uklanjanje para veæe-manje");
	        	    	relationshipAddButton.setText("Pokreni odabir para veæe-manje");
					} catch (Exception e1) {
						
					}
		        }  
		    });
	        modeButton.setBackground(colorsOrder[modeButtonNumber]);
	        frame.add(modeButton);
	    }

	    y += h + space;
	    ChangeBoxBorder currentChangeBoxBorder = this;

        makeAButton("Nastavi dizajn", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
					readFile("");
	        		currentChangeBoxBorder.checkBoxes();
	        	} catch (Exception e1) {

				}
	        }  
	    });
	    y += h + space;

        makeAButton("Spremi dizajn", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
					writeToFile("");
	        	} catch (Exception e1) {

				}
	        }  
	    });
	    y -= h + space;
	    x += w + space;


        makeAButton("Ispuni brojeve za spremljenu zagonetku", x, y, w + h * 5, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (checkBoxes()) {
		        		readFile("");
		        		@SuppressWarnings("unused")
						CreateSudoku createSudoku = new CreateSudoku(currentChangeBoxBorder.rows, currentChangeBoxBorder.cols, currentChangeBoxBorder.xLim, currentChangeBoxBorder.yLim, currentChangeBoxBorder.border, currentChangeBoxBorder.boxNumber, currentChangeBoxBorder.diagonalOn, currentChangeBoxBorder.sizeRelationships, currentChangeBoxBorder.userInput, currentChangeBoxBorder.lastUsedPath);
		        		currentChangeBoxBorder.checkBoxes();
	        		}
	        	} catch (Exception e1) {

				}
	        }  
	    });
	    y += h + space;

        makeAButton("Ispuni brojeve za nasumiènu zagonetku", x, y, w + h * 5, h, new ActionListener(){  
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


        diagonalButton = makeAButton("", x, y, w + h * 4, h, new ActionListener(){  
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
	    if (diagonalOn) {
	    	diagonalButton.setText("Jedinstvena dijagonala");
	    } else {
	    	diagonalButton.setText("Dijagonala nije jedinstvena");
	    }
        
	    y += h + space;
	    
	    
	    relationshipAddButton = makeAButton("", x, y, w + h * 4, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
        	    	relationshipStatus = 1;
        	    	relationshipAddButton.setText("Odaberi veæu æeliju");
        	    	relationshipRemoveButton.setText("Pokreni uklanjanje para veæe-manje");
        	    	mode = 4;
	        	    checkBoxes();
				} catch (Exception e1) {

				}
	        }  
	    }); 
	    
	    if (relationshipStatus == 0) {
	    	relationshipAddButton.setText("Pokreni odabir para veæe-manje");
	    }
	    if (relationshipStatus == 1) {
	    	relationshipAddButton.setText("Odaberi veæu æeliju");
	    }
	    if (relationshipStatus == 2) {
	    	relationshipAddButton.setText("Odaberi manju æeliju");
	    }


	    y += h + space;
	    
	    relationshipRemoveButton = makeAButton("", x, y, w + h * 4, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
        	    	relationshipStatus = 1;
        	    	relationshipRemoveButton.setText("Odaberi veæu æeliju");
        	    	relationshipAddButton.setText("Pokreni odabir para veæe-manje");
        	    	mode = 5;
	        	    checkBoxes();
				} catch (Exception e1) {

				}
	        }  
	    }); 
	    
        if (relationshipStatus == 0) {
	    	relationshipRemoveButton.setText("Pokreni uklanjanje para veæe-manje");
	    }
	    if (relationshipStatus == 1) {
	    	relationshipRemoveButton.setText("Odaberi veæu æeliju");
	    }
	    if (relationshipStatus == 2) {
	    	relationshipRemoveButton.setText("Odaberi manju æeliju");
	    }

        x += w + h * 4 + space * 2;

	    frame.setSize(Math.max(Math.max(widthOne, widthTwo), x), Math.max(digitEnd, buttonEnd) + (int) (40 * heightScaling));  
        Container contentPane = frame.getContentPane ();
        contentPane.setLayout (new BorderLayout ());
	}

	public static void main(String args[]) {
		@SuppressWarnings("unused")
		ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(9, 9, 3, 3, true);
	}

}
