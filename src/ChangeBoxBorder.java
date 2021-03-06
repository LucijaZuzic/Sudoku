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
	JButton sumButton = new JButton("");
    JButton relationshipAddButton = new JButton("");  
    JButton relationshipRemoveButton = new JButton("");  
    JButton diagonalButton = new JButton("");  
    JButton wrapAroundButton = new JButton("");
    Set<Integer> boxToAdd = new HashSet<Integer>();
    int numberOfNextBox;
    JTextField sumValue = new JTextField();
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

    public void clearBox(int numOfBox) {
		sumBoxSums[numOfBox] = -1;
    	for (int numCell = 0; numCell < rows * cols; numCell++) {
			if (sumBoxNumber[numCell] == numOfBox) {
				sumBoxNumber[numCell] = -1;
			}
		}
    }
    
    public void addBox() {
    	if (boxToAdd.size() == 0) {
    		checkBoxes();
	    	sumButton.setText("Dodaj kutiju sume");
	    	boxToAdd.clear();
	    	sumValue.setEditable(true);
	    	return;
		}
		int minSum = (boxToAdd.size() + 1) * boxToAdd.size() / 2;
		int maxSum = (cols - boxToAdd.size() + 1 + cols) * boxToAdd.size() / 2;
		int actualMaxSum = (1 + cols) * cols / 2;
		if (boxToAdd.size() > cols) {
			InformationBox.infoBox("Kutija sa sumom ne mo?e imati vi?e od " + cols + " ?lanova.", "Kutija sa sumom");
			clearBox(numberOfNextBox);
		}
		if (1 > Integer.parseInt(sumValue.getText())) {
			InformationBox.infoBox("Kutija ne mo?e imati sumu manju od 1, a upisana je suma " + sumValue.getText() + ".", "Kutija sa sumom");
			clearBox(numberOfNextBox);
		}
		if (actualMaxSum < Integer.parseInt(sumValue.getText())) {
			InformationBox.infoBox("Kutija ne mo?e imati sumu ve?u od " + actualMaxSum + ", a upisana je suma " + sumValue.getText() + ".", "Kutija sa sumom");
			clearBox(numberOfNextBox);
		}
		if (minSum > Integer.parseInt(sumValue.getText())) {
			InformationBox.infoBox("Kutija sa " + boxToAdd.size() + " ?lanova ne mo?e imati sumu manju od " + minSum + ", a upisana je suma " + sumValue.getText() + ".", "Kutija sa sumom");
			clearBox(numberOfNextBox);
		}
		if (maxSum < Integer.parseInt(sumValue.getText())) {
			InformationBox.infoBox("Kutija sa " + boxToAdd.size() + " ?lanova ne mo?e imati sumu ve?u od " + maxSum + ", a upisana je suma " + sumValue.getText() + ".", "Kutija sa sumom");
			clearBox(numberOfNextBox);
		}
		checkBoxes();
    	sumButton.setText("Dodaj kutiju sume");
    	boxToAdd.clear();
    	sumValue.setEditable(true);
    }
    
    public void mode8(int numCell) {
    	if (relationshipStatus == 1) {
			largerCell = numCell;
			relationshipStatus = 2;
			relationshipRemoveButton.setText("Manja ?elija");
			return;
		} 

		if (relationshipStatus == 2) {
			smallerCell = numCell;
			relationshipStatus = 1;
			relationshipRemoveButton.setText("Ve?a ?elija");
			if (neighbourCheck(largerCell, smallerCell)) {
				String relationship = String.valueOf(largerCell) + " " + String.valueOf(smallerCell);
				if (!sizeRelationships.contains(relationship)) {
	    			InformationBox.infoBox("?elija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") nije jo? manja od susjedne ?elije (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + "), pa se odnos ne mo?e ukloniti.", "Ve?e-manje");
	    			return;
				}
				sizeRelationships.remove(relationship);
				return;
			} 
    		InformationBox.infoBox("?elija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") nije susjedna ?eliji (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ").", "Ve?e-manje");
		}
    }
    
    
    public void mode7(int numCell) {
    	if (relationshipStatus == 1) {
			largerCell = numCell;
			relationshipStatus = 2;
			relationshipAddButton.setText("Manja ?elija");
			return;
		} 
    	if (relationshipStatus == 2) {
			smallerCell = numCell;
			relationshipStatus = 1;
			relationshipAddButton.setText("Ve?a ?elija");
			if (neighbourCheck(largerCell, smallerCell)) {
				String relationship = String.valueOf(largerCell) + " " + String.valueOf(smallerCell);
				String relationshipReverse = String.valueOf(smallerCell) + " " + String.valueOf(largerCell);
				if (sizeRelationships.contains(relationship)) {
	    			InformationBox.infoBox("?elija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") je ve? manja od susjedne ?elije (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ").", "Ve?e-manje");
	    			return;
				} 
				if (sizeRelationships.contains(relationshipReverse)) {
	    			InformationBox.infoBox("?elija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") ne mo?e istodobno biti i ve?a i manja od susjedne ?elije (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ").", "Ve?e-manje");
	    			return;
				} 
				sizeRelationships.add(relationship);
			    // Inicijaliziramo mogu?nosti za sve vrijednosti u svim ?elijama na 1
				initPencilmarks();
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
	    			InformationBox.infoBox("?elija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") ne mo?e biti manja od ?elije (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ") jer bi ove ?elije ostale bez mogu?ih vrijednosti: " + impossibleString, "Ve?e-manje");
	    			sizeRelationships.remove(relationship);
			    }
			    return;
			}			
			InformationBox.infoBox("?elija (" + String.valueOf(smallerCell / cols + 1) + ", " + String.valueOf(smallerCell % cols + 1) + ") nije susjedna ?eliji (" + String.valueOf(largerCell / cols + 1) + ", " + String.valueOf(largerCell % cols + 1) + ").", "Ve?e-manje");
		}
	}
    
    
    public void mode9(int numCell) {
    	if (!boxToAdd.contains(numCell)) {
			if (sumBoxNumber[numCell] != -1) {	        				
				InformationBox.infoBox("?elija (" + String.valueOf(numCell / cols + 1) + ", " + String.valueOf(numCell % cols + 1) + ") je ve? u kutiji sa sumom " + sumBoxSums[sumBoxNumber[numCell]] + ".", "Kutija sa sumom");
				return;
			} 
			boolean found = false;
			if (boxToAdd.size() == 0) {
				found = true;
			} else {
				for (int neighbourCell = 0; neighbourCell < rows * cols; neighbourCell++) {
					if (neighbourCheck(numCell, neighbourCell) && boxToAdd.contains(neighbourCell)) {
						found = true;
						break;
					}
				}
			}
			if (found) {
				boxToAdd.add(numCell);
				sumBoxNumber[numCell] = numberOfNextBox;
				return;
			} 
			InformationBox.infoBox("?elija (" + String.valueOf(numCell / cols + 1) + ", " + String.valueOf(numCell % cols + 1) + ") nije susjedna niti jednoj ?eliji u kutiji sa sumom " + sumValue.getText() +".", "Kutija sa sumom");		
			return;
		}
		InformationBox.infoBox("?elija (" + String.valueOf(numCell / cols + 1) + ", " + String.valueOf(numCell % cols + 1) + ") je ve? u ovoj kutiji sa sumom " + sumValue.getText() + ".", "Kutija sa sumom");
    }
    
	@Override
	public ActionListener makeActionListener(int numCell) {
		return new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (mode < 0) {
	        			return;
	        		}
	        		showBoxMsg = false;
	        		if (mode < 7)  {
			    		field[numCell].setBackground(colorsOrder[mode]);
		        		border[numCell] = mode;
	        			checkBoxes();
		        		showBoxMsg = true;
		        		return;
	        		} 
	        		if (mode == 7)  {
	        			mode7(numCell);
	        			checkBoxes();
		        		showBoxMsg = true;
	        			return;
	        		} 
	        		if (mode == 8)  {
	        			mode8(numCell);
	        			checkBoxes();
		        		showBoxMsg = true;
	        			return;
	        		} 
	        		if (mode == 9) {
	        			mode9(numCell);
	        			checkBoxes();
		        		showBoxMsg = true;
	        			return;
	        		}
	        		if (mode == 10) {
	        			clearBox(sumBoxNumber[numCell]);
	        			checkBoxes();
		        		showBoxMsg = true;
	        			return;
	        		}
				} catch (Exception e1) {

				}
	        }  
	    };
	}
	
	@Override
	public int makeButtons() {
	    for (int row = 0; row < rows; row++){ 
	    	x = space;
	    	for (int col = 0; col < cols; col++) {
	    		int numCell = row * cols + col;
	    	    field[numCell] = new JButton();  
			    field[numCell].setMargin(new Insets(1,1,1,1));
			    field[numCell].setFont(new Font("Arial", Font.PLAIN, numberFontsize));
			    field[numCell].setBounds(x, y, wNumber, hNumber);
			    field[numCell].setUI(new MetalButtonUI() {
    			    protected Color getDisabledTextColor() {
    			        return Color.CYAN;
    			    }
    			});
			    field[numCell].addActionListener(makeActionListener(numCell));
			    frame.add(field[numCell]);
		    	x += wNumber;
		    }
		    y += hNumber;
	    }
	    y += space;
	    return space + wNumber * cols;
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
	    radio.setBounds(x, y, widthDifficulty, h);
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
	
	public void initSudokuType() {
		if (diagonalOn) {
	    	diagonalButton.setText("X-sudoku");
	    } else {
	    	diagonalButton.setText("Bez dijagonale");
	    }
	    if (wrapAround) {
	    	wrapAroundButton.setText("Toroidalni sudoku");
	    } else {
	    	wrapAroundButton.setText("Klasi?ne kutije");
	    }
	}
	
	public void changeDifficulty(JTextField mini, JTextField maksi) {
		if (checkBoxes()) {
      		if (Integer.parseInt(mini.getText()) < 200) {
				InformationBox.infoBox("Te?ina ne mo?e biti manja od 200 (dva uklonjena polja).", "Neispravan raspon te?ine");
				mini.setText("200");
				return;
			}
      		if (Integer.parseInt(maksi.getText()) < 200) {
  				InformationBox.infoBox("Te?ina ne mo?e biti manja od 200 (dva uklonjena polja).", "Neispravan raspon te?ine");
  				maksi.setText("200");
  				return;
  			}
  			if (Integer.parseInt(mini.getText()) > Integer.parseInt(maksi.getText())) {
  				InformationBox.infoBox("Maksimalna te?ina ne mo?e biti manja od minimalne te?ine.", "Neispravan raspon te?ine");
  				maksi.setText(mini.getText());
  				return;
  			}
			@SuppressWarnings("unused")
			SolveSudoku solveSudoku = new SolveSudoku(rows, cols, xLim, yLim, border, boxNumber, diagonalOn, wrapAround, sizeRelationships, Integer.parseInt(mini.getText()), Integer.parseInt(maksi.getText()), true,  sumBoxSums, sumBoxNumber);
		}
	}
	
	public void changeSize(JTextField row, JTextField col, JTextField xLimVal, JTextField yLimVal) {
		if (Integer.parseInt(row.getText()) < 4) {
				InformationBox.infoBox("Najmanji broj redova u zagonetki je 4.", "Stvaranje zagonetke");
			row.setText("4");
			col.setText("4");
			xLimVal.setText("2");
			yLimVal.setText("2");
			return;
		}
  		if (Integer.parseInt(row.getText()) > 25) {
				InformationBox.infoBox("Najve?i broj redova u zagonetki je 25.", "Stvaranje zagonetke");
			row.setText("25");
			col.setText("25");
			xLimVal.setText("5");
			yLimVal.setText("5");
			return;
		}
  		col.setText(row.getText());
  		if (Integer.parseInt(row.getText()) % Integer.parseInt(xLimVal.getText()) != 0) {
  			InformationBox.infoBox("Broj redaka mre?e mora biti djeljiv brojem redaka kutije.", "Stvaranje zagonetke");
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
	    
	    JLabel rowLabel = new JLabel("Broj redaka mre?e: ");
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
	    
	    JLabel colLabel = new JLabel("Broj stupaca mre?e: ");
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
		      		changeSize(row, col, xLimVal, yLimVal);
				} catch (Exception e1) {

				}
	        }  
	    });

	    y += h + space;


	    JLabel miniLabel = new JLabel("Minimalna te?ina: ");
	    miniLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    miniLabel.setBounds(x, y, w, h);
	    frame.add(miniLabel);
	    
	    JTextField mini = new JTextField(String.valueOf(mintargetDifficulty));
	    mini.setFont(new Font("Arial", Font.PLAIN, fontsize));
        mini.setBounds(x + w, y, 2 * h, h);
	    frame.add(mini);
	    JTextField maksi = new JTextField(String.valueOf(maxtargetDifficulty));

	    y += h + space;
	    
	    JLabel maksiLabel = new JLabel("Maksimalna te?ina: ");
	    maksiLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    maksiLabel.setBounds(x, y, w, h);
	    frame.add(maksiLabel);
	    
	    maksi.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    maksi.setBounds(x + w, y, 2 * h, h);
	    frame.add(maksi);

	    y += h + space;

	    int widthDifficulty = (int) (300 * widthScaling);

	    ButtonGroup group = new ButtonGroup();
	    group.add(makeRadioButton("Po?etni?ka", "4000", "3600", "4500", mini, maksi));
	    group.add(makeRadioButton("Lagano", "4900", "4300", "5500", mini, maksi));
	    group.add(makeRadioButton("Srednje", "6000", "5300", "6900", mini, maksi));
	    group.add(makeRadioButton("Zahtjevno", "7600", "6500", "9300", mini, maksi));
	    group.add(makeRadioButton("Izazovno", "10000", "8300", "14000", mini, maksi));
	    group.add(makeRadioButton("Pakleno", "18000", "11000", "25000", mini, maksi));
	    y += space;
	    int widthTwo = x + widthDifficulty +  2 * space;
	    
	    makeAButton("Rije?i nasumi?no", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		changeDifficulty(mini, maksi);
				} catch (Exception e1) {

				}
	        }  
	    });
	    
	    x += w + space;

	    makeAButton("Rije?i spremljeno", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (readFile("") == 1) {
	        			return;
	        		}
	        		@SuppressWarnings("unused")
	        		SolveSudoku SolveSudoku = new SolveSudoku(rows, cols, xLim, yLim, border, boxNumber, diagonalOn, wrapAround, sizeRelationships, userInput, true,  sumBoxSums, sumBoxNumber, lastUsedPath);
	        		checkBoxes();
	        		initSudokuType();
	        	} catch (Exception e1) {

				}
	        }  
	    });

	    x -= w + space;
	    y += h + space;
	    makeAButton("Ispuni dizajn", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
		        	if (checkBoxes()) {
		        		@SuppressWarnings("unused")
						CreateSudoku createSudoku = new CreateSudoku(rows, cols, xLim, yLim, border, boxNumber, diagonalOn, wrapAround, sizeRelationships,  sumBoxSums, sumBoxNumber);
	        		}
	        	} catch (Exception e1) {

				}
	        }  
	    });
	    x += w + space;
	    makeAButton("Ispuni spremljeno", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
        			if (readFile("") == 1) {
        				return;
        			}
        			@SuppressWarnings("unused")
        			CreateSudoku createSudoku = new CreateSudoku(rows, cols, xLim, yLim, border, boxNumber, diagonalOn, wrapAround, sizeRelationships, userInput, lastUsedPath,  sumBoxSums, sumBoxNumber);
        			checkBoxes();
	        		initSudokuType();
	        	} catch (Exception e1) {

				}
	        }  
	    });
	    x -= w + space;
	    y += h + space;
	    
	    JLabel boxColorLabel = new JLabel("Boja kutije: ");
	    boxColorLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    boxColorLabel.setBounds(x, y, w, h);
	    frame.add(boxColorLabel);
	    		
	    y += h + space;
	    for (int modeButtonNumber = 0; modeButtonNumber < 7; modeButtonNumber++) {
	    	int modeNumber = modeButtonNumber;
	    	JButton modeButton = makeAButton("", x + modeButtonNumber * w / 7, y, w / 7, h, new ActionListener(){public void actionPerformed(ActionEvent e) {  
		        	try {
		        		addBox();
		        		mode = modeNumber;
	        	    	relationshipStatus = 0;
	        	    	relationshipRemoveButton.setText("Ukloni odnos >");
	        	    	relationshipAddButton.setText("Dodaj odnos >");
					} catch (Exception e1) {
						
					}
		        }  
		    });
	        modeButton.setBackground(colorsOrder[modeButtonNumber]);
	        frame.add(modeButton);
	    }

	    y += h + space;

        makeAButton("Nastavi dizajn", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (readFile("") == 1) {
	        			return;
	        		}
	        		checkBoxes();
	        		initSudokuType();
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
	    y += h + space;
	    
	    x += w + space;
		int buttonEnd = y;
	    y -= 3 * h + 3 * space;
	    JLabel sumLabel = new JLabel("Suma kutije: ");
        sumLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    sumLabel.setBounds(x, y, w - h, h);
	    frame.add(sumLabel);
	    
	    sumValue = new JTextField(String.valueOf(0));
	    sumValue.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    sumValue.setBounds(x + w - h, y, h, h);
	    frame.add(sumValue);

        y += h + space;

        sumButton = makeAButton("Dodaj kutiju sume", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (mode != 9) {
		        		mode = 9;
		    	    	relationshipAddButton.setText("Dodaj odnos >");
		    	    	relationshipRemoveButton.setText("Ukloni odnos >");
		    	    	numberOfNextBox = getMinBoxNumber();
		    	    	sumValue.setEditable(false);
        				sumBoxSums[numberOfNextBox] = Integer.parseInt(sumValue.getText());
		    	    	sumButton.setText("Zatvori kutiju sume");
		    	    	boxToAdd.clear();
	        		} else {
		        		addBox();
		        		mode = 0;
		    	    }
	        	} catch (Exception e1) {

				}
	        }  
	    });
        
        y += h + space;
       
        makeAButton("Ukloni kutiju sume", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		addBox();
		        	mode = 10;
	        	} catch (Exception e1) {

				}
	        }  
	    });
        
        y += h + space;

		y -= h * 5 + space * 5;
      
	    int widthOne = x + w + 2 * space;
	    x -= w + space;
        x += w + h + space;
        y = space;


        diagonalButton = makeAButton("Bez dijagonale", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		diagonalOn = !diagonalOn;
	        		initSudokuType();
	        	    checkBoxes();
	        	} catch (Exception e1) {

				}
	        }  
	    }); 
	    y += h + space;
	    
	    wrapAroundButton = makeAButton("Klasi?ne kutije", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		wrapAround = !wrapAround;
	        		initSudokuType();
	        	    checkBoxes();
	        	} catch (Exception e1) {

				}
	        }  
	    }); 
	    y += h + space;
	    
	    
	    relationshipAddButton = makeAButton("Dodaj odnos >", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
        	    	relationshipStatus = 1;
        	    	relationshipAddButton.setText("Ve?a ?elija");
        	    	relationshipRemoveButton.setText("Ukloni odnos >");
	        		addBox();
        	    	mode = 7;
	        	    checkBoxes();
				} catch (Exception e1) {

				}
	        }  
	    });
	    y += h + space;
	    
	    relationshipRemoveButton = makeAButton("Ukloni odnos >", x, y, w, h, new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
        	    	relationshipStatus = 1;
        	    	relationshipRemoveButton.setText("Ve?a ?elija");
        	    	relationshipAddButton.setText("Dodaj odnos >");
	        		addBox();
        	    	mode = 8;
	        	    checkBoxes();
				} catch (Exception e1) {

				}
	        }  
	    }); 
        x += w + 2 * space;

	    frame.setSize(Math.max(Math.max(widthOne, widthTwo), x), Math.max(digitEnd, buttonEnd) + (int) (40 * heightScaling));  
        Container contentPane = frame.getContentPane ();
        contentPane.setLayout (new BorderLayout ());
	}

	public static void main(String args[]) {
		@SuppressWarnings("unused")
		ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(9, 9, 3, 3, true);
	}

}
