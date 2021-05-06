import javax.swing.JOptionPane;

public interface InformationBox {
	public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
	
	public static boolean stepBox(String infoMessage, String titleBar)
    {
		Object[] optionsForDialog = {"Iduæi korak", "Prekini"};
		int dialogResult = JOptionPane.showOptionDialog (null, infoMessage,titleBar,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,     //do not use a custom Icon
				optionsForDialog,  //the titles of buttons
				optionsForDialog[0]); //default button title
		if(dialogResult == JOptionPane.YES_OPTION){
			return true;
		} else {
			return false;
		}
    }
	
	public static boolean yesNoBox(String infoMessage, String titleBar)
    {
		Object[] optionsForDialog = {"Da", "Ne"};
		int dialogResult = JOptionPane.showOptionDialog (null, infoMessage,titleBar,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,     //do not use a custom Icon
				optionsForDialog,  //the titles of buttons
				optionsForDialog[0]); //default button title
		if(dialogResult == JOptionPane.YES_OPTION){
			return true;
		} else {
			return false;
		}
    }
}
