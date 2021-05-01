import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class ChooseSize {

	public static void main(String args[]) {
		JFrame frame = new JFrame("Odaberi velièinu za sudoku");  
	    frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
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
		int w = (int) (150 * widthScaling);
		int h = (int) (30 * heightScaling);
		int fontsize = (int) (16 * heightScaling);
		int r = 9, c = 9, xl = 3, yl = 3;
	    JLabel rowLabel = new JLabel("Broj redaka mreže: ");
	    rowLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    rowLabel.setBounds(x, y, w, h);
	    frame.add(rowLabel);
	    
	    JTextField row = new JTextField(String.valueOf(r));
	    row.setFont(new Font("Arial", Font.PLAIN, fontsize));
        row.setBounds(x + w, y, h, h);
	    frame.add(row);
	    y += h;
	    JLabel xLimLabel = new JLabel("Broj redaka kutije: ");
	    xLimLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    xLimLabel.setBounds(x, y, w, h);
	    frame.add(xLimLabel);

	    JTextField xLimVal = new JTextField(String.valueOf(xl));
	    xLimVal.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    xLimVal.setBounds(x + w, y, h, h);
	    frame.add(xLimVal);

	    y += h;
	    JLabel colLabel = new JLabel("Broj stupaca mreže: ");
	    colLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    colLabel.setBounds(x, y, w, h);
	    frame.add(colLabel);
	    
	    JTextField col = new JTextField(String.valueOf(c));
	    col.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    col.setBounds(x + w, y, h, h);
	    frame.add(col);

	    y += h;
	    JLabel yLimLabel = new JLabel("Broj stupaca kutije: ");
	    yLimLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    yLimLabel.setBounds(x, y, w, h);
	    frame.add(yLimLabel);
	    
	    JTextField yLimVal = new JTextField(String.valueOf(yl));
	    yLimVal.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    yLimVal.setBounds(x + w, y, h, h);
	    frame.add(yLimVal);

	    y += h + space;
	    JButton createButton = new JButton("Stvori sudoku");
	    createButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    createButton.setBounds(x, y, w + h, h);
	    frame.add(createButton);
	    y += h + space;

	    createButton.addActionListener(new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (Integer.parseInt(row.getText()) == Integer.parseInt(col.getText()) && Integer.parseInt(yLimVal.getText()) * Integer.parseInt(xLimVal.getText()) == Integer.parseInt(row.getText())) {
		        		@SuppressWarnings("unused")
						ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(Integer.parseInt(row.getText()), Integer.parseInt(col.getText()), Integer.parseInt(yLimVal.getText()), Integer.parseInt(xLimVal.getText()), true);
	        		} else {
	        			if (Integer.parseInt(row.getText()) != Integer.parseInt(col.getText())) {
	        				InformationBox.infoBox("Zagonetka nije kvadratna.", "Stvaranje zagonetke");
	        				return;
	        			}
	        			if (Integer.parseInt(yLimVal.getText()) * Integer.parseInt(xLimVal.getText()) > Integer.parseInt(row.getText())) {
	        				InformationBox.infoBox("Kutije imaju previše znamenki.", "Stvaranje zagonetke");
	        				return;
	        			}
	        			if (Integer.parseInt(yLimVal.getText()) * Integer.parseInt(xLimVal.getText()) < Integer.parseInt(row.getText())) {
	        				InformationBox.infoBox("Kutije imaju premalo znamenki.", "Stvaranje zagonetke");
	        				return;
	        			}
	        		}
				} catch (Exception e1) {

				}
	        }  
	    });
	    x += w + h + space * 2;
	    frame.setSize(x, y + (int) (40 * heightScaling));  
	    frame.setLayout(null);  
		frame.setVisible(true);
	}
}
