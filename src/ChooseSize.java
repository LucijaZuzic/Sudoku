import java.awt.Color;
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

	    int x = 15;
		int y = 15;
		int w = 130;
		int h = 30;
		int r = 9, c = 9, xl = 3, yl = 3;
	    JLabel rowLabel = new JLabel("Broj redaka mreže: ");
	    rowLabel.setBounds(x, y, w, h);
	    frame.add(rowLabel);
	    
	    JTextField row = new JTextField(String.valueOf(r));
        row.setBounds(x + w, y, w / 4, h);
	    frame.add(row);

	    JLabel xlimLabel = new JLabel("Broj redaka kutije: ");
	    xlimLabel.setBounds(x, y + h, w, h);
	    frame.add(xlimLabel);
	    
	    JTextField xlimval = new JTextField(String.valueOf(xl));
	    xlimval.setBounds(x + w, y + h, w / 4, h);
	    frame.add(xlimval);

	    JLabel colLabel = new JLabel("Broj stupaca mreže: ");
	    colLabel.setBounds(x, y + h * 2, w, h);
	    frame.add(colLabel);
	    
	    JTextField col = new JTextField(String.valueOf(c));
	    col.setBounds(x + w, y + h * 2, w / 4, h);
	    frame.add(col);

	    JLabel ylimLabel = new JLabel("Broj stupaca kutije: ");
	    ylimLabel.setBounds(x, y + h * 3, w, h);
	    frame.add(ylimLabel);
	    
	    JTextField ylimval = new JTextField(String.valueOf(yl));
	    ylimval.setBounds(x + w, y + h * 3, w / 4, h);
	    frame.add(ylimval);
	    
	    JButton createb = new JButton("Stvori sudoku");
	    createb.setBounds(x, y + h * 9 / 2, w * 5 / 4, h);
	    frame.add(createb);

	    createb.addActionListener(new ActionListener(){  
	        public void actionPerformed(ActionEvent e) {  
	        	try {
	        		if (Integer.parseInt(row.getText()) == Integer.parseInt(col.getText()) && Integer.parseInt(ylimval.getText()) * Integer.parseInt(xlimval.getText()) == Integer.parseInt(row.getText())) {
		        		ChangeBoxBorder b = new ChangeBoxBorder(Integer.parseInt(row.getText()), Integer.parseInt(col.getText()), Integer.parseInt(ylimval.getText()), Integer.parseInt(xlimval.getText()));
	        		} else {
	        			if (Integer.parseInt(row.getText()) != Integer.parseInt(col.getText())) {
	        				System.out.println("Zagonetka nije kvadratna.");
	        				return;
	        			}
	        			if (Integer.parseInt(ylimval.getText()) * Integer.parseInt(xlimval.getText()) > Integer.parseInt(row.getText())) {
	        				System.out.println("Kutije imaju previše znamenki.");
	        				return;
	        			}
	        			if (Integer.parseInt(ylimval.getText()) * Integer.parseInt(xlimval.getText()) < Integer.parseInt(row.getText())) {
	        				System.out.println("Kutije imaju premalo znamenki.");
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
