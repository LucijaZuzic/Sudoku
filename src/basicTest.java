import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

class basicTest {

	String getCorrectSolution(String filename) throws FileNotFoundException {
		  File myObj = new File(filename);
		  java.util.Scanner myReader = new java.util.Scanner(myObj);
		  String correctSolution = "";
		  while (myReader.hasNextLine()) {
			  correctSolution += myReader.nextLine();
		  }
			  myReader.close(); 
		  return correctSolution;
	}
	
	String getCreateSolution(int size, String filename) {
		  ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(size, size, 1, size, false);
		  changeBoxBorder.readFile(filename);
		  CreateSudoku createSudoku = new CreateSudoku(changeBoxBorder.rows, changeBoxBorder.cols, changeBoxBorder.xLim, changeBoxBorder.yLim, changeBoxBorder.border, changeBoxBorder.boxNumber, changeBoxBorder.diagonalOn, changeBoxBorder.sizeRelationships, changeBoxBorder.userInput, changeBoxBorder.lastUsedPath);
		  createSudoku.frame.setVisible(false);
		  createSudoku.checkBoxes();
		  createSudoku.isOnlyOneSolution();
		  String solution = "";
		  for (int row = 0; row < createSudoku.rows; row++){
			  for (int col = 0; col < createSudoku.cols; col++) {
				  solution += String.valueOf(createSudoku.temporary[row * createSudoku.cols + col]);
			  }
		  } 
		  createSudoku.writeToFile(filename);
		  return solution;
	}
	
	String getSolveSolution(int size, String filename) {
		  ChangeBoxBorder changeBoxBorder = new ChangeBoxBorder(size, size, 1, size, false);
		  changeBoxBorder.readFile(filename);
		  SolveSudoku solveSudoku = new SolveSudoku(changeBoxBorder.rows, changeBoxBorder.cols, changeBoxBorder.xLim, changeBoxBorder.yLim, changeBoxBorder.border, changeBoxBorder.boxNumber, changeBoxBorder.diagonalOn, changeBoxBorder.sizeRelationships, changeBoxBorder.userInput);
		  solveSudoku.frame.setVisible(false);
		  solveSudoku.checkBoxes();
		  solveSudoku.isOnlyOneSolution();
		  String solution = "";
		  for (int row = 0; row < solveSudoku.rows; row++){
			  for (int col = 0; col < solveSudoku.cols; col++) {
				  solution += String.valueOf(solveSudoku.temporary[row * solveSudoku.cols + col]);
			  }
		  } 
		  solveSudoku.writeToFile(filename);
		  return solution;
	}
	
	@Test
	void testCase() throws FileNotFoundException {
		for (int i = 1; i < 15; i++) {
			  String solutionFilename = "src/solutions/solution" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCase" + String.valueOf(i) + ".txt";
		      assertEquals(getCreateSolution(9, testFilename), getCorrectSolution(solutionFilename));
		      assertEquals(getSolveSolution(9, testFilename), getCorrectSolution(solutionFilename));
		}
	}
	
	@Test
	void testCaseBoxes() throws FileNotFoundException {
		for (int i = 1; i < 3; i++) {
			  String solutionFilename = "src/solutions/solutionBoxes" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseBoxes" + String.valueOf(i) + ".txt";
		      assertEquals(getCreateSolution(9, testFilename), getCorrectSolution(solutionFilename));
		      assertEquals(getSolveSolution(9, testFilename), getCorrectSolution(solutionFilename));
		}
	}
	
	@Test
	void testCaseBoxesDiagonal() throws FileNotFoundException {
		for (int i = 1; i < 3; i++) {
			  String solutionFilename = "src/solutions/solutionBoxesDiagonal" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseBoxesDiagonal" + String.valueOf(i) + ".txt";
		      assertEquals(getCreateSolution(9, testFilename), getCorrectSolution(solutionFilename));
		      assertEquals(getSolveSolution(9, testFilename), getCorrectSolution(solutionFilename));
		}
	}
	
	@Test
	void testCaseSix() throws FileNotFoundException {
		for (int i = 1; i < 4; i++) {
			  String solutionFilename = "src/solutions/solutionSix" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseSix" + String.valueOf(i) + ".txt";
		      assertEquals(getCreateSolution(6, testFilename), getCorrectSolution(solutionFilename)); 
		      assertEquals(getSolveSolution(6, testFilename), getCorrectSolution(solutionFilename));
		}
	}
	
	@Test
	void testCaseLargerSmaller() throws FileNotFoundException {
		for (int i = 1; i < 12; i++) {
			  String solutionFilename = "src/solutions/solutionLargerSmaller" + String.valueOf(i) + ".txt";
			  String testFilename = "src/testCases/testCaseLargerSmaller" + String.valueOf(i) + ".txt";
		      assertEquals(getCreateSolution(9, testFilename), getCorrectSolution(solutionFilename)); 
		      assertEquals(getSolveSolution(9, testFilename), getCorrectSolution(solutionFilename));
		}
	}
}
