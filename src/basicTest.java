import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

class basicTest {

	@Test
	void testCase() throws FileNotFoundException {
		for (int i = 1; i < 15; i++) {
			  String filename = "src/solutions/solution" + String.valueOf(i) + ".txt";
			  File myObj = new File(filename);
			  java.util.Scanner myReader = new java.util.Scanner(myObj);
			  String correctSolution = "";
			  while (myReader.hasNextLine()) {
				  correctSolution += myReader.nextLine();
			  }
			  ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(9, 9, 3, 3, false);
			  FileManipulator fileManipulator = new FileManipulator(); 
			  CreateSudoku createSudoku = new CreateSudoku(changeBoxBorder.rows, changeBoxBorder.cols, changeBoxBorder.xLim, changeBoxBorder.yLim, changeBoxBorder.border, changeBoxBorder.boxNumber, changeBoxBorder.diagonalOn, new HashSet<String>(), changeBoxBorder.userInput, fileManipulator);
			  createSudoku.frame.setVisible(false);
			  fileManipulator.setSudoku(createSudoku);
			  fileManipulator.ReadFile("src/testCases/testCase" + String.valueOf(i) + ".txt");
			  createSudoku.checkBoxes();
			  createSudoku.isOnlyOneSolution();
			  String solution = "";
			  for (int row = 0; row < createSudoku.rows; row++){
				  for (int col = 0; col < createSudoku.cols; col++) {
					  solution += String.valueOf(createSudoku.temporary[row * createSudoku.cols + col]);
				  }
			  }
		      assertEquals(solution, correctSolution);
			  myReader.close(); 
		}
	}
	

	@Test
	void testCaseBoxes() throws FileNotFoundException {
		for (int i = 1; i < 3; i++) {
			  String filename = "src/solutions/solutionBoxes" + String.valueOf(i) + ".txt";
			  File myObj = new File(filename);
			  java.util.Scanner myReader = new java.util.Scanner(myObj);
			  String correctSolution = "";
			  while (myReader.hasNextLine()) {
				  correctSolution += myReader.nextLine();
			  }
			  ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(9, 9, 3, 3, false);
			  FileManipulator fileManipulator = new FileManipulator(); 
			  CreateSudoku createSudoku = new CreateSudoku(changeBoxBorder.rows, changeBoxBorder.cols, changeBoxBorder.xLim, changeBoxBorder.yLim, changeBoxBorder.border, changeBoxBorder.boxNumber, changeBoxBorder.diagonalOn, new HashSet<String>(), changeBoxBorder.userInput, fileManipulator);
			  createSudoku.frame.setVisible(false);
			  fileManipulator.setSudoku(createSudoku);
			  fileManipulator.ReadFile("src/testCases/testCaseBoxes" + String.valueOf(i) + ".txt");
			  createSudoku.checkBoxes();
			  createSudoku.isOnlyOneSolution();
			  String solution = "";
			  for (int row = 0; row < createSudoku.rows; row++){
				  for (int col = 0; col < createSudoku.cols; col++) {
					  solution += String.valueOf(createSudoku.temporary[row * createSudoku.cols + col]);
				  }
			  }
		      assertEquals(solution, correctSolution);
			  myReader.close(); 
		}
	}


	@Test
	void testCaseBoxesDiagonal() throws FileNotFoundException {
		for (int i = 1; i < 3; i++) {
			  String filename = "src/solutions/solutionBoxesDiagonal" + String.valueOf(i) + ".txt";
			  File myObj = new File(filename);
			  java.util.Scanner myReader = new java.util.Scanner(myObj);
			  String correctSolution = "";
			  while (myReader.hasNextLine()) {
				  correctSolution += myReader.nextLine();
			  }
			  ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(9, 9, 3, 3, false);
			  FileManipulator fileManipulator = new FileManipulator(); 
			  CreateSudoku createSudoku = new CreateSudoku(changeBoxBorder.rows, changeBoxBorder.cols, changeBoxBorder.xLim, changeBoxBorder.yLim, changeBoxBorder.border, changeBoxBorder.boxNumber, changeBoxBorder.diagonalOn, new HashSet<String>(), changeBoxBorder.userInput, fileManipulator);
			  createSudoku.frame.setVisible(false);
			  fileManipulator.setSudoku(createSudoku);
			  fileManipulator.ReadFile("src/testCases/testCaseBoxesDiagonal" + String.valueOf(i) + ".txt");
			  createSudoku.checkBoxes();
			  createSudoku.isOnlyOneSolution();
			  String solution = "";
			  for (int row = 0; row < createSudoku.rows; row++){
				  for (int col = 0; col < createSudoku.cols; col++) {
					  solution += String.valueOf(createSudoku.temporary[row * createSudoku.cols + col]);
				  }
			  }
		      assertEquals(solution, correctSolution);
			  myReader.close(); 
		}
	}
}
