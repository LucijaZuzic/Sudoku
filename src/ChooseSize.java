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

	    int x = (int) (15 * widthScaling);
		int y = (int) (15 * heightScaling);
		int w = (int) (130 * widthScaling);
		int h = (int) (30 * heightScaling);
		int fontsize = (int) (12 * heightScaling);
		int r = 9, c = 9, xl = 3, yl = 3;
	    JLabel rowLabel = new JLabel("Broj redaka mreže: ");
	    rowLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    rowLabel.setBounds(x, y, w, h);
	    frame.add(rowLabel);
	    
	    JTextField row = new JTextField(String.valueOf(r));
	    row.setFont(new Font("Arial", Font.PLAIN, fontsize));
        row.setBounds(x + w, y, w / 4, h);
	    frame.add(row);

	    JLabel xLimLabel = new JLabel("Broj redaka kutije: ");
	    xLimLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    xLimLabel.setBounds(x, y + h, w, h);
	    frame.add(xLimLabel);
	    
	    JTextField xLimVal = new JTextField(String.valueOf(xl));
	    xLimVal.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    xLimVal.setBounds(x + w, y + h, w / 4, h);
	    frame.add(xLimVal);

	    JLabel colLabel = new JLabel("Broj stupaca mreže: ");
	    colLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    colLabel.setBounds(x, y + h * 2, w, h);
	    frame.add(colLabel);
	    
	    JTextField col = new JTextField(String.valueOf(c));
	    col.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    col.setBounds(x + w, y + h * 2, w / 4, h);
	    frame.add(col);

	    JLabel yLimLabel = new JLabel("Broj stupaca kutije: ");
	    yLimLabel.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    yLimLabel.setBounds(x, y + h * 3, w, h);
	    frame.add(yLimLabel);
	    
	    JTextField yLimVal = new JTextField(String.valueOf(yl));
	    yLimVal.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    yLimVal.setBounds(x + w, y + h * 3, w / 4, h);
	    frame.add(yLimVal);
	    
	    JButton createButton = new JButton("Stvori sudoku");
	    createButton.setFont(new Font("Arial", Font.PLAIN, fontsize));
	    createButton.setBounds(x, y + h * 9 / 2, w * 5 / 4, h);
	    frame.add(createButton);

	    createButton.addActionListener(new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (Integer.parseInt(row.getText()) == Integer.parseInt(col.getText()) && Integer.parseInt(yLimVal.getText()) * Integer.parseInt(xLimVal.getText()) == Integer.parseInt(row.getText())) {
		        		ChangeBoxBorder b = new ChangeBoxBorder(Integer.parseInt(row.getText()), Integer.parseInt(col.getText()), Integer.parseInt(yLimVal.getText()), Integer.parseInt(xLimVal.getText()));
	        		} else {
	        			if (Integer.parseInt(row.getText()) != Integer.parseInt(col.getText())) {
	        				//System.out.println("Zagonetka nije kvadratna.");
	        				return;
	        			}
	        			if (Integer.parseInt(yLimVal.getText()) * Integer.parseInt(xLimVal.getText()) > Integer.parseInt(row.getText())) {
	        				//System.out.println("Kutije imaju previše znamenki.");
	        				return;
	        			}
	        			if (Integer.parseInt(yLimVal.getText()) * Integer.parseInt(xLimVal.getText()) < Integer.parseInt(row.getText())) {
	        				//System.out.println("Kutije imaju premalo znamenki.");
	        				return;
	        			}
	        		}
				} catch (Exception e1) {

				}
	        }  
	    });
	    frame.setSize(w * 5 / 3, h * 8);  
	    frame.setLayout(null);  
		frame.setVisible(true);
	}
}
